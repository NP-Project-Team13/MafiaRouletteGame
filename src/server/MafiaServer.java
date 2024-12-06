package server;

import characters.*;
import resources.Gun;

import java.io.*;
import java.net.*;
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
            Character5.class, Character6.class, Character7.class
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

                // 게임 상태 전송
                sendGameStateToClients();

                // 턴 시작 브로드캐스트
//                broadcast("현재 턴: " + currentPlayer.getNickname() + "의 턴입니다.");
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

    public ServerResponse handleUseAbility(ClientHandler user, String targetNickname) {
        CharacterTemplate character = user.getCharacter();
        if (character == null) {
            return new ServerResponse("error", "캐릭터가 설정되지 않았습니다.", null, null, currentRound, currentTurnIndex);
        }

        ClientHandler target = clients.stream()
                .filter(client -> client.getNickname().equals(targetNickname))
                .findFirst()
                .orElse(null);

        if (target == null && character.getInfo().contains("대상 필요")) {
            return new ServerResponse("error", "타겟 플레이어를 찾을 수 없습니다.", null, null, currentRound, currentTurnIndex);
        }

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

        if (hit) {
            target.getCharacter().receiveDamage();

            for (ClientHandler ch :
                    clients) {
                System.out.print(ch.getCharacter().getHealth() + " ");
            }
            System.out.println();

            return new ServerResponse("shoot",
                    shooter.getNickname() + "이(가) " + targetNickname + "을(를) 적중시켰습니다.",
                    collectCharacters(), gun.getChambers(), currentRound, currentTurnIndex);
        }
        return new ServerResponse("miss",
                shooter.getNickname() + "이(가) " + targetNickname + "을(를) 빗맞췄습니다.",
                collectCharacters(), gun.getChambers(), currentRound, currentTurnIndex);

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
            winner = "A팀";
        } else if (isTeamBAlive && !isTeamAAlive) {
            winner = "B팀";
        } else {
            winner = "없음"; // 모든 팀이 전멸한 경우
        }

        broadcast(winner + "이(가) 승리했습니다!");

        for (ClientHandler client : clients) {
            client.sendResponse(new ServerResponse()); // client에 end response 전달
            client.sendMessage("게임이 종료되었습니다. 연결을 종료합니다.");
            client.closeConnection();
        }
    }
}
