package com.yuntechstudent.yuntechapp.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.yuntechstudent.yuntechapp.MainActivity;

import java.util.Map;

public class LoginViewModel extends ViewModel {

    public enum AuthenticationState {
        UNAUTHENTICATED,        // Initial state, the user needs to authenticate
        AUTHENTICATED,          // The user has authenticated successfully
        INVALID_AUTHENTICATION  // Authentication failed
    }
    final public MutableLiveData<AuthenticationState> authenticationState = new MutableLiveData<>();
    private MutableLiveData<String> account;
    private MutableLiveData<String> password;
    private MutableLiveData<Boolean> keepAccount;
    private MutableLiveData<Boolean> keepLogin;
    private MutableLiveData<String> profileName;
    private MutableLiveData<String> profileMajor;
    private MutableLiveData<String> profileImage;


    public LoginViewModel() {
        authenticationState.setValue(AuthenticationState.UNAUTHENTICATED);

        account = new MutableLiveData<>();
        setAccount(MainActivity.keyStoreHelper.decrypt(MainActivity.preferencesHelper.getInput("ACCOUNT")));

        password = new MutableLiveData<>();
        setPassword(MainActivity.keyStoreHelper.decrypt(MainActivity.preferencesHelper.getInput("PASSWORD")));

        keepAccount = new MutableLiveData<>();
        setKeepAccount(Boolean.parseBoolean(MainActivity.preferencesHelper.getInput("KEEP_ACCOUNT")));

        keepLogin = new MutableLiveData<>();
        setKeepLogin(Boolean.parseBoolean(MainActivity.preferencesHelper.getInput("KEEP_LOGIN")));

        profileName = new MutableLiveData<>();
        profileMajor = new MutableLiveData<>();
        profileImage = new MutableLiveData<>();


    }

    //--------------------登入驗證--------------------//
    public void authenticate(Map status, Map Profile) {
        profileName.postValue(Profile.get("student_name").toString());
        profileMajor.postValue(Profile.get("student_major").toString());
        profileImage.postValue(Profile.get("student_image").toString());
        if (status.get("status").toString().equals("success")) {
            storeAccountData();
            authenticationState.postValue(AuthenticationState.AUTHENTICATED);
        }
        else {
            keepLogin.postValue(false);
            MainActivity.preferencesHelper.setInput("KEEP_LOGIN", String.valueOf(false));
            authenticationState.postValue(AuthenticationState.INVALID_AUTHENTICATION);
        }
    }
    //--------------------取消登入狀態--------------------//
    public void refuseAuthentication(Boolean loginFail) {
        profileName.postValue("未登入");
        profileMajor.postValue("請先登入");
        profileImage.postValue("");

        keepLogin.postValue(false);
        MainActivity.preferencesHelper.setInput("KEEP_LOGIN", String.valueOf(false));
        if(!loginFail){
            password.postValue("");
            MainActivity.preferencesHelper.setInput("PASSWORD", "");
        }

        switch(authenticationState.getValue()){
            case UNAUTHENTICATED:
                break;
            default:
                authenticationState.setValue(AuthenticationState.UNAUTHENTICATED);
        }
    }

    //--------------------儲存帳密--------------------//
    public void storeAccountData(){
        String recoredAccount = keepAccount.getValue() ? account.getValue() : "";
        String recoredPassword = keepLogin.getValue() ? password.getValue() : "";
        MainActivity.preferencesHelper.setInput("ACCOUNT", MainActivity.keyStoreHelper.encrypt(recoredAccount));
        MainActivity.preferencesHelper.setInput("PASSWORD", MainActivity.keyStoreHelper.encrypt(recoredPassword));
        MainActivity.preferencesHelper.setInput("KEEP_ACCOUNT", String.valueOf(keepAccount.getValue()));
        MainActivity.preferencesHelper.setInput("KEEP_LOGIN", String.valueOf(keepLogin.getValue()));
    }

    //--------------------處理監聽--------------------//
    public void setAccount(String input){
        if(account.getValue() == null || !account.getValue().equals(input))
            account.setValue(input);
    }
    public LiveData<String> getAccount(){
        return account;
    }

    public void setPassword(String input){
        if(password.getValue() == null || !password.getValue().equals(input))
            password.setValue(input);
    }
    public LiveData<String> getPassword(){
        return password;
    }

    public void setKeepAccount(Boolean input){
        if(keepAccount.getValue() == null || keepAccount.getValue() != input)
            keepAccount.setValue(input);
    }
    public LiveData<Boolean> getKeepAccount(){
        return keepAccount;
    }

    public void setKeepLogin(Boolean input){
        if(keepLogin.getValue() == null || keepLogin.getValue() != input)
            keepLogin.setValue(input);
    }
    public LiveData<Boolean> getKeepLogin(){
        return keepLogin;
    }

    public LiveData<String> getProfileName(){
        return profileName;
    }
    public LiveData<String> getProfileMajor(){
        return profileMajor;
    }
    public LiveData<String> getProfileImage(){
        return profileImage;
    }

}

