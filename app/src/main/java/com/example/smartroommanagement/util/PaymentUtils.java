package com.example.smartroommanagement.util;

import android.net.Uri;
import android.text.TextUtils;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class PaymentUtils {
    private static final String DEFAULT_BANK_ID = "MB";
    private static final String DEFAULT_ACCOUNT_NO = "0325789630";
    private static final String DEFAULT_ACCOUNT_NAME = "NGUYEN THANH DO";
    private static final String TEMPLATE = "compact2";

    private static final Map<String, String> BANK_MAP = new HashMap<>();

    static {
        BANK_MAP.put("mb", "MB");
        BANK_MAP.put("mbbank", "MB");
        BANK_MAP.put("mb bank", "MB");
        BANK_MAP.put("vcb", "VCB");
        BANK_MAP.put("vietcombank", "VCB");
        BANK_MAP.put("vtb", "ICB");
        BANK_MAP.put("vietinbank", "ICB");
        BANK_MAP.put("icb", "ICB");
        BANK_MAP.put("tcb", "TCB");
        BANK_MAP.put("techcombank", "TCB");
        BANK_MAP.put("bidv", "BIDV");
        BANK_MAP.put("agribank", "VBA");
        BANK_MAP.put("vba", "VBA");
        BANK_MAP.put("agri", "VBA");
        BANK_MAP.put("tpbank", "TPB");
        BANK_MAP.put("tpb", "TPB");
        BANK_MAP.put("vpbank", "VPB");
        BANK_MAP.put("vpb", "VPB");
        BANK_MAP.put("acb", "ACB");
        BANK_MAP.put("sacombank", "STB");
        BANK_MAP.put("stb", "STB");
        BANK_MAP.put("hdb", "HDB");
        BANK_MAP.put("hdbank", "HDB");
        BANK_MAP.put("vib", "VIB");
        BANK_MAP.put("shb", "SHB");
    }

    public static String normalizeBankId(String input) {
        if (TextUtils.isEmpty(input)) return DEFAULT_BANK_ID;

        String cleanInput = input.trim().toLowerCase(Locale.US).replaceAll("\\s+", " ");
        if (BANK_MAP.containsKey(cleanInput)) {
            return BANK_MAP.get(cleanInput);
        }

        String compactInput = cleanInput.replace(" ", "");
        if (BANK_MAP.containsKey(compactInput)) {
            return BANK_MAP.get(compactInput);
        }

        return compactInput.toUpperCase(Locale.US);
    }

    public static String getQrUrl(double amount, String description, String bankId, String accountNo, String accountName) {
        String bId = normalizeBankId(bankId);
        String accNo = TextUtils.isEmpty(accountNo) ? DEFAULT_ACCOUNT_NO : accountNo.trim();
        String accName = TextUtils.isEmpty(accountName) ? DEFAULT_ACCOUNT_NAME : accountName.trim();
        long roundedAmount = Math.max(0, Math.round(amount));

        String cleanDesc = sanitizeVietQrText(description);
        String cleanName = sanitizeVietQrText(accName).toUpperCase(Locale.US);

        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority("img.vietqr.io")
                .appendPath("image")
                .appendPath(bId + "-" + accNo + "-" + TEMPLATE + ".png")
                .appendQueryParameter("amount", String.valueOf(roundedAmount))
                .appendQueryParameter("addInfo", cleanDesc);

        if (!TextUtils.isEmpty(cleanName)) {
            builder.appendQueryParameter("accountName", cleanName);
        }

        String url = builder.build().toString();
        android.util.Log.d("QR_PAYMENT", "URL: " + url);
        return url;
    }

    public static String getQrUrl(double amount, String description) {
        return getQrUrl(amount, description, DEFAULT_BANK_ID, DEFAULT_ACCOUNT_NO, DEFAULT_ACCOUNT_NAME);
    }

    private static String sanitizeVietQrText(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        String noAccent = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        return noAccent
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .replaceAll("[^a-zA-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
