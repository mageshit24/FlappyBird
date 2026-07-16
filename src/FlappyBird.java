import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * FlappyBird.java
 *
 * Core game panel: owns the game loop, physics, rendering, input, and
 * the overall game-state machine (START -> PLAYING -> PAUSED / GAME_OVER).
 *
 * Changes from the original version:
 *   - Introduces an explicit GameState enum instead of a single boolean
 *     `gameOver` flag, so start/pause/restart flows are unambiguous.
 *   - All previously package-private mutable fields are now private;
 *     external classes can no longer reach in and mutate game state.
 *   - Difficulty scales gradually with score (pipes speed up and the
 *     gap narrows), instead of staying flat for the whole game.
 *   - Adds a pause feature (P key), a start screen, and a redesigned
 *     game-over / HUD overlay with the persisted high score.
 *   - Adds simple drawn (non-image) polish: parallax clouds, a ground
 *     strip, a subtle score panel, and bird tilt - all vector-drawn,
 *     so no new image assets are required.
 *   - Resource loading is wrapped in error handling instead of letting
 *     a missing asset throw an unhandled NullPointerException.
 */
public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    // Standard Swing/Serializable requirement; this panel is never actually
    // serialized, but declaring it silences a compiler warning and follows
    // best practice for any Serializable subclass.
    private static final long serialVersionUID = 1L;

    /** Simple state machine for the game's lifecycle. */
    private enum GameState {
        START, PLAYING, PAUSED, GAME_OVER
    }

    // ---- Images ----
    private final Image backgroundImg;
    private final Image birdImg;
    private final Image topPipeImg;
    private final Image bottomPipeImg;

    // ---- Entities ----
    private Bird bird;
    private final List<Pipe> pipes = new ArrayList<>();
    private final Random random = new Random();

    // ---- Timers ----
    private final Timer gameLoop;
    private final Timer placePipeTimer;

    // ---- State ----
    private GameState state = GameState.START;
    private double score = 0;
    private int pipeSpeed = Constants.BASE_PIPE_SPEED;
    private int pipeGap = Constants.BASE_PIPE_GAP;

    // ---- Persistence ----
    private final HighScoreManager highScoreManager = new HighScoreManager();

    // ---- Decorative parallax clouds (purely visual, no gameplay effect) ----
    private final int[] cloudX = new int[4];
    private final int[] cloudY = new int[4];

    public FlappyBird() {
        setPreferredSize(new java.awt.Dimension(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        backgroundImg = loadImage(Constants.BACKGROUND_IMG);
        birdImg = loadImage(Constants.BIRD_IMG);
        topPipeImg = loadImage(Constants.TOP_PIPE_IMG);
        bottomPipeImg = loadImage(Constants.BOTTOM_PIPE_IMG);

        bird = new Bird(birdImg);
        initClouds();

        placePipeTimer = new Timer(Constants.PIPE_SPAWN_INTERVAL_MS, e -> {
            if (state == GameState.PLAYING) {
                placePipes();
            }
        });

        gameLoop = new Timer(1000 / Constants.FPS, this);
    }

    /** Starts the render/animation timers. Call once the panel is on-screen. */
    public void start() {
        gameLoop.start();
        placePipeTimer.start();
    }

    /**
     * Loads an image from the classpath. Missing/corrupt assets no longer
     * crash the whole application with a raw NullPointerException - we
     * fail loudly but gracefully with a clear message instead.
     */
    private Image loadImage(String resourcePath) {
        java.net.URL url = getClass().getResource(resourcePath);
        if (url == null) {
            throw new IllegalStateException("Missing required game asset on classpath: " + resourcePath);
        }
        return new ImageIcon(url).getImage();
    }

    private void initClouds() {
        for (int i = 0; i < cloudX.length; i++) {
            cloudX[i] = random.nextInt(Constants.BOARD_WIDTH);
            cloudY[i] = 20 + random.nextInt(140);
        }
    }

    private void placePipes() {
        int randomPipeY = (int) (-pipeGap / 2 - Constants.PIPE_HEIGHT / 4.0
                - random.nextDouble() * (Constants.PIPE_HEIGHT / 2.0));

        Pipe topPipe = new Pipe(topPipeImg, Constants.BOARD_WIDTH, randomPipeY,
                Constants.PIPE_WIDTH, Constants.PIPE_HEIGHT);
        pipes.add(topPipe);

        int bottomY = randomPipeY + Constants.PIPE_HEIGHT + pipeGap;
        Pipe bottomPipe = new Pipe(bottomPipeImg, Constants.BOARD_WIDTH, bottomY,
                Constants.PIPE_WIDTH, Constants.PIPE_HEIGHT);
        pipes.add(bottomPipe);
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        draw(g2);
    }

    private void draw(Graphics2D g) {
        g.drawImage(backgroundImg, 0, 0, Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT, null);

        drawClouds(g);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.getImage(), pipe.getX(), pipe.getY(), pipe.getWidth(), pipe.getHeight(), null);
        }

        drawGround(g);
        drawBird(g);
        drawHud(g);

        switch (state) {
            case START -> drawStartOverlay(g);
            case PAUSED -> drawPausedOverlay(g);
            case GAME_OVER -> drawGameOverOverlay(g);
            default -> { /* PLAYING: no overlay */ }
        }
    }

    private void drawClouds(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 140));
        for (int i = 0; i < cloudX.length; i++) {
            int cx = cloudX[i];
            int cy = cloudY[i];
            g.fillOval(cx, cy, 46, 18);
            g.fillOval(cx + 14, cy - 8, 34, 22);
            g.fillOval(cx + 30, cy, 40, 16);
        }
    }

    private void drawGround(Graphics2D g) {
        int groundY = Constants.BOARD_HEIGHT - Constants.GROUND_HEIGHT;
        GradientPaint groundPaint = new GradientPaint(
                0, groundY, new Color(222, 184, 112),
                0, Constants.BOARD_HEIGHT, new Color(180, 140, 80));
        g.setPaint(groundPaint);
        g.fillRect(0, groundY, Constants.BOARD_WIDTH, Constants.GROUND_HEIGHT);
        g.setColor(new Color(90, 60, 30));
        g.drawLine(0, groundY, Constants.BOARD_WIDTH, groundY);
    }

    private void drawBird(Graphics2D g) {
        java.awt.geom.AffineTransform old = g.getTransform();
        double cx = bird.getX() + bird.getWidth() / 2.0;
        double cy = bird.getY() + bird.getHeight() / 2.0;
        g.rotate(Math.toRadians(bird.getTiltDegrees()), cx, cy);
        g.drawImage(bird.getImage(), bird.getX(), bird.getY(), bird.getWidth(), bird.getHeight(), null);
        g.setTransform(old);
    }

    private void drawHud(Graphics2D g) {
        String scoreText = String.valueOf((int) score);
        g.setFont(new Font("SansSerif", Font.BOLD, 34));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(scoreText);

        int panelX = Constants.BOARD_WIDTH / 2 - textWidth / 2 - 16;
        int panelY = 12;
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(panelX, panelY, textWidth + 32, 46, 16, 16);

        g.setColor(Color.WHITE);
        g.drawString(scoreText, Constants.BOARD_WIDTH / 2 - textWidth / 2, panelY + 34);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(255, 255, 255, 220));
        g.drawString("Best: " + highScoreManager.getHighScore(), 12, 24);
    }

    private void drawStartOverlay(Graphics2D g) {
        drawDimOverlay(g);
        drawCenteredTitle(g, "FLAPPY BIRD", -60);
        drawCenteredSubtitle(g, "Press SPACE to start", -10);
        drawCenteredSubtitle(g, "P to pause  \u2022  SPACE to flap", 20);
    }

    private void drawPausedOverlay(Graphics2D g) {
        drawDimOverlay(g);
        drawCenteredTitle(g, "PAUSED", -20);
        drawCenteredSubtitle(g, "Press P to resume", 20);
    }

    private void drawGameOverOverlay(Graphics2D g) {
        drawDimOverlay(g);
        drawCenteredTitle(g, "GAME OVER", -60);
        drawCenteredSubtitle(g, "Score: " + (int) score, -10);
        drawCenteredSubtitle(g, "Best: " + highScoreManager.getHighScore(), 20);
        drawCenteredSubtitle(g, "Press SPACE to restart", 60);
    }

    private void drawDimOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 130));
        g.fillRect(0, 0, Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);
    }

    private void drawCenteredTitle(Graphics2D g, String text, int yOffset) {
        g.setFont(new Font("SansSerif", Font.BOLD, 30));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        int x = Constants.BOARD_WIDTH / 2 - fm.stringWidth(text) / 2;
        int y = Constants.BOARD_HEIGHT / 2 + yOffset;
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(text, x + 2, y + 2);
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }

    private void drawCenteredSubtitle(Graphics2D g, String text, int yOffset) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        FontMetrics fm = g.getFontMetrics();
        int x = Constants.BOARD_WIDTH / 2 - fm.stringWidth(text) / 2;
        int y = Constants.BOARD_HEIGHT / 2 + yOffset;
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }

    // ------------------------------------------------------------------
    // Physics / update loop
    // ------------------------------------------------------------------

    private void move() {
        bird.applyGravity();
        moveClouds();
        applyDifficultyScaling();

        Iterator<Pipe> iterator = pipes.iterator();
        while (iterator.hasNext()) {
            Pipe pipe = iterator.next();
            pipe.move(pipeSpeed);

            if (pipe.isOffScreen()) {
                iterator.remove();
                continue;
            }

            if (!pipe.isPassed() && pipe.isPassedBy(bird.getX())) {
                pipe.markPassed();
                score += 0.5;
            }

            if (bird.getBounds().intersects(pipe.getBounds())) {
                endGame();
            }
        }

        if (bird.hasFallenBelow(Constants.BOARD_HEIGHT - Constants.GROUND_HEIGHT)) {
            endGame();
        }
    }

    private void moveClouds() {
        for (int i = 0; i < cloudX.length; i++) {
            cloudX[i] -= 1;
            if (cloudX[i] < -80) {
                cloudX[i] = Constants.BOARD_WIDTH + random.nextInt(60);
                cloudY[i] = 20 + random.nextInt(140);
            }
        }
    }

    /** Gradually raises the difficulty as the player's score increases. */
    private void applyDifficultyScaling() {
        int level = (int) (score / Constants.DIFFICULTY_STEP);
        pipeSpeed = Math.min(Constants.BASE_PIPE_SPEED + level, Constants.MAX_PIPE_SPEED);
        pipeGap = Math.max(
                Constants.BASE_PIPE_GAP - level * Constants.GAP_SHRINK_PER_STEP,
                Constants.MIN_PIPE_GAP);
    }

    private void endGame() {
        if (state != GameState.PLAYING) {
            return;
        }
        state = GameState.GAME_OVER;
        highScoreManager.reportScore((int) score);
        gameLoop.stop();
        placePipeTimer.stop();
    }

    private void restartGame() {
        bird.resetPosition();
        pipes.clear();
        score = 0;
        pipeSpeed = Constants.BASE_PIPE_SPEED;
        pipeGap = Constants.BASE_PIPE_GAP;
        state = GameState.PLAYING;
        gameLoop.start();
        placePipeTimer.start();
    }

    // ------------------------------------------------------------------
    // Input handling
    // ------------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        if (state == GameState.PLAYING) {
            move();
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE -> handleSpace();
            case KeyEvent.VK_P -> handlePauseToggle();
            default -> { /* ignore other keys */ }
        }
    }

    private void handleSpace() {
        switch (state) {
            case START -> {
                state = GameState.PLAYING;
                start();
            }
            case PLAYING -> bird.flap();
            case GAME_OVER -> restartGame();
            default -> { /* PAUSED: ignore flap while paused */ }
        }
    }

    private void handlePauseToggle() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
            gameLoop.stop();
            placePipeTimer.stop();
        } else if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
            gameLoop.start();
            placePipeTimer.start();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Intentionally unused: all input is handled in keyPressed().
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Intentionally unused: no press-and-hold behavior in this game.
    }
}
