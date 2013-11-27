package es.uji.ei1057.ledparty;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by oscar on 27/11/13.
 */
public class BluetoothSingleton extends BroadcastReceiver {

    /**
     * Para gestionar la transferencia en el bluetooth
     */
    public BluetoothSocket socket;
    public BluetoothAdapter bluetooth;
    public BluetoothDevice device;

    public OutputStream outputStream;
    public InputStream inputStream;

    private Handler handler = new Handler();
    private static BluetoothSingleton instance;
    private Context context;

    private BluetoothSingleton(Context ctx) {
        context = ctx;
        bluetooth = BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothSingleton getInstance(Context ctx) {
        if (instance == null) {
            return new BluetoothSingleton(ctx);
        }
        return instance;
    }

    public static BluetoothSingleton getInstance() {
        return instance;
    }

    public void toggleBluetooth() {
        if (bluetooth.isEnabled()) {
            if (socket != null)
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            bluetooth.disable();
        } else {
            context.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            context.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            Log.d("ledparty", "toggleBluetooth");
            //bluetooth.enable();
        }
    }

    public void getDeviceList() {
        // TODO: Empezar a buscar dispositivos. Copiar de BTManager
        Toast.makeText(context, "Buscando dispositivos LED", Toast.LENGTH_LONG).show();
    }

    /**
     * Conectarse a un dispositivo, dado su UUID en formato String
     *
     * @return
     */
    public boolean connect(String uuid) {
        //Create a Socket connection: need the server's UUID number of registered
        try {
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));

            socket.connect();
            Log.d("bluetoothcar", "conectado!");

            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean connect() {
        return connect("00001101-0000-1000-8000-00805F9B34FB");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ledparty", "dentro de onReceive");
        if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) //Bluetooth permission request window
            Log.d("ledparty", "" + intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR));
        if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) == BluetoothAdapter.STATE_ON) {
            Toast.makeText(context, "Bluetooth Activado!", Toast.LENGTH_SHORT).show();

            Intent modesIntent = new Intent(context, ModesActivity.class);
            context.startActivity(modesIntent);
        } else {
            Toast.makeText(context, "Bluetooth NO Activado!", Toast.LENGTH_SHORT).show();
        }
    }
}
