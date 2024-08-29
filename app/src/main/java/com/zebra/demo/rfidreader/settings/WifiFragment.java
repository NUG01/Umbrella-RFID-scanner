package com.zebra.demo.rfidreader.settings;


import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.MAIN_RFID_SETTINGS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.WIFI_MAIN_PAGE;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.common.CustomProgressDialog;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


public class WifiFragment extends BackPressedFragment {
    Context context;
    private CheckBox checkBoxWifi;
    private ProgressDialog progressDialog;
    private ListView lvStatus;
    private static final String TAG = "WifiFragment";


    private WifiFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WifiFragment.
     */
    public static WifiFragment newInstance() {
        return new WifiFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View rootview = inflater.inflate(R.layout.fragment_wifi, container, false);

        context = rootview.getContext();
        checkBoxWifi = rootview.findViewById(R.id.checkboxwifi);
        lvStatus = rootview.findViewById(R.id.lv_status);

        checkBoxWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(RFIDController.mConnectedReader != null) {
                    try {
                        if (isChecked) {
                            RFIDController.mConnectedReader.Config.wifiEnable();
                        } else {
                            RFIDController.mConnectedReader.Config.wifiDisable();
                        }
                    } catch (InvalidUsageException | OperationFailureException e) {
                        if (e != null && e.getStackTrace().length > 0) {
                            Log.e(TAG, e.getStackTrace()[0].toString());
                        }
                    }
                    updateUI();
                }else{
                    Toast.makeText(getActivity(), "Reader not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        updateUI();

        return rootview;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(RFIDController.mConnectedReader != null) {
//            HashMap<String, String> status = new HashMap<>();
//            try {
//                RFIDResults rfidResults = RFIDController.mConnectedReader.Config.wifiGetStatus(status);
//                if (rfidResults == RFIDResults.RFID_API_SUCCESS
//                        && Objects.equals(status.get("wifi"), "ENABLE")) {
//                    checkBoxWifi.setChecked(true);
//                } else {
//                    updateUI();
//                }
//            } catch (InvalidUsageException | OperationFailureException e) {
//                e.printStackTrace();
//            }
            updateUI();
        }
    }

    @Override
    public void onBackPressed() {
        if(getActivity() instanceof SettingsDetailActivity)
            ((SettingsDetailActivity) getActivity()).callBackPressed();
        if(getActivity() instanceof ActiveDeviceActivity) {
            ((ActiveDeviceActivity) getActivity()).callBackPressed();
            ((ActiveDeviceActivity) getActivity()).loadNextFragment(WIFI_MAIN_PAGE);
        }
    }

    private void updateUI(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        HashMap<String, String> status = new HashMap<>();
        AtomicReference<RFIDResults> rfidResults = new AtomicReference<>();
        executor.execute(() -> {
            try {

                rfidResults.set(RFIDController.mConnectedReader.Config.wifiGetStatus(status));

            } catch (InvalidUsageException | OperationFailureException e) {
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }
            handler.post(() -> {

                if (rfidResults.get() == RFIDResults.RFID_API_SUCCESS) {
                    ArrayList<String> wifiStatusArray = new ArrayList<>();
                    for (String key : status.keySet()) {
                       wifiStatusArray.add(key+"  : "+status.get(key));
                    }
                    WifiStatusAdapter wifiStatusAdapter = new WifiStatusAdapter(getActivity(), wifiStatusArray);
                    lvStatus.setAdapter(wifiStatusAdapter);
                }
            });
        });
    }

    public void readWifiScanNotification(String scanStatus, String ssid) {

        Log.d(TAG, " readWifiScanNotification " + scanStatus);

        requireActivity().runOnUiThread(() -> {
            switch (scanStatus) {
                case "ScanStart":
                case "ScanStop":
                    break;
                case "Connect":
                case "Disconnect":
                    updateUI();
                    break;
            }
        });
    }
}
