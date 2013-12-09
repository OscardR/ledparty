package es.uji.ei1057.ledparty;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Óscar Gómez <oscar.gomez@uji.es> on 27/11/13.
 */
public class BluetoothSingleton extends BroadcastReceiver {

    /**
     * Para gestionar la transferencia en el bluetooth
     */
    private BluetoothSocket socket;
    private BluetoothAdapter bluetooth;
    private BluetoothDevice device;
    private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    private DeviceListAdapter listAdapter;

    // UUID y MAC por defecto, para funcionar con el K70IC
    private String DEFAULT_MAC = "00:0A:3A:7D:66:08";
    private String DEFAULT_UUID = "0000110a-0000-1000-8000-00805f9b34fb";

    public OutputStream outputStream;
    public InputStream inputStream;

    private Handler handler = new Handler();
    ProgressDialog progressDialog;

    /**
     * Para gestionar el contexto y la instanciación singletoniana
     */
    private static BluetoothSingleton instance;
    private Context context;

    /**
     * Constructor privado para singleton. Inicializa ciertos elementos
     *
     * @param ctx
     */
    private BluetoothSingleton(Context ctx) {
        context = ctx;
        bluetooth = BluetoothAdapter.getDefaultAdapter();
        listAdapter = new DeviceListAdapter(context, deviceList);
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

    /**
     * Activa el Bluetooth si está apagado, y comienza a buscar dispositivos
     */
    public void startBluetooth() {
        if (bluetooth.isEnabled()) {
            startDiscovery();
        } else {
            context.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            context.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            Log.d("ledparty", "startBluetooth");
        }
    }

    /**
     * Realizar el rastreo para encontrar dispositivos
     */
    private void startDiscovery() {

        // Iniciar un diálogo de progreso
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Descubriendo...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        deviceList.clear();

        context.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        context.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        Toast.makeText(context, "Descubriendo dispositivos Bluetooth...", Toast.LENGTH_SHORT).show();
        bluetooth.startDiscovery();
    }

    /**
     * Obtiene una lista de dispositivos, y muestra un listado para poder elegir
     */
    public void showDeviceList() {
        Toast.makeText(context, "Mostrando lista de dispositivos", Toast.LENGTH_LONG).show();

        // Experimento: añadir los dispositivos pareados
//        for (Object bt : bluetooth.getBondedDevices().toArray()) {
//            deviceList.add((BluetoothDevice) bt);
//        }

        AlertDialog bluetoothDialog = new AlertDialog.Builder(context)
                .setTitle("Elige un dispositivo")
                .setAdapter(listAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // TODO: Conectar con el dispositivo elegido
                        device = (BluetoothDevice) listAdapter.getItem(which);
                        ParcelUuid uuids[] = device.getUuids();
                        for (ParcelUuid uuid : uuids) {
                            Log.d("ledparty", "UUID: " + uuid.toString());
                        }
                        //String uuid = uuids[uuids.length - 1].toString();
                        Toast.makeText(context, "Conectando a dispositivo con UUID " + DEFAULT_UUID, Toast.LENGTH_LONG).show();

                        if (connect()) { //connect(uuid)) { // UUID por defecto: 0000110a-0000-1000-8000-00805f9b34fb
                            Intent modesIntent = new Intent(context, ModesActivity.class);
                            context.startActivity(modesIntent);
                        } else {
                            Toast.makeText(context, "Conexión rechazada!", Toast.LENGTH_SHORT).show();
                        }
                        ;

                        //Toast.makeText(context, "Has hecho click en " + which, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    /**
     * Conectarse a un dispositivo, dado su UUID en formato String
     *
     * @return
     */
    public boolean connect(final String uuid) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                //Create a Socket connection: need the server's UUID number of registered
                try {
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));

                    socket.connect();
                    Log.d("ledparty", "conectado!");

                    try {
                        inputStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();
                    } catch (Exception e) {
                        Log.d("ledparty", "EXCEPCIÓN!!: " + e.getMessage());
                    }

                    outputStream.write("AAAAAHHHH!!!".getBytes());

                    return true;
                } catch (Exception e) {
                    //e.printStackTrace();
                    Log.e("ledparty", "excepción en bluetooth.connect()");
                    return false;
                }
            }
        };
        Future<Boolean> future = executor.submit(callable);
        boolean exito = false;
        try {
            exito = future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            return exito;
        }
    }

    /**
     * Conexión por defecto al UUID del BluetoothCar
     * TODO: Cambiar por el UUID del módulo Bluetooth de pruebas
     *
     * @return
     */
    public boolean connect() {
        return connect(DEFAULT_UUID);
    }

    /**
     * Handler para reaccionar a la activación del Bluetooth (y otros Intents)
     *
     * @param context el contexto en el cual se ha procesado el Intent
     * @param intent  el Intent que vamos a procesar
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("ledparty", "dentro de onReceive");
        if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) { //Bluetooth permission request window

            Log.d("ledparty", "" + intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR));

            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) == BluetoothAdapter.STATE_ON) {
                Toast.makeText(context, "Bluetooth Activado!", Toast.LENGTH_SHORT).show();

                // Empezar a buscar dispositivos
                startDiscovery();

            } else {
                Toast.makeText(context, "Bluetooth NO Activado!", Toast.LENGTH_SHORT).show();
            }

        } else if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {

            // Extraer el dispositivo y añadir a la lista
            device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            deviceList.add(device);
            Toast.makeText(context, "Nuevo dispositivo encontrado", Toast.LENGTH_SHORT).show();

        } else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
            Toast.makeText(context, "Descubrimiento finalizado", Toast.LENGTH_SHORT).show();

            // Cerrar el diálogo de progreso
            progressDialog.dismiss();

            // Mostrar la lista de dispositivos encontrados
            showDeviceList();

        } else {
            Log.d("ledparty", "recibido un intent sin handler");
        }
    }

    /**
     * Aquí finalizamos y cerramos los sockets
     */
    public void destroy() {
        context.unregisterReceiver(this);
        if (socket != null)
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
