import java.awt.Image;
import java.awt.Rectangle;

/**
 * Bird.java
 *
 * Represents the player-controlled bird sprite: its position, size,
 * image, and vertical velocity.
 *
 * Encapsulation note: every field is `private`. In the original code
 * these fields were package-private (no modifier at all), which meant
 * any class added later in the same package could silently mutate a
 * bird's position or velocity with no validation. Here, the only way
 * to change state is through the setter methods below, which is where
 * we can safely clamp values (e.g. never let velocity exceed terminal
 * fall speed, never let the bird fly above the top of the board).
 */
public class Bird {

    private int x;
    private int y;
    private final int width;
    private final int height;
    private final Image image;

    private double velocityY;
    private double tiltDegrees;

    public Bird(Image image) {
        this.image = image;
        this.width = Constants.BIRD_WIDTH;
        this.height = Constants.BIRD_HEIGHT;
        resetPosition();
    }

    /** Resets the bird to its starting position and clears velocity. */
    public void resetPosition() {
        this.x = Constants.BIRD_START_X;
        this.y = Constants.BIRD_START_Y;
        this.velocityY = 0;
        this.tiltDegrees = 0;
    }

    /** Applies one physics tick of gravity + the current velocity. */
    public void applyGravity() {
        velocityY = Math.min(velocityY + Constants.GRAVITY, Constants.MAX_FALL_SPEED);
        y += (int) velocityY;
        y = Math.max(y, 0); // never let the bird fly off the top of the screen

        // Tilt the sprite based on velocity for a nicer "diving/climbing" look.
        double targetTilt = velocityY < 0
                ? Constants.MAX_TILT_DEGREES
                : Math.max(-90, -velocityY * 4);
        tiltDegrees = targetTilt;
    }

    /** Gives the bird an upward flap impulse. */
    public void flap() {
        velocityY = Constants.FLAP_VELOCITY;
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

    public double getTiltDegrees() {
        return tiltDegrees;
    }

    public boolean hasFallenBelow(int boardHeight) {
        return y > boardHeight;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
