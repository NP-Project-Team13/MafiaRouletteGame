package server;

import characters.*;
import client.ClientAction;
import resources.Gun;
import resources.JsonUtil;

import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static client.MainMenu.showCharacterDescriptions;

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
            broadcast("---------------------------------- 라운드 " + currentRound + " 시작 ----------------------------------");
            // 모든 플레이어의 턴 진행
            for (int i = 0; i < clients.size(); i++) {
                ClientHandler currentPlayer = clients.get(currentTurnIndex);

                if(!currentPlayer.getCharacter().isAlive()){
                    System.out.println(currentPlayer.getNickname() + "님은 사망했습니다. 턴을 건너뜁니다.");
                    currentTurnIndex = (currentTurnIndex + 1) % clients.size();
                    continue;
                }

                // 게임 상태 전송
                sendGameStateToClients();

                // 턴 시작 브로드캐스트
                currentPlayer.startTurn();

                // 현재 턴 플레이어가 요청을 처리할 시간을 제공
                waitForPlayerTurn(currentPlayer);
                sendGameStateToClients();
                System.out.println(currentPlayer.getNickname()+"의 턴 종료");

                if (checkGameOver()) {
                    endGame();
                    return;
                }

            }

            System.out.println("라운드 종료");
            // 라운드 종료 처리 및 resetRound 호출
            
            clients.forEach(client -> {
                    String resetMessage = client.getCharacter().resetRound(); // resetRound 결과 받기
                    client.sendMessage(resetMessage);
            });

            // 라운드 증가
            currentRound++;
            System.out.println("라운드 " + currentRound + " 종료");
        }

    }

    private void sendGameStateToClients() {
        List<CharacterTemplate> characters = clients.stream()
                .map(ClientHandler::getCharacter)
                .collect(Collectors.toList());

        List<Boolean> chambers = Gun.getChambers();

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

    /**
     * 현재 턴 플레이어의 요청 처리 대기
     */
    private void waitForPlayerTurn(ClientHandler currentPlayer) {
        while (true) {
            if (!isCurrentTurn(currentPlayer)) {
                break; // 턴 종료되면 루프 탈출
            }
            try{
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 현재 턴 종료 처리
     */
    public void endCurrentTurn() {
        currentTurnIndex = (currentTurnIndex + 1) % clients.size();
    }


    // todo 격발 여부 모든 유저에게 알림 필요
    public ServerResponse handleShoot(ClientHandler shooter, String targetNickname) {
        ClientHandler target = clients.stream()
                .filter(client -> client.getNickname().equals(targetNickname))
                .findFirst()
                .orElse(null);

        // shooter가 Character6이고, 같은 팀에 죽은 플레이어가 있는지 확인
        if (shooter.getCharacter() instanceof Character6) {
            boolean isTeammateDead = clients.stream()
                    .filter(client -> client != shooter) // shooter 자신 제외
                    .anyMatch(client -> client.getTeam().equals(shooter.getTeam()) && !client.getCharacter().isAlive());

            if (isTeammateDead) {
                // resetRound 호출 및 결과 메시지 추가
                shooter.getCharacter().resetRound();
            }
        }


        if (target == null) {
            return new ServerResponse("error", "타겟 플레이어를 찾을 수 없습니다.", null, null, currentRound, currentTurnIndex);
        }

        boolean hit = gun.fire();
        String action;
        String message;

        if (hit) {
            action = "shoot";
            message = shooter.getCharacter().shoot(target.getCharacter());
        } else {
            action = "miss";

            // target의 캐릭터가 Character6 타입인지 확인
            if ((shooter.getCharacter() instanceof Character6) && ((Character6) shooter.getCharacter()).isReady()) {
                message = shooter.getNickname() + "님이 " + targetNickname + "님의 체력을 증가시켜주려고 했으나, 총알이 없어 무효처리 됩니다.";
            } else {
                message = shooter.getNickname() + "님이 " + targetNickname + "님을 빗맞췄습니다!❌";
            }
        }

        ServerResponse response = new ServerResponse(action, message, collectCharacters(), Gun.getChambers(), currentRound, currentTurnIndex);

        for (ClientHandler client : clients) {
            if (!client.equals(shooter)) { // 제외할 클라이언트 검사
                client.sendResponse(response);
            }
        }

        return response;
    }

    public boolean isCurrentTurn(ClientHandler client) {
        return clients.get(currentTurnIndex).equals(client);
    }

    private List<CharacterTemplate> collectCharacters() {
        return clients.stream()
                .map(ClientHandler::getCharacter)
                .collect(Collectors.toList());
    }


    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage("📣 " + message);
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

        // MVP 투표 시작 ~ 종료
        startVote();
        String mvpPlayer = voteCount();
        writeHistory(winner, mvpPlayer);

        for (ClientHandler client : clients) {
            client.sendResponse(new ServerResponse()); // client에 end response 전달
            client.sendMessage("게임이 종료되었습니다. 연결을 종료합니다.");
            client.closeConnection();
        }
    }

    // MVP 투표 시작 후 모든 플레이어 투표 완료 시까지 대기
    private void startVote() {
        broadcast("MVP 투표를 시작합니다!");

        for (ClientHandler client : clients) {
            client.sendResponse(new ServerResponse("voteStart")); // client에 투표 전달
        }

        broadcast("투표 대기중");

        // 모든 클라이언트의 투표가 완료될 때까지 대기
        while (true) {
            boolean allVotesCompleted = clients.stream().allMatch(ClientHandler::isVoteCompleted);
            if (allVotesCompleted) {
                broadcast("모든 플레이어가 투표를 완료했습니다!");
                break;
            }

            try {
                Thread.sleep(500); // 0.5초 간격으로 투표 상태 확인
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // MVP 투표 결과 집계
    private String voteCount() {
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
        System.out.println("투표 결과 MVP 플레이어는 " + mvpPlayer + "님으로 선정되었습니다!");

        broadcast("투표 결과 MVP 플레이어는 " + mvpPlayer + "님으로 선정되었습니다!");
        for (ClientHandler client : clients) {
            client.sendResponse(new ServerResponse("voteEnd", mvpPlayer)); // client에 투표 전달
        }

        return mvpPlayer;
    }

    // 텍스트 파일에 게임 결과 작성
    private void writeHistory(String winningTeam, String mvpPlayer) {
        String filepath = "src/resources/history.txt";
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
