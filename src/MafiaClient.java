import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MafiaClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        MafiaClient client = new MafiaClient();
        client.showMainMenu();
    }


    public void showMainMenu() {
        while (true) {
            System.out.println("==== Main Menu ====");
            System.out.println("1. Join Game");
            System.out.println("2. Game Rules");
            System.out.println("3. Hall of Fame");
            System.out.println("4. Exit Game");
            System.out.print("Select: ");
            int choice = scanner.nextInt();
            scanner.nextLine();  // 개행 문자 제거

            switch (choice) {
                case 1:
                    joinGame();
                    break;
                case 2:
                    showGameRules();
                    break;
                case 3:
                    showHallOfFame();
                    break;
                case 4:
                    System.out.println("Exiting the game...");
                    return;
                default:
                    System.out.println("Wrong selection. Select again.");
            }
        }
    }

    private void joinGame() {
        System.out.println("Joining the game...");
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println(in.readLine()); // 서버 환영 메시지 출력

            String serverMessage = null;
            while ((serverMessage = in.readLine()) != null) {
//                System.out.println("Server: " + serverMessage);
                if (serverMessage.startsWith("Game Started!")) {
                    System.out.println("Server: " + serverMessage);
                }
                else if (serverMessage.startsWith("Your Turn")) {
                    System.out.println("Server: " + serverMessage);
                    String target = scanner.nextLine();

                    // JSON 요청 생성
                    ClientAction action = new ClientAction("shoot", target);
                    String actionJson = JsonUtil.actionToJson(action);
                    out.println(actionJson);
                }
                else if(serverMessage.startsWith("{")) {
                    // 서버로부터 JSON 응답 수신
                    ServerResponse serverResponse = JsonUtil.jsonToResponse(serverMessage);
                    System.out.println("Server: " + serverResponse.message);
                    out.println("TE");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showHallOfFame() {
        // 텍스트 파일, 데이터 파일을 읽어 명예의 전당, 업적 출력
        System.out.println("==== Hall of Fame ====");
    }

    private void showGameRules() {
        System.out.println("==== Game Rules ====");
        System.out.println("4명의 플레이어가 순서대로 번호를 부여받고, 모두 접속 시 게임이 시작됩니다.");
        System.out.println("게임 규칙 열람을 완료했습니다.");
    }
}
