package com.example.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapp.UtilisService.SharedPreferenceClass;
import com.example.myapp.UtilisService.UtilService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edUsername, edEmail, edPassword, edConfirmPassword;
    private Button btn;
    private TextView tv;
    private String username, email, password, confirmPassword;

    ProgressBar progressBar;
    UtilService utilService;
    SharedPreferenceClass sharedPreferenceClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edUsername = findViewById(R.id.editTextRegUsername);
        edEmail = findViewById(R.id.editTextRegEmail);
        edPassword = findViewById(R.id.editTextRegPassword);
        edConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        btn = findViewById(R.id.buttonRegister);
        tv = findViewById(R.id.textViewExistingUser);
        utilService = new UtilService();
        progressBar = findViewById(R.id.progress_Bar);
        sharedPreferenceClass = new SharedPreferenceClass(this);
        //when button is clicked, the function is executed
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                utilService.hideKeyboard(view, RegisterActivity.this);
                username = edUsername.getText().toString();
                email = edEmail.getText().toString();
                password = edPassword.getText().toString();
                confirmPassword = edConfirmPassword.getText().toString();

                if (validate(view)) {
                    registerUser(view);
                }
//                if(username.length()==0 || email.length()==0 || password.length()==0 || confirm.length()==0) {
//                    Toast.makeText(getApplicationContext(), "Please fill All details", Toast.LENGTH_SHORT).show();
//                }else{
//                    if(password.compareTo(confirm)==0){
//                        if(isValid(password)) {
//                            Toast.makeText(getApplicationContext(), "Record Inserted", Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
//
//                        }else{
//                            Toast.makeText(getApplicationContext(), "Password must contain at least 8 characters, having letter, digit and special symbol", Toast.LENGTH_SHORT).show();
//                        }
//
//                    }else{
//                        Toast.makeText(getApplicationContext(), "Password and Confirm password didn't match", Toast.LENGTH_SHORT).show();
//
//                    }
//                }

            }
        });

        //Redirect to new user registration when it is clicked
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });


    }

    private void registerUser(View view) {
        progressBar.setVisibility(View.VISIBLE);
        final HashMap<String, String> params = new HashMap<>();
        params.put("UserName", username);
        params.put("UserPassword", password);
        params.put("UserConfirmPassword", confirmPassword);
        params.put("UserEmail", email);

        String apiKey = "https://weposeapi-production.up.railway.app/registerUser";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        String token = response.getString("msg");
//                        sharedPreferenceClass.setValue_string("token", token);
                        Toast.makeText(RegisterActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                if(error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        JSONObject obj = new JSONObject(res);
                        Toast.makeText(RegisterActivity.this, obj.getString("msg"), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    } catch (JSONException | UnsupportedEncodingException je) {
                        je.printStackTrace();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return params;
            }
        };

        // set retry policy
        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        // request add
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);

    }

    public boolean validate(View view) {
        boolean isValid;

        if(!TextUtils.isEmpty(username)) {
            if(!TextUtils.isEmpty(email)) {
                if(!TextUtils.isEmpty(password)) {
                    isValid = true;
                } else {
                    utilService.showSnackBar(view, "Please enter password...");
                    isValid = false;
                }
            } else {
                utilService.showSnackBar(view, "Please enter email...");
                isValid = false;
            }
        } else {
            utilService.showSnackBar(view, "Please enter username...");
            isValid = false;
        }
        return isValid;
    }

//    public static boolean isValid(String passwordhere) {
//        int f1=0, f2=0, f3=0;
//        if (passwordhere.length() < 8){
//            return false;
//        } else {
//            for (int p = 0; p < passwordhere.length(); p++) {
//                if (Character.isLetter(passwordhere.charAt(p))) {
//                    f1=1;
//                }
//            }
//            for (int r = 0; r < passwordhere.length(); r++) {
//                if (Character.isDigit(passwordhere.charAt(r))) {
//                    f2=1;
//                }
//            }
//            for (int s = 0; s < passwordhere.length(); s++) {
//                char c = passwordhere.charAt(s);
//                if(c>=33&&c<=46||c==64){
//                    f3=1;
//                }
//            }
//            if(f1==1 && f2==1 && f3==1)
//                return true;
//            return false;
//        }
//    };

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        SharedPreferences wepose_pref = getSharedPreferences("user_wepose", MODE_PRIVATE);
//        if(wepose_pref.contains("token")) {
//            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
//            finish();
//        }
//    }

}