package com.kelompok6.game;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.screen.Screen;

public class Main {
    public static void main(String[] args) {
        try {

            DefaultTerminalFactory factory = new DefaultTerminalFactory();

            factory.setPreferTerminalEmulator(true);

            Screen screen = factory.createScreen();
            // --------------------------

            screen.startScreen();
            screen.setCursorPosition(null);

            Game game = new Game(screen);
            game.run();

            screen.stopScreen();
            System.out.println("Game Over!");

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}