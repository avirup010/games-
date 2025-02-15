import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class PixelJumper extends JPanel implements ActionListener, KeyListener {
    // Window dimensions
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    // Game constants
    private static final int PLAYER_WIDTH = 32;
    private static final int PLAYER_HEIGHT = 48;
    private static final int PLATFORM_HEIGHT = 16;
    private static final int COIN_SIZE = 16;
    private static final int GRAVITY = 1;
    private static final int JUMP_FORCE = 15;
    private static final int MOVE_SPEED = 5;
    
    // Game state
    private Player player;
    private ArrayList<Platform> platforms;
    private ArrayList<Coin> coins;
    private int score = 0;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private Random random = new Random();
    private Timer timer;
    private Color backgroundColor = new Color(135, 206, 235); // Sky blue
    
    // Double buffering
    private BufferedImage offScreenImage;
    private Graphics2D offScreenGraphics;
    
    // Constructor
    public PixelJumper() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        
        // Initialize offscreen buffer
        offScreenImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        offScreenGraphics = offScreenImage.createGraphics();
        offScreenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Start game timer
        timer = new Timer(1000/60, this); // 60 FPS
        timer.start();
        
        resetGame();
    }
    
    // Reset/initialize the game
    private void resetGame() {
        // Create player
        player = new Player(WIDTH / 2 - PLAYER_WIDTH / 2, HEIGHT / 2);
        
        // Create platforms
        platforms = new ArrayList<>();
        // Add starting platform
        platforms.add(new Platform(WIDTH / 2 - 50, HEIGHT / 2 + 50, 100));
        
        // Generate initial platforms
        for (int i = 0; i < 10; i++) {
            addPlatform();
        }
        
        // Create coins
        coins = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            addCoin();
        }
        
        score = 0;
        gameOver = false;
    }
    
    // Add a new platform
    private void addPlatform() {
        int x = random.nextInt(WIDTH - 100);
        int y = random.nextInt(HEIGHT - 200) + 100;
        int width = random.nextInt(70) + 50; // Random width between 50 and 120
        platforms.add(new Platform(x, y, width));
    }
    
    // Add a new coin
    private void addCoin() {
        int x = random.nextInt(WIDTH - COIN_SIZE);
        int y = random.nextInt(HEIGHT - 300); // Keep coins in the upper part
        coins.add(new Coin(x, y));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted && !gameOver) {
            updateGame();
        }
        repaint();
    }
    
// ... existing code ...

private void updateGame() {
    // Update player
    player.update();
    
    // Check if player fell off the bottom
    if (player.y > HEIGHT) {
        gameOver = true;
        return;
    }
    
    // Check platform collisions
    player.onGround = false;
    for (Platform platform : platforms) {
        if (player.dy > 0 && // Moving downward
            player.x + PLAYER_WIDTH > platform.x &&
            player.x < platform.x + platform.width &&
            player.y + PLAYER_HEIGHT > platform.y &&
            player.y + PLAYER_HEIGHT < platform.y + PLATFORM_HEIGHT + 10) {
            player.y = platform.y - PLAYER_HEIGHT;
            player.dy = 0;
            player.onGround = true;
        }
    }
    
    // Create lists to track changes
    ArrayList<Coin> coinsToAdd = new ArrayList<>();
    ArrayList<Coin> coinsToRemove = new ArrayList<>();
    
    // Check coin collisions
    for (Coin coin : coins) {
        if (player.x + PLAYER_WIDTH > coin.x &&
            player.x < coin.x + COIN_SIZE &&
            player.y + PLAYER_HEIGHT > coin.y &&
            player.y < coin.y + COIN_SIZE) {
            score += 10;
            coinsToRemove.add(coin);
            coinsToAdd.add(new Coin(
                random.nextInt(WIDTH - COIN_SIZE),
                random.nextInt(HEIGHT - 300)
            ));
        }
    }
    
    // Apply coin changes after iteration
    coins.removeAll(coinsToRemove);
    coins.addAll(coinsToAdd);
    
    // Scroll the world when player goes above half screen
    if (player.y < HEIGHT / 3) {
        int offset = HEIGHT / 3 - (int)player.y;
        player.y += offset;
        
        // Move platforms down
        for (Platform platform : platforms) {
            platform.y += offset;
        }
        
        // Move coins down
        for (Coin coin : coins) {
            coin.y += offset;
        }
        
        // Use separate collections to track additions and removals
        ArrayList<Platform> platformsToAdd = new ArrayList<>();
        ArrayList<Platform> platformsToRemove = new ArrayList<>();
        
        // Check platforms that went off-screen
        for (Platform platform : platforms) {
            if (platform.y > HEIGHT) {
                platformsToRemove.add(platform);
                platformsToAdd.add(new Platform(
                    random.nextInt(WIDTH - 100),
                    random.nextInt(HEIGHT - 200) + 100,
                    random.nextInt(70) + 50
                ));
                score++; // Increment score for each platform passed
            }
        }
        
        // Apply platform changes after iteration
        platforms.removeAll(platformsToRemove);
        platforms.addAll(platformsToAdd);
        
        // Check coins that went off-screen
        coinsToAdd.clear();
        coinsToRemove.clear();
        
        for (Coin coin : coins) {
            if (coin.y > HEIGHT) {
                coinsToRemove.add(coin);
                coinsToAdd.add(new Coin(
                    random.nextInt(WIDTH - COIN_SIZE),
                    random.nextInt(HEIGHT - 300)
                ));
            }
        }
        
        // Apply coin changes after iteration
        coins.removeAll(coinsToRemove);
        coins.addAll(coinsToAdd);
    }
}

    
    @Override
    protected void paintComponent(Graphics g) {
        // Draw to offscreen buffer
        offScreenGraphics.setColor(backgroundColor);
        offScreenGraphics.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Draw clouds (simple)
        offScreenGraphics.setColor(new Color(255, 255, 255, 180));
        drawCloud(offScreenGraphics, 100, 100, 60);
        drawCloud(offScreenGraphics, 300, 180, 50);
        drawCloud(offScreenGraphics, 600, 120, 70);
        
        if (!gameStarted) {
            // Draw start screen
            offScreenGraphics.setColor(Color.WHITE);
            offScreenGraphics.setFont(new Font("Arial", Font.BOLD, 32));
            offScreenGraphics.drawString("PIXEL JUMPER", WIDTH/2 - 120, HEIGHT/2 - 50);
            offScreenGraphics.setFont(new Font("Arial", Font.PLAIN, 18));
            offScreenGraphics.drawString("Press SPACE to start", WIDTH/2 - 100, HEIGHT/2);
            offScreenGraphics.drawString("Use LEFT/RIGHT arrows to move", WIDTH/2 - 140, HEIGHT/2 + 30);
            offScreenGraphics.drawString("Press SPACE to jump", WIDTH/2 - 100, HEIGHT/2 + 60);
        } else if (gameOver) {
            // Draw game over screen
            offScreenGraphics.setColor(Color.WHITE);
            offScreenGraphics.setFont(new Font("Arial", Font.BOLD, 32));
            offScreenGraphics.drawString("GAME OVER", WIDTH/2 - 100, HEIGHT/2 - 50);
            offScreenGraphics.setFont(new Font("Arial", Font.PLAIN, 24));
            offScreenGraphics.drawString("Score: " + score, WIDTH/2 - 50, HEIGHT/2);
            offScreenGraphics.setFont(new Font("Arial", Font.PLAIN, 18));
            offScreenGraphics.drawString("Press R to restart", WIDTH/2 - 80, HEIGHT/2 + 50);
        } else {
            // Draw platforms
            for (Platform platform : platforms) {
                platform.draw(offScreenGraphics);
            }
            
            // Draw coins
            for (Coin coin : coins) {
                coin.draw(offScreenGraphics);
            }
            
            // Draw player
            player.draw(offScreenGraphics);
            
            // Draw score
            offScreenGraphics.setColor(Color.WHITE);
            offScreenGraphics.setFont(new Font("Arial", Font.BOLD, 20));
            offScreenGraphics.drawString("Score: " + score, 20, 30);
        }
        
        // Copy offscreen buffer to screen
        g.drawImage(offScreenImage, 0, 0, this);
    }
    
    private void drawCloud(Graphics2D g, int x, int y, int size) {
        int halfSize = size / 2;
        g.fillOval(x, y, size, size);
        g.fillOval(x + halfSize, y - 10, size, size);
        g.fillOval(x + size, y, size, size);
        g.fillOval(x + halfSize, y + 10, size, size);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (!gameStarted) {
            if (key == KeyEvent.VK_SPACE) {
                gameStarted = true;
            }
            return;
        }
        
        if (gameOver) {
            if (key == KeyEvent.VK_R) {
                resetGame();
                gameStarted = true;
            }
            return;
        }
        
        if (key == KeyEvent.VK_LEFT) {
            player.movingLeft = true;
        } else if (key == KeyEvent.VK_RIGHT) {
            player.movingRight = true;
        } else if (key == KeyEvent.VK_SPACE && player.onGround) {
            player.jump();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT) {
            player.movingLeft = false;
        } else if (key == KeyEvent.VK_RIGHT) {
            player.movingRight = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
    
    // Player class
    private class Player {
        private double x, y;
        private double dy = 0;
        private boolean movingLeft = false;
        private boolean movingRight = false;
        private boolean onGround = false;
        private Color bodyColor = new Color(255, 200, 150);  // Light peach for skin tone
        private Color clothesColor = new Color(70, 130, 180);  // Steel blue for clothes
        
        public Player(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public void update() {
            if (movingLeft) {
                x -= MOVE_SPEED;
                if (x < 0) x = 0;
            }
            if (movingRight) {
                x += MOVE_SPEED;
                if (x > WIDTH - PLAYER_WIDTH) x = WIDTH - PLAYER_WIDTH;
            }
            
            // Apply gravity
            dy += GRAVITY;
            y += dy;
        }
        
        public void jump() {
            dy = -JUMP_FORCE;
            onGround = false;
        }
        
        public void draw(Graphics2D g) {
            // Body (clothes)
            g.setColor(clothesColor);
            g.fillRect((int)x, (int)y + PLAYER_HEIGHT/3, PLAYER_WIDTH, 2*PLAYER_HEIGHT/3);
            
            // Head
            g.setColor(bodyColor);
            g.fillOval((int)x + 4, (int)y, PLAYER_WIDTH - 8, PLAYER_HEIGHT/3);
            
            // Arms
            g.setColor(bodyColor);
            g.fillRect((int)x - 4, (int)y + PLAYER_HEIGHT/3, 6, PLAYER_HEIGHT/3);
            g.fillRect((int)x + PLAYER_WIDTH - 2, (int)y + PLAYER_HEIGHT/3, 6, PLAYER_HEIGHT/3);
            
            // Eyes
            g.setColor(Color.BLACK);
            if (movingLeft) {
                g.fillOval((int)x + 8, (int)y + 8, 4, 4);
                g.fillOval((int)x + 18, (int)y + 8, 4, 4);
            } else {
                g.fillOval((int)x + 10, (int)y + 8, 4, 4);
                g.fillOval((int)x + 20, (int)y + 8, 4, 4);
            }
            
            // Legs
            g.setColor(Color.BLACK);
            g.fillRect((int)x + 6, (int)y + 2*PLAYER_HEIGHT/3, 6, PLAYER_HEIGHT/3);
            g.fillRect((int)x + PLAYER_WIDTH - 12, (int)y + 2*PLAYER_HEIGHT/3, 6, PLAYER_HEIGHT/3);
        }
    }
    
    // Platform class
    private class Platform {
        private int x, y;
        private int width;
        private Color color = new Color(34, 139, 34);  // Forest green
        
        public Platform(int x, int y, int width) {
            this.x = x;
            this.y = y;
            this.width = width;
        }
        
        public void draw(Graphics2D g) {
            // Main platform
            g.setColor(color);
            g.fillRect(x, y, width, PLATFORM_HEIGHT);
            
            // Top grass
            g.setColor(new Color(124, 252, 0));  // Lawn green
            g.fillRect(x, y, width, 4);
            
            // Detail dots (representing grass tufts)
            g.setColor(new Color(173, 255, 47));  // Green yellow
            for (int i = 0; i < width; i += 8) {
                g.fillRect(x + i, y - 2, 2, 2);
            }
        }
    }
    
    // Coin class
    private class Coin {
        private int x, y;
        private Color color = new Color(255, 215, 0);  // Gold
        
        public Coin(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public void draw(Graphics2D g) {
            // Outer circle
            g.setColor(color);
            g.fillOval(x, y, COIN_SIZE, COIN_SIZE);
            
            // Inner circle (gives 3D effect)
            g.setColor(new Color(218, 165, 32));  // Goldenrod
            g.fillOval(x + 3, y + 3, COIN_SIZE - 6, COIN_SIZE - 6);
            
            // Highlight
            g.setColor(new Color(255, 255, 200));
            g.fillOval(x + 3, y + 3, 3, 3);
        }
    }
    
    // Main method
    public static void main(String[] args) {
        JFrame frame = new JFrame("Pixel Jumper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        PixelJumper game = new PixelJumper();
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}