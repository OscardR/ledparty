package es.uji.ei1057.ledparty;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by oscar on 29/10/13.
 */
public class DeviceListAdapter extends BaseAdapter {

    private List<BluetoothDevice> listData;

    private LayoutInflater layoutInflater;

    public DeviceListAdapter(Context context, List<BluetoothDevice> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.device_list_item, null);
            holder = new ViewHolder();
            holder.deviceName = (TextView) convertView.findViewById(R.id.txtName);
            holder.deviceMAC = (TextView) convertView.findViewById(R.id.txtMAC);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.deviceName.setText(listData.get(position).getName());
        holder.deviceMAC.setText(listData.get(position).getAddress());

        return convertView;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceMAC;
    }
}