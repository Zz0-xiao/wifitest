package com.xiao.wifitest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity implements
        OnClickListener {

    private EditText edtIP;
    private EditText edtPort;
    EditText edtSend;
    //接收区
    private EditText edtReceiver;

    private Button btnConn;
    private Button btnSend;
    private Button cleanR;

    private CheckBox checkBoxTimer;

    private String TAG = "MainActivity";

    InputStream in;
    PrintWriter printWriter = null;
   // BufferedReader reader;

    Socket mSocket = null;
    public boolean isConnected = false;

    private MyHandler myHandler;

    Thread receiverThread;

    CheckBoxListener listener;
    private Button onon0;
    private Button onona;
    private Button ononf;
    private Button ononp;


    private class MyReceiverRunnable implements Runnable {

        public void run() {

            while (true) {

                Log.i(TAG, "---->>client receive....");
                if (isConnected) {
                    if (mSocket != null && mSocket.isConnected()) {
                       // Utility utility = new Utility();
                        String result = new Utility().readFromInputStream(in);

                        try {
                            // String str = "";
                            //
                            // while ((str = reader.readLine()) != null) {
                            // Log.i(tag, "---->> read data:" + str);
                            // result += str;
                            // }
                            if (!result.equals("")) {

                                Message msg = new Message();
                                msg.what = 1;
                                Bundle data = new Bundle();
                                data.putString("msg", result);
                                msg.setData(data);
                                myHandler.sendMessage(msg);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "--->>读取失败!" + e.toString());
                        }
                    }
                }
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            receiverData(msg.what);
            if (msg.what == 1) {
                String result = msg.getData().get("msg").toString();
                edtReceiver.append(result);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        edtIP = (EditText) this.findViewById(R.id.id_edt_inputIP);
        edtPort = (EditText) this.findViewById(R.id.id_edt_inputport);
        edtSend = (EditText) this.findViewById(R.id.id_edt_sendArea);
        edtReceiver = (EditText) findViewById(R.id.id_edt_jieshou);

        checkBoxTimer = (CheckBox) this.findViewById(R.id.id_checkBox_timer);
        listener = new CheckBoxListener(this);
        checkBoxTimer.setOnCheckedChangeListener(listener);

        btnSend = (Button) findViewById(R.id.id_btn_send);
        btnSend.setOnClickListener(this);
        btnConn = (Button) findViewById(R.id.id_btn_connClose);
        btnConn.setOnClickListener(this);
        cleanR = (Button) findViewById(R.id.clean_Receiving);
        cleanR.setOnClickListener(this);

        onon0 = (Button) findViewById(R.id.ONON0);
        onon0.setOnClickListener(this);
        onona = (Button) findViewById(R.id.ONONA);
        onona.setOnClickListener(this);
        ononf = (Button) findViewById(R.id.ONONF);
        ononf.setOnClickListener(this);
        ononp = (Button) findViewById(R.id.ONONP);
        ononp.setOnClickListener(this);

        myHandler = new MyHandler();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 启动2个工作线程:发送、接收。
            case R.id.id_btn_connClose:
                connectThread();
                break;
            case R.id.id_btn_send:
                sendData();
                break;
            case R.id.clean_Receiving:
                edtReceiver.setText("");
                break;
            case R.id.ONON0:
                printWriter.print("ONON0");
                printWriter.flush();
                break;
            case R.id.ONONA:
                printWriter.print("ONONA");
                printWriter.flush();
                break;
            case R.id.ONONF:
                printWriter.print("ONONF");
                printWriter.flush();
                break;
            case R.id.ONONP:
                printWriter.print("ONONP");
                printWriter.flush();
                break;
        }
    }

    /**
     * 当连接到服务器时,可以触发接收事件.
     */
    private void receiverData(int flag) {
        if (flag == 2) {
            // mTask = new ReceiverTask();
            receiverThread = new Thread(new MyReceiverRunnable());
            receiverThread.start();

            Log.i(TAG, "--->>socket 连接成功!");
            Toast.makeText(MainActivity.this, "连接成功!", Toast.LENGTH_SHORT).show();
            btnConn.setText("断开");

            isConnected = true;
            // mTask.execute(null);
        }
    }

    /**
     * 发送数据线程.
     */
    private void sendData() {
        // sendThread.start();
        try {
            String context = edtSend.getText().toString();

            if (printWriter == null || context == null) {

                if (printWriter == null) {
                    Toast.makeText(MainActivity.this, "连接失败!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (context == null) {
                    Toast.makeText(MainActivity.this, "连接失败!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            printWriter.print(context);
            printWriter.flush();
            Log.i(TAG, "--->> client send data!");
        } catch (Exception e) {
            Log.e(TAG, "--->> send failure!" + e.toString());
        }
    }

    /**
     * 启动连接线程.
     */
    private void connectThread() {
        if (!isConnected) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    Looper.prepare();
                    Log.i(TAG, "---->> 连接/关闭服务器!");

                    connectServer(edtIP.getText().toString(), edtPort.getText()
                            .toString());
                }
            }).start();
        } else {
            try {
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                    Log.i(TAG, "--->>取消server.");
                    // receiverThread.interrupt();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            btnConn.setText("连接");
            isConnected = false;
        }
    }

    // 连接服务器.(网络调试助手的服务器端编码方式:gb2312)
    private void connectServer(String ip, String port) {
        try {
            Log.e(TAG, "--->>开始连接服务器!" + ip + "," + port);

            mSocket = new Socket(ip, Integer.parseInt(port));
            //Log.e(TAG, "--->>结束连接服务器!");

            OutputStream outputStream = mSocket.getOutputStream();

            printWriter = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(outputStream,
                            Charset.forName("gb2312"))));
            listener.setOutStream(printWriter);
            // reader = new BufferedReader(new InputStreamReader(
            // mSocket.getInputStream()));

            in = mSocket.getInputStream();
            //当连接到服务器时,可以触发接收事件.
            myHandler.sendEmptyMessage(2);

        } catch (Exception e) {
            isConnected = false;
            //Toast.makeText(MainActivity.this, "连接失败！", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "连接失败！:" + e.toString());
        }
    }
}
