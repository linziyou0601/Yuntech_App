package com.yuntechstudent.yuntechapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.yuntechstudent.yuntechapp.ui.login.LoginViewModel;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    public static Connect connect = new Connect();

    public static KeyStoreHelper keyStoreHelper;
    public static SharedPreferencesHelper preferencesHelper;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //--------------------設定儲存器、加密器、登入資料庫--------------------//
        preferencesHelper = new SharedPreferencesHelper(getApplicationContext());
        keyStoreHelper = new KeyStoreHelper(getApplicationContext(), preferencesHelper);
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        //--------------------取得toolbar、drawer、navigationView--------------------//
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        //--------------------預設標題欄綁定、側邊欄UI--------------------//
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_login, R.id.nav_course, R.id.nav_profile, R.id.nav_score, R.id.nav_graduate,
                R.id.nav_map, R.id.nav_news, R.id.nav_bus, R.id.nav_queryCourse, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //--------------------監聽是否按按鈕--------------------//
        navigationView.setNavigationItemSelectedListener(this);

        //--------------------設定標題欄項目可見度、課表讀取視圖--------------------//
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        findViewById(R.id.spinner_semester).setVisibility(View.GONE);
    }

    public void showAbout(MenuItem item){
        new MaterialAlertDialogBuilder(this, R.style.MyThemeOverlayAlertDialog)
                .setTitle("關於YunTech App")
                .setMessage("\n　　本App為非官方軟體，目的為方便使用手機查詢資料，內容僅供查詢參考，所有資訊皆以「雲科大單一入口網」為準。\n\n　　本App運作時會將帳號密碼加密處理，且不會主動紀錄使用者之帳號密碼，若有疑慮請勿使用。\n\n\n©Linziyou0601\nYunTech App (ver. 1.7)")
                .setNegativeButton("關閉", null)
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int fragment = -99;
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_profile:
                fragment = R.id.nav_profile;
                break;
            case R.id.nav_course:
                fragment = R.id.nav_course;
                break;
            case R.id.nav_score:
                fragment = R.id.nav_score;
                break;
            case R.id.nav_graduate:
                fragment = R.id.nav_graduate;
                break;
            case R.id.nav_map:
                fragment = R.id.nav_map;
                break;
            case R.id.nav_news:
                fragment = R.id.nav_news;
                break;
            case R.id.nav_bus:
                fragment = R.id.nav_bus;
                break;
            case R.id.nav_queryCourse:
                fragment = R.id.nav_queryCourse;
                break;
            case R.id.nav_logout:
                new MaterialAlertDialogBuilder(this, R.style.MyThemeOverlayAlertDialog)
                        .setTitle("登出")
                        .setMessage("確定要登出？")
                        .setPositiveButton("登出", (dialog, which) -> loginViewModel.refuseAuthentication(false))
                        .setNegativeButton("取消", null)
                        .show();
                break;
        }
        if(fragment!=-99){
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(fragment);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish();
            System.exit(0);
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "再按一次返回關閉程式", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }
}