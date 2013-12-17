package es.uji.ei1057.ledparty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by oscar on 11/12/13.
 */
public class BeatboxModeFragment extends ModeFragment {

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

        Toast.makeText(getActivity().getApplicationContext(), "BeatboxMode", Toast.LENGTH_SHORT).show();
        return rootView;
    }
}
