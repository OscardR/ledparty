package es.uji.ei1057.ledparty;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by oscar on 11/12/13.
 */
public class TextModeFragment extends ModeFragment {

    /**
     * UI elements
     */
    private EditText editText;
    private Button btnSend;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static Fragment newInstance() {
        TextModeFragment fragment = new TextModeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, ModeFragment.MODE_TEXT);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mode_text, container, false);

        editText = (EditText) rootView.findViewById(R.id.editText);
        btnSend = (Button) rootView.findViewById(R.id.btnSend);

        Toast.makeText(getActivity().getApplicationContext(), "TextMode", Toast.LENGTH_SHORT).show();
        return rootView;
    }
}
