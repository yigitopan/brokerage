package com.opan.brokerageapi.utils;

public class IBANValidator {

    public static boolean isValid(String IBAN) {
        return IBAN != null && IBAN.matches("[A-Z0-9]+") && IBAN.length() >= 15 && IBAN.length() <= 34;
    }
}