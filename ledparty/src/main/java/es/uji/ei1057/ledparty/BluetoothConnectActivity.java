package es.uji.ei1057.ledparty;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class BluetoothConnectActivity extends Activity {

    BluetoothMaster bluetoothMaster;
    ImageButton btnBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);
        btnBluetooth = (ImageButton) findViewById(R.id.btnBluetooth);

        // Al crear la actividad, se instancia el BluetoothMaster, para el resto de operaciones
        bluetoothMaster = ((LEDPartyApp) getApplicationContext()).getBluetoothMaster(this);
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
            Toast.makeText(this, "No implementado", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handler para el botón principal, que activa el bluetooth y busca dispositivos
     *
     * @param view la vista del botón pulsado
     */
    public void onClickBluetoothConnect(View view) {
        // Desactivar el botón mientras se buscan dispositivos
        btnBluetooth.setEnabled(false);

        // El singleton del bluetooth lo hace todo
        bluetoothMaster.startBluetooth();

        // Activar al acabar de buscar dispositivos
        btnBluetooth.setEnabled(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothMaster.destroy();
    }
}
