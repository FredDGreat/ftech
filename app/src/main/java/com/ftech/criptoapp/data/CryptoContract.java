package com.ftech.criptoapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by FRED.
 */

public final class CryptoContract {

    public static final String CONTENT_AUTHORITY = "com.ftech.criptoapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible psth ( append to base content URI for possible URI's)
    public static final String PATH_CURRENCY = "currency";


    // TO prevent accidental instantiation of this class,
    // give it an empty constructor
    private CryptoContract() {}

    /**
     * Inner class that defines constant values for the pets database table.
     * Each entry in the table represents a single pet.
     */
    public static final class CurrencyEntry implements BaseColumns {

        // THe content URI to access the currency data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CURRENCY);


        // Name of the database table
        public final static String TABLE_NAME = "rates";


        /**
         * Unique ID number for the eth and btc (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Currency name
         *
         * Type: TEXT
         */
        public final static String COLUMN_CURRENCY_NAME = "cur_name";

        /**
         * Eth value
         *
         * Type: BigDecimal
         */
        public final static String COLUMN_ETH_VALUE = "eth_value";

        /**
         * Btc value
         *
         * Type: BigDecimal
         */
        public final static String COLUMN_BTC_VALUE = "btc_value";

    }


}
