package com.example.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class EditUserPasswordActivity extends AppCompatActivity {

    TextInputEditText etOldPassword, etNewPassword, etNewConfirmPassword;
    SharedPreferenceClass sharedPreferenceClass;
    UtilService utilService;
    String userEmail, userToken, userOldPassword, userNewPassword, userNewConfirmPassword;
    Button buttonSave, buttonCancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_password);

        sharedPreferenceClass = new SharedPreferenceClass(this);
        utilService = new UtilService();
        SharedPreferences wepose_pref = getSharedPreferences("user_wepose", MODE_PRIVATE);
        userEmail = wepose_pref.getString("useremail", "");
        userToken = wepose_pref.getString("token", "");
        etOldPassword = findViewById(R.id.editTextOldPassword);
        etNewPassword = findViewById(R.id.editTextNewPassword);
        etNewConfirmPassword = findViewById(R.id.editTextNewConfirmPassword);

        buttonSave = findViewById(R.id.buttonSaveUpdatePassword);
        buttonCancel = findViewById(R.id.buttonCancelUpdatePassword);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                utilService.hideKeyboard(view, EditUserPasswordActivity.this);
                userOldPassword = etOldPassword.getText().toString();
                userNewPassword = etNewPassword.getText().toString();
                userNewConfirmPassword = etNewConfirmPassword.getText().toString();

                if(validate(view)) {
                    editUserPassword(view);
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int value = 1;
                Intent i = new Intent(EditUserPasswordActivity.this, MainActivity.class);
                i.putExtra("key", value);
                startActivity(i);
            }
        });
    }

    public void editUserPassword(View view) {
        final HashMap<String, String> params = new HashMap<>();
        params.put("UserEmail", userEmail);
        params.put("UserOldPassword", userOldPassword);
        params.put("UserNewPassword", userNewPassword);
        params.put("UserNewConfirmPassword", userNewConfirmPassword);

        String apiKey = "https://weposeapi-production.up.railway.app/UserProfile/UpdateUserPassword";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH,
                apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        String token = response.getString("msg");
                        Toast.makeText(EditUserPasswordActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                    int value = 1;
                    Intent i = new Intent(EditUserPasswordActivity.this, MainActivity.class);
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
                        Toast.makeText(EditUserPasswordActivity.this, obj.getString("msg"), Toast.LENGTH_SHORT).show();
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

        if(!TextUtils.isEmpty(userOldPassword)) {
            if(!TextUtils.isEmpty(userNewPassword)) {
                if(!TextUtils.isEmpty(userOldPassword)) {
                    isValid = true;
                } else {
                    utilService.showSnackBar(view, "Please enter Confirm Password...");
                    isValid = false;
                }
            } else {
                utilService.showSnackBar(view, "Please enter New Password...");
                isValid = false;
            }
        } else {
            utilService.showSnackBar(view, "Please enter Old Password...");
            isValid = false;
        }
        return isValid;
    }
}