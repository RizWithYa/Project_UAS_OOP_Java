package com.kelompok6.game;

import com.googlecode.lanterna.TextColor;

public class Player extends Entity {

    public Player(int startX, int startY) {
        // Simbol '^' warna Hijau
        super(startX, startY, '^', TextColor.ANSI.GREEN);
    }

    public void move(int dx) {
        this.x += dx;
        // Batasi agar tidak keluar layar terminal (lebar standar ~80)
        if(this.x < 0) this.x = 0;
        if(this.x > 79) this.x = 79;
    }
}