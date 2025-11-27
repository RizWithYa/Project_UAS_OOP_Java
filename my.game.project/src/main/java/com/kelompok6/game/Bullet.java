package com.kelompok6.game;

import com.googlecode.lanterna.TextColor;

public class Bullet extends Entity {
    private int dy;
    private int speedDelay; // Seberapa sering peluru bergerak
    private int tickCounter = 0;

    // Constructor baru menerima parameter 'speedDelay'
    public Bullet(int x, int y, int dy, TextColor color, int speedDelay) {
        super(x, y, '|', color);
        this.dy = dy;
        this.speedDelay = speedDelay;
    }

    public void update() {
        tickCounter++;
        // Hanya bergerak jika counter sudah mencapai speedDelay
        if (tickCounter >= speedDelay) {
            y += dy;
            tickCounter = 0;
        }
    }
}