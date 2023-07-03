package io.agora.meta.example_voice_driver.utils;

import android.content.Context;
import android.os.Process;

import java.io.IOException;
import java.io.InputStream;

public class AudioFileReader {
    private static final String AUDIO_FILE = "test_local_audio.pcm";
    public static final float BYTE_PER_SAMPLE = 1.0f * MetaConstants.AUDIO_BITS_PER_SAMPLE / 8 * MetaConstants.AUDIO_SAMPLE_NUM_OF_CHANNEL;
    public static final float DURATION_PER_SAMPLE = 1000.0f / MetaConstants.AUDIO_SAMPLE_RATE; // ms
    public static final float SAMPLE_COUNT_PER_MS = MetaConstants.AUDIO_SAMPLE_RATE * 1.0f / 1000; // ms

    private static final int BUFFER_SAMPLE_COUNT = (int) (SAMPLE_COUNT_PER_MS * 40); // 40ms sample count
    private static final int BUFFER_BYTE_SIZE = (int) (BUFFER_SAMPLE_COUNT * BYTE_PER_SAMPLE); // byte
    private static final long BUFFER_DURATION = (long) (BUFFER_SAMPLE_COUNT * DURATION_PER_SAMPLE); // ms

    private final Context context;
    private final OnAudioReadListener audioReadListener;
    private volatile boolean pushing = false;
    private InnerThread thread;
    private InputStream inputStream;

    public AudioFileReader(Context context, OnAudioReadListener listener) {
        this.context = context;
        this.audioReadListener = listener;
    }

    public void start() {
        if (thread == null) {
            thread = new InnerThread();
            thread.start();
        }
    }

    public void stop() {
        pushing = false;
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                thread = null;
            }
        }
    }

    public interface OnAudioReadListener {
        void onAudioRead(byte[] buffer, long timestamp);
    }

    private class InnerThread extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                inputStream = context.getAssets().open(AUDIO_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            pushing = true;

            long startTime = System.currentTimeMillis();
            ;
            int sentAudioFrames = 0;
            while (pushing) {
                if (audioReadListener != null) {
                    audioReadListener.onAudioRead(readBuffer(), System.currentTimeMillis());
                }
                ++sentAudioFrames;
                long nextFrameStartTime = sentAudioFrames * BUFFER_DURATION + startTime;
                long now = System.currentTimeMillis();

                if (nextFrameStartTime > now) {
                    long sleepDuration = nextFrameStartTime - now;
                    try {
                        Thread.sleep(sleepDuration);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    inputStream = null;
                }
            }
        }

        private byte[] readBuffer() {
            byte[] buffer = new byte[BUFFER_BYTE_SIZE];
            try {
                if (inputStream.read(buffer) < 0) {
                    inputStream.reset();
                    return readBuffer();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return buffer;
        }
    }
}
