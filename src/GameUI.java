import characters.CharacterTemplate; // Character0로 변경

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameUI {
    private JTextArea gameLog;
    private JLabel turnLabel;
    private JPanel playerInfoPanel;
    private MafiaClient client; // 클라이언트 인스턴스 추가
    private List<CharacterTemplate> characters; // Character0 리스트로 변경
    private int currentPlayerIndex;
    private int roundNumber = 1;
    private List<Boolean> bulletPositions;
    private int currentSlot = 1;

    public GameUI(MafiaClient client) {
        this.client = client; // 클라이언트 인스턴스 초기화
        this.characters = new ArrayList<>();
        this.bulletPositions = new ArrayList<>();
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

        turnLabel = new JLabel("게임 대기중", SwingConstants.CENTER);
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

        updatePlayerInfoPanel();

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(logScrollPane, BorderLayout.CENTER);
        frame.add(playerInfoPanel, BorderLayout.WEST);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    private void updatePlayerInfoPanel() {
        playerInfoPanel.removeAll();
        if (characters.isEmpty()) {
            // 임시 정보 표시
            JLabel tempInfo = new JLabel("플레이어 정보가 아직 로드되지 않았습니다.");
            playerInfoPanel.add(tempInfo);
        } else {
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

                if (characters.get(currentPlayerIndex).getName().equals(client.getNickname()) && character.getName().equals(client.getNickname())) {
                    JButton shootButton = new JButton("Shoot");
                    shootButton.addActionListener(e -> shoot(character));
                    playerPanel.add(shootButton);
                }
                if (character.getName().equals(client.getNickname())) {
                    JButton abilityButton = new JButton("Use Ability");
                    abilityButton.addActionListener(e -> useAbility(character));
                    playerPanel.add(abilityButton);
                }

                playerInfoPanel.add(playerPanel);
            }
        }

        JLabel bulletLabel2 = new JLabel(" [현재 슬롯] " + (currentSlot));
        playerInfoPanel.add(bulletLabel2);
        playerInfoPanel.revalidate();
        playerInfoPanel.repaint();
    }

    private void shoot(CharacterTemplate currentCharacter) {
        CharacterTemplate target = selectTarget("타겟을 선택하세요");
        if (target == null) return;

        logMessage(currentCharacter.getName() + "이(가) " + target.getName() + "을(를) 향해 슬롯 " + currentSlot + "에서 총을 쏩니다!");

        // 서버에 총 쏘기 요청 전송
        client.sendShootRequest(currentCharacter, target, currentSlot);

    }

    private void useAbility(CharacterTemplate currentCharacter) {
        if (currentCharacter.isAbilityUsed()) {
            logMessage(currentCharacter.getName() + "은(는) 이미 능력을 사용했습니다.");
            return;
        }

        // 서버에 능력 사용 요청 전송
        client.sendAbilityRequest(currentCharacter);

        logMessage(currentCharacter.getName() + "이(가) 능력을 사용했습니다.");
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


    // 서버 응답 처리 메소드 추가
    public void handleServerResponse(ServerResponse response) {
        // 서버 응답에 따라 UI 업데이트 로직 추가
        switch (response.getAction()) {
            case "updateGameState" -> updateGameState(response); // 게임 상태 업데이트 메소드 호출
            case "message" -> logMessage(response.getMessage());
            case "shoot" -> {
                logMessage(response.getMessage());
            }
            case "useAbility" -> {
                logMessage(response.getMessage());
            }

            // 추가적인 응답 처리 로직
            default -> logMessage("알 수 없는 행동: " + response.getAction());
        }
    }

    private void updateGameState(ServerResponse response) {

        // 플레이어 정보 업데이트
        characters = response.getCharacters();
        for (CharacterTemplate character : characters) {
            logMessage(" - 팀: " + character.getTeam() + ", 체력: " + character.getHealth() + ", 생존: " + (character.isAlive() ? "Yes" : "No"));
        }

        // 총알 슬롯 상태 업데이트
        bulletPositions = response.getChambers();
        String chamberStatus = bulletPositions.stream().map(bulletPosition -> " " + (bulletPosition ? "O " : "X ")).collect(Collectors.joining("", "총알 슬롯 상태: " + "슬롯 ", ""));
        logMessage(chamberStatus);

        // 현재 턴과 라운드 번호 업데이트
        currentPlayerIndex = response.getCurrentPlayerIndex();
        roundNumber = response.getRoundNumber();
        updateTurnLabel(); // 현재 턴 레이블 업데이트
        updatePlayerInfoPanel(); // 플레이어 정보 패널 업데이트
    }
}