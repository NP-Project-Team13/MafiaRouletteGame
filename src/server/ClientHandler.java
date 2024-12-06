package server;

import java.io.*;
import java.net.*;

import characters.CharacterTemplate;
import client.ClientAction;
import resources.JsonUtil;
import server.MafiaServer;
import server.ServerResponse;

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

            // 클라이언트에 대기 메시지 전송
//            sendMessage("게임 대기 중...");
            handleReq();
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

    public void handleReq() {
        try {
            String actionJson = in.readLine();
            ClientAction action = JsonUtil.jsonToAction(actionJson);
            if (action.getAction().equals("ready")) { // 준비 상태 처리
                setReady();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startTurn() {
        sendMessage("당신의 턴입니다. '총 쏘기' 또는 '능력 사용'을 선택하세요:");
        try {
            String actionJson = in.readLine();
            ClientAction action = JsonUtil.jsonToAction(actionJson);

            if ("shoot".equalsIgnoreCase(action.getAction())) {
                sendResponse(server.handleShoot(this, action.getTarget()));
            } else if ("useAbility".equalsIgnoreCase(action.getAction())) {
                // 능력 사용 처리 추가 가능
                sendResponse(server.handleUseAbility(this, action.getTarget()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

