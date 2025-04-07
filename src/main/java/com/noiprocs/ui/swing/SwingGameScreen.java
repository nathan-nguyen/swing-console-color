package com.noiprocs.ui.swing;

import com.noiprocs.core.graphics.RenderableSprite;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.ui.console.ConsoleGameScreen;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class SwingGameScreen extends ConsoleGameScreen {
    private static final boolean USE_SQUARE_FONT = false;
    public final JFrame jframe = new JFrame();
    public final JTextArea jTextArea = new JTextArea();

    public SwingGameScreen() {
        jframe.setSize(440, 670);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setVisible(true);

        jframe.add(jTextArea);
        jTextArea.setEditable(false);
        jTextArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        jTextArea.setBackground(Color.BLACK);
        jTextArea.setForeground(Color.WHITE);

        if (USE_SQUARE_FONT) this.useSquareFont();
    }

    @Override
    public void render(int delta) {
        Model playerModel = gameContext.modelManager.getModel(gameContext.username);
        // Only render when playerModel is existing
        if (playerModel == null) return;

        // Render map
        jTextArea.setText(this.getScreenContentInString((PlayerModel) playerModel));
    }

    private void useSquareFont() {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/square.ttf"));
            jTextArea.setFont(font.deriveFont(12f));
            jframe.setSize(750, 580);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }
}
