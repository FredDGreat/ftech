package com.ftech.criptoapp.cardview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ftech.criptoapp.CreatedCards;
import com.ftech.criptoapp.R;
import com.ftech.criptoapp.service.TaskSchedulerService;
import com.ftech.criptoapp.data.CurrencyDBHelper;
import com.ftech.criptoapp.data.CurrencyHelper;
import com.ftech.criptoapp.networkutil.NetworkUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Objects;

public class CardActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    //TODO: Remove
    // For logging
    public static final String LOG_TAG = CardActivity.class.getSimpleName();
    // String to identify intent source
    private static final String ETH_CODE = "eth_value";
    private static final String BTC_CODE = "btc_value";
    private static final String MY_INTENT = "com.ftech.criptoapp.cryptservice.CUSTOM_INTENT";
    private static final String CONNECTION_INTENT = "android.net.conn.CONNECTIVITY_CHANGE";
    /**
     * JobScheduler Job ID
     */
    private static final int JOB_ID = 1;
    /**
     * Currency to convert
     */
    private String code;
    /**
     * Conversion box status
     */
    private boolean editBoxWithText;
    // Create a spinners
    Spinner spinner;
    Spinner curSpinner;
    /**
     * Name of the database currency
     * value from the Intent origin.
     */
    String currency_code;
    /**
     * Reserves the original currency value
     */
    private double reserveCurValue;
    /**
     * Name of the column for which
     * the Intent originated
     */
    int columnPosition;
    /**
     * Value to convert
     */
    double userInput;
    /**
     * Format to use for displayed currencies
     */
    DecimalFormat df = new DecimalFormat("#,###.###");
    // Get the Card currency value
    TextView curValue;
    // The Currency logo
    TextView logoText;
    // Get the cryto currency image
    ImageView cryptImage;
    /**
     * Create an instance of the JobScheduler class
     */
    JobScheduler mJobScheduler;

    /**
     * Used to set the menu items
     */
    Menu menu = null;
    /**
     * Used to check network status
     */
    String status;
    /**
     * Initiating the buttons for creating and viewing created cards
     */
    private AppCompatButton newCard,viewCards;
    private ImageView backBtn;
    //----
    private EditText convertBox;
    /**
     * Used to check network status
     */
    boolean online;
    //Create an instance of CryptoCurrencyDBHelper
    private CurrencyDBHelper mDBHelper;
    /**
     * Used to check if the spinner is
     * drawn for the first time
     */
    private boolean spinnerClicked = false;
    /**
     * Used to check if the crypto spinner
     * was drawn for the first time
     */
    private boolean curSpinnerClicked = false;

    /**
     *Months initiation
     */
    public static String[] months = {"Jan", "Feb","March","April","May","June","July","Aug","Sept","Oct","Nov","Dec"};

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        // Register the intent here
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MY_INTENT);
        intentFilter.addAction(CONNECTION_INTENT);
        registerReceiver(this.broadcastReceiver, intentFilter);

        // Initialize JobScheduler
        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        mJobScheduler.schedule(new JobInfo.Builder(JOB_ID,
                new ComponentName(this, TaskSchedulerService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(1000)
                .build());

        Bundle extras = getIntent().getExtras();

        currency_code = extras.getString("CURRENCY_CODE");
        columnPosition = extras.getInt("COLUMN_NAME");
        //reserveCurValue = extras.getInt("COLUMN_NAME");

        //Instantiating the spinners
        spinner = (Spinner) findViewById(R.id.cur_name_spiner);
        curSpinner = (Spinner) findViewById(R.id.cur_coin_spiner);
        //instantiate the convertion box, newCard and viewCards buttons
        convertBox = (EditText)findViewById(R.id.convertBox);
        convertBox.addTextChangedListener(textWatcher);
        // Get the Card currency value
        curValue = (TextView) findViewById(R.id.card_currency_value);
        // Get the Currency logo
        logoText = (TextView) findViewById(R.id.card_currency_symbol);

        // Get the cryto currency image
        cryptImage = (ImageView) findViewById(R.id.card_crypto_img);
        //---
        newCard = (AppCompatButton)findViewById(R.id.CreateNewCard);
        viewCards = (AppCompatButton)findViewById(R.id.viewCards);
        backBtn = (ImageView)findViewById(R.id.backBtn);
        TextView title = (TextView)findViewById(R.id.title);
        title.setText("CryptoConversion");
        //-------------
        //make newCard and viewCards buttons clickable
        newCard.setOnClickListener(this);
        viewCards.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        //-------------
        // Load the spinner data from database
        loadSpinnerData();
        // Load the crypto spinner
        loadCryptoSpinner();

        //TODO: Remove
        Log.i(LOG_TAG, "Column Position id sent here: " + columnPosition);
        //setting the initial value of spinner
        //spinner.setSelection(columnPosition - 1);
        // Spinner listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //
                // Get the item that was selected or clicked
                String code = parent.getItemAtPosition(position).toString();
                //TODO: Remove
                Log.i(LOG_TAG, "Spinner selected code is: " + code);
                Log.i(LOG_TAG, "currency_code is: " + currency_code + " and code is " + code);
                mDBHelper = new CurrencyDBHelper(getApplicationContext());

                // Check the state of the spinner
                if (!spinnerClicked) {

                    spinner.setSelection(columnPosition);
                    spinnerChecker();

                }

                if (currency_code != null) {
                    //TODO: Remove
                    Log.i(LOG_TAG, "Inside currency_code if block ...");

                    if (currency_code.equals(ETH_CODE)) {

                        //TODO: Remove
                        Log.i(LOG_TAG, "Calling value from database in eth if block spinner...");

                        String value = mDBHelper.getCurrencyValue(code, ETH_CODE);
                        double num = Double.parseDouble(value);
                        curValue.setText(df.format(num));
                        logoText.setText(CurrencyHelper.getCurrencySymbol(code));
                        // Top image for CardView
                        cryptImage.setImageResource(R.mipmap.ethereum);
                        reserveCurValue = Double.parseDouble(value);
                        if (editBoxWithText) {
                            Log.i(LOG_TAG, "convert() called from Spinner object");
                            convert();
                        }
                    }
                    if (currency_code.equals(BTC_CODE)) {

                        //TODO: Remove
                        Log.i(LOG_TAG, "Calling value from database in btc if block spinner...");

                        String value = mDBHelper.getCurrencyValue(code, BTC_CODE);
                        double num = Double.parseDouble(value);
                        curValue.setText(df.format(num));
                        logoText.setText(CurrencyHelper.getCurrencySymbol(code));
                        // Top image for CardView
                        cryptImage.setImageResource(R.mipmap.bitcoin);
                        //----------------
                        reserveCurValue = Double.parseDouble(value);
                        if (editBoxWithText) {
                            Log.i(LOG_TAG, "convert() called from Spinner object");
                            convert();
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinner.setSelection(columnPosition);
            }
        });

        curSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Get the spinner item that is currently selected
                String code = spinner.getSelectedItem().toString();
                String cryptSelected = parent.getItemAtPosition(position).toString();
                //TODO: Remove
                Log.i(LOG_TAG, "code value in curSpinner: " + code);

                mDBHelper = new CurrencyDBHelper(getApplicationContext());
                //TODO: Remove
                Log.i(LOG_TAG, "cryptoCurSpinnerChecker() called and the value: " + curSpinnerClicked);

                if (!curSpinnerClicked) {
                    //TODO: Remove
                    Log.i(LOG_TAG, "Setting the displayed cryptoCurSpinner ...");

                    if (currency_code != null) {
                        if (currency_code.equals(ETH_CODE)) {
                            //TODO: Remove
                            Log.i(LOG_TAG, "Setting the displayed cryptoCurSpinner to ETH...");
                            curSpinner.setSelection(0);

                        }
                        if (currency_code.equals(BTC_CODE)) {
                            //TODO: Remove
                            Log.i(LOG_TAG, "Setting the displayed cryptoCurSpinner to BTC...");
                            curSpinner.setSelection(1);

                        }


                    }

                    cryptoCurSpinnerChecker();


                }

                if (currency_code != null) {

                    if (cryptSelected.equals(getString(R.string.code_eth_text))) {
                        //TODO: Remove
                        Log.i(LOG_TAG, "Setting eth_ value currency_code to " + cryptSelected);
                        currency_code = "eth_value";
                    }

                    if (cryptSelected.equals(getString(R.string.code_btc_text))) {
                        //TODO: Remove
                        Log.i(LOG_TAG, "Setting btc_ value currency_code to " + cryptSelected);
                        currency_code = "btc_value";
                    }
                }


                //TODO: Remove
                Log.i(LOG_TAG, "cryptoCurSpinnerChecker() called and the value: " + curSpinnerClicked);


                if (currency_code != null) {
                    //TODO: Remove
                    Log.i(LOG_TAG, "Inside currency_code if block of curSpinner... Value of currency_code " + currency_code + "cryptSelected: " + cryptSelected);

                    if (currency_code.equals(ETH_CODE)) {

                        //TODO: Remove
                        Log.i(LOG_TAG, "Calling value from database in eth if block of curSpinner...Value of currency_code " + currency_code);

                        String value = mDBHelper.getCurrencyValue(code, ETH_CODE);
                        double num = Double.parseDouble(value);
                        curValue.setText(df.format(num));
                        // Top image for CardView
                        cryptImage.setImageResource(R.mipmap.ethereum);
                        reserveCurValue = Double.parseDouble(value);
                        if (editBoxWithText) {
                            Log.i(LOG_TAG, "convert() called from Spinner object");
                            convert();
                        }
                    }
                    if (currency_code.equals(BTC_CODE)) {

                        //TODO: Remove
                        Log.i(LOG_TAG, "Calling value from database in btc if block of curSpinner...Value of currency_code " + currency_code);

                        String value = mDBHelper.getCurrencyValue(code, BTC_CODE);
                        double num = Double.parseDouble(value);
                        curValue.setText(df.format(num));
                        // Top image for CardView
                        cryptImage.setImageResource(R.mipmap.bitcoin);
                        reserveCurValue = Double.parseDouble(value);
                        if (editBoxWithText) {
                            Log.i(LOG_TAG, "convert() called from Spinner object");
                            convert();
                        }
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // Set up CardView to take user to convert view
        CardView cardView = (CardView) findViewById(R.id.card_container);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*//TODO: Remove
                Log.i(LOG_TAG, "CardView onCLick event fired ...");
                Toast.makeText(CardActivity.this, "Clicked on CardView", Toast.LENGTH_LONG).show();

                Intent customConversionRate = new Intent(getApplicationContext(), ConversionActivity.class);
                startActivity(customConversionRate);*/

            }
        });

    }
    TextWatcher textWatcher =  new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            editBoxWithText = true;
            convert();
        }

        @Override
        public void afterTextChanged(Editable s) {
            editBoxWithText = true;
        }
    };
    //@RequiresApi(api = Build.VERSION_CODES.N)
    public void convert() {
        // Calculation result
        double cal;
        String result;
        //
        mDBHelper = new CurrencyDBHelper(getApplicationContext());
        // Get the Card currency value
        //curValue = (TextView) findViewById(R.id.card_currency_value);
        // Grab the TextViews to update
        TextView resultTextView = (TextView) findViewById(R.id.card_currency_value);
        //TODO: Remove
        Log.i(LOG_TAG, "Result textview grabbed ...");
        //-------------
        convertBox = (EditText)findViewById(R.id.convertBox);
        curSpinner = (Spinner) findViewById(R.id.cur_coin_spiner);
        //Checked to make sure user input isn't empty
        if (convertBox.getText().toString().trim().length() > 0) {
            String code = curSpinner.getSelectedItem().toString();
            if (code.equalsIgnoreCase("BTC")) {
                currency_code = "btc_value";
            } else {
                currency_code = "eth_value";
            }
            // Grab user input.
            userInput = Double.parseDouble(convertBox.getText().toString().trim());
                /*Toast.makeText(CardActivity.this, "curValue="+curValue.getText().toString()+
                        "\nuserInput="+userInput, Toast.LENGTH_SHORT).show();*/
            try {
                // Format to use for calculated convert
                DecimalFormat df = new DecimalFormat("#,###.###");
                // Get the value of the currency from tha database
                // Get the value of the currency from tha database
                //double value = Double.parseDouble(curValue.getText().toString().replace(",",""));
                //double value = Double.parseDouble(mDBHelper.getCurrencyValue(code, currency_code).replace(",",""));
                //TODO: Remove
                Log.i(LOG_TAG, "Database value: " + reserveCurValue);
                /*reserveCurValue = Double.parseDouble(value);
                convert();*/
                // Calculate the convert rate
                cal = userInput/reserveCurValue;
                // Used to format the calculation output
                result = df.format(cal);
                //Toast.makeText(CardActivity.this, "result="+result+"cal="+cal, Toast.LENGTH_SHORT).show();
                //TODO: Remove
                Log.i(LOG_TAG, "Result of the convert: " + result);
                //Set the Conversion Result TextView
                //curValue.setText(""+cal);
                resultTextView.setText(result);
            } catch (NumberFormatException e) {
                //TODO: Remove
                Log.i(LOG_TAG, "Calculation error: " + e);
            } catch (IllegalFormatException g) {
                //TODO: Remove
                Log.i(LOG_TAG, "Error: " + g);
            }
            Log.i(LOG_TAG, "Converting user input to double for user in convert ...");
        } else {
            Toast.makeText(getApplicationContext(), "Box empty", Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Use this to catch the intent sent from the TaskSchedulerService class
     */
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public IBinder peekService(Context myContext, Intent service) {
            return super.peekService(myContext, service);
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void onReceive(Context context, Intent intent) {

            //TODO: Remove
            //Toast.makeText(context, "Intent detected", Toast.LENGTH_SHORT).show();

            /*if (intent.getAction().equals(MY_INTENT)) {

                //TODO: Remove
                //Toast.makeText(context, "MY_INTENT", Toast.LENGTH_SHORT).show();

                MenuItem refreshMenuItem = menu.findItem(R.id.menu_refresh);
                refreshMenuItem.setVisible(true);
//                if ( !getLoaderManager().getLoader(CRYPTOCURRENCY_LOADER_ID).isStarted()) {
//                    refreshMenuItem.setVisible(false);
//                }

            }*/

            // Set the network menu status
            if (intent.getAction().equals(CONNECTION_INTENT)) {

                //TODO: Remove
                //Toast.makeText(context, "CONNECT_INTENT", Toast.LENGTH_SHORT).show();

                status = NetworkUtil.getConnectivityStatusString(context);
                online = (Objects.equals(status, "Wifi enabled") || Objects.equals(status, "Mobile data enabled"));
                supportInvalidateOptionsMenu();

            }
        }
    };
    /**
     * Loads currency choice spinner
     */
    private void loadSpinnerData() {

        //TODO: Remove
        // For logging
        Log.i(LOG_TAG, "loadSpinnerData() called...");

        mDBHelper = new CurrencyDBHelper(getApplicationContext());

        // Spinner dropdown elements
        List<String> codes = mDBHelper.getAllCurrencyCodeNames();


        // Create adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, codes);

        // Dropdown layer style
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown);

        // Attach dataAdapter to spinner
        spinner.setAdapter(dataAdapter);


    }

    private void loadCryptoSpinner() {

        //TODO: Remove
        // For logging
        Log.i(LOG_TAG, "loadCryptoSpinner() called ...");

        // Create an adapter from the string array resource and use
        // android's inbuilt layout file simple_spinner_item
        // that represents the default spinner in the UI
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.crypto_array, android.R.layout.simple_spinner_item);

        // Set the layout to use for each dropdown item
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);

        curSpinner.setAdapter(adapter);


    }

    /**
     * This methods saves data for creating new card
     */
    public void createCard(){
        //making instance of a persistent storage
        SharedPreferences dataPref  = getSharedPreferences("NEW_CARD_DATA", MODE_PRIVATE);
        SharedPreferences.Editor dataEditor = dataPref.edit();
        //count number of cards created
        SharedPreferences pref4newCardCount = getSharedPreferences("NEW_CARD_COUNTER",MODE_PRIVATE);
        SharedPreferences.Editor e = pref4newCardCount.edit();
        if(!pref4newCardCount.getBoolean("start_counter",false)){
            e.putBoolean("start_counter",true);
            e.putInt("card_tracker",0);
            e.apply();
        }
        //get date and time stamp when new card is created
        Calendar cal = Calendar.getInstance();
        int yy = cal.get(Calendar.YEAR);
        int mm = cal.get(Calendar.MONTH);
        int dd = cal.get(Calendar.DAY_OF_MONTH);
        //Date dateRepresentation = cal.getTime();
        //for current time
        Date d= cal.getTime();
        SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a");
        String currentDateTimeString = new StringBuilder()
                // Month is 0 based, just add 1
                .append(dd).append("-").append(months[mm]).append("-")
                .append(yy)+","+sdf.format(d);
        String curCoin = curSpinner.getSelectedItem().toString();
        String curName = spinner.getSelectedItem().toString();
        int spinnerPos = spinner.getSelectedItemPosition();
        int coinSpinnerPos = curSpinner.getSelectedItemPosition();
        String cValue = curValue.getText().toString();
        String curLogo = logoText.getText().toString();
        String convertBoxStr = convertBox.getText().toString();
        //Toast.makeText(this, "spinnerPos = "+spinnerPos, Toast.LENGTH_SHORT).show();
        //start new card counter iteration
        if(pref4newCardCount.getBoolean("start_counter",false)){
            //if(pref4newCardCount.getInt("card_tracker",-1) <= 3) {
                int c = pref4newCardCount.getInt("card_tracker", -1) + 1;
                e.putInt("card_tracker", c);
                e.apply();
            //}
            //--------------
            dataEditor.putBoolean("cardHasBeenCreated",true);
            dataEditor.putString("curSpinner"+c,curCoin);
            dataEditor.putInt("curSpinnerPosition"+c,coinSpinnerPos);
            dataEditor.putString("spinner"+c,curName);
            dataEditor.putInt("spinnerPosition"+c,spinnerPos);
            dataEditor.putString("curValue"+c,cValue);
            dataEditor.putString("convertBox"+c,convertBoxStr);
            dataEditor.putString("logoText"+c,curLogo);
            dataEditor.putString("currency_code"+c,currency_code);
            dataEditor.putString("date"+c,currentDateTimeString);
            dataEditor.apply();
            Toast.makeText(this, "Card created.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

        // TODO: What should happen when nothing is selected?

    }

    /**
     * Checks the original state
     * of the currency spinner setOnItemSelectedListener
     * event.
     */
    public void spinnerChecker() {

        if (!spinnerClicked) {

            spinnerClicked = true;

        }
    }

    /**
     * Checks the original state
     * of the crypto spinner setOnItemSelectedListener
     * event.
     */
    public void cryptoCurSpinnerChecker() {

        if (!curSpinnerClicked) {

            curSpinnerClicked = true;
        }
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        //TODO: Simplify this
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(MY_INTENT));
        registerReceiver(broadcastReceiver, new IntentFilter(CONNECTION_INTENT));
        //With the help of this persistent storage, you can be able to know when an item is being edited from CreatedCards.
        //Hence, it will reflect the selected item from CreatedCards to CardActivity
        SharedPreferences dataPref  = getSharedPreferences("NEW_CARD_DATA", MODE_PRIVATE);
        if(dataPref.getBoolean("isEditedItem",false)){
            convertBox.setText(dataPref.getString("convertBox",null));
            logoText.setText(dataPref.getString("logoText",null));
            curValue.setText(dataPref.getString("curValue",null));
            currency_code = dataPref.getString("currency_code",null);
            spinner.setSelection(dataPref.getInt("spinnerPosition",-1));
            curSpinner.setSelection(dataPref.getInt("curSpinnerPosition",-1));
            if(currency_code.equals(BTC_CODE)){
                // Top image for CardView
                cryptImage.setImageResource(R.mipmap.bitcoin);
            }else{
                cryptImage.setImageResource(R.mipmap.ethereum);
            }
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        if(v == newCard){
            if(editBoxWithText)
                createCard();
            else
                Toast.makeText(this, "Can't create new card with empty box", Toast.LENGTH_SHORT).show();
        }
        if(v == viewCards){
            Intent viewCardsIntent = new Intent(this, CreatedCards.class);
            startActivity(viewCardsIntent);
            //finish();
        }
        if(v == backBtn){
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
