package com.nullexcom.find;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MarkerInfo extends BottomSheetDialogFragment {

    public static MarkerInfo newInstance(Client client) {
        MarkerInfo markerInfo = new MarkerInfo();
        markerInfo.client = client;
        return markerInfo;
    }

    private TextView tvUsername;
    private TextView tvDeviceName;
    private TextView tvStatus;

    private Client client;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marker_info, container, false);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvDeviceName = view.findViewById(R.id.tvDeviceName);
        tvStatus = view.findViewById(R.id.tvStatus);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvUsername.setText(client.getName());
        tvDeviceName.setText(client.getDeviceName());
        if (client.isActive()) {
            tvStatus.setText("Đang hoạt động");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(client.getLastOnline());
            tvStatus.setText("Hoạt động gần nhất lúc: " + sdf.format(calendar.getTime()));
        }
    }
}
