package com.noiprocs;

import com.noiprocs.core.GameContext;
import com.noiprocs.core.graphics.HitboxManagerInterface;
import com.noiprocs.ui.console.ConsoleHitboxManager;
import com.noiprocs.ui.console.ConsoleSpriteManager;
import com.noiprocs.ui.swing.SwingGameScreen;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class App {
    public static void main(String[] args) {
        String platform = args[0];
        String username = args[1];
        String type = args[2];
        String hostname = args[3];
        int port = Integer.parseInt(args[4]);

        // Initialize gameContext
        GameContext gameContext = new GameContext(platform, username, type, hostname, port);
        gameContext.setSpriteManager(new ConsoleSpriteManager());

        SwingGameScreen gameScreen = new SwingGameScreen();
        gameContext.setGameScreen(gameScreen);

        HitboxManagerInterface hitboxManager = new ConsoleHitboxManager();
        gameContext.setHitboxManager(hitboxManager);

        // Start a separate thread for game, main thread is for control
        Runnable task = () -> gameContext.run();
        Thread thread = new Thread(task);
        thread.start();

        gameScreen.jTextArea.addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent event) {
                        gameContext.controlManager.processInput(String.valueOf(event.getKeyChar()));
                    }
                }
        );
    }
}
