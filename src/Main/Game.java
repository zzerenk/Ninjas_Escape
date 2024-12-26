package Main;

import Gamestate.Gamestate;
import Gamestate.Playing;
import Gamestate.Menu;

import java.awt.Graphics;
import java.time.Duration;
import java.time.LocalDateTime;

public class Game implements Runnable {
    /* Runnable arayüzü implemente edilerek, run metodunun ayrı bir thread'de (iş parçacığında)
    çalıştırılması sağlanır. Oyun döngüsünün ana thread'de değil, paralel bir başka thread'de
    çalıştırılması oyun performansını arttırır. */

    private GamePanel gamePanel;
    private GameWindow gameWindow;
    private Thread gameThread;
    private Playing playing;
    private Menu menu;

    /* Oyun ekranının saniyede 120 defa yeniden çizilmesini ve 200 defa güncellenmesini
    istiyoruz. */
    private final int FPS_SET = 120;
    private final int UPS_SET = 200;



    /* Oyun penceresi boyutunu sabit tam sayılarla belirlemek yerine her bir karonun boyutunu
    belirleyerek ve pencerede kaç karo istediğimizi belirterek buluyoruz.*/
    public final static int TILES_DEFAULT_SIZE = 32;
    public final static float SCALE = 2.0f;
    public final static int TILES_IN_WIDTH = 26;
    public final static int TILES_IN_HEIGHT = 14;
    public final static int TILES_SIZE = (int) (TILES_DEFAULT_SIZE * SCALE);
    public final static int GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
    public final static int GAME_HEIGHT = TILES_SIZE * TILES_IN_HEIGHT;

    public Game() {
        initClasses();

        gamePanel = new GamePanel(this);
        gameWindow = new GameWindow(gamePanel);
        gamePanel.requestFocus();

        startGameLoop();
    }

    private void initClasses() {

        /* Menu sınıfından yeni bir nesne oluşturulur ve mevcut game nesnesini Menu sınıfının
        kurucu metodunun parametresi olarak atar. Bu sayede Menu sınıfından Game sınıfının
        özellik ve metotlarına erişilebilir. Aynı işlem Playing sınıfı için de yapılır.*/
        menu = new Menu(this);
        playing = new Playing(this);
    }

    private void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start(); /* Bu nesne aracılığıyla Thread sınıfına ait start() metodu çağırılır.
        Yeni bir thread başlatılır ve run() metodu bu thread içinde çalıştırılır. */
    }

    public void update() {
        switch (Gamestate.state) {
            case MENU:
                /* Program menu görsellerini sadece oyun MENU durumundayken güncellemeli. */
                menu.update();
                break;
            case PLAYING:
                /* Program oyuncuyu sadece oyun PLAYING durumundayken güncellemeli. */
                playing.update();
                break;
                /* Programımızın son halinde Options menüsü bulunmamaktadır. Oyuncu ana menüde Options
                seçeneğine tıkladığında oyundan çıkılacaktır. */
            case OPTIONS:
            case QUIT:
            default:
                System.exit(0);
                break;
        }
    }

    public void render(Graphics g) {
        switch (Gamestate.state) {
            case MENU:
                menu.draw(g);
                /* Program menu görsellerini sadece oyun MENU durumundayken çizmeli.*/
                break;
            case PLAYING:
                playing.draw(g);
                /* Program oyuncuyu sadece oyun PLAYING durumundayken çizmeli.*/
                break;
            default:
                break;
        }
    }

    public void run() {
        // Tek bir güncelleme ve tek bir frame için ayrılacak süreyi hesaplıyoruz.
        double timePerFrame = 1000000000.0 / FPS_SET;
        double timePerUpdate = 1000000000.0 / UPS_SET;

        // LocalDateTime tipinden previousTime değişkenine değer olarak lastCheck zamanı atıyoruz.
        LocalDateTime previousTime = LocalDateTime.now();
        // Bu anda henüz hiçbir çizim veya güncelleme yapılmamış.
        int frames = 0;
        int updates = 0;
        LocalDateTime lastCheck = LocalDateTime.now();  // FPS ve UPS sayacını kontrol etmek için

        double deltaUpdates = 0;
        double deltaFrames = 0;

        while (true) {

            // Şu anki zamanı alıyoruz
            LocalDateTime currentTime = LocalDateTime.now();

            // Delta hesaplama için Duration kullanıyoruz
            Duration elapsed = Duration.between(previousTime, currentTime);
            deltaUpdates += elapsed.toNanos() / timePerUpdate;
            deltaFrames += elapsed.toNanos() / timePerFrame;

            previousTime = currentTime;  // Sonraki hesaplama için

            // Güncelleme (update) işlemi
            if (deltaUpdates >= 1) {
                update();
                updates++;
                deltaUpdates--;
            }

            // Frame render işlemi
            if (deltaFrames >= 1) {
                gamePanel.repaint();
                frames++;
                deltaFrames--;
            }

            // Her saniye için FPS ve UPS bilgilerini konsola yazdırıyoruz.
            if (Duration.between(lastCheck, LocalDateTime.now()).toMillis() >= 1000) {
                lastCheck = LocalDateTime.now();
                System.out.println("FPS: " + frames + " UPS: " + updates);
                frames = 0;
                updates = 0;
            }
        }
    }



    public void windowFocusLost() {
        if(Gamestate.state == Gamestate.PLAYING) {
            playing.getPlayer().resetDirectionBooleans();
        }
    }

    /* getMenu() metodu sayesinde, dış sınıflar Game sınıfının içindeki menu nesnesine
    erişebilir ve bu nesne üzerinden menü ile ilgili işlemleri gerçekleştirebilir. Aynısı
    getPlaying için de geçerlidir.*/
    public Menu getMenu() {
        return menu;
    }

    public Playing getPlaying() {
        return playing;
    }

}