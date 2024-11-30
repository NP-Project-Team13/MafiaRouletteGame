import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
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
                    URL imageUrl = getClass().getResource("/image/home.png");
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
        JButton rankingButton = createStyledButton("View Ranking");
        JButton exitButton = createStyledButton("Exit");

        // 버튼 이벤트
        startButton.addActionListener(e -> {
            frame.dispose();
            gameUI.createAndShowGUI();
        });

        rulesButton.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "게임 규칙:\n1. 4명의 플레이어가 2인 1팀으로 참여합니다.\n" +
                        "2. 무작위로 총알을 배치하고, 각 플레이어는 능력을 사용해 팀을 승리로 이끕니다.\n" +
                        "3. 상대팀을 모두 제거하면 승리!",
                "게임 규칙", JOptionPane.INFORMATION_MESSAGE));

        rankingButton.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "랭킹 기능은 아직 구현되지 않았습니다!", "랭킹 보기", JOptionPane.INFORMATION_MESSAGE));

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
        buttonPanel.add(rankingButton, gbc);

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