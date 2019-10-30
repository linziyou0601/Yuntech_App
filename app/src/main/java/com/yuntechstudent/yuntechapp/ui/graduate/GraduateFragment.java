package com.yuntechstudent.yuntechapp.ui.graduate;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.yuntechstudent.yuntechapp.MainActivity;
import com.yuntechstudent.yuntechapp.R;
import com.yuntechstudent.yuntechapp.ui.login.LoginViewModel;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GraduateFragment extends Fragment {

    private GraduateViewModel graduateViewModel;
    private LoginViewModel loginViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------取得本視圖、側邊欄視圖、登入資料--------------------//
        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        graduateViewModel = ViewModelProviders.of(this).get(GraduateViewModel.class);
        View root = inflater.inflate(R.layout.fragment_graduate, container, false);
        final NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);

        //--------------------監聽View Object--------------------//
        final NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);

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


        //--------------------設定標題欄項目可見度、課表讀取視圖--------------------//
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        getActivity().findViewById(R.id.spinner_semester).setVisibility(View.GONE);


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
}