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

    // --- STATE MANAGEMENT ---
    private boolean applicationRunning = true; // Loop Utama Aplikasi
    private boolean isPlaying = true;          // Loop Gameplay
    private boolean isGameOver = false;        // Loop Game Over

    private Player player;

    // PERBAIKAN 1: Generics (Wajib <...>)
    private List<Enemy> enemies;
    private List<Bullet> bullets;
    private List<Bullet> enemyBullets;

    private int level = 1;
    private int score = 0;

    // --- INPUT VARIABLES (Metode Time Decay) ---
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;

    // Timer Input untuk "Key Released" palsu
    private long lastTimeLeft = 0;
    private long lastTimeRight = 0;
    private long lastTimeSpace = 0;
    private static final long INPUT_TIMEOUT = 100;

    // Timer Gameplay
    private long lastShotTime = 0;
    private static final long FIRE_RATE = 100;
    private long lastEnemyShotTime = 0;
    private long enemyFireCooldown = 2000;

    public Game(Screen screen) {
        this.screen = screen;

        // Inisialisasi List
        this.enemies = new ArrayList<>();
        this.bullets = new ArrayList<>();
        this.enemyBullets = new ArrayList<>();

        // Reset/Init awal
        resetGame();
    }

    // Method untuk memulai ulang game dari nol
    private void resetGame() {
        level = 1;
        score = 0;
        player = new Player(40, 22);

        enemies.clear();
        bullets.clear();
        enemyBullets.clear();

        initLevel(level);

        // Reset State
        isPlaying = true;
        isGameOver = false;

        // Reset Input State agar tidak nyangkut
        leftPressed = false;
        rightPressed = false;
        spacePressed = false;
    }

    private void initLevel(int lvl) {
        enemies.clear();
        bullets.clear();
        enemyBullets.clear();
        int rows = Math.min(lvl + 1, 5);
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < 8; x++) {
                enemies.add(new Enemy(10 + (x * 6), 2 + (y * 2), lvl));
            }
        }
    }

    public void run() throws Exception {
        // --- LOOP UTAMA APLIKASI ---
        while (applicationRunning) {

            // 1. FASE BERMAIN
            while (isPlaying) {
                // Baca input untuk menggerakkan player
                processGameplayInput();

                update();
                draw();

                Thread.sleep(15); // Speed Game
            }

            // 2. FASE GAME OVER
            while (isGameOver) {
                // Baca input khusus Game Over (R atau ESC)
                processGameOverInput();

                drawGameOver();

                Thread.sleep(50); // Hemat CPU saat game over
            }
        }
    }

    // --- INPUT HANDLING KHUSUS GAMEPLAY ---
    private void processGameplayInput() throws java.io.IOException {
        KeyStroke key = screen.pollInput();
        while (key != null) {
            long now = System.currentTimeMillis();

            if (key.getKeyType() == KeyType.ArrowLeft) {
                leftPressed = true;
                lastTimeLeft = now;
            }
            if (key.getKeyType() == KeyType.ArrowRight) {
                rightPressed = true;
                lastTimeRight = now;
            }
            if (key.getKeyType() == KeyType.Character && key.getCharacter() == ' ') {
                spacePressed = true;
                lastTimeSpace = now;
            }
            if (key.getKeyType() == KeyType.Escape) {
                applicationRunning = false;
                isPlaying = false;
                isGameOver = false;
            }
            key = screen.pollInput(); // Baca tombol berikutnya (drain)
        }
    }

    // --- INPUT HANDLING KHUSUS GAME OVER ---
    private void processGameOverInput() throws java.io.IOException {
        KeyStroke key = screen.pollInput();
        while (key != null) {
            if (key.getKeyType() == KeyType.Character &&
                    (key.getCharacter() == 'e' || key.getCharacter() == 'E')) {
                resetGame(); // RESTART!
            }
            if (key.getKeyType() == KeyType.Escape) {
                applicationRunning = false;
                isGameOver = false; // Keluar total
            }
            key = screen.pollInput();
        }
    }

    private void update() {
        long now = System.currentTimeMillis();

        // 1. DECAY LOGIC (Menggantikan keyReleased AWT)
        if (now - lastTimeLeft > INPUT_TIMEOUT) leftPressed = false;
        if (now - lastTimeRight > INPUT_TIMEOUT) rightPressed = false;
        if (now - lastTimeSpace > INPUT_TIMEOUT) spacePressed = false;

        // 2. PLAYER MOVEMENT
        if (leftPressed) player.move(-1);
        if (rightPressed) player.move(1);

        // 3. PLAYER SHOOT
        if (spacePressed) {
            if (now - lastShotTime > FIRE_RATE) {
                bullets.add(new Bullet(player.getX(), player.getY() - 1, -1, TextColor.ANSI.YELLOW, 0));
                lastShotTime = now;
            }
        }

        // 4. UPDATE BULLETS
        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet b = bulletIter.next();
            b.update();
            if (b.getY() < 0) bulletIter.remove();
        }

        // 5. UPDATE ENEMY BULLETS
        Iterator<Bullet> ebIter = enemyBullets.iterator();
        while (ebIter.hasNext()) {
            Bullet b = ebIter.next();
            b.update();
            if (b.getX() == player.getX() && b.getY() == player.getY()) {
                triggerGameOver();
            }
            if (b.getY() > 25) ebIter.remove();
        }

        // 6. UPDATE ENEMIES
        for (Enemy e : enemies) {
            e.update();
            if (e.getX() == player.getX() && e.getY() == player.getY()) {
                triggerGameOver();
            }
        }

        // 7. ENEMY SHOOTING (Global Cooldown Logic)
        if (level >= 2 && !enemies.isEmpty()) {
            if (now - lastEnemyShotTime > enemyFireCooldown) {
                int randomIndex = (int) (Math.random() * enemies.size());
                Enemy shooter = enemies.get(randomIndex);
                enemyBullets.add(new Bullet(shooter.getX(), shooter.getY() + 1, 1, TextColor.ANSI.RED, 15));
                lastEnemyShotTime = now;
                enemyFireCooldown = Math.max(1000, 2000 - (level * 100));
            }
        }

        checkCollisions();

        // 8. LEVEL UP
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
                    enemyIter.remove();
                    hit = true;
                    score += 10;
                    break;
                }
            }
            if (hit) bulletIter.remove();
        }
    }

    private void draw() throws Exception {
        screen.clear();
        player.draw(screen);
        for (Enemy e : enemies) e.draw(screen);
        for (Bullet b : bullets) b.draw(screen);
        for (Bullet eb : enemyBullets) eb.draw(screen);

        screen.newTextGraphics().putString(0, 0, "SCORE: " + score);
        screen.newTextGraphics().putString(20, 0, "LEVEL: " + level);
        screen.refresh();
    }

    private void drawGameOver() throws Exception {
        screen.clear();

        // Menggunakan TextGraphics agar lebih rapi
        String title = "GAME OVER";
        String scoreText = "Final Score: " + score;
        String info = "Press [E] to Retry or [ESC] to Quit";

        screen.newTextGraphics().setForegroundColor(TextColor.ANSI.RED).putString(35, 10, title);
        screen.newTextGraphics().setForegroundColor(TextColor.ANSI.WHITE).putString(32, 12, scoreText);
        screen.newTextGraphics().setForegroundColor(TextColor.ANSI.YELLOW).putString(25, 15, info);

        screen.refresh();
    }
}