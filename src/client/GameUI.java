package client;

import characters.Character6;
import characters.CharacterTemplate;
import resources.SoundPlayer;
import server.ServerResponse;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static client.MainMenu.showCharacterDescriptions;

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
    private JTextField chatInputField; // 채팅 입력 필드

    private JScrollPane logScrollPane; // 게임 로그 패널을 멤버 변수로 선언

    public boolean characterDescriptionShown = false; // 설명이 표시되었는지 여부



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
        backButton.setFont(new Font("나눔 고딕", Font.BOLD, 16));
        backButton.setBackground(new Color(80, 80, 80));
        backButton.setForeground(Color.GRAY);
        backButton.addActionListener(e -> goBack(frame));

        // 커스텀 폰트 불러오기
        Font dokdoFont = loadCustomFont("/resources/Dokdo.ttf", 28f);
        turnLabel = new JLabel("게임 대기중", SwingConstants.CENTER);
        turnLabel.setFont(dokdoFont); // 불러온 커스텀 폰트 적용
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

        logScrollPane = new JScrollPane(gameLog);
        logScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Game Log"));


        playerInfoPanel = new JPanel();
        playerInfoPanel.setLayout(new BoxLayout(playerInfoPanel, BoxLayout.Y_AXIS));
        playerInfoPanel.setOpaque(false);

        updatePlayerInfoPanel();

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(logScrollPane, BorderLayout.CENTER);
        frame.add(playerInfoPanel, BorderLayout.WEST);

        // 게임 로그 스크롤 패널 아래에 채팅 입력 패널 추가
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatInputField = new JTextField();
        JButton sendButton = new JButton("전송");

        sendButton.addActionListener(e -> sendChatMessage());

        chatPanel.add(chatInputField, BorderLayout.CENTER);
        chatPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.setPreferredSize(new Dimension(logScrollPane.getWidth(), 50)); // 높이 50으로 설정

        // 프레임의 아래쪽에 채팅 패널 추가
        frame.add(chatPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    private void sendChatMessage() {
        String message = chatInputField.getText().trim(); // 입력된 메시지 가져오기
        if (!message.isEmpty()) {
            client.sendChatMessage(message); // 서버로 메시지 전송
            chatInputField.setText(""); // 입력 필드 초기화
        }
    }

    private void updatePlayerInfoPanel() {
        playerInfoPanel.removeAll();
        if (characters.isEmpty()) {
            playerInfoPanel.setLayout(new GridBagLayout());
            playerInfoPanel.setBackground(Color.BLACK);
            playerInfoPanel.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight())); // 패널 크기를 창 크기에 맞춤

            JLabel tempInfo = new JLabel("", SwingConstants.CENTER);
            tempInfo.setForeground(Color.WHITE);
            tempInfo.setFont(new Font("Serif", Font.BOLD, 35));
//            tempInfo.setFont(loadCustomFont("/resources/Stylish.ttf", 30));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;

            playerInfoPanel.add(tempInfo, gbc);


            typeText(tempInfo, "다른 플레이어가 로드될 때까지 기다려주세요!");


        } else {
            for (CharacterTemplate character : characters) {
                // 모든 캐릭터가 로드되면 로그 창을 보이게 설정
                playerInfoPanel.setPreferredSize(new Dimension(300, frame.getHeight())); // 패널 폭을 300픽셀로 설정
                playerInfoPanel.setLayout(new BoxLayout(playerInfoPanel, BoxLayout.Y_AXIS));

                JPanel playerPanel = new JPanel();
                playerPanel.setLayout(new GridBagLayout()); // GridBagLayout 사용
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.HORIZONTAL; // 수평 방향으로 꽉 채우기
                gbc.weightx = 1.0; // 가중치 설정

                // TitledBorder 생성 및 속성 설정
                TitledBorder border = BorderFactory.createTitledBorder(character.getName());
                border.setTitleColor(Color.WHITE); // 제목 글씨 색상 설정
                border.setTitleFont(new Font("Serif", Font.BOLD, 16)); // 제목 글씨 크기 설정
                playerPanel.setBorder(border);
                playerPanel.setPreferredSize(new Dimension(250, 150)); // 일관된 크기 설정

                JLabel playerInfo1 = new JLabel(String.format(" Team: %s", character.getTeam()));
                playerInfo1.setForeground(Color.BLACK); // 글씨 색상 흰색
                playerInfo1.setFont(new Font("Serif", Font.BOLD, 16)); // 글씨 크기 증가

                gbc.gridx = 0;
                gbc.gridy = 0;
                playerPanel.add(playerInfo1, gbc); // 팀 정보 추가

                // Life 정보를 하트 아이콘으로 표시
                JPanel lifePanel = new JPanel();
                lifePanel.setOpaque(false); // 투명하게 설정
                int health = character.getHealth();
                for (int i = 0; i < health; i++) {
                    ImageIcon heartIcon = new ImageIcon(getClass().getResource("/resources/heart.png"));
                    Image heartImage = heartIcon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH); // 크기 조정
                    JLabel heartLabel = new JLabel(new ImageIcon(heartImage));
                    lifePanel.add(heartLabel);
                }

                gbc.gridx = 0;
                gbc.gridy = 1;
                playerPanel.add(lifePanel, gbc); // 하트 아이콘 추가

                // 능력 정보
                JLabel playerInfo2 = new JLabel(String.format(" [능력] %s", character.getInfo()));
                playerInfo2.setForeground(Color.WHITE); // 글씨 색상 흰색
                playerInfo2.setFont(new Font("나눔 고딕", Font.BOLD, 16)); // 글씨 크기 증가

                gbc.gridx = 0;
                gbc.gridy = 2;
                playerPanel.add(playerInfo2, gbc); // 능력 정보 추가

                // 생존 여부에 따라 패널의 색상 조정 및 버튼 생성
                if (!character.isAlive()) {
                    playerPanel.setBackground(new Color(255, 0, 0, 120));
                } else {
                    playerPanel.setBackground(new Color(60, 60, 60));

                    // Shoot 버튼 생성
                    JButton shootButton = getShootButton(character, gbc);
                    playerPanel.add(shootButton, gbc); // Shoot 버튼 추가

                    // Use Ability 버튼 생성
                    if (character.getName().equals(client.getNickname())) {
                        JButton abilityButton = getAbilityButton(character, gbc);
                        playerPanel.add(abilityButton, gbc); // Use Ability 버튼 추가
                    }
                }

                playerInfoPanel.add(playerPanel);
            }
        }

        playerInfoPanel.revalidate();
        playerInfoPanel.repaint();
    }

    private JButton getAbilityButton(CharacterTemplate character, GridBagConstraints gbc) {
        JButton abilityButton = new JButton("Use Ability");
        abilityButton.setBackground(new Color(100, 100, 100));
        abilityButton.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 4;
        abilityButton.addActionListener(e -> useAbility(character));
        return abilityButton;
    }

    private JButton getShootButton(CharacterTemplate character, GridBagConstraints gbc) {
        JButton shootButton = new JButton("Shoot");
        shootButton.setBackground(new Color(100, 100, 100));
        shootButton.setForeground(Color.BLACK);

        // Character가 힐러일 때
        if (character instanceof Character6) {
            Character6 character6 = (Character6) character;
            // 다른 아군의 생존 여부 확인
            boolean hasAliveAlly = characters.stream()
                    .anyMatch(c -> c.isAlive() && c.getTeam().equals(character6.getTeam()) && !c.getName().equals(character6.getName()));

            if (character6.isReady() && hasAliveAlly) {
                shootButton.setText("Heal"); // 버튼의 텍스트를 "Heal"로 변경
            } else {
                character6.setReady(false); // 힐을 준비한 상태를 false로 변경
                updateCharacterInList(character6); // 리스트에 반영
            }
        }

        shootButton.setEnabled(characters.get(currentPlayerIndex).getName().equals(client.getNickname()) && character.getName().equals(client.getNickname())); // 상태에 따라 활성화 또는 비활성화
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1.0; // 버튼이 패널을 꽉 채우도록 가중치 설정
        shootButton.addActionListener(e -> shoot(character));
        return shootButton;
    }

    private void shoot(CharacterTemplate currentCharacter) {
        CharacterTemplate target = selectTarget("타겟을 선택하세요");
        if (target == null) return;

        // 서버에 총 쏘기 요청 전송
        client.sendShootRequest(currentCharacter, target);
    }

    private void useAbility(CharacterTemplate currentCharacter) {
        if (currentCharacter.isAbilityUsed()) {
            logMessage(currentCharacter.getName() + "님은 이미 능력을 사용했습니다.");
            return;
        }

        // 서버에 능력 사용 요청 전송
        client.sendAbilityRequest(currentCharacter);

        logMessage(currentCharacter.getName() + "님이 능력을 사용했습니다.");
    }

    private void updateTurnLabel() {
        Font dokdoFont = loadCustomFont("/resources/Dokdo.ttf", 35f); // 원하는 크기로 설정
        turnLabel.setFont(dokdoFont);
        turnLabel.setText("[현재 턴] " + characters.get(currentPlayerIndex) + "  [라운드] " + roundNumber);
    }


    private CharacterTemplate selectTarget(String message) {
        // 상대 플레이어의 팀을 가져옴
        String teamToShow = characters.get(currentPlayerIndex).getTeam();

        // isAlive()가 true인 캐릭터만 필터링하고, 팀이 다른 캐릭터만 포함
        List<CharacterTemplate> aliveCharacters = characters.stream()
                .filter(character -> character.isAlive() && !character.getTeam().equals(teamToShow)) // 팀이 다른 캐릭터
                .collect(Collectors.toList());

        // 힐러인 경우, 자신이 아닌 자기 팀을 가져옴
        CharacterTemplate currentCharacter = characters.get(currentPlayerIndex);
        if (currentCharacter instanceof Character6 && ((Character6) currentCharacter).isReady()) {aliveCharacters = characters.stream()
                .filter(character -> character.isAlive() && character.getTeam().equals(teamToShow) && !character.getName().equals(currentCharacter.getName())) // 팀이 같은 캐릭터
                .collect(Collectors.toList());
        }


        // 다이얼로그 생성
        JDialog dialog = new JDialog(frame, "타겟 선택", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // 라벨 추가
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        messageLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialog.add(messageLabel, gbc);

        // 캐릭터 선택 드롭다운
        JComboBox<CharacterTemplate> targetComboBox = new JComboBox<>(aliveCharacters.toArray(new CharacterTemplate[0]));
        targetComboBox.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        gbc.gridy = 1;
        dialog.add(targetComboBox, gbc);

        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // 확인 버튼
        JButton confirmButton = new JButton("확인");
        styleButton(confirmButton);
        confirmButton.addActionListener(e -> dialog.dispose());

        // 취소 버튼
        JButton cancelButton = new JButton("취소");
        styleButton(cancelButton);
        cancelButton.addActionListener(e -> {
            targetComboBox.setSelectedItem(null);
            dialog.dispose();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        gbc.gridy = 2;
        dialog.add(buttonPanel, gbc);

        dialog.pack(); // 다이얼로그 크기 조정
        dialog.setVisible(true);

        return (CharacterTemplate) targetComboBox.getSelectedItem();
    }


    // MVP 플레이어 선택 창
    private CharacterTemplate selectVote(String message) {
        // 새로운 커스텀 다이얼로그 생성
        JDialog dialog = new JDialog(frame, "MVP 투표", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // 라벨 추가
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        messageLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialog.add(messageLabel, gbc);

        // 플레이어 선택 드롭다운
        List<CharacterTemplate> charactersList = characters.stream().collect(Collectors.toList());
        JComboBox<CharacterTemplate> characterComboBox = new JComboBox<>(charactersList.toArray(new CharacterTemplate[0]));
        characterComboBox.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        dialog.add(characterComboBox, gbc);

        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // 확인 버튼
        JButton confirmButton = new JButton("확인");
        styleButton(confirmButton);
        confirmButton.addActionListener(e -> dialog.dispose());

        // 취소 버튼
        JButton cancelButton = new JButton("취소");
        styleButton(cancelButton);
        cancelButton.addActionListener(e -> {
            characterComboBox.setSelectedItem(null);
            dialog.dispose();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        gbc.gridy = 2;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);

        return (CharacterTemplate) characterComboBox.getSelectedItem();
    }

    public void logMessage(String message) {
        gameLog.append(message + "\n");
        gameLog.setCaretPosition(gameLog.getDocument().getLength()); // 자동 스크롤
    }

    private void goBack(JFrame frame) {
        // 새로운 커스텀 다이얼로그 생성
        JDialog dialog = new JDialog(frame, "뒤로가기 확인", true);
        dialog.setSize(400, 180);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // 라벨 추가
        JLabel messageLabel = new JLabel("뒤로 가시겠습니까? 진행 상황이 저장되지 않을 수 있습니다.");
        messageLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        messageLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialog.add(messageLabel, gbc);

        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // 예 버튼
        JButton yesButton = new JButton("예");
        styleButton(yesButton);
        yesButton.addActionListener(e -> {
            frame.dispose(); // 현재 창 닫기
            System.out.println("뒤로가기 버튼 클릭됨. 메인 메뉴로 이동합니다.");
            MainMenu.createAndShowGUI(client, client.getGameUI()); // 메인 메뉴 화면으로 돌아가기
            dialog.dispose();
        });

        // 아니오 버튼
        JButton noButton = new JButton("아니오");
        styleButton(noButton);
        noButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    // 버튼 스타일을 설정하는 메서드
    private void styleButton(JButton button) {
        button.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(100, 40));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);  // 버튼 배경을 흰색으로 설정
        button.setForeground(Color.BLACK);  // 버튼 글씨 색상을 검정으로 설정
        button.setBorder(new MafiaClient.RoundedBorder(20));
    }


    // 서버 응답 처리 메소드 추가
    public void handleServerResponse(ServerResponse response) {
        // 서버 응답에 따라 UI 업데이트 로직 추가
        switch (response.getAction()) {
            case "updateGameState" -> updateGameState(response); // 게임 상태 업데이트 메소드 호출
            case "message" -> { // 메시지 전송
                if (response.getMessage().equalsIgnoreCase("voteStart")) { // 투표 시작
                    votePlayer();
                }else if(response.getMessage().startsWith("history:")) {
                    String history = response.getMessage().substring("history:".length());
                    saveHistoryToFile(history);
                }
                else {
                    logMessage(response.getMessage());
                }
            }
            case "shoot" -> { // 격발
                logMessage(response.getMessage());
                SoundPlayer.playSound("/resources/총소리.wav"); // 음향 재생
            }
            case "miss" -> { // 불발
                logMessage(response.getMessage());
                SoundPlayer.playSound("/resources/빈총소리.wav"); // 음향 재생
            }
            case "useAbility" -> { // 능력 사용
                logMessage(response.getMessage());
                updateGameState(response); // useAbility시 턴이 바뀌지 않아 updateGameState가 한 번 호출됨
            }
            case "voteEnd" -> { // 투표 종료
                String mvp = response.getMessage();
                showMVPDialog(mvp);
                logMessage("3초 후 메인 화면으로 돌아갑니다...");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            case "end" -> { // 게임 종료
                frame.dispose(); // 현재 창 닫기
                MainMenu.createAndShowGUI(client, client.getGameUI()); // 메인 메뉴 화면으로 돌아가기
            }

            // 추가적인 응답 처리 로직
            default -> logMessage("알 수 없는 행동: " + response.getAction());
        }
    }

    private void saveHistoryToFile(String history) {
        String filePath = "src/resources/history.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            writer.println(history); // history 기록 저장
            System.out.println("게임 기록이 저장되었습니다: " + history);
        } catch (IOException e) {
            System.err.println("기록 저장 실패: " + e.getMessage());
        }
    }

    private void updateGameState(ServerResponse response) {

        // 플레이어 정보 업데이트
        characters = response.getCharacters();
//        characterLog(response);

        // 현재 턴과 라운드 번호 업데이트
        currentPlayerIndex = response.getCurrentPlayerIndex();
        roundNumber = response.getRoundNumber();

        updateTurnLabel(); // 현재 턴 레이블 업데이트
        updatePlayerInfoPanel(); // 플레이어 정보 패널 업데이트
    }

    private void characterLog(ServerResponse response) {
        logMessage("\n\n");
        for (CharacterTemplate character : characters) {
            logMessage("   📍 [" + character.getTeam() + "팀] " + (character.isAlive() ? "생존자 " : "사망자 ") + character.getName() +
                    (character.getHealth() == 3 ? " ❤️❤️❤️" : (character.getHealth() == 2 ? " ❤️❤️" : (character.getHealth() == 1 ? " ❤️" : "")))
            );
        }

        // 총알 슬롯 상태 업데이트
        bulletPositions = response.getChambers();
        String chamberStatus = bulletPositions.stream()
                .map(bulletPosition -> " " + (bulletPosition ? "O " : "X "))
                .collect(Collectors.joining("", "\uD83D\uDCA1총알 슬롯 상태: " + "슬롯 ", ""));
        logMessage(chamberStatus+"\n");
    }

    // MVP 투표 UI 출력
    public void votePlayer() {
        CharacterTemplate voteCharacter = selectVote("제일 활약이 좋았던 플레이어를 고르세요.");
        String mvpPlayer = voteCharacter.getName();
        if (mvpPlayer == null) return;

        client.sendVote(mvpPlayer);
    }

    private void typeText(JLabel label, String text) {
        Font dokdoFont = loadCustomFont("/resources/Dokdo.ttf", 40f);

        new Thread(() -> {
            for (int i = 0; i <= text.length(); i++) {
                final String subText = text.substring(0, i);
                SwingUtilities.invokeLater(() -> {
                    label.setFont(dokdoFont);
                    label.setText(subText);
                });
                try {
                    Thread.sleep(50); // 각 글자가 표시되는 속도 조절 (50밀리초)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public CharacterTemplate getAssignedCharacter(String nickname) {
        // 현재 플레이어 리스트에서 닉네임과 일치하는 캐릭터를 반환
        for (CharacterTemplate character : characters) {
            if (character.getName().equals(nickname)) {
                return character;
            }
        }
        return null; // 일치하는 캐릭터가 없을 경우 null 반환
    }

    private Font loadCustomFont(String path, float size) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream(path));
            return font.deriveFont(size);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("나눔 고딕", Font.PLAIN, (int) size); // 기본 폰트로 대체
        }
    }

    private void showMVPDialog(String mvpPlayer) {

        // 다이얼로그 생성
        JDialog dialog = new JDialog(frame, "🎉 MVP 선정 🎉", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(new Color(255, 240, 200)); // 따뜻한 배경색

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.CENTER;

        // 메시지 라벨
        JLabel messageLabel = new JLabel("투표 결과 MVP는 " + mvpPlayer + "님입니다!");
        messageLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        messageLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(messageLabel, gbc);

        // 확인 버튼
        JButton okButton = new JButton("확인");
        styleMVPButton(okButton);
        okButton.addActionListener(e -> dialog.dispose());
        gbc.gridy = 1;
        dialog.add(okButton, gbc);

        dialog.setVisible(true);
    }

    private void styleMVPButton(JButton button) {
        button.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(100, 40));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }


    private void updateCharacterInList(CharacterTemplate updatedCharacter) {
        for (int i = 0; i < characters.size(); i++) {
            if (characters.get(i).getName().equals(updatedCharacter.getName())) {
                characters.set(i, updatedCharacter);
                break;
            }
        }
    }


}
