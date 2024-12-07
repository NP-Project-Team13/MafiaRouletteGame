package server;

import java.io.*;
import java.net.*;

import characters.CharacterTemplate;
import client.ClientAction;
import resources.JsonUtil;

public class ClientHandler implements Runnable {
    private final Socket socket; // 클라이언트와의 연결 소켓
    private final MafiaServer server; // 서버 참조
    private ObjectOutputStream out; // 클라이언트로 데이터 전송용 스트림
    private BufferedReader in; // 클라이언트로부터 데이터 수신용 스트림
    private String nickname; // 클라이언트의 닉네임
    private CharacterTemplate character; // 플레이어 캐릭터 정보
    private String team; // 플레이어 팀 정보
    private String votePlayer; // 투표 대상
    private boolean ready = false; // 준비 상태 플래그

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

    // 클라이언트와의 통신을 처리하는 메인 스레드
    @Override
    public void run() {
        try {
            receiveNickname(); // 닉네임 수신
            processClientRequests(); // 클라이언트 요청 처리
        } catch (IOException e) {
            System.out.println("클라이언트 연결 종료: " + nickname);
        } finally {
            closeConnection();
        }
    }

    // 클라이언트의 닉네임을 수신
    private void receiveNickname() throws IOException {
        System.out.println("클라이언트에서 닉네임을 기다리는 중...");
        this.nickname = in.readLine(); // 닉네임 수신
        System.out.println("닉네임 입력 완료: " + nickname);
    }

    // 클라이언트 요청을 처리
    private void processClientRequests() throws IOException {
        while (true) {
            String actionJson = in.readLine(); // 요청 데이터를 JSON으로 수신
            if (actionJson == null) {
                throw new IOException("클라이언트 연결이 종료되었습니다.");
            }

            ClientAction action = JsonUtil.jsonToAction(actionJson); // JSON을 ClientAction 객체로 변환
            handleAction(action); // 요청에 따라 처리
        }
    }

    // 클라이언트 요청에 따라 동작 수행
    private void handleAction(ClientAction action) {
        switch (action.getAction().toLowerCase()) {
            case "shoot" -> handleShootAction(action); // 총 쏘기 요청
            case "useability" -> handleUseAbilityAction(); // 능력 사용 요청
            case "ready" -> handleReadyAction(); // 준비 요청
            case "vote" -> handleVoteAction(action); // 투표 요청
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

    // 'ready' 요청 처리
    private void handleReadyAction() {
        this.ready = true; // 준비 상태 설정
        sendMessage("준비 상태로 설정되었습니다.");
    }
    // 'vote' 요청 처리
    private void handleVoteAction(ClientAction action) {
        this.votePlayer = action.getTarget(); // 투표 대상 저장
        sendMessage("투표 완료: " + votePlayer);
    }

    // 능력 사용 처리
    public void sendMessage(String message) {
        sendResponse(new ServerResponse(message));
    }

    protected void sendResponse(ServerResponse response) {
        try {
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

    public void startTurn() {
        sendMessage("당신의 턴입니다. '총 쏘기' 또는 '능력 사용'을 선택하세요:");
    }



    public boolean isReady() {
        return ready;
    }

    // 해당 플레이어 투표 완료 여부
    public boolean isVoteCompleted() {
        return votePlayer != null;
    }

    public String getNickname() {
        return this.nickname;
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

    public String getVote() {
        return votePlayer;
    }
}

