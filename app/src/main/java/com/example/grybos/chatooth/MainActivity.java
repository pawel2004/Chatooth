package com.example.grybos.chatooth;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    //zmienne
    private BluetoothAdapter bluetoothAdapter;
    private Intent bIntent;
    private boolean bluetoothState;

    //widgety
    private LinearLayout create;
    private LinearLayout join;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        create = findViewById(R.id.create);
        join = findViewById(R.id.join);

        checkIfBluetooth();

        if (!bluetoothState){

            startActivityForResult(bIntent, 100);

        }

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                intent.putExtra("key", 0);
                startActivity(intent);

            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, ChooseActivity.class));

            }
        });

    }

    private void checkIfBluetooth(){

        if (bluetoothAdapter != null){

            Log.d("xxx", "Bluetooth supported");

            bluetoothState = bluetoothAdapter.isEnabled();

            bIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        }
        else {

            Log.d("xxx", "Bluetooth not supported");

            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Twoje urządzenie nie obsługuje Bluetooth!");
            alert.setCancelable(false);
            alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    finish();

                }
            });
            alert.show();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK){

            bluetoothState = true;

        }

        if (requestCode == 200 && resultCode == RESULT_OK){



        }

    }
}
