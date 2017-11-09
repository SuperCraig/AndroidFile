package com.rtk.simpleconfig_wizard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class DeviceListActivity extends Activity {
    private ArrayList<String> devNameArray = new ArrayList<>();
    private ArrayList<String> devIpArray = new ArrayList<>();
    private ArrayList<Integer> devPortArray = new ArrayList<>();
    private ArrayList<String> devListArray = new ArrayList<>();
    private ListView devListView;
    private int layoutId;
    private ArrayAdapter<String> adapter;

    private static final int SEND_FILE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        Intent intent = getIntent();
        devNameArray = intent.getStringArrayListExtra("DEV_NAME_ARRAY");
        devIpArray = intent.getStringArrayListExtra("DEV_IP_ARRAY");
        devPortArray = intent.getIntegerArrayListExtra("DEV_PORT_ARRAY");

        for(int i= 0; i < devIpArray.size(); i++){
            devListArray.add(devNameArray.get(i) + " ("+ devIpArray.get(i)+ ")");
        }

        devListView = (ListView) findViewById(R.id.list_devices);
        layoutId = android.R.layout.simple_list_item_1;
        adapter = new ArrayAdapter<String>(this, layoutId, devListArray);
        devListView.setAdapter(adapter);

        devListView.setOnItemClickListener(listener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Log.i("ASIX", "RESULT_OK");
            switch (requestCode) {
                case SEND_FILE_REQUEST:
                    if (data.getBooleanExtra("UPGRADE_FW", false)) {
                        finish();
                    }
                    break;
            }
        }
    }

    private ListView.OnItemClickListener listener = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            Intent intent = new Intent(DeviceListActivity.this, SendFileActivity.class);
            intent.putExtra("DEST_IP", devIpArray.get(position));
            intent.putExtra("DEST_PORT", devPortArray.get(position));
            //startActivity(intent);
            startActivityForResult(intent, SEND_FILE_REQUEST);
        }
    };
}
