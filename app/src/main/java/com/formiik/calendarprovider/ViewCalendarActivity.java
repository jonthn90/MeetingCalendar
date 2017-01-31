package com.formiik.calendarprovider;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

public class ViewCalendarActivity extends AppCompatActivity {

    DatePicker mDate;
    Button mInsert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_calendar);


        mDate   =    (DatePicker) findViewById(R.id.datePicker);
        mInsert =  (Button) findViewById(R.id.button_check);


        mInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Calendar beginTime = Calendar.getInstance();
                beginTime.set(mDate.getYear(), mDate.getMonth(), mDate.getDayOfMonth(), 0, 0);

                beginTime.getTimeInMillis();

                Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                builder.appendPath("time");
                ContentUris.appendId(builder, beginTime.getTimeInMillis());
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setData(builder.build());
                startActivity(intent);


            }
        });




    }
}
