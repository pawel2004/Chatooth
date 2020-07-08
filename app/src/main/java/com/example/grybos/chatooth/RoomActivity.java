package com.example.grybos.chatooth;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class RoomActivity extends AppCompatActivity {

    //zmienne
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private Communication communication;
    private ProgressDialog progressDialog;
    private MessageAdapter adapter;
    private ArrayList<com.example.grybos.chatooth.Message> messages = new ArrayList<>();
    private com.example.grybos.chatooth.Message message;

    //widgets
    private TextView txt1;
    private ListView listView;
    private ImageView send;
    private EditText et1;
    private TextView status;
    private TextView start;

    //serwerowe
    private static final String APP_NAME = "Chatooth_chat";
    private static final UUID MY_UUID = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        getSupportActionBar().hide();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final Bundle bundle = getIntent().getExtras();

        txt1 = findViewById(R.id.txt1);
        status = findViewById(R.id.status);
        listView = findViewById(R.id.list_view1);
        send = findViewById(R.id.send);
        et1 = findViewById(R.id.edit_text);
        start = findViewById(R.id.start);

        txt1.setText("Nazwa: " + bluetoothAdapter.getName());

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivityForResult(intent, 200);

        progressDialog = new ProgressDialog(RoomActivity.this);
        progressDialog.setMessage("server listening...");
        progressDialog.setCancelable(false);

        if (bundle.getInt("key") == 1) {

            bluetoothDevice = (BluetoothDevice) bundle.get("device");

            Log.d("xxx", bluetoothDevice.getName());

            Client client = new Client(bluetoothDevice);
            client.start();

        }

        adapter = new MessageAdapter(
                RoomActivity.this,
                R.layout.message_list,
                messages
        );
        listView.setAdapter(adapter);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bundle.getInt("key") == 1){

                    AlertDialog.Builder alert = new AlertDialog.Builder(RoomActivity.this);
                    alert.setTitle("Jesteś Klientem!");
                    alert.setCancelable(false);
                    alert.setNeutralButton("OK", null);
                    alert.show();

                }
                else {

                    Server server = new Server();
                    server.start();
                    progressDialog.show();

                }

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (status.getText().equals("Połączono!!!")){

                    byte[] send_tmp = et1.getText().toString().getBytes();

                    communication.writeMessage(send_tmp);

                    messages.add(new com.example.grybos.chatooth.Message("Ja: ", et1.getText().toString()));

                    adapter.notifyDataSetChanged();

                }
                else {

                    AlertDialog.Builder alert = new AlertDialog.Builder(RoomActivity.this);
                    alert.setTitle("Nie masz połączenia!!!");
                    alert.setCancelable(false);
                    alert.setNeutralButton("OK", null);
                    alert.show();

                }

            }
        });


    }

    private class Server extends Thread{

        //server socket
        private BluetoothServerSocket bluetoothServerSocket;
        //prosty konstruktor
        public Server(){

            try {
                bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            super.run();

            //klient socket
            BluetoothSocket bluetoothClientSocket = null;

            Log.d("xxx", "server starting...");

            while (bluetoothClientSocket == null){

                Log.d("xxx", "server listening...");

                try {
                    bluetoothClientSocket = bluetoothServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bluetoothClientSocket != null){

                    Log.d("xxx", "server success connected!!!");

                    Message message = Message.obtain();
                    message.what = 0;
                    handler.sendMessage(message);

                    communication = new Communication(bluetoothClientSocket);
                    communication.start();

                    break;

                }

            }

        }
    }

    private class Client extends Thread{

        private BluetoothSocket bluetoothSocket;
        private BluetoothDevice bluetoothDevice;

        //klient w konstruktorze przekazuje dane urządzenia

        public Client(BluetoothDevice bluetoothDevice){

            this.bluetoothDevice = bluetoothDevice;

            try {
                bluetoothSocket = this.bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            super.run();

            //koniec szukania urządzeń

            bluetoothAdapter.cancelDiscovery();

            //łącz z serwerem
            try {
                bluetoothSocket.connect();
                Log.d("xxx", "client connected !!!");
                Message message = Message.obtain();
                message.what = 1;
                handler.sendMessage(message);
                communication = new Communication(bluetoothSocket);
                communication.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            switch (message.what){

                case 0:
                    status.setText("Połączono!!!");
                    progressDialog.dismiss();
                    break;

                case 1:
                    status.setText("Połączono!!!");
                    break;

                case 2:
                    byte[] readBuffer = (byte[]) message.obj;
                    String msg = new String(readBuffer, 0, message.arg1);
                    messages.add(new com.example.grybos.chatooth.Message("Rozmówca: ", msg));
                    adapter.notifyDataSetChanged();
                    break;
            }

            return false;
        }
    });

    private class Communication extends Thread{

        private BluetoothSocket bluetoothSocket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public Communication(BluetoothSocket bluetoothSocket){

            this.bluetoothSocket = bluetoothSocket;
            try {
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void writeMessage(byte[] data){

            try {
                outputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            super.run();

            byte[] buffer = new byte[1024];
            int bytes = 0;

            while (true){

                try {
                    bytes = inputStream.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bytes > 0){

                    handler.obtainMessage(2, bytes, -1, buffer).sendToTarget();

                }

            }

        }
    }

}
