package com.ftech.criptoapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ftech.criptoapp.cardview.CardActivity;
import com.ftech.criptoapp.service.BroadcastService;
import com.ftech.criptoapp.service.TaskSchedulerService;
import com.ftech.criptoapp.data.CryptoContract;
import com.ftech.criptoapp.networkutil.NetworkUtil;

import java.util.IllegalFormatException;
import java.util.List;
import java.util.Objects;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class HomeActivity extends AppCompatActivity implements LoaderCallbacks<List<Currency>> {

    /**
     * Drawer icon
     */
    ImageView optionIcon;
    //TODO: Remove
    public static final String LOG_TAG = HomeActivity.class.getSimpleName();
    //
    // String to identify intent source
    private String BTC_ETH = "btc_value";
    // URL for the currency data from cryptocompare
    private static final String CRYPTO_CURRENRY_URL = "https://min-api.cryptocompare.com/data/pricemulti";
    /**
     * Constant value for the earthquake loader ID. We can choose any integer
     * This really comes into play when you're using multiple loaders
     */
    private static final int CRYPTOCURRENCY_LOADER_ID = 1;

    /**
     * JobScheduler Job ID
     */
    private static final int JOB_ID = 1;
    private static final String TAG = HomeActivity.class.getSimpleName();

    /**
     * The controller class for individual pages and its Tablayout
     */
    private ViewPager viewPager;
    private TabLayout tabLayout;
    //------------
    private static final String MY_INTENT = "com.ftech.criptoapp.cryptservice.CUSTOM_INTENT";
    private static final String CONNECTION_INTENT = "android.net.conn.CONNECTIVITY_CHANGE";

    /**
     * Declares the drawer layout
     */
    private DrawerLayout mDrawerLayout;

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
     * Used to check network status
     */
    boolean online;
    /**
     * intent for calling up broadcastReceiver
     */
    private Intent intent;
    ProgressBar progressBar;
    /**
     * Use this to catch the intent sent from the TaskSchedulerService class
     */
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public IBinder peekService(Context myContext, Intent service) {
            return super.peekService(myContext, service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            //TODO: Remove
            //Toast.makeText(context, "Intent detected", Toast.LENGTH_SHORT).show();

            if (intent.getAction().equals(MY_INTENT)) {
                //TODO: Remove
                //Toast.makeText(context, "MY_INTENT", Toast.LENGTH_SHORT).show();

                /*MenuItem refreshMenuItem = menu.findItem(R.id.menu_refresh);
                refreshMenuItem.setVisible(true);*/
                getLoaderManager().restartLoader(CRYPTOCURRENCY_LOADER_ID, null, HomeActivity.this);
//                if ( !getLoaderManager().getLoader(CRYPTOCURRENCY_LOADER_ID).isStarted()) {
//                    refreshMenuItem.setVisible(false);
//                }
            }

            // Set the network menu status
            if (intent.getAction().equals(CONNECTION_INTENT)) {

                //TODO: Remove
                //Toast.makeText(context, "CONNECT_INTENT", Toast.LENGTH_SHORT).show();

                status = NetworkUtil.getConnectivityStatusString(context);
                online = (Objects.equals(status, "Wifi enabled") || Objects.equals(status, "Mobile data enabled"));
                //supportInvalidateOptionsMenu();
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the intent here
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MY_INTENT);
        intentFilter.addAction(CONNECTION_INTENT);
        registerReceiver(this.broadcastReceiver, intentFilter);

        // Initializing JobScheduler
        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        mJobScheduler.schedule(new JobInfo.Builder(JOB_ID,
                new ComponentName(this, TaskSchedulerService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(1000)
                .build());
        intent = new Intent(this, BroadcastService.class);
        // Get a reference to the ConnectivityManager to check state of network connectivity
        Log.i(LOG_TAG, "TEST: Connectivity Manager Instance created ...");
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //check internet connection
        Log.i(LOG_TAG, "TEST: Internet connection checked ...");
        NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            //call.run();
            //This is where my sync code will be, but for testing purposes I only have a Log statement
            Log.v("Sync_test", "this will run every minute");
            // Get a reference to the loader manager in order to interact with loaders
            Log.i(LOG_TAG, "TEST: Get the LoadManager being used ...");
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            Log.i(LOG_TAG, "TEST: Calling initloader()...");
            loaderManager.initLoader(CRYPTOCURRENCY_LOADER_ID, null, HomeActivity.this);
        }


        // set the content activity to use for the activity_home.xml layout file
        setContentView(R.layout.activity_home);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        // Find the view pager that will allow the user to swipe between fragments
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        //
        optionIcon = (ImageView)findViewById(R.id.optionIcon);
        //Create an adapter that knows which fragment should be shown on each page
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager(),
                HomeActivity.this);

        // Set the adapter onto the view pager
        assert viewPager != null;
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        BTC_ETH = "btc_value";
                        //Toast.makeText(HomeActivity.this, "BTC active", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        BTC_ETH = "eth_value";
                        //Toast.makeText(HomeActivity.this, "ETH active", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //
        tabLayout = (TabLayout) findViewById(R.id.sliding_tab);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);


        // Create a Navigation drawer and inflate the nav_view
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        // Add menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        //This icon opens the drawer
        optionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        // Set behavior of Navigation drawer
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    // This method will trigger on item Click of navigation menu
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // TODO: handle navigation\
                        switch (menuItem.getItemId()){
                            case R.id.home:
                                switchFragment(0);
                                break;
                            case R.id.new_card:
                                Intent cardViewIntent = new Intent(HomeActivity.this, CardActivity.class);
                                cardViewIntent.putExtra("CURRENCY_CODE", BTC_ETH);
                                cardViewIntent.putExtra("COLUMN_NAME", 0);
                                startActivity(cardViewIntent);
                                SharedPreferences newCardFromButtonPref0  = getSharedPreferences("newCardFromButtonPref", MODE_PRIVATE);
                                newCardFromButtonPref0.edit().clear().apply();
                                break;
                            case R.id.view_all:
                                //This preference determines how the CreatedCard activity will react to its items when clicked
                                SharedPreferences newCardFromButtonPref  = getSharedPreferences("newCardFromButtonPref", MODE_PRIVATE);
                                SharedPreferences.Editor newCardEditor = newCardFromButtonPref.edit();
                                newCardEditor.putBoolean("view_all_from_menu",true);
                                newCardEditor.apply();
                                Intent allCards = new Intent(HomeActivity.this, CreatedCards.class);
                                startActivity(allCards);
                            case R.id.exit:
                                //finish();
                                break;
                            default: ;
                        }
                        // Set item in checked state
                        menuItem.setChecked(false);
                        // Closing drawer on item click
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });

//        Add floating action button to the main activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cardViewIntent = new Intent(HomeActivity.this, CardActivity.class);
                cardViewIntent.putExtra("CURRENCY_CODE", BTC_ETH);
                cardViewIntent.putExtra("COLUMN_NAME", 0);
                startActivity(cardViewIntent);
                SharedPreferences newCardFromButtonPref  = getSharedPreferences("newCardFromButtonPref", MODE_PRIVATE);
                newCardFromButtonPref.edit().clear().apply();
            }
        });
    }
    void switchFragment(int target){
        viewPager.setCurrentItem(target);
    }
    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        //TODO: Simplify this
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    public void onResume() {
        super.onResume();
        startService(intent);
        registerReceiver(broadcastReceiver, new IntentFilter(MY_INTENT));
        registerReceiver(broadcastReceiver, new IntentFilter(CONNECTION_INTENT));

    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    public Loader<List<Currency>> onCreateLoader(int id, Bundle args) {

        // Create a new loader for the given URL
        Log.i(LOG_TAG, "TEST: onCreateLoader() called ...");
        //progressBar = new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        // Setup the baseURI
        Uri baseUri = Uri.parse(CRYPTO_CURRENRY_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("fsyms", "ETH,BTC");
        uriBuilder.appendQueryParameter("tsyms", "USD,EUR,NGN,RUB,CAD,JPY,GBP,AUD,INR,HKD,IDR,SGD,CHF,CNY,ZAR,THB,SAR,KRW,GHS,BRL");

        Log.i(LOG_TAG, "TEST: uriBuilder String" + uriBuilder.toString());

        return new CrytoCurrencyLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Currency>> loader, List<Currency> data) {
        //TODO: Load the information from CryptocrrencyQueryUtils into database using content provider
        Log.i(LOG_TAG, "TEST: onLoadFinished() called ...");
        Log.i(LOG_TAG, "TEST: Database data insertion started ...");
        // Create a ContentValues class object
        ContentValues values = new ContentValues();
        // Check if database table already present, if it exists
        // then update current records instead of inserting.
        Log.i(LOG_TAG, "Checking if database is present...");
        boolean found = isTableExists();
        Log.i(LOG_TAG, "Found: " + found + "...");
        try {
            if (found) {
                try {
                    for (Currency element : data) {
                        values.put(CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE, element.getcEthValue());
                        values.put(CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE, element.getcBtcValue());
                        // Update database
                        int mRowsUpdated = getContentResolver().update(
                                CryptoContract.CurrencyEntry.CONTENT_URI,
                                values,
                                "_id = ?",
                                new String[]{String.valueOf(element.getcId())}
                        );
                        // Log data insertion to catch any errors
                        // TODO: Remove
                        Log.v("HomeActivity_db_update", "New row ID " + mRowsUpdated + " Element id " + element.getcId());
                        Log.i("Row_Entry " + mRowsUpdated, element.getcName() + " " + element.getcEthValue() + " " + element.getcBtcValue());
                    }
                    Log.i(LOG_TAG, "TEST: Database data update finished ...");
                } catch (NullPointerException e) {
                    Log.i(LOG_TAG, "Update error iterating over the data ... " + e);
                } catch (IllegalFormatException f) {
                    Log.i(LOG_TAG, "Update format error ... " + f);
                }
            } else {
                try {
                    for (Currency element : data) {
                        values.put(CryptoContract.CurrencyEntry.COLUMN_CURRENCY_NAME, element.getcName());
                        values.put(CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE, element.getcEthValue());
                        values.put(CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE, element.getcBtcValue());
                        // Insert data into SQLiteDatabase
                        Uri uri = getContentResolver().insert(CryptoContract.CurrencyEntry.CONTENT_URI, values);
                        // Log data insertion to catch any errors
                        // TODO: Remove
                        Log.v("HomeActivity", "Insert new row ID " + uri);
                        Log.i("Row Entry ", element.getcName() + " " + element.getcEthValue() + " " + element.getcBtcValue());
                    }
                    //TODO: Remove
                    Log.i(LOG_TAG, "TEST: Database data insertion finished ...");

                } catch (NullPointerException e) {
                    Log.i(LOG_TAG, "database insert error no data to iterate over ... " + e);
                } catch (IllegalFormatException f) {
                    Log.i(LOG_TAG, "Update format error ... " + f);
                }
            }
        } catch (NullPointerException g) {
            Log.i(LOG_TAG, "Database existent confirmation error " + g);
        } catch (IllegalFormatException f) {
            Log.i(LOG_TAG, "Update format error ... " + f);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Currency>> loader) {

        //TODO: Remove
        Log.i(LOG_TAG, "TEST: onLoadReset() called ...");
        getLoaderManager().destroyLoader(CRYPTOCURRENCY_LOADER_ID);

    }

    /**
     * Used to determine if the database exists
     * so either an update is done or insert.
     *
     * @return true
     */
    public boolean isTableExists() {

        String[] projection = {

                CryptoContract.CurrencyEntry._ID,
                CryptoContract.CurrencyEntry.COLUMN_CURRENCY_NAME,
                CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE,
                CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE

        };

        Cursor cursor = getContentResolver().query(CryptoContract.CurrencyEntry.CONTENT_URI, projection, null, null, null);

        assert cursor != null;
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    @Override
    protected void onStop() {
        stopService(intent);
        unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}




