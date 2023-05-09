package com.example.myapp;

import android.content.DialogInterface;
import android.icu.text.SimpleDateFormat;
import android.icu.text.UFormat;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.example.myapp.UtilisService.SharedPreferenceClass;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private Button mStartButton;
    private Button mStopButton;
    private Button mResetButton;
    private Chronometer mChronometer;

    private long lastPause;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mStartButton = v.findViewById(R.id.startButton);
        mStopButton = v.findViewById(R.id.stopButton);
        mResetButton = v.findViewById(R.id.resetButton);
        mChronometer = v.findViewById(R.id.chronometer);

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
}