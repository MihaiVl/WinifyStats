package com.android.winifystats;

import android.*;
import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.winifystats.model.EmployeeCredentials;
import com.android.winifystats.model.TokenDTO;
import com.android.winifystats.model.WifiBroadcastReceiver;
import com.android.winifystats.model.WinifyEmployee;


import butterknife.BindView;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LoginActivity extends AppCompatActivity {


    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private WinifyEmployee service;
    private CheckBox savePasswordCheckBox;
    private ProgressDialog mProgressDialog;
    private Button mEmployeeSignInButton;
    private String mDefaultUsername = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        savePasswordCheckBox = (CheckBox) findViewById(R.id.save_password);


        checkCredentials();
        mProgressDialog = new ProgressDialog(this);

        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
        mLoginFormView = findViewById(R.id.login_form);
        mPasswordView = (EditText) findViewById(R.id.password);


        mEmployeeSignInButton = (Button) findViewById(R.id.employee_sign_in_button);
        mEmployeeSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();

            }
        });

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://192.168.3.145")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();
        service = retrofit.create(WinifyEmployee.class);

        populateCredentials();

    }


    private void populateCredentials() {

        savePasswordCheckBox.setChecked(This.getCache().isPasswordCheckBoxSaved());
        mUsernameView.setText(This.getCache().getUsername());
        if (This.getCache().isPasswordCheckBoxSaved()) {
            mPasswordView.setText(This.getCache().getPassword());

        }
    }


    private void checkCredentials() {
        if (This.getCache().isLogged()) {
            startMainActivity();
        }
    }


    private void attemptLogin() {


        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_email));
            focusView = mUsernameView;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            login(username, password);
        }

    }

    private boolean isUsernameValid(String username) {
        return username.length() > 0;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    public void login(final String username, final String password) {
        showProgress();

        EmployeeCredentials loginRequest = new EmployeeCredentials();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        service.postCredentials(loginRequest).enqueue(new Callback<TokenDTO>() {
            @Override
            public void onResponse(retrofit2.Call<TokenDTO> call, Response<TokenDTO> response) {
                hideProgress();
                if (response.isSuccessful()) {
                    if (response.body().getToken() != null) {
                        This.getCache().saveToken(response.body().getToken());
                        This.getCache().saveCredentials(savePasswordCheckBox.isChecked());
                        This.getCache().saveUsername(username);
                        if (This.getCache().isPasswordCheckBoxSaved()) {
                            This.getCache().savePassword(password);
                        } else {
                            This.getCache().savePassword(null);
                        }

                        startMainActivity();
                    }

                } else {
                    Toast.makeText(LoginActivity.this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<TokenDTO> call, Throwable t) {
                hideProgress();
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void startMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }


    public void showProgress() {
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.show();
    }

    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }


}

