package com.example.lukasz.bluetoothservo;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class ServoControl extends AppCompatActivity
{
    boolean isReceivingBytes;
    long startTime;
    int counter;
    Handler myHandler = new Handler();
    ImageView image;
    Handler handler;
    TextView lmn;
    Button btnOn, btnOff, btnDis, btnStop;
    SeekBar servoAngle;
    String address = null;
    String readMessage;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    TextView tv1, tv2;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servo_control);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);

        btnOn = (Button) findViewById(R.id.button2);
        btnOff = (Button) findViewById(R.id.button3);
        btnDis = (Button) findViewById(R.id.button4);
        btnStop = (Button) findViewById(R.id.button5);
        servoAngle = (SeekBar) findViewById(R.id.seekBar);
        lmn = (TextView) findViewById(R.id.textView2);
        tv1 = (TextView) findViewById(R.id.textView);
        tv2 = (TextView) findViewById(R.id.textView3);
        image = (ImageView)findViewById(R.id.imageView);

        isReceivingBytes = true;

        receiveData();

        startTime = System.currentTimeMillis();

        servoAngle.setMax(180);

        new ConnectBT().execute();

        btnOn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AngleMax();      //method to angle to 180
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AngleZero();   //method to set angle to 0
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

        servoAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (fromUser == true)
                {

                    lmn.setText(String.valueOf(progress));
                    try
                    {
                        btSocket.getOutputStream().write(String.valueOf(progress).getBytes());
                    } catch (IOException e)
                    {

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

    }
    public void onClickStop(View view)
    {
        if(isReceivingBytes)
        {
            btnStop.setText("START RECEIVING BYTES");
            isReceivingBytes = false;
        }
        else
        {
            btnStop.setText("STOP RECEIVING BYTES");
            isReceivingBytes = true;
            receiveData();
        }
    }


    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void Disconnect()
    {
        if (isReceivingBytes)
        {
            msg("Stop receiving bytes from Arduino first!");
        }
        else
        {
            if (btSocket != null) //If the btSocket is busy
            {
                try
                {
                    btSocket.close(); //close connection
                } catch (IOException e)
                {
                    msg("Error");
                }
            }
            finish(); //return to the first layout
        }
    }

    private void AngleZero()
    {
        if (btSocket != null)
        {
            try
            {
                btSocket.getOutputStream().write("ZERO".toString().getBytes());
                servoAngle.setProgress(0);
            } catch (IOException e)
            {
                msg("Error");
            }
        }
    }


    private void AngleMax()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("MAX".toString().getBytes());
                servoAngle.setProgress(180);
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ServoControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
                isReceivingBytes = true;
            }
            progress.dismiss();
        }
    }

    public void receiveData()
    {
        handler = new Handler()
        {
            public void handleMessage(Message message)
            {
                super.handleMessage(message);
                myHandler.post(new Runnable() {
                    @Override
                    public void run()
                    {
                        if (Objects.equals(readMessage, "9"))
                        {
                            tv2.setText("BANGLA!");
                            image.setImageResource(R.drawable.led_on);
                        } else
                        {
                            tv2.setText("NIE BANGLA!");
                            image.setImageResource(R.drawable.led_off);
                        }

                    }
                });

            }
        };

        new Thread(new Runnable()
        {
            public void run()
            {
                while(isReceivingBytes)
                {
                    counter = (int) (System.currentTimeMillis()-startTime);
                    try
                    {
                        int bytesAvailable = btSocket.getInputStream().available();
                        byte[] packetBytes = new byte[bytesAvailable];
                        if (bytesAvailable > 0)
                        {
                            btSocket.getInputStream().read(packetBytes);
                            //int readMessageInt = btSocket.getInputStream().read(packetBytes);
                            //readMessage = new String(packetBytes, 0, readMessageInt);
                            readMessage = new String(packetBytes, 0, 1);
                            Message messageReceived = new Message();
                            messageReceived.obj = readMessage;
                            handler.handleMessage(messageReceived);
                            startTime = System.currentTimeMillis();
                        }
                        else if(counter >= 50) //reset of the image
                        {
                            readMessage = "0";
                            Message messageReceived = new Message();
                            messageReceived.obj = readMessage;
                            handler.handleMessage(messageReceived);
                        }

                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
