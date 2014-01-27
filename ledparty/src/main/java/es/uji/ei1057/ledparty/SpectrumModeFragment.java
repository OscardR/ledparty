package es.uji.ei1057.ledparty;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by oscar on 11/12/13.
 */
public class SpectrumModeFragment extends ModeFragment implements AudioFragment {

    volatile private double[] lastAudioValues;
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

        audioReadThread = new AudioReadThread(this, new FFTAudioTransform());
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
                        ((ModesActivity) context).updateSpectrum(lastAudioValues);

                        Thread.sleep(500, 0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        ;
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

    @Override
    public void putAudioValues(double[] values) {
        this.lastAudioValues = values;
    }
}