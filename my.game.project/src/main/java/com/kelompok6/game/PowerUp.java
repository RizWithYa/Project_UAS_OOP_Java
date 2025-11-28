package com.kelompok6.game;

import com.googlecode.lanterna.TextColor;

public class PowerUp extends Entity {
    private int type;

    public PowerUp(int x, int y, int type) {
        super(x, y, '?', TextColor.ANSI.CYAN);
        this.type = type;

        if(type == 1) {
            this.symbol = 'M';
            this.color = TextColor.ANSI.YELLOW;
        }
        if(type == 2) {
            this.symbol = 'S';
            this.color = TextColor.ANSI.CYAN;
        }
    }

    public int getType() { return type; }

    public void update() {
        y++;
    }
}