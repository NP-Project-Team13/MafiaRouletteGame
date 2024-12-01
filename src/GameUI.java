import characters.CharacterTemplate; // Character0로 변경
import characters.Character1;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class GameUI {
    private JTextArea gameLog;
    private JLabel turnLabel;
    private JPanel playerInfoPanel;
    private MafiaClient client; // 클라이언트 인스턴스 추가
    private ArrayList<CharacterTemplate> characters; // Character0 리스트로 변경
    private int currentPlayerIndex;
    private int roundNumber = 1;
    private static final int CYLINDER_SIZE = 5;
    private boolean[] bulletPositions;
    private int turnCounter = 0;
    private int currentSlot = 1;

    public GameUI(MafiaClient client) {
        this.client = client; // 클라이언트 인스턴스 초기화
        this.characters = new ArrayList<>();
        initializeCharacters(); // 캐릭터 초기화
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Mafia Roulette - Game Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 1000);

        JPanel gameViewPanel = new JPanel(new BorderLayout());
        gameViewPanel.setBackground(Color.DARK_GRAY);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // 뒤로가기 버튼
        JButton backButton = new JButton("뒤로가기");
        backButton.setFont(new Font("Serif", Font.BOLD, 16));
        backButton.addActionListener(e -> goBack(frame));

        turnLabel = new JLabel("현재 턴: " + characters.get(currentPlayerIndex).getName() + " | 라운드: " + roundNumber, SwingConstants.CENTER);
        turnLabel.setFont(new Font("Serif", Font.BOLD, 28));
        turnLabel.setForeground(Color.RED);

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(turnLabel, BorderLayout.CENTER);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        gameLog = new JTextArea();
        gameLog.setEditable(false);
        gameLog.setLineWrap(true);
        gameLog.setWrapStyleWord(true);
        gameLog.setFont(new Font("Monospaced", Font.PLAIN, 14));
        gameLog.setBackground(new Color(30, 30, 30));
        gameLog.setForeground(Color.WHITE);
        JScrollPane logScrollPane = new JScrollPane(gameLog);
        logScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Game Log"));

        playerInfoPanel = new JPanel();
        playerInfoPanel.setLayout(new BoxLayout(playerInfoPanel, BoxLayout.Y_AXIS));
        playerInfoPanel.setOpaque(false);

        initializeBullets();
        updatePlayerInfoPanel();

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(logScrollPane, BorderLayout.CENTER);
        frame.add(playerInfoPanel, BorderLayout.WEST);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        logMessage("게임이 시작되었습니다. " + characters.get(currentPlayerIndex).getName() + "의 턴입니다.");
    }

    private void initializeCharacters() {
        // 서버에서 캐릭터 정보 받아오기 (예시로 Character0 사용)
        for (int i = 0; i < 8; i++) {
            characters.add(new Character1("name" + (i + 1), "Team A")); // 예시로 Character1 사용
        }
        currentPlayerIndex = 0;
    }

    private void initializeBullets() {
        bulletPositions = new boolean[CYLINDER_SIZE];
        Random random = new Random();
        int bulletsInRound = Math.min(roundNumber, CYLINDER_SIZE);

        for (int i = 0; i < bulletsInRound; i++) {
            int position;
            do {
                position = random.nextInt(CYLINDER_SIZE);
            } while (bulletPositions[position]);
            bulletPositions[position] = true;
        }

        logMessage("라운드 " + roundNumber + " 시작! 총알이 장전된 슬롯: " + getBulletPositionsString());
    }

    private String getBulletPositionsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bulletPositions.length; i++) {
            if (bulletPositions[i]) {
                sb.append(i + 1).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private void updatePlayerInfoPanel() {
        playerInfoPanel.removeAll();
        for (CharacterTemplate character : characters) {
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
            playerPanel.setBorder(BorderFactory.createTitledBorder(character.getName()));

            JLabel playerInfo = new JLabel(
                    String.format(" [팀] %s [체력] %d [능력] %s",
                            character.getTeam(),
                            character.getHealth(),
                            character.getInfo())
            );
            playerPanel.add(playerInfo);

            JButton shootButton = new JButton("Shoot");
            shootButton.addActionListener(e -> shoot(character));

            JButton abilityButton = new JButton("Use Ability");
            abilityButton.addActionListener(e -> useAbility(character));

            playerPanel.add(shootButton);
            playerPanel.add(abilityButton);
            playerInfoPanel.add(playerPanel);
        }

        JLabel bulletLabel = new JLabel(" [총알 슬롯] " + getBulletPositionsString());
        playerInfoPanel.add(bulletLabel);
        JLabel bulletLabel2 = new JLabel(" [현재 슬롯] " + (currentSlot));
        playerInfoPanel.add(bulletLabel2);
        playerInfoPanel.revalidate();
        playerInfoPanel.repaint();
    }

    private void nextTurn() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % characters.size();
        } while (!characters.get(currentPlayerIndex).isAlive());

        turnCounter++;

        if (turnCounter >= alivePlayerCount()) {
            logMessage("모든 플레이어가 턴을 완료했습니다. 라운드 " + roundNumber + " 종료!");
            roundNumber++;
            resetCharacterAbilities();
            turnCounter = 0;
        }

        updateTurnLabel();
        logMessage("▶ " + characters.get(currentPlayerIndex).getName() + "의 턴입니다.");
        updatePlayerInfoPanel();
    }

    private void shoot(CharacterTemplate currentCharacter) {
        CharacterTemplate target = selectTarget("타겟을 선택하세요");
        if (target == null) return;

        logMessage(currentCharacter.getName() + "이(가) " + target.getName() + "을(를) 향해 슬롯 " + currentSlot + "에서 총을 쏩니다!");

        // 서버에 총 쏘기 요청 전송
        client.sendShootRequest(currentCharacter, target, currentSlot);

        // 슬롯 증가
        currentSlot = (currentSlot % CYLINDER_SIZE) + 1;

        if (!anyBulletsLeft()) {
            logMessage("모든 총알이 소모되었습니다. 총알 재장전 중...");
            initializeBullets(); // 총알 재장전
        }

        nextTurn();
    }

    private boolean anyBulletsLeft() {
        for (boolean bullet : bulletPositions) {
            if (bullet) return true;
        }
        return false;
    }

    private void useAbility(CharacterTemplate currentCharacter) {
        if (currentCharacter.isAbilityUsed()) {
            logMessage(currentCharacter.getName() + "은(는) 이미 능력을 사용했습니다.");
            return;
        }

//        // 타겟 선택을 위한 입력 받기
//        String targetNickname = JOptionPane.showInputDialog("능력을 사용할 타겟의 닉네임을 입력하세요:");
//        if (targetNickname == null || targetNickname.trim().isEmpty()) {
//            logMessage("타겟이 선택되지 않았습니다.");
//            return;
//        }

//        // 서버에 능력 사용 요청 전송
//        client.sendAbilityRequest(currentCharacter, targetNickname);

        // 서버에 능력 사용 요청 전송
        client.sendAbilityRequest(currentCharacter);

        logMessage(currentCharacter.getName() + "이(가) 능력을 사용했습니다.");
        updatePlayerInfoPanel();
    }

    private void updateTurnLabel() {
        turnLabel.setText("현재 턴: " + characters.get(currentPlayerIndex).getName() + " | 라운드: " + roundNumber);
    }

    private CharacterTemplate selectTarget(String message) {
        return (CharacterTemplate) JOptionPane.showInputDialog(
                null,
                message,
                "대상 선택",
                JOptionPane.QUESTION_MESSAGE,
                null,
                characters.toArray(),
                characters.get(0) // 기본 선택
        );
    }

    private void resetCharacterAbilities() {
        for (CharacterTemplate character : characters) {
            if (character.isAlive()) {
                character.resetAbilityUsage();
            }
        }
    }

    public void logMessage(String message) {
        gameLog.append(message + "\n");
        gameLog.setCaretPosition(gameLog.getDocument().getLength()); // 자동 스크롤
    }

    private void goBack(JFrame frame) {
        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "뒤로 가시겠습니까? 진행 상황이 저장되지 않을 수 있습니다.",
                "뒤로가기 확인",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            frame.dispose(); // 현재 창 닫기
            System.out.println("뒤로가기 버튼 클릭됨. 메인 메뉴로 이동합니다.");
            MainMenu.createAndShowGUI(client, client.getGameUI()); // 메인 메뉴 화면으로 돌아가기
        }
    }


    private int alivePlayerCount() {
        int count = 0;
        for (CharacterTemplate character : characters) {
            if (character.isAlive()) {
                count++;
            }
        }
        return count;
    }

    // 서버 응답 처리 메소드 추가
    public void handleServerResponse(ServerResponse response) {
        // 서버 응답에 따라 UI 업데이트 로직 추가
        switch (response.getAction()) {
            case "shoot", "useAbility":
                logMessage(response.getMessage());
                break;
            // 추가적인 응답 처리 로직
            default:
                logMessage("알 수 없는 행동: " + response.getAction());
                break;
        }
        updatePlayerInfoPanel(); // UI 업데이트
    }
}