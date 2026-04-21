import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class SnakeGame {
    private static final int WINDOW_SIZE = 600;
    private static final int CELL_SIZE = 30;
    private static final int GRID_COUNT = WINDOW_SIZE / CELL_SIZE;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(WINDOW_SIZE, WINDOW_SIZE);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new GamePanel());
            frame.setVisible(true);
        });
    }

    static class GamePanel extends JPanel {
        private static final Color BACKGROUND_COLOR = Color.DARK_GRAY;
        private static final Color GRID_LINE_COLOR = new Color(85, 85, 85);
        private static final Color SNAKE_COLOR = Color.GREEN;

        GamePanel() {
            setPreferredSize(new Dimension(WINDOW_SIZE, WINDOW_SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, WINDOW_SIZE, WINDOW_SIZE);

            g.setColor(GRID_LINE_COLOR);
            for (int i = 0; i <= GRID_COUNT; i++) {
                int pos = i * CELL_SIZE;
                g.drawLine(pos, 0, pos, WINDOW_SIZE);
                g.drawLine(0, pos, WINDOW_SIZE, pos);
            }

            int centerX = GRID_COUNT / 2;
            int centerY = GRID_COUNT / 2;

            // Head is the right-most segment so the snake faces right.
            int[][] snakeSegments = {
                    {centerX - 2, centerY},
                    {centerX - 1, centerY},
                    {centerX, centerY}
            };

            g.setColor(SNAKE_COLOR);
            for (int[] segment : snakeSegments) {
                g.fillRect(segment[0] * CELL_SIZE, segment[1] * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }
}
