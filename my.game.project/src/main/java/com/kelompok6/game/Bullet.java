package com.kelompok6.game;

import com.googlecode.lanterna.TextColor;

public class Bullet extends Entity {
    private int dx;
    private int dy;
    private int speedDelay;
    private int tickCounter = 0;


    public Bullet(int x, int y, int dx, int dy, TextColor color, int speedDelay) {
        super(x, y, '|', color);
        this.dx = dx;
        this.dy = dy;
        this.speedDelay = speedDelay;


        if(dx < 0) this.symbol = '\\';
        if(dx > 0) this.symbol = '/';
    }

    public void update() {
        tickCounter++;
        if (tickCounter >= speedDelay) {
            x += dx;
            y += dy;
            tickCounter = 0;
        }
    }
}