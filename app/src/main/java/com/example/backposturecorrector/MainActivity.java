package com.example.backposturecorrector;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.backposturecorrector.thread.CreateConnectThread;

import static com.example.backposturecorrector.thread.ConnectedThread.MESSAGE_READ;
import static com.example.backposturecorrector.thread.CreateConnectThread.CONNECTING_STATUS;

public class MainActivity extends AppCompatActivity {

    private String deviceName;
    private String deviceAddress;
    public static Handler handler;
    public static CreateConnectThread createConnectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Initialization
        final Button buttonConnectDisconnect = findViewById(R.id.buttonConnectDisconnect);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle(" ");
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        final TextView textViewInfo = findViewById(R.id.textViewInfo);
        final Button buttonToggle = findViewById(R.id.buttonToggle);
        buttonToggle.setEnabled(false);
        final ImageView imageView = findViewById(R.id.imageView);
        imageView.setBackgroundColor(getResources().getColor(R.color.colorOff));

        /*
        Second most important piece of Code. GUI Handler
         */
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        connectionStatus(msg);
                        break;

                    case MESSAGE_READ:
                        messageRead(msg);
                        break;
                }
            }

            private void connectionStatus(Message msg) {
                switch (msg.arg1) {
                    case 1:
                        toolbar.setSubtitle("Connected to " + deviceName);
                        progressBar.setVisibility(View.GONE);
                        buttonConnectDisconnect.setText("Disconnect");
                        buttonConnectDisconnect.setEnabled(true);
                        buttonToggle.setEnabled(true);
                        break;
                    case -1:
                        toolbar.setSubtitle("Device fails to connect");
                        progressBar.setVisibility(View.GONE);
                        buttonConnectDisconnect.setEnabled(true);
                        break;
                }
            }

            private void messageRead(Message msg) {
                String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                switch (arduinoMsg.toLowerCase()) {
                    case "led is turned on":
                        imageView.setBackgroundColor(getResources().getColor(R.color.colorOn));
                        textViewInfo.setText("Arduino Message : " + arduinoMsg);
                        break;
                    case "led is turned off":
                        imageView.setBackgroundColor(getResources().getColor(R.color.colorOff));
                        textViewInfo.setText("Arduino Message : " + arduinoMsg);
                        break;
                }
            }
        };

        // Select Bluetooth Device
        buttonConnectDisconnect.setOnClickListener(view -> {
            String btnState = buttonConnectDisconnect.getText().toString().toLowerCase();
            switch (btnState) {
                case "connect":
                    Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                    startActivity(intent);
                    buttonConnectDisconnect.setText("Disconnect");
                    break;
                case "disconnect":
                    createConnectThread.getConnectedThread().cancel();
                    buttonConnectDisconnect.setText("Connect");
                    toolbar.setSubtitle(" ");
                    break;
            }
        });

        // Button to ON/OFF LED on Arduino Board
        buttonToggle.setOnClickListener(view -> {
            String cmdText = null;
            String btnState = buttonToggle.getText().toString().toLowerCase();
            switch (btnState) {
                case "turn on":
                    buttonToggle.setText("Turn Off");
                    // Command to turn on LED on Arduino. Must match with the command in Arduino code
                    cmdText = "<turn on>";
                    break;
                case "turn off":
                    buttonToggle.setText("Turn On");
                    // Command to turn off LED on Arduino. Must match with the command in Arduino code
                    cmdText = "<turn off>";
                    break;
            }
            // Send command to Arduino board
            createConnectThread.getConnectedThread().write(cmdText);
        });

        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progree and connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnectDisconnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress, handler);
            createConnectThread.start();
        }


    }

    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null) {
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}