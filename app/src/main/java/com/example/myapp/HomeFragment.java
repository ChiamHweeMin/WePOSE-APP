package com.example.myapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    LineChart mLineChartPitch, mLineChartRoll;
    private Button mStartButton;
    private Button mStopButton;
    private Button mResetButton;
    private Chronometer mChronometer;
    private TextView pitchTextView, rollTextView, editTextSlouchCount;
    Float pitch, roll, meanPitch, meanRoll;
    int prediction;

    int x = 0;
    private long lastPause;
    SharedPreferenceClass sharedPreferenceClass;
    String userEmail;

    private Timer timer; // Declare a Timer variable

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPreferenceClass = new SharedPreferenceClass(getActivity());
        SharedPreferences wepose_pref = this.getActivity().getSharedPreferences("user_wepose", MODE_PRIVATE);
        userEmail = wepose_pref.getString("useremail", "");
        meanPitch = wepose_pref.getFloat("meanPitch", 0.0f);
        meanRoll = wepose_pref.getFloat("meanRoll", 0.0f);

        mLineChartPitch = (LineChart) v.findViewById(R.id.lineChartPitch);
        ArrayList<Entry> dataValues = new ArrayList<>();
        mLineChartPitch.setDrawBorders(true);
        mLineChartPitch.setBorderColor(Color.RED);

        LineDataSet lineDataSet = new LineDataSet(dataValues, "Pitch");
        lineDataSet.setLineWidth(4);
        lineDataSet.setColor(Color.RED);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircleHole(false);

        // Add the threshold lines
        LimitLine upperThresholdLinePitch = new LimitLine(meanPitch+10, "upperlimit");
        upperThresholdLinePitch.setLineColor(Color.BLACK);
        upperThresholdLinePitch.setLineWidth(2f);
        upperThresholdLinePitch.enableDashedLine(10f, 10f, 0f); // Add dashes to the line
        upperThresholdLinePitch.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        upperThresholdLinePitch.setTextSize(12f);

        LimitLine lowerThresholdLinePitch = new LimitLine(meanPitch-10, "lowerlimit");
        lowerThresholdLinePitch.setLineColor(Color.BLACK);
        lowerThresholdLinePitch.setLineWidth(2f);
        lowerThresholdLinePitch.enableDashedLine(10f, 10f, 0f); // Add dashes to the line
        lowerThresholdLinePitch.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        lowerThresholdLinePitch.setTextSize(12f);

        YAxis yAxisPitch = mLineChartPitch.getAxisLeft();
        yAxisPitch.setAxisMinimum(meanPitch-20);  // Set the minimum value for y-axis
        yAxisPitch.setAxisMaximum(meanPitch+20);  // Set the maximum value for y-axis
        yAxisPitch.setDrawAxisLine(true);
        yAxisPitch.setDrawGridLines(false);
        YAxis rightYAxisPitch = mLineChartPitch.getAxisRight();
        rightYAxisPitch.setEnabled(false);

        // Add the threshold lines to the chart
        yAxisPitch.addLimitLine(upperThresholdLinePitch);
        yAxisPitch.addLimitLine(lowerThresholdLinePitch);

        XAxis xAxisPitch = mLineChartPitch.getXAxis();
        xAxisPitch.setDrawAxisLine(true);
        xAxisPitch.setDrawGridLines(false);
        xAxisPitch.setDrawLabels(false); // Show labels for bottom x-axis only

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);

        LineData data = new LineData(dataSets);
        mLineChartPitch.setData(data);
        mLineChartPitch.invalidate();

        mLineChartRoll = (LineChart) v.findViewById(R.id.lineChartRoll);
        ArrayList<Entry> dataValues2 = new ArrayList<>();
        mLineChartRoll.setDrawBorders(true);
        mLineChartRoll.setBorderColor(Color.BLUE);

        LineDataSet lineDataSet2 = new LineDataSet(dataValues2, "Roll");
        lineDataSet2.setLineWidth(4);
        lineDataSet2.setColor(Color.BLUE);
        lineDataSet2.setDrawValues(false);
        lineDataSet2.setDrawCircleHole(true);

        LimitLine upperThresholdLineRoll = new LimitLine(meanRoll+10, "upperlimit");
        upperThresholdLineRoll.setLineColor(Color.BLACK);
        upperThresholdLineRoll.setLineWidth(2f);
        upperThresholdLineRoll.enableDashedLine(10f, 10f, 0f); // Add dashes to the line
        upperThresholdLineRoll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        upperThresholdLineRoll.setTextSize(12f);

        LimitLine lowerThresholdLineRoll = new LimitLine(meanRoll-10, "lowerlimit");
        lowerThresholdLineRoll.setLineColor(Color.BLACK);
        lowerThresholdLineRoll.setLineWidth(2f);
        lowerThresholdLineRoll.enableDashedLine(10f, 10f, 0f); // Add dashes to the line
        lowerThresholdLineRoll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        lowerThresholdLineRoll.setTextSize(12f);

        YAxis yAxisRoll = mLineChartRoll.getAxisLeft();
        yAxisRoll.setAxisMinimum(meanRoll-20);  // Set the minimum value for y-axis
        yAxisRoll.setAxisMaximum(meanRoll+20);  // Set the maximum value for y-axis
        yAxisRoll.setDrawAxisLine(true);
        yAxisRoll.setDrawGridLines(false);
        YAxis rightYAxisRoll = mLineChartRoll.getAxisRight();
        rightYAxisRoll.setEnabled(false);

        // Add the threshold lines to the chart
        yAxisRoll.addLimitLine(upperThresholdLineRoll);
        yAxisRoll.addLimitLine(lowerThresholdLineRoll);

        XAxis xAxisRoll = mLineChartRoll.getXAxis();
        xAxisRoll.setDrawAxisLine(true);
        xAxisRoll.setDrawGridLines(false);
        xAxisRoll.setDrawLabels(false); // Show labels for bottom x-axis only

        List<ILineDataSet> dataSets2 = new ArrayList<>();
        dataSets2.add(lineDataSet2);

        LineData data2 = new LineData(dataSets2);
        mLineChartRoll.setData(data2);
        mLineChartRoll.invalidate();

        mStartButton = v.findViewById(R.id.startButton);
        mStopButton = v.findViewById(R.id.stopButton);
        mResetButton = v.findViewById(R.id.resetButton);
        mChronometer = v.findViewById(R.id.chronometer);

        pitchTextView = v.findViewById(R.id.pitchTextView);
        rollTextView = v.findViewById(R.id.rollTextView);
        editTextSlouchCount = v.findViewById(R.id.editTextSlouchCount);

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
                                pitchTextView.setText(String.valueOf(pitch));
                                rollTextView.setText(String.valueOf(roll));
                                editTextSlouchCount.setText(String.valueOf(prediction));

                                if (pitch != null && roll != null) {
                                    Entry entry1 = new Entry(x, pitch);
                                    lineDataSet.addEntry(entry1);
                                    data.notifyDataChanged();
                                    mLineChartPitch.notifyDataSetChanged();
                                    mLineChartPitch.setVisibleXRangeMaximum(10); // Display only 10 data points
                                    mLineChartPitch.moveViewToX(lineDataSet.getEntryCount() - 10); // Scroll to the latest data point

                                    Entry entry2 = new Entry(x, roll);
                                    lineDataSet2.addEntry(entry2);
                                    data2.notifyDataChanged();
                                    mLineChartRoll.notifyDataSetChanged();
                                    mLineChartRoll.setVisibleXRangeMaximum(10); // Display only 10 data points
                                    mLineChartRoll.moveViewToX(lineDataSet2.getEntryCount() - 10); // Scroll to the latest data point
                                }

                                x++;
                            }
                        });
                    }
                };

                // Schedule the updates to occur every second (500 milliseconds)
                timer.scheduleAtFixedRate(updateTask, 0, 500);

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

                Date currentDate = new Date();
                SimpleDateFormat sdf = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                }
                String formattedDate = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    formattedDate = sdf.format(currentDate);
                }

                // Get the time from the chronometer
                long elapsedMillis = SystemClock.elapsedRealtime() - mChronometer.getBase();
//                String elapsedMillisString = String.valueOf(elapsedMillis);

                // Get the slouch count from the editTextSlouchCount
                int slouchCount = Integer.parseInt(editTextSlouchCount.getText().toString());

                sendDataToAPI(formattedDate, elapsedMillis, slouchCount);

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
                editTextSlouchCount.setText(String.valueOf(0));
                prediction = 0;
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

    public void sendDataToAPI(String date, long time, int count) {
        final HashMap<String, String> params = new HashMap<>();
        params.put("date", date);
        params.put("ElapsedTime", String.valueOf(time));
        params.put("SlouchCount", String.valueOf(count));

        String apiKey = "https://weposeapi-production.up.railway.app/WEPOSE/sendSlouchCount/"+userEmail;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        Toast.makeText(getActivity(), "Success send data", Toast.LENGTH_SHORT).show();
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
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // set retry policy
        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        // request add
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }
    public void receiveData(View view) {

        String apiKey = "https://weposeapi-production.up.railway.app/WEPOSE/predictSitPosture/"+userEmail;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        pitch = (float) response.getDouble("pitch");
                        roll = (float) response.getDouble("roll");
                        prediction += response.getInt("prediction");
                        meanPitch = (float) response.getDouble("meanPitch");
                        meanRoll = (float) response.getDouble("meanRoll");
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
                        Toast.makeText(getActivity(), obj.getString("error"), Toast.LENGTH_SHORT).show();
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

    private  class MyAxisValueFormatter extends ValueFormatter implements IAxisValueFormatter{
        public String getFormattedValue(float value, AxisBase axis) {
            axis.setLabelCount(3, true);    // only 3 label
            return "Day " +value;
        }
    }

}