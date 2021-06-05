package com.noiprocs.ui.swing;

import com.noiprocs.core.GameContext;
import com.noiprocs.core.config.Config;
import com.noiprocs.core.graphics.GameScreenInterface;
import com.noiprocs.core.graphics.RenderableSprite;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.ui.console.sprite.ConsoleSprite;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.noiprocs.ui.console.ConsoleUIConfig.HEIGHT;
import static com.noiprocs.ui.console.ConsoleUIConfig.WIDTH;

public class SwingGameScreen implements GameScreenInterface {
    private static final boolean USE_SQUARE_FONT = false;
    private final char[][] map = new char[HEIGHT][WIDTH];
    private GameContext gameContext;
    public final JFrame jframe = new JFrame();
    public final JTextArea jTextArea = new JTextArea();

    public SwingGameScreen() {
        jframe.setSize(440, 660);
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
    public void setGameContext(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    @Override
    public void render(int delta) {
        PlayerModel playerModel = (PlayerModel) gameContext.modelManager.getModel(gameContext.username);

        // Only render when playerModel is existing
        if (playerModel == null) return;

        // Get list of objects not far from player
        List<RenderableSprite> renderableSpriteList = gameContext.spriteManager.getRenderableObjectListWithinRange(
                playerModel.posX,
                playerModel.posY,
                Config.RENDER_RANGE
        );

        /* Render order:
         * - PlayerModel renders last.
         * - Models with smaller posY render first.
         */
        renderableSpriteList.sort(
                (u, v) -> {
                    Model uModel = u.getModel();
                    Model vModel = v.getModel();
                    if (uModel.id.equals(gameContext.username)) return 1;
                    if (vModel.id.equals(gameContext.username)) return -1;
                    return Integer.compare(uModel.posY, vModel.posY);
                }
        );

        int offsetX = playerModel.posX - HEIGHT / 2;
        int offsetY = playerModel.posY - WIDTH / 2;

        this.clearMap();

        for (RenderableSprite renderableSprite : renderableSpriteList) {
            char[][] texture = ((ConsoleSprite) renderableSprite).getTexture();

            Model model = renderableSprite.getModel();
            int posX = model.posX;
            int posY = model.posY;

            this.updateMap(posX, posY, texture, offsetX, offsetY);
        }

        // Render map
        jTextArea.setText(printMap());
    }

    private void clearMap() {
        for (int i = 0; i < HEIGHT; ++i) {
            for (int j = 0; j < WIDTH; ++j) map[i][j] = 0;
        }
    }

    private void updateMap(int posX, int posY, char[][] texture, int offsetX, int offsetY) {
        for (int i = 0; i < texture.length; ++i) {
            for (int j = 0; j < texture[0].length; ++j) {
                if (texture[i][j] == 0) continue;
                int x = posX + i - offsetX;
                int y = posY + j - offsetY;
                if (x >= 0 && x < HEIGHT && y >= 0 && y < WIDTH) map[x][y] = texture[i][j];
            }
        }
    }

    private String printMap() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < map.length; ++i) {
            // Add top border
            if (i == 0) {
                for (int j = 0; j < map[0].length + 2; ++j) sb.append('-');
                sb.append('\n');
            }

            // Add map content
            for (int j = 0; j < map[0].length; ++j) {
                // Add left border
                if (j == 0) sb.append('|');

                sb.append(map[i][j] == 0 ? ' ' : map[i][j]);

                // Add right border
                if (j == map[0].length - 1) sb.append('|');
            }
            sb.append('\n');

            // Add bottom border
            if (i == map.length - 1) {
                for (int j = 0; j < map[0].length + 2; ++j) sb.append('-');
                sb.append('\n');
            }
        }
        return sb.toString();
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
