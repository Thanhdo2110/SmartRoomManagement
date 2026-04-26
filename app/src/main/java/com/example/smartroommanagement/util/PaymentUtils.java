package com.example.smartroommanagement.util;

import android.net.Uri;

public class PaymentUtils {
    // Bank details provided by user
    private static final String BANK_ID = "mbbank";
    private static final String ACCOUNT_NO = "0325789630";
    private static final String ACCOUNT_NAME = "NGUYEN THANH DO";
    private static final String TEMPLATE = "compact2"; // QR template with info

    /**
     * Generates a VietQR URL for payment
     * @param amount The total amount to pay
     * @param description Payment description (e.g., Room 101 May 2024)
     * @return String URL to the QR image
     */
    public static String getQrUrl(double amount, String description) {
        String encodedDesc = Uri.encode(description);
        return String.format("https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                BANK_ID, ACCOUNT_NO, TEMPLATE, (long) amount, encodedDesc, Uri.encode(ACCOUNT_NAME));
    }
}
