import mafia.character.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class GameUI {
    private JTextArea gameLog;
    private JLabel turnLabel;
    private JPanel playerInfoPanel;
    private Player[] players;
    private int currentPlayerIndex;
    private int roundNumber = 1;
    private static final int CYLINDER_SIZE = 5;
    private boolean[] bulletPositions;
    private int turnCounter = 0;
    private int currentSlot = 1;

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Mafia Roulette - Game Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 1000);

        initializePlayers();

        JPanel gameViewPanel = new JPanel(new BorderLayout());
        gameViewPanel.setBackground(Color.DARK_GRAY);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // 뒤로가기 버튼
        JButton backButton = new JButton("뒤로가기");
        backButton.setFont(new Font("Serif", Font.BOLD, 16));
        backButton.addActionListener(e -> goBack(frame)); // 뒤로가기 동작 연결

        turnLabel = new JLabel("현재 턴: " + players[currentPlayerIndex].getName() + " | 라운드: " + roundNumber, SwingConstants.CENTER);
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

        logMessage("게임이 시작되었습니다. " + players[currentPlayerIndex].getName() + "의 턴입니다.");
    }

    private void initializePlayers() {
        players = new Player[8];
        players[0] = new Player("Player 1", new Character1("name1", "Team A"));
        players[1] = new Player("Player 2", new Character2("name2", "Team A"));
        players[2] = new Player("Player 3", new Character3("name3", "Team A"));
        players[3] = new Player("Player 4", new Character4("name4", "Team A"));
        players[4] = new Player("Player 5", new Character5("name5", "Team B"));
        players[5] = new Player("Player 6", new Character6("name6", "Team B"));
        players[6] = new Player("Player 7", new Character7("name7", "Team B"));
        players[7] = new Player("Player 8", new Character7("name8", "Team B"));
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
        for (Player player : players) {
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
            playerPanel.setBorder(BorderFactory.createTitledBorder(player.getName()));

            JLabel playerInfo = new JLabel(
                    String.format(" [팀] %s [체력] %d [능력] %s",
                            player.getCharacter().getTeam(),
                            player.getCharacter().getHealth(),
                            player.getCharacter().getInfo())
            );
            playerPanel.add(playerInfo);

            JButton shootButton = new JButton("Shoot");
            shootButton.addActionListener(e -> shoot(player));

            JButton abilityButton = new JButton("Use Ability");
            abilityButton.addActionListener(e -> useAbility(player));

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
            currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
        } while (!players[currentPlayerIndex].getCharacter().isAlive());

        turnCounter++;

        // 살아있는 플레이어의 턴이 모두 끝난 경우
        if (turnCounter >= alivePlayerCount()) {
            logMessage("모든 플레이어가 턴을 완료했습니다. 라운드 " + roundNumber + " 종료!");
            roundNumber++;
            resetPlayerAbilities();
            turnCounter = 0;
        }

        updateTurnLabel(); // 현재 턴 레이블 업데이트
        logMessage("▶ " + players[currentPlayerIndex].getName() + "의 턴입니다.");
        updatePlayerInfoPanel(); // UI 갱신
    }

    private void shoot(Player currentPlayer) {
        Player target = selectTarget("타겟을 선택하세요");
        if (target == null) return;

        logMessage(currentPlayer.getName() + "이(가) " + target.getName() + "을(를) 향해 슬롯 " + currentSlot + "에서 총을 쏩니다!");

        if (bulletPositions[currentSlot - 1]) { // 슬롯은 1 기반으로 표시하므로 배열 접근은 -1
            logMessage("총알 발사 성공! 슬롯 " + currentSlot + "에 총알이 있었습니다.");
            currentPlayer.getCharacter().shoot(target.getCharacter());
            bulletPositions[currentSlot - 1] = false;
        } else {
            logMessage("빈 슬롯입니다. 총알이 발사되지 않았습니다.");
        }

        // 슬롯 증가
        currentSlot = (currentSlot % CYLINDER_SIZE) + 1; // 다음 슬롯으로 이동 (1 기반)

        // 총알이 모두 소모되었는지 확인
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



    private void useAbility(Player currentPlayer) {
        Character character = currentPlayer.getCharacter();

        if (character.isAbilityUsed()) {
            logMessage(currentPlayer.getName() + "은(는) 이미 능력을 사용했습니다.");
            return;
        }

        // 캐릭터 능력 호출
        switch (character.getClass().getSimpleName()) {
            case "Character1", "Character2", "Character5", "Character7":
                character.useAbility();
                break;
            case "Character3": {
                Player target = selectTarget("순서를 교체할 플레이어를 선택하세요");
                if (target == null) return;
                character.useAbility(target.getCharacter());
                swapPlayers(currentPlayer, target);
                break;
            }
            case "Character4": {
                // 40% 확률로 아군, 60% 확률로 적군 선택
                Random random = new Random();
                String targetTeam = random.nextInt(100) < 40 ? character.getTeam() : // 40% 확률로 아군 선택
                        (character.getTeam().equals("Team A") ? "Team B" : "Team A"); // 60% 확률로 적군 선택

                // targetTeam에 해당하는 캐릭터를 직접 추출
                ArrayList<Character> targetCharacters = new ArrayList<>();
                for (Player player : players) {
                    if (player.getCharacter().getTeam().equals(targetTeam)) {
                        targetCharacters.add(player.getCharacter());
                    }
                }

                // 추출된 캐릭터들을 useAbility에 전달
                character.useAbility(targetCharacters.toArray(new Character[0]));
                break;
            }
            case "Character6": {
                Player target = selectTarget("체력을 회복할 플레이어를 선택하세요");
                if (target == null) return;
                character.useAbility(target.getCharacter());
                break;
            }
            default:
                logMessage("이 캐릭터는 특별한 능력이 없습니다.");
                return;
        }

        logMessage(currentPlayer.getName() + "이(가) 능력을 사용했습니다.");
        updatePlayerInfoPanel();
    }


    private void swapPlayers(Player player1, Player player2) { // 두번째 파라미터가 바꿈 당할 대상
        int index1 = findPlayerIndex(player1);
        int index2 = findPlayerIndex(player2);
         // 플레이어 순서 교체
        Player temp = players[index1];
        players[index1] = players[index2];
        players[index2] = temp;

        if (currentPlayerIndex == index2){
            currentPlayerIndex = index1;
        }

        // UI 업데이트
        logMessage(player1.getName() + "과(와) " + player2.getName() + "의 순서를 교체했습니다.");
        updateTurnLabel(); // 현재 턴 레이블 갱신
    }

    private void updateTurnLabel() {
        turnLabel.setText("현재 턴: " + players[currentPlayerIndex].getName() + " | 라운드: " + roundNumber);
    }

    private int findPlayerIndex(Player player) {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == player) {
                return i;
            }
        }
        return -1;
    }

    private Player selectTarget(String message) {
        return (Player) JOptionPane.showInputDialog(
                null,
                message,
                "대상 선택",
                JOptionPane.QUESTION_MESSAGE,
                null,
                players,
                players[0]
        );
    }


    private void resetPlayerAbilities() {
        for (Player player : players) {
            if (player.getCharacter().isAlive()) {
                player.getCharacter().resetAbilityUsage();
            }
        }
    }

    private void logMessage(String message) {
        gameLog.append(message + "\n");
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
            MainMenu.createAndShowGUI(); // 메인 메뉴 화면으로 돌아가기
        }
    }

    private int alivePlayerCount() {
        int count = 0;
        for (Player player : players) {
            if (player.getCharacter().isAlive()) {
                count++;
            }
        }
        return count;
    }


}