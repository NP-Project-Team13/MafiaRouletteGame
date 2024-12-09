package client;

import characters.CharacterTemplate;
import resources.JsonUtil;
import server.ServerResponse;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
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

                    // 캐릭터 설명 게임 시작 직후 한 번만 보여줍니다.
                    if (!gameUI.characterDescriptionShown) {
                        CharacterTemplate myCharacter = getAssignedCharacter();
                        if (myCharacter != null) {
                            MainMenu.showCharacterDescriptions(myCharacter);
                            gameUI.characterDescriptionShown = true;
                        }
                    }

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

    // 서버로 투표 결과 전송
    public void sendVote(String player) {
        ClientAction action = new ClientAction("vote", player);
        out.println(JsonUtil.actionToJson(action));
    }


    public static void main(String[] args) {

        JFrame frame = new JFrame("Mafia Roulette Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);

        // 닉네임 입력을 위한 커스터마이징된 패널 생성
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel label = new JLabel("닉네임을 입력해주세요.");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label, gbc);

        JTextField nicknameField = new JTextField(15);
        nicknameField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        nicknameField.setPreferredSize(new Dimension(200, 40));
        nicknameField.setBorder(new RoundedBorder(15)); // 둥근 테두리 적용
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(nicknameField, gbc);

        int result = JOptionPane.showConfirmDialog(frame, panel, "", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String nickname = nicknameField.getText();
            // 클라이언트 연결 설정
//        String serverAddress = JOptionPane.showInputDialog(frame, "서버 주소를 입력하세요:", "localhost");
//        int serverPort = Integer.parseInt(JOptionPane.showInputDialog(frame, "서버 포트를 입력하세요:", "12345"));
        String serverAddress = "192.168.14.222";
//            String serverAddress = "localhost";
            int serverPort = 12345;

            MafiaClient client = new MafiaClient(serverAddress, serverPort, nickname); // 클라이언트 인스턴스 생성
            client.start(); // 서버와의 연결 시작
            SwingUtilities.invokeLater(() -> new MainMenu().createAndShowGUI(client, client.gameUI));
        } else {
            System.exit(0);
        }
    }

    // 둥근 테두리를 위한 커스텀 Border 클래스
    static class RoundedBorder extends AbstractBorder {
        private final int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.GRAY);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius, radius, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius;
            return insets;
        }
    }


    public CharacterTemplate getAssignedCharacter() {
        return gameUI.getAssignedCharacter(nickname);
    }

}
