package com.formiik.calendarprovider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;

public class InsertActivity extends AppCompatActivity {

    private DatePicker mDate;
    private Button mCheckDate, mInsert;
    private ToggleButton mDateChange;
    private LinearLayout mDateLayout, mScheduleLayout;
    private ListView mScheduleList;

    private List<me.everything.providers.android.calendar.Calendar> mCalendarsList;
    private me.everything.providers.android.calendar.Calendar mCalendar;

    private List<Event> mEventsList;

    private Calendar mSelectedDate;
    private ArrayList<Event> mEventsDay;

    private ArrayList<Schedule> mSchedule;
    private int mSelectedSchedule;
    AdapterSchedule mAdapterSchedule;

    private String mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);



        Intent intent = getIntent();
        mAccount = intent.getStringExtra("mAccount");

        initCalendar();

        mDate = (DatePicker) findViewById(R.id.datePicker);

        mDateLayout = (LinearLayout) findViewById(R.id.layout_date);
        mScheduleLayout = (LinearLayout) findViewById(R.id.layout_schedule);
        mScheduleList = (ListView) findViewById(R.id.list_schedule);

        mInsert = (Button) findViewById(R.id.button_insert);
        mCheckDate = (Button) findViewById(R.id.button_date);
        mDateChange = (ToggleButton) findViewById(R.id.button_date_change);

        mCheckDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadSchedule();

                mDateLayout.setVisibility(View.GONE);
                String date = "" + mDate.getDayOfMonth() + " - " + String.valueOf(mDate.getMonth() + 1) + " - " + mDate.getYear();

                mDateChange.setTextOn(date);

                mDateChange.setChecked(true);
                mScheduleLayout.setVisibility(View.VISIBLE);

            }
        });

        mDateChange.setChecked(false);
        mDateChange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked) {
                    mScheduleLayout.setVisibility(View.GONE);
                    mDateLayout.setVisibility(View.VISIBLE);
                }

            }
        });


        mInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertEvent();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCalendar();
        loadSchedule();
        mAdapterSchedule.notifyDataSetChanged();
    }

    private void initCalendar() {

        CalendarProvider calendarProvider = new CalendarProvider(getApplicationContext());
        mCalendarsList = calendarProvider.getCalendars().getList();

        for (int i = 0; i < mCalendarsList.size(); i++) {
            if (mCalendarsList.get(i).name != null
            && mCalendarsList.get(i).name.contains(mAccount)) {
                mCalendar = mCalendarsList.get(i);
                mEventsList = calendarProvider.getEvents(mCalendar.id).getList();
                break;
            }
        }
    }

    private void loadSchedule() {

        mSelectedDate = Calendar.getInstance();
        mSelectedDate.set(mDate.getYear(), mDate.getMonth(), mDate.getDayOfMonth(), 0, 0);

        mEventsDay = new ArrayList<>();

        for (Event event : mEventsList) {

            //Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id);
            //getContentResolver().delete(deleteUri, null, null);

            if(!event.deleted) {

                Calendar eventDate = Calendar.getInstance();
                eventDate.setTimeInMillis(event.dTStart);

                if (eventDate.get(Calendar.YEAR) == mSelectedDate.get(Calendar.YEAR)
                        && eventDate.get(Calendar.MONTH) == mSelectedDate.get(Calendar.MONTH)
                        && eventDate.get(Calendar.DAY_OF_MONTH) == mSelectedDate.get(Calendar.DAY_OF_MONTH)) {

                    mEventsDay.add(event);
                }
            }
        }

        mSchedule = new ArrayList<>();

        int hour = 7;
        int minute = 0;

        for (int i = 1; hour <= 22; i++) {

            Calendar scheduleTStart = Calendar.getInstance();
            scheduleTStart.set(mSelectedDate.get(Calendar.YEAR), mSelectedDate.get(Calendar.MONTH), mSelectedDate.get(Calendar.DAY_OF_MONTH), hour, minute);

            Calendar scheduleTEnd = Calendar.getInstance();
            scheduleTEnd.set(mSelectedDate.get(Calendar.YEAR), mSelectedDate.get(Calendar.MONTH), mSelectedDate.get(Calendar.DAY_OF_MONTH), hour, minute+30);


            boolean availability = true;

            for (int j = 0; j < mEventsDay.size() ; j++){

                Calendar eventTStart = Calendar.getInstance();
                eventTStart.setTimeInMillis(mEventsDay.get(j).dTStart);

                Calendar eventTEnd = Calendar.getInstance();
                eventTEnd.setTimeInMillis(mEventsDay.get(j).dTend);

                long diff = eventTEnd.getTimeInMillis() - eventTStart.getTimeInMillis();
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                Log.e("DIFERENCIA", "minutos: " + minutes);

                Log.e("TIME", "Inicio " + scheduleTStart.get(Calendar.HOUR_OF_DAY)+ ":" + scheduleTStart.get(Calendar.MINUTE) + " (" + scheduleTStart.getTimeInMillis() + ")" + " ---- "
                        + mEventsDay.get(j).title + " " + eventTStart.get(Calendar.HOUR_OF_DAY) + ":" + eventTStart.get(Calendar.MINUTE)+  " (" + eventTStart.getTimeInMillis() + ")"

                        + " ---- Fin " + scheduleTEnd.get(Calendar.HOUR_OF_DAY)+ ":" + scheduleTEnd.get(Calendar.MINUTE) + " (" + scheduleTEnd.getTimeInMillis() + ")" + " ---- "
                        + mEventsDay.get(j).title + " " + eventTEnd.get(Calendar.HOUR_OF_DAY) + ":" + eventTEnd.get(Calendar.MINUTE) +  " (" + eventTEnd.getTimeInMillis() + ")" );

                if(eventTStart.get(Calendar.HOUR_OF_DAY) >= scheduleTStart.get(Calendar.HOUR_OF_DAY)  && eventTStart.get(Calendar.MINUTE) >= scheduleTStart.get(Calendar.MINUTE) &&
                        eventTEnd.get(Calendar.HOUR_OF_DAY) <= scheduleTEnd.get(Calendar.HOUR_OF_DAY)  && eventTEnd.get(Calendar.MINUTE) <= scheduleTEnd.get(Calendar.MINUTE)){
                    availability = false;
                    break;
                }
            }

            mSchedule.add(new Schedule(availability, scheduleTStart, scheduleTEnd));

            if (i % 2 == 0) {
                minute = 0;
                hour++;
            } else {
                minute = 30;
            }
        }

        mAdapterSchedule = new AdapterSchedule(mSchedule);
        mScheduleList.setAdapter(mAdapterSchedule);

        mSelectedSchedule = -1;
        mScheduleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                if(mSchedule.get(position).availability) {

                    mSelectedSchedule = position;
                    mAdapterSchedule.notifyDataSetChanged();
                    mInsert.setVisibility(View.VISIBLE);

                }else{
                    mSelectedSchedule = -1;
                    mAdapterSchedule.notifyDataSetChanged();
                    mInsert.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"Horario no disponible", Toast.LENGTH_LONG).show();
                }
            }
        });


        mScheduleList.setLongClickable(true);
        mScheduleList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(!mSchedule.get(i).availability)
                    deleteEvent(i);

                return true;
            }
        });

    }

    class Schedule {

        public boolean availability;
        public Calendar timeStart, timeEnd;

        public Schedule(boolean availability, Calendar timeStart, Calendar timeEnd) {
            this.availability = availability;
            this.timeStart = timeStart;
            this.timeEnd = timeEnd;
        }
    }

    class AdapterSchedule extends BaseAdapter {

        ArrayList<Schedule> mSchedule;

        public AdapterSchedule(ArrayList<Schedule> mSchedule) {
            this.mSchedule = mSchedule;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            final ViewHolder mViewHolder;

            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_schedule, viewGroup, false);

            mViewHolder = new ViewHolder();

            mViewHolder.mTextTime = (TextView) view.findViewById(R.id.text_time);
            mViewHolder.mLayoutTime = (LinearLayout) view.findViewById(R.id.layout_time);

            if (mSchedule.get(position).availability) {
                mViewHolder.mLayoutTime.setBackgroundColor(getResources().getColor(R.color.green_item));
                mViewHolder.mLayoutTime.setPadding(0,0,0,0);
            }

            if (mSelectedSchedule == position) {
                mViewHolder.mLayoutTime.setBackgroundColor(getResources().getColor(R.color.blue_item));
                mViewHolder.mLayoutTime.setPadding(0,5,0,15);
            }

            if (!mSchedule.get(position).availability){
                mViewHolder.mLayoutTime.setBackgroundColor(getResources().getColor(R.color.red_item));
                mViewHolder.mLayoutTime.setPadding(0,0,0,45);
            }

            String time = "" + mSchedule.get(position).timeStart.get(Calendar.HOUR_OF_DAY) + ":" +
                    ((int) mSchedule.get(position).timeStart.get(Calendar.MINUTE) == 0 ? "00" : String.valueOf(mSchedule.get(position).timeStart.get(Calendar.MINUTE))) +
                    " - " + mSchedule.get(position + 1).timeStart.get(Calendar.HOUR_OF_DAY) + ":" +
                    ((int) mSchedule.get(position + 1).timeStart.get(Calendar.MINUTE) == 0 ? "00" : String.valueOf(mSchedule.get(position + 1).timeStart.get(Calendar.MINUTE)));

            mViewHolder.mTextTime.setText(time);

            return view;
        }

        class ViewHolder {

            TextView mTextTime;
            LinearLayout mLayoutTime;
        }

        @Override
        public int getCount() {
            return mSchedule.size() - 1;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }
    }

    private void insertEvent() {

        long startMillis = 0;
        long endMillis = 0;

        Calendar beginTime = mSchedule.get(mSelectedSchedule).timeStart;
        startMillis = beginTime.getTimeInMillis();

        Calendar endTime = mSchedule.get(mSelectedSchedule + 1).timeStart;
        endMillis = endTime.getTimeInMillis();

        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, "Test " + Calendar.getInstance().getTimeInMillis());
        values.put(CalendarContract.Events.DESCRIPTION, "Esta es una cita generada externamente");
        values.put(CalendarContract.Events.CALENDAR_ID, mCalendar.id);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, mCalendar.calendarTimeZone);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

        Toast.makeText(getApplicationContext(), "Cita creada", Toast.LENGTH_SHORT).show();

        finish();

    }

    private void deleteEvent(int position){

        final int pos = position;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(InsertActivity.this);
        alertDialog.setTitle("Eliminar cita");
        alertDialog.setMessage("¿Desea eliminar la cita seleccionada?");
        alertDialog.setPositiveButton("Eliminar",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {



                Calendar scheduleTStart = Calendar.getInstance();
                scheduleTStart.setTimeInMillis(mSchedule.get(pos).timeStart.getTimeInMillis());

                Calendar scheduleTEnd = Calendar.getInstance();
                scheduleTEnd.setTimeInMillis(mSchedule.get(pos).timeEnd.getTimeInMillis());

                for (int i = 0; i < mEventsDay.size() ; i++){

                    Calendar eventTStart = Calendar.getInstance();
                    eventTStart.setTimeInMillis(mEventsDay.get(i).dTStart);

                    Calendar eventTEnd = Calendar.getInstance();
                    eventTEnd.setTimeInMillis(mEventsDay.get(i).dTend);

                    if(eventTStart.get(Calendar.HOUR_OF_DAY) == scheduleTStart.get(Calendar.HOUR_OF_DAY)  && eventTStart.get(Calendar.MINUTE) == scheduleTStart.get(Calendar.MINUTE) &&
                            eventTEnd.get(Calendar.HOUR_OF_DAY) == scheduleTEnd.get(Calendar.HOUR_OF_DAY)  && eventTEnd.get(Calendar.MINUTE) == scheduleTEnd.get(Calendar.MINUTE)){

                        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, mEventsDay.get(i).id);
                        int rows = getContentResolver().delete(deleteUri, null, null);
                        Log.i("ELIMINADO", "Rows deleted: " + rows);

                        Toast.makeText(getApplicationContext(), "Cita borrada", Toast.LENGTH_SHORT).show();

                        mEventsDay.remove(i);

                        initCalendar();
                        loadSchedule();
                        mAdapterSchedule.notifyDataSetChanged();

                        break;
                    }
                }

            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        alertDialog.show();

    }
}
