// JDK 25 (JEP 511): module import declarations pull in every exported
// package of a module at once, instead of listing each type. java.desktop
// covers AWT/Swing/sound; java.base covers util/io/net. Where two modules
// export a same-named type, a single-type import is required to
// disambiguate - the compiler rejects the ambiguous reference otherwise,
// which is the intended safety net for this feature. Two collisions here:
// java.util.Timer vs javax.swing.Timer, and java.util.List vs the legacy
// AWT listbox component java.awt.List.

import java.util.List;
import javax.swing.Timer;

import module java.base;
import module java.desktop;

/**
 * FlappyBird.java
 *
 * Core game panel: owns the game loop, physics, rendering, input, and the
 * overall game-state machine (START -> PLAYING -> PAUSED / GAME_OVER).
 *
 * Changes from the original version: - Introduces an explicit GameState enum
 * instead of a single boolean `gameOver` flag, so start/pause/restart flows are
 * unambiguous. - All previously package-private mutable fields are now private;
 * external classes can no longer reach in and mutate game state. - Difficulty
 * scales gradually with score (pipes speed up and the gap narrows), instead of
 * staying flat for the whole game. - Adds a pause feature (P key), a start
 * screen, and a redesigned game-over / HUD overlay with the persisted high
 * score. - Adds simple drawn (non-image) polish: parallax clouds, a ground
 * strip, a subtle score panel, and bird tilt - all vector-drawn, so no new
 * image assets are required. - Resource loading is wrapped in error handling
 * instead of letting a missing asset throw an unhandled NullPointerException.
 *
 * New in this revision (Java 25 / feature update): - Sound effects (flap,
 * score, hit, power-up pickup, shield break) via SoundManager, with an M-key
 * mute toggle. Fully optional at runtime - see SoundManager for the
 * no-audio-device fallback. - Two collectible power-ups: a one-hit Shield and a
 * temporary Slow-Mo that eases pipe speed. Active effects are modeled as a
 * sealed PowerUpEffect interface (Shield/SlowMo records) so every switch over
 * an effect is compiler-checked for exhaustiveness, and effect data is read via
 * record deconstruction patterns (Java 21+ pattern matching for switch).
 */
public final class FlappyBird extends JPanel implements ActionListener, KeyListener {

    // Standard Swing/Serializable requirement; this panel is never actually
    // serialized, but declaring it silences a compiler warning and follows
    // best practice for any Serializable subclass.
    private static final long serialVersionUID = 1L;

    /**
     * Simple state machine for the game's lifecycle.
     */
    private enum GameState {
        START, PLAYING, PAUSED, GAME_OVER
    }

    // ---- Images ----
    // transient: java.awt.Image isn't Serializable, and this panel is never
    // actually serialized (see serialVersionUID note above) - these fields
    // would be meaningless in a serialized snapshot anyway.
    private final transient Image backgroundImg;
    private final transient Image birdImg;
    private final transient Image topPipeImg;
    private final transient Image bottomPipeImg;

    // ---- Entities ----
    // transient for the same reason: live game-session state (custom entity
    // types, collections of them) isn't meant to survive serialization.
    private transient Bird bird;
    private final transient List<Pipe> pipes = new ArrayList<>();
    private final transient List<PowerUp> powerUps = new ArrayList<>();
    private final transient ActiveEffects activeEffects = new ActiveEffects();
    private final Random random = new Random();

    // ---- Timers ----
    private final Timer gameLoop;
    private final Timer placePipeTimer;
    private final Timer placePowerUpTimer;

    // ---- State ----
    private GameState state = GameState.START;
    private double score = 0;
    private int pipeSpeed = Constants.BASE_PIPE_SPEED;
    private int pipeGap = Constants.BASE_PIPE_GAP;
    // Brief grace period after a shield absorbs a hit, so the same
    // still-overlapping pipe can't immediately end the game.
    private long invulnerableUntil = 0L;

    // ---- Audio ----
    private final transient SoundManager soundManager = new SoundManager();

    // ---- Persistence ----
    private final transient HighScoreManager highScoreManager = new HighScoreManager();

    // ---- Decorative parallax clouds (purely visual, no gameplay effect) ----
    private final int[] cloudX = new int[4];
    private final int[] cloudY = new int[4];

    public FlappyBird() {
        // JDK 25 (JEP 513): flexible constructor bodies allow statements
        // before an explicit super()/this() call, as long as they don't
        // touch the instance under construction. Before JDK 25 this was a
        // compile error - the superclass call had to be the very first
        // statement, so a config sanity check like this had to happen
        // after JPanel was already constructed. Failing fast here means
        // a broken Constants configuration never gets as far as building
        // a half-initialized JPanel.
        if (Constants.BOARD_WIDTH <= 0 || Constants.BOARD_HEIGHT <= 0) {
            throw new IllegalStateException("Board dimensions must be positive");
        }
        super();

        setPreferredSize(new Dimension(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT));
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

        // One-shot timer that reschedules itself with a fresh random delay
        // each time it fires, so power-ups spawn at an irregular interval
        // rather than a fixed, learnable cadence.
        placePowerUpTimer = new Timer(randomPowerUpDelay(), null);
        placePowerUpTimer.setRepeats(false);
        placePowerUpTimer.addActionListener(e -> {
            if (state == GameState.PLAYING) {
                placePowerUp();
            }
            placePowerUpTimer.setInitialDelay(randomPowerUpDelay());
            placePowerUpTimer.restart();
        });

        gameLoop = new Timer(1000 / Constants.FPS, this);
    }

    /**
     * Starts the render/animation timers. Call once the panel is on-screen.
     */
    public void start() {
        gameLoop.start();
        placePipeTimer.start();
        placePowerUpTimer.restart();
    }

    /**
     * Picks the next power-up spawn delay, randomized within the configured
     * range.
     */
    private int randomPowerUpDelay() {
        int span = Constants.POWERUP_MAX_SPAWN_MS - Constants.POWERUP_MIN_SPAWN_MS;
        return Constants.POWERUP_MIN_SPAWN_MS + random.nextInt(span + 1);
    }

    /**
     * Loads an image from the classpath. Missing/corrupt assets no longer crash
     * the whole application with a raw NullPointerException - we fail loudly
     * but gracefully with a clear message instead.
     */
    private Image loadImage(String resourcePath) {
        URL url = getClass().getResource(resourcePath);
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

    /**
     * Spawns a random-kind power-up at a random height, clear of the ground and
     * top margin.
     */
    private void placePowerUp() {
        PowerUpKind kind = random.nextBoolean() ? PowerUpKind.SHIELD : PowerUpKind.SLOW_MO;
        int margin = 40;
        int usableHeight = Constants.BOARD_HEIGHT - Constants.GROUND_HEIGHT - Constants.POWERUP_SIZE - margin * 2;
        int y = margin + (usableHeight > 0 ? random.nextInt(usableHeight) : 0);
        powerUps.add(new PowerUp(kind, Constants.BOARD_WIDTH, y, Constants.POWERUP_SIZE, Constants.POWERUP_SIZE));
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

        drawPowerUps(g);
        drawGround(g);
        drawBird(g);
        drawHud(g);
        drawEffectBadges(g);

        switch (state) {
            case START ->
                drawStartOverlay(g);
            case PAUSED ->
                drawPausedOverlay(g);
            case GAME_OVER ->
                drawGameOverOverlay(g);
            default -> {
                /* PLAYING: no overlay */ }
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

    /**
     * Draws every power-up currently on the board with its kind-specific icon.
     */
    private void drawPowerUps(Graphics2D g) {
        for (PowerUp powerUp : powerUps) {
            switch (powerUp.getKind()) {
                case SHIELD ->
                    drawShieldIcon(g, powerUp.getX(), powerUp.getY(), powerUp.getWidth());
                case SLOW_MO ->
                    drawSlowMoIcon(g, powerUp.getX(), powerUp.getY(), powerUp.getWidth());
            }
        }
    }

    /**
     * Vector-drawn shield pickup icon: a filled circle with a ring outline. No
     * image asset needed.
     */
    private void drawShieldIcon(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(80, 200, 255, 235));
        g.fillOval(x, y, size, size);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2f));
        g.drawOval(x + 4, y + 4, size - 8, size - 8);
    }

    /**
     * Vector-drawn slow-mo pickup icon: a filled circle with a simple clock
     * face. No image asset needed.
     */
    private void drawSlowMoIcon(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(190, 120, 255, 235));
        g.fillOval(x, y, size, size);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2f));
        int cx = x + size / 2;
        int cy = y + size / 2;
        g.drawOval(cx - size / 3, cy - size / 3, size * 2 / 3, size * 2 / 3);
        g.drawLine(cx, cy, cx, cy - size / 4);
        g.drawLine(cx, cy, cx + size / 5, cy);
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
        double cx = bird.getX() + bird.getWidth() / 2.0;
        double cy = bird.getY() + bird.getHeight() / 2.0;

        if (activeEffects.hasShield()) {
            int auraSize = (int) (Math.max(bird.getWidth(), bird.getHeight()) * 1.7);
            g.setColor(new Color(80, 200, 255, 110));
            g.fillOval((int) (cx - auraSize / 2.0), (int) (cy - auraSize / 2.0), auraSize, auraSize);
        }

        AffineTransform old = g.getTransform();
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

    /**
     * Small "SHIELD" / "SLOW-MO Ns" pills under the score panel while effects
     * are active.
     */
    private void drawEffectBadges(Graphics2D g) {
        if (activeEffects.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        int badgeY = 64;
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        for (PowerUpEffect effect : activeEffects.snapshot()) {
            String label = switch (effect) {
                case PowerUpEffect.Shield _ ->
                    "SHIELD";
                case PowerUpEffect.SlowMo(var expiresAt) ->
                    "SLOW-MO " + Math.max(0, (expiresAt - now) / 1000 + 1) + "s";
            };
            Color accent = (effect instanceof PowerUpEffect.Shield)
                    ? new Color(80, 200, 255)
                    : new Color(190, 120, 255);

            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(label);
            int panelX = Constants.BOARD_WIDTH / 2 - textWidth / 2 - 10;
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRoundRect(panelX, badgeY, textWidth + 20, 22, 11, 11);
            g.setColor(accent);
            g.drawString(label, Constants.BOARD_WIDTH / 2 - textWidth / 2, badgeY + 16);
            badgeY += 26;
        }
    }

    private void drawStartOverlay(Graphics2D g) {
        drawDimOverlay(g);
        drawCenteredTitle(g, "FLAPPY BIRD", -60);
        drawCenteredSubtitle(g, "Press SPACE to start", -10);
        drawCenteredSubtitle(g, "P to pause  \u2022  M to mute", 20);
        drawCenteredSubtitle(g, "Grab \u25CF for a shield, a slow-mo boost", 50);
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
    /**
     * One physics tick: gravity, clouds, difficulty, effect expiry, then moves
     * and checks collisions for both pipes and power-ups. Pipe/ power-up speed
     * is halved while Slow-Mo is active.
     */
    private void move() {
        bird.applyGravity();
        moveClouds();
        applyDifficultyScaling();
        activeEffects.expire(System.currentTimeMillis());

        int effectivePipeSpeed = activeEffects.hasSlowMo()
                ? (int) Math.max(1, Math.round(pipeSpeed * Constants.SLOW_MO_SPEED_MULTIPLIER))
                : pipeSpeed;

        Iterator<Pipe> pipeIterator = pipes.iterator();
        while (pipeIterator.hasNext()) {
            Pipe pipe = pipeIterator.next();
            pipe.move(effectivePipeSpeed);

            if (pipe.isOffScreen()) {
                pipeIterator.remove();
                continue;
            }

            if (!pipe.isPassed() && pipe.isPassedBy(bird.getX())) {
                pipe.markPassed();
                score += 0.5;
                soundManager.play(SoundManager.Effect.SCORE);
            }

            if (bird.getBounds().intersects(pipe.getBounds())) {
                handlePipeCollision();
            }
        }

        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            powerUp.move(effectivePipeSpeed);

            if (powerUp.isOffScreen()) {
                powerUpIterator.remove();
                continue;
            }

            if (bird.getBounds().intersects(powerUp.getBounds())) {
                collectPowerUp(powerUp);
                powerUpIterator.remove();
            }
        }

        if (bird.hasFallenBelow(Constants.BOARD_HEIGHT - Constants.GROUND_HEIGHT)) {
            endGame();
        }
    }

    /**
     * A pipe hit either ends the game, or - if a shield is active - is absorbed
     * instead.
     */
    private void handlePipeCollision() {
        long now = System.currentTimeMillis();
        if (now < invulnerableUntil) {
            return; // still in the brief grace window right after a shield broke
        }
        if (activeEffects.consumeShield()) {
            invulnerableUntil = now + Constants.POST_SHIELD_INVULN_MS;
            soundManager.play(SoundManager.Effect.SHIELD_BREAK);
            return;
        }
        endGame();
    }

    /**
     * Converts a collected pickup into a timed effect via ActiveEffects.
     */
    private void collectPowerUp(PowerUp powerUp) {
        soundManager.play(SoundManager.Effect.POWERUP);
        long now = System.currentTimeMillis();
        switch (powerUp.getKind()) {
            case SHIELD ->
                activeEffects.activateShield(Constants.SHIELD_DURATION_MS, now);
            case SLOW_MO ->
                activeEffects.activateSlowMo(Constants.SLOW_MO_DURATION_MS, now);
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

    /**
     * Gradually raises the difficulty as the player's score increases.
     */
    private void applyDifficultyScaling() {
        Difficulty.Level level = Difficulty.forScore(score);
        pipeSpeed = level.pipeSpeed();
        pipeGap = level.pipeGap();
    }

    private void endGame() {
        if (state != GameState.PLAYING) {
            return;
        }
        state = GameState.GAME_OVER;
        highScoreManager.reportScore((int) score);
        gameLoop.stop();
        placePipeTimer.stop();
        placePowerUpTimer.stop();
        soundManager.play(SoundManager.Effect.HIT);
    }

    private void restartGame() {
        bird.resetPosition();
        pipes.clear();
        powerUps.clear();
        activeEffects.clear();
        invulnerableUntil = 0L;
        score = 0;
        pipeSpeed = Constants.BASE_PIPE_SPEED;
        pipeGap = Constants.BASE_PIPE_GAP;
        state = GameState.PLAYING;
        gameLoop.start();
        placePipeTimer.start();
        placePowerUpTimer.setInitialDelay(randomPowerUpDelay());
        placePowerUpTimer.restart();
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
            case KeyEvent.VK_SPACE ->
                handleSpace();
            case KeyEvent.VK_P ->
                handlePauseToggle();
            case KeyEvent.VK_M ->
                soundManager.toggleMuted();
            default -> {
                /* ignore other keys */ }
        }
    }

    private void handleSpace() {
        switch (state) {
            case START -> {
                state = GameState.PLAYING;
                start();
            }
            case PLAYING -> {
                bird.flap();
                soundManager.play(SoundManager.Effect.FLAP);
            }
            case GAME_OVER ->
                restartGame();
            default -> {
                /* PAUSED: ignore flap while paused */ }
        }
    }

    private void handlePauseToggle() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
            gameLoop.stop();
            placePipeTimer.stop();
            placePowerUpTimer.stop();
        } else if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
            gameLoop.start();
            placePipeTimer.start();
            placePowerUpTimer.restart();
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
