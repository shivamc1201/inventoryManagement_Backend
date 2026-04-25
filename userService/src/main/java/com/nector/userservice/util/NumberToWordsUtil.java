package com.nector.userservice.util;

public class NumberToWordsUtil {

    private static final String[] ones = {
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] tens = {
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public static String convertToWords(double amount) {
        long rupees = (long) amount;
        long paise = Math.round((amount - rupees) * 100);

        String result = convertRupees(rupees);
        if (paise > 0) {
            result += " and " + convertBelowHundred((int) paise) + " Paise";
        }
        return result + " Only";
    }

    private static String convertRupees(long n) {
        if (n == 0) return "Zero Rupees";
        return convertIndianSystem(n) + " Rupees";
    }

    private static String convertIndianSystem(long n) {
        if (n == 0) return "";

        StringBuilder sb = new StringBuilder();

        long crore = n / 10000000;
        n %= 10000000;
        long lakh = n / 100000;
        n %= 100000;
        long thousand = n / 1000;
        n %= 1000;
        long hundred = n / 100;
        n %= 100;

        if (crore > 0) sb.append(convertBelowHundred((int) crore)).append(" Crore ");
        if (lakh > 0) sb.append(convertBelowHundred((int) lakh)).append(" Lakh ");
        if (thousand > 0) sb.append(convertBelowHundred((int) thousand)).append(" Thousand ");
        if (hundred > 0) sb.append(ones[(int) hundred]).append(" Hundred ");
        if (n > 0) sb.append(convertBelowHundred((int) n));

        return sb.toString().trim();
    }

    private static String convertBelowHundred(int n) {
        if (n < 20) return ones[n];
        return tens[n / 10] + (n % 10 != 0 ? " " + ones[n % 10] : "");
    }
}

