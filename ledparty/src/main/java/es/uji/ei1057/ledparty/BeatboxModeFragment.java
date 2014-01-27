package es.uji.ei1057.ledparty;

import android.app.Activity;
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
public class BeatboxModeFragment extends ModeFragment implements AudioFragment {

    volatile private double[] lastAudioValues = {0};
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

        audioReadThread = new AudioReadThread(this, new MeanAudioTransform());
        try {
            audioReadThread.start();
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
                                // actualizar canvas
                                imgBg.setAlpha((float) lastAudioValues[0]);
                            }
                        });
                        // publicar en Bluetooth el valor
                        if (context != null)
                            ((ModesActivity) context).updateBeatbox(lastAudioValues[0]);

                        Thread.sleep(30, 0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (NullPointerException npe) {
                        ;
                    }
                }
            }
        };
        actualizar.start();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("ledparty", "beatbox.onpause");
        if (audioReadThread != null)
            audioReadThread.interrupt();
        audioReadThread = null;
        canvasThreadRunning = false;
    }

    @Override
    public void putAudioValues(double[] values) {
        this.lastAudioValues = values;
    }
}
