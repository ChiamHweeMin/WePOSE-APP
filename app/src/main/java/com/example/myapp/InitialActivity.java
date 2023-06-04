package com.example.myapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class InitialActivity extends AppCompatActivity {

    Toolbar toolbarInitial;

    private TextView meanPitchTextView, meanRollTextView, textViewLogOut, textViewTime;

    private Button buttonInitial;
    private ProgressBar progressBar;
    UtilService utilService;

    float meanPitch, meanRoll;
    String userEmail;

    SharedPreferenceClass sharedPreferenceClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        toolbarInitial = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbarInitial);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        meanPitchTextView = findViewById(R.id.editTextMeanPitch);
        meanRollTextView = findViewById(R.id.editTextMeanRoll);
        buttonInitial = findViewById(R.id.buttonInitial);
        progressBar = findViewById(R.id.progress_Bar);
        textViewLogOut = findViewById(R.id.textViewLogOut);
        textViewTime = findViewById(R.id.textViewTime);

        utilService = new UtilService();
        sharedPreferenceClass = new SharedPreferenceClass(this);
        SharedPreferences wepose_pref = this.getSharedPreferences("user_wepose", MODE_PRIVATE);
        userEmail = wepose_pref.getString("useremail", "");

        meanPitch = wepose_pref.getFloat("meanPitch", 0.0f);
        meanRoll = wepose_pref.getFloat("meanRoll", 0.0f);

        meanPitchTextView.setText(String.valueOf(meanPitch));
        meanRollTextView.setText(String.valueOf(meanRoll));
        buttonInitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                utilService.hideKeyboard(view, InitialActivity.this);
                initialization(view);
                Toast.makeText(InitialActivity.this, "Initialization...", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void initialization(View view) {
        textViewTime.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        String apiKey = "https://weposeapi-production.up.railway.app/WEPOSE/initSitPosture/"+userEmail;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(InitialActivity.this, "Please log out and log in again", Toast.LENGTH_LONG).show();
                        textViewTime.setVisibility(View.GONE);
                        textViewLogOut.setVisibility(View.VISIBLE);
                    }

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
                        Toast.makeText(InitialActivity.this, obj.getString("error"), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    } catch (JSONException | UnsupportedEncodingException je) {
                        je.printStackTrace();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // set retry policy
        int socketTime = 10000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        // request add
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}