package com.yuntechstudent.yuntechapp.ui.login;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.yuntechstudent.yuntechapp.MainActivity;
import com.yuntechstudent.yuntechapp.R;

import java.util.Map;

import static com.yuntechstudent.yuntechapp.ui.login.LoginViewModel.AuthenticationState.AUTHENTICATED;
import static com.yuntechstudent.yuntechapp.ui.login.LoginViewModel.AuthenticationState.INVALID_AUTHENTICATION;


public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------取得本視圖、側邊欄視圖--------------------//
        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_login, container, false);
        final NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);

        //--------------------監聽View Object--------------------//
        final NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        final ImageView user_image = header.findViewById(R.id.imageView);
        final TextView text_userName = header.findViewById(R.id.text_userName);
        final TextView text_userMajor = header.findViewById(R.id.text_userMajor);
        final EditText editAccount = root.findViewById(R.id.input_account);
        final EditText editPassword = root.findViewById(R.id.input_password);
        final Switch switchKeepAccount = root.findViewById(R.id.switch_keep_account);
        final Switch switchKeepLogin = root.findViewById(R.id.switch_keep_login);

        //---監聽 authenticationState<LoginViewModel.AuthenticationState>---//
        loginViewModel.authenticationState.observe(getViewLifecycleOwner(), authenticationState -> {
            if(authenticationState.equals(AUTHENTICATED)){
                //更改側邊欄選單
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.activity_main_drawer);
                //頁面重新導向
                navController.navigate(R.id.nav_profile);
            }else if(authenticationState.equals(INVALID_AUTHENTICATION)){
                //提示登入失敗
                new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                        .setTitle("登入失敗")
                        .setMessage("可能是帳號、密碼錯誤，或目前連線狀態有問題！")
                        .setPositiveButton("確認", null)
                        .show();
                loginViewModel.refuseAuthentication(true);
            }
        });

        //---監聽 profileName<String>---//
        loginViewModel.getProfileName().observe(this, s -> {
            if(!text_userName.getText().toString().equals(s)) text_userName.setText(s);
        });

        //---監聽 profileMajor<String>---//
        loginViewModel.getProfileMajor().observe(this, s -> {
            if(!text_userMajor.getText().toString().equals(s)) text_userMajor.setText(s);
        });

        //---監聽 profileImage<String>---//
        loginViewModel.getProfileImage().observe(this, s -> {
            if(!s.equals("")){
                Snackbar.make(getView(), "登入成功：" + loginViewModel.getAccount().getValue(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                byte[] decodedString = Base64.decode(s, Base64.NO_WRAP);
                user_image.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                user_image.setPadding(0, dp2px(15),0,0);
            }else{
                user_image.setImageResource(R.drawable.yuntechbobo);
                user_image.setPadding(0, 0,0,0);
            }
        });

        //---監聽 account<String>---//
        loginViewModel.getAccount().observe(this, s -> {
            if(!editAccount.getEditableText().toString().equals(s))
                editAccount.setText(s);
        });
        editAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loginViewModel.setAccount(s.toString());
            }
        });

        //---監聽 password<String>---//
        loginViewModel.getPassword().observe(this, s -> {
            if(!editPassword.getEditableText().toString().equals(s))
                editPassword.setText(s);
        });
        editPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loginViewModel.setPassword(s.toString());
            }
        });

        //---監聽 keepAccount<Boolean>---//
        loginViewModel.getKeepAccount().observe(this, s -> {
            if(switchKeepAccount.isChecked() != s) switchKeepAccount.setChecked(s);
        });
        switchKeepAccount.setOnClickListener(view -> {
            loginViewModel.setKeepAccount(((Switch)view).isChecked());
            if(!((Switch)view).isChecked()) loginViewModel.setKeepLogin(false);
        });

        //---監聽 keepLogin<Boolean>---//
        loginViewModel.getKeepLogin().observe(this, s -> {
            if(switchKeepLogin.isChecked() != s) switchKeepLogin.setChecked(s);
        });
        switchKeepLogin.setOnClickListener(view -> {
            loginViewModel.setKeepLogin(((Switch)view).isChecked());
            if(((Switch)view).isChecked()) loginViewModel.setKeepAccount(true);
        });

        //---監聽 登入按鈕---//
        Button submit = root.findViewById(R.id.button_login);
        submit.setOnClickListener(arg0 -> loginFunc(root));


        //--------------------設定標題欄項目可見度--------------------//
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        getActivity().findViewById(R.id.spinner_semester).setVisibility(View.GONE);
        unloadingLogin(root);

        //--------------------若啟用自動登入則呼叫--------------------//
        if(loginViewModel.getKeepLogin().getValue())
            loginFunc(root);

        return root;
    }

    //--------------------叫出、關閉Loading--------------------//
    protected void loadingLogin(View root){
        root.findViewById(R.id.login_loading).setVisibility(View.VISIBLE);
        root.findViewById(R.id.login_page).setVisibility(View.GONE);
    }
    protected void unloadingLogin(View root){
        root.findViewById(R.id.login_loading).setVisibility(View.GONE);
        root.findViewById(R.id.login_page).setVisibility(View.VISIBLE);
    }

    //--------------------登入函式--------------------//
    private void loginFunc(final View root){
        loadingLogin(root);

        new Thread(){
            public void run(){
                //----------爬蟲取得登入狀態----------//
                Map result = MainActivity.connect.login(loginViewModel.getAccount().getValue(),
                                                        loginViewModel.getPassword().getValue());
                Map status = (Map)result.get("status");
                Map profile = (Map)result.get("data");
                loginViewModel.authenticate(status, profile);
                getActivity().runOnUiThread(() -> unloadingLogin(root));
            }
        }.start();
    }

    //--------------------dp轉px、節次轉數字--------------------//
    protected int dp2px(int dp){
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}