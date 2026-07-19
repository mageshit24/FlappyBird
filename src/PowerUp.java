
import module java.desktop;

/**
 * PowerUp.java
 *
 * A floating collectible on the board (shield or slow-mo). Scrolls left with
 * the pipes; the bird collects it on overlap.
 *
 * As with Bird and Pipe, fields are private with explicit accessors so the game
 * loop can't corrupt a power-up's state from outside.
 */
public final class PowerUp {

    private int x;
    private final int y;
    private final int width;
    private final int height;
    private final PowerUpKind kind;
    private boolean collected;

    public PowerUp(PowerUpKind kind, int startX, int startY, int width, int height) {
        this.kind = kind;
        this.x = startX;
        this.y = startY;
        this.width = width;
        this.height = height;
        this.collected = false;
    }

    /**
     * Moves the power-up left by the given speed (pixels per tick).
     */
    public void move(int speed) {
        x -= speed;
    }

    public boolean isOffScreen() {
        return x + width < 0;
    }

    public boolean isCollected() {
        return collected;
    }

    public void markCollected() {
        this.collected = true;
    }

    public PowerUpKind getKind() {
        return kind;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
