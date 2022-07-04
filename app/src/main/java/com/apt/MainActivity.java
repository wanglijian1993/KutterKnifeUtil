package com.apt;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.wlj.butterknife.ButterKnife;
import com.wlj.libannotations.BindView;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.tv)
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        tv.setText("Hello world pzq");

    }

}