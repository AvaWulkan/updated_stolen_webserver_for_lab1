package sources;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import javax.swing.JPanel;
class SwingDemo extends javax.swing.JFrame {

    Image img = Toolkit.getDefaultToolkit().getImage("C:\\dog.jpg");
    public SwingDemo() throws IOException {
        this.setContentPane(new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(img, 0, 0, null);
            }
        });
        pack();
        setVisible(true);
    }
    public static void main(String[] args) throws Exception {
        new SwingDemo();
    }
}