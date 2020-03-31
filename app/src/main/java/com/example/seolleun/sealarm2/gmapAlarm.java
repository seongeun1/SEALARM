package com.example.seolleun.sealarm2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class gmapAlarm extends AppCompatActivity {

    AlarmManager alarm_manager;
    TimePicker alarm_timepicker;
    Context context;
    PendingIntent pendingIntent;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapalarmlayout);
        this.context = this;

        long now = System.currentTimeMillis();
        Date mDate=new Date(now);
        SimpleDateFormat time= new SimpleDateFormat("hh");
        SimpleDateFormat minute= new SimpleDateFormat("mm");
        int getTime=Integer.parseInt(time.format(mDate));
        int getMinute=Integer.parseInt(minute.format(mDate));



        // 알람매니저 설정
        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // 타임피커 설정
        alarm_timepicker = findViewById(R.id.time_picker);

        // Calendar 객체 생성
        final Calendar calendar = Calendar.getInstance();

        // 알람리시버 intent 생성
        final Intent my_intent = new Intent(this.context, Alarm_Reciver.class);
        // calendar에 시간 셋팅

        calendar.set(Calendar.HOUR_OF_DAY, getTime);
        calendar.set(Calendar.MINUTE, getMinute);

        // reveiver에 string 값 넘겨주기
        my_intent.putExtra("state","alarm on");

        pendingIntent = PendingIntent.getBroadcast(gmapAlarm.this, 0, my_intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // 알람셋팅
        alarm_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                pendingIntent);


        Button alarm_off = findViewById(R.id.btn_finish);
        alarm_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(gmapAlarm.this,"Alarm 종료",Toast.LENGTH_SHORT).show();
                // 알람매니저 취소
                alarm_manager.cancel(pendingIntent);

                my_intent.putExtra("state","alarm off");

                // 알람취소
                sendBroadcast(my_intent);

                Intent intent=new Intent(gmapAlarm.this, main.class);
                startActivity(intent);
            }
        });
    }

    }



