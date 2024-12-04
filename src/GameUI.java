import characters.CharacterTemplate;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameUI {
    private JFrame frame;
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
        frame = new JFrame("Mafia Roulette - Game Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 1000);
        frame.getContentPane().setBackground(new Color(50, 50, 50));

        JPanel gameViewPanel = new JPanel(new BorderLayout());
        gameViewPanel.setBackground(new Color(40, 40, 40));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JButton backButton = new JButton("뒤로가기");
        backButton.setFont(new Font("Serif", Font.BOLD, 16));
        backButton.setBackground(new Color(80, 80, 80));
        backButton.setForeground(Color.WHITE);
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
        gameLog.setFont(new Font("Monospaced", Font.PLAIN, 16)); // 글씨 크기 증가
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
            JLabel tempInfo = new JLabel("플레이어 정보가 아직 로드되지 않았습니다.", SwingConstants.CENTER);
            tempInfo.setForeground(Color.WHITE);
            playerInfoPanel.add(tempInfo);
        } else {
            for (CharacterTemplate character : characters) {
                JPanel playerPanel = new JPanel();
                playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
                // TitledBorder 생성 및 속성 설정
                TitledBorder border = BorderFactory.createTitledBorder(character.getName());
                border.setTitleColor(Color.WHITE); // 제목 글씨 색상 설정
                border.setTitleFont(new Font("Serif", Font.BOLD, 16)); // 제목 글씨 크기 설정
                playerPanel.setBorder(border);
                playerPanel.setPreferredSize(new Dimension(250, 200)); // 일관된 크기 설정

                JLabel playerInfo1 = new JLabel(
                        String.format(" Team:" + character.getTeam())
                );

                JLabel playerInfo2 = new JLabel(
                        String.format(" Life: " +
                                character.getHealth())
                );

                // 테스트용, 최종적으로 삭제할 내용
                JLabel playerInfo3 = new JLabel(
                        String.format(" [능력] " +
                                character.getInfo())
                );
                playerInfo1.setForeground(Color.WHITE); // 글씨 색상 흰색
                playerInfo2.setForeground(Color.WHITE);
                playerInfo3.setForeground(Color.WHITE);
                playerInfo1.setFont(new Font("Serif", Font.BOLD, 16)); // 글씨 크기 증가
                playerInfo2.setFont(new Font("Serif", Font.BOLD, 16));
                playerInfo3.setFont(new Font("Serif", Font.BOLD, 16));
                playerPanel.add(playerInfo1);
                playerPanel.add(playerInfo2);
                playerPanel.add(playerInfo3);

                // 생존 여부에 따라 패널의 색상 조정 및 버튼 생성
                if (!character.isAlive()) {
                    playerPanel.setBackground(new Color(255, 0, 0, 120));
                } else {
                    playerPanel.setBackground(new Color(60, 60, 60));

                    // 항상 Shoot 버튼 생성, 상태에 따라 disabled 설정
                    JButton shootButton = new JButton("Shoot");
                    shootButton.setBackground(new Color(100, 100, 100));
                    shootButton.setForeground(Color.WHITE);
                    shootButton.setEnabled(characters.get(currentPlayerIndex).getName().equals(client.getNickname()) && character.getName().equals(client.getNickname())); // 상태에 따라 활성화 또는 비활성화
                    shootButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // 버튼 크기 설정
                    shootButton.addActionListener(e -> shoot(character));
                    playerPanel.add(shootButton);

                    // 본인 Character의 useAbility 버튼 생성
                    if (character.getName().equals(client.getNickname())) {
                        JButton abilityButton = new JButton("Use Ability");
                        abilityButton.setBackground(new Color(100, 100, 100));
                        abilityButton.setForeground(Color.WHITE);
                        abilityButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // 버튼 크기 설정
                        abilityButton.addActionListener(e -> useAbility(character));
                        playerPanel.add(abilityButton);
                    }
                }

                playerInfoPanel.add(playerPanel);
            }
        }

        JLabel bulletLabel2 = new JLabel(" [현재 슬롯] " + (currentSlot));
        bulletLabel2.setForeground(Color.WHITE); // 총알 슬롯 레이블 색상 설정
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
        // isAlive()가 true인 캐릭터만 필터링
        List<CharacterTemplate> aliveCharacters = characters.stream()
                .filter(CharacterTemplate::isAlive)
                .collect(Collectors.toList());

        // JOptionPane을 사용하여 대상 선택
        return (CharacterTemplate) JOptionPane.showInputDialog(
                null,
                message,
                "대상 선택",
                JOptionPane.QUESTION_MESSAGE,
                null,
                aliveCharacters.toArray(), // 필터링된 캐릭터 배열
                aliveCharacters.get(0) // 기본 선택
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
                SoundPlayer.playSound("/resources/총소리.wav"); // 음향 재생
            }
            case "miss" -> {
                logMessage(response.getMessage());
                SoundPlayer.playSound("/resources/빈총소리.wav"); // 음향 재생
            }
            case "useAbility" -> {
                logMessage(response.getMessage());
            }
            case "end" -> {
                frame.dispose(); // 현재 창 닫기
                MainMenu.createAndShowGUI(client, client.getGameUI()); // 메인 메뉴 화면으로 돌아가기
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
        String chamberStatus = bulletPositions.stream()
                .map(bulletPosition -> " " + (bulletPosition ? "O " : "X "))
                .collect(Collectors.joining("", "총알 슬롯 상태: " + "슬롯 ", ""));
        logMessage(chamberStatus);

        // 현재 턴과 라운드 번호 업데이트
        currentPlayerIndex = response.getCurrentPlayerIndex();
        roundNumber = response.getRoundNumber();
        updateTurnLabel(); // 현재 턴 레이블 업데이트
        updatePlayerInfoPanel(); // 플레이어 정보 패널 업데이트
    }
}
