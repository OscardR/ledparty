package es.uji.ei1057.ledparty;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by oscar on 9/12/13.
 */
public class LEDPartyApp extends Application {
    private static BluetoothMaster bluetoothMaster;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothMaster = new BluetoothMaster(this);
        Log.d("ledparty", "LEDPartyApp onCreate");
    }

    public BluetoothMaster getBluetoothMaster(Context ctx) {
        Log.d("ledparty", "LEDPartyApp getBluetoothMaster(ctx)");
        return bluetoothMaster.getInstanceWithContext(ctx);
    }

    public BluetoothMaster getBluetoothMaster() {
        Log.d("ledparty", "LEDPartyApp getBluetoothMaster()");
        return bluetoothMaster;
    }
}
