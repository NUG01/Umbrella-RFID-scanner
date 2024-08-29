package com.zebra.demo.rfidreader.settings;

import static com.zebra.demo.rfidreader.rfid.RFIDController.dynamicPowerSettings;
import static com.zebra.demo.rfidreader.rfid.RFIDController.isLocatingTag;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedReader;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mIsInventoryRunning;
import static com.zebra.demo.rfidreader.settings.ProfileContent.linkProfileUtil;

import android.content.Context;
import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.common.LinkProfileUtil;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.DYNAMIC_POWER_OPTIMIZATION;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.SESSION;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileFragmentRE40 extends BackPressedFragment  {

    private ProfileRecyclerViewAdapterRE40 profileViewAdapter;
    private ProfileRecyclerViewAdapter.OnListFragmentInteractionListener mListener;
    private String mProfileName;
    private int mPowerlevel;
    private int mLinkIndex;
    private int mSessionIndex;
    private Boolean mDPO;
    ArrayAdapter<String> linkAdapter;
    ArrayAdapter<CharSequence> sessionAdapter;

    public ProfileFragmentRE40() {
        // Required empty public constructor
    }

    public static ProfileFragmentRE40 newInstance() {
        ProfileFragmentRE40 fragment = new ProfileFragmentRE40();
        return fragment;
    }

    public static ProfileFragmentRE40 getInstance() {
        ProfileFragmentRE40 fragment = new ProfileFragmentRE40();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_list_re40, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.readerprofilelistRFD40);
        Context context = view.getContext();

        List<ProfileContentRE40> items = new ArrayList<ProfileContentRE40>();
        if (items.size() == 0) {
            items.add(new ProfileContentRE40("Fastest Read", "FASTEST_READ"));
            items.add(new ProfileContentRE40("Cycle Count", "CYCLE_COUNT"));
            items.add(new ProfileContentRE40("Max Range", "MAX_RANGE"));
            items.add(new ProfileContentRE40("Optimal Battery", "OPTIMAL_BATTERY"));
            items.add(new ProfileContentRE40("Balanced Performance", "BALANCED_PERFORMANCE"));
            items.add(new ProfileContentRE40("User Defined", "USER_DEFINED", "Custom profile \nUsed for custom requirement ", 240, 0, 1, true));
        }
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        profileViewAdapter = new ProfileRecyclerViewAdapterRE40(context, items);
        recyclerView.setAdapter(profileViewAdapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mLayoutManager.getOrientation());
        mDividerItemDecoration.setDrawable((getContext().getResources().getDrawable(R.drawable.profile_divider)));
        recyclerView.addItemDecoration(mDividerItemDecoration);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        linkProfileUtil = LinkProfileUtil.getInstance();

        linkAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_small_font, linkProfileUtil.getSimpleProfiles());
        linkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sessionAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.session_array, R.layout.custom_spinner_layout);
        sessionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        profileViewAdapter.mLinkProfileAdapter = linkAdapter;
        profileViewAdapter.mSessionAdapter = sessionAdapter;

        if (SettingsDetailActivity.mSettingOnFactory == false) {
            Button button = getActivity().findViewById(R.id.profilebutton);
            button.setVisibility(View.INVISIBLE);
        }
    }

    public void onBackPressed() {
        if (SettingsDetailActivity.mSettingOnFactory == true)
            return;
        saveDataToAntenna();
        if (getActivity() instanceof SettingsDetailActivity)
            ((SettingsDetailActivity) getActivity()).callBackPressed();
        else
            ((ActiveDeviceActivity) getActivity()).callBackPressed();
    }

    public void saveDataToAntenna() {
        String mRfidDefaultProfile = mConnectedReader.Config.getDefaultProfile();
        Application.cycleCountProfileData = null;
        if (mConnectedReader != null && mConnectedReader.isConnected()) {
            if (!(mIsInventoryRunning || isLocatingTag) && mConnectedReader.Config.Antennas != null) {
                if (Objects.equals(mRfidDefaultProfile, "USER_DEFINED")) {
                    for (ProfileRecyclerViewAdapterRE40.UserProfileViewHolder holder : profileViewAdapter.mUserProfileAdapter) {
                        if (!holder.mTextPower.getText().toString().equals(""))
                            mPowerlevel = Integer.parseInt(holder.mTextPower.getText().toString());
                        mLinkIndex = holder.mLinkProfileSpinner.getSelectedItemPosition();
                        mLinkIndex = LinkProfileUtil.getInstance().getSimpleProfileModeIndex(mLinkIndex);
                        mSessionIndex = holder.mSession.getSelectedItemPosition();
                        mDPO = holder.mDynamicPower.isChecked();
                    }
                    try {
                        Antennas.AntennaRfConfig antennaRfConfig;
                        antennaRfConfig = mConnectedReader.Config.Antennas.getAntennaRfConfig(1);
                        int maxPower = LinkProfileUtil.getInstance().getMaxPower();
                        if (mPowerlevel > maxPower) {
                            mPowerlevel = maxPower;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.invalid_antenna_power), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        int powerIndex = LinkProfileUtil.getInstance().getPowerLevelIndex(mPowerlevel);
                        antennaRfConfig.setTransmitPowerIndex(powerIndex);
                        if (antennaRfConfig.getrfModeTableIndex() != mLinkIndex) {
                            antennaRfConfig.setTari(0);
                            antennaRfConfig.setrfModeTableIndex(mLinkIndex);
                        }
                        mConnectedReader.Config.Antennas.setAntennaRfConfig(1, antennaRfConfig);
                        RFIDController.antennaRfConfig = antennaRfConfig;

                        // Singulation
                        Antennas.SingulationControl singulationControl;
                        singulationControl = mConnectedReader.Config.Antennas.getSingulationControl(1);
                        singulationControl.setSession(SESSION.GetSession(mSessionIndex));
                        mConnectedReader.Config.Antennas.setSingulationControl(1, singulationControl);
                        RFIDController.singulationControl = singulationControl;

                        // DPO
                        if (mDPO)
                            mConnectedReader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.ENABLE);
                        else
                            mConnectedReader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
                        dynamicPowerSettings = mConnectedReader.Config.getDPOState();

                    } catch (InvalidUsageException e) {
                        e.printStackTrace();
                    } catch (OperationFailureException e) {
                        e.printStackTrace();
                    }

                } else if (!Objects.equals(mRfidDefaultProfile, "USER_DEFINED")) {
                    try {
                        String mRfidProfile = mConnectedReader.Config.getRFIDProfile();
                        JSONArray jsonArrayValue = new JSONArray(mRfidProfile);
                        for (int i = 0; i < jsonArrayValue.length(); i++) {
                            JSONObject jsonObjectValue = jsonArrayValue.getJSONObject(i);
                            mProfileName = jsonObjectValue.getString("profile_name");
                            if (mProfileName.equals(mRfidDefaultProfile)) {
                                mPowerlevel = jsonObjectValue.getInt("tx") * 10;
                                mLinkIndex = jsonObjectValue.getInt("link");
                                mLinkIndex = LinkProfileUtil.getInstance().getSimpleProfileModeIndex(mLinkIndex);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        // Antenna
                        Antennas.AntennaRfConfig antennaRfConfig;
                        antennaRfConfig = mConnectedReader.Config.Antennas.getAntennaRfConfig(1);
                        int powerIndex = LinkProfileUtil.getInstance().getPowerLevelIndex(mPowerlevel);
                        antennaRfConfig.setTransmitPowerIndex(powerIndex);
                        if (antennaRfConfig.getrfModeTableIndex() != mLinkIndex) {
                            antennaRfConfig.setTari(0);
                            antennaRfConfig.setrfModeTableIndex(mLinkIndex);
                        }
                        mConnectedReader.Config.Antennas.setAntennaRfConfig(1, antennaRfConfig);
                        RFIDController.antennaRfConfig = antennaRfConfig;
                    } catch (InvalidUsageException e) {
                        e.printStackTrace();
                    } catch (OperationFailureException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}