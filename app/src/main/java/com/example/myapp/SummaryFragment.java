package com.example.myapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.myapp.adapters.SummaryAdapter;
import com.example.myapp.interfaces.RecyclerViewClickListener;
import com.example.myapp.models.SummaryModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SummaryFragment extends Fragment implements RecyclerViewClickListener{
    SharedPreferenceClass sharedPreferenceClass;
    String userEmail;
    SummaryAdapter summaryAdapter;
    RecyclerView recyclerView;
    TextView empty_tv;
    ProgressBar progressBar;
    ArrayList<SummaryModel> arrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        sharedPreferenceClass = new SharedPreferenceClass(getContext());
        SharedPreferences wepose_pref = this.getActivity().getSharedPreferences("user_wepose", MODE_PRIVATE);
        userEmail = wepose_pref.getString("useremail", "");

        recyclerView = view.findViewById(R.id.recycle_view);
        empty_tv = view.findViewById(R.id.empty_tv);
        progressBar = view.findViewById(R.id.progress_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        getDateUsage(view);

        return view;
    }


    public void getDateUsage(View view) {
        arrayList = new ArrayList<>();
        Toast.makeText(getActivity(), "Getting data...", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.VISIBLE);
        String apiKey = "https://weposeapi-production.up.railway.app/WEPOSE/dataDateUsage/"+userEmail;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {

                        JSONArray jsonArray = response.getJSONArray("data");
                        if(jsonArray.length() == 0) {
                            empty_tv.setVisibility(View.VISIBLE);
                        } else {
                            empty_tv.setVisibility(View.GONE);
                            for(int i =0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                SummaryModel summaryModel = new SummaryModel(
                                        jsonObject.getString("date"),
                                        formatElapsedTime(jsonObject.getLong("ElapsedTime")),
                                        jsonObject.getInt("SlouchCount")
                                );
                                arrayList.add(summaryModel);
                            }
                            summaryAdapter = new SummaryAdapter(getActivity(), arrayList, SummaryFragment.this);
                            recyclerView.setAdapter(summaryAdapter);
                        }
                        progressBar.setVisibility((View.GONE));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility((View.GONE));
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

    public String formatElapsedTime(long elapsedMillis) {
        long seconds = (elapsedMillis / 1000) % 60;
        long minutes = (elapsedMillis / (1000 * 60)) % 60;
        long hours = (elapsedMillis / (1000 * 60 * 60)) % 24;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onLongItemClick(int position) {

    }

    @Override
    public void onEditButtonClick(int position) {

    }

    @Override
    public void onDeleteButtonClick(int position) {

    }

    @Override
    public void onDoneButtonClick(int position) {

    }
}