package Project;

import java.awt.*;

public class Hero {
    private Image spriteSheet;
    private Image fallbackSprite;
    private int x, y;
    private int width, height;
    private int speed;
    private int facingDir = 0;
    private int animFrame = 0;
    private int animTick = 0;
    private boolean moving = false;

    private static final int ANIM_DELAY = 8;
    private static final int SHEET_COLS = 4;
    private static final int SHEET_ROWS = 4;

    public Hero(Image spriteSheet, Image fallbackSprite, int x, int y, int width, int height, int speed) {
        this.spriteSheet = spriteSheet;
        this.fallbackSprite = fallbackSprite;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    public void update(boolean up, boolean down, boolean left, boolean right, int boundsW, int boundsH) {
        int prevX = x;
        int prevY = y;

        if (up)    y -= speed;
        if (down)  y += speed;
        if (left)  x -= speed;
        if (right) x += speed;

        int dx = x - prevX;
        int dy = y - prevY;
        moving = (dx != 0 || dy != 0);

        if (moving) {
            if (Math.abs(dx) >= Math.abs(dy)) {
                facingDir = (dx > 0) ? 2 : 1;
            } else {
                facingDir = (dy > 0) ? 0 : 3;
            }

            animTick++;
            if (animTick >= ANIM_DELAY) {
                animTick = 0;
                animFrame = (animFrame + 1) % 4;
            }
        } else {
            animFrame = 0;
            animTick = 0;
        }

        x = Math.max(0, Math.min(x, boundsW - width));
        y = Math.max(0, Math.min(y, boundsH - height));
    }

    public void draw(Graphics g, Component observer) {
        if (spriteSheet != null) {
            int fw = spriteSheet.getWidth(observer) / SHEET_COLS;
            int fh = spriteSheet.getHeight(observer) / SHEET_ROWS;
            if (fw > 0 && fh > 0) {
                int sx = animFrame * fw;
                int sy = facingDir * fh;
                g.drawImage(spriteSheet, x, y, x + width, y + height,
                        sx, sy, sx + fw, sy + fh, observer);
                return;
            }
        }

        if (fallbackSprite != null) {
            g.drawImage(fallbackSprite, x, y, width, height, observer);
            return;
        }

        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
        g.setColor(Color.WHITE);
        String[] dirLabels = { "D", "L", "R", "U" };
        g.drawString(dirLabels[facingDir], x + width / 2 - 4, y + height / 2 + 4);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void resetAnimation() {
        moving = false;
        animFrame = 0;
        animTick = 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getCenterX() { return x + width / 2; }
    public int getCenterY() { return y + height / 2; }
    public boolean isMoving() { return moving; }
    public int getFacingDir() { return facingDir; }
}
