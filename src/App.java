
import module java.desktop;
import module java.logging;

/**
 * App.java
 *
 * Application entry point. Builds the game window and starts the game panel.
 *
 * Security / robustness fix: the original `main` declared `throws Exception`,
 * which meant any failure (e.g. a missing image asset) would print an uncaught
 * stack trace - including local file paths and internal class names - straight
 * to the console and crash with no user-friendly message. Startup is now
 * wrapped so failures are logged and shown as a clean dialog instead of a raw
 * trace.
 *
 * Swing components must be created on the Event Dispatch Thread (EDT); this now
 * uses SwingUtilities.invokeLater() for that, which the original code skipped.
 */
public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::createAndShowGame);
    }

    private static void createAndShowGame() {
        try {
            JFrame frame = new JFrame("Flappy Bird");
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            FlappyBird flappyBird = new FlappyBird();
            frame.add(flappyBird);
            frame.pack();
            frame.setVisible(true);

            flappyBird.requestFocusInWindow();
            flappyBird.start();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start Flappy Bird", e);
            JOptionPane.showMessageDialog(null,
                    "Flappy Bird couldn't start. Please reinstall or re-download the game.",
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
