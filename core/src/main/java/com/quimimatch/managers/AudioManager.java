package com.quimimatch.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;

/**
 * Sprint E — Sistema de audio procedural.
 * Genera efectos de sonido y música usando PCM sin archivos externos.
 * Cada mundo tiene un tema musical con melodía y escala distintas.
 */
public class AudioManager {

    private static AudioManager instance;
    public static AudioManager get() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    private static final int SAMPLE_RATE = 44100;

    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private float   musicVolume  = 0.4f;
    private float   sfxVolume    = 0.7f;

    // Hilo de música en background
    private Thread  musicThread;
    private volatile boolean musicRunning = false;
    private volatile int     currentWorld = -1;

    // ── Escalas musicales por mundo ──────────────────────────────
    // Frecuencias en Hz de cada nota de la melodía
    private static final double[][] WORLD_SCALES = {
        // Mundo 1 — Do mayor (alegre, simple)
        { 261.6, 293.7, 329.6, 349.2, 392.0, 440.0, 493.9, 523.3 },
        // Mundo 2 — Re menor (misterioso)
        { 293.7, 329.6, 349.2, 392.0, 440.0, 466.2, 523.3, 587.3 },
        // Mundo 3 — Mi frigio (tenso, ácidos)
        { 329.6, 349.2, 370.0, 415.3, 466.2, 493.9, 554.4, 622.3 },
        // Mundo 4 — Sol mixolidio (pesado, metales)
        { 196.0, 220.0, 246.9, 261.6, 293.7, 329.6, 349.2, 392.0 },
        // Mundo 5 — La mayor (orgánico, fluido)
        { 440.0, 493.9, 523.3, 587.3, 659.3, 698.5, 784.0, 880.0 },
        // Mundo 6 — Si disminuido (complejo, avanzado)
        { 493.9, 523.3, 554.4, 622.3, 659.3, 739.9, 830.6, 987.8 },
    };

    // Patrones de melodía (índices en la escala) por mundo
    private static final int[][] WORLD_PATTERNS = {
        { 0,2,4,5,4,2,0,2,4,7,5,4,2,0 },  // Mundo 1
        { 0,1,3,2,1,0,3,2,4,3,1,0,2,1 },  // Mundo 2
        { 0,2,1,3,2,4,3,2,1,0,2,3,1,0 },  // Mundo 3
        { 0,0,3,2,0,0,4,3,1,1,4,3,2,1 },  // Mundo 4
        { 4,5,7,5,4,2,0,2,4,5,4,2,4,5 },  // Mundo 5
        { 7,5,4,3,5,4,2,1,3,2,0,1,2,3 },  // Mundo 6
    };

    private AudioManager() {}

    // ── API pública ──────────────────────────────────────────────

    public void playMatch()     { if (soundEnabled) playTone(880, 80,  0.5f, WaveType.SINE); }
    public void playSwap()      { if (soundEnabled) playTone(440, 60,  0.3f, WaveType.SINE); }
    public void playSpecial()   { if (soundEnabled) playChord(new double[]{523,659,784}, 200, 0.4f); }
    public void playVictory()   { if (soundEnabled) playMelody(new double[]{523,587,659,698,784}, 120, 0.5f); }
    public void playDefeat()    { if (soundEnabled) playMelody(new double[]{392,349,329,293,261}, 140, 0.5f); }
    public void playPowerup()   { if (soundEnabled) playMelody(new double[]{440,523,659,880}, 80,  0.4f); }
    public void playCollect()   { if (soundEnabled) playTone(660, 50,  0.3f, WaveType.SINE); }
    public void playObstacle()  { if (soundEnabled) playTone(220, 100, 0.4f, WaveType.SQUARE); }
    public void playMenuClick() { if (soundEnabled) playTone(523, 60,  0.3f, WaveType.SINE); }
    public void playCascade()   { if (soundEnabled) playChord(new double[]{659,784,987}, 150, 0.4f); }

    /** Inicia la música del mundo indicado (1-6) */
    public void startWorldMusic(int world) {
        if (!musicEnabled) return;
        if (currentWorld == world && musicRunning) return;
        stopMusic();
        currentWorld = world;
        musicRunning = true;
        int w = Math.max(0, Math.min(world - 1, 5));
        musicThread = new Thread(() -> runWorldMusic(w));
        musicThread.setDaemon(true);
        musicThread.start();
    }

    public void stopMusic() {
        musicRunning = false;
        if (musicThread != null) {
            musicThread.interrupt();
            try { musicThread.join(500); } catch (InterruptedException ignored) {}
            musicThread = null;
        }
        currentWorld = -1;
    }

    /** Silencia música temporalmente (tablero) sin perder qué mundo era */
    public void muteMusic() {
        musicRunning = false;
        if (musicThread != null) {
            musicThread.interrupt();
            try { musicThread.join(300); } catch (InterruptedException ignored) {}
            musicThread = null;
        }
    }

    /** Reanuda música del mundo actual */
    public void unmuteMusic() {
        if (musicEnabled && currentWorld >= 0) {
            startWorldMusic(currentWorld + 1);
        }
    }

    public void pauseMusic()  { muteMusic(); }
    public void resumeMusic() { if (currentWorld >= 0) startWorldMusic(currentWorld + 1); }

    public void setSoundEnabled(boolean v) { soundEnabled = v; }
    public void setMusicEnabled(boolean v) { musicEnabled = v; if (!v) stopMusic(); }
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isMusicEnabled() { return musicEnabled; }

    public void dispose() { stopMusic(); }

    // ── Generación de música procedural ─────────────────────────

    private void runWorldMusic(int worldIdx) {
        double[] scale   = WORLD_SCALES[worldIdx];
        int[]    pattern = WORLD_PATTERNS[worldIdx];

        AudioDevice device = null;
        try {
            device = Gdx.audio.newAudioDevice(SAMPLE_RATE, true); // mono
            int noteIdx = 0;

            while (musicRunning) {
                int scaleIdx  = pattern[noteIdx % pattern.length];
                double freq   = scale[scaleIdx];
                double bassFreq = scale[0] / 2.0; // nota de bajo una octava abajo

                // Nota melódica
                short[] melody = generateTone(freq,     180, musicVolume * 0.6f, WaveType.SINE);
                // Nota de bajo
                short[] bass   = generateTone(bassFreq, 360, musicVolume * 0.4f, WaveType.TRIANGLE);
                // Mezclar
                short[] mix    = mixSamples(melody, bass);

                if (!musicRunning || Thread.currentThread().isInterrupted()) break;
                device.writeSamples(mix, 0, mix.length);

                noteIdx++;
                // Pausa breve entre notas
                short[] silence = new short[SAMPLE_RATE / 20]; // 50ms
                if (musicRunning && !Thread.currentThread().isInterrupted())
                    device.writeSamples(silence, 0, silence.length);
            }
        } catch (Exception e) {
            // Audio error — silencio
        } finally {
            if (device != null) device.dispose();
        }
    }

    // ── Efectos de sonido ────────────────────────────────────────

    private void playTone(double freq, int ms, float vol, WaveType type) {
        new Thread(() -> {
            AudioDevice dev = null;
            try {
                dev = Gdx.audio.newAudioDevice(SAMPLE_RATE, true);
                short[] samples = generateTone(freq, ms, vol, type);
                dev.writeSamples(samples, 0, samples.length);
            } catch (Exception ignored) {
            } finally {
                if (dev != null) dev.dispose();
            }
        }).start();
    }

    private void playChord(double[] freqs, int ms, float vol) {
        new Thread(() -> {
            AudioDevice dev = null;
            try {
                dev = Gdx.audio.newAudioDevice(SAMPLE_RATE, true);
                short[] mix = new short[(int)(SAMPLE_RATE * ms / 1000.0)];
                for (double f : freqs) {
                    short[] tone = generateTone(f, ms, vol / freqs.length, WaveType.SINE);
                    for (int i = 0; i < mix.length && i < tone.length; i++)
                        mix[i] = clamp(mix[i] + tone[i]);
                }
                applyEnvelope(mix);
                dev.writeSamples(mix, 0, mix.length);
            } catch (Exception ignored) {
            } finally {
                if (dev != null) dev.dispose();
            }
        }).start();
    }

    private void playMelody(double[] freqs, int noteMs, float vol) {
        new Thread(() -> {
            AudioDevice dev = null;
            try {
                dev = Gdx.audio.newAudioDevice(SAMPLE_RATE, true);
                for (double f : freqs) {
                    short[] samples = generateTone(f, noteMs, vol, WaveType.SINE);
                    dev.writeSamples(samples, 0, samples.length);
                }
            } catch (Exception ignored) {
            } finally {
                if (dev != null) dev.dispose();
            }
        }).start();
    }

    // ── Síntesis PCM ─────────────────────────────────────────────

    private enum WaveType { SINE, SQUARE, TRIANGLE, SAW }

    private short[] generateTone(double freq, int ms, float vol, WaveType type) {
        int samples = (int)(SAMPLE_RATE * ms / 1000.0);
        short[] buf = new short[samples];
        for (int i = 0; i < samples; i++) {
            double t    = (double) i / SAMPLE_RATE;
            double phase = 2 * Math.PI * freq * t;
            double sample;
            switch (type) {
                case SQUARE:   sample = Math.sin(phase) >= 0 ? 1.0 : -1.0; break;
                case TRIANGLE: sample = 2.0 / Math.PI * Math.asin(Math.sin(phase)); break;
                case SAW:      sample = 2.0 * (freq * t - Math.floor(freq * t + 0.5)); break;
                default:       sample = Math.sin(phase); break; // SINE
            }
            // Envolvente ADSR simple
            double env = 1.0;
            double attackSamples  = samples * 0.05;
            double releaseSamples = samples * 0.2;
            if (i < attackSamples) env = i / attackSamples;
            else if (i > samples - releaseSamples) env = (samples - i) / releaseSamples;
            buf[i] = clamp((short)(sample * env * vol * Short.MAX_VALUE));
        }
        return buf;
    }

    private void applyEnvelope(short[] buf) {
        int release = buf.length / 4;
        for (int i = 0; i < release; i++) {
            double factor = (double)(buf.length - release + i) / buf.length;
            buf[buf.length - release + i] = clamp((short)(buf[buf.length - release + i] * (1.0 - factor)));
        }
    }

    private short[] mixSamples(short[] a, short[] b) {
        int len = Math.max(a.length, b.length);
        short[] out = new short[len];
        for (int i = 0; i < len; i++) {
            int sa = i < a.length ? a[i] : 0;
            int sb = i < b.length ? b[i] : 0;
            out[i] = clamp((short)((sa + sb) / 2));
        }
        return out;
    }

    private short clamp(short v) { return v; }
    private short clamp(int v) {
        if (v > Short.MAX_VALUE) return Short.MAX_VALUE;
        if (v < Short.MIN_VALUE) return Short.MIN_VALUE;
        return (short) v;
    }
}
