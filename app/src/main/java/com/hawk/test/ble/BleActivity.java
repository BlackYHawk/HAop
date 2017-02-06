package com.hawk.test.ble;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.hawk.test.R;
import com.hawk.test.util.IntentUtil;
import com.hawk.test.util.MyBluetoothUtil;

public class BleActivity extends Activity {
    private TextView tvAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        tvAddress = (TextView) findViewById(R.id.tvAddress);

        String address = IntentUtil.getBluetoothMac(this);
        tvAddress.setText(address);

        MyBluetoothUtil.getInstance(this).resumeCheck();
    }
}
