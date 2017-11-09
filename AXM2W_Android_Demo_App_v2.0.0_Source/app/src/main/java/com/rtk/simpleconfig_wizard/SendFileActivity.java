package com.rtk.simpleconfig_wizard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SendFileActivity extends Activity {

    private String              strFilePath;
    private String              destIp;
    private InetAddress         inetIp;
    private AlertDialog.Builder alertDialog;
    private String              strName;
    private String              strPort;
    private int                 nDevPort;
    private int                 deviceState = -1;
    private String              strBaud;
    private ProgressBar         pbSendFile;
    private int                 fileSize;
    private String              strVer;
    private String              strFwFilePath;
    public static Socket        socketTCP = null;
    int                         receivedFileBlock;
    int                         transmitedFileBlock;
    int                         fileTransferState;
    Handler                     globalHandler;
    String                      sewingNumber;
    private ProgressBar         pbFwUpgrade;
    private int                 fwFileSize;

    // Donald
    private int nConfigPktLength = 0;

    private int nServerClientMode;
    private int nServerListeningPort;
    private String strClientDestinationHostIpName;
    private int nClientDestinationPort;
    private String strStaticIpAddress;
    private String strStaticDefaultGateway;
    private String strStaticSubnetMask;
    private String strStaticDnsServer;

    private int nUartDataBaudRate;
    private int nUartDataBits;
    private int nUartDataParity;
    private int nUartStopBits;
    private int nUartFlowControl;

    private int nWifiNetworkMode;
    private int nWifiApChannel;
    private String strWifiSSID;
    private int nWifiSecurityMode;
    private int nWepKeyLength;
    private int nWepKeyIndexSelect;
    private String strWeb64KeyIndex0;
    private String strWeb64KeyIndex1;
    private String strWeb64KeyIndex2;
    private String strWeb64KeyIndex3;
    private String strWeb128KeyIndex0;
    private String strWeb128KeyIndex1;
    private String strWeb128KeyIndex2;
    private String strWeb128KeyIndex3;
    private String strAesTkipPassphrase;

    private String strDeviceName;
    private String strDeviceIpAddress;
    private String strWiFiMacAddressHex;
    private String strDhcpStatus;
    private String strProtocolType;
    private String strImageFileName;

    private int nTvUartRxDataEachLineMaxChars = 0;
    private int nTvUartRxDataCount = 0;

    private ArrayAdapter<String> aaWifiSecurityMode;
    private ArrayAdapter<String> aaWifiSecurityModeAp;

    private boolean bTcpSocketConnected = false;
    private boolean bUartSocketConnected = false;

    private byte[] setReqData = new byte[1024];
    private DatagramPacket setReqDp = new DatagramPacket(setReqData, setReqData.length);

    private static final int CHOOSE_FILE_REQUEST = 1;
    private static final int CHOOSE_FW_FILE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_send_file);
        setContentView(R.layout.activity_tab);

        final TabHost host = (TabHost) findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("UART");
        spec.setContent(R.id.uart);
        spec.setIndicator("UART");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Send File");
        spec.setContent(R.id.sendfile);
        spec.setIndicator("Send File");
        host.addTab(spec);

        //Tab 3
        spec = host.newTabSpec("Configuration");
        spec.setContent(R.id.deviceConfiguration);
        spec.setIndicator("Config");
        host.addTab(spec);

        //Tab 4
        spec = host.newTabSpec("FW Upgrade");
        spec.setContent(R.id.firmware);
        spec.setIndicator("FW Upgrade");
        host.addTab(spec);

        //Tab 5
        spec = host.newTabSpec("Status");
        spec.setContent(R.id.status);
        spec.setIndicator("Status");
        host.addTab(spec);

        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String arg0) {
                if (host.getCurrentTab() == 2) {
                    TextView tvConfigTabTop = (TextView) findViewById(R.id.tvConfigTabTop);
                    tvConfigTabTop.setFocusableInTouchMode(true);
                }

                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        // Serial To Network Settings
        Spinner spServerClientMode = (Spinner) findViewById(R.id.spServerClientMode);
        String[] itemsServerClientMode = new String[]{"Server", "Client"};

        ArrayAdapter<String> aaServerClientMode = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsServerClientMode);
        spServerClientMode.setAdapter(aaServerClientMode);

        spServerClientMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("spServerClientMode = ", (String) parent.getItemAtPosition(position));
                nServerClientMode = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        // Serial Port Settings
        Spinner spDataBuadRate = (Spinner) findViewById(R.id.spDataBuadRate);
        String[] itemsDataBuadRate = new String[]{"921600", "115200", "57600", "38400", "19200", "9600", "4800", "2400", "1200"};

        ArrayAdapter<String> aaDataBuadRate = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsDataBuadRate);
        spDataBuadRate.setAdapter(aaDataBuadRate);

        spDataBuadRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        Spinner spDataBits = (Spinner) findViewById(R.id.spDataBits);
        String[] itemsDataBits = new String[]{"7", "8"};

        ArrayAdapter<String> aaDataBits = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsDataBits);
        spDataBits.setAdapter(aaDataBits);

        spDataBits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        Spinner spDataParity = (Spinner) findViewById(R.id.spDataParity);
        String[] itemsDataParity = new String[]{"None", "Odd", "Even"};

        ArrayAdapter<String> aaDataParity = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsDataParity);
        spDataParity.setAdapter(aaDataParity);

        spDataParity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        Spinner spStopBits = (Spinner) findViewById(R.id.spStopBits);
        String[] itemsStopBits = new String[]{"1", "2"};

        ArrayAdapter<String> aaStopBits = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsStopBits);
        spStopBits.setAdapter(aaStopBits);

        spStopBits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        Spinner spFlowControl = (Spinner) findViewById(R.id.spFlowControl);
        String[] itemsFlowControl = new String[]{"None", "RTS+CTS"};

        ArrayAdapter<String> aaFlowControl = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsFlowControl);
        spFlowControl.setAdapter(aaFlowControl);

        spFlowControl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        // WiFi System Settings
        Spinner spWifiSecurityMode = (Spinner) findViewById(R.id.spWifiSecurityMode);
        String[] itemsWifiSecurityMode = new String[]{"Open", "WEP", "WPA/WPA2 Auto"};
        String[] itemsWifiSecurityModeAp = new String[]{"Open", "WPA/WPA2 Auto"};
        aaWifiSecurityMode = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsWifiSecurityMode);
        aaWifiSecurityModeAp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsWifiSecurityModeAp);

        spWifiSecurityMode.setAdapter(aaWifiSecurityMode);

        spWifiSecurityMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Log.v("spWifiSecurityMode = ", (String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        Spinner spWifiNetworkMode = (Spinner) findViewById(R.id.spWifiNetworkMode);
        String[] itemsWifiNetworkMode = new String[]{"Station", "AP"};

        ArrayAdapter<String> aaWifiNetworkMode = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsWifiNetworkMode);
        spWifiNetworkMode.setAdapter(aaWifiNetworkMode);

        spWifiNetworkMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spWifiApChannel = (Spinner) findViewById(R.id.spWifiApChannel);
                Spinner spWifiSecurityMode = (Spinner) findViewById(R.id.spWifiSecurityMode);

                if (position == 0) { // Station mode
                    spWifiSecurityMode.setAdapter(aaWifiSecurityMode);
                    spWifiSecurityMode.setSelection(nWifiSecurityMode);

                    spWifiApChannel.setSelection(10); // Fixed at channel 11 in Station mode
                    spWifiApChannel.setEnabled(false);
                } else { // AP mode
                    spWifiSecurityMode.setAdapter(aaWifiSecurityModeAp);
                    if (nWifiSecurityMode == 0) {
                        spWifiSecurityMode.setSelection(0);
                    }
                    else {
                        spWifiSecurityMode.setSelection(1);
                    }

                    spWifiApChannel.setSelection(nWifiApChannel); // Fixed at channel 11 in Station mode
                    spWifiApChannel.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        Spinner spWifiApChannel = (Spinner) findViewById(R.id.spWifiApChannel);
        String[] itemsWifiApChannel = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};

        ArrayAdapter<String> aaWifiApChannel = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsWifiApChannel);
        spWifiApChannel.setAdapter(aaWifiApChannel);

        spWifiApChannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        //EditText etWifiSSID = (EditText) findViewById(R.id.etWifiSSID);

        // WEP Encryption Key Settings
        Spinner spWepKeyLength = (Spinner) findViewById(R.id.spWepKeyLength);
        String[] itemsWepKeyLength = new String[]{"64 bits", "128 bits"};

        ArrayAdapter<String> aaWepKeyLength = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsWepKeyLength);
        spWepKeyLength.setAdapter(aaWepKeyLength);

        spWepKeyLength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                EditText etWepKeyIndex0 = (EditText) findViewById(R.id.etWepKeyIndex0);
                EditText etWepKeyIndex1 = (EditText) findViewById(R.id.etWepKeyIndex1);
                EditText etWepKeyIndex2 = (EditText) findViewById(R.id.etWepKeyIndex2);
                EditText etWepKeyIndex3 = (EditText) findViewById(R.id.etWepKeyIndex3);

                if (position == 0) {
                    etWepKeyIndex0.setText(strWeb64KeyIndex0);
                    etWepKeyIndex1.setText(strWeb64KeyIndex1);
                    etWepKeyIndex2.setText(strWeb64KeyIndex2);
                    etWepKeyIndex3.setText(strWeb64KeyIndex3);
                } else {
                    etWepKeyIndex0.setText(strWeb128KeyIndex0);
                    etWepKeyIndex1.setText(strWeb128KeyIndex1);
                    etWepKeyIndex2.setText(strWeb128KeyIndex2);
                    etWepKeyIndex3.setText(strWeb128KeyIndex3);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        Spinner spWepKeyIndexSelect = (Spinner) findViewById(R.id.spWepKeyIndexSelect);
        String[] itemsWepKeyIndexSelect = new String[]{"Key Index 0", "Key Index 1", "Key Index 2", "Key Index 3"};

        ArrayAdapter<String> aaWepKeyIndexSelect = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsWepKeyIndexSelect);
        spWepKeyIndexSelect.setAdapter(aaWepKeyIndexSelect);

        spWepKeyIndexSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        // WEP Encryption Key Settings
        //EditText etAesTkipPassphrase = (EditText) findViewById(R.id.etAesTkipPassphrase);

        alertDialog = new AlertDialog.Builder(this);

        Intent intent = getIntent();
        destIp = intent.getStringExtra("DEST_IP");
        nDevPort = intent.getIntExtra("DEST_PORT", 0);
        strPort = String.valueOf(nDevPort);

        try {
            inetIp = InetAddress.getByName(destIp);

            new DevSearchTask().execute();
        } catch (UnknownHostException e) {

        }

        TextView ipText = (TextView) findViewById(R.id.destIp);
        ipText.setText(destIp);

        Button sendFile = (Button) findViewById(R.id.sendFile);
        sendFile.setEnabled(false);

        Button upgradeFw = (Button) findViewById(R.id.upgradeFw);
        upgradeFw.setEnabled(false);

        Button fileChooser = (Button) findViewById(R.id.fileChooser);
        fileChooser.setEnabled(false);

        //Button btStartUartPortConnection = (Button) findViewById(R.id.btStartUartPortConnection);
        //btStartUartPortConnection.setFocusableInTouchMode(true);

        EditText etUartTxData = (EditText) findViewById(R.id.etUartTxData);
        etUartTxData.setEnabled(false);

        final Button btSendDataToUartPort = (Button) findViewById(R.id.btSendDataToUartPort);
        btSendDataToUartPort.setEnabled(false);

        Button btStartUartPortConnection = (Button) findViewById(R.id.btStartUartPortConnection);
        btStartUartPortConnection.setFocusableInTouchMode(true);
        btStartUartPortConnection.setFocusable(true);
        btStartUartPortConnection.requestFocus();

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        globalHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    // Below codes are only needed for a specific customer
                    //TextView textSewingNumber = (TextView) findViewById(R.id.numberOfSewing);
                    //textSewingNumber.setText(sewingNumber);
                } else if (msg.what == 9) {
                    Bundle uart_rx_data = msg.getData();
                    String data = uart_rx_data.getString("data");

                    TextView tvUartRxData = (TextView) findViewById(R.id.tvUartRxData);

                    if (nTvUartRxDataEachLineMaxChars == 0) {
                        for (int i = 0; i < 64; i++) {
                            tvUartRxData.append(" ");
                            String fullString = tvUartRxData.getText().toString();
                            int totalCharstoFit = tvUartRxData.getPaint().breakText(fullString, 0, fullString.length(),
                                    true, tvUartRxData.getWidth(), null);

                            if (nTvUartRxDataEachLineMaxChars == totalCharstoFit) {
                                nTvUartRxDataEachLineMaxChars -= 2;
                                tvUartRxData.setText("");
                                break;
                            }

                            nTvUartRxDataEachLineMaxChars = totalCharstoFit;
                        }
                    }

                    // Support display 12 lines of RX data
                    if (tvUartRxData.getLineCount() > 12) {
                        tvUartRxData.setText("");
                        nTvUartRxDataCount = 0;
                    }

                    for (int i = 0; i < data.length(); i++) {
                        tvUartRxData.append(data.substring(i, i + 1));
                        nTvUartRxDataCount++;

                        if (nTvUartRxDataCount % (nTvUartRxDataEachLineMaxChars) == 0) {
                            tvUartRxData.append("\n");
                        }
                    }
                }
            }
        };
    }

    public void startTcpConnection(View view) throws IOException {
        alertDialog = new AlertDialog.Builder(this);
        pbSendFile = (ProgressBar) findViewById(R.id.showSendFileProgress);
        pbFwUpgrade = (ProgressBar) findViewById(R.id.showUpgradeFwProgress);

        if (bTcpSocketConnected) {
            try {
                socketTCP.close();
                socketTCP = null;
                Toast.makeText(SendFileActivity.this, "TCP socket closed", Toast.LENGTH_LONG).show();

                Button btStartUartPortConnection = (Button) findViewById(R.id.btStartUartPortConnection);
                btStartUartPortConnection.setEnabled(true);

                Button fileChooser = (Button) findViewById(R.id.fileChooser);
                fileChooser.setEnabled(false);

                Button sendFile = (Button) findViewById(R.id.sendFile);
                sendFile.setEnabled(false);

                TextView showDeviceStateText = (TextView) findViewById(R.id.showDeviceState);
                showDeviceStateText.setText("TCP connection closed");

                final Button btStartTcpConnection = (Button) findViewById(R.id.btStartTcpConnection);
                btStartTcpConnection.setText("Start TCP Connection");

                bTcpSocketConnected = false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return;
        }

        if (nDevPort != 0) {
            Log.i("ASIX", "Do TCP connection");
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    TextView showDeviceStateText = (TextView) findViewById(R.id.showDeviceState);
                    if (msg.what == 1) {
                        showDeviceStateText.setText("TCP connection success");
                        Toast.makeText(SendFileActivity.this, "TCP socket connected.", Toast.LENGTH_LONG).show();

                        Button fileChooser = (Button) findViewById(R.id.fileChooser);
                        fileChooser.setEnabled(true);

                        Button btStartUartPortConnection = (Button) findViewById(R.id.btStartUartPortConnection);
                        btStartUartPortConnection.setEnabled(false);

                        final Button btStartTcpConnection = (Button) findViewById(R.id.btStartTcpConnection);
                        btStartTcpConnection.setText("Close TCP Connection");

                        bTcpSocketConnected = true;
                    } else if (msg.what == 2) {
                        showDeviceStateText.setText("TCP connection failed");
                        Toast.makeText(SendFileActivity.this, "Create TCP socket fail.", Toast.LENGTH_LONG).show();
                    } else if (msg.what == 3) {
                        Button chooseFile = (Button) findViewById(R.id.fileChooser);
                        chooseFile.setEnabled(false);
                        showDeviceStateText.setText("TCP connection failed");
                        Toast.makeText(SendFileActivity.this, "TCP connection already be occupied by another one.", Toast.LENGTH_LONG).show();
                    } else if (msg.what == 4) {
                        showDeviceStateText.setText("TCP connection failed");
                        Toast.makeText(SendFileActivity.this, "Device did not reply UDP management.", Toast.LENGTH_LONG).show();
                    }
                }
            };

            TcpConnectThread tcpConnectThread = new TcpConnectThread(handler, destIp, nDevPort);
            tcpConnectThread.start();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (socketTCP != null) {
                ConfirmExit();
            } else {
                SendFileActivity.this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void ConfirmExit() {
        AlertDialog.Builder ad = new AlertDialog.Builder(SendFileActivity.this);
        ad.setTitle("Exit");

        if (bUartSocketConnected) {
            ad.setMessage("Exit will close UART connection.\nPress YES to Exit.");
        }
        else {
            ad.setMessage("Exit will close TCP connection.\nPress YES to Exit.");
        }

        ad.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                try {
                        socketTCP.close();
                        socketTCP = null;

                        if (bUartSocketConnected) {
                            Toast.makeText(SendFileActivity.this, "UART connection closed", Toast.LENGTH_LONG).show();
                            bUartSocketConnected = false;
                        }
                        else {
                            Toast.makeText(SendFileActivity.this, "TCP socket closed", Toast.LENGTH_LONG).show();
                            bTcpSocketConnected = false;
                        }
                } catch (IOException e) {
                        // TODO Auto-generated catch block
                       e.printStackTrace();
                }
                SendFileActivity.this.finish();
            }
        });
        ad.setNegativeButton("No",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {

            }
        });
        ad.show();
    }

    class DevSearchTask extends AsyncTask<Void, Integer, String> {

        protected String doInBackground(Void... params) {
            return SearchAxmDev();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String result) {

            if (result.equals("DEV_FOUND")) {
                TextView nameText = (TextView) findViewById(R.id.showName);
                nameText.setText(strName);

                TextView portText = (TextView) findViewById(R.id.showPort);
                portText.setText(strPort);

                TextView baudText = (TextView) findViewById(R.id.showBaudrate);
                baudText.setText(strBaud);

                TextView verText = (TextView) findViewById(R.id.showFwVer);
                verText.setText(strVer);

                SetAppUiDeviceConfiguration();

            } else if (result.equals("DEV_NOT_FOUND")) {
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Devs not found.");
                alertDialog.show();
            }
            else if (result.equals("SOCKET_ERROR")) {
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Create socket error.");
                alertDialog.show();
            }
        }
    }

    private String SearchAxmDev() {
        DatagramSocket clientSocket = null;
        byte searchReq[] = {'A', 'S', 'I', 'X', 'X', 'I', 'S', 'A', 0x0E};
        byte statusReq[] = {'A', 'S', 'I', 'X', 'X', 'I', 'S', 'A', 0x10};
        ByteBuffer sendData = ByteBuffer.allocate(searchReq.length);
        ByteBuffer statusData = ByteBuffer.allocate(statusReq.length);
        byte[] rxData = new byte[1024];
        DatagramPacket dp = new DatagramPacket(rxData, rxData.length);
        boolean getSearchResult = false;
        boolean getStatusResult = false;

        sendData.put(searchReq);
        statusData.put(statusReq);

        Log.i("ASIX", "Start search");

        int retry = 0;
        try {
            clientSocket = new DatagramSocket(25120);
            DatagramPacket packet = new DatagramPacket(sendData.array(), sendData.array().length,
                    inetIp, 25122);

            DatagramPacket statusPacket = new DatagramPacket(statusData.array(), statusData.array().length,
                    inetIp, 25122);

            clientSocket.send(packet);
            clientSocket.send(statusPacket);

            Log.i("ASIX", "Send req");
            Log.i("ASIX", "wait rx data");

            while (retry < 10)
            {
                try{
                    clientSocket.setSoTimeout(500);
                    clientSocket.receive(dp);

                    if (dp.getData()[0] == 'A' &&
                            dp.getData()[1] == 'S' &&
                            dp.getData()[2] == 'I' &&
                            dp.getData()[3] == 'X' &&
                            dp.getData()[4] == 'X' &&
                            dp.getData()[5] == 'I' &&
                            dp.getData()[6] == 'S' &&
                            dp.getData()[7] == 'A' &&
                            dp.getData()[8] == 0x01){
                        // handle rx data
                        int value;
                        Byte b;
                        String strLog;

                        // Get device name
                        strName = "";
                        for(int i = 0; i < 16; i++)
                        {
                            if(dp.getData()[10 + i] > 0x20 &&
                                    dp.getData()[10 + i] < 0x7F){
                                strName += (char)dp.getData()[10 + i];
                            }
                            else{
                                break;
                            }
                        }

                        //strPort = "";
                        //value = dp.getData()[42];
                        //value <<= 8;
                        //value += (int)(dp.getData()[43]) & 0x000000FF;
                        //nDevPort = value;
                        //strPort = String.valueOf(value);

                        // Get baud rate
                        strBaud = "";
                        value = dp.getData()[103];
                        value <<= 8;
                        value += (int)(dp.getData()[102])& 0x000000FF;
                        value <<= 8;
                        value += (int)(dp.getData()[101])& 0x000000FF;
                        value <<= 8;
                        value += (int)(dp.getData()[100])& 0x000000FF;
                        strBaud = String.valueOf(value);

                        getSearchResult = true;

                        // Get device state
                        if (dp.getData()[104] == 0) {
                            deviceState = 0;
                        } else if (dp.getData()[104] == 1) {
                            deviceState = 1;
                        }

                        HandleConfigurationFromDeviceToApp(dp);

                    } else if (dp.getData()[0] == 'A' &&
                            dp.getData()[1] == 'S' &&
                            dp.getData()[2] == 'I' &&
                            dp.getData()[3] == 'X' &&
                            dp.getData()[4] == 'X' &&
                            dp.getData()[5] == 'I' &&
                            dp.getData()[6] == 'S' &&
                            dp.getData()[7] == 'A' &&
                            dp.getData()[8] == 0x11){

                        strVer = "";
                        for (int i = 0; i < 14; i++)
                        {
                            if(dp.getData()[10 + i] > 0x20 &&
                                    dp.getData()[10 + i] < 0x7F){
                                strVer += (char)dp.getData()[10 + i];
                            }
                            else{
                                break;
                            }
                        }

                        getStatusResult = true;
                    }

                    // Receive Rx, leave loop
                    if (getSearchResult && getStatusResult){
                        break;
                    }

                } catch (IOException e) {
                    retry +=1;
                    if (!getSearchResult) {
                        clientSocket.send(packet);
                        String str = String.format("Retry search Device : %d", retry);
                        Log.i("ASIX", str);
                    }
                    if (!getStatusResult) {
                        clientSocket.send(statusPacket);
                        String str = String.format("Retry monitor Device : %d", retry);
                        Log.i("ASIX", str);
                    }
                }

            }
        } catch (SocketException e) {
            return "SOCKET_ERROR";
        } catch (IOException e) {
            if (clientSocket != null) {
                clientSocket.close();
            }

            if (getSearchResult) {
                return "DEV_FOUND";
            } else {
                deviceState = -2;
                return "DEV_NOT_FOUND";
            }
        }

        if (clientSocket != null) {
            clientSocket.close();
        }

        if (getSearchResult) {
            return "DEV_FOUND";
        } else {
            deviceState = -2;
            return "DEV_NOT_FOUND";
        }
    }

    private void GetAppUiDeviceConfiguration() {
        int tempValue;
        boolean bIsApMode = false;

        // Serial To Network Settings
        Spinner spServerClientMode = (Spinner) findViewById(R.id.spServerClientMode);

        if (spServerClientMode.getSelectedItemPosition() == 0) {
            setReqDp.getData()[32] &= 0x7F; // Server mode: bit 8 = 0
        }
        else {
            setReqDp.getData()[32] |= 0x80; // Client Mode: bit 8 = 1
        }

        EditText etServerListeningPort = (EditText) findViewById(R.id.etServerListeningPort);
        tempValue = Integer.valueOf(etServerListeningPort.getText().toString());
        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 42, 2);

        EditText etClientDestinationHostIpName = (EditText) findViewById(R.id.etClientDestinationHostIpName);
        SetStringItemDataToDatagramPacket(setReqDp, etClientDestinationHostIpName.getText().toString(), 48, 36);

        EditText etClientDestinationPort = (EditText) findViewById(R.id.etClientDestinationPort);
        tempValue = Integer.valueOf(etClientDestinationPort.getText().toString());
        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 46, 2);

        // Static IP Settings
        EditText etStaticIpAddress = (EditText) findViewById(R.id.etStaticIpAddress);
        SetIpAddrItemToDatagramPacket(setReqDp, etStaticIpAddress.getText().toString(), 38);

        EditText etStaticDefaultGateway = (EditText) findViewById(R.id.etStaticDefaultGateway);
        SetIpAddrItemToDatagramPacket(setReqDp, etStaticDefaultGateway.getText().toString(), 88);

        EditText etStaticSubnetMask = (EditText) findViewById(R.id.etStaticSubnetMask);
        SetIpAddrItemToDatagramPacket(setReqDp, etStaticSubnetMask.getText().toString(), 84);

        EditText etStaticDnsServer = (EditText) findViewById(R.id.etStaticDnsServer);
        SetIpAddrItemToDatagramPacket(setReqDp, etStaticDnsServer.getText().toString(), 92);

        // Serial Port Settings
        Spinner spDataBuadRate = (Spinner) findViewById(R.id.spDataBuadRate);
        tempValue = Integer.valueOf(spDataBuadRate.getSelectedItem().toString());
        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 100, 4);

        Spinner spDataBits = (Spinner) findViewById(R.id.spDataBits);
        tempValue = Integer.valueOf(spDataBits.getSelectedItem().toString());
        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 96, 1);

        Spinner spDataParity = (Spinner) findViewById(R.id.spDataParity);
        tempValue = spDataParity.getSelectedItemPosition();
        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 98, 1);

        Spinner spStopBits = (Spinner) findViewById(R.id.spStopBits);
        tempValue = Integer.valueOf(spStopBits.getSelectedItem().toString());
        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 97, 1);

        Spinner spFlowControl = (Spinner) findViewById(R.id.spFlowControl);
        tempValue = spFlowControl.getSelectedItemPosition();
        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 99, 1);

        // WiFi System Settings
        Spinner spWifiNetworkMode = (Spinner) findViewById(R.id.spWifiNetworkMode);
        // nWifiNetworkMode =  1: Station Mode,  2: AP mode
        tempValue = spWifiNetworkMode.getSelectedItemPosition() + 1;

        if (tempValue == 2) {
            bIsApMode = true;
        }

        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 433, 1);

        // Station mode: enable DHCP client,  AP mode: disable DHCP client
        if (tempValue == 2) {
            setReqDp.getData()[32] &= 0xBF; // Disable DHCP client: bit 4 = 0
            setReqDp.getData()[630] = 1;
        }
        else if (tempValue == 1) {
            setReqDp.getData()[32] |= 0x40; // Enable DHCP client: bit 4 = 1
            setReqDp.getData()[630] = 0;
        }

        Spinner spWifiApChannel = (Spinner) findViewById(R.id.spWifiApChannel);
        //String[] itemsWifiApChannel = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};
        tempValue = spWifiApChannel.getSelectedItemPosition() + 1;
        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 434, 1);

        EditText etWifiSSID = (EditText) findViewById(R.id.etWifiSSID);

        if (etWifiSSID.getText().length() > 33) {
            String strValidId = etWifiSSID.getText().toString().substring(0, 32);
            etWifiSSID.setText(strValidId);
        }

        SetStringItemDataToDatagramPacket(setReqDp, etWifiSSID.getText().toString(), 436, 33);
        tempValue = etWifiSSID.getText().length();
        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 435, 1);

        Spinner spWifiSecurityMode = (Spinner) findViewById(R.id.spWifiSecurityMode);
        //String[] itemsWifiSecurityMode = new String[]{"Open", "WEP", "WPA/WPA2 Auto"};
        //String[] itemsWifiSecurityModeAp = new String[]{"Open", "WPA/WPA2 Auto"};

        tempValue = spWifiSecurityMode.getSelectedItemPosition();

        if ((bIsApMode == true) && (tempValue == 1)) {
            tempValue = 2; //  [WPA/WPA2 Auto]
        }

        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 469, 1);

        Spinner spWepKeyLength = (Spinner) findViewById(R.id.spWepKeyLength);
        //String[] itemsWepKeyLength = new String[]{"64 bits", "128 bits"};
        tempValue = spWepKeyLength.getSelectedItemPosition();
        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 471, 1);

        Spinner spWepKeyIndexSelect = (Spinner) findViewById(R.id.spWepKeyIndexSelect);
        //String[] itemsWepKeyIndexSelect = new String[]{"Key Index 0", "Key Index 1", "Key Index 2", "Key Index 3"};
        tempValue = spWepKeyIndexSelect.getSelectedItemPosition();
        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 470, 1);

        EditText etWepKeyIndex0 = (EditText) findViewById(R.id.etWepKeyIndex0);
        EditText etWepKeyIndex1 = (EditText) findViewById(R.id.etWepKeyIndex1);
        EditText etWepKeyIndex2 = (EditText) findViewById(R.id.etWepKeyIndex2);
        EditText etWepKeyIndex3 = (EditText) findViewById(R.id.etWepKeyIndex3);

        if (spWepKeyLength.getSelectedItemPosition() == 0) {
            SetStringItemDataToDatagramPacket(setReqDp, etWepKeyIndex0.getText().toString(), 472, 5);
            SetStringItemDataToDatagramPacket(setReqDp, etWepKeyIndex1.getText().toString(), 477, 5);
            SetStringItemDataToDatagramPacket(setReqDp, etWepKeyIndex2.getText().toString(), 482, 5);
            SetStringItemDataToDatagramPacket(setReqDp, etWepKeyIndex3.getText().toString(), 487, 5);
        }
        else {
            SetStringItemDataToDatagramPacket(setReqDp, etWepKeyIndex0.getText().toString(), 492, 13);
            SetStringItemDataToDatagramPacket(setReqDp, etWepKeyIndex1.getText().toString(), 505, 13);
            SetStringItemDataToDatagramPacket(setReqDp, etWepKeyIndex2.getText().toString(), 518, 13);
            SetStringItemDataToDatagramPacket(setReqDp, etWepKeyIndex3.getText().toString(), 531, 13);
        }

        // WEP Encryption Key Settings
        EditText etAesTkipPassphrase = (EditText) findViewById(R.id.etAesTkipPassphrase);
        SetStringItemDataToDatagramPacket(setReqDp, etAesTkipPassphrase.getText().toString(), 545, 65);
        tempValue = etAesTkipPassphrase.getText().length();
        SetNumberItemDataToDatagramPacket(setReqDp, tempValue, 544, 1);
    }

    private void SetAppUiDeviceConfiguration() {
        int i;

        // Serial To Network Settings
        Spinner spServerClientMode = (Spinner) findViewById(R.id.spServerClientMode);

        if (nServerClientMode == 0) {
            spServerClientMode.setSelection(0); // Server mode
        }
        else {
            spServerClientMode.setSelection(1); // Client Mode
        }

        EditText etServerListeningPort = (EditText) findViewById(R.id.etServerListeningPort);
        etServerListeningPort.setText(Integer.toString(nServerListeningPort));

        EditText etClientDestinationHostIpName = (EditText) findViewById(R.id.etClientDestinationHostIpName);
        etClientDestinationHostIpName.setText(strClientDestinationHostIpName);

        EditText etClientDestinationPort = (EditText) findViewById(R.id.etClientDestinationPort);
        etClientDestinationPort.setText(Integer.toString(nClientDestinationPort));

        // Static IP Settings
        EditText etStaticIpAddress = (EditText) findViewById(R.id.etStaticIpAddress);
        etStaticIpAddress.setText(strStaticIpAddress);

        EditText etStaticDefaultGateway = (EditText) findViewById(R.id.etStaticDefaultGateway);
        etStaticDefaultGateway.setText(strStaticDefaultGateway);

        EditText etStaticSubnetMask = (EditText) findViewById(R.id.etStaticSubnetMask);
        etStaticSubnetMask.setText(strStaticSubnetMask);

        EditText etStaticDnsServer = (EditText) findViewById(R.id.etStaticDnsServer);
        etStaticDnsServer.setText(strStaticDnsServer);

        // Serial Port Settings
        Spinner spDataBuadRate = (Spinner) findViewById(R.id.spDataBuadRate);
        int[] itemsDataBuadRate = new int[]{921600, 115200, 57600, 38400, 19200, 9600, 4800, 2400, 1200};

        for (i = 0; i < 9; i++) {
            if (nUartDataBaudRate == itemsDataBuadRate[i]) {
                spDataBuadRate.setSelection(i);
            }
        }

        Spinner spDataBits = (Spinner) findViewById(R.id.spDataBits);
        int[] itemsDataBits = new int[]{7, 8};

        for (i = 0; i < 2; i++) {
            if (nUartDataBits == itemsDataBits[i]) {
                spDataBits.setSelection(i);
            }
        }

        Spinner spDataParity = (Spinner) findViewById(R.id.spDataParity);
        //String[] itemsDataParity = new String[]{"None", "Odd", "Even"};
        spDataParity.setSelection(nUartDataParity);

        Spinner spStopBits = (Spinner) findViewById(R.id.spStopBits);
        int[] itemsStopBits = new int[]{1, 2};

        for (i = 0; i < 2; i++) {
            if (nUartStopBits == itemsStopBits[i]) {
                spStopBits.setSelection(i);
            }
        }

        Spinner spFlowControl = (Spinner) findViewById(R.id.spFlowControl);
        //String[] itemsFlowControl = new String[]{"None", "RTS+CTS"};
        spFlowControl.setSelection(nUartFlowControl);

        // WiFi System Settings
        EditText etWifiSSID = (EditText) findViewById(R.id.etWifiSSID);
        etWifiSSID.setText(strWifiSSID);

        //==============================================================================
        // Note: Below 3 setting items are related:
        // spWifiApChannel and spWifiSecurityMode are handlled in
        // spWifiNetworkMode.setOnItemSelectedListener() function.

        //Spinner spWifiApChannel = (Spinner) findViewById(R.id.spWifiApChannel);
        //String[] itemsWifiApChannel = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};
        //spWifiApChannel.setSelection(nWifiApChannel - 1);

        //Spinner spWifiSecurityMode = (Spinner) findViewById(R.id.spWifiSecurityMode);
        //String[] itemsWifiSecurityMode = new String[]{"Open", "WEP", "WPA/WPA2 Auto"};
        //spWifiSecurityMode.setSelection(nWifiSecurityMode);

        Spinner spWifiNetworkMode = (Spinner) findViewById(R.id.spWifiNetworkMode);
        //String[] itemsWifiNetworkMode = new String[]{"Station", "AP"};
        // nWifiNetworkMode =  1: Station mode,  2: AP mode
        spWifiNetworkMode.setSelection(nWifiNetworkMode - 1);
        //==============================================================================

        Spinner spWepKeyLength = (Spinner) findViewById(R.id.spWepKeyLength);
        //String[] itemsWepKeyLength = new String[]{"64 bits", "128 bits"};
        spWepKeyLength.setSelection(nWepKeyLength);

        Spinner spWepKeyIndexSelect = (Spinner) findViewById(R.id.spWepKeyIndexSelect);
        //String[] itemsWepKeyIndexSelect = new String[]{"Key Index 0", "Key Index 1", "Key Index 2", "Key Index 3"};
        spWepKeyIndexSelect.setSelection(nWepKeyIndexSelect);

        EditText etWepKeyIndex0 = (EditText) findViewById(R.id.etWepKeyIndex0);
        EditText etWepKeyIndex1 = (EditText) findViewById(R.id.etWepKeyIndex1);
        EditText etWepKeyIndex2 = (EditText) findViewById(R.id.etWepKeyIndex2);
        EditText etWepKeyIndex3 = (EditText) findViewById(R.id.etWepKeyIndex3);

        if (nWepKeyLength == 0) {
            etWepKeyIndex0.setText(strWeb64KeyIndex0);
            etWepKeyIndex1.setText(strWeb64KeyIndex1);
            etWepKeyIndex2.setText(strWeb64KeyIndex2);
            etWepKeyIndex3.setText(strWeb64KeyIndex3);
        }
        else {
            etWepKeyIndex0.setText(strWeb128KeyIndex0);
            etWepKeyIndex1.setText(strWeb128KeyIndex1);
            etWepKeyIndex2.setText(strWeb128KeyIndex2);
            etWepKeyIndex3.setText(strWeb128KeyIndex3);
        }

        // WEP Encryption Key Settings
        EditText etAesTkipPassphrase = (EditText) findViewById(R.id.etAesTkipPassphrase);
        etAesTkipPassphrase.setText(strAesTkipPassphrase);

        // Set Status Tab items
        TextView tvDeviceName = (TextView) findViewById(R.id.tvDeviceName);
        tvDeviceName.setText(strDeviceName);

        TextView tvDeviceIpAddress = (TextView) findViewById(R.id.tvDeviceIpAddress);
        tvDeviceIpAddress.setText(strDeviceIpAddress);

        TextView tvWifiMacAddress = (TextView) findViewById(R.id.tvWifiMacAddress);
        tvWifiMacAddress.setText(strWiFiMacAddressHex);

        TextView tvDhcpStatus = (TextView) findViewById(R.id.tvDhcpStatus);
        tvDhcpStatus.setText(strDhcpStatus);

        TextView tvProtocolType = (TextView) findViewById(R.id.tvProtocolType);
        tvProtocolType.setText(strProtocolType);

        TextView tvImageFilename = (TextView) findViewById(R.id.tvImageFilename);
        tvImageFilename.setText(strImageFileName);
    }

    private void SetNumberItemDataToDatagramPacket(DatagramPacket dp, int nData, int offset, int length) {
        if (length == 1) {
            dp.getData()[offset] = (byte) nData;
        }
        else if (length == 2) {
            // Note: use Big-endian here
            dp.getData()[offset] = (byte) (nData >> 8);
            dp.getData()[offset + 1] = (byte) nData;
        }
        else if (length == 4) {
            // Note: use Little-endian here (for Baud Rate item data bytes order)
            dp.getData()[offset + 3] = (byte) (nData >> 24);
            dp.getData()[offset + 2] = (byte) (nData >> 16);
            dp.getData()[offset + 1] = (byte) (nData >> 8);
            dp.getData()[offset] = (byte) nData;
        }
    }

    private void SetStringItemDataToDatagramPacket(DatagramPacket dp, String strData, int offset, int length) {
        int i;

        for (i = 0; i < strData.length(); i++) {
            dp.getData()[offset + i] = strData.getBytes()[i];
        }

        if (length > strData.length()) {
            Log.v(" len = " + Integer.toHexString(length), "data len = " + Integer.toHexString(strData.length()));
            for (i = strData.length(); i < length; i++) {
                dp.getData()[offset + i] = 0x00;
            }
        }

    }

    private void SetIpAddrItemToDatagramPacket(DatagramPacket dp, String strIpAddr, int offset) {
        try {
            InetAddress ip = InetAddress.getByName(strIpAddr);

            for (int i = 0; i < 4; i++) {
                dp.getData()[offset + i] = ip.getAddress()[i];
            }
        }
        catch (UnknownHostException e) {

        }
    }

    private int GetNumberItemDataFromDatagramPacket(DatagramPacket dp, int offset, int length) {
        int a, b;

        if (length == 1) {
            return (dp.getData()[offset] & 0xFF);
        }
        else if (length == 2) {
            // Note: use Big-endian here
            return  ((dp.getData()[offset] & 0xFF) * 256) + (dp.getData()[offset + 1] & 0xFF);
        }
        else if (length == 4) {
            // Note: use Little-endian here (for Baud Rate item data bytes order)
            return  (dp.getData()[offset] & 0xFF) + ((dp.getData()[offset + 1] & 0xFF) * 256) + ((dp.getData()[offset + 2] & 0xFF) * 65536) + ((dp.getData()[offset + 3] & 0xFF) * 16777216);
        }

        return -1;
    }

    private String GetStringItemDataFromDatagramPacket(DatagramPacket dp, int offset, int length) {
        String strItem = "";

        for (int i = 0; i < length; i++) {
            if (dp.getData()[offset + i] > 0x20 && dp.getData()[offset + i] < 0x7F) {
                strItem += (char) dp.getData()[offset + i];
            }
            else {
                break;
            }
        }

        return strItem;
    }

    private String GetIpAddrItemDataFromDatagramPacket(DatagramPacket dp, int offset) {
        String strItem = "";
        int byDat;

        for (int i = 0; i < 4; i++) {
            byDat = (dp.getData()[offset + i] & 0xFF);
            strItem += Integer.toString(byDat);

            if (i < 3) {
                strItem += ".";
            }
        }

        return strItem;
    }

    private String GetMacAddrItemDataFromDatagramPacket(DatagramPacket dp, int offset) {
        String strItem = "";
        int byDat;

        for (int i = 0; i < 6; i++) {
            byDat = (dp.getData()[offset + i] & 0xFF);

            if (byDat < 0x0F) {
                strItem += "0";
            }

            strItem += Integer.toHexString(byDat);
        }

        return strItem.toUpperCase();
    }

    private void HandleConfigurationFromDeviceToApp(DatagramPacket dp) {
        int tempValue = 0;

        setReqDp.setSocketAddress(dp.getSocketAddress());
        nConfigPktLength =  dp.getLength();
        System.arraycopy(dp.getData(), 0, setReqData, 0, nConfigPktLength);

        // Serial To Network Settings
        // bit 8 = 0: Server mode,  1: Client mode
        tempValue = GetNumberItemDataFromDatagramPacket(dp, 32, 1);
        nServerClientMode = ((tempValue >> 7) & 0x01);

        nServerListeningPort = GetNumberItemDataFromDatagramPacket(dp, 42, 2);
        strClientDestinationHostIpName = GetStringItemDataFromDatagramPacket(dp, 48, 36);
        nClientDestinationPort = GetNumberItemDataFromDatagramPacket(dp, 46, 2);

        // Static IP Settings
        strStaticIpAddress = GetIpAddrItemDataFromDatagramPacket(dp, 38);
        strStaticDefaultGateway = GetIpAddrItemDataFromDatagramPacket(dp, 88);
        strStaticSubnetMask = GetIpAddrItemDataFromDatagramPacket(dp, 84);
        strStaticDnsServer = GetIpAddrItemDataFromDatagramPacket(dp, 92);

        // Serial Port Settings
        nUartDataBaudRate = GetNumberItemDataFromDatagramPacket(dp, 100, 4);
        nUartDataBits = GetNumberItemDataFromDatagramPacket(dp, 96, 1);
        nUartDataParity = GetNumberItemDataFromDatagramPacket(dp, 98, 1);
        nUartStopBits = GetNumberItemDataFromDatagramPacket(dp, 97, 1);
        nUartFlowControl = GetNumberItemDataFromDatagramPacket(dp, 99, 1);

        // WiFi System Settings
        nWifiNetworkMode = GetNumberItemDataFromDatagramPacket(dp, 433, 1);
        nWifiApChannel = GetNumberItemDataFromDatagramPacket(dp, 434, 1);
        tempValue = GetNumberItemDataFromDatagramPacket(dp, 435, 1); // Get SSID length
        strWifiSSID = GetStringItemDataFromDatagramPacket(dp, 436, tempValue);
        nWifiSecurityMode = GetNumberItemDataFromDatagramPacket(dp, 469, 1); // WifiEncryptMode

        // WEP Encryption Key Settings
        nWepKeyLength = GetNumberItemDataFromDatagramPacket(dp, 471, 1);
        nWepKeyIndexSelect = GetNumberItemDataFromDatagramPacket(dp, 470, 1);

        strWeb64KeyIndex0 = GetStringItemDataFromDatagramPacket(dp, 472, 5);
        strWeb64KeyIndex1 = GetStringItemDataFromDatagramPacket(dp, 477, 5);
        strWeb64KeyIndex2 = GetStringItemDataFromDatagramPacket(dp, 482, 5);
        strWeb64KeyIndex3 = GetStringItemDataFromDatagramPacket(dp, 487, 5);

        strWeb128KeyIndex0 = GetStringItemDataFromDatagramPacket(dp, 492, 13);
        strWeb128KeyIndex1 = GetStringItemDataFromDatagramPacket(dp, 505, 13);
        strWeb128KeyIndex2 = GetStringItemDataFromDatagramPacket(dp, 518, 13);
        strWeb128KeyIndex3 = GetStringItemDataFromDatagramPacket(dp, 531, 13);

        // WEP Encryption Key Settings
        tempValue = GetNumberItemDataFromDatagramPacket(dp, 544, 1); // Get pre-share key length
        strAesTkipPassphrase = GetStringItemDataFromDatagramPacket(dp, 545, tempValue);

        // Handle Status Tab items
        strDeviceName = GetStringItemDataFromDatagramPacket(dp, 10, 16);
        strDeviceIpAddress = GetIpAddrItemDataFromDatagramPacket(dp, 34);
        strWiFiMacAddressHex = "0x" + GetMacAddrItemDataFromDatagramPacket(dp, 26);

        tempValue = GetNumberItemDataFromDatagramPacket(dp, 32, 1);
        strDhcpStatus = "Disable";

        if ((tempValue & 0x40) == 0x40) {
            strDhcpStatus = "Enable";
        }

        strProtocolType = "?";

        if ((tempValue & 0x04) == 0x04) {
            strProtocolType = "TCP";
        }
        else if ((tempValue & 0x08) == 0x08) {
            strProtocolType = "UDP";
        }

        strImageFileName = GetStringItemDataFromDatagramPacket(dp, 368, 64);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Log.i("ASIX", "RESULT_OK");
            switch (requestCode) {
                case CHOOSE_FILE_REQUEST:
                    Log.i("ASIX", "CHOOSE_FILE_REQUEST");
                    strFilePath = data.getStringExtra("FILE_PATH");
                    TextView nameText = (TextView) findViewById(R.id.filePath);
                    if (!strFilePath.equalsIgnoreCase(""))
                    {
                        nameText.setText(strFilePath);
                        File f = new File(strFilePath);
                        long size = f.length();
                        String str;

                        TextView fileSizeText = (TextView) findViewById(R.id.fileSize);
                        str = String.format("%d Byte", size);
                        fileSizeText.setText(str);

                        TextView fileProgressText = (TextView) findViewById(R.id.sendFileProgressText);
                        str = String.format("0/%d", size);
                        fileProgressText.setText(str);
                        pbSendFile = (ProgressBar) findViewById(R.id.showSendFileProgress);
                        pbSendFile.setProgress(0);
                    } else {
                        nameText.setText("Select File to Send");
                    }

                    Button sendFile = (Button) findViewById(R.id.sendFile);
                    if (strFilePath.equalsIgnoreCase("")) {
                        sendFile.setEnabled(false);
                    } else {
                        sendFile.setEnabled(true);
                    }
                    break;
                case CHOOSE_FW_FILE_REQUEST:
                    Log.i("ASIX", "CHOOSE_FW_FILE_REQUEST");
                    strFwFilePath = data.getStringExtra("FILE_PATH");
                    TextView fwFileText = (TextView) findViewById(R.id.showFwFilePath);
                    if (!strFwFilePath.equalsIgnoreCase("")) {
                        fwFileText.setText(strFwFilePath);

                        File f = new File(strFwFilePath);
                        long size = f.length();
                        String str;

                        TextView fwFileSizeText = (TextView) findViewById(R.id.showFwFileSize);
                        str = String.format("%d Byte", size);
                        fwFileSizeText.setText(str);

                        TextView fwProgressText = (TextView) findViewById(R.id.upgradeFwProgressText);
                        str = String.format("0/%d", size);
                        fwProgressText.setText(str);
                        pbFwUpgrade = (ProgressBar) findViewById(R.id.showUpgradeFwProgress);
                        pbFwUpgrade.setProgress(0);
                    } else {
                        fwFileText.setText("Select FW File to Upgrade");
                    }

                    Button upgradeFw = (Button) findViewById(R.id.upgradeFw);
                    if (strFwFilePath.equalsIgnoreCase("")) {
                        upgradeFw.setEnabled(false);
                    } else {
                        upgradeFw.setEnabled(true);
                    }
                    break;
            }

        }
    }

    public void chooseFile(View view) {
        Intent intent = new Intent(SendFileActivity.this, FileChooser.class);
        intent.putExtra("TEMP_IP", destIp);
        //startActivity(intent);
        startActivityForResult(intent, CHOOSE_FILE_REQUEST);
        //finish();
    }

    public void chooseFwFile(View view) {
        Intent intent = new Intent(SendFileActivity.this, FileChooser.class);
        intent.putExtra("TEMP_IP", destIp);
        //startActivity(intent);
        startActivityForResult(intent, CHOOSE_FW_FILE_REQUEST);
        //finish();
    }

    class TcpConnectThread extends Thread {
        String dstAddress;
        int dstPort;
        Handler uihandler;
        int     createRXThread;

        TcpConnectThread(Handler handler, String address, int port) {
            uihandler = handler;
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Message sendmsg;

            try {
                while (true) {
                    if (deviceState == 0) {
                        createRXThread = 1;
                        break;
                    } else if (deviceState == 1) {
                        sendmsg = Message.obtain();
                        sendmsg.what = 3;
                        uihandler.sendMessage(sendmsg);
                        createRXThread = 0;
                        return;
                    } else if (deviceState == -2) {
                        sendmsg = Message.obtain();
                        sendmsg.what = 4;
                        uihandler.sendMessage(sendmsg);
                        createRXThread = 0;
                        return;
                    }
                }
                socketTCP = new Socket(dstAddress, dstPort);
            } catch (IOException e) {
                sendmsg = Message.obtain();
                sendmsg.what = 2;
                uihandler.sendMessage(sendmsg);
                Log.i("socketTCP", "IOException e");
            } finally {
                if (createRXThread == 1 && socketTCP != null) {
                    sendmsg = Message.obtain();
                    sendmsg.what = 1;
                    uihandler.sendMessage(sendmsg);

                    ClientRxThread clientRxThread = new ClientRxThread(globalHandler);
                    clientRxThread.start();
                }
            }
        }
    }

    class ClientRxThread extends Thread {
        Handler uihandler;

        ClientRxThread(Handler handler) {
            uihandler = handler;
        }

        @Override
        public void run() {
            byte[]  recvBytes = new byte[512];
            int     recvBytesRead;

            try {
                InputStream is = socketTCP.getInputStream();

                while (true) {
                    Arrays.fill(recvBytes, (byte) 0);
                    recvBytesRead = is.read(recvBytes, 0, recvBytes.length);

                    // TODO: you can hamdle TCP RX data at here
                }
            } catch (IOException e) {
                Log.i("NASH", "IO exception in ClientRxThread( )");
            }
        }
    }

    public String TransferHexStringToAsciiString (byte[] buffer, int length) {
        int     totalStringBytes = (length * 2);
        byte[]  stringByteResult = new byte[totalStringBytes];
        int     i, j;
        char    tempByte1 = 0;
        char    tempByte2 = 0;

        if ((totalStringBytes % 2) != 0) {
            return null;
        }

        j = 0;

        for (i = 0; i < totalStringBytes; i++) {
            if (buffer[i] >= '0' && buffer[i] <= '9') {
                tempByte1 = (char) buffer[i];
                tempByte1 -= '0';
                tempByte2 |= tempByte1;
            } else if (buffer[i] >= 'A' && buffer[i] <= 'F') {
                tempByte1 = (char) buffer[i];
                tempByte1 -= 0x37;
                tempByte2 |= tempByte1;
            } else if (buffer[i] >= 'a' && buffer[i] <= 'f') {
                tempByte1 = (char) buffer[i];
                tempByte1 -= 0x57;
                tempByte2 |= tempByte1;
            } else {
                return null;
            }

            if ((i % 2) == 0) {
                tempByte2 <<= 4;
            } else {
                j++;
                stringByteResult[i - j] = (byte) tempByte2;
                tempByte2 = 0;
            }
        }

        return new String(stringByteResult);
    }

    public void tcpSendFile(View view) {
        final Button chooseFile = (Button) findViewById(R.id.fileChooser);
        final Button sendFile = (Button) findViewById(R.id.sendFile);
        final TextView fileProgressText = (TextView) findViewById(R.id.sendFileProgressText);

        chooseFile.setEnabled(false);
        sendFile.setEnabled(false);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    chooseFile.setEnabled(true);
                    sendFile.setEnabled(true);
                    pbSendFile.setProgress(fileSize);
                    fileProgressText.setText(String.format("%d/ %d", fileSize, fileSize));
                    Toast.makeText(SendFileActivity.this, "Send File Finish!", Toast.LENGTH_LONG).show();
                } else if (msg.what == 2) {
                    fileSize = msg.getData().getInt("FileSize", 0);
                    if (fileSize != 0){
                        pbSendFile.setMax(fileSize);
                    }
                } else if (msg.what == 3) {
                    int fileStep = msg.getData().getInt("FileStep", 0);
                    if (fileStep !=0) {
                        pbSendFile.setProgress(fileStep);
                        fileProgressText.setText(String.format("%d/ %d", fileStep, fileSize));
                    }
                } else if (msg.what == 4) {
                    chooseFile.setEnabled(true);
                    sendFile.setEnabled(true);
                    Toast.makeText(SendFileActivity.this, "Send File Fail!", Toast.LENGTH_LONG).show();
                }
            }
        };

        ClientTxThread clientTxThread = new ClientTxThread(handler, destIp, nDevPort, strFilePath);
        clientTxThread.start();
    }

    class ClientTxThread extends Thread {
        String  dstAddress;
        int     dstPort;
        String  filePath;
        Handler uihandler;

        ClientTxThread(Handler handler, String address, int port, String path) {
            uihandler = handler;
            dstAddress = address;
            dstPort = port;
            filePath = path;
        }

        @Override
        public void run() {
            File file = new File(filePath);
            byte[] readFileBytes = new byte[(int) file.length()];
            BufferedInputStream bis;
            int sendByteCounts = 0;
            int fileLength = (int) file.length();
            Message sendmsg;

            sendmsg = Message.obtain();
            sendmsg.what = 2;
            sendmsg.getData().putInt("FileSize", fileLength);
            uihandler.sendMessage(sendmsg);

            try {
                socketTCP.setSendBufferSize(512);
                OutputStream os = socketTCP.getOutputStream();

                bis = new BufferedInputStream(new FileInputStream(file));
                bis.read(readFileBytes, 0, fileLength);

                while ((fileLength - sendByteCounts) > 512) {
                    os.write(readFileBytes, sendByteCounts, 512);
                    sendByteCounts += 512;

                    sendmsg = Message.obtain();
                    sendmsg.what = 3;
                    sendmsg.getData().putInt("FileStep", sendByteCounts);
                    uihandler.sendMessage(sendmsg);

                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        sendmsg = Message.obtain();
                        sendmsg.what = 4;
                        uihandler.sendMessage(sendmsg);
                    }
                }

                os.write(readFileBytes, sendByteCounts, (fileLength - sendByteCounts));
                os.flush();

                sendmsg = Message.obtain();
                sendmsg.what = 1;
                uihandler.sendMessage(sendmsg);
            } catch (IOException e) {
                e.printStackTrace();
                sendmsg = Message.obtain();
                sendmsg.what = 4;
                uihandler.sendMessage(sendmsg);
            }
        }
    }

    public void TftpUpgradeFw(View view) {
        final Button selectFwFile = (Button) findViewById(R.id.selectFwFile);
        final Button upgradeFw = (Button) findViewById(R.id.upgradeFw);
        final TextView fwProgressText = (TextView) findViewById(R.id.upgradeFwProgressText);
        final TextView warningText = (TextView) findViewById(R.id.upgradeFwWarning);

        AlertDialog.Builder ad = new AlertDialog.Builder(SendFileActivity.this);
        ad.setTitle("Warning");
        ad.setMessage("After firmware upgrade, AXM23001 will reboot. Are you sure to upgrade firmware?");
        ad.setNegativeButton("No",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {

            }
        });
        ad.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                warningText.setText("Please don't turn off power, the process may fail due to power failure or loss of network connection.");
                selectFwFile.setEnabled(false);
                upgradeFw.setEnabled(false);
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == 1) {
                            warningText.setText("");
                            selectFwFile.setEnabled(true);
                            upgradeFw.setEnabled(true);
                            Toast.makeText(SendFileActivity.this, "FW Upgrade Finish!", Toast.LENGTH_LONG).show();

                            Intent intent = getIntent();
                            intent.putExtra("UPGRADE_FW", true);
                            setResult(RESULT_OK,intent);
                            finish();
                        } else if (msg.what == 2) {
                            fwFileSize = msg.getData().getInt("FWFileSize", 0);
                            if (fwFileSize != 0) {
                                pbFwUpgrade.setMax(fwFileSize);
                            }
                        } else if (msg.what == 3) {
                            int fwFileStep = msg.getData().getInt("FWFileStep", 0);
                            if (fwFileStep != 0) {
                                pbFwUpgrade.setProgress(fwFileStep);
                                fwProgressText.setText(String.format("%d/ %d", fwFileStep, fwFileSize));
                            }
                        } else if (msg.what == 4) {
                            int fwUpgradeError = msg.getData().getInt("FWUpgradeError", 0);
                            warningText.setText(String.format("FW upgrade failed (err = %d), please select correct FW file.", fwUpgradeError));
                            selectFwFile.setEnabled(true);
                            upgradeFw.setEnabled(true);
                            Toast.makeText(SendFileActivity.this, "FW Upgrade Failed!", Toast.LENGTH_LONG).show();
                        }
                    }
                };

                if (strFwFilePath.equalsIgnoreCase("")) {
                } else {
                    FWUpgradeThread fwUpgradeThread = new FWUpgradeThread(handler);
                    fwUpgradeThread.start();
                }
            }
        });
        ad.show();
    }

    class FWUpgradeThread extends Thread {
        Handler uihandler;

        FWUpgradeThread(Handler handler) {
            uihandler = handler;
        }

        @Override
        public void run() {
            File            file = new File(strFwFilePath);
            int             totalFileSize = (int) file.length();
            byte[]          readFileBytes = new byte[totalFileSize];
            BufferedInputStream bis;
            DatagramSocket  tftpcSocket = null;
            int             tftpcSourcePort = 9999;
            int             tftpcState;
            byte            opCodeWriteReq[] = {0x00, 0x02};
            byte            octetMode[] = {'o', 'c', 't', 'e', 't', 0x00};
            byte            blockSize[] = {'b', 'l', 'k', 's', 'i', 'z', 'e', 0x00, '1', '0', '2', '4', 0x00};
            byte            tSize[] = {'t', 's', 'i', 'z', 'e', 0x00};
            int             fileNameSize = file.getName().length();
            String          fileSizeString = String.format("%d", totalFileSize);
            ByteBuffer      writeReqBuffer = ByteBuffer.allocate(opCodeWriteReq.length + fileNameSize + 1 + octetMode.length + blockSize.length + tSize.length + fileSizeString.length() + 1);
            ByteBuffer      dataBuffer = ByteBuffer.allocate(1028);
            int             i, j;
            byte[]          rxData = new byte[512];
            DatagramPacket  dp = new DatagramPacket(rxData, rxData.length);
            int             retry;
            byte            opCodeData[] = {0x00, 0x03};
            byte            opCodeAck[] = {0x00, 0x04};
            byte            opCodeError[] = {0x00, 0x05};
            byte            opCodeOptionAck[] = {0x00, 0x06};
            int             remainFileSize = totalFileSize;
            int             transmittedFileSize;
            int             sendLength;
            int             blockNum;
            Message         sendmsg;
            boolean         fwUpgradeErrorOccurred = false;
            int             fwUpgradeErrorCode = 0;

            sendmsg = Message.obtain();
            sendmsg.what = 2;
            sendmsg.getData().putInt("FWFileSize", totalFileSize);
            uihandler.sendMessage(sendmsg);

            // Opcode
            writeReqBuffer.array()[0] = opCodeWriteReq[0];
            writeReqBuffer.array()[1] = opCodeWriteReq[1];

            // Destination file
            for (i = 0; i < fileNameSize; i++) {
                writeReqBuffer.array()[2 + i] = file.getName().getBytes()[i];
            }
            j = 2 + fileNameSize;
            writeReqBuffer.array()[j] = 0x00;
            j++;

            // Mode
            for (i = 0; i < octetMode.length; i++) {
                writeReqBuffer.array()[j + i] = octetMode[i];
            }
            j += octetMode.length;

            // Block size header & content (1024)
            for (i = 0; i < blockSize.length; i++) {
                writeReqBuffer.array()[j + i] = blockSize[i];
            }
            j += blockSize.length;

            // Tsize header
            for (i = 0; i < tSize.length; i++) {
                writeReqBuffer.array()[j + i] = tSize[i];
            }
            j += tSize.length;

            // Tsize content
            for (i = 0; i < fileSizeString.length(); i++) {
                writeReqBuffer.array()[j + i] = fileSizeString.getBytes()[i];
            }
            j += fileSizeString.length();
            writeReqBuffer.array()[j] = 0x00;

            try {
                tftpcSocket = new DatagramSocket(tftpcSourcePort);
                DatagramPacket packetWriteRequest = new DatagramPacket(writeReqBuffer.array(), writeReqBuffer.array().length, inetIp, 69);
                DatagramPacket packetData = new DatagramPacket(dataBuffer.array(), dataBuffer.array().length, inetIp, 69);
                bis = new BufferedInputStream(new FileInputStream(file));
                bis.read(readFileBytes, 0, readFileBytes.length);

                tftpcState = 0;
                retry = 0;
                transmittedFileSize = 0;
                blockNum = 1;

                while ((remainFileSize > 0) && (retry < 10)) {
                    try {
                        if (tftpcState == 0) {
                            tftpcSocket.send(packetWriteRequest);
                            tftpcState = 1;
                        } else if (tftpcState == 1) {
                            tftpcSocket.setSoTimeout(1000);
                            tftpcSocket.receive(dp);

                            if ((dp.getData()[0] == opCodeOptionAck[0]) && (dp.getData()[1] == opCodeOptionAck[1])) {
                                retry = 0;
                                tftpcState = 2;
                            } else if ((dp.getData()[0] == opCodeError[0]) && (dp.getData()[1] == opCodeError[1])) {
                                fwUpgradeErrorCode = dp.getData()[3];
                                fwUpgradeErrorOccurred = true;
                                break;
                            }
                        } else if (tftpcState == 2) {
                            packetData.getData()[0] = opCodeData[0];
                            packetData.getData()[1] = opCodeData[1];
                            packetData.getData()[2] = (byte) ((blockNum & 0x0000FF00) >> 8);
                            packetData.getData()[3] = (byte) ((blockNum & 0x000000FF));

                            if (remainFileSize >= 1024) {
                                for (i = 0; i < 1024; i++) {
                                    packetData.getData()[4 + i] =  readFileBytes[transmittedFileSize + i];
                                }
                                sendLength = 1024;
                            } else {
                                for (i = 0; i < remainFileSize; i++) {
                                    packetData.getData()[4 + i] =  readFileBytes[transmittedFileSize + i];
                                }
                                sendLength = remainFileSize;
                            }
                            transmittedFileSize += sendLength;
                            remainFileSize -= sendLength;
                            blockNum++;
                            packetData.setLength(sendLength + 4);
                            tftpcSocket.send(packetData);
                            tftpcState = 3;

                            sendmsg = Message.obtain();
                            sendmsg.what = 3;
                            sendmsg.getData().putInt("FWFileStep", totalFileSize - remainFileSize);
                            uihandler.sendMessage(sendmsg);

                        } else if (tftpcState == 3) {
                            tftpcSocket.setSoTimeout(2000);
                            tftpcSocket.receive(dp);

//                            String str = String.format("Recv: %x %x %x %x", dp.getData()[0], dp.getData()[1], dp.getData()[2], dp.getData()[3]);
//                            Log.i("TFTPC", str);
//                            str = String.format("Previous TX: %x %x %x %x", packetData.getData()[0], packetData.getData()[1], packetData.getData()[2], packetData.getData()[3]);
//                            Log.i("TFTPC", str);

                            if ((dp.getData()[0] == opCodeAck[0]) && (dp.getData()[1] == opCodeAck[1])) {
                                if ((dp.getData()[2] == packetData.getData()[2]) && (dp.getData()[3] == packetData.getData()[3])) {
                                    retry = 0;
                                    tftpcState = 2;
                                }
                            } else if ((dp.getData()[0] == opCodeError[0]) && (dp.getData()[1] == opCodeError[1])) {
                                fwUpgradeErrorCode = dp.getData()[3];
                                fwUpgradeErrorOccurred = true;
                                break;
                            }
                        }
                    } catch (IOException e) {
                        retry++;
                        if (tftpcState == 1) {
                            String str = String.format("Retry write request: %d", retry);
                            Log.i("TFTPC", str);
                        } else if (tftpcState == 3) {
                            tftpcSocket.send(packetData);
                            String str = String.format("Retry data: %d", retry);
                            Log.i("TFTPC", str);
                        }
                    }
                }

//                bis.close();

                if (fwUpgradeErrorOccurred == false) {
                    sendmsg = Message.obtain();
                    sendmsg.what = 1;
                    uihandler.sendMessage(sendmsg);
                } else {
                    sendmsg = Message.obtain();
                    sendmsg.what = 4;
                    sendmsg.getData().putInt("FWUpgradeError", fwUpgradeErrorCode);
                    uihandler.sendMessage(sendmsg);
                }

            } catch (SocketException e) {
                Log.i("TFTPC", "SOCKET ERROR");
            } catch (IOException e) {
                Log.i("TFTPC", "IO ERROR");
            }

            if (tftpcSocket != null) {
                Log.i("TFTPC", "CLOSE SOCKET");
                tftpcSocket.close();
            }
        }
    }

    public void updateDeviceConfiguration(View view) throws IOException {
        final Button btUpdateDeviceConfiguration = (Button) findViewById(R.id.btUpdateDeviceConfiguration);
        btUpdateDeviceConfiguration.setEnabled(false);

        AlertDialog.Builder ad = new AlertDialog.Builder(SendFileActivity.this);
        ad.setTitle("Submit");
        ad.setMessage("To submit configuration and reboot device?\nPress YES to submit.");
        ad.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                // Check App UI configuration settings
                GetAppUiDeviceConfiguration();

                setReqDp.getData()[8] = 0x02; // set rqq
                setReqDp.getData()[9] = 0x01; // 1: enable reboot

                new UpdateAxmDevConfigurationTask().execute();
            }
        });

        ad.setNegativeButton("No",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                btUpdateDeviceConfiguration.setEnabled(true);
            }
        });
        ad.show();
    }

    class UpdateAxmDevConfigurationTask extends AsyncTask<Void, Integer, String> {

        protected String doInBackground(Void... params) {
            return UpdateAxmDevConfiguration();
        }

        protected void onPostExecute(String result) {
            if (result.equals("DEV_CONFIG_SUCCESS")) {
                Toast.makeText(SendFileActivity.this, "Waiting 6 seconds for device reboot...", Toast.LENGTH_LONG).show();

                new CountDownTimer(6000, 1000) {
                    public void onFinish() {
                        Toast.makeText(SendFileActivity.this, "Device reboot done", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SendFileActivity.this, discoveryActivity.class);
                        startActivity(intent);
                    }

                    public void onTick(long millisUntilFinished) {
                    }
                }.start();
            } else if (result.equals("DEV_CONFIG_FAILED")) {
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Device has no response! Please check its connection.");
                alertDialog.show();
            } else if (result.equals("SOCKET_ERROR")) {
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Create socket error.");
                alertDialog.show();
            }
        }
    }

    private String UpdateAxmDevConfiguration() {
        DatagramSocket clientSocket = null;
        byte[] rxData = new byte[1024];
        DatagramPacket dp = new DatagramPacket(rxData, rxData.length);
        boolean getSetDeviceResult = false;

        Log.i("ASIX", "Submit device configuration request");

        int retry = 0;
        try {
            clientSocket = new DatagramSocket(25120);

            setReqDp.setLength(nConfigPktLength);
            clientSocket.send(setReqDp);

            Log.i("ASIX", "Send device configuration reuest packet");
            Log.i("ASIX", "Wait device reply ACK packet");

            while (retry < 5) {
                try {
                    clientSocket.setSoTimeout(2000);
                    clientSocket.receive(dp);
                    Log.d("ASIX", "get rx data dp.getData()[8] = " + dp.getData()[8]);

                    if (dp.getData()[0] == 'A' &&
                            dp.getData()[1] == 'S' &&
                            dp.getData()[2] == 'I' &&
                            dp.getData()[3] == 'X' &&
                            dp.getData()[4] == 'X' &&
                            dp.getData()[5] == 'I' &&
                            dp.getData()[6] == 'S' &&
                            dp.getData()[7] == 'A' &&
                            dp.getData()[8] == 0x03) {

                        getSetDeviceResult = true;
                        break;
                    }
                } catch (IOException e) {
                    retry += 1;
                    if (!getSetDeviceResult) {
                        clientSocket.send(setReqDp);
                        String str = String.format("Retry device configuration request : %d", retry);
                        Log.i("ASIX", str);
                    }
                }
            }
        } catch (SocketException e) {
            return "SOCKET_ERROR";
        } catch (IOException e) {
            if (clientSocket != null) {
                clientSocket.close();
            }

            if (getSetDeviceResult) {
                return "DEV_CONFIG_SUCCESS";
            } else {
                return "DEV_CONFIG_FAILED";
            }
        }

        if (clientSocket != null) {
            clientSocket.close();
        }

        if (getSetDeviceResult) {
            return "DEV_CONFIG_SUCCESS";
        } else {
            return "DEV_CONFIG_FAILED";
        }
    }

    public void startUartPortConnection(View view) throws IOException {

        if (bUartSocketConnected) {
            try {
                socketTCP.close();
                socketTCP = null;
                Toast.makeText(SendFileActivity.this, "UART connection closed", Toast.LENGTH_LONG).show();

                Button btStartTcpConnection = (Button) findViewById(R.id.btStartTcpConnection);
                btStartTcpConnection.setEnabled(true);

                final Button btSendDataToUartPort = (Button) findViewById(R.id.btSendDataToUartPort);
                btSendDataToUartPort.setEnabled(false);

                EditText etUartTxData = (EditText) findViewById(R.id.etUartTxData);
                etUartTxData.setEnabled(false);

                final Button btStartUartPortConnection = (Button) findViewById(R.id.btStartUartPortConnection);
                btStartUartPortConnection.setText("Start UART Connection");

                bUartSocketConnected = false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return;
        }

        if (nDevPort != 0) {
            Log.i("ASIX", "Do UART connection");
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    TextView showDeviceStateText = (TextView) findViewById(R.id.showDeviceState);
                    if (msg.what == 1) {
                        showDeviceStateText.setText("UART connection success");
                        Toast.makeText(SendFileActivity.this, "UART socket connected.", Toast.LENGTH_LONG).show();

                        Button btStartTcpConnection = (Button) findViewById(R.id.btStartTcpConnection);
                        btStartTcpConnection.setEnabled(false);

                        final Button btSendDataToUartPort = (Button) findViewById(R.id.btSendDataToUartPort);
                        btSendDataToUartPort.setEnabled(true);

                        EditText etUartTxData = (EditText) findViewById(R.id.etUartTxData);
                        etUartTxData.setEnabled(true);

                        final Button btStartUartPortConnection = (Button) findViewById(R.id.btStartUartPortConnection);
                        btStartUartPortConnection.setText("Close UART Connection");

                        bUartSocketConnected = true;
                    } else if (msg.what == 2) {
                        showDeviceStateText.setText("UART connection failed");
                        Toast.makeText(SendFileActivity.this, "Create TCP socket fail.", Toast.LENGTH_LONG).show();
                    } else if (msg.what == 3) {
                        showDeviceStateText.setText("UART connection failed");
                        Toast.makeText(SendFileActivity.this, "TCP connection already be occupied by another one.", Toast.LENGTH_LONG).show();
                    } else if (msg.what == 4) {
                        showDeviceStateText.setText("UART connection failed");
                        Toast.makeText(SendFileActivity.this, "Device did not reply UDP management.", Toast.LENGTH_LONG).show();
                    }
                }
            };

            AxmDevUartConnectThread axmDevUartConnectThread = new AxmDevUartConnectThread(handler, destIp, nDevPort);
            axmDevUartConnectThread.start();
        }
    }

    class AxmDevUartConnectThread extends Thread {
        String dstAddress;
        int dstPort;
        Handler uihandler;
        int     createRXThread;

        AxmDevUartConnectThread(Handler handler, String address, int port) {
            uihandler = handler;
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Message sendmsg;

            try {
                while (true) {
                    if (deviceState == 0) {
                        createRXThread = 1;
                        break;
                    } else if (deviceState == 1) {
                        sendmsg = Message.obtain();
                        sendmsg.what = 3;
                        uihandler.sendMessage(sendmsg);
                        createRXThread = 0;
                        return;
                    } else if (deviceState == -2) {
                        sendmsg = Message.obtain();
                        sendmsg.what = 4;
                        uihandler.sendMessage(sendmsg);
                        createRXThread = 0;
                        return;
                    }
                }
                socketTCP = new Socket(dstAddress, dstPort);
            } catch (IOException e) {
                sendmsg = Message.obtain();
                sendmsg.what = 2;
                uihandler.sendMessage(sendmsg);
                Log.i("socketTCP", "IOException e");
            } finally {
                if (createRXThread == 1 && socketTCP != null) {
                    sendmsg = Message.obtain();
                    sendmsg.what = 1;
                    uihandler.sendMessage(sendmsg);

                    AxmDevUartRxThread axmDevUartRxThread = new AxmDevUartRxThread(globalHandler);
                    axmDevUartRxThread.start();
                }
            }
        }
    }

    class AxmDevUartRxThread extends Thread {
        Handler uihandler;
        Message rx_msg;

        AxmDevUartRxThread(Handler handler) {
            uihandler = handler;
        }

        @Override
        public void run() {
            byte[]  recvBytes = new byte[512];
            int     recvBytesRead;
            int     i;
            Bundle rx_data = new Bundle();

            try {
                InputStream is = socketTCP.getInputStream();

                while (true) {
                    Arrays.fill(recvBytes, (byte) 0);
                    recvBytesRead = is.read(recvBytes, 0, recvBytes.length);

                    String strData = "";
                    for (i = 0; i < recvBytesRead; i++) {
                        strData += (char) recvBytes[i];
                    }

                    rx_data.putString("data", strData);

                    rx_msg = Message.obtain();
                    rx_msg.setData(rx_data);
                    rx_msg.what = 9;
                    uihandler.sendMessage(rx_msg);
                }
            } catch (IOException e) {
                Log.i("NASH", "IO exception in ClientRxThread( )");
            }
        }
    }

    public void sendDataToAxmDevUartPort(View view) {
        final Button btSendDataToUartPort = (Button) findViewById(R.id.btSendDataToUartPort);
        final EditText etUartTxData = (EditText) findViewById(R.id.etUartTxData);

        btSendDataToUartPort.setEnabled(false);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    btSendDataToUartPort.setEnabled(true);
                    etUartTxData.setText("");
                    Toast.makeText(SendFileActivity.this, "Send data finished!", Toast.LENGTH_LONG).show();
                } else if (msg.what == 2) {
                    btSendDataToUartPort.setEnabled(true);
                    Toast.makeText(SendFileActivity.this, "Send data failed!", Toast.LENGTH_LONG).show();
                }
            }
        };

        AxmDevUartTxThread axmDevUartTxThread = new AxmDevUartTxThread(handler, destIp, nDevPort, etUartTxData.getText().toString());
        axmDevUartTxThread.start();
    }

    class AxmDevUartTxThread extends Thread {
        String  dstAddress;
        int     dstPort;
        String  sendData;
        Handler uihandler;

        AxmDevUartTxThread(Handler handler, String address, int port, String data) {
            uihandler = handler;
            dstAddress = address;
            dstPort = port;
            sendData = data;
        }

        @Override
        public void run() {
            BufferedInputStream bis;
            byte[] sendBytes = new byte[64];
            Message sendmsg = Message.obtain();

            for (int i = 0; i < sendData.length(); i++) {
                sendBytes[i] = (byte) sendData.charAt(i);
            }

            try {
                socketTCP.setSendBufferSize(64);
                OutputStream os = socketTCP.getOutputStream();
                os.write(sendBytes, 0, sendData.length());
                os.flush();

                sendmsg = Message.obtain();
                sendmsg.what = 1;
                uihandler.sendMessage(sendmsg);
            } catch (IOException e) {
                e.printStackTrace();
                sendmsg = Message.obtain();
                sendmsg.what = 2;
                uihandler.sendMessage(sendmsg);
            }
        }
    }

    public void clearUartRxData(View view) {
        TextView tvUartRxData = (TextView) findViewById(R.id.tvUartRxData);
        tvUartRxData.setText("");
        nTvUartRxDataCount = 0;
    }
}
