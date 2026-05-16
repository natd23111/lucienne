package Project;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.LineUnavailableException;

public class SoundManager {

    private static final int SAMPLE_RATE = 22050;

    public void playCorrect() {
        playThread(() -> {
            playTone(500, 100, 0.5);
            sleep(60);
            playTone(700, 150, 0.5);
        });
    }

    public void playWrong() {
        playThread(() -> {
            playTone(180, 150, 0.4);
            sleep(40);
            playTone(110, 220, 0.35);
        });
    }

    public void playCollect() {
        playThread(() -> {
            playTone(500, 70, 0.35);
            sleep(40);
            playTone(600, 70, 0.35);
            sleep(40);
            playTone(750, 120, 0.4);
        });
    }

    public void playPurchase() {
        playThread(() -> {
            playTone(600, 90, 0.45);
            sleep(30);
            playTone(600, 90, 0.45);
            sleep(30);
            playTone(800, 140, 0.45);
        });
    }

    public void playDamage() {
        playThread(() -> playTone(70, 250, 0.45));
    }

    public void playVictory() {
        playThread(() -> {
            playTone(350, 120, 0.45);
            sleep(70);
            playTone(450, 120, 0.45);
            sleep(70);
            playTone(550, 120, 0.45);
            sleep(70);
            playTone(650, 250, 0.5);
        });
    }

    private void playThread(Runnable sounds) {
        Thread t = new Thread(() -> {
            try {
                sounds.run();
            } catch (Exception ex) {
                System.err.println("Sound error: " + ex.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void playTone(int hz, int msecs, double vol) {
        try {
            AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(af);
            line.open(af, SAMPLE_RATE);
            line.start();

            byte[] buf = new byte[msecs * SAMPLE_RATE / 1000];
            for (int i = 0; i < buf.length; i++) {
                double angle = i / (SAMPLE_RATE / (double) hz) * 2.0 * Math.PI;
                buf[i] = (byte) (Math.sin(angle) * 127 * vol);
            }

            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (LineUnavailableException e) {
            System.err.println("Sound unavailable: " + e.getMessage());
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
