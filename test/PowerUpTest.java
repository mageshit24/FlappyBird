
import module java.desktop;

/**
 * PowerUpTest.java — feature tests for the collectible PowerUp entity.
 */
final class PowerUpTest {

    private PowerUpTest() {
    }

    private static PowerUp newPowerUp(int startX) {
        return new PowerUp(PowerUpKind.SHIELD, startX, 50, Constants.POWERUP_SIZE, Constants.POWERUP_SIZE);
    }

    static void moveShiftsLeft() {
        PowerUp powerUp = newPowerUp(200);
        powerUp.move(5);
        TestRunner.assertEquals(195, powerUp.getX(), "move(5) should shift the power-up 5px left");
    }

    static void offScreenDetection() {
        PowerUp powerUp = newPowerUp(0);
        TestRunner.assertFalse(powerUp.isOffScreen(), "a power-up at x=0 should still be on screen");
        powerUp.move(Constants.POWERUP_SIZE + 1);
        TestRunner.assertTrue(powerUp.isOffScreen(), "a power-up fully past the left edge should be off screen");
    }

    static void boundsAreAccurate() {
        PowerUp powerUp = newPowerUp(120);
        var bounds = powerUp.getBounds();
        TestRunner.assertEquals(120, bounds.x, "bounds.x should match the power-up's x");
        TestRunner.assertEquals(50, bounds.y, "bounds.y should match the power-up's y");
        TestRunner.assertEquals(Constants.POWERUP_SIZE, bounds.width, "bounds.width should match POWERUP_SIZE");
        TestRunner.assertEquals(Constants.POWERUP_SIZE, bounds.height, "bounds.height should match POWERUP_SIZE");
    }

    static void collectionTracksState() {
        PowerUp powerUp = newPowerUp(0);
        TestRunner.assertFalse(powerUp.isCollected(), "a new power-up should not start out collected");
        powerUp.markCollected();
        TestRunner.assertTrue(powerUp.isCollected(), "markCollected should be reflected by isCollected");
    }
}
