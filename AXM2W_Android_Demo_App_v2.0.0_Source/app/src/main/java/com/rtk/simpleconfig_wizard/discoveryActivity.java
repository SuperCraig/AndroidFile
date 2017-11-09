package com.rtk.simpleconfig_wizard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class discoveryActivity extends Activity {

    AlertDialog.Builder alertDialog;
    private String name;
    private InetAddress devIp;
    private String strIp;
    private ArrayList<String> nameArray = new ArrayList<>();
    private ArrayList<String> ipArray = new ArrayList<>();
    private ArrayList<Integer> portArray = new ArrayList<>();
    private int devCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        alertDialog = new AlertDialog.Builder(this);
    }

    public void searchDevs(View view){
        new DevSearchTask().execute("Login");
    }
    class DevSearchTask extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... strs) {
            return SearchAxmDev();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String result) {

            if (result.equals("DEV_FOUND")) {
                Intent intent = new Intent(discoveryActivity.this, DeviceListActivity.class);
                intent.putExtra("DEV_NAME_ARRAY", nameArray);
                intent.putExtra("DEV_IP_ARRAY", ipArray);
                intent.putExtra("DEV_PORT_ARRAY", portArray);
                startActivity(intent);

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
        ByteBuffer sendData = ByteBuffer.allocate(searchReq.length);
        //ByteBuffer rxData = ByteBuffer.allocate(512);
        byte[] rxData = new byte[1024];
        DatagramPacket dp = new DatagramPacket(rxData, rxData.length);
        nameArray.clear();
        ipArray.clear();
        portArray.clear();
        devCount = 0;
        int retry = 0;
        int port = 0;
        sendData.put(searchReq);

        Log.i("ASIX", "Start search");

        try {
            clientSocket = new DatagramSocket(25120);
            DatagramPacket packet = new DatagramPacket(sendData.array(), sendData.array().length,
                    getBroadcastAddress(), 25122);

            clientSocket.setBroadcast(true);

            clientSocket.send(packet);
            Log.i("ASIX", "Send search req");
            clientSocket.send(packet);
            Log.i("ASIX", "Send search req");
            clientSocket.send(packet);
            Log.i("ASIX", "Send search req");

            Log.i("ASIX", "wait rx data");

            while(true){
                try{
                    clientSocket.setSoTimeout(500);
                    clientSocket.receive(dp);

                    if (dp.getData()[0] != 'A' ||
                            dp.getData()[1] != 'S' ||
                            dp.getData()[2] != 'I' ||
                            dp.getData()[3] != 'X' ||
                            dp.getData()[4] != 'X' ||
                            dp.getData()[5] != 'I' ||
                            dp.getData()[6] != 'S' ||
                            dp.getData()[7] != 'A' ||
                            dp.getData()[8] != 0x01){
                        String opAck = String.format("ACK: 0x%x", dp.getData()[8]);
                        Log.i("ASIX", opAck);
                    }
                    else{
                        // handle rx data
                        devIp = dp.getAddress();
                        strIp = devIp.getHostAddress();

                        if(!ipArray.contains(strIp)){
                            ipArray.add(strIp);
                            name = "";
                            for (int i = 0; i < 16; i++) {
                                if (dp.getData()[10 + i] > 0x20 &&
                                        dp.getData()[10 + i] < 0x7F) {
                                    name += (char) dp.getData()[10 + i];
                                } else {
                                    break;
                                }
                            }
                            if (name == "") name = "DSM0";
                            nameArray.add(name);

                            port = dp.getData()[42];
                            port <<= 8;
                            port += (int)(dp.getData()[43]) & 0x000000FF;
                            portArray.add(port);

                            devCount += 1;
                            Log.i("ASIX", "Add Dev");
                        }

                    }
                }catch (IOException e) {
                    Log.i("ASIX", "Rx IOException");
                    if (devCount == 0){
                        retry += 1;
                        if(retry > 5){
                            if(clientSocket != null){
                                clientSocket.close();
                            }
                            return "DEV_NOT_FOUND";
                        } else{
                            Log.i("ASIX", "Send search req");
                            clientSocket.send(packet);
                            continue;
                        }
                    }
                    // No more device ACK
                    break;
                }
            }

        } catch (SocketException e) {
            if(clientSocket != null){
                clientSocket.close();
            }
            return "SOCKET_ERROR";
        }catch (IOException e) {
            if(clientSocket != null){
                clientSocket.close();
            }
            return "DEV_NOT_FOUND";
        }
        if(clientSocket != null){
            clientSocket.close();
        }

        return "DEV_FOUND";
    }

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }
}
