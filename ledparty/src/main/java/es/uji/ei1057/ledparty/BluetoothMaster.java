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
public class BluetoothMaster extends BroadcastReceiver {

    /**
     * Para gestionar la transferencia en el bluetooth
     */
    private BluetoothSocket socket;
    private BluetoothAdapter bluetooth;
    private BluetoothDevice device;
    private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    private DeviceListAdapter listAdapter;

    /**
     * UUID y MAC por defecto, para funcionar con el K70IC
     */
    private String DEFAULT_MAC = "00:0A:3A:7D:66:08";
    private String DEFAULT_UUID = "00001101-0000-1000-8000-00805f9b34fb";

    /**
     * Streams para enviar y recibir datos
     */
    public OutputStream outputStream;
    public InputStream inputStream;

    /**
     * Constantes para el envío de datos y comandos al servidor
     */
    private static final byte[] MODE_TEXT_COMMAND = "\\T".getBytes();
    private static final byte[] MODE_SPECTRAL_COMMAND = "\\S".getBytes();
    private static final byte[] MODE_BEATBOX_COMMAND = "\\B".getBytes();

    /**
     * Para gestionar el contexto y los diálogos
     */
    private static Context context;
    ProgressDialog progressDialog;

    /**
     * Constructor privado para singleton. Inicializa ciertos elementos
     *
     * @param ctx
     */
    public BluetoothMaster(Context ctx) {
        context = ctx;
        bluetooth = BluetoothAdapter.getDefaultAdapter();
        listAdapter = new DeviceListAdapter(context, deviceList);
    }

    public BluetoothMaster getInstanceWithContext(Context ctx) {
        context = ctx;
        return this;
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

        // Resetear la lista
        deviceList.clear();

        // Setear this como receptor de los intentos de descubrimiento y dispositivo encontrado
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
        // for (Object bt : bluetooth.getBondedDevices().toArray()) { deviceList.add((BluetoothDevice) bt); }

        // Mostrar un diálogo para seleccionar el dispositivo
        AlertDialog bluetoothDialog = new AlertDialog.Builder(context)
                .setTitle("Elige un dispositivo")
                .setAdapter(listAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        device = (BluetoothDevice) listAdapter.getItem(which);
                        ParcelUuid uuids[] = device.getUuids();

                        if (uuids != null)
                            // Mostrar todos los UUIDs de los servicios ofrecidos por el dispositivo encontrado
                            for (ParcelUuid uuid : uuids) {
                                Log.d("ledparty", "UUID: " + uuid.toString());
                            }

                        Toast.makeText(context, "Conectando a dispositivo...", Toast.LENGTH_LONG).show();

                        // UUID por defecto: 00001101-0000-1000-8000-00805f9b34fb
                        if (connect()) {
                            // Si hay conexión, iniciar la actividad de los modos de transmisión
                            Intent modesIntent = new Intent(context, ModesActivity.class);
                            context.startActivity(modesIntent);
                        } else {
                            Toast.makeText(context, "Conexión rechazada!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    /**
     * Conexión por defecto al UUID del BluetoothCar
     *
     * @return
     */
    public boolean connect() {
        return connect(DEFAULT_UUID);
    }

    /**
     * Conectarse a un servicio, dado su UUID en formato String
     *
     * @return
     */
    public boolean connect(final String uuid) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                //Crear una conexión a Socket: se necesita el UUID del servicio registrado
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

                    return true;
                } catch (Exception e) {
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
     * Handler para reaccionar a la activación del Bluetooth (y otros Intents)
     *
     * @param context el contexto en el cual se ha procesado el Intent
     * @param intent  el Intent que vamos a procesar
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) { //Bluetooth permission request window

            Log.d("ledparty", "EXTRA_STATE: " + intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR));

            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) == BluetoothAdapter.STATE_ON) {
                Toast.makeText(context, "Bluetooth Activado!", Toast.LENGTH_SHORT).show();

                // Empezar a buscar dispositivos
                startDiscovery();

            } else {
                Toast.makeText(context, "Bluetooth NO Activado!", Toast.LENGTH_SHORT).show();
            }

        } else if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {

            // Extraer el dispositivo y añadir a la lista
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
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
        try {
            context.unregisterReceiver(this);
            if (socket != null)
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        } catch (IllegalArgumentException iae) {
            Log.d("ledparty", "todo cerrado!");
        }
    }

    /**
     * Mandar el código de cambio de modo por el socket Bluetooth
     *
     * @param mode
     */
    public void setMode(int mode) {
        try {
            outputStream = socket.getOutputStream();
            switch (mode) {
                case ModeFragment.MODE_TEXT:
                    outputStream.write(MODE_TEXT_COMMAND);
                    break;
                case ModeFragment.MODE_SPECTRAL:
                    outputStream.write(MODE_SPECTRAL_COMMAND);
                    break;
                case ModeFragment.MODE_BEATBOX:
                    outputStream.write(MODE_BEATBOX_COMMAND);
                    break;
            }
        } catch (IOException e) {
            Log.e("ledparty", "No se puede cambiar el modo");
        } catch (NullPointerException npe) {
            if (socket == null)
                Log.e("ledparty", "Ya no hay socket...");
            else if (outputStream == null)
                Log.e("ledparty", "Ya no hay outputStream...");

        }
    }

    /**
     * Manda una cadena de texto por el canal RF de Bluetooth
     *
     * @param message
     */
    public void sendMessage(String message) {
        try {
            outputStream = socket.getOutputStream();
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            //Log.e("ledparty", "No pudo ser");
        } catch (NullPointerException npe) {
            ;
        }
    }
}
