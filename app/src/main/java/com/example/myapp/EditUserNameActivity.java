package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

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
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapp.databinding.ActivityEditUserNameBinding;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class EditUserNameActivity extends AppCompatActivity {

    TextInputEditText etUsername;
    SharedPreferenceClass sharedPreferenceClass;
    UtilService utilService;
    String userName, userEmail, userToken;
    Button buttonSave, buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_name);

        SharedPreferences wepose_pref = getSharedPreferences("user_wepose", MODE_PRIVATE);
        userName = wepose_pref.getString("username", "");
        userEmail = wepose_pref.getString("useremail", "");
        userToken = wepose_pref.getString("token", "");
        etUsername = findViewById(R.id.editTextUpdateUsername);
        etUsername.setText(userName);
        sharedPreferenceClass = new SharedPreferenceClass(this);
        utilService = new UtilService();

        buttonSave = findViewById(R.id.buttonSaveUserName);
        buttonCancel = findViewById(R.id.buttonCancelUserName);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                utilService.hideKeyboard(view, EditUserNameActivity.this);
                userName = etUsername.getText().toString();

                if(validate(view)) {
                    editUserName(view);
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int value = 1;
                Intent i = new Intent(EditUserNameActivity.this, MainActivity.class);
                i.putExtra("key", value);
                startActivity(i);
            }
        });
    }
    public void editUserName(View view) {
        final HashMap<String, String> params = new HashMap<>();
        params.put("UserEmail", userEmail);
        params.put("UserName", userName);

        String apiKey = "https://weposeapi-production.up.railway.app/UserProfile/UpdateUserName";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH,
                apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        String token = response.getString("msg");
                        Toast.makeText(EditUserNameActivity.this, token, Toast.LENGTH_SHORT).show();
                        String nameUpdate = response.getString("UserName");
                        sharedPreferenceClass.setValue_string("username", nameUpdate);
                    }
                    int value = 1;
                    Intent i = new Intent(EditUserNameActivity.this, MainActivity.class);
                    i.putExtra("key", value);
                    startActivity(i);
                } catch (JSONException e) {
                    e.printStackTrace();
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
                        Toast.makeText(EditUserNameActivity.this, obj.getString("msg"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException | UnsupportedEncodingException je) {
                        je.printStackTrace();
                    }
                }
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer "+ userToken);
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

        if(!TextUtils.isEmpty(userName)) {
            isValid = true;
        } else {
            utilService.showSnackBar(view, "Please enter username...");
            isValid = false;
        }
        return isValid;
    }

}