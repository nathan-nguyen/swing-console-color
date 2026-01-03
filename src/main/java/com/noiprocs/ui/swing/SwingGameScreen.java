package com.noiprocs.ui.swing;

import static com.noiprocs.ui.console.ConsoleUIConfig.HEIGHT;
import static com.noiprocs.ui.console.ConsoleUIConfig.WIDTH;

import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.ui.console.ConsoleGameScreen;
import com.noiprocs.ui.console.util.ColorMapper;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.*;

public class SwingGameScreen extends ConsoleGameScreen {
  private static final boolean USE_SQUARE_FONT = false;
  public final JFrame jframe = new JFrame();
  public final JTextPane jTextPane = new JTextPane();

  // Color character to AWT Color mapping
  private static final Map<Character, Color> COLOR_CHAR_MAP = new HashMap<>();

  static {
    for (Map.Entry<Character, ColorMapper.RGB> entry : ColorMapper.COLOR_MAP.entrySet()) {
      ColorMapper.RGB rgb = entry.getValue();
      COLOR_CHAR_MAP.put(entry.getKey(), new Color(rgb.r, rgb.g, rgb.b));
    }
  }

  public SwingGameScreen() {
    jframe.setSize(440, 690);
    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jframe.setVisible(true);

    jframe.add(jTextPane);
    jTextPane.setEditable(false);
    jTextPane.setFont(new Font("monospaced", Font.PLAIN, 12));
    jTextPane.setBackground(Color.BLACK);
    jTextPane.setForeground(Color.WHITE);

    if (USE_SQUARE_FONT) this.useSquareFont();
  }

  @Override
  public void render(int delta) {
    Model playerModel = gameContext.modelManager.getModel(gameContext.username);
    // Only render when playerModel is existing
    if (playerModel == null) return;

    // Render map with color support
    this.constructScreen((PlayerModel) playerModel);
    renderFromMaps((PlayerModel) playerModel);
  }

  private void renderFromMaps(PlayerModel playerModel) {
    try {
      // Create a new StyledDocument for this render
      DefaultStyledDocument newDocument = new DefaultStyledDocument();

      // 1. Add HUD and top border
      StringBuilder header = new StringBuilder();
      header.append(getHudString(playerModel)).append('\n');
      for (int j = 0; j < WIDTH + 2; j++) {
        header.append('-');
      }
      header.append('\n');
      newDocument.insertString(
          newDocument.getLength(), header.toString(), getOrCreateStyle(newDocument, Color.WHITE));

      // 2. Render all rows - batch entire segments across all rows
      StringBuilder textSegment = new StringBuilder();
      Color segmentColor = null;

      for (int i = 0; i < HEIGHT; i++) {
        // Process left border with current segment if color matches, otherwise flush and start new
        if (segmentColor != null && !segmentColor.equals(Color.WHITE)) {
          newDocument.insertString(
              newDocument.getLength(),
              textSegment.toString(),
              getOrCreateStyle(newDocument, segmentColor));
          textSegment.setLength(0);
          segmentColor = null;
        }
        if (segmentColor == null) {
          segmentColor = Color.WHITE;
        }
        textSegment.append('|');

        // Process row content
        for (int j = 0; j < WIDTH; j++) {
          char ch = map[i][j] == 0 ? ' ' : map[i][j];
          char colorChar = colorMap[i][j];

          Color color =
              (colorChar != 0) ? COLOR_CHAR_MAP.getOrDefault(colorChar, Color.WHITE) : Color.WHITE;

          // If color changed, flush current segment
          if (segmentColor != null && !color.equals(segmentColor)) {
            newDocument.insertString(
                newDocument.getLength(),
                textSegment.toString(),
                getOrCreateStyle(newDocument, segmentColor));
            textSegment.setLength(0);
            segmentColor = color;
          }

          textSegment.append(ch);
        }

        // Right border and newline - flush if color doesn't match white
        if (segmentColor != null && !segmentColor.equals(Color.WHITE)) {
          newDocument.insertString(
              newDocument.getLength(),
              textSegment.toString(),
              getOrCreateStyle(newDocument, segmentColor));
          textSegment.setLength(0);
          segmentColor = Color.WHITE;
        }
        textSegment.append("|\n");
      }

      // Flush any remaining segment
      if (textSegment.length() > 0) {
        newDocument.insertString(
            newDocument.getLength(),
            textSegment.toString(),
            getOrCreateStyle(newDocument, segmentColor));
      }

      // 3. Add bottom border
      StringBuilder footer = new StringBuilder();
      for (int j = 0; j < WIDTH + 2; j++) {
        footer.append('-');
      }
      newDocument.insertString(
          newDocument.getLength(), footer.toString(), getOrCreateStyle(newDocument, Color.WHITE));

      // Atomically replace the document (no flashing!)
      SwingUtilities.invokeLater(
          () -> {
            jTextPane.setStyledDocument(newDocument);
          });
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  private Style getOrCreateStyle(StyledDocument document, Color color) {
    String key = color.toString();
    Style style = document.getStyle(key);
    if (style == null) {
      style = document.addStyle(key, null);
      StyleConstants.setForeground(style, color);
    }
    return style;
  }

  private void useSquareFont() {
    try {
      Font font = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/square.ttf"));
      jTextPane.setFont(font.deriveFont(12f));
      jframe.setSize(750, 580);
    } catch (FontFormatException | IOException e) {
      e.printStackTrace();
    }
  }
}
