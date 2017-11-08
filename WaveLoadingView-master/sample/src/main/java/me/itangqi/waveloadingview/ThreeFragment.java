package me.itangqi.waveloadingview;

/**
 * Created by Craig on 2017/11/1.
 */
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import devlight.io.library.ArcProgressStackView;
import me.itangqi.waveloadingview.R;

public class ThreeFragment extends Fragment{
    //    Craig Udp
//    private UDPUtils udpUtils;

    private Button btSend;
    private TextView tvServerLabel;
    private EditText etIp, etRxPort, etTxPort, etMessage;    //Craig static 171106
    private CheckBox cbBroadcast;
    private LinearLayout llLog;

    private String serverLabelMessage = "UDP server listening at port ";

    private String testServer = "192.168.1.107"; //127.0.0.1      //Craig static 171106
    private String udpMsg = "HELLO!\r\n\0";
    private int defaultServerPort = 25122;           //Craig static 171106
    /**
     * Pattern for validate ip well writted
     */
    private static final Pattern PARTIAl_IP_ADDRESS =
            Pattern.compile("^((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])\\.){0,3}"+
                    "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])){0,1}$");

    private Handler handler =new Handler();

    public static HashMap<String,Object> params = new HashMap<String, Object>( );       //Craig changes it  to public

    public static int ThreeFragmentShow;        //To avoid don't exist  component causing crush
    public static boolean UDPSetUpFlag = false;

    public ThreeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.udp_activity, container, false);

        etIp = (EditText) v.findViewById(R.id.etIp);
        etRxPort = (EditText) v.findViewById(R.id.etRxPort);
        etTxPort = (EditText) v.findViewById(R.id.etTxPort);
        etMessage = (EditText) v.findViewById(R.id.etMessage);
        tvServerLabel = (TextView) v.findViewById(R.id.tvServerLabel);
        btSend = (Button) v.findViewById(R.id.btSend);
        llLog = (LinearLayout) v.findViewById(R.id.llLog);
        cbBroadcast = (CheckBox) v.findViewById(R.id.cbBroadcast);

        etRxPort.setText(String.valueOf(defaultServerPort), TextView.BufferType.EDITABLE);
        etIp.setText(String.valueOf(testServer), TextView.BufferType.EDITABLE);

        UdpPageInit();


        return v;
    }

    public void UdpPageInit(){
        //Starting udp server
//        udpUtils = new UDPUtils(getActivity());

        MainActivity.udpUtils.setServerPort(defaultServerPort);
//        udpUtils.startReceiveUdp();

        //Label with the terminal ip and listening port, by default 45450
        tvServerLabel.setText(serverLabelMessage + getIPAddress(true) + ":" +String.valueOf(MainActivity.udpUtils.getServerPort()) );

        //Listener for the Reception port Edit text for change the server label text view and the listening port of udp server
        etRxPort.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) {}

            private String mPreviousText = "";
            @Override
            public void afterTextChanged(Editable s) {
                if ( !(etRxPort.getText().toString().matches("")) ) {
//                    udpUtils.restartReceiveUdp( Integer.parseInt(etRxPort.getText().toString()) );

                    MainActivity.udpUtils.setServerPort( Integer.parseInt(etRxPort.getText().toString()));
                    tvServerLabel.setText(serverLabelMessage + getIPAddress(true) + ":" +String.valueOf(MainActivity.udpUtils.getServerPort()) );
                }
            }
        });


        //Listener for the Ip Edit text that makes it an edit text that only accepts valid ip numbers
        etIp.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) {}

            private String mPreviousText = "";
            @Override
            public void afterTextChanged(Editable s) {
                if(isValidIp(s.toString())) {
                    mPreviousText = s.toString();
                    etIp.setTextColor(Color.BLACK);
                    //Log.v("IP" , "" + etIp.getText());
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Not valid IP" , Toast.LENGTH_SHORT).show();
                    //etIp.setTextColor(Color.RED);
                    s.replace(0, s.length(), mPreviousText);
                }
            }
        });


        //Listener on send button for send an UDP msg datagram to specific ip:port
        btSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                if ( (isValidIp(etIp.getText().toString())) && !(etTxPort.getText().toString().matches("")) ){

//                    HashMap<String,Object> params = new HashMap<String, Object>( );
//                    params.put("ip", testServer);
//                    params.put("msg", udpMsg);
//                    params.put("port", defaultServerPort);

                    if ( cbBroadcast.isChecked() ) {

                    }
                    params.put("ip", etIp.getText().toString() );
                    params.put("msg", etMessage.getText().toString() );
                    params.put("port", Integer.parseInt(etTxPort.getText().toString()) );
                    params.put("bdc", cbBroadcast.isChecked() );

                    //new UDPSender().execute(params);
                    MainActivity.udpUtils.sendUDP(params);

//                    if(handler==null)
//                        handler.postDelayed(runUdpServive,5000);    //Auto Send Udp Packet

                    UDPSetUpFlag =true;

                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Parameters not corrects" , Toast.LENGTH_SHORT).show();
                }

            }

        });

//        UDPListenerService xx = new UDPListenerService();
//        xx.startListenForUDPBroadcast();
        //Toast.makeText(getApplicationContext(), "" + tv.getText(), LENGTH_SHORT).show();

    }

    @Override
    public void onDestroy() {
        if ( MainActivity.udpUtils != null ) MainActivity.udpUtils.stopReceiveUdp();
        super.onDestroy();
        Toast.makeText(getActivity().getApplicationContext(),"Stopped UDP Server", Toast.LENGTH_SHORT).show();
    }

    private boolean isValidIp (String s){
        return PARTIAl_IP_ADDRESS.matcher(s).matches();
    }


    /**
     * Get IP address from first non-localhost interface
     * @param ipv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }


    private Runnable runUdpServive= new Runnable( ) {
        public void run ( ) {
            // Get colors
            MainActivity.udpUtils.sendUDP(params);

            handler.postDelayed(this,3000);
        }
    };

    @Override
    public void onStop() {
//        MainActivity.udpUtils.stopReceiveUdp();
        super.onStop();
    }

    @Override
    public void onPause() {
//        handler.removeCallbacks(runUdpServive);
        ThreeFragmentShow=0;
        super.onPause();
    }

    @Override
    public void onResume(){
//        if (handler == null){
//            handler.postDelayed(runUdpServive,5000);
//            MainActivity.udpUtils.restartReceiveUdp(Integer.parseInt(etTxPort.getText().toString()));
//        }
        ThreeFragmentShow=1;
        super.onResume();
    }

}
