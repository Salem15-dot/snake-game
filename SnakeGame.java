import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

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
        private static final Color FOOD_COLOR = Color.RED;
        private static final int TICK_MS = 150;

        private enum Direction {
            UP, DOWN, LEFT, RIGHT
        }

        private final Deque<Point> snake = new ArrayDeque<>();
        private final Random random = new Random();
        private Direction currentDirection = Direction.RIGHT;
        private Direction pendingDirection = Direction.RIGHT;
        private final Timer gameTimer;
        private Point food;
        private int score;
        private boolean gameOver;

        GamePanel() {
            setPreferredSize(new Dimension(WINDOW_SIZE, WINDOW_SIZE));
            setFocusable(true);

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
                        resetGame();
                        return;
                    }

                    Direction requestedDirection = switch (e.getKeyCode()) {
                        case KeyEvent.VK_UP -> Direction.UP;
                        case KeyEvent.VK_DOWN -> Direction.DOWN;
                        case KeyEvent.VK_LEFT -> Direction.LEFT;
                        case KeyEvent.VK_RIGHT -> Direction.RIGHT;
                        default -> null;
                    };

                    if (!gameOver && requestedDirection != null && !isOpposite(requestedDirection, pendingDirection)) {
                        pendingDirection = requestedDirection;
                    }
                }
            });

            gameTimer = new Timer(TICK_MS, this::onTimerTick);
            resetGame();
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

            if (food != null) {
                g.setColor(FOOD_COLOR);
                g.fillRect(food.x * CELL_SIZE, food.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.drawString("Score: " + score, 12, 24);

            if (gameOver) {
                g.setFont(new Font("SansSerif", Font.BOLD, 34));
                g.drawString("Game Over", WINDOW_SIZE / 2 - 105, WINDOW_SIZE / 2 - 10);
                g.setFont(new Font("SansSerif", Font.BOLD, 20));
                g.drawString("Final Score: " + score, WINDOW_SIZE / 2 - 75, WINDOW_SIZE / 2 + 22);
                g.drawString("Press R to Restart", WINDOW_SIZE / 2 - 88, WINDOW_SIZE / 2 + 52);
            }
        }

        @Override
        public void addNotify() {
            super.addNotify();
            requestFocusInWindow();
        }

        private void onTimerTick(ActionEvent ignored) {
            if (gameOver) {
                return;
            }

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

            if (nextX < 0 || nextX >= GRID_COUNT || nextY < 0 || nextY >= GRID_COUNT) {
                gameOver = true;
                gameTimer.stop();
                repaint();
                return;
            }

            boolean ateFood = food != null && food.x == nextX && food.y == nextY;
            Point tail = snake.peekFirst();
            if (collidesWithSnake(nextX, nextY, ateFood ? null : tail)) {
                gameOver = true;
                gameTimer.stop();
                repaint();
                return;
            }

            snake.addLast(new Point(nextX, nextY));
            if (ateFood) {
                score++;
                spawnFood();
            } else {
                snake.removeFirst();
            }
            repaint();
        }

        private void resetGame() {
            snake.clear();

            int centerX = GRID_COUNT / 2;
            int centerY = GRID_COUNT / 2;
            snake.addLast(new Point(centerX - 2, centerY));
            snake.addLast(new Point(centerX - 1, centerY));
            snake.addLast(new Point(centerX, centerY));

            currentDirection = Direction.RIGHT;
            pendingDirection = Direction.RIGHT;
            score = 0;
            gameOver = false;
            spawnFood();
            gameTimer.start();
            requestFocusInWindow();
            repaint();
        }

        private void spawnFood() {
            if (snake.size() >= GRID_COUNT * GRID_COUNT) {
                food = null;
                return;
            }

            int x;
            int y;
            do {
                x = random.nextInt(GRID_COUNT);
                y = random.nextInt(GRID_COUNT);
            } while (collidesWithSnake(x, y, null));

            food = new Point(x, y);
        }

        private boolean collidesWithSnake(int x, int y, Point ignoredSegment) {
            for (Point segment : snake) {
                if (ignoredSegment != null && segment == ignoredSegment) {
                    continue;
                }
                if (segment.x == x && segment.y == y) {
                    return true;
                }
            }
            return false;
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
