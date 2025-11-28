package com.kelompok6.game;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.TextCharacter;

public class Player extends Entity {

    public Player(int startX, int startY) {
        super(startX, startY, '^', TextColor.ANSI.GREEN);
    }

    public void move(int dx) {
        this.x += dx;
        if(this.x < 1) this.x = 1;
        if(this.x > 78) this.x = 78;
    }

    @Override
    public void draw(Screen screen) {
        screen.setCharacter(x, y, new TextCharacter('^', color, TextColor.ANSI.BLACK));

        screen.setCharacter(x - 1, y + 1, new TextCharacter('/', color, TextColor.ANSI.BLACK));

        screen.setCharacter(x, y + 1, new TextCharacter('M', TextColor.ANSI.CYAN, TextColor.ANSI.BLACK));

        screen.setCharacter(x + 1, y + 1, new TextCharacter('\\', color, TextColor.ANSI.BLACK));
    }
}
