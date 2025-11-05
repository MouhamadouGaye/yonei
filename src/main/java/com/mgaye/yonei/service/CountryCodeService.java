package com.mgaye.yonei.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgaye.yonei.dto.CountryCodeEntry;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
public class CountryCodeService {

    // @Async
    // @PostConstruct
    // public void loadCountryCodes() {
    // try {
    // ObjectMapper mapper = new ObjectMapper();
    // InputStream is = getClass().getResourceAsStream("/country-codes.json");

    // // System.out.println("Data Stream: " + is);
    // if (is == null) {
    // throw new RuntimeException("country-codes.json not found in resources!");
    // }
    // codes = mapper.readValue(is, new TypeReference<List<CountryCodeEntry>>() {
    // });
    // } catch (Exception e) {
    // throw new RuntimeException("Failed to load country codes", e);
    // }
    // }

    // // Get full country info for display
    // public CountryCodeEntry getCountryByPhoneNumber(String phoneNumber) {
    // if (phoneNumber == null)
    // return null;

    // String normalized = phoneNumber.replaceAll("\\s+", "").replaceAll("-", "");
    // return codes.stream()
    // .filter(c -> normalized.startsWith(c.getPrefix()))
    // .findFirst()
    // .orElse(null);

    // }

    // // Get just the currency symbol for quick display
    // public String getCurrencySymbolByPhoneNumber(String phoneNumber) {
    // CountryCodeEntry country = getCountryByPhoneNumber(phoneNumber);
    // return country != null ? country.getCurrencySymbol() : "$"; // Default to USD
    // symbol
    // }

    private List<CountryCodeEntry> codes;
    private boolean loaded = false;

    // Remove @Async to ensure it loads synchronously
    @PostConstruct
    public void loadCountryCodes() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/country-codes.json");
            System.out.println("Data Sream: " + is);

            if (is == null) {
                throw new RuntimeException("country-codes.json not found in resources!");
            }
            codes = mapper.readValue(is, new TypeReference<List<CountryCodeEntry>>() {
            });

            loaded = true;
            System.out.println("✅ Country codes loaded: " + codes.size() + " countries");

        } catch (Exception e) {
            throw new RuntimeException("Failed to load country codes", e);
        }
    }

    public CountryCodeEntry getCountryByPhoneNumber(String phoneNumber) {
        if (!loaded) {
            throw new IllegalStateException("Country codes not loaded yet");
        }

        if (phoneNumber == null)
            return null;

        String normalized = phoneNumber.replaceAll("\\s+", "").replaceAll("-", "");
        return codes.stream()
                .filter(c -> normalized.startsWith(c.getPrefix()))
                .findFirst()
                .orElse(null);
    }

    // Get currency code
    public String getCurrencyByPhoneNumber(String phoneNumber) {
        CountryCodeEntry country = getCountryByPhoneNumber(phoneNumber);
        return country != null ? country.getCurrency() : "USD"; // Default to USD
    }

    // Get country name
    public String getCountryNameByPhoneNumber(String phoneNumber) {
        CountryCodeEntry country = getCountryByPhoneNumber(phoneNumber);
        return country != null ? country.getName() : "International";
    }

    public List<CountryCodeEntry> getAllCountryCodes() {
        return codes;
    }
}
