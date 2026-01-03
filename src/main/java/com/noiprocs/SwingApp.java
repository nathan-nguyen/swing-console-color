package com.noiprocs;

import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Config;
import com.noiprocs.core.control.InputCommand;
import com.noiprocs.ui.console.hitbox.ConsoleHitboxManager;
import com.noiprocs.ui.console.sprite.ConsoleSpriteManager;
import com.noiprocs.ui.swing.SwingGameScreen;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class SwingApp {
  public static void main(String[] args) {
    String platform = args[0];
    String username = args[1];
    String type = args[2];
    String hostname = args[3];
    int port = Integer.parseInt(args[4]);

    Config.CLEAR_SCREEN = false;

    SwingGameScreen gameScreen = new SwingGameScreen();

    // Initialize gameContext
    GameContext gameContext =
        GameContext.build(
            platform,
            username,
            type,
            hostname,
            port,
            new ConsoleHitboxManager(),
            new ConsoleSpriteManager(),
            gameScreen);

    // Start a separate thread for game, main thread is for control
    Thread thread = new Thread(gameContext::run);
    thread.start();

    gameScreen.jTextPane.addKeyListener(
        new KeyAdapter() {
          private final Set<Character> keyPressedSet = new HashSet<>();

          @Override
          public void keyPressed(KeyEvent event) {
            // Handle inventory HUD interactions (client-side only)
            if (gameScreen.hud.inventoryInteractionHud.isChestOpen()) {
              if (event.getKeyCode() == KeyEvent.VK_ESCAPE) { // ESC
                gameScreen.hud.inventoryInteractionHud.close();
                return;
              }
              if (event.getKeyCode() == KeyEvent.VK_ENTER) { // Enter
                gameScreen.hud.inventoryInteractionHud.transferSelectedItem();
                return;
              }
              char ch = event.getKeyChar();
              if (ch == 'w' || ch == 'a' || ch == 's' || ch == 'd') {
                gameScreen.hud.inventoryInteractionHud.handleNavigation(ch);
                return;
              }
              // Ignore other keys when HUD is open
              return;
            }

            keyPressedSet.add(event.getKeyChar());
            gameContext.controlManager.processInput(
                new InputCommand(gameContext.username, event.getKeyChar()));
          }

          @Override
          public void keyReleased(KeyEvent event) {
            keyPressedSet.remove(event.getKeyChar());
            if (keyPressedSet.isEmpty()) {
              gameContext.controlManager.processInput(new InputCommand(gameContext.username, "h"));
            }
          }
        });
  }
}
