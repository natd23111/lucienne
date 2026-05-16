package Project;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.LineUnavailableException;

public class SoundManager {

    private static final int SAMPLE_RATE = 8000;

    public void playCorrect() {
        playThread(() -> {
            playTone(800, 80, 0.6);
            sleep(60);
            playTone(1200, 120, 0.6);
        });
    }

    public void playWrong() {
        playThread(() -> {
            playTone(200, 150, 0.5);
            sleep(40);
            playTone(120, 200, 0.4);
        });
    }

    public void playCollect() {
        playThread(() -> {
            playTone(1000, 60, 0.4);
            sleep(40);
            playTone(1200, 60, 0.4);
            sleep(40);
            playTone(1400, 100, 0.5);
        });
    }

    public void playPurchase() {
        playThread(() -> {
            playTone(1500, 80, 0.5);
            sleep(30);
            playTone(1500, 80, 0.5);
            sleep(30);
            playTone(1800, 120, 0.5);
        });
    }

    public void playDamage() {
        playThread(() -> playTone(80, 200, 0.5));
    }

    public void playVictory() {
        playThread(() -> {
            playTone(600, 100, 0.5);
            sleep(60);
            playTone(800, 100, 0.5);
            sleep(60);
            playTone(1000, 100, 0.5);
            sleep(60);
            playTone(1200, 200, 0.6);
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
