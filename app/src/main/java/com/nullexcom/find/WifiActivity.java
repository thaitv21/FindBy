package com.nullexcom.find;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WifiActivity extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener {

    public static String IP = null;
    @BindView(R.id.rvDevices)
    RecyclerView rvDevices;

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;

    IntentFilter intentFilter;

    private LoadingDialog loadingDialog;


    private DeviceP2PAdapter adapter;
    private List<WifiP2pDevice> devices = new ArrayList<>();

    private ServerClass serverClass;
    private ClientClass clientClass;
    private SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        ButterKnife.bind(this);
        loadingDialog = new LoadingDialog(this);
        adapter = new DeviceP2PAdapter(this, devices);
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        rvDevices.setAdapter(adapter);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, peers -> {
            loadingDialog.cancel();
            devices.clear();
            devices.addAll(peers.getDeviceList());
            adapter.notifyDataSetChanged();
        });

        adapter.setOnItemClickListener(i -> {
//            WifiP2pConfig config = new WifiP2pConfig();
//            WifiP2pDevice device = devices.get(i);
//            config.deviceAddress = device.deviceAddress;
//            config.wps.setup = WpsInfo.PBC;
//            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
//                @Override
//                public void onSuccess() {
//                    manager.requestConnectionInfo(channel, WifiActivity.this);
//                }
//
//                @Override
//                public void onFailure(int reason) {
//
//                }
//            });
        });

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @OnClick(R.id.tvDiscover)
    void discover() {
        loadingDialog.show();
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reasonCode) {
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (info.groupFormed && info.isGroupOwner) {
            serverClass = new ServerClass();
            serverClass.start();
        } else {
            clientClass = new ClientClass(info.groupOwnerAddress);
            clientClass.start();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                byte[] bytes = (byte[]) msg.obj;
                String m = new String(bytes, 0, msg.arg1);
                Toast.makeText(WifiActivity.this, m, Toast.LENGTH_SHORT).show();
            }
        }
    };

    class SendReceive extends Thread {

        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket socket) {
            this.socket = socket;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (socket != null) {
                try {
                    bytes = inputStream.read();
                    if (bytes > 0) {
                        handler.obtainMessage(1, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    class ClientClass extends Thread {

        Socket socket;
        String hostAdd;

        ClientClass(InetAddress inetAddress) {
            this.hostAdd = inetAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888), 30000);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
