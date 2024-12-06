package server;

import java.io.*;
import java.net.*;

import characters.CharacterTemplate;
import client.ClientAction;
import resources.JsonUtil;

public class ClientHandler implements Runnable {
    private Socket socket;
    private MafiaServer server;
    private ObjectOutputStream out;
    private BufferedReader in;
    private String nickname;
    private CharacterTemplate character;
    private String teams;
    private String votePlayer = null;
    private boolean ready;

    public ClientHandler(Socket socket, MafiaServer server) {
        this.socket = socket;
        this.server = server;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
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
            setNickname(in.readLine()); // 닉네임 수신
            System.out.println("닉네임 입력 완료: " + getNickname()); // 디버깅용 로그

            while (true) {
                handleReq(); // 요청 처리
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setReady() {
        this.ready = true;
    }

    public boolean isReady() {
        return ready;
    }

    public void handleReq() throws IOException {
        String actionJson = in.readLine(); // 클라이언트로부터 요청 읽기
        if (actionJson == null) {
            throw new IOException("클라이언트 연결 종료");
        }

        ClientAction action = JsonUtil.jsonToAction(actionJson);

        switch (action.getAction().toLowerCase()) {
            case "shoot" -> handleShootAction(action);
            case "useability" -> handleUseAbilityAction();
            case "ready" -> {
                setReady();
                sendMessage("준비 상태로 설정되었습니다.");
            }
            case "vote" -> {
                setVote(action.getTarget());
                sendMessage("투표 완료: " + action.getTarget());
            }
            default -> sendMessage("알 수 없는 요청입니다: " + action.getAction());
        }
    }

    /**
     * shoot 요청 처리
     */
    private void handleShootAction(ClientAction action) {
        if (server.isCurrentTurn(this)) {
            sendResponse(server.handleShoot(this, action.getTarget()));
            server.endCurrentTurn(); // 턴 종료
        } else {
            sendMessage("총을 쏘려면 본인의 턴이어야 합니다.");
        }
    }

    /**
     * useAbility 요청 처리
     */
    private void handleUseAbilityAction() {
        sendResponse(server.handleUseAbility(this));
        // 턴 유지
    }

    public void startTurn() {
        sendMessage("당신의 턴입니다. '총 쏘기' 또는 '능력 사용'을 선택하세요:");
    }

    public void votePlayer() {
        try {
            String actionJson = in.readLine();
            ClientAction action = JsonUtil.jsonToAction(actionJson);

            if ("vote".equalsIgnoreCase(action.getAction())) {
                setVote(action.getTarget());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 해당 플레이어 투표 완료 여부
    public boolean isVoteCompleted() {
        return votePlayer != null;
    }

    // 능력 사용 처리
    public void sendMessage(String message) {
        sendResponse(new ServerResponse(message));
    }

    protected void sendResponse(ServerResponse response) {
        try {
            // todo ClientHandler에서 sendResponse 전에 Characters의 health 확인
//            if (!response.getAction().equals("message")) {
//                for (CharacterTemplate ct :
//                        response.getCharacters()) {
//                    System.out.println(ct.getHealth());
//                }
//            }
            out.writeObject(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public CharacterTemplate getCharacter() {
        return character;
    }

    public void setCharacter(CharacterTemplate character) {
        this.character = character;
    }

    public void setTeam(String team) {
        this.character.setTeam(team);
    }

    public String getTeam() {
        return character.getTeam();
    }

    public void setVote(String player) {
        votePlayer = player;
    }

    public String getVote() {
        return votePlayer;
    }
}

