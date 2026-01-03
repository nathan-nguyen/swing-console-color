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
import java.util.List;
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

      // 1. Add player info HUD
      newDocument.insertString(
          newDocument.getLength(),
          hud.getPlayerInfo(playerModel) + "\n",
          getOrCreateStyle(newDocument, Color.WHITE));

      // 2. Build map lines as array of strings
      String[] mapLines = buildMapLines();

      // 3. Check for overlay content
      List<String> overlayLines = hud.getOverlayContent(playerModel);

      // 4. Render each line with appropriate colors
      for (int i = 0; i < mapLines.length; ++i) {
        String line = mapLines[i];

        // Use overlay line if available
        if (overlayLines != null && i < overlayLines.size()) {
          newDocument.insertString(
              newDocument.getLength(),
              overlayLines.get(i) + "\n",
              getOrCreateStyle(newDocument, Color.WHITE));
        } else if (i == 0 || i == mapLines.length - 1) {
          // Top or bottom border - render as white
          newDocument.insertString(
              newDocument.getLength(), line + "\n", getOrCreateStyle(newDocument, Color.WHITE));
        } else {
          // Map content line with colors
          renderMapLineWithColors(newDocument, line, i - 1);
        }
      }

      // Atomically replace the document (no flashing!)
      SwingUtilities.invokeLater(
          () -> {
            jTextPane.setStyledDocument(newDocument);
          });
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  private String[] buildMapLines() {
    String[] lines = new String[HEIGHT + 2]; // +2 for top and bottom borders
    int lineIndex = 0;

    // Top border
    StringBuilder borderSb = new StringBuilder();
    for (int j = 0; j < WIDTH + 2; j++) {
      borderSb.append('-');
    }
    lines[lineIndex++] = borderSb.toString();

    // Map rows
    for (int i = 0; i < HEIGHT; i++) {
      StringBuilder lineSb = new StringBuilder();
      lineSb.append('|');
      for (int j = 0; j < WIDTH; j++) {
        char ch = map[i][j] == 0 ? ' ' : map[i][j];
        lineSb.append(ch);
      }
      lineSb.append('|');
      lines[lineIndex++] = lineSb.toString();
    }

    // Bottom border
    lines[lineIndex] = borderSb.toString();

    return lines;
  }

  private void renderMapLineWithColors(DefaultStyledDocument document, String line, int mapRow)
      throws BadLocationException {
    StringBuilder textSegment = new StringBuilder();
    Color segmentColor = Color.WHITE;

    for (int i = 0; i < line.length(); i++) {
      char ch = line.charAt(i);

      // Border characters are always white
      if (ch == '|') {
        if (textSegment.length() > 0) {
          document.insertString(
              document.getLength(),
              textSegment.toString(),
              getOrCreateStyle(document, segmentColor));
          textSegment.setLength(0);
        }
        segmentColor = Color.WHITE;
        textSegment.append(ch);
        continue;
      }

      // Map content - check color
      int mapCol = i - 1; // Account for left border
      if (mapCol >= 0 && mapCol < WIDTH && mapRow >= 0 && mapRow < HEIGHT) {
        char colorChar = colorMap[mapRow][mapCol];
        Color color =
            (colorChar != 0) ? COLOR_CHAR_MAP.getOrDefault(colorChar, Color.WHITE) : Color.WHITE;

        if (!color.equals(segmentColor)) {
          if (textSegment.length() > 0) {
            document.insertString(
                document.getLength(),
                textSegment.toString(),
                getOrCreateStyle(document, segmentColor));
            textSegment.setLength(0);
          }
          segmentColor = color;
        }
      }

      textSegment.append(ch);
    }

    // Flush remaining segment
    if (textSegment.length() > 0) {
      document.insertString(
          document.getLength(), textSegment.toString(), getOrCreateStyle(document, segmentColor));
    }
    document.insertString(document.getLength(), "\n", getOrCreateStyle(document, Color.WHITE));
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
