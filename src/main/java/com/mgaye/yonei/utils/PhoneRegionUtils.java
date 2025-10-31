package com.mgaye.yonei.utils;

import java.util.HashMap;
import java.util.Map;

public class PhoneRegionUtils {

    // ✅ Map prefix ➜ Country Code
    private static final Map<String, String> prefixToCountry = new HashMap<>();

    // ✅ Map country ➜ Currency
    private static final Map<String, String> countryToCurrency = new HashMap<>();

    static {
        // 🌍 West Africa
        prefixToCountry.put("+225", "CI");
        prefixToCountry.put("+229", "BJ");
        prefixToCountry.put("+226", "BF");
        prefixToCountry.put("+223", "ML");
        prefixToCountry.put("+227", "NE");
        prefixToCountry.put("+228", "TG");
        prefixToCountry.put("+221", "SN");
        prefixToCountry.put("+220", "GM");
        prefixToCountry.put("+231", "LR");
        prefixToCountry.put("+232", "SL");
        prefixToCountry.put("+224", "GN");
        prefixToCountry.put("+245", "GW");
        prefixToCountry.put("+234", "NG");
        prefixToCountry.put("+238", "CV");
        prefixToCountry.put("+222", "MR");

        // 🌎 Global
        prefixToCountry.put("+1", "US"); // default to US
        prefixToCountry.put("+86", "CN");
        prefixToCountry.put("+81", "JP");
        prefixToCountry.put("+49", "DE");
        prefixToCountry.put("+44", "GB");
        prefixToCountry.put("+33", "FR");
        prefixToCountry.put("+39", "IT");
        prefixToCountry.put("+55", "BR");
        prefixToCountry.put("+7", "RU");
        prefixToCountry.put("+91", "IN");
        prefixToCountry.put("+82", "KR");
        prefixToCountry.put("+34", "ES");
        prefixToCountry.put("+61", "AU");
        prefixToCountry.put("+1-CA", "CA"); // Canada override if needed

        // 💱 Currency mapping
        countryToCurrency.put("CI", "XOF");
        countryToCurrency.put("BJ", "XOF");
        countryToCurrency.put("BF", "XOF");
        countryToCurrency.put("ML", "XOF");
        countryToCurrency.put("NE", "XOF");
        countryToCurrency.put("TG", "XOF");
        countryToCurrency.put("SN", "XOF");
        countryToCurrency.put("GM", "GMD");
        countryToCurrency.put("LR", "LRD");
        countryToCurrency.put("SL", "SLL");
        countryToCurrency.put("GN", "GNF");
        countryToCurrency.put("GW", "XOF");
        countryToCurrency.put("NG", "NGN");
        countryToCurrency.put("CV", "CVE");
        countryToCurrency.put("MR", "MRU");

        countryToCurrency.put("US", "USD");
        countryToCurrency.put("CA", "CAD");
        countryToCurrency.put("CN", "CNY");
        countryToCurrency.put("JP", "JPY");
        countryToCurrency.put("DE", "EUR");
        countryToCurrency.put("GB", "GBP");
        countryToCurrency.put("FR", "EUR");
        countryToCurrency.put("IT", "EUR");
        countryToCurrency.put("BR", "BRL");
        countryToCurrency.put("RU", "RUB");
        countryToCurrency.put("IN", "INR");
        countryToCurrency.put("KR", "KRW");
        countryToCurrency.put("ES", "EUR");
        countryToCurrency.put("AU", "AUD");
    }

    /**
     * ✅ Get ISO country code (e.g. SN) from a phone prefix (+221)
     */
    public static String countryFromPrefix(String prefix) {
        if (prefix == null)
            return null;
        return prefixToCountry.getOrDefault(prefix.trim(), "UNKNOWN");
    }

    /**
     * ✅ Get currency (e.g. XOF) from a country code (e.g. SN)
     */
    public static String currencyFromCountry(String countryCode) {
        if (countryCode == null)
            return null;
        return countryToCurrency.getOrDefault(countryCode.trim(), "USD");
    }

    /**
     * ✅ Optional helper: get currency directly from prefix
     */
    public static String currencyFromPrefix(String prefix) {
        String country = countryFromPrefix(prefix);
        return currencyFromCountry(country);
    }
}
