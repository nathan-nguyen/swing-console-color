package com.noiprocs;

import com.noiprocs.core.GameContext;
import com.noiprocs.core.control.command.InputCommand;
import com.noiprocs.ui.console.ConsoleUIConfig;
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

    ConsoleUIConfig.CLEAR_SCREEN = false;

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
            // Map arrow keys to w/a/s/d for HUD navigation
            Character arrowKey = null;
            if (event.getKeyCode() == KeyEvent.VK_UP) arrowKey = 'w';
            else if (event.getKeyCode() == KeyEvent.VK_DOWN) arrowKey = 's';
            else if (event.getKeyCode() == KeyEvent.VK_LEFT) arrowKey = 'a';
            else if (event.getKeyCode() == KeyEvent.VK_RIGHT) arrowKey = 'd';

            // Handle crafting HUD interactions (client-side only)
            if (gameScreen.hud.craftingHud.isOpen()) {
              if (event.getKeyCode() == KeyEvent.VK_ESCAPE) { // ESC
                gameScreen.hud.craftingHud.close();
                return;
              }
              if (event.getKeyCode() == KeyEvent.VK_TAB) { // TAB to toggle to equipment HUD
                gameScreen.hud.craftingHud.close();
                gameScreen.hud.equipmentHud.open();
                return;
              }
              if (event.getKeyCode() == KeyEvent.VK_ENTER) { // Enter
                gameScreen.hud.craftingHud.craftSelectedItem();
                return;
              }
              // Handle arrow keys for navigation
              if (arrowKey != null) {
                gameScreen.hud.craftingHud.handleNavigation(arrowKey);
                return;
              }
              // Ignore other keys when HUD is open
              return;
            }

            // Handle equipment HUD interactions (client-side only)
            if (gameScreen.hud.equipmentHud.isOpen()) {
              if (event.getKeyCode() == KeyEvent.VK_ESCAPE) { // ESC
                gameScreen.hud.equipmentHud.close();
                return;
              }
              if (event.getKeyCode() == KeyEvent.VK_TAB) { // TAB to toggle to crafting HUD
                gameScreen.hud.equipmentHud.close();
                gameScreen.hud.craftingHud.open();
                return;
              }
              if (event.getKeyCode() == KeyEvent.VK_ENTER) { // Enter
                gameScreen.hud.equipmentHud.handleEquipmentAction();
                return;
              }
              // Handle arrow keys for navigation
              if (arrowKey != null) {
                gameScreen.hud.equipmentHud.handleNavigation(arrowKey);
                return;
              }
              // Handle number keys 1-4 to swap inventory slots (only when inventory is selected)
              if (!gameScreen.hud.equipmentHud.isEquipmentSelected()) {
                char ch = event.getKeyChar();
                if (ch >= '1' && ch <= '4') {
                  int targetSlot = ch - '1'; // Convert '1'-'4' to 0-3
                  int currentSlot = gameScreen.hud.equipmentHud.getSelectedSlot();
                  if (targetSlot != currentSlot) {
                    gameContext.controlManager.swapInventorySlots(currentSlot, targetSlot);
                  }
                  return;
                }
              }
              // Ignore other keys when HUD is open
              return;
            }

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
              if (ch == 'e' || ch == 'E') {
                gameScreen.hud.inventoryInteractionHud.handleEquipmentAction();
                return;
              }
              // Handle arrow keys for navigation
              if (arrowKey != null) {
                gameScreen.hud.inventoryInteractionHud.handleNavigation(arrowKey);
                return;
              }
              // Ignore other keys when HUD is open
              return;
            }

            char ch = event.getKeyChar();

            // Check for 'e' key to open equipment HUD
            if (ch == 'e') {
              gameScreen.hud.equipmentHud.open();
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
