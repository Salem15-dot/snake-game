import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Deque;

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

        private enum Direction {
            UP, DOWN, LEFT, RIGHT
        }

        private final Deque<Point> snake = new ArrayDeque<>();
        private Direction currentDirection = Direction.RIGHT;
        private Direction pendingDirection = Direction.RIGHT;
        private final Timer gameTimer;

        GamePanel() {
            setPreferredSize(new Dimension(WINDOW_SIZE, WINDOW_SIZE));
            setFocusable(true);

            int centerX = GRID_COUNT / 2;
            int centerY = GRID_COUNT / 2;
            snake.addLast(new Point(centerX - 2, centerY));
            snake.addLast(new Point(centerX - 1, centerY));
            snake.addLast(new Point(centerX, centerY));

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    Direction requestedDirection = switch (e.getKeyCode()) {
                        case KeyEvent.VK_UP -> Direction.UP;
                        case KeyEvent.VK_DOWN -> Direction.DOWN;
                        case KeyEvent.VK_LEFT -> Direction.LEFT;
                        case KeyEvent.VK_RIGHT -> Direction.RIGHT;
                        default -> null;
                    };

                    if (requestedDirection != null && !isOpposite(requestedDirection, currentDirection)) {
                        pendingDirection = requestedDirection;
                    }
                }
            });

            gameTimer = new Timer(150, this::onTimerTick);
            gameTimer.start();
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

            g.setColor(SNAKE_COLOR);
            for (Point segment : snake) {
                g.fillRect(segment.x * CELL_SIZE, segment.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        @Override
        public void addNotify() {
            super.addNotify();
            requestFocusInWindow();
        }

        private void onTimerTick(ActionEvent ignored) {
            currentDirection = pendingDirection;

            Point head = snake.peekLast();
            int nextX = head.x;
            int nextY = head.y;

            switch (currentDirection) {
                case UP -> nextY--;
                case DOWN -> nextY++;
                case LEFT -> nextX--;
                case RIGHT -> nextX++;
            }

            if (nextX < 0) {
                nextX = GRID_COUNT - 1;
            } else if (nextX >= GRID_COUNT) {
                nextX = 0;
            }

            if (nextY < 0) {
                nextY = GRID_COUNT - 1;
            } else if (nextY >= GRID_COUNT) {
                nextY = 0;
            }

            snake.addLast(new Point(nextX, nextY));
            snake.removeFirst();
            repaint();
        }

        private boolean isOpposite(Direction first, Direction second) {
            return (first == Direction.UP && second == Direction.DOWN)
                    || (first == Direction.DOWN && second == Direction.UP)
                    || (first == Direction.LEFT && second == Direction.RIGHT)
                    || (first == Direction.RIGHT && second == Direction.LEFT);
        }

        private static class Point {
            private final int x;
            private final int y;

            private Point(int x, int y) {
                this.x = x;
                this.y = y;
            }
        }
    }
}
