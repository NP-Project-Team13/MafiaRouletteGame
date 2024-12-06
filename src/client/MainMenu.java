package client;

import client.MafiaClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class MainMenu {
    public static void createAndShowGUI(MafiaClient client, GameUI gameUI) {

        JFrame frame = new JFrame("Mafia Roulette Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // 배경 이미지 로드 및 설정
        JPanel backgroundPanel = new JPanel() {
            private Image backgroundImage;

            {
                try {
                    URL imageUrl = getClass().getResource("/resources/home.png");
                    if (imageUrl != null) {
                        backgroundImage = ImageIO.read(imageUrl).getScaledInstance(800, 600, Image.SCALE_SMOOTH);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, this);
                } else {
                    // 배경 이미지가 없을 경우 기본 색상
                    setBackground(new Color(34, 40, 49));
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        // 타이틀 설정
        JLabel titleLabel = new JLabel("Mafia Roulette", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 60));
        titleLabel.setForeground(Color.RED);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        backgroundPanel.add(titleLabel, BorderLayout.NORTH);

        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // 배경 투명
        buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // 버튼 사이 여백

        // 버튼 생성
        JButton startButton = createStyledButton("Start Game");
        JButton rulesButton = createStyledButton("View Rules");
        JButton historyButton = createStyledButton("Game History");
        JButton exitButton = createStyledButton("Exit");

        // 버튼 이벤트
        startButton.addActionListener(e -> {
            frame.dispose();
            gameUI.createAndShowGUI();
            client.sendReady();
        });

        rulesButton.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "게임 규칙:\n1. 4명의 플레이어가 2인 1팀으로 참여합니다.\n" +
                        "2. 무작위로 총알을 배치하고, 각 플레이어는 능력을 사용해 팀을 승리로 이끕니다.\n" +
                        "3. 상대팀을 모두 제거하면 승리!",
                "게임 규칙", JOptionPane.INFORMATION_MESSAGE));

        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 새 창 생성
                JFrame historyPopup = new JFrame("Game History");
                historyPopup.setSize(800, 400);
                historyPopup.setLayout(new BorderLayout());

                // 색상 설정
                Color backgroundColor = new Color(240, 240, 240); // 밝은 회색
                Color textColor = new Color(50, 50, 50); // 어두운 회색
                Color winColor = new Color(0, 0, 255); // 파란색
                Color loseColor = new Color(255, 0, 0); // 빨간색

                // 상단 제목 라벨
                JLabel titleLabel = new JLabel("Game History", SwingConstants.CENTER);
                titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
                titleLabel.setOpaque(true);
                titleLabel.setBackground(backgroundColor);
                titleLabel.setForeground(textColor);
                titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
                historyPopup.add(titleLabel, BorderLayout.NORTH);

                // 텍스트 출력 영역 (JTextPane)
                JTextPane textPane = new JTextPane();
                textPane.setEditable(false);
                textPane.setBackground(backgroundColor);
                textPane.setForeground(textColor);
                textPane.setFont(new Font("Monospaced", Font.PLAIN, 14));

                // 텍스트 내용 추가
                String filepath = "src/resources/history.txt";
                StyledDocument doc = textPane.getStyledDocument();
                SimpleAttributeSet normal = new SimpleAttributeSet();
                StyleConstants.setFontFamily(normal, "Monospaced");
                StyleConstants.setFontSize(normal, 14);
                StyleConstants.setForeground(normal, textColor);
                StyleConstants.setAlignment(normal, StyleConstants.ALIGN_CENTER);

                SimpleAttributeSet winStyle = new SimpleAttributeSet(normal);
                StyleConstants.setForeground(winStyle, winColor);
                StyleConstants.setBold(winStyle, true);

                SimpleAttributeSet loseStyle = new SimpleAttributeSet(normal);
                StyleConstants.setForeground(loseStyle, loseColor);
                StyleConstants.setBold(loseStyle, true);

                try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] result = line.split(" ");
                        String date = result[0];
                        String P1 = result[1], P2 = result[2];
                        String P3 = result[3], P4 = result[4];
                        String winningTeam = result[5];
                        String MVP = result[6];

                        String formattedLine = date + "   "
                                + "Team A (" + P1 + ", " + P2 + ")   ";

                        // 기본 텍스트 추가
                        doc.insertString(doc.getLength(), formattedLine, normal);

                        // Win / Lose 스타일 추가
                        if (winningTeam.equals("A")) {
                            doc.insertString(doc.getLength(), "Win", winStyle);
                            doc.insertString(doc.getLength(), " : ", normal);
                            doc.insertString(doc.getLength(), "Lose", loseStyle);
                        } else {
                            doc.insertString(doc.getLength(), "Lose", loseStyle);
                            doc.insertString(doc.getLength(), " : ", normal);
                            doc.insertString(doc.getLength(), "Win", winStyle);
                        }

                        String teamB = "   Team B (" + P3 + ", " + P4 + ")   MVP: ";
                        doc.insertString(doc.getLength(), teamB, normal);

                        // MVP 강조
                        doc.insertString(doc.getLength(), MVP + "\n\n", normal);
                    }
                    doc.setParagraphAttributes(0, doc.getLength(), normal, false);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(historyPopup, "Error reading history file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }

                // 텍스트 패딩 및 스크롤 추가
                textPane.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                JScrollPane scrollPane = new JScrollPane(textPane);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                historyPopup.add(scrollPane, BorderLayout.CENTER);

                // 새 창 위치 및 표시
                historyPopup.setLocationRelativeTo(null);
                historyPopup.setVisible(true);
            }
        });



        exitButton.addActionListener(e -> System.exit(0));

        // 버튼 추가
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(startButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        buttonPanel.add(rulesButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        buttonPanel.add(historyButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        buttonPanel.add(exitButton, gbc);

        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);

        frame.add(backgroundPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(180, 60));
        button.setOpaque(true);
        button.setBackground(new Color(50, 50, 50));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.RED, 2));

        // Hover 효과
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(70, 70, 70));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(50, 50, 50));
            }
        });

        return button;
    }
}