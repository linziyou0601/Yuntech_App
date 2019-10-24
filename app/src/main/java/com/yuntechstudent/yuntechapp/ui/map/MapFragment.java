package com.yuntechstudent.yuntechapp.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.yuntechstudent.yuntechapp.MainActivity;
import com.yuntechstudent.yuntechapp.R;
import com.yuntechstudent.yuntechapp.ui.login.LoginViewModel;
import com.google.android.material.navigation.NavigationView;

public class MapFragment extends Fragment {

    private MapViewModel mapViewModel;
    private LoginViewModel loginViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------取得本視圖、側邊欄視圖、登入資料--------------------//
        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        mapViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        View root = inflater.inflate(R.layout.fragment_map, container, false);
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

        return root;
    }
}