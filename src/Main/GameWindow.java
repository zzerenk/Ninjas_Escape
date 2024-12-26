package Main;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JFrame;

public class GameWindow {

    private JFrame frame;

    public GameWindow(GamePanel gamePanel) {

        frame = new JFrame();

        frame.setTitle("Ninja's Escape");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(gamePanel);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowFocusListener(new WindowFocusListener() {
            /* Kullanıcının oyunu oynadığı süreç içerisinde oyun penceresinin odağının kaybedilip
            kaybedilmediğine bakıyoruz. Varsayalım ki bilgisayar sisteminden bir bildirim alıyoruz
            ve oyun penceresi otomatik olarak arka planda kalıyor. Bu durumda tüm boolean değerlerinin
            aniden sıfırlanmasını istiyoruz. */

            @Override
            public void windowLostFocus(WindowEvent e) {
                gamePanel.getGame().windowFocusLost();
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {

            }
        });
    }
}