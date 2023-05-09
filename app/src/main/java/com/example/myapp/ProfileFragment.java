package com.example.myapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapp.UtilisService.SharedPreferenceClass;

import android.content.Intent;

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

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapp.databinding.ActivityEditUserNameBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    TextView tvUseremail, tvAlertDeleteAccount;
    EditText edUsername;
    String userName, userEmail, userToken;
    UtilService utilService;

    Button buttonEditUsername, buttonEditPassword, buttonDeleteAccount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        SharedPreferences wepose_pref = this.getActivity().getSharedPreferences("user_wepose", MODE_PRIVATE);
        userName = wepose_pref.getString("username", "");
        userEmail = wepose_pref.getString("useremail", "");
        userToken = wepose_pref.getString("token", "");
        tvUseremail = v.findViewById(R.id.textUseremail);
        edUsername = v.findViewById(R.id.editTextUsername);
        buttonEditUsername = v.findViewById(R.id.buttonEditUsername);
        buttonEditPassword = v.findViewById(R.id.buttonEditPassword);
        buttonDeleteAccount = v.findViewById(R.id.buttonDeleteUser);
        tvAlertDeleteAccount = v.findViewById(R.id.dialogBoxAlertDeleteAccount);
        tvUseremail.setText(userEmail);
        edUsername.setText(userName);
        utilService = new UtilService();

        buttonEditUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), EditUserNameActivity.class));
            }
        });

        buttonEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), EditUserPasswordActivity.class));
            }
        });

        buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setCancelable(true);
                builder.setTitle("Delete Account");
                builder.setMessage("Are you sure you want to delete your account? This will permanently erase your account.");

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        tvAlertDeleteAccount.setVisibility(View.INVISIBLE);
                    }
                });

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        utilService.hideKeyboard(v, getActivity());
                        deleteAccount(v);
                    }
                });
                builder.show();
            }
        });

        return v;
    }


    public void deleteAccount(View view) {

        String apiKey = "https://weposeapi-production.up.railway.app/UserProfile/DeleteAccount/"+userEmail;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE,
                apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        String token = response.getString("msg");
                        Toast.makeText(getActivity(), token, Toast.LENGTH_SHORT).show();
                        int value = 2;
                        Intent i = new Intent(getActivity(), MainActivity.class);
                        i.putExtra("key", value);
                        startActivity(i);
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
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer "+ userToken);
                return params;
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