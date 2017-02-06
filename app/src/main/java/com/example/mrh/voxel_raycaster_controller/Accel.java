package com.example.mrh.voxel_raycaster_controller;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class Accel extends AppCompatActivity {

    public float getX_dat() { return x_dat; }

    public float getY_dat() {
        return y_dat;
    }

    public float getZ_dat() {
        return z_dat;
    }

    // x y and z accelerometer data
    private float x_dat;
    private float y_dat;
    private float z_dat;

    // Cycling list of x y z datas to do some sort of smoothing
    private List<Float> accel_smoother_x;
    private List<Float> accel_smoother_y;
    private List<Float> accel_smoother_z;

    // The servers port, address and current status
    private String server_port = "5000";
    private String server_addr = "192.168.1.102";
    private String server_stat = "Disconnected";


    private SensorManager sensorManager;
    private Sensor sensor;

    // Functions for averaging the list of accel values
    private float average_x(){
        float sum = 0;
        for(int i = 0; i < accel_smoother_x.size(); i++)
            sum += accel_smoother_x.get(i);
        return sum / accel_smoother_x.size();
    }
    private float average_y(){
        float sum = 0;
        for(int i = 0; i < accel_smoother_y.size(); i++)
            sum += accel_smoother_y.get(i);
        return sum / accel_smoother_y.size();
    }
    private float average_z(){
        float sum = 0;
        for(int i = 0; i < accel_smoother_z.size(); i++)
            sum += accel_smoother_z.get(i);
        return sum / accel_smoother_z.size();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);

        accel_smoother_x = new LinkedList<Float>(Arrays.asList(0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        accel_smoother_y = new LinkedList<Float>(Arrays.asList(0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        accel_smoother_z = new LinkedList<Float>(Arrays.asList(0.0f, 0.0f, 0.0f, 0.0f, 0.0f));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_ip) {
            getUserInput("The servers ip", "", R.id.server_ip_text_view);
            TextView t = (TextView)findViewById(R.id.server_ip_text_view);
            server_addr = new String(t.getText().toString());

        } else if (id == R.id.action_port) {
            getUserInput("The servers port", "", R.id.server_port_text_view);
            TextView t = (TextView)findViewById(R.id.server_port_text_view);
            server_port = new String(t.getText().toString());

        } else if (id == R.id.action_connect) {
            Thread cThread = new Thread(new ClientThread());
            cThread.start();

        } else if (id == R.id.action_disconnect) {
            connected = false;
        }

        return super.onOptionsItemSelected(item);
    }



    private SensorEventListener accelerationListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }
        @Override
        public void onSensorChanged(SensorEvent event) {

            // Set the values
            accel_smoother_x.remove(0);
            accel_smoother_x.add(event.values[0]);

            accel_smoother_y.remove(0);
            accel_smoother_y.add(event.values[1]);

            accel_smoother_z.remove(0);
            accel_smoother_z.add(event.values[2]);

            x_dat = event.values[0];
            y_dat= event.values[1];
            z_dat = event.values[2];

            // Refresh the display of the values
            refreshValues();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(accelerationListener, sensor,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(accelerationListener);
        super.onStop();
    }

    void refreshValues(){

        // Draw the dot
        MyView c = (MyView) findViewById(R.id.canvas);
        c.accel_x = -average_x();
        c.accel_y = average_y();
        c.accel_z = average_z();

        // Update the text
        TextView t = (TextView)findViewById(R.id.x_data_text_view);
        t.setText(String.valueOf(x_dat));

        t = (TextView)findViewById(R.id.y_data_text_view);
        t.setText(String.valueOf(y_dat));

        t = (TextView)findViewById(R.id.z_data_text_view);
        t.setText(String.valueOf(z_dat));

        t = (TextView)findViewById(R.id.server_status_text_view);
        t.setText(server_stat);

        t = (TextView)findViewById(R.id.server_port_text_view);
        server_port = new String(t.getText().toString());

        t = (TextView)findViewById(R.id.server_ip_text_view);
        server_addr = new String(t.getText().toString());
    }


    public int PORT = 15000;
    private boolean connected = false;
    PrintWriter out;

    public class ClientThread implements Runnable {
        Socket socket;
        public void run() {

            try {

                PORT = Integer.parseInt(server_port);
                InetAddress serverAddr = InetAddress.getByName(server_addr);
                socket = new Socket(serverAddr, PORT);

                connected = true;
                server_stat = "Connected";

                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                while (connected) {

                    OutputStream o = socket.getOutputStream();
                    ByteBuffer b = ByteBuffer.allocate(12);
                    b.order(ByteOrder.LITTLE_ENDIAN);

                    b.putFloat(x_dat);
                    b.putFloat(y_dat);
                    b.putFloat(z_dat);

                    o.write(b.array());
                    o.flush();

                    Thread.sleep(16);
                }
            }
            catch (Exception e) {
                server_stat = "Error Connecting";
            }
            finally{
                try{
                    socket.close();
                    server_stat = "Closed";
                }
                catch(Exception a){

                }
            }
        }
    };

    // Helper function that gets user input. StackOverflow
    private void getUserInput(String title, String message, final int rid){

        final EditText input = new EditText(this);
        input.setHint(message);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setView(input)
                .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        TextView t = (TextView)findViewById(rid);
                        t.setText(input.getText().toString());
                        refreshValues();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }


}
