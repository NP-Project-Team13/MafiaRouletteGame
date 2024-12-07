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

    // 서버 시작
    public void startServer() {
        System.out.println("서버가 시작되었습니다.");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            acceptPlayers(serverSocket); // 클라이언트 연결 대기
            waitForReadyState(); // 모든 클라이언트 준비 상태 확인
            System.out.println("모든 플레이어가 준비되었습니다. 게임을 시작합니다.");
            assignTeamsAndCharacters(); // 팀 및 캐릭터 할당
            startGame(); // 게임 시작
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 클라이언트 연결 처리
    private void acceptPlayers(ServerSocket serverSocket) throws IOException {
        while (clients.size() < MAX_PLAYERS) {
            Socket socket = serverSocket.accept();
            System.out.println("새 클라이언트가 연결되었습니다.");
            ClientHandler clientHandler = new ClientHandler(socket, this);
            clients.add(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    // 모든 플레이어의 준비 상태를 확인
    private void waitForReadyState() {
        while (!clients.stream().allMatch(ClientHandler::isReady)) {
            try {
                Thread.sleep(500); // 0.5초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 게임 루프 시작
    private void startGame() {
        while (true) {
            broadcastMessage("💎💎💎 라운드 " + currentRound + " 시작 💎💎💎");

            for (int i = 0; i < clients.size(); i++) {
                if (processTurn(clients.get(currentTurnIndex))) continue;
            }

            if (checkGameOver()) { // 게임 종료 조건 확인
                endGame();
                break;
            }
            // 각 플레이어의 능력을 초기화
            resetPlayersForNewRound();
            currentRound++; // 다음 라운드로 진행
        }
    }

    // 새로운 라운드를 위해 모든 플레이어의 능력을 초기화
    private void resetPlayersForNewRound() {
        clients.forEach(client -> {
            CharacterTemplate character = client.getCharacter();
            if (character != null && character.isAlive()) {
                String resetMessage = character.resetRound(); // 캐릭터의 resetRound 호출
                client.sendMessage("라운드 초기화: " + resetMessage); // 초기화 결과를 플레이어에게 알림
            }
        });
    }

    // 턴 처리
    private boolean processTurn(ClientHandler currentPlayer) {
        if (!currentPlayer.getCharacter().isAlive()) {
            System.out.println(currentPlayer.getNickname() + "은(는) 사망했습니다. 턴을 건너뜁니다.");
            endCurrentTurn();
            return true; // 턴을 건너뛴 경우
        }

        sendGameStateToClients(); // 현재 게임 상태 전송
        currentPlayer.startTurn(); // 현재 플레이어에게 턴 시작 알림
        waitForPlayerTurnCompletion(currentPlayer); // 플레이어가 턴을 완료할 때까지 대기
        sendGameStateToClients(); // 턴 종료 후 상태 업데이트
        return false;
    }
    // 현재 턴 플레이어 요청 처리 대기
    private void waitForPlayerTurnCompletion(ClientHandler currentPlayer) {
        while (isCurrentTurn(currentPlayer)) {
            try {
                Thread.sleep(100); // 0.1초 간격으로 상태 확인
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    // 현재 턴 종료
    public void endCurrentTurn() {
        currentTurnIndex = (currentTurnIndex + 1) % clients.size(); // 다음 플레이어로 이동
    }

    // 게임 상태를 모든 클라이언트에 전송
    private void sendGameStateToClients() {
        List<CharacterTemplate> characters = collectCharacters();
        List<Boolean> chambers = Gun.getChambers(); // 총 상태 가져오기

        clients.forEach(client -> client.sendResponse(new ServerResponse(
                "updateGameState", "게임 상태 업데이트", characters, chambers, currentRound, currentTurnIndex
        )));
    }
    // 모든 클라이언트에게 메시지 브로드캐스트
    private void broadcastMessage(String message) {
        clients.forEach(client -> client.sendMessage("📣 " + message));
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
            action = "shoot";
            message = shooter.getCharacter().shoot(target.getCharacter());
        } else {
            action = "miss";
            message = shooter.getNickname() + "이(가) " + targetNickname + "을(를) 빗맞췄습니다!❌";
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
        broadcastMessage("게임이 종료되었습니다!");
        String winningTeam = determineWinningTeam();
        broadcastMessage(winningTeam + "팀이 승리했습니다!");

        startVote();
        String mvpPlayer = voteCount();
        writeHistory(winningTeam, mvpPlayer);

        for (ClientHandler client : clients) {
            client.sendResponse(new ServerResponse()); // client에 end response 전달
            client.sendMessage("게임이 종료되었습니다. 연결을 종료합니다.");
            client.closeConnection();
        }
    }

    // 승리 팀 결정
    private String determineWinningTeam() {
        boolean isTeamAAlive = clients.stream()
                .anyMatch(client -> client.getCharacter().isAlive() && "A".equals(client.getCharacter().getTeam()));
        boolean isTeamBAlive = clients.stream()
                .anyMatch(client -> client.getCharacter().isAlive() && "B".equals(client.getCharacter().getTeam()));

        if (isTeamAAlive && !isTeamBAlive) return "A";
        if (isTeamBAlive && !isTeamAAlive) return "B";
        return "무승부"; // 양 팀이 모두 전멸한 경우
    }

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
        System.out.println("투표 결과 MVP 플레이어는 " + mvpPlayer + "로 선정되었습니다!");

        broadcast("투표 결과 MVP 플레이어는 " + mvpPlayer + "로 선정되었습니다!");
        for (ClientHandler client : clients) {
            client.sendResponse(new ServerResponse("voteEnd", mvpPlayer)); // client에 투표 전달
        }

        return mvpPlayer;
    }

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
