package client;

import characters.CharacterTemplate;
import resources.JsonUtil;
import server.ServerResponse;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class MafiaClient {
    private Socket socket;
    private ObjectInputStream in;
    private PrintWriter out;
    private String nickname;
    private GameUI gameUI; // client.GameUI 인스턴스 추가

    public MafiaClient(String serverAddress, int serverPort, String nickname) {
        try {
            this.nickname = nickname; // 닉네임 설정
            this.gameUI = new GameUI(this); // client.GameUI 인스턴스 초기화
            socket = new Socket(serverAddress, serverPort); // 서버 연결
            in = new ObjectInputStream(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            // 닉네임 전송
            out.println(this.nickname);
            out.flush();
            System.out.println("닉네임 전송 완료: " + this.nickname);
        } catch (IOException e) {
            System.out.println("서버에 연결할 수 없습니다.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public String getNickname() {
        return nickname;
    }


    public void start() {
        new Thread(() -> {
            try {
                while (true) {
                    ServerResponse response = (ServerResponse) in.readObject();

                    // todo MafiaClient에서 response 받자마자 Characters의 health 확인
//                    if (!response.getAction().equals("message")) {
//                        System.out.println("받은 character");
//                        for (CharacterTemplate ct:
//                                response.getCharacters()) {
//                            System.out.println(ct.getHealth());
//                        }
//                    }

                    gameUI.handleServerResponse(response);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("서버와의 연결이 끊어졌습니다.");
                e.printStackTrace();
            }
        }).start(); // 수신 스레드 시작
    }


    public GameUI getGameUI() {
        return gameUI;
    }

    public void sendReady() {
        out.println(JsonUtil.actionToJson(new ClientAction("ready", nickname))); // 준비 상태 전송
    }

    public void sendAbilityRequest(CharacterTemplate character) {
        String targetNickname = "타겟의 닉네임"; // 타겟의 닉네임을 UI에서 수집하는 방법으로 변경해야 합니다.
        ClientAction action = new ClientAction("useAbility", targetNickname);
        out.println(JsonUtil.actionToJson(action)); // JSON으로 변환하여 서버로 전송
    }

    public void sendShootRequest(CharacterTemplate shooter, CharacterTemplate target) {
        String targetNickname = target.getName(); // 타겟의 이름
        ClientAction action = new ClientAction("shoot", targetNickname);
        out.println(JsonUtil.actionToJson(action)); // JSON으로 변환하여 서버로 전송
    }

    // todo 채팅 기능 구현
    public void sendChatMessage(String message) {
        ClientAction action = new ClientAction("chat", message);
        out.println(JsonUtil.actionToJson(action)); // JSON으로 변환하여 서버로 전송
        out.flush();
    }

    public void sendVote(String player) {
        ClientAction action = new ClientAction("vote", player);
        out.println(JsonUtil.actionToJson(action));
    }


    public static void main(String[] args) {

        JFrame frame = new JFrame("Mafia Roulette Game");

        // 클라이언트 연결 설정
//        String serverAddress = JOptionPane.showInputDialog(frame, "서버 주소를 입력하세요:", "localhost");
//        int serverPort = Integer.parseInt(JOptionPane.showInputDialog(frame, "서버 포트를 입력하세요:", "12345"));
//        String serverAddress = "122.45.203.188";
        String serverAddress = "localhost";
        int serverPort = 12345;
        String nickname = JOptionPane.showInputDialog(frame, "닉네임을 입력하세요: ");

        MafiaClient client = new MafiaClient(serverAddress, serverPort, nickname); // 클라이언트 인스턴스 생성
        client.start(); // 서버와의 연결 시작
        SwingUtilities.invokeLater(() -> new MainMenu().createAndShowGUI(client, client.gameUI));
    }

}
