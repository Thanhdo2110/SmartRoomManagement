package com.example.smartroommanagement.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.smartroommanagement.data.AppDatabase;
import com.example.smartroommanagement.data.entity.UserEntity;
import com.example.smartroommanagement.data.repository.UserRepository;

public class AuthViewModel extends AndroidViewModel {
    private final UserRepository repository;
    private final MutableLiveData<UserEntity> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public LiveData<UserEntity> getUserLiveData() { return userLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getRegisterSuccess() { return registerSuccess; }

    public void login(String username, String password) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity user = repository.login(username, password);
            if (user != null) {
                userLiveData.postValue(user);
            } else {
                errorLiveData.postValue("Tài khoản hoặc mật khẩu không đúng");
            }
        });
    }

    public void register(String fullName, String username, String password) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (repository.isUsernameTaken(username)) {
                errorLiveData.postValue("Tên đăng nhập đã tồn tại");
            } else {
                repository.register(new UserEntity(username, password, fullName));
                registerSuccess.postValue(true);
            }
        });
    }
}
