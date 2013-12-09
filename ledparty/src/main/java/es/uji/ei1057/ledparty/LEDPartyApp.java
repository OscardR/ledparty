package es.uji.ei1057.ledparty;

import android.app.Application;
import android.content.Context;

/**
 * Created by oscar on 9/12/13.
 */
public class LEDPartyApp extends Application {
    public static BluetoothSingleton bluetoothSingleton;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothSingleton = BluetoothSingleton.getInstance(this);
    }

    public BluetoothSingleton getBluetoothSingleton(Context ctx) {
        return BluetoothSingleton.getInstance(ctx);
    }
}
