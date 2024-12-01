import characters.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class MafiaServer {
    private static final int PORT = 12345;
    private static final int MAX_PLAYERS = 4;
    public List<ClientHandler> clients = new ArrayList<>();
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
            waitForNicknames();
            System.out.println("모든 플레이어가 연결되었습니다. 게임을 시작합니다.");
            assignTeamsAndCharacters();
            startGame();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 모든 클라이언트의 닉네임 입력 완료 대기
    private void waitForNicknames() {
        while (true) {
            boolean allNicknamesSet = clients.stream().allMatch(ClientHandler::isNicknameSet);
            if (allNicknamesSet) {
                System.out.println("모든 플레이어 닉네임 입력 완료!");
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
                client.sendMessage("당신은 " + teams[i] + "팀입니다.");
                client.sendMessage("캐릭터: " + characterClass.getSimpleName() + " - 능력: " + character.getInfo());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startGame() {
        while (true) {
            // 라운드 시작 브로드캐스트
            broadcast("===== 라운드 " + currentRound + " 시작 =====");

            // 모든 플레이어의 턴 진행
            for (int i = 0; i < clients.size(); i++) {
                ClientHandler currentPlayer = clients.get(currentTurnIndex);

                // 턴 시작 브로드캐스트
                broadcast("현재 턴: " + currentPlayer.getNickname() + "의 턴입니다.");
                currentPlayer.startTurn();

                currentTurnIndex = (currentTurnIndex + 1) % clients.size();

                if (checkGameOver()) {
                    endGame();
                    return;
                }
            }
            broadcast("라운드가 종료되었습니다. 다음 라운드를 준비합니다.");

            currentRound++; // 라운드 증가
        }
    }

    public void handleShoot(ClientHandler shooter, String targetNickname) {
        ClientHandler target = clients.stream()
                .filter(client -> client.getNickname().equals(targetNickname))
                .findFirst()
                .orElse(null);

        if (target == null) {
            sendResponse(shooter, new ServerResponse("shoot", shooter.getNickname(), targetNickname, "miss", 0, "타겟 플레이어를 찾을 수 없습니다."));
            return;
        }

        boolean hit = gun.fire();
        int targetHealth = target.getCharacter().getHealth();

        if (hit) {
            target.getCharacter().receiveDamage();
            targetHealth--;
        }

        sendResponse(shooter, new ServerResponse("shoot", shooter.getNickname(), targetNickname, hit ? "hit" : "miss", targetHealth,
                hit ? targetNickname + "에게 데미지를 입혔습니다." : targetNickname + "을(를) 빗맞췄습니다."));

        if (hit) {
            sendResponse(target, new ServerResponse("hit", shooter.getNickname(), targetNickname, "hit", targetHealth,
                    shooter.getNickname() + "에게 총을 맞았습니다. 체력이 1 감소합니다."));
        }

        broadcast(shooter.getNickname() + "이(가) " + targetNickname + "을(를) " + (hit ? "적중시켰습니다!" : "빗맞췄습니다!"));

        if (!target.getCharacter().isAlive()) {
            broadcast(targetNickname + "은(는) 사망했습니다.");
        }
    }

    private void sendResponse(ClientHandler client, ServerResponse response) {
        client.sendMessage(JsonUtil.responseToJson(response));
    }

    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private boolean checkGameOver() {
        long aliveCount = clients.stream()
                .filter(client -> client.getCharacter().isAlive())
                .count();
        return aliveCount <= 1;
    }

    private void endGame() {
        broadcast("게임이 종료되었습니다!");
        String winner = clients.stream()
                .filter(client -> client.getCharacter().isAlive())
                .map(ClientHandler::getNickname)
                .findFirst()
                .orElse("없음");
        broadcast(winner + "이(가) 승리했습니다!");

        startVote();

        for (ClientHandler client : clients) {
            client.sendMessage("게임이 종료되었습니다. 연결을 종료합니다.");
            client.closeConnection();
        }
    }

    private void startVote() {
        broadcast("MVP 투표를 시작합니다!");
        String playerStr = "";
        for(int i=0; i<clients.size(); i++) {
            playerStr += (i + 1 + ") " + clients.get(i).getNickname() + "    ");
        }
        broadcast(playerStr);

        for(int i=0; i<clients.size(); i++) {
            clients.get(i).votePlayer();
        }
        voteCount();
    }

    private void voteCount() {
        while (true) {
            boolean allVoteCompleted = clients.stream().allMatch(ClientHandler::isVoteCompleted);
            if (allVoteCompleted) {
                System.out.println("모든 플레이어 MVP 투표 완료!");
                broadcast("모든 플레이어의 투표가 완료되었습니다!");

                ArrayList<Integer> votes = new ArrayList<>();
                for (int i = 0; i < clients.size(); i++) {
                    votes.add(0); // 초기값은 0
                }
                for (int i = 0; i < clients.size(); i++) {
                    votes.set(clients.get(i).getVoteNum()-1, votes.get(clients.get(i).getVoteNum()-1) + 1);
                }
                int maxIndex = votes.indexOf(Collections.max(votes));
                String mvpPlayer = clients.get(maxIndex).getNickname();
                broadcast("투표 결과 MVP 플레이어는 " + mvpPlayer + "로 선정되었습니다!");

                break;
            }
        }
    }
}
