package com.zebra.demo.rfidreader;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.zebra.demo.DeviceDiscoverActivity;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.common.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Spinner companySpinner;
    private Button loginButton;
    private Button refreshButton;
    private JSONArray companiesArray;  // To store the company list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        companySpinner = findViewById(R.id.company_spinner);
        loginButton = findViewById(R.id.login_button);
        refreshButton = findViewById(R.id.refresh_button);

        // Initially, the company spinner is disabled
        companySpinner.setEnabled(false);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginButton.getText().equals("Check Credentials")) {
                    String email = emailEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    new CheckCredentialsTask().execute(email, password);
                } else {
                    loginToCompany();
                }
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLoginState();
            }
        });
    }

    private class CheckCredentialsTask extends AsyncTask<String, Void, Boolean> {

        private String result;

        @Override
        protected Boolean doInBackground(String... params) {
            String email = params[0];
            String password = params[1];

            try {
                URL url = new URL("http://10.0.2.2:8000/rfid/login");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true);

                String jsonInputString = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}";

                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = urlConnection.getResponseCode();

                if (code == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    result = convertStreamToString(inputStream);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                Log.e("LoginActivity", "Error logging in", e);
                return false;
            }
        }


        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                try {
                    Log.d("LoginActivity", "Response received: " + result);

                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.getString("status");

                    if ("success".equals(status)) {
                        // Save user data in the session
                        JSONObject user = jsonResponse.getJSONObject("user");
                        SessionManager session = new SessionManager(LoginActivity.this);
                        session.setUser(user);

                        // Now proceed with populating the company spinner
                        companiesArray = jsonResponse.getJSONArray("companies");
                        populateCompanySpinner(companiesArray);

                        // Change button text to "Log In to Company"
                        loginButton.setText("Log In to Company");

                        // Disable email and password fields
                        emailEditText.setEnabled(false);
                        passwordEditText.setEnabled(false);

                        Log.d("LoginActivity", "Companies populated and spinner shown.");
                    } else {
                        String message = jsonResponse.getString("message");
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Error processing login data.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }

        }



        private String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }


    private void populateCompanySpinner(JSONArray companies) throws JSONException {
        List<String> companyNames = new ArrayList<>();

        // Log the companies array to ensure it's being processed
        Log.d("LoginActivity", "Populating spinner with companies: " + companies.toString());

        for (int i = 0; i < companies.length(); i++) {
            JSONObject company = companies.getJSONObject(i);
            JSONObject userCompany = company.optJSONObject("user_company");

            if (userCompany != null) {
                String companyName = userCompany.optString("name", "Unknown Company");
                companyNames.add(companyName);
            } else {
                Log.d("LoginActivity", "Company at index " + i + " has no associated userCompany.");
            }
        }

        if (!companyNames.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, companyNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            companySpinner.setAdapter(adapter);
            companySpinner.setVisibility(View.VISIBLE);  // Show the spinner
            companySpinner.setEnabled(true);  // Enable the spinner
        } else {
            Log.d("LoginActivity", "No companies available to populate the spinner.");
            Toast.makeText(this, "No valid companies found for this user.", Toast.LENGTH_SHORT).show();
        }
    }


    private void loginToCompany() {
        try {
            // Get the selected position from the spinner
            int selectedPosition = companySpinner.getSelectedItemPosition();

            // Ensure a company is selected
            if (selectedPosition == AdapterView.INVALID_POSITION) {
                Toast.makeText(this, "Please select a company.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the selected company based on the position
            JSONObject selectedCompany = companiesArray.getJSONObject(selectedPosition);

            // Log the selected company for debugging purposes
            Log.d("LoginActivity", "Selected company: " + selectedCompany.toString());

            // Save login state and selected company in the session
            SessionManager session = new SessionManager(LoginActivity.this);
            session.setLogin(true);

            // Save the user data if it's already stored in the session (from the initial login step)
            if (session.getUser() == null) {
                Toast.makeText(this, "User data missing. Please check your login flow.", Toast.LENGTH_SHORT).show();
                return;
            }

            session.setCompanies(companiesArray);
            session.setSelectedCompany(selectedCompany);


            // Navigate to the main app
            navigateToMainApp();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("LoginActivity", "JSONException: " + e.getMessage());
            Toast.makeText(this, "Error selecting company. Please try again.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("LoginActivity", "Unexpected error: " + e.getMessage());
            Toast.makeText(this, "Unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshLoginState() {
        try {
            // Log that the refresh process has started

            // Clear the session data
            SessionManager session = new SessionManager(LoginActivity.this);
            session.setLogin(false);
            session.setUser(null);
            session.setCompanies(null);
            session.setSelectedCompany(null);

            // Clear the input fields
            emailEditText.setText("");
            passwordEditText.setText("");

            // Reset the company spinner
            companySpinner.setAdapter(null);
            companySpinner.setVisibility(View.GONE);
            companySpinner.setEnabled(false);

            // Reset the button text
            loginButton.setText("Check Credentials");

            // Re-enable the email and password fields
            emailEditText.setEnabled(true);
            passwordEditText.setEnabled(true);

            // Show a toast to confirm the refresh action
            Toast.makeText(LoginActivity.this, "Login form reset.", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(LoginActivity.this, "Error resetting the form. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }


    private void navigateToMainApp() {
        Intent intent = new Intent(LoginActivity.this, DeviceDiscoverActivity.class);
        startActivity(intent);
        finish();
    }
}
