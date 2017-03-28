package com.formiik.calendarprovider;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;

public class ActivityWidgetMeeting extends AppCompatActivity {

    private ListView                                    mHourListView;

    private String                                      mAccount;

    private List<Calendar>                              mCalendarList;
    private Calendar                                    mCalendar;

    private java.util.Calendar                          mSelectedDate;
    private ArrayList<Event>                            mEventsDay;

    private ArrayList<HourMeeting>                      mHourList;
    ActivityWidgetMeeting.AdapterMeeting                mAdapterMeeting;

    private List<Event>                                 mEventsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_meeting);

        Intent intent = getIntent();
        mAccount = intent.getStringExtra("mAccount");

        initCalendar();

        loadSchedule();

    }

    private void initCalendar() {

        mHourListView = (ListView) findViewById(R.id.list_meeting);

        CalendarProvider calendarProvider = new CalendarProvider(getApplicationContext());
        mCalendarList = calendarProvider.getCalendars().getList();

        for (int i = 0; i < mCalendarList.size(); i++) {
            if (mCalendarList.get(i).name != null
                    && mCalendarList.get(i).name.contains(mAccount)) {
                mCalendar = mCalendarList.get(i);
                mEventsList = calendarProvider.getEvents(mCalendar.id).getList();
                break;
            }
        }
    }

    private void loadSchedule() {

        mSelectedDate = java.util.Calendar.getInstance();
        //mSelectedDate.set(mDate.getYear(), mDate.getMonth(), mDate.getDayOfMonth(), 0, 0);

        mEventsDay = new ArrayList<>();

        for (Event event : mEventsList) {

            if(!event.deleted) {

                java.util.Calendar eventDate = java.util.Calendar.getInstance();
                eventDate.setTimeInMillis(event.dTStart);

                if (eventDate.get(java.util.Calendar.YEAR) == mSelectedDate.get(java.util.Calendar.YEAR)
                        && eventDate.get(java.util.Calendar.MONTH) == mSelectedDate.get(java.util.Calendar.MONTH)
                        && eventDate.get(java.util.Calendar.DAY_OF_MONTH) == mSelectedDate.get(java.util.Calendar.DAY_OF_MONTH)) {

                    mEventsDay.add(event);
                }
            }
        }

        mHourList = new ArrayList<>();


        for (int hour = 0; hour <= 23; hour++) {

            java.util.Calendar hourStart = java.util.Calendar.getInstance();
            hourStart.set(mSelectedDate.get(java.util.Calendar.YEAR), mSelectedDate.get(java.util.Calendar.MONTH), mSelectedDate.get(java.util.Calendar.DAY_OF_MONTH), hour, 0);

            java.util.Calendar hourEnd = java.util.Calendar.getInstance();

            if (hour != 23) {
                hourEnd.set(mSelectedDate.get(java.util.Calendar.YEAR), mSelectedDate.get(java.util.Calendar.MONTH), mSelectedDate.get(java.util.Calendar.DAY_OF_MONTH), hour + 1, 0);
            }else{
                hourEnd.set(mSelectedDate.get(java.util.Calendar.YEAR), mSelectedDate.get(java.util.Calendar.MONTH), mSelectedDate.get(java.util.Calendar.DAY_OF_MONTH), hour, 59);
            }

            mHourList.add(new HourMeeting(hour, hourStart, hourEnd, new ArrayList<Event>()));

            for (int i = 0; i < mEventsDay.size() ; i++){

                java.util.Calendar eventTStart = java.util.Calendar.getInstance();
                eventTStart.setTimeInMillis(mEventsDay.get(i).dTStart);

                java.util.Calendar eventTEnd = java.util.Calendar.getInstance();
                eventTEnd.setTimeInMillis(mEventsDay.get(i).dTend);

                if(eventTStart.get(java.util.Calendar.HOUR_OF_DAY) >= hourStart.get(java.util.Calendar.HOUR_OF_DAY)  &&
                        eventTStart.get(java.util.Calendar.HOUR_OF_DAY) < hourEnd.get(java.util.Calendar.HOUR_OF_DAY)){

                    mHourList.get(hour).mEventList.add(mEventsDay.get(i));
                }
            }
        }

        mAdapterMeeting = new AdapterMeeting(mHourList);
        mHourListView.setAdapter(mAdapterMeeting);

        /*
        mHourListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                if(mHourList.get(position).availability) {

                    mSelectedSchedule = position;
                    mAdapterMeeting.notifyDataSetChanged();
                    mInsert.setVisibility(View.VISIBLE);

                }else{
                    mSelectedSchedule = -1;
                    mAdapterMeeting.notifyDataSetChanged();
                    mInsert.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"Horario no disponible", Toast.LENGTH_LONG).show();
                }
            }
        });


        mHourListView.setLongClickable(true);
        mHourListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(!mHourList.get(i).availability)
                    deleteEvent(i);

                return true;
            }
        });
        */

    }

    private void deleteEvent(int position){

        final int pos = position;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityWidgetMeeting.this);
        alertDialog.setTitle("Eliminar cita");
        alertDialog.setMessage("¿Desea eliminar la cita seleccionada?");
        alertDialog.setPositiveButton("Eliminar",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                java.util.Calendar scheduleTStart = java.util.Calendar.getInstance();
                scheduleTStart.setTimeInMillis(mHourList.get(pos).mHourStart.getTimeInMillis());

                java.util.Calendar scheduleTEnd = java.util.Calendar.getInstance();
                scheduleTEnd.setTimeInMillis(mHourList.get(pos).mHourEnd.getTimeInMillis());

                for (int i = 0; i < mEventsDay.size() ; i++){

                    java.util.Calendar eventTStart = java.util.Calendar.getInstance();
                    eventTStart.setTimeInMillis(mEventsDay.get(i).dTStart);

                    java.util.Calendar eventTEnd = java.util.Calendar.getInstance();
                    eventTEnd.setTimeInMillis(mEventsDay.get(i).dTend);

                    if(eventTStart.get(java.util.Calendar.HOUR_OF_DAY) == scheduleTStart.get(java.util.Calendar.HOUR_OF_DAY)  && eventTStart.get(java.util.Calendar.MINUTE) == scheduleTStart.get(java.util.Calendar.MINUTE) &&
                            eventTEnd.get(java.util.Calendar.HOUR_OF_DAY) == scheduleTEnd.get(java.util.Calendar.HOUR_OF_DAY)  && eventTEnd.get(java.util.Calendar.MINUTE) == scheduleTEnd.get(java.util.Calendar.MINUTE)){

                        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, mEventsDay.get(i).id);
                        int rows = getContentResolver().delete(deleteUri, null, null);
                        Log.i("ELIMINADO", "Rows deleted: " + rows);

                        Toast.makeText(getApplicationContext(), "Cita borrada", Toast.LENGTH_SHORT).show();

                        mEventsDay.remove(i);

                        initCalendar();
                        loadSchedule();
                        mAdapterMeeting.notifyDataSetChanged();

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


    class HourMeeting {

        public int mPositionHourList;
        public java.util.Calendar mHourStart, mHourEnd;
        public List<Event> mEventList;

        public HourMeeting(int mPositionHourList, java.util.Calendar mHourStart, java.util.Calendar mHourEnd, List<Event> mEventList) {
            this.mPositionHourList = mPositionHourList;
            this.mHourStart = mHourStart;
            this.mHourEnd = mHourEnd;
            this.mEventList = mEventList;
        }
    }


    class AdapterMeeting extends BaseAdapter{

        ArrayList<HourMeeting> mHourList;

        public AdapterMeeting(ArrayList<HourMeeting> mHourList) {
            this.mHourList = mHourList;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            final ViewHolder mViewHolder;

            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_meeting, viewGroup, false);

            mViewHolder = new ViewHolder();

            mViewHolder.mItemMeetingLayout = (LinearLayout) view.findViewById(R.id.layout_item_meeting);
            mViewHolder.mMeetingHourLayout = (LinearLayout) view.findViewById(R.id.layout_meeting_hour);
            mViewHolder.mMeetingContainerLayout = (LinearLayout) view.findViewById(R.id.layout_meeting_container);
            mViewHolder.mSeparatorLayout = (LinearLayout) view.findViewById(R.id.layout_separator);
            mViewHolder.mTimeStartText = (TextView) view.findViewById(R.id.text_time_start);
            mViewHolder.mTimeEndText = (TextView) view.findViewById(R.id.text_time_end);

            mViewHolder.mTimeEndText.setVisibility(View.GONE);


            String timeStart = "" + mHourList.get(position).mHourStart.get(java.util.Calendar.HOUR_OF_DAY) + ":" +
                    ((int) mHourList.get(position).mHourStart.get(java.util.Calendar.MINUTE) == 0 ? "00" : String.valueOf(mHourList.get(position).mHourStart.get(java.util.Calendar.MINUTE)));

            mViewHolder.mTimeStartText.setText(timeStart);

            Log.e("POSITION", " "+ position + "size " + mHourList.size());

            /*
            if(position == mHourList.size()-1){
                String timeEnd = "" + mHourList.get(position).timeEnd.get(java.util.Calendar.HOUR_OF_DAY) + ":" +
                        ((int) mHourList.get(position).timeEnd.get(java.util.Calendar.MINUTE) == 0 ? "00" : String.valueOf(mHourList.get(position).timeEnd.get(java.util.Calendar.MINUTE)));

                mViewHolder.mTimeEndText.setText(timeEnd);
            }

             TextView valueTV = new TextView(getApplicationContext());
                valueTV.setText("hallo hallo");
                valueTV.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                mViewHolder.mMeetingContainerLayout.addView(valueTV);
            */

            if (position == 3) {

                TextView valueTV = new TextView(getApplicationContext());
                valueTV.setText("This is a test");
                valueTV.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 360));
                valueTV.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.colorPrimary));
                valueTV.setPadding(5,5,5,5);

                mViewHolder.mMeetingContainerLayout.addView(valueTV);

            }

            return view;
        }

        @Override
        public int getCount() {
            return mHourList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        class ViewHolder {

            LinearLayout    mItemMeetingLayout,
                            mMeetingHourLayout,
                            mMeetingContainerLayout,
                            mSeparatorLayout;

            TextView        mTimeStartText,
                            mTimeEndText;
        }




    }
}
