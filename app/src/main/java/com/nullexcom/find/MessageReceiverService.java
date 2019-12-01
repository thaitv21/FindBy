package com.nullexcom.find;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MessageReceiverService extends IntentService {
    public MessageReceiverService() {
        super("MessageReceiverService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Context context = getApplicationContext();
        String host = WifiActivity.IP;
        int port = 8888;
        int len;
        Socket socket = new Socket();
        byte buf[]  = new byte[1024];

        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            socket.bind(null);
//            Log.d(ClientSocket.TAG,"Trying to connect...");

            socket.connect((new InetSocketAddress(host, port)), 500);
//            Log.d(ClientSocket.TAG,"Connected...");


            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data will be retrieved by the server device.
             */
            OutputStream outputStream = socket.getOutputStream();
            //ContentResolver cr = context.getContentResolver();
            InputStream inputStream = null;
            inputStream = new ByteArrayInputStream("Hello".getBytes());

            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            //catch logic
            e.printStackTrace();
//            Log.d(ClientSocket.TAG,e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            //catch logic
            //activity.makeToast(ClientSocket.TAG + " " +e.toString());
//            Log.d(ClientSocket.TAG,e.toString());
        }

        /**
         * Clean up any open sockets when done
         * transferring or if an exception occurred.
         */
        finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //catch logic
                    }
                }
            }
        }
    }
}
