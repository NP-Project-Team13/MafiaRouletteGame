import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MafiaClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;

    public MafiaClient(String serverAddress, int serverPort, String nickname) {
        try {
            this.nickname = nickname; // 닉네임 설정
            socket = new Socket(serverAddress, serverPort); // 서버 연결
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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

    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {

                String serverMessage = in.readLine();
                if (serverMessage.startsWith("{")) {
                    ServerResponse response = JsonUtil.jsonToResponse(serverMessage);
                    System.out.println("행동: " + response.getAction());
                    System.out.println("결과: " + response.getResult());
                    System.out.println("메시지: " + response.getMessage());
                } else {
                    System.out.println("서버: " + serverMessage);
                }

                if (serverMessage.startsWith("당신의 턴")) {
                    System.out.print("행동을 선택하세요 ('총 쏘기' 또는 '능력 사용'): ");
                    String actionType = scanner.nextLine();
                    String target = null;

                    if ("shoot".equalsIgnoreCase(actionType)) {
                        System.out.print("쏘고 싶은 플레이어의 닉네임을 입력하세요: ");
                        target = scanner.nextLine();
                    }else if ("useAbility".equalsIgnoreCase(actionType)) {
                        // 능력 사용일 경우 타겟 입력(필요할 때만)
                        System.out.print("능력을 사용할 타겟의 닉네임을 입력하세요 (없으면 엔터): ");
                        target = scanner.nextLine();
                        if (target.isEmpty()) target = null;
                    } else {
                        System.out.println("잘못된 행동입니다. 다시 선택해주세요.");
                        continue;
                    }

                    ClientAction action = new ClientAction(actionType, target);
                    out.println(JsonUtil.actionToJson(action));
                }
            }
        } catch (IOException e) {
            System.out.println("서버와의 연결이 끊어졌습니다.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("서버 주소를 입력하세요 (기본값: localhost): ");
        String serverAddress = scanner.nextLine();
        if (serverAddress.isEmpty()) serverAddress = "localhost";

        System.out.print("서버 포트를 입력하세요 (기본값: 12345): ");
        String portInput = scanner.nextLine();
        int serverPort = portInput.isEmpty() ? 12345 : Integer.parseInt(portInput);

        System.out.print("닉네임을 입력하세요: ");
        String nickname = scanner.nextLine().trim();

        MafiaClient client = new MafiaClient(serverAddress, serverPort, nickname);

        client.start();
    }
}
