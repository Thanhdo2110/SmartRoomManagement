package com.example.smartroommanagement.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "SmartRoomSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_REMEMBER_USER = "remember_username";
    private static final String KEY_REMEMBER_PASS = "remember_password";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(int userId, String username, String password) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_REMEMBER_USER, username);
        editor.putString(KEY_REMEMBER_PASS, password);
        editor.apply();
    }

    // Cập nhật mật khẩu mới vào SharedPreferences để đồng bộ gợi ý đăng nhập
    public void updateSavedPassword(String newPassword) {
        editor.putString(KEY_REMEMBER_PASS, newPassword);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getSavedUsername() {
        return pref.getString(KEY_REMEMBER_USER, "");
    }

    public String getSavedPassword() {
        return pref.getString(KEY_REMEMBER_PASS, "");
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public void logoutUser() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.putInt(KEY_USER_ID, -1);
        editor.apply();
    }
}
