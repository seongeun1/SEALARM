package com.example.seolleun.sealarm2;

import android.content.Intent;

import android.os.Bundle;

import android.view.View;

import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Seolleun on 2020-01-28.
 */

public class main extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ImageView clock=(ImageView)findViewById(R.id.clock);
        clock.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(main.this, mapAlarmJava.MainActivity.class);
                startActivity(intent);
            }
        });

        ImageView mapicon=(ImageView)findViewById(R.id.mapicon);
        mapicon.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent2=new Intent(main.this, googlemap.class);
                startActivity(intent2);
            }
        });





    }
}
