package Main;

import Inputs.KeyboardInputs;
import Inputs.MouseInputs;

import javax.swing.*;
import java.awt.*;

import static Main.Game.GAME_HEIGHT;
import static Main.Game.GAME_WIDTH;

public class GamePanel extends JPanel {

    private MouseInputs mouseInputs;
    private Game game;


    public GamePanel(Game game) {

        mouseInputs = new MouseInputs(this);
        this.game = game;

        setPanelSize();
        addKeyListener(new KeyboardInputs(this));
        addMouseListener(mouseInputs);
        addMouseMotionListener(mouseInputs);

    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocus();
    }

    private void setPanelSize() {
        /* Statik GAME_WIDTH ve GAME_HEIGHT özelliklerine nesne kullanmadan, sınıf adıyla
        ulaşıyoruz. */
        Dimension screenSize = new Dimension(GAME_WIDTH, GAME_HEIGHT);
        setPreferredSize(screenSize);
        System.out.println("Size: " + GAME_WIDTH + " : " +GAME_HEIGHT);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        game.render(g);
    }

    public Game getGame() {
        return game;
    }

}