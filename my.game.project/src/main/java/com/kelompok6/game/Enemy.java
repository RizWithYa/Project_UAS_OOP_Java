package com.kelompok6.game;

import com.googlecode.lanterna.TextColor;

public class Enemy extends Entity {
    private int direction = 1; // 1 = Kanan, -1 = Kiri
    private int speedDelay;
    private int tickCounter = 0;

    public Enemy(int x, int y, int level) {
        super(x, y, 'W', TextColor.ANSI.RED);
        // Semakin tinggi level, semakin kecil delay (semakin cepat)
        this.speedDelay = Math.max(2, 10 - level);
    }

    public void update() {
        tickCounter++;
        // Bergerak hanya jika tickCounter mencapai speedDelay
        if(tickCounter >= speedDelay) {
            x += direction;
            tickCounter = 0;

            // Logika Patroli (Bolak-balik)
            if(x > 75 || x < 2) {
                direction *= -1; // Balik arah
                y++; // Turun satu baris
            }
        }
    }
}