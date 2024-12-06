package server;

import characters.*;
import resources.Gun;

import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MafiaServer {
    private static final int PORT = 12345;
    private static final int MAX_PLAYERS = 4;
    public static List<ClientHandler> clients = new ArrayList<>();
    private int currentTurnIndex = 0;
    private int currentRound = 1; // 현재 라운드
    private Gun gun = new Gun();

    // 캐릭터 클래스 리스트
    private List<Class<? extends CharacterTemplate>> characterClasses = Arrays.asList(
            Character1.class, Character2.class, Character3.class, Character4.class,
            Character5.class, Character6.class
    );

    public static void main(String[] args) {
        new MafiaServer().startServer();
    }

    public void startServer() {
        System.out.println("서버가 시작되었습니다.");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (clients.size() < MAX_PLAYERS) {
                Socket socket = serverSocket.accept();
                System.out.println("새 클라이언트가 연결되었습니다.");
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
            waitForReady();
            System.out.println("모든 플레이어가 연결되었습니다. 게임을 시작합니다.");
            assignTeamsAndCharacters();
            startGame();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 모든 클라이언트의 닉네임 입력 완료 대기
    private void waitForReady() {
        while (true) {
            boolean allPlayersReady = clients.stream().allMatch(ClientHandler::isReady);
            if (allPlayersReady) {
                System.out.println("모든 플레이어 준비 완료!");
                break;
            }
            try {
                Thread.sleep(500); // 0.5초 대기
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 팀과 캐릭터 할당
    private void assignTeamsAndCharacters() {
        // 팀 무작위로 섞기
        String[] teams = {"A", "A", "B", "B"};
        Collections.shuffle(Arrays.asList(teams));

        // 캐릭터 클래스 무작위로 섞기
        Collections.shuffle(characterClasses);

        for (int i = 0; i < clients.size(); i++) {
            ClientHandler client = clients.get(i);

            try {
                // 캐릭터 클래스에서 하나 선택 후 인스턴스 생성
                Class<? extends CharacterTemplate> characterClass = characterClasses.get(i);
                CharacterTemplate character = characterClass.getConstructor(String.class, String.class)
                        .newInstance(client.getNickname(), teams[i]);

                client.setCharacter(character);
                client.setTeam(teams[i]);

                // 클라이언트에게 할당 정보 전달
//                client.sendMessage("당신은 " + teams[i] + "팀입니다.");
//                client.sendMessage("캐릭터: " + characterClass.getSimpleName() + " - 능력: " + character.getInfo());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startGame() {
        while (true) {
            // 라운드 시작 브로드캐스트
//            broadcast("===== 라운드 " + currentRound + " 시작 =====");

            // 모든 플레이어의 턴 진행
            for (int i = 0; i < clients.size(); i++) {
                ClientHandler currentPlayer = clients.get(currentTurnIndex);

                if(!currentPlayer.getCharacter().isAlive()){
                    System.out.println(currentPlayer.getNickname() + "은(는) 사망했습니다. 턴을 건너뜁니다.");
                    currentTurnIndex = (currentTurnIndex + 1) % clients.size();
                    continue;
                }

                // 게임 상태 전송
                sendGameStateToClients();

                // 턴 시작 브로드캐스트
                currentPlayer.startTurn();

                currentTurnIndex = (currentTurnIndex + 1) % clients.size();

                if (checkGameOver()) {
                    endGame();
                    return;
                }

            }
//            broadcast("라운드가 종료되었습니다. 다음 라운드를 준비합니다.");

            currentRound++; // 라운드 증가
        }
    }

    private void sendGameStateToClients() {
        List<CharacterTemplate> characters = clients.stream()
                .map(ClientHandler::getCharacter)
                .collect(Collectors.toList());

        List<Boolean> chambers = gun.getChambers();

        int currentPlayerIndex = currentTurnIndex;
        int roundNumber = currentRound;

        clients.forEach(client -> client.sendResponse(new ServerResponse("updateGameState", "게임 상태 업데이트", characters, chambers, roundNumber, currentPlayerIndex)));
    }

    public ServerResponse handleUseAbility(ClientHandler user) {
        CharacterTemplate character = user.getCharacter();

        if (character == null) {
            return new ServerResponse("error", "캐릭터가 설정되지 않았습니다.", null, null, currentRound, currentTurnIndex);
        }
        System.out.println("Charater nickname: "+character.getName());
        try {
            String ablilitymessage = character.useAbility(); // useAbility에는 argument가 존재하지 않음
            return new ServerResponse("useAbility", ablilitymessage, collectCharacters(), Gun.getChambers(), currentRound, currentTurnIndex);
        } catch (Exception e) {
            return new ServerResponse("error", "능력 사용 중 오류 발생: " + e.getMessage(), null, null, currentRound, currentTurnIndex);
        }
    }


    // todo 격발 여부 모든 유저에게 알림 필요
    public ServerResponse handleShoot(ClientHandler shooter, String targetNickname) {
        ClientHandler target = clients.stream()
                .filter(client -> client.getNickname().equals(targetNickname))
                .findFirst()
                .orElse(null);

        if (target == null) {
            return new ServerResponse("error", "타겟 플레이어를 찾을 수 없습니다.", null, null, currentRound, currentTurnIndex);
        }

        boolean hit = gun.fire();
        String action;
        String message;

        if (hit) {
            target.getCharacter().receiveDamage();
            action = "shoot";
            message = shooter.getNickname() + "이(가) " + targetNickname + "을(를) 적중시켰습니다!";
        } else {
            action = "miss";
            message = shooter.getNickname() + "이(가) " + targetNickname + "을(를) 빗맞췄습니다!";
        }

        ServerResponse response = new ServerResponse(action, message, collectCharacters(), Gun.getChambers(), currentRound, currentTurnIndex);

        for (ClientHandler client : clients) {
            if (!client.equals(shooter)) { // 제외할 클라이언트 검사
                client.sendResponse(response);
            }
        }

        return response;

    }

    private List<CharacterTemplate> collectCharacters() {
        return clients.stream()
                .map(ClientHandler::getCharacter)
                .collect(Collectors.toList());
    }


    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private boolean checkGameOver() {
        boolean isTeamAAlive = clients.stream()
                .anyMatch(client -> client.getCharacter().isAlive() && "A".equals(client.getCharacter().getTeam()));

        boolean isTeamBAlive = clients.stream()
                .anyMatch(client -> client.getCharacter().isAlive() && "B".equals(client.getCharacter().getTeam()));

        // A팀 또는 B팀 중 한 팀이 전멸하면 true 반환
        return !(isTeamAAlive && isTeamBAlive);
    }

    private void endGame() {
        broadcast("게임이 종료되었습니다!");

        boolean isTeamAAlive = clients.stream()
                .anyMatch(client -> client.getCharacter().isAlive() && "A".equals(client.getCharacter().getTeam()));

        boolean isTeamBAlive = clients.stream()
                .anyMatch(client -> client.getCharacter().isAlive() && "B".equals(client.getCharacter().getTeam()));

        String winner;
        if (isTeamAAlive && !isTeamBAlive) {
            winner = "A";
        } else if (isTeamBAlive && !isTeamAAlive) {
            winner = "B";
        } else {
            winner = "없음"; // 모든 팀이 전멸한 경우
        }

        broadcast(winner + "팀이 승리했습니다!");

        startVote();
        String mvpPlayer = voteCount();
        writeHistory(winner, mvpPlayer);

        for (ClientHandler client : clients) {
            client.sendResponse(new ServerResponse()); // client에 end response 전달
            client.sendMessage("게임이 종료되었습니다. 연결을 종료합니다.");
            client.closeConnection();
        }
    }

    private void startVote() {
        broadcast("MVP 투표를 시작합니다!");

        for (ClientHandler client : clients) {
            client.sendResponse(new ServerResponse("voteStart")); // client에 투표 전달
        }
        for (ClientHandler client : clients) {
            client.votePlayer();
        }
    }

    private String voteCount() {
        while (true) {
            boolean allVoteCompleted = clients.stream().allMatch(ClientHandler::isVoteCompleted);
            if (allVoteCompleted) {
                broadcast("모든 플레이어의 투표가 완료되었습니다!");

                Map<String, Integer> votes = new HashMap<>();
                clients.forEach(client -> {
                    votes.put(client.getNickname(), 0);
                });
                clients.forEach(client -> {
                    votes.put(client.getVote(), votes.get(client.getVote()) + 1);
                });

                List<String> keys = new ArrayList<>(votes.keySet());
                Collections.sort(keys, (v1, v2) -> (votes.get(v2).compareTo(votes.get(v1))));

                String mvpPlayer = keys.get(0);

                for (String key : keys) {
                    System.out.print("Key : " + key);
                    System.out.println(", Val : " + votes.get(key));
                }
                System.out.println("투표 결과 MVP 플레이어는 " + mvpPlayer + "로 선정되었습니다!");

                broadcast("투표 결과 MVP 플레이어는 " + mvpPlayer + "로 선정되었습니다!");
                for (ClientHandler client : clients) {
                    client.sendResponse(new ServerResponse("voteEnd", mvpPlayer)); // client에 투표 전달
                }

                return mvpPlayer;
            }
        }
    }

    private void writeHistory(String winningTeam, String mvpPlayer) {
        String filepath = "src/history.txt";
        String history = "";
        List<String> teamA = new ArrayList<>();
        List<String> teamB = new ArrayList<>();
        for (int i = 0; i < clients.size(); i++) {
            if(clients.get(i).getTeam().equals("A")){
                teamA.add(clients.get(i).getNickname());
            } else {
                teamB.add(clients.get(i).getNickname());
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(filepath, true))){
            // 현재 날짜 가져오기
            LocalDate currentDate = LocalDate.now();
            // yy-MM-dd 형식의 포매터 정의
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd");
            // 포맷팅된 문자열로 변환
            String formattedDate = currentDate.format(formatter);
            history += formattedDate + " " + teamA.get(0) + " " + teamA.get(1) + " " + teamB.get(0) + " " + teamB.get(1) + " " + winningTeam + " " + mvpPlayer;
            pw.println(history);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
