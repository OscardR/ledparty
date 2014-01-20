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
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by oscar on 11/12/13.
 */
public class BeatboxModeFragment extends ModeFragment {

    volatile private double lastAmplitude;
    private AudioReadThread audioReadThread;
    private Activity context;
    private boolean canvasThreadRunning;
    private ImageView imgBg;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ModeFragment newInstance() {
        BeatboxModeFragment fragment = new BeatboxModeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, ModeFragment.MODE_BEATBOX);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mode_beatbox, container, false);
        imgBg = (ImageView) rootView.findViewById(R.id.imgBgBeatbox);

        Toast.makeText(getActivity().getApplicationContext(), "BeatboxMode", Toast.LENGTH_SHORT).show();
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
        audioReadThread.start();

        Thread actualizar = new Thread() {
            public void run() {
                canvasThreadRunning = true;
                while (canvasThreadRunning) {
                    try {
                        context.runOnUiThread(new Runnable() {
                            public void run() {
                                // TODO: actualizar canvas
                                imgBg.setAlpha((float) lastAmplitude);
                            }
                        });
                        // TODO: publicar en Bluetooth el valor
                        //Log.d("ledparty", "amplitud: " + lastAmplitude);
                        Thread.sleep(30, 0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        actualizar.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (audioReadThread != null)
            audioReadThread.interrupt();
        audioReadThread = null;
        canvasThreadRunning = false;
    }

    public void putNewAmplitude(double amplitude) {
        this.lastAmplitude = amplitude;
    }

    class AudioReadThread extends Thread {

        private int SAMPLING_RATE = 44100;
        private AudioRecord record;
        private int bufferSize = 8000;
        private boolean running;
        private double max_amplitude_square = 11000;

        public AudioReadThread() {
            record = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLING_RATE,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);
        }

        public void run() {
            running = true;
            record.startRecording();
            while (running) {
                try {
                    short[] buff = new short[8000];
                    record.read(buff, 0, buff.length);

                    double amplitude = calculateMean(buff);
                    putNewAmplitude(amplitude);

                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            record.stop();
        }

        private double calculateMean(short[] nums) {
            double ms = 0;
            for (short num : nums) {
                ms += num * num;
            }
            ms /= nums.length;
            if (ms > max_amplitude_square * max_amplitude_square)
                max_amplitude_square = Math.sqrt(ms);
            return Math.sqrt(ms) / max_amplitude_square;
        }

        public void interrupt() {
            this.running = false;
        }
    }
}
