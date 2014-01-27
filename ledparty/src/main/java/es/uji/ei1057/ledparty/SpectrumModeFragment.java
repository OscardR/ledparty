package es.uji.ei1057.ledparty;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by oscar on 11/12/13.
 */
public class SpectrumModeFragment extends ModeFragment {

    volatile private double[] lastAmplitude;
    private AudioReadThread audioReadThread;
    private Activity context;
    private boolean canvasThreadRunning;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ModeFragment newInstance() {
        SpectrumModeFragment fragment = new SpectrumModeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, ModeFragment.MODE_SPECTRAL);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mode_spectrum, container, false);

        Toast.makeText(getActivity().getApplicationContext(), "SpectrumMode", Toast.LENGTH_SHORT).show();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        audioReadThread = new AudioReadThread();
        try {
            //audioReadThread.start();
        } catch (IllegalStateException ise) {
            Toast.makeText(context, "No se puede inicializar el micrófono :(", Toast.LENGTH_SHORT).show();
            return;
        }

        Thread actualizar = new Thread() {
            public void run() {
                canvasThreadRunning = true;
                while (canvasThreadRunning) {
                    try {
                        context.runOnUiThread(new Runnable() {
                            public void run() {
                                // TODO: actualizar canvas con valores
                            }
                        });
                        // publicar en Bluetooth el valor
                        ((ModesActivity) context).updateSpectrum(lastAmplitude);

                        Thread.sleep(500, 0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        //actualizar.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("ledparty", "spectrum.onpause");
        if (audioReadThread != null)
            audioReadThread.interrupt();
        audioReadThread = null;
        canvasThreadRunning = false;
    }

    public void putNewAmplitude(double[] amplitude) {
        this.lastAmplitude = amplitude;
    }

    class AudioReadThread extends Thread {

        private boolean running;

        private AudioRecord record;
        private int SAMPLING_RATE = 44100;
        private int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        public AudioReadThread() {
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
            while (running) {
                try {
                    short[] buff = new short[BUFFER_SIZE];
                    record.read(buff, 0, buff.length);

                    double[] amplitude = FFT.transform(buff, BUFFER_SIZE / 2, 1);
                    putNewAmplitude(amplitude);

                    Thread.sleep(500);
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
}