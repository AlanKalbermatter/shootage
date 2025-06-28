package utils;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {
    private static Clip backgroundClip;
    private static boolean soundEffectsEnabled = true;
    private static boolean musicEnabled = true;

    // plays looping background music
    public static void playBackgroundMusic(String resourcePath) {
        if (!musicEnabled) return;
        try {
            stopBackgroundMusic();
            URL url = SoundManager.class.getResource(resourcePath);
            if (url == null) throw new IOException("Sound not found: " + resourcePath);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioIn);
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMusicVolume(float volume) {
        // volume: 0.0 (mute) to 1.0 (max)
        if (backgroundClip != null) {
            FloatControl gainControl = (FloatControl) backgroundClip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum(); // typically -80.0f
            float max = gainControl.getMaximum(); // typically 6.0f
            // Convert 0..1 volume to dB logarithmically
            float dB = (float) (Math.log10(Math.max(volume, 0.0001)) * 20.0);
            dB = Math.max(min, Math.min(max, dB));
            gainControl.setValue(dB);
        }
    }

    // stops the background music if playing
    public static void stopBackgroundMusic() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
            backgroundClip.close();
        }
    }

    // plays a (short) sound effect, e.g. for shots
    public static void playSoundEffect(String resourcePath) {
        if (!soundEffectsEnabled) return;
        new Thread(() -> {
            try {
                URL url = SoundManager.class.getResource(resourcePath);
                if (url == null) throw new IOException("Sound not found: " + resourcePath);
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}