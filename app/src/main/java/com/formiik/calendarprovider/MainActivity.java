package com.formiik.calendarprovider;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {


    private Button mInsertEventButton, mViewCalendarButton, mMeetingFormiikButton;

    private TextView mTextAccount;

    private ArrayList<String> mAccountsList;
    private String mAccount;

    private final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 0x01;
    private final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 0x02;
    private final int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR= 0x03;

    private SharedPreferences mSharedPreferences;
    private final String mKeyAccount = "CurrentAccount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInsertEventButton = (Button) findViewById(R.id.button_insert);
        mInsertEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), InsertActivity.class);
                intent.putExtra("mAccount", mAccount);
                startActivity(intent);

            }
        });

        mViewCalendarButton = (Button) findViewById(R.id.button_view);
        mViewCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), ViewCalendarActivity.class);

                startActivity(intent);

            }
        });

        mMeetingFormiikButton = (Button) findViewById(R.id.button_meeting);
        mMeetingFormiikButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), ActivityWidgetMeeting.class);
                intent.putExtra("mAccount", mAccount);
                startActivity(intent);

            }
        });

        mTextAccount = (TextView) findViewById(R.id.text_account);

        mSharedPreferences = MainActivity.this.getPreferences(Context.MODE_PRIVATE);

        chooseAccount();

        mTextAccount.setText(mAccount);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.READ_CALENDAR  }, MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.WRITE_CALENDAR  }, MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
        }
    }

    private void chooseAccount() {

        mAccount = mSharedPreferences.getString(mKeyAccount, "");

        if (mAccount.equalsIgnoreCase("")) {

            mAccountsList = new ArrayList<>();

            Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.GET_ACCOUNTS  }, MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
            }
            Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    mAccountsList.add(account.name);
                }
            }

            if (accounts.length > 1) {

                final AlertDialogAccount dialogAccount = new AlertDialogAccount(getApplicationContext(), mAccountsList);

                dialogAccount.setTitle("Selecciona una cuenta");
                dialogAccount.setView(getLayoutInflater().inflate(R.layout.alert_dialog_account, null));
                dialogAccount.setCancelable(false);
                dialogAccount.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (!dialogAccount.getAccount().equalsIgnoreCase("") && dialogAccount.getAccount() != null) {

                                    mAccount = dialogAccount.getAccount();

                                    dialogAccount.dismiss();
                                }
                            }
                        });

                dialogAccount.show();

            } else if (accounts.length == 1) {
                mAccount = mAccountsList.get(0);

            } else if (accounts.length == 0) {

                mAccount = "";

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());
                alertDialog.setTitle("Sin cuentas");
                alertDialog.setMessage("Es necesario ligar al menos una cuenta de Google al dispositivo");
                alertDialog.create();

                finish();
            }

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(mKeyAccount, mAccount);
            editor.commit();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                chooseAccount();
                break;
            }
        }
    }

    class AlertDialogAccount extends AlertDialog{

        private Context context;
        private ListView mListView;
        private TextView mTextAccount;
        private List<String> mAccountsList;

        protected AlertDialogAccount(@NonNull Context context, List<String> mAccountsList) {
            super(context);
            this.context = context;
            this.mAccountsList = mAccountsList;
        }

        @Override
        public void setView(View view) {
            super.setView(view);
            mListView = (ListView) view.findViewById(R.id.list_accounts);
            mTextAccount = (TextView) view.findViewById(R.id.text_account);

            ListAdapter mListAdapter = new ArrayAdapter(context,android.R.layout.simple_list_item_1, mAccountsList);

            mListView.setAdapter(mListAdapter);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    mTextAccount.setText(mAccountsList.get(i));
                }
            });

        }

        public String getAccount(){
            return mTextAccount.getText().toString();
        }

    }
}