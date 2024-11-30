import java.io.*;
import java.net.*;

import characters.Character0;

public class ClientHandler implements Runnable {
    private Socket socket;
    private MafiaServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;
    private Character0 character;
    private String teams;

    public ClientHandler(Socket socket, MafiaServer server) {
        this.socket = socket;
        this.server = server;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // 닉네임 수신
            System.out.println("클라이언트에서 닉네임을 기다리는 중...");
            this.nickname = in.readLine(); // 닉네임 수신
            System.out.println("닉네임 입력 완료: " + this.nickname); // 디버깅용 로그

            // 클라이언트에 대기 메시지 전송
            sendMessage("게임 대기 중...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isNicknameSet() {
        return nickname != null && !nickname.trim().isEmpty();
    }

    public void startTurn() {
        sendMessage("당신의 턴입니다. '총 쏘기' 또는 '능력 사용'을 선택하세요:");
        try {
            String actionJson = in.readLine();
            ClientAction action = JsonUtil.jsonToAction(actionJson);

            if ("shoot".equalsIgnoreCase(action.getAction())) {
                server.handleShoot(this, action.getTarget());
            } else if ("useAbility".equalsIgnoreCase(action.getAction())) {
                // 능력 사용 처리 추가 가능
                handleUseAbility(action.getTarget());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 능력 사용 처리
    private void handleUseAbility(String targetNickname) {
        try {
            if (character == null) {
                sendMessage("캐릭터가 설정되지 않았습니다.");
                return;
            }

            // 타겟을 찾음
            ClientHandler target = server.clients.stream()
                    .filter(client -> client.getNickname().equals(targetNickname))
                    .findFirst()
                    .orElse(null);

            if (target != null) {
                character.useAbility(target.getCharacter());
                sendMessage("능력을 사용했습니다: " + character.getInfo());
            } else {
                character.useAbility();
                sendMessage("능력을 사용했습니다: " + character.getInfo());
            }
        } catch (Exception e) {
            sendMessage("능력 사용에 실패했습니다: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public Character0 getCharacter() {
        return character;
    }

    public void setCharacter(Character0 character) {
        this.character = character;
    }

    public void setTeam(String team) {
        this.teams = team;
    }
}

