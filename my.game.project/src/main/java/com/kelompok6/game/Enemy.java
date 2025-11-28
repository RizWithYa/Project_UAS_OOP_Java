package com.kelompok6.game;

import com.googlecode.lanterna.TextColor;

public class Enemy extends Entity {
    private int direction = 1;
    private int speedDelay;
    private int tickCounter = 0;

    public Enemy(int x, int y, int level) {
        super(x, y, 'W', TextColor.ANSI.RED);

        this.speedDelay = Math.max(2, 10 - level);
    }

    public void update() {
        tickCounter++;

        if(tickCounter >= speedDelay) {
            x += direction;
            tickCounter = 0;


            if(x > 75 || x < 2) {
                direction *= -1;
                y++;
            }
        }
    }
}