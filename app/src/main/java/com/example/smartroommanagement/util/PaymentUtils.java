package com.example.smartroommanagement.util;

import android.net.Uri;
import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;

public class PaymentUtils {
    // Default values if user hasn't configured their own
    private static final String DEFAULT_BANK_ID = "mbbank";
    private static final String DEFAULT_ACCOUNT_NO = "0325789630";
    private static final String DEFAULT_ACCOUNT_NAME = "NGUYEN THANH DO";
    private static final String TEMPLATE = "compact2";

    private static final Map<String, String> BANK_MAP = new HashMap<>();
    static {
        // MB Bank
        BANK_MAP.put("mb", "mbbank");
        BANK_MAP.put("mbbank", "mbbank");
        BANK_MAP.put("mb bank", "mbbank");
        
        // Vietcombank
        BANK_MAP.put("vcb", "vcb");
        BANK_MAP.put("vietcombank", "vcb");
        
        // Vietinbank
        BANK_MAP.put("vtb", "vietinbank");
        BANK_MAP.put("vietinbank", "vietinbank");
        
        // Techcombank
        BANK_MAP.put("tcb", "tcb");
        BANK_MAP.put("techcombank", "tcb");
        
        // BIDV
        BANK_MAP.put("bidv", "bidv");
        
        // Agribank
        BANK_MAP.put("agribank", "agribank");
        BANK_MAP.put("agri", "agribank");
        
        // TPBank
        BANK_MAP.put("tpb", "tpb");
        BANK_MAP.put("tpbank", "tpb");
        
        // VPBank
        BANK_MAP.put("vpb", "vpbank");
        BANK_MAP.put("vpbank", "vpbank");
        
        // ACB
        BANK_MAP.put("acb", "acb");
        
        // Sacombank
        BANK_MAP.put("stb", "sacombank");
        BANK_MAP.put("sacombank", "sacombank");
    }

    /**
     * Normalizes bank input to VietQR supported bank ID
     */
    public static String normalizeBankId(String input) {
        if (TextUtils.isEmpty(input)) return DEFAULT_BANK_ID;
        
        String cleanInput = input.trim().toLowerCase().replaceAll("\\s+", " ");
        if (BANK_MAP.containsKey(cleanInput)) {
            return BANK_MAP.get(cleanInput);
        }
        
        // If not in map, just remove spaces and hope it works (VietQR usually uses short names)
        return cleanInput.replace(" ", "");
    }

    /**
     * Generates a VietQR URL for payment using specific bank details
     */
    public static String getQrUrl(double amount, String description, String bankId, String accountNo, String accountName) {
        String bId = normalizeBankId(bankId);
        String accNo = TextUtils.isEmpty(accountNo) ? DEFAULT_ACCOUNT_NO : accountNo;
        String accName = TextUtils.isEmpty(accountName) ? DEFAULT_ACCOUNT_NAME : accountName;
        
        String encodedDesc = Uri.encode(description);
        String url = String.format("https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                bId, accNo, TEMPLATE, (long) amount, encodedDesc, Uri.encode(accName));
        
        android.util.Log.d("QR_PAYMENT", "Generated URL: " + url);
        return url;
    }

    /**
     * Legacy method for backward compatibility or default usage
     */
    public static String getQrUrl(double amount, String description) {
        return getQrUrl(amount, description, DEFAULT_BANK_ID, DEFAULT_ACCOUNT_NO, DEFAULT_ACCOUNT_NAME);
    }
}
