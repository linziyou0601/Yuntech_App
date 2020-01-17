package com.yuntechstudent.yuntechapp.ui.course;

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

import static com.yuntechstudent.yuntechapp.ui.login.LoginViewModel.AuthenticationState.AUTHENTICATED;


public class CourseFragment extends Fragment {

    private CourseViewModel courseViewModel;
    private LoginViewModel loginViewModel;
    public Map<String, Map> courseDetail = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------取得本視圖、側邊欄視圖、登入資料--------------------//
        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        courseViewModel = ViewModelProviders.of(this).get(CourseViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_course, container, false);
        final NavigationView navigationView = getActivity().findViewById(R.id.nav_view);


        //--------------------監聽View Object--------------------//
        final Spinner spinner = getActivity().findViewById(R.id.spinner_semester);
        final Map<String, Document> conObject = new HashMap<>();
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

        //---監聽 學期選擇按鈕---//
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Spinner spinner = getActivity().findViewById(R.id.spinner_semester);
                String seme = spinner.getSelectedItem().toString().replaceAll("(學年第)|(學期)", "");
                createCourse(seme, root, conObject);
                loadedCourse(root);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });


        //--------------------設定標題欄項目可見度、課表讀取視圖、清空spinner--------------------//
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        getActivity().findViewById(R.id.spinner_semester).setVisibility(View.VISIBLE);
        loadingCourse(root);
        spinner.setAdapter(new ArrayAdapter(getActivity(), R.layout.spinner_item, courseViewModel.getSemestersList()));


        //--------------------取得學期項目--------------------//
        //取得學年及連線資料
        Thread a = new Thread(){
            public void run(){
                if(MainActivity.connect.cookies == null)
                    MainActivity.connect.login(loginViewModel.getAccount().getValue(),
                                               loginViewModel.getPassword().getValue());
                Map result = MainActivity.connect.getSemester("https://webapp.yuntech.edu.tw/WebNewCAS/StudentFile/Course/");
                Map status = (Map)result.get("status");
                Map semesters = (Map)result.get("data");

                while(status.get("status").toString().equals("fail")){
                    try { Thread.sleep(3000); }
                    catch (Exception e) { System.out.println(e); }
                    result = MainActivity.connect.getSemester("https://webapp.yuntech.edu.tw/WebNewCAS/StudentFile/Course/");
                    status = (Map)result.get("status");
                    semesters = (Map)result.get("data");
                }

                Map temp = (Map)result.get("dom");
                conObject.put("dom", (Document)temp.get("document"));
                courseViewModel.setSemestersList(semesters);

                final ArrayAdapter<String> semeList = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, courseViewModel.getSemestersList());
                semeList.setDropDownViewResource(R.layout.spinner_dropdown_item);
                getActivity().runOnUiThread(() -> {
                    spinner.setAdapter(semeList);
                    String seme = courseViewModel.getSemestersList().get(0).toString().replaceAll("(學年第)|(學期)", "");
                    createCourse(seme, root, conObject);
                    loadedCourse(root);
                });
            }
        };
        a.start();

        return root;
    }

    //--------------------叫出、關閉Loading--------------------//
    protected void loadingCourse(View root){
        root.findViewById(R.id.course_loading).setVisibility(View.VISIBLE);
        root.findViewById(R.id.course_finishing).setVisibility(View.GONE);
    }
    protected void loadedCourse(View root){
        root.findViewById(R.id.course_loading).setVisibility(View.GONE);
        root.findViewById(R.id.course_finishing).setVisibility(View.VISIBLE);
    }

    //--------------------取得及儲存課程內容--------------------//
    protected void createCourse(String seme, View root, Map conObject){
        //----------取得課表資料----------//
        //取得課表資料
        final ArrayList<Element> courseRow = new ArrayList<>();
        class MyRunnable implements Runnable {
            String seme;
            Document dom;
            public MyRunnable(String s, Document d) {
                seme = s; dom = d;
            }
            public void run() {
                Elements el = MainActivity.connect.courseData(seme, dom);
                for(Element k: el)
                    courseRow.add(k);
            }
        }
        Thread b = new Thread(new MyRunnable(seme, (Document)conObject.get("dom")));
        b.start();

        //建立課表內容
        try{ b.join(); }
        catch(InterruptedException e) { e.printStackTrace(); }

        //初始化儲存格
        String[] colorArray = {"#FFBD69F6", "#FF1976D2", "#FF009688", "#FFF95943", "#FFFFA726",
                "#FFF06292", "#FF795548", "#FF004986", "#FF0FCBB0", "#FF6D6650"};
        LinearLayout[] columnDay = { root.findViewById(R.id.column_mon),
                root.findViewById(R.id.column_tue),
                root.findViewById(R.id.column_wed),
                root.findViewById(R.id.column_thu),
                root.findViewById(R.id.column_fri) };
        boolean[][] time = new boolean[5][16];
        String[][] name = new String[5][16];
        String[][] s_num = new String[5][16];
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 16; j++){
                time[i][j] = false;
                name[i][j] = "";
                s_num[i][j] = "";
            }
        }

        //課程資料存入儲存格
        courseDetail.clear();
        for(Element row: courseRow){
            //取得td陣列
            Elements td = row.select("td");
            //取得課程時間
            String schedule = td.get(7).text();
            if(!schedule.equals("")) {
                //將節次(星期-節次,星期-節次/教室)分割成(星期-節次,星期-節次)，再分割成 [星期-節次, 星期-節次] 陣列
                String[] days = (schedule.split("/")[0]).split(",");
                //循序存取 [星期-節次, 星期-節次]
                for (String day: days){
                    //將 (星期-節次) 分割成 [星期, 節次]
                    String[] day_times = day.split("-");
                    //放入時間表
                    for (char k : day_times[1].toCharArray()) {
                        time[Integer.parseInt(day_times[0]) - 1][dayHour(k)] = true;
                        name[Integer.parseInt(day_times[0]) - 1][dayHour(k)] = td.get(2).select("a").text();
                        s_num[Integer.parseInt(day_times[0]) - 1][dayHour(k)] = td.get(0).text();
                    }
                }

            }
            //存課程詳細資料
            Map<String, String> detail = new HashMap<>();
            detail.put("Serial_No", td.get(0).text());
            detail.put("Curriculum_No", td.get(1).text());
            detail.put("Course_Name_Zh", td.get(2).select("a").text());
            detail.put("Course_Name_En", td.get(2).select("span").text());
            detail.put("Class", td.get(3).text());
            detail.put("Team", td.get(4).text());
            detail.put("Required_Elective", td.get(5).text());
            detail.put("Credits", td.get(6).text());
            detail.put("Schedule", td.get(7).text());
            detail.put("Instructor", td.get(8).text());
            detail.put("Remarks", td.get(11).text());
            courseDetail.put(td.get(0).text(), detail);
        }

        //課程資料放入課表
        int c = 0;
        for(int i = 0; i < 5; i++){
            LinearLayout column = columnDay[i];
            column.removeAllViews();
            for(int j = 0; j < 16;){
                int weight = 1;
                String color = (time[i][j])? colorArray[(c++)%10]: "#00FFFFFF";
                int k = j+1;
                while(k<16 && name[i][j].equals(name[i][k])){
                    weight++;
                    k++;
                }

                TextView courseText = new TextView(getActivity());
                LinearLayout.LayoutParams heightParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(64 * weight - 4));
                heightParams.setMargins(dp2px(2), dp2px(2), dp2px(2), dp2px(2));
                courseText.setPadding(dp2px(2), dp2px(2), dp2px(2), dp2px(2));
                courseText.setLayoutParams(heightParams);
                courseText.setTextSize(16);
                courseText.setTextColor(Color.parseColor("#FFFFFF"));
                courseText.setBackgroundResource(R.drawable.course_square);
                ((GradientDrawable)courseText.getBackground()).setColor(Color.parseColor(color));
                courseText.setGravity(Gravity.CENTER);
                courseText.setText(name[i][j]);
                if(time[i][j]){
                    courseText.setClickable(true);
                    courseText.setOnClickListener(new CourseOnClick(s_num[i][j]));
                }
                putCourseInColumn(column, courseText);
                j += weight;
            }
        }
    }

    //--------------------課程放入每日欄位--------------------//
    public void putCourseInColumn(final LinearLayout column, final TextView courseText) {
        getActivity().runOnUiThread(() -> column.addView(courseText));
    }

    //--------------------監聽 課程點擊--------------------//
    public class CourseOnClick implements View.OnClickListener {
        String s_num;
        CourseOnClick(String n) { s_num = n; }

        @Override
        public void onClick(View v){
            //載入中
            Map detail = courseDetail.get(s_num);
            View course_detail = getLayoutInflater().inflate(R.layout.course_detail, null);
            LinearLayout course_detail_view = course_detail.findViewById(R.id.course_detail_view);
            TextView var_course_zh = course_detail.findViewById(R.id.var_course_zh);
            TextView var_course_en = course_detail.findViewById(R.id.var_course_en);
            TextView var_course_teacher = course_detail.findViewById(R.id.var_course_teacher);
            TextView var_course_number = course_detail.findViewById(R.id.var_course_number);
            TextView var_course_credit = course_detail.findViewById(R.id.var_course_credit);
            TextView var_course_class = course_detail.findViewById(R.id.var_course_class);
            TextView var_course_schedule = course_detail.findViewById(R.id.var_course_schedule);
            TextView var_course_markup = course_detail.findViewById(R.id.var_course_markup);
            var_course_zh.setText(detail.get("Course_Name_Zh").toString());
            var_course_en.setText(detail.get("Course_Name_En").toString());
            var_course_teacher.setText(detail.get("Instructor").toString());
            var_course_number.setText(detail.get("Serial_No").toString() + "／" + detail.get("Curriculum_No").toString());
            var_course_credit.setText(detail.get("Credits").toString().replaceAll("/", "／"));
            var_course_class.setText(detail.get("Class").toString() + "／" + detail.get("Team").toString());
            var_course_schedule.setText(detail.get("Schedule").toString().replaceAll("-|/", "／"));
            var_course_markup.setText(detail.get("Remarks").toString());

            new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                    .setTitle(detail.get("Required_Elective").toString())
                    .setView(course_detail_view)
                    .show();
        }
    }

    //--------------------dp轉px、節次轉數字--------------------//
    protected int dp2px(int dp){
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
    protected int dayHour(char ch) {
        int x=0;
        switch(ch){
            case 'W':
                x = 0;
                break;
            case 'X':
                x = 1;
                break;
            case 'A':
                x = 2;
                break;
            case 'B':
                x = 3;
                break;
            case 'C':
                x = 4;
                break;
            case 'D':
                x = 5;
                break;
            case 'Y':
                x = 6;
                break;
            case 'E':
                x = 7;
                break;
            case 'F':
                x = 8;
                break;
            case 'G':
                x = 9;
                break;
            case 'H':
                x = 10;
                break;
            case 'Z':
                x = 11;
                break;
            case 'I':
                x = 12;
                break;
            case 'J':
                x = 13;
                break;
            case 'K':
                x = 14;
                break;
            case 'L':
                x = 15;
                break;
        }
        return x;
    }
}