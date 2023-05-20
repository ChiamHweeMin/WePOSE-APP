package com.example.myapp;

import android.content.DialogInterface;
import android.icu.text.SimpleDateFormat;
import android.icu.text.UFormat;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private Button mStartButton;
    private Button mStopButton;
    private Button mResetButton;
    private Chronometer mChronometer;

    private TextView xAccTextView, yAccTextView, zAccTextView;
    private TextView xGyrTextView, yGyrTextView, zGyrTextView;

    Float accX, accY, accZ, gyroX, gyroY, gyroZ;
    private long lastPause;

    private Timer timer; // Declare a Timer variable

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mStartButton = v.findViewById(R.id.startButton);
        mStopButton = v.findViewById(R.id.stopButton);
        mResetButton = v.findViewById(R.id.resetButton);
        mChronometer = v.findViewById(R.id.chronometer);

        xAccTextView = v.findViewById(R.id.xAccTextView);
        yAccTextView = v.findViewById(R.id.yAccTextView);
        zAccTextView = v.findViewById(R.id.zAccTextView);
        xGyrTextView = v.findViewById(R.id.xGyrTextView);
        yGyrTextView = v.findViewById(R.id.yGyrTextView);
        zGyrTextView = v.findViewById(R.id.zGyrTextView);


        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChronometer.setVisibility(View.VISIBLE);
                long base = SystemClock.elapsedRealtime();
                if (lastPause != 0) {
                    mChronometer.setBase(mChronometer.getBase() + base - lastPause);

                }else {
                    mChronometer.setBase(base);
                }
                mChronometer.start();
                mStartButton.setEnabled(false);
                mStopButton.setEnabled(true);
                // Start a Timer to schedule periodic updates
                timer = new Timer();
                TimerTask updateTask = new TimerTask() {
                    @Override
                    public void run() {
                        // Fetch data from the API and update the TextViews
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                receiveData(v);
                                xAccTextView.setText(String.valueOf(accX));
                                yAccTextView.setText(String.valueOf(accY));
                                zAccTextView.setText(String.valueOf(accZ));
                                xGyrTextView.setText(String.valueOf(gyroX));
                                yGyrTextView.setText(String.valueOf(gyroY));
                                zGyrTextView.setText(String.valueOf(gyroZ));
                            }
                        });
                    }
                };

                // Schedule the updates to occur every second (1000 milliseconds)
                timer.scheduleAtFixedRate(updateTask, 0, 1000);

            }
        });


        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChronometer.setVisibility(View.VISIBLE);
                lastPause = SystemClock.elapsedRealtime();
                mChronometer.stop();
                mStopButton.setEnabled(false);
                mStartButton.setEnabled(true);

                // Cancel the Timer to stop periodic updates
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }
        });

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChronometer.setVisibility(View.GONE);
                mChronometer.stop();
                mChronometer.setBase(SystemClock.elapsedRealtime());
                lastPause = 0;
                mStartButton.setEnabled(true);
                mStopButton.setEnabled(false);
            }
        });

        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                onChronometerTickHandler();
            }
        });

        return v;
    }

    private void onChronometerTickHandler()  {
        long delta = SystemClock.elapsedRealtime() - mChronometer.getBase();

        int hours = (int) ((delta / 1000) / 3600);
        int minutes = (int) (((delta / 1000) / 60) % 60);
        int seconds = (int) ((delta / 1000) % 60);

        String customText = String.format("%02d",hours) + ":" + String.format("%02d",minutes) + ":" + String.format("%02d",seconds);

        mChronometer.setText(customText);
    }

    public void receiveData(View view) {

        String apiKey = "https://weposeapi-production.up.railway.app/WEPOSE/SendDataIMU";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        Toast.makeText(getActivity(), "Getting data...", Toast.LENGTH_SHORT).show();
                        accX = Float.parseFloat(response.getString("accel_x"));
                        accY = Float.parseFloat(response.getString("accel_y"));
                        accZ = Float.parseFloat(response.getString("accel_z"));
                        gyroX = Float.parseFloat(response.getString("gyro_x"));
                        gyroY = Float.parseFloat(response.getString("gyro_y"));
                        gyroZ = Float.parseFloat(response.getString("gyro_z"));

                    }

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
                        Toast.makeText(getActivity(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException | UnsupportedEncodingException je) {
                        je.printStackTrace();
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
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }
}