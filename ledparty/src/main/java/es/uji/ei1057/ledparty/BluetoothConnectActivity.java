package es.uji.ei1057.ledparty;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class BluetoothConnectActivity extends Activity {

    ImageButton btnBluetooth;
    BluetoothConnectFragment bluetoothConnectFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);

        btnBluetooth = (ImageButton) findViewById(R.id.btnBluetooth);

        if (savedInstanceState == null) {
            bluetoothConnectFragment = new BluetoothConnectFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(bluetoothConnectFragment, "bluetooth_connect_fragment")
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth_connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickBluetoothConnect(View view) {
        // Desactivar el botón mientras se buscan dispositivos
        btnBluetooth.setEnabled(false);

        bluetoothConnectFragment.onClickBluetoothConnect();

        // Activar al acabar de buscar dispositivos
        btnBluetooth.setEnabled(true);
    }

}
