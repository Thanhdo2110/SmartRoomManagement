package com.example.smartroommanagement.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class FinanceUtils {
    
    public static double calculateTotal(double basePrice, double electricityUsage, double electricityPrice, double waterUsage, double waterPrice) {
        return basePrice + (electricityUsage * electricityPrice) + (waterUsage * waterPrice);
    }

    public static String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    /**
     * Tự động thêm dấu phân cách hàng nghìn (.) khi người dùng nhập tiền
     */
    public static void addCurrencyFormatter(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[.,]", "");

                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.GERMANY); // Dùng chuẩn Đức để có dấu . phân cách
                            formatter.applyPattern("#,###");
                            String formatted = formatter.format(parsed);

                            current = formatted;
                            editText.setText(formatted);
                            editText.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            // Ignored
                        }
                    } else {
                        current = "";
                        editText.setText("");
                    }

                    editText.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Lấy giá trị số thực từ chuỗi có chứa dấu . phân cách
     */
    public static double parseFormattedCurrency(String text) {
        if (text == null || text.isEmpty()) return 0;
        try {
            return Double.parseDouble(text.replaceAll("[.,]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
