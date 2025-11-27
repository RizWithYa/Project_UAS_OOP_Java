package com.kelompok6.game;

import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;

public abstract class Entity {
    protected int x, y;
    protected char symbol;
    protected TextColor color;

    public Entity(int x, int y, char symbol, TextColor color) {
        this.x = x;
        this.y = y;
        this.symbol = symbol;
        this.color = color;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    // Fungsi menggambar ke layar Lanterna
    public void draw(Screen screen) {
        screen.setCharacter(x, y, new TextCharacter(symbol, color, TextColor.ANSI.BLACK));
    }
}