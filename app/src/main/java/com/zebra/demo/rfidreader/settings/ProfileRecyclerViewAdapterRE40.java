package com.zebra.demo.rfidreader.settings;

import static com.zebra.demo.rfidreader.rfid.RFIDController.TAG;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedReader;
import static com.zebra.demo.rfidreader.settings.ProfileContent.linkProfileUtil;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.demo.R;
import com.zebra.demo.rfidreader.common.LinkProfileUtil;

import java.util.ArrayList;
import java.util.List;

public class ProfileRecyclerViewAdapterRE40 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<ProfileContentRE40> items;
    public ProfileContent.ProfilesItem mItem;
    public int selectedItemPosition = -1;
    public ArrayAdapter<String> mLinkProfileAdapter = null;
    public ArrayAdapter<CharSequence> mSessionAdapter;
    public ArrayList<UserProfileViewHolder> mUserProfileAdapter = new ArrayList<>();

    public ProfileRecyclerViewAdapterRE40(Context context, List<ProfileContentRE40> items) {
        this.context = context;
        this.items = items;
        String defaultProfile = mConnectedReader.Config.getDefaultProfile();
        for (int i = 0; i < items.size(); i++) {
            if (defaultProfile.equals(items.get(i).profileId)) {
                selectedItemPosition = i;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ProfileContentRE40.LayoutTwo:
                return new UserProfileViewHolder(LayoutInflater.from(context).inflate(R.layout.fragment_user_profile, parent, false));
            default:
                return new ProfileViewHolderRE40(LayoutInflater.from(context).inflate(R.layout.fragment_profile_re40, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ProfileViewHolderRE40) {
            ((ProfileViewHolderRE40)holder).setData(position, items.get(position));
        } else if(holder instanceof UserProfileViewHolder) {
            ((UserProfileViewHolder)holder).setData2(position, items.get(position));
            mUserProfileAdapter.add( (UserProfileViewHolder) holder);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getViewType();
    }

    public class ProfileViewHolderRE40 extends RecyclerView.ViewHolder {
        public final View mItemView;
        TextView contentView;
        public ProfileContentRE40 mItem;
        public int position = -1;

        public ProfileViewHolderRE40(View itemView) {
            super(itemView);
            mItemView = itemView;
            contentView = itemView.findViewById(R.id.content_re40);
            itemView.findViewById(R.id.titleLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int previousSelectedPosition = selectedItemPosition;
                    selectedItemPosition = position;
                    notifyItemChanged(previousSelectedPosition);
                    notifyItemChanged(selectedItemPosition);
                    boolean setDefaultProfile = mConnectedReader.Config.setRFIDProfile(mItem.profileId);
                    Log.d(TAG, "Default Profile is Set" + setDefaultProfile );
                }
            });
        }

        public void setData(int position, ProfileContentRE40 itemData) {
            this.position = position;
            this.mItem = itemData;
            contentView.setText(itemData.getContent());
            if (selectedItemPosition == position) {
                contentView.setTextColor(0xFFFF7043);
            } else {
                contentView.setTextColor(Color.BLACK);
            }
        }
    }

    public class UserProfileViewHolder extends RecyclerView.ViewHolder {
        public final View mItemView;
        public TextView mTextViewDetails;
        public EditText mTextPower;
        public Spinner mLinkProfileSpinner;
        public ProfileContentRE40 mItem;
        public Spinner mSession;
        public CheckBox mDynamicPower;
        public int position = -1;
        TextView contentView;
        private final LinearLayout detailLayout;

        public UserProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            mItemView = itemView;
            mTextViewDetails = itemView.findViewById(R.id.contentDetails);
            mTextPower = itemView.findViewById(R.id.powerLevelProfile);
            mLinkProfileSpinner = itemView.findViewById(R.id.linkProfile);
            contentView = itemView.findViewById(R.id.content2_re40);
            detailLayout = itemView.findViewById(R.id.userprofileSettings);
            mDynamicPower = itemView.findViewById(R.id.dynamicPower);
            mSession = itemView.findViewById(R.id.session);
            itemView.findViewById(R.id.titleLayout).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    contentView.setTextColor(0xFFFF7043);
                    int previousSelectedPosition = selectedItemPosition;
                    selectedItemPosition = position;
                    notifyItemChanged(previousSelectedPosition);
                    mConnectedReader.Config.setRFIDProfile(mItem.profileId);
                    detailLayout.setVisibility(View.VISIBLE);
                    // show current one
                    linkProfileUtil = LinkProfileUtil.getInstance();
                    mTextViewDetails.setText(mItem.contentDetails);
                    mTextPower.setText(String.valueOf(mItem.powerLevel));
                    mLinkProfileSpinner.setAdapter(mLinkProfileAdapter);
                    mSession.setAdapter(mSessionAdapter);
                    mItem.LinkProfileIndex = LinkProfileUtil.getInstance().getSimpleProfileModeIndex(mLinkProfileSpinner.getSelectedItemPosition());
                    mDynamicPower.setChecked(items.get(position).DPO_On);
                    mSession.setSelection(items.get(position).SessionIndex);
                }
            });

        }

        public void setData2(int position, ProfileContentRE40 itemData) {
            this.position = position;
            this.mItem = itemData;
            contentView.setText(itemData.getContent());
            if (selectedItemPosition == position) {
                contentView.setTextColor(0xFFFF7043);
                detailLayout.setVisibility(View.VISIBLE);
                linkProfileUtil = LinkProfileUtil.getInstance();
                mTextViewDetails.setText(mItem.contentDetails);
                mTextPower.setText(String.valueOf(mItem.powerLevel));
                mLinkProfileSpinner.setAdapter(mLinkProfileAdapter);
                mSession.setAdapter(mSessionAdapter);
                mItem.LinkProfileIndex = LinkProfileUtil.getInstance().getSimpleProfileModeIndex(mLinkProfileSpinner.getSelectedItemPosition());
                mDynamicPower.setChecked(items.get(position).DPO_On);
                mSession.setSelection(items.get(position).SessionIndex);
            } else {
                detailLayout.setVisibility(View.GONE);
                contentView.setTextColor(Color.BLACK);
            }
        }
    }
}
