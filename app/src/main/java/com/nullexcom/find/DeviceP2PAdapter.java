package com.nullexcom.find;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceP2PAdapter extends RecyclerView.Adapter<DeviceP2PAdapter.DeviceViewHolder> {

    private Context mContext;
    private List<WifiP2pDevice> devices;
    private LayoutInflater inflater;
    private OnItemClickListener onItemClickListener;

    public DeviceP2PAdapter(Context mContext, List<WifiP2pDevice> devices) {
        this.mContext = mContext;
        this.devices = devices;
        this.inflater = LayoutInflater.from(mContext);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_device_p2p, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        WifiP2pDevice device = devices.get(position);
        holder.tvDeviceName.setText(device.deviceName);
        holder.tvDeviceAddress.setText(device.deviceAddress);
        String status = "";
        switch (device.status) {
            case 0: status = "CONNECTED";
            break;
            case 1: status = "INVITED";
            break;
            case 2: status = "FAILED";
            break;
            case 3: status = "AVAILABLE";
            break;
            case 4: status = "UNAVAILABLE";
        }
        holder.tvStatus.setText(status);
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvDeviceName)
        TextView tvDeviceName;

        @BindView(R.id.tvDeviceAddress)
        TextView tvDeviceAddress;

        @BindView(R.id.tvStatus)
        TextView tvStatus;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
