package com.example.pritishgupta.conbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class Connection extends AppCompatActivity {
    private Handler mHandler;
    private ConnectedThread mConnectedThread;

    private TextView mBluetoothStatus,mReadBuffer;
    private Button mScanBtn,mOffBtn,mListPairedDevicesBtn,mDiscoverBtn;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    private Button mLED1,mLED2;
    private BluetoothSocket mBTSocket = null;
    private  String mDevices="";

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        mBluetoothStatus=findViewById(R.id.textView);
        mReadBuffer=findViewById(R.id.textView2);
        mScanBtn = findViewById(R.id.scan);
        mOffBtn = findViewById(R.id.off);
        mDiscoverBtn = findViewById(R.id.discover);
        mListPairedDevicesBtn = findViewById(R.id.pairedBtn);
        mLED1=findViewById(R.id.mLED1);
        mLED2=findViewById(R.id.mLED2);

        mBTArrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mBTAdapter= BluetoothAdapter.getDefaultAdapter();

        mDevicesListView=findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter);
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        mHandler=new Handler()
        {
            public void handleMessage(Message msg)
            {
                if (msg.what== MESSAGE_READ)

                {
                    String readMessage=null;
                    try {
                        readMessage=new String((byte[]) msg.obj,"UTF-8");
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        e.printStackTrace();
                    }
                    mReadBuffer.setText(readMessage);
                }
                if (msg.what==CONNECTING_STATUS)
                {
                    if (msg.arg1==1)
                        mBluetoothStatus.setText("connected to device:"+(String)msg.obj);
                    else
                        mBluetoothStatus.setText("Connection Failed");
                }

            }
        };
        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
        else
        {
            mLED1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write("1");
                    else
                        mConnectedThread.write("0");
                }

            });
            mLED2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write("0");

                }

            });
            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                }
            });
            mOffBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    bluetoothOff(v);
                }
            });

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    listPairedDevices(v);
                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover(v);
                }
            });
        }
    }
    private void bluetoothOn(View view){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText("Enabled");
            }
            else
                mBluetoothStatus.setText("Disabled");
        }
    }
    private void bluetoothOff(View view){
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }
    private void discover(View view){


        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {


                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();

                //registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                IntentFilter filter = new IntentFilter();

                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                final BroadcastReceiver blReceiver=new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action=intent.getAction();
                        if (BluetoothDevice.ACTION_FOUND.equals(action))
                        {
                            BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                            mBTArrayAdapter.add(device.getName()+"\n"+device.getAddress());
                            mBTArrayAdapter.notifyDataSetChanged();
                            mDevicesListView.setAdapter(mBTArrayAdapter);
                            Toast.makeText(Connection.this,"new device",Toast.LENGTH_LONG).show();
                        }
                    }
                };


                registerReceiver(blReceiver, filter);



            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();


        // Don't forget to unregister the ACTION_FOUND receiver.

    }


    private void listPairedDevices(View view)
    {
        mPairedDevices=mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled())
        {
            for (BluetoothDevice device :mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();

        }
        else
        {
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
        }
    }
    private AdapterView.OnItemClickListener mDeviceClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(!mBTAdapter.isEnabled())
            {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }


            String info=((TextView) view).getText().toString();
            final String address=info.substring(info.length()-17);
            final String name=info.substring(0,info.length()-17);

            new Thread()
            {
                public void run()
                {
                    boolean fail=false;

                    BluetoothDevice device=mBTAdapter.getRemoteDevice(address);
                    try {
                        mBTSocket=createBluetoothSocket(device);
                    }
                    catch (IOException e)
                    {
                        fail=true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    try {
                        mBTSocket.connect();
                    }
                    catch (IOException e)
                    {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        }
                        catch (IOException e2)
                        {
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (fail==false)
                    {
                        mConnectedThread=new ConnectedThread(mHandler,mBTSocket);
                        mConnectedThread.start();


                    }
                }
            }.start();
        }
    };
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException

    {
        try {
            final Method m=device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord",UUID.class);
        }
        catch (Exception e)
        {

        }
        return device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private class ConnectedThread extends Thread {


        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final Handler mmHandler;

        public ConnectedThread(Handler handler, BluetoothSocket socket) {
            mmHandler = handler;
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        buffer = new byte[1024];
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mmHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();
                    }
                }
                catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent i=new Intent(Connection.this,MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(i);
        finish();
    }

    }

