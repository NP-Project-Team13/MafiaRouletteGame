package client;

import characters.*;
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
            // 게임 시작
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
                textPane.setCaret(null);
                textPane.setBackground(backgroundColor);
                textPane.setForeground(textColor);
                textPane.setFont(new Font("Monospaced", Font.PLAIN, 14));

                // history.txt 파일을 읽어서 게임 히스토리 메뉴 표시
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

    public static void showCharacterDescriptions(CharacterTemplate character) {
        String description;
        String imagePath = "/resources/character.jpg";

//        if (character instanceof Character1) {
//            description = "<html><h2>캐릭터1: 총알의 위치를 확인하는 능력</h2>"
//                    + "<p>언제든지 비동기적으로 총알의 위치를 확인할 수 있습니다.<br>"
//                    + "능력을 사용하면 즉시 총알이 어느 슬롯에 있는지 확인할 수 있습니다.<br>"
//                    + "라운드마다 능력 사용 가능 상태가 초기화되며, 매 라운드마다 사용 가능합니다.</p></html>";
//        } else if (character instanceof Character2) {
//            description = "<html><h2>캐릭터2: 총알을 튕겨내는 방어 능력</h2>"
//                    + "<p>언제든지 비동기적으로 총알을 튕겨낼 준비를 할 수 있습니다.<br>"
//                    + "총에 맞을 경우 자동으로 총알을 튕겨냅니다.<br>"
//                    + "라운드마다 능력 사용 가능 상태가 초기화되며, 매 라운드마다 사용 가능합니다.</p></html>";
//        } else if (character instanceof Character3) {
//            description = "<html><h2>캐릭터3: 무작위 생존 결정 능력</h2>"
//                    + "<p>50:50 확률로 자신과 상대 중 한 명이 사망합니다.<br>"
//                    + "라운드마다 능력 사용 가능 상태가 초기화되며, 매 라운드마다 사용 가능합니다.</p></html>";
//        } else if (character instanceof Character4) {
//            description = "<html><h2>캐릭터4: 기관총 발사 능력</h2>"
//                    + "<p>기관총을 발사하여 적 전체를 공격합니다.<br>"
//                    + "총알이 없으면 턴이 종료됩니다.<br>"
//                    + "라운드마다 사용 가능합니다.</p></html>";
//        } else if (character instanceof Character5) {
//            description = "<html><h2>캐릭터5: 데미지 2배 능력</h2>"
//                    + "<p>상대가 총에 맞을 때 데미지를 2배로 줍니다.<br>"
//                    + "게임 중 1회만 사용 가능합니다.</p></html>";
//        } else if (character instanceof Character6) {
//            description = "<html><h2>캐릭터6: 힐러 능력</h2>"
//                    + "<p>자신의 체력을 소모해 아군을 치료할 수 있습니다.<br>"
//                    + "라운드마다 사용 가능합니다.</p></html>";
//        } else {
//            description = "<html><h2>알 수 없는 캐릭터입니다.</h2></html>";
//        }
        if (character instanceof Character1) {
            description = "<html>"
                    + "<h2>캐릭터1: 총알의 위치를 확인하는 능력</h2>"
                    + "<p>이 능력은 총알이 어느 슬롯에 있는지 확인할 수 있는 능력입니다.</p>"
                    + "<ul>"
                    + "<li><b>능력 설명:</b> 비동기적으로 총알의 위치를 확인합니다.</li>"
                    + "<li><b>사용 방법:</b> 능력을 활성화하면, 즉시 총알이 장전된 슬롯의 위치를 확인할 수 있습니다.</li>"
                    + "<li><b>쿨다운:</b> 라운드가 시작될 때마다 능력 사용 상태가 초기화되므로, 매 라운드마다 다시 사용 가능합니다.</li>"
                    + "<li><b>전략 포인트:</b> 적의 총알 패턴을 파악하여 방어 전략을 세울 수 있습니다.</li>"
                    + "</ul>"
                    + "</html>";
        } else if (character instanceof Character2) {
            description = "<html>"
                    + "<h2>캐릭터2: 총알을 튕겨내는 방어 능력</h2>"
                    + "<p>총에 맞을 때 자동으로 총알을 튕겨내는 방어형 능력입니다.</p>"
                    + "<ul>"
                    + "<li><b>능력 설명:</b> 비동기적으로 방어 태세를 준비하고, 총에 맞으면 자동으로 총알을 튕겨냅니다.</li>"
                    + "<li><b>사용 방법:</b> 방어 준비를 완료한 후, 공격받을 경우 자동으로 발동합니다.</li>"
                    + "<li><b>쿨다운:</b> 라운드마다 초기화되므로, 매 라운드마다 다시 준비할 수 있습니다.</li>"
                    + "<li><b>제약 사항:</b> 준비 상태에서만 발동되며, 준비하지 않으면 방어할 수 없습니다.</li>"
                    + "</ul>"
                    + "</html>";
        } else if (character instanceof Character3) {
            description = "<html>"
                    + "<h2>캐릭터3: 무작위 생존 결정 능력</h2>"
                    + "<p>50:50 확률로 자신과 상대 중 한 명이 사망하는 도박성 능력입니다.</p>"
                    + "<ul>"
                    + "<li><b>능력 설명:</b> 자신과 상대 중 한 명이 무작위로 선택되어 사망합니다.</li>"
                    + "<li><b>사용 방법:</b> 본인의 턴에 상대를 지목하면 확률에 따라 둘 중 한 명이 사망합니다.</li>"
                    + "<li><b>쿨다운:</b> 라운드가 초기화될 때마다 사용 상태가 리셋되며, 매 라운드마다 사용할 수 있습니다.</li>"
                    + "<li><b>전략 포인트:</b> 불리한 상황에서 역전을 노릴 수 있으나, 위험이 따릅니다.</li>"
                    + "</ul>"
                    + "</html>";
        } else if (character instanceof Character4) {
            description = "<html>"
                    + "<h2>캐릭터4: 기관총 발사 능력</h2>"
                    + "<p>기관총을 발사해 적 전체를 공격하는 강력한 공격형 능력입니다.</p>"
                    + "<ul>"
                    + "<li><b>능력 설명:</b> 기관총을 발사하여 적 전체에게 피해를 줍니다.</li>"
                    + "<li><b>사용 조건:</b> 총알이 있어야 능력이 발동됩니다. 총알이 없으면 턴이 종료됩니다.</li>"
                    + "<li><b>쿨다운:</b> 라운드마다 초기화되며, 매 라운드마다 사용할 수 있습니다.</li>"
                    + "<li><b>전략 포인트:</b> 적의 수가 많을 때 유리하며, 적 전체에 큰 피해를 줄 수 있습니다.</li>"
                    + "</ul>"
                    + "</html>";
        } else if (character instanceof Character5) {
            description = "<html>"
                    + "<h2>캐릭터5: 데미지 2배 능력</h2>"
                    + "<p>상대에게 입히는 데미지를 두 배로 증가시키는 결정적 능력입니다.</p>"
                    + "<ul>"
                    + "<li><b>능력 설명:</b> 총에 맞은 상대에게 데미지를 2배로 줍니다.</li>"
                    + "<li><b>사용 조건:</b> 게임 전체에서 단 한 번만 사용할 수 있습니다.</li>"
                    + "<li><b>전략 포인트:</b> 중요한 순간에 사용하여 승기를 잡을 수 있습니다.</li>"
                    + "</ul>"
                    + "</html>";
        } else if (character instanceof Character6) {
            description = "<html>"
                    + "<h2>캐릭터6: 힐러 능력</h2>"
                    + "<p>자신의 체력을 소모하여 아군의 체력을 회복할 수 있는 지원형 능력입니다.</p>"
                    + "<ul>"
                    + "<li><b>능력 설명:</b> 자신의 체력을 1 소모하고, 아군의 체력을 1 회복시킵니다.</li>"
                    + "<li><b>사용 방법:</b> 본인의 턴에서 공격 대신 회복을 선택하여 사용합니다.</li>"
                    + "<li><b>쿨다운:</b> 라운드마다 초기화되며, 매 라운드마다 다시 사용할 수 있습니다.</li>"
                    + "<li><b>제약 사항:</b> 본인의 체력이 소진되면 더 이상 사용할 수 없습니다.</li>"
                    + "</ul>"
                    + "</html>";
        } else {
            description = "<html>"
                    + "<h2>알 수 없는 캐릭터입니다.</h2>"
                    + "<p>이 캐릭터에 대한 정보가 존재하지 않습니다.</p>"
                    + "</html>";
        }


        // 커스텀 다이얼로그 생성
        showCustomDialog(description, imagePath);
    }

    private static void showCustomDialog(String description, String imagePath) {
        // 다이얼로그 설정
        JDialog dialog = new JDialog();
        dialog.setTitle("당신의 캐릭터 설명");
        dialog.setSize(700, 700);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        // 메인 패널
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 이미지 추가
        JLabel imageLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(MainMenu.class.getResource(imagePath));
            Image img = icon.getImage().getScaledInstance(dialog.getWidth() - 20, -1, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        } catch (Exception e) {
            imageLabel.setText("이미지를 불러올 수 없습니다.");
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }

        // 설명 추가 (HTML을 지원하는 JLabel 사용)
        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        descriptionLabel.setVerticalAlignment(SwingConstants.TOP);
        descriptionLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // 설명 패널에 여백 추가
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setBackground(Color.WHITE);
        descriptionPanel.add(descriptionLabel, BorderLayout.CENTER);
        descriptionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // 닫기 버튼
        JButton closeButton = new JButton("닫기");
        closeButton.addActionListener(e -> dialog.dispose());
        closeButton.setBackground(new Color(220, 53, 69));
        closeButton.setForeground(Color.BLACK);
        closeButton.setFocusPainted(false);
        closeButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        buttonPanel.setBackground(Color.WHITE);

        // 컴포넌트 배치
        panel.add(imageLabel, BorderLayout.NORTH);
        panel.add(descriptionPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }




}