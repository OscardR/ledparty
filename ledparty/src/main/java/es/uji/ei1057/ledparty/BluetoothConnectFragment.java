package es.uji.ei1057.ledparty;

import android.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class BluetoothConnectFragment extends Fragment {

    public BluetoothConnectFragment() {
    }

    public void onClickBluetoothConnect() {
        Log.d("ledparty", "bluetoothConnectFragment.onClickBluetoothConnect");
        Toast.makeText(getActivity(), "Buscando dispositivos LED", Toast.LENGTH_LONG).show();
        // TODO: Empezar a buscar dispositivos. Copiar de BTManager
    }
}