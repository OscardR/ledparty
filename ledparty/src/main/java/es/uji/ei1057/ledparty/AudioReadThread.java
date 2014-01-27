package es.uji.ei1057.ledparty;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by oscar on 27/01/14.
 */
class AudioReadThread extends Thread {

    private AudioFragment main;
    private AudioTransform audioTransformer;
    private boolean running;

    private AudioRecord record;
    private int SAMPLING_RATE = 44100;
    private int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    private double max_amplitude_square = 11000;

    public AudioReadThread(AudioFragment fragment, AudioTransform at) {

        this.main = fragment;
        this.audioTransformer = at;

        if (record != null && record.getState() != AudioRecord.STATE_UNINITIALIZED) {
            if (record.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
                record.stop();
            }
            record.release();
        }
        record = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLING_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE);
        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("ledparty", "AudioRecord no inicializado!");
        }
    }

    public void run() throws IllegalStateException {
        running = true;
        record.startRecording();
        double[] audioValues;
        while (running) {
            try {
                short[] buff = new short[BUFFER_SIZE];
                record.read(buff, 0, buff.length);

                audioValues = audioTransformer.transform(buff);
                main.putAudioValues(audioValues);

                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        record.stop();
        record.release();
    }

    public void interrupt() {
        this.running = false;
    }
}