package game;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class MainFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameEngine engine = new GameEngine();
            GamePanel panel = new GamePanel(engine);

            JFrame frame = new JFrame("Shootage AI Evolution");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(null);

            panel.setBounds(0, 0, GameEngine.FIELD_WIDTH, GameEngine.FIELD_HEIGHT);

            frame.add(panel);

            frame.setSize(GameEngine.FIELD_WIDTH, GameEngine.FIELD_HEIGHT);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}