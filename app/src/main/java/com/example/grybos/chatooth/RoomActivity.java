package com.example.grybos.chatooth;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RoomActivity extends AppCompatActivity {

    //zmienne
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    //widgets
    private TextView txt1;
    private ListView listView;
    private ImageView send;
    private EditText et1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        getSupportActionBar().hide();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Bundle bundle = getIntent().getExtras();

        txt1 = findViewById(R.id.txt1);
        listView = findViewById(R.id.list_view1);
        send = findViewById(R.id.send);
        et1 = findViewById(R.id.edit_text);

        txt1.setText("Nazwa: " + bluetoothAdapter.getName());

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivityForResult(intent, 200);

        if (bundle.getInt("key") == 1) {

            bluetoothDevice = (BluetoothDevice) bundle.get("device");

            Log.d("xxx", bluetoothDevice.getName());

        }

    }
}
