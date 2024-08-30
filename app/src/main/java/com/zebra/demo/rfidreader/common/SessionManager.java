package com.zebra.demo.rfidreader.common;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SessionManager {

    // Name of the SharedPreference file
    private static final String PREF_NAME = "login_pref";

    // Keys for storing data
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER = "user";
    private static final String KEY_COMPANIES = "companies";
    private static final String KEY_SELECTED_COMPANY = "selected_company";
    private static final String KEY_LAST_ACTIVITY_TIME = "last_activity_time";
    private static final long TIMEOUT_DURATION = 1 * 60 * 1000;  // 5 minutes in milliseconds


    // SharedPreferences and Editor instances
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    // Constructor
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Method to set the login state
    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.commit();
    }

    // Method to get the login state
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Method to save user data
    public void setUser(JSONObject user) {
        if (user == null) {
            editor.remove(KEY_USER);  // Remove the user data if null
        } else {
            editor.putString(KEY_USER, user.toString());
        }
        editor.commit();
    }

    // Method to get user data
    public JSONObject getUser() {
        String userString = pref.getString(KEY_USER, null);
        if (userString != null) {
            try {
                return new JSONObject(userString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Method to save companies data
    public void setCompanies(JSONArray companies) {
        if (companies == null) {
            editor.remove(KEY_COMPANIES);  // Remove the companies data if null
        } else {
            editor.putString(KEY_COMPANIES, companies.toString());
        }
        editor.commit();
    }


    // Method to get companies data
    public JSONArray getCompanies() {
        String companiesString = pref.getString(KEY_COMPANIES, null);
        if (companiesString != null) {
            try {
                return new JSONArray(companiesString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Method to save the selected company
    public void setSelectedCompany(JSONObject selectedCompany) {
        if (selectedCompany == null) {
            editor.remove(KEY_SELECTED_COMPANY);  // Remove the selected company data if null
        } else {
            editor.putString(KEY_SELECTED_COMPANY, selectedCompany.toString());
        }
        editor.commit();
    }


    // Method to get the selected company
    public JSONObject getSelectedCompany() {
        String companyString = pref.getString(KEY_SELECTED_COMPANY, null);
        if (companyString != null) {
            try {
                return new JSONObject(companyString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void updateLastActivityTime() {
        long currentTime = System.currentTimeMillis();
        editor.putLong(KEY_LAST_ACTIVITY_TIME, currentTime);
        editor.commit();

    }

    public boolean isSessionExpired() {
        long lastActivityTime = pref.getLong(KEY_LAST_ACTIVITY_TIME, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastActivityTime) > TIMEOUT_DURATION;
    }

}
