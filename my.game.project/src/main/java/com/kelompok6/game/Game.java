package com.kelompok6.game;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Game {
    private Screen screen;
    private boolean applicationRunning = true;
    private boolean isPlaying = true;
    private boolean isGameOver = false;

    private Player player;

    private List<Enemy> enemies;
    private List<Bullet> bullets;
    private List<Bullet> enemyBullets;
    private List<PowerUp> powerUps;

    private int level = 1;
    private int score = 0;

    private boolean tripleShotActive = false;
    private long tripleShotEndTime = 0;
    private boolean hasShield = false;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;

    private long lastTimeLeft = 0;
    private long lastTimeRight = 0;
    private long lastTimeSpace = 0;
    private static final long INPUT_TIMEOUT = 100;

    private long lastShotTime = 0;
    private static final long FIRE_RATE = 100;

    private long lastEnemyShotTime = 0;
    private long enemyFireCooldown = 2000;

    public Game(Screen screen) {
        this.screen = screen;

        this.enemies = new ArrayList<>();
        this.bullets = new ArrayList<>();
        this.enemyBullets = new ArrayList<>();
        this.powerUps = new ArrayList<>();


        resetGame();
    }

    private void initLevel(int lvl) {
        enemies.clear();
        bullets.clear();
        enemyBullets.clear();
        powerUps.clear();
        int rows = Math.min(lvl + 1, 5);
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < 8; x++) {
                enemies.add(new Enemy(10 + (x * 6), 2 + (y * 2), lvl));
            }
        }
    }

    private void resetGame() {
        level = 1;
        score = 0;
        player = new Player(40, 22);
        initLevel(1);
        isGameOver = false;
        isPlaying = true;
        tripleShotActive = false;
        hasShield = false;

        leftPressed = false; rightPressed = false; spacePressed = false;
    }

    public void run() throws Exception {
        while (applicationRunning) {
            while (isPlaying) {
                processInput();
                update();
                draw();
                Thread.sleep(15);
            }
            while (isGameOver) {
                processGameOverInput();
                drawGameOver();
                Thread.sleep(50);
            }
        }
    }

    private void processInput() throws java.io.IOException {
        KeyStroke key = screen.pollInput();
        long now = System.currentTimeMillis();

        while (key != null) {
            if (key.getKeyType() == KeyType.ArrowLeft) {
                leftPressed = true; lastTimeLeft = now;
            }
            if (key.getKeyType() == KeyType.ArrowRight) {
                rightPressed = true; lastTimeRight = now;
            }
            if (key.getKeyType() == KeyType.Character && key.getCharacter() == ' ') {
                spacePressed = true; lastTimeSpace = now;
            }
            if (key.getKeyType() == KeyType.Escape) {
                applicationRunning = false; isPlaying = false; isGameOver = false;
            }
            key = screen.pollInput();
        }

        if (now - lastTimeLeft > INPUT_TIMEOUT) leftPressed = false;
        if (now - lastTimeRight > INPUT_TIMEOUT) rightPressed = false;
        if (now - lastTimeSpace > INPUT_TIMEOUT) spacePressed = false;
    }

    private void processGameOverInput() throws java.io.IOException {
        KeyStroke key = screen.pollInput();
        while(key != null) {
            if (key.getKeyType() == KeyType.Character && (key.getCharacter() == 'r' || key.getCharacter() == 'R')) {
                resetGame();
            }
            if (key.getKeyType() == KeyType.Escape) {
                applicationRunning = false; isGameOver = false;
            }
            key = screen.pollInput();
        }
    }

    private boolean checkHitPlayer(int targetX, int targetY) {
        boolean hitX = targetX >= player.getX() - 1 && targetX <= player.getX() + 1;
        boolean hitY = targetY >= player.getY() && targetY <= player.getY() + 1;
        return hitX && hitY;
    }

    private void update() {
        long now = System.currentTimeMillis();

        if(tripleShotActive && now > tripleShotEndTime) tripleShotActive = false;

        if (leftPressed) player.move(-1);
        if (rightPressed) player.move(1);

        if (spacePressed) {
            if (now - lastShotTime > FIRE_RATE) {
                bullets.add(new Bullet(player.getX(), player.getY() - 1, 0, -1, TextColor.ANSI.YELLOW, 0));

                if(tripleShotActive) {
                    bullets.add(new Bullet(player.getX() - 1, player.getY() - 1, -1, -1, TextColor.ANSI.YELLOW, 2));
                    bullets.add(new Bullet(player.getX() + 1, player.getY() - 1, 1, -1, TextColor.ANSI.YELLOW, 2));
                }
                lastShotTime = now;
            }
        }

        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet b = bulletIter.next();
            b.update();
            if (b.getY() < 0 || b.getX() < 0 || b.getX() > 80) bulletIter.remove();
        }

        Iterator<PowerUp> puIter = powerUps.iterator();
        while (puIter.hasNext()) {
            PowerUp p = puIter.next();
            p.update();

            if(checkHitPlayer(p.getX(), p.getY())) {
                if(p.getType() == 1) {
                    tripleShotActive = true;
                    tripleShotEndTime = now + 5000;
                }
                if(p.getType() == 2) hasShield = true;
                puIter.remove();
            }
            else if(p.getY() > 30) puIter.remove();
        }

        Iterator<Bullet> ebIter = enemyBullets.iterator();
        while (ebIter.hasNext()) {
            Bullet b = ebIter.next();
            b.update();

            if (checkHitPlayer(b.getX(), b.getY())) {
                if(hasShield) {
                    hasShield = false;
                    ebIter.remove();
                } else {
                    triggerGameOver();
                }
            }
            if (b.getY() > 30) ebIter.remove();
        }

        Iterator<Enemy> enemyIter = enemies.iterator();
        while(enemyIter.hasNext()) {
            Enemy e = enemyIter.next();
            e.update();

            if (checkHitPlayer(e.getX(), e.getY())) {
                if(hasShield) {
                    hasShield = false;
                    enemyIter.remove();
                } else {
                    triggerGameOver();
                }
            }
        }

        if (level >= 2 && !enemies.isEmpty()) {
            if (now - lastEnemyShotTime > enemyFireCooldown) {
                int randomIndex = (int) (Math.random() * enemies.size());
                Enemy shooter = enemies.get(randomIndex);
                enemyBullets.add(new Bullet(shooter.getX(), shooter.getY() + 1, 0, 1, TextColor.ANSI.RED, 15));
                lastEnemyShotTime = now;
                enemyFireCooldown = Math.max(1000, 2000 - (level * 100));
            }
        }

        checkCollisions();

        if (enemies.isEmpty()) {
            level++;
            initLevel(level);
        }
    }

    private void triggerGameOver() {
        isPlaying = false;
        isGameOver = true;
    }

    private void checkCollisions() {
        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet b = bulletIter.next();

            Iterator<Enemy> enemyIter = enemies.iterator();
            boolean hit = false;

            while (enemyIter.hasNext()) {
                Enemy e = enemyIter.next();
                if (b.getX() == e.getX() && b.getY() == e.getY()) {
                    if(Math.random() < 0.1) {
                        int type = Math.random() < 0.5 ? 1 : 2;
                        powerUps.add(new PowerUp(e.getX(), e.getY(), type));
                    }
                    enemyIter.remove();
                    hit = true;
                    score += 10;
                    break;
                }
            }
            if(hit) bulletIter.remove();
        }
    }

    private void draw() throws Exception {
        screen.clear();

        if(hasShield) {
            screen.newTextGraphics().setForegroundColor(TextColor.ANSI.CYAN);
            player.draw(screen);
        } else {
            screen.newTextGraphics().setForegroundColor(TextColor.ANSI.GREEN);
            player.draw(screen);
        }

        for (Enemy e : enemies) e.draw(screen);
        for (Bullet b : bullets) b.draw(screen);
        for (Bullet eb : enemyBullets) eb.draw(screen);
        for (PowerUp p : powerUps) p.draw(screen);

        screen.newTextGraphics().setForegroundColor(TextColor.ANSI.WHITE);
        screen.newTextGraphics().putString(0, 0, "SCORE: " + score);
        screen.newTextGraphics().putString(20, 0, "LEVEL: " + level);

        if(tripleShotActive) screen.newTextGraphics().putString(40, 0, "[TRIPLE SHOT]");
        if(hasShield) screen.newTextGraphics().putString(60, 0, "[SHIELD ACTIVE]");

        screen.refresh();
    }

    private void drawGameOver() throws Exception {
        screen.clear();
        screen.newTextGraphics().putString(35, 10, "GAME OVER");
        screen.newTextGraphics().putString(32, 12, "Final Score: " + score);
        screen.newTextGraphics().putString(28, 15, "Press [R] Retry or [ESC] Quit");
        screen.refresh();
    }
}