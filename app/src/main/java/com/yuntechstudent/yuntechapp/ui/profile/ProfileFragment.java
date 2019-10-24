package com.yuntechstudent.yuntechapp.ui.profile;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.yuntechstudent.yuntechapp.MainActivity;
import com.yuntechstudent.yuntechapp.R;
import com.yuntechstudent.yuntechapp.ui.login.LoginViewModel;
import com.google.android.material.navigation.NavigationView;

import java.util.Map;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private LoginViewModel loginViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------取得本視圖、側邊欄視圖、登入資料--------------------//
        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);

        //--------------------監聽View Object--------------------//
        final NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        final TextView profile_stuNum = root.findViewById(R.id.profile_stuNum);
        final TextView profile_name = root.findViewById(R.id.profile_name);
        final TextView profile_major = root.findViewById(R.id.profile_major);
        final ImageView profile_image = root.findViewById(R.id.profile_image);

        //---監聽 authenticationState<LoginViewModel.AuthenticationState>---//
        loginViewModel.authenticationState.observe(getViewLifecycleOwner(), new Observer<LoginViewModel.AuthenticationState>() {
            @Override
            public void onChanged(LoginViewModel.AuthenticationState authenticationState) {
                switch (authenticationState) {
                    case AUTHENTICATED:
                        break;
                    default:
                        //更改側邊欄選單
                        navigationView.getMenu().clear();
                        navigationView.inflateMenu(R.menu.activity_login_drawer);
                        //頁面重新導向
                        navController.navigate(R.id.nav_login);
                }
            }
        });

        //---監聽 account<String>---//
        loginViewModel.getAccount().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if(!profile_stuNum.getText().toString().equals(s))
                    profile_stuNum.setText(s);
            }
        });

        //---監聽 profileName<String>---//
        loginViewModel.getProfileName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if(!profile_name.getText().toString().equals(s)) profile_name.setText(s);
            }
        });

        //---監聽 profileMajor<String>---//
        loginViewModel.getProfileMajor().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if(!profile_major.getText().toString().equals(s)) profile_major.setText(s);
            }
        });

        //---監聽 profileImage<String>---//
        loginViewModel.getProfileImage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if(!s.equals("")){
                    byte[] decodedString = Base64.decode(s, Base64.NO_WRAP);
                    profile_image.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                    profile_image.setPadding(0, dp2px(25),0,0);
                }else{
                    profile_image.setImageResource(R.drawable.yuntechbobo);
                    profile_image.setPadding(0, 0,0,0);
                }
            }
        });


        //--------------------設定標題欄項目可見度、課表讀取視圖--------------------//
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        getActivity().findViewById(R.id.spinner_semester).setVisibility(View.GONE);


        //--------------------取得個人資料--------------------//
        final TextView profile_class = root.findViewById(R.id.var_profile_class);
        final TextView profile_double_major = root.findViewById(R.id.var_profile_double_major);
        final TextView profile_mainteacher = root.findViewById(R.id.var_profile_mainteacher);
        final TextView profile_graduation = root.findViewById(R.id.var_profile_graduation);
        final TextView profile_entrance = root.findViewById(R.id.var_profile_entrance);
        final TextView profile_academic_status = root.findViewById(R.id.var_profile_academic_status);

        //取得學年及連線資料
        Thread a = new Thread(){
            public void run(){
                if(MainActivity.connect.cookies == null)
                    MainActivity.connect.login(loginViewModel.getAccount().getValue(),
                                               loginViewModel.getPassword().getValue());
                Map result = MainActivity.connect.studentProfile();
                Map status = (Map)result.get("status");
                Map data = (Map)result.get("data");

                while(status.get("status").toString().equals("fail")){
                    try { Thread.sleep(3000); }
                    catch (Exception e) { System.out.println(e); }
                    result = MainActivity.connect.studentProfile();
                    status = (Map)result.get("status");
                    data = (Map)result.get("data");
                }

                final Map profiles = data;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        profile_class.setText(profiles.get("profile_class").toString());
                        profile_double_major.setText(profiles.get("profile_double_major").toString());
                        profile_mainteacher.setText(profiles.get("profile_mainteacher").toString());
                        profile_graduation.setText(profiles.get("profile_graduation").toString());
                        profile_entrance.setText(profiles.get("profile_entrance").toString());
                        profile_academic_status.setText(profiles.get("profile_academic_status").toString());
                    }
                });
            }
        };
        a.start();

        return root;
    }

    //--------------------dp轉px、節次轉數字--------------------//
    protected int dp2px(int dp){
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}