package com.ftech.criptoapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ftech.criptoapp.cardview.CardActivity;
import com.ftech.criptoapp.data.CryptoContract;
import com.ftech.criptoapp.data.CurrencyDBHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.security.AccessController.getContext;

/**
 * Created by FRED.
 */

public class CreatedCards extends AppCompatActivity implements View.OnClickListener{
    //The view that adds new card to this activity
    private View rootView;
    //the inflater machanism
    LayoutInflater inflater;
    private LinearLayout cardBody;
    private ImageView backBtn;
    //This preference determines how the CreatedCard activity will react to its items when clicked
    SharedPreferences newCardFromButtonPref;
    SharedPreferences.Editor newCardEditor;
    //the loop control variable
    int i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_card_layout);
        //title textView
        TextView title = (TextView)findViewById(R.id.title);
        title.setText("All Cards");
        //------
        //making instance of a persistent storage for determining when this activity is opened via menu in HomeActivity
        newCardFromButtonPref  = getSharedPreferences("newCardFromButtonPref", MODE_PRIVATE);
        newCardEditor = newCardFromButtonPref.edit();
        //--------
        cardBody = (LinearLayout)findViewById(R.id.cardBody);
        //---------
        backBtn = (ImageView)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this);
        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //making instance of a persistent storage
        final SharedPreferences dataPref  = getSharedPreferences("NEW_CARD_DATA", MODE_PRIVATE);
        final SharedPreferences.Editor dataEditor = dataPref.edit();
        //count number of cards created
        final SharedPreferences pref4newCardCount = getSharedPreferences("NEW_CARD_COUNTER",MODE_PRIVATE);
        final SharedPreferences.Editor e = pref4newCardCount.edit();
        //-------
        //draws the original position of any card as it was created from CardActivity
        //final int spinnerPos,curSpinnerPos;
        if(pref4newCardCount.getBoolean("start_counter",false)){
            int c = pref4newCardCount.getInt("card_tracker", -1);
            do{
                i++;
                TextView date,currencySymbol,currencyCode,rate,currencyCoin;
                final String coin_value,coinRate;
                rootView = inflater.inflate(R.layout.new_card_list_item, null, false);
                //currencySymbol
                coin_value = dataPref.getString("currency_code"+i,null);
                coinRate = dataPref.getString("curValue"+i,null);
                final int spinnerPos = dataPref.getInt("spinnerPosition"+i,-1);
                final int curSpinnerPos = dataPref.getInt("curSpinnerPosition"+i,-1);
                rate = (TextView)rootView.findViewById(R.id.rate);
                date = (TextView)rootView.findViewById(R.id.date);
                currencySymbol = (TextView)rootView.findViewById(R.id.currency_symbol);
                currencyCode = (TextView)rootView.findViewById(R.id.currency_code);
                currencyCoin = (TextView)rootView.findViewById(R.id.currencyCoin);
                final String curSpinner = dataPref.getString("curSpinner"+i,null);
                final String curName = dataPref.getString("spinner"+i,null);
                final String logoText = dataPref.getString("logoText"+i,null);
                final String convertBox = dataPref.getString("convertBox"+i,null);
                //--------
                currencyCoin.setText(dataPref.getString("curSpinner"+i,null));
                currencySymbol.setText(dataPref.getString("logoText"+i,null));
                rate.setText(dataPref.getString("curValue"+i,null));
                date.setText(dataPref.getString("date"+i,null));
                currencyCode.setText(dataPref.getString("spinner"+i,null));
                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    extractData(curSpinner,curName,coinRate,convertBox,logoText,coin_value,spinnerPos,curSpinnerPos);
                    }
                });
                cardBody.addView(rootView,0);
            }while(i < c);
        }
    }
    private void extractData(String curSpinner,String curName,String coinRate,String convertBox,String logoText,String coin_value,
                             int spinnerPos,int curSpinnerPos) {
        final SharedPreferences dataPref  = getSharedPreferences("NEW_CARD_DATA", MODE_PRIVATE);
        final SharedPreferences.Editor dataEditor = dataPref.edit();
                    /*newCardEditor.putBoolean("view_all_from_menu",true);
                    newCardEditor.apply();*/
        if(newCardFromButtonPref.getBoolean("view_all_from_menu",false)){
            //--------------
            dataEditor.putString("curSpinner",curSpinner);
            dataEditor.putString("spinner",curName);
            dataEditor.putString("curValue",coinRate);
            dataEditor.putString("convertBox",convertBox);
            dataEditor.putString("logoText",logoText);
            dataEditor.putString("currency_code",coin_value);
            dataEditor.putInt("spinnerPosition",spinnerPos);
            dataEditor.putInt("curSpinnerPosition",curSpinnerPos);
            dataEditor.putBoolean("isEditedItem",true);
            dataEditor.apply();
            //-----------
            Intent cardViewIntent = new Intent(rootView.getContext(), CardActivity.class);
            cardViewIntent.putExtra("CURRENCY_CODE", coin_value);
            cardViewIntent.putExtra("COLUMN_NAME", spinnerPos);
            startActivity(cardViewIntent);
            /*Toast.makeText(CreatedCards.this, "view from menu flag = "+newCardFromButtonPref.getBoolean("view_all_from_menu",false)
                    , Toast.LENGTH_SHORT).show();*/
            newCardFromButtonPref.edit().clear().apply();
            finish();
        }else{
            //--------------
            dataEditor.putString("curSpinner",curSpinner);
            dataEditor.putString("spinner",curName);
            dataEditor.putString("curValue",coinRate);
            dataEditor.putString("convertBox",convertBox);
            dataEditor.putString("logoText",logoText);
            dataEditor.putString("currency_code",coin_value);
            dataEditor.putInt("spinnerPosition",spinnerPos);
            dataEditor.putInt("curSpinnerPosition",curSpinnerPos);
            dataEditor.putBoolean("isEditedItem",true);
            dataEditor.apply();
            /*Toast.makeText(CreatedCards.this, "view2 from menu flag2 = "+newCardFromButtonPref.getBoolean("view_all_from_menu",false)
                    , Toast.LENGTH_SHORT).show();*/
            finish();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(getContext(), "onCreateView", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if(v == backBtn){
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
