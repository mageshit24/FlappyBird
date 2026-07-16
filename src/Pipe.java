import java.awt.Image;
import java.awt.Rectangle;

/**
 * Pipe.java
 *
 * Represents a single pipe obstacle (top or bottom half of a pair).
 *
 * As with Bird, fields are private with explicit accessors so the
 * game loop can't accidentally corrupt a pipe's state (e.g. marking
 * it "passed" twice and double-counting score) from outside this class.
 */
public class Pipe {

    private int x;
    private final int y;
    private final int width;
    private final int height;
    private final Image image;
    private boolean passed;

    public Pipe(Image image, int startX, int startY, int width, int height) {
        this.image = image;
        this.x = startX;
        this.y = startY;
        this.width = width;
        this.height = height;
        this.passed = false;
    }

    /** Moves the pipe left by the given speed (pixels per tick). */
    public void move(int speed) {
        x -= speed;
    }

    public boolean isOffScreen() {
        return x + width < 0;
    }

    public boolean isPassedBy(int birdX) {
        return birdX > x + width;
    }

    public boolean isPassed() {
        return passed;
    }

    public void markPassed() {
        this.passed = true;
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

    public Image getImage() {
        return image;
    }
}
