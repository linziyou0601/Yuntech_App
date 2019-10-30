package com.yuntechstudent.yuntechapp.ui.graduate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.navigation.NavigationView;
import com.yuntechstudent.yuntechapp.MainActivity;
import com.yuntechstudent.yuntechapp.R;
import com.yuntechstudent.yuntechapp.ui.login.LoginViewModel;

import java.util.Map;

import static com.yuntechstudent.yuntechapp.ui.login.LoginViewModel.AuthenticationState.AUTHENTICATED;


public class GraduateFragment extends Fragment {

    private GraduateViewModel graduateViewModel;
    private LoginViewModel loginViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------取得本視圖、側邊欄視圖、登入資料--------------------//
        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        graduateViewModel = ViewModelProviders.of(this).get(GraduateViewModel.class);
        View root = inflater.inflate(R.layout.fragment_graduate, container, false);
        final NavigationView navigationView = getActivity().findViewById(R.id.nav_view);

        //--------------------監聽View Object--------------------//
        final NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);

        //---監聽 authenticationState<LoginViewModel.AuthenticationState>---//
        loginViewModel.authenticationState.observe(getViewLifecycleOwner(), authenticationState -> {
            if(!authenticationState.equals(AUTHENTICATED)){
                //更改側邊欄選單
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.activity_login_drawer);
                //頁面重新導向
                navController.navigate(R.id.nav_login);
            }
        });


        //--------------------設定標題欄項目可見度、課表讀取視圖--------------------//
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        getActivity().findViewById(R.id.spinner_semester).setVisibility(View.GONE);
        loadingGraduate(root);


        //取得應修未修畢業學分資料
        Thread a = new Thread(){
            public void run(){
                if(MainActivity.connect.cookies == null)
                    MainActivity.connect.login(loginViewModel.getAccount().getValue(),
                                               loginViewModel.getPassword().getValue());
                Map result = MainActivity.connect.studentGraduate();
                Map status = (Map)result.get("status");
                Map data = (Map)result.get("data");

                while(status.get("status").toString().equals("fail")){
                    try { Thread.sleep(3000); }
                    catch (Exception e) { System.out.println(e); }
                    result = MainActivity.connect.studentGraduate();
                    status = (Map)result.get("status");
                    data = (Map)result.get("data");
                }

                final Map<String, String> grads = data;
                getActivity().runOnUiThread(() -> {
                    ((TextView) root.findViewById(R.id.graduate_stuName)).setText(grads.get("stuName"));
                    ((TextView) root.findViewById(R.id.graduate_stuNum)).setText(grads.get("stuNum"));
                    //
                    ((TextView) root.findViewById(R.id.graduate_masterDept)).setText(grads.get("masterDept"));
                    ((TextView) root.findViewById(R.id.graduate_doubDept)).setText(grads.get("doubDept"));
                    ((TextView) root.findViewById(R.id.graduate_minorDept)).setText(grads.get("minorDept"));
                    ((TextView) root.findViewById(R.id.graduate_degree)).setText(grads.get("degree"));
                    //
                    ((TextView) root.findViewById(R.id.graduate_dateEnter)).setText(grads.get("dateEnter"));
                    ((TextView) root.findViewById(R.id.graduate_dateGrad)).setText(grads.get("dateGrad"));
                    ((TextView) root.findViewById(R.id.graduate_totalCredits)).setText(grads.get("totalCredits"));
                    ((TextView) root.findViewById(R.id.graduate_gradScore)).setText(grads.get("gradScore"));
                    ((TextView) root.findViewById(R.id.graduate_gradAvg)).setText(grads.get("gradAvg"));
                    ((TextView) root.findViewById(R.id.graduate_testScore)).setText(grads.get("testScore"));
                    ((TextView) root.findViewById(R.id.graduate_topicCHN)).setText(grads.get("topicCHN"));
                    ((TextView) root.findViewById(R.id.graduate_topicENG)).setText(grads.get("topicENG"));
                    //
                    ((TextView) root.findViewById(R.id.graduate_grdMP)).setText(grads.get("grdMP"));
                    ((TextView) root.findViewById(R.id.graduate_getMP)).setText(grads.get("getMP"));
                    ((TextView) root.findViewById(R.id.graduate_nsMP)).setText(grads.get("nsMP"));
                    ((TextView) root.findViewById(R.id.graduate_outMP)).setText(grads.get("outMP"));
                    //
                    ((TextView) root.findViewById(R.id.graduate_grdIJ)).setText(grads.get("grdIJ"));
                    ((TextView) root.findViewById(R.id.graduate_getIJ)).setText(grads.get("getIJ"));
                    ((TextView) root.findViewById(R.id.graduate_nsIJ)).setText(grads.get("nsIJ"));
                    ((TextView) root.findViewById(R.id.graduate_outIJ)).setText(grads.get("outIJ"));
                    //
                    ((TextView) root.findViewById(R.id.graduate_grdCom)).setText(grads.get("grdCom"));
                    ((TextView) root.findViewById(R.id.graduate_getCom)).setText(grads.get("getCom"));
                    ((TextView) root.findViewById(R.id.graduate_nsCom)).setText(grads.get("nsCom"));
                    ((TextView) root.findViewById(R.id.graduate_outCom)).setText(grads.get("outCom"));
                    //
                    ((TextView) root.findViewById(R.id.graduate_grdMOpt)).setText(grads.get("grdMOpt"));
                    ((TextView) root.findViewById(R.id.graduate_getMOpt1)).setText(grads.get("getMOpt1"));
                    ((TextView) root.findViewById(R.id.graduate_getMOpt2)).setText(grads.get("getMOpt2"));
                    ((TextView) root.findViewById(R.id.graduate_nsMOpt)).setText(grads.get("nsMOpt"));
                    ((TextView) root.findViewById(R.id.graduate_outMOpt)).setText(grads.get("outMOpt"));
                    //
                    ((TextView) root.findViewById(R.id.graduate_title_Grd_EOSpe)).setText("選修 (c) ／ 可外選" + grads.get("title_Grd_EOSpe") + "學分");
                    ((TextView) root.findViewById(R.id.graduate_grdOSpe)).setText(grads.get("grdOSpe"));
                    ((TextView) root.findViewById(R.id.graduate_getOSpe1)).setText(grads.get("getOSpe1"));
                    ((TextView) root.findViewById(R.id.graduate_getOSpe2)).setText(grads.get("getOSpe2"));
                    ((TextView) root.findViewById(R.id.graduate_getOSpe3)).setText(grads.get("getOSpe3"));
                    ((TextView) root.findViewById(R.id.graduate_nsOSpe1)).setText(grads.get("nsOSpe1"));
                    ((TextView) root.findViewById(R.id.graduate_nsOSpe2)).setText(grads.get("nsOSpe2"));
                    ((TextView) root.findViewById(R.id.graduate_outOSpe)).setText(grads.get("outOSpe"));
                    //
                    ((TextView) root.findViewById(R.id.graduate_grdTotal)).setText(grads.get("grdTotal"));
                    ((TextView) root.findViewById(R.id.graduate_getTotal)).setText(grads.get("getTotal"));
                    ((TextView) root.findViewById(R.id.graduate_nsTotal)).setText(grads.get("nsTotal"));
                    ((TextView) root.findViewById(R.id.graduate_outTotal)).setText(grads.get("outTotal"));
                    //
                    ((TextView) root.findViewById(R.id.graduate_grdServ)).setText(grads.get("grdServ"));
                    ((TextView) root.findViewById(R.id.graduate_getServ)).setText(grads.get("getServ"));
                    ((TextView) root.findViewById(R.id.graduate_nsServ)).setText(grads.get("nsServ"));
                    ((TextView) root.findViewById(R.id.graduate_outServ)).setText(grads.get("outServ"));
                    //
                    ((TextView) root.findViewById(R.id.graduate_grdDB)).setText(grads.get("grdDB"));
                    ((TextView) root.findViewById(R.id.graduate_getDB)).setText(grads.get("getDB"));
                    ((TextView) root.findViewById(R.id.graduate_nsDB)).setText(grads.get("nsDB"));
                    ((TextView) root.findViewById(R.id.graduate_outDB)).setText(grads.get("outDB"));
                    //
                    ((TextView) root.findViewById(R.id.graduate_grdUHT)).setText(grads.get("grdUHT"));
                    ((TextView) root.findViewById(R.id.graduate_getUHT)).setText(grads.get("getUHT"));
                    ((TextView) root.findViewById(R.id.graduate_nsUHT)).setText(grads.get("nsUHT"));
                    ((TextView) root.findViewById(R.id.graduate_outUHT)).setText(grads.get("outUHT"));
                    //
                    ((TextView) root.findViewById(R.id.graduate_passEng)).setText(grads.get("passEng"));
                    ((TextView) root.findViewById(R.id.graduate_passIntern)).setText(grads.get("passIntern"));
                    ((TextView) root.findViewById(R.id.graduate_passEthics)).setText(grads.get("passEthics"));
                    //
                    ((TextView) root.findViewById(R.id.graduate_distanceCredits)).setText(grads.get("distanceCredits"));
                    ((TextView) root.findViewById(R.id.graduate_notExistsCourse)).setText(grads.get("notExistsCourse"));
                    //
                    loadedGraduate(root);
                });
            }
        };
        a.start();

        return root;
    }

    //--------------------叫出、關閉Loading--------------------//
    protected void loadingGraduate(View root){
        root.findViewById(R.id.graduate_loading).setVisibility(View.VISIBLE);
        root.findViewById(R.id.graduate_finishing).setVisibility(View.GONE);
    }
    protected void loadedGraduate(View root){
        root.findViewById(R.id.graduate_loading).setVisibility(View.GONE);
        root.findViewById(R.id.graduate_finishing).setVisibility(View.VISIBLE);
    }
}