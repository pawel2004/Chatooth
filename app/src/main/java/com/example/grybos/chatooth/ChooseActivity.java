package com.example.grybos.chatooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Set;

public class ChooseActivity extends AppCompatActivity {

    //zmienne
    private BluetoothAdapter bluetoothAdapter;
    private LocationManager locationManager;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private IntentFilter startFilter;
    private IntentFilter foundFilter;
    private IntentFilter finishFilter;
    private IntentFilter scanFilter;
    private Receivers receivers;
    private ListViewAdapter adapter;

    //widgety
    private ListView listView;
    private TextView search;
    private TextView show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        getSupportActionBar().hide();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        listView = findViewById(R.id.list_view2);
        search = findViewById(R.id.search);
        show = findViewById(R.id.show);

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivityForResult(intent, 200);

        adapter = new ListViewAdapter(
                ChooseActivity.this,
                R.layout.device_info,
                devices
        );
        listView.setAdapter(adapter);

        receivers = new Receivers(ChooseActivity.this, adapter, devices);
        startFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        finishFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        scanFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(ChooseActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){

                    Log.d("xxx", "Nie ma uprawnienia!");

                    ActivityCompat.requestPermissions(
                            ChooseActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            999);

                }else {

                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

                        Log.d("location", "Nie ma GPS!");

                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }else {

                        devices.clear();
                        bluetoothAdapter.startDiscovery();

                    }

                }

            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Set<BluetoothDevice> bluetoothDeviceSet = bluetoothAdapter.getBondedDevices();

                devices.clear();

                for (BluetoothDevice device : bluetoothDeviceSet){

                    devices.add(device);

                }

                adapter.notifyDataSetChanged();

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(ChooseActivity.this, RoomActivity.class);
                intent.putExtra("device", devices.get(position));
                intent.putExtra("key", 1);
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(receivers.startReceiver, startFilter);
        registerReceiver(receivers.findReceiver, foundFilter);
        registerReceiver(receivers.stopReceiver, finishFilter);
        registerReceiver(receivers.scanReceiver, scanFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receivers.startReceiver);
        unregisterReceiver(receivers.findReceiver);
        unregisterReceiver(receivers.stopReceiver);
        unregisterReceiver(receivers.scanReceiver);

    }
}
