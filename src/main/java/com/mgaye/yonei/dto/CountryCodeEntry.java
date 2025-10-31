package com.mgaye.yonei.dto;

import lombok.Data;

import lombok.Data;

@Data
public class CountryCodeEntry {
    private String prefix;
    private String code;
    private String name;
    private String currency;
    private String currencySymbol; // Added this field

}
