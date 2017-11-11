package com.ftech.criptoapp.data;

/**
 * Created by FRED.
 * This class provides currency symbol for each Fragment
 */

public class CurrencyHelper {

    public static String getCurrencySymbol(String cur) {

        String symbol = null;

        switch (cur) {
            case "USD":
            case "AUD":
            case "CAD":
            case "HKD":
            case "SGD":
                symbol = "$";
                break;
            case "JPY":
            case "CNY":
                symbol = "¥";
                break;
            case "RUB":
                symbol = "p.";
                break;
            case "NGN":
                symbol = "₦";
                break;
            case "GBP":
                symbol = "£";
                break;
            case "INR":
                symbol = "₹";
                break;
            case "THB":
                symbol = "฿";
                break;
            case "SAR":
                symbol = "ر.س";
                break;
            case "KRW":
                symbol = "₩";
                break;
            case "BRL":
                symbol = "R$";
                break;
            case "EUR":
                symbol = "€";
                break;
            case "IDR":
                symbol = "Rp";
                break;
            case "ZAR":
                symbol = "R";
                break;
            case "CHF":
                symbol = "₣";
                break;
            case "GHS":
                symbol =  "₵";
                break;

        }

        return symbol;
    }




}
