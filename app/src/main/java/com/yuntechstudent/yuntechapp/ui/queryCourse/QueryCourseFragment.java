package com.yuntechstudent.yuntechapp.ui.queryCourse;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationView;
import com.yuntechstudent.yuntechapp.MainActivity;
import com.yuntechstudent.yuntechapp.R;
import com.yuntechstudent.yuntechapp.ui.login.LoginViewModel;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.yuntechstudent.yuntechapp.ui.login.LoginViewModel.AuthenticationState.AUTHENTICATED;

public class QueryCourseFragment extends Fragment {

    private QueryCourseViewModel queryCourseViewModel;
    private LoginViewModel loginViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------取得本視圖、側邊欄視圖、登入資料--------------------//
        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        queryCourseViewModel = ViewModelProviders.of(this).get(QueryCourseViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_query_course, container, false);
        final NavigationView navigationView = getActivity().findViewById(R.id.nav_view);

        //--------------------監聽View Object--------------------//
        final NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        final Spinner spinnerAcadSeme = root.findViewById(R.id.queryCourse_AcadSeme);
        final Spinner spinnerCollege = root.findViewById(R.id.queryCourse_College);
        final Spinner spinnerDeptCode = root.findViewById(R.id.queryCourse_DeptCode);
        final Map<String, Document> conObject = new HashMap<>();

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
        spinnerCollege.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Spinner spinnerCollege = root.findViewById(R.id.queryCourse_College);
                Spinner spinnerDeptCode = root.findViewById(R.id.queryCourse_DeptCode);
                //更改學系按鈕
                final ArrayAdapter<String> deptList = new ArrayAdapter(getActivity(), R.layout.spinner_form_item, queryCourseViewModel.getDeptsList(spinnerCollege.getSelectedItem().toString()));
                deptList.setDropDownViewResource(R.layout.spinner_dropdown_item);
                getActivity().runOnUiThread(() -> {
                    spinnerDeptCode.setAdapter(deptList);
                });
                //選擇學院後重新取得document
                Map result = selectCollege(root, conObject.get("dom"));
                conObject.put("dom", (Document)result.get("dom"));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        //---監聽 查詢按鈕---//
        Button submit = root.findViewById(R.id.queryCourse_submit);
        submit.setOnClickListener(arg0 -> queryFunc(root, conObject.get("dom")));

        //--------------------設定標題欄項目可見度、課程讀取視圖--------------------//
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        getActivity().findViewById(R.id.spinner_semester).setVisibility(View.GONE);
        loadingQueryCourse(root);

        //--------------------取得學期項目--------------------//
        //取得學年及連線資料
        Thread a = new Thread(){
            public void run(){
                Map result = MainActivity.connect.getSemester("https://webapp.yuntech.edu.tw/WebNewCAS/Course/QueryCour.aspx");
                Map status = (Map)result.get("status");
                Map semesters = (Map)result.get("data");

                while(status.get("status").toString().equals("fail")){
                    try { Thread.sleep(3000); }
                    catch (Exception e) { System.out.println(e); }
                    result = MainActivity.connect.getSemester("https://webapp.yuntech.edu.tw/WebNewCAS/Course/QueryCour.aspx");
                    status = (Map)result.get("status");
                    semesters = (Map)result.get("data");
                }

                Map temp = (Map)result.get("dom");
                conObject.put("dom", (Document)temp.get("document"));

                //設定學期Adapter
                queryCourseViewModel.setSemestersList(semesters);
                final ArrayAdapter<String> semeList = new ArrayAdapter<>(getActivity(), R.layout.spinner_form_item, queryCourseViewModel.getSemestersList());
                semeList.setDropDownViewResource(R.layout.spinner_dropdown_item);
                getActivity().runOnUiThread(() -> {
                    //初始化學期選單
                    spinnerAcadSeme.setAdapter(semeList);
                    //初始化學院選單
                    final ArrayAdapter<String> collegeList = new ArrayAdapter(getActivity(), R.layout.spinner_form_item, queryCourseViewModel.getCollegesList());
                    collegeList.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinnerCollege.setAdapter(collegeList);
                    //初始化學系選單
                    final ArrayAdapter<String> deptList = new ArrayAdapter(getActivity(), R.layout.spinner_form_item, queryCourseViewModel.getDeptsList(spinnerCollege.getSelectedItem().toString()));
                    deptList.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinnerDeptCode.setAdapter(deptList);
                    loadedQueryCourse(root);
                });
            }
        };
        a.start();

        return root;
    }

    //--------------------叫出、關閉Loading--------------------//
    protected void loadingQueryCourse(View root){
        root.findViewById(R.id.queryCourse_loading).setVisibility(View.VISIBLE);
        root.findViewById(R.id.queryCourse_finishing).setVisibility(View.GONE);
    }
    protected void loadedQueryCourse(View root){
        root.findViewById(R.id.queryCourse_loading).setVisibility(View.GONE);
        root.findViewById(R.id.queryCourse_finishing).setVisibility(View.VISIBLE);
    }

    //--------------------選擇學院後重新取得document函式--------------------//
    private Map selectCollege(final View root, final Document document){
        final Map<String, Document> conObjectSub = new HashMap<>();
        //填入資料
        Map<String, String> datas = new HashMap<>();
        //隱藏欄位
        datas.put("ctl00_ToolkitScriptManager1_HiddenField", ";;AjaxControlToolkit, Version=4.1.60919.0, Culture=neutral, PublicKeyToken=28f01b0e84b6d53e:zh-TW:ab75ae50-1505-49da-acca-8b96b908cb1a:de1feab2:f9cec9bc:35576c48:f2c8e708:720a52bf:589eaa30:a67c2700:ab09e3fe:87104b7c:8613aea7:3202a5a2:be6fb298;");
        datas.put("ctl00$ToolkitScriptManager1", "ctl00$ContentPlaceHolder1$UpdatePanel1|ctl00$ContentPlaceHolder1$College");
        datas.put("__LASTFOCUS", document.select("#__LASTFOCUS").first().attr("value"));
        datas.put("__EVENTTARGET", document.select("#__EVENTTARGET").first().attr("value"));
        datas.put("__EVENTARGUMENT", document.select("#__EVENTARGUMENT").first().attr("value"));
        datas.put("__LASTFOCUS", document.select("#__LASTFOCUS").first().attr("value"));
        datas.put("__VIEWSTATE", document.select("#__VIEWSTATE").first().attr("value"));
        datas.put("__VIEWSTATEGENERATOR", document.select("#__VIEWSTATEGENERATOR").first().attr("value"));
        datas.put("__EVENTVALIDATION", document.select("#__EVENTVALIDATION").first().attr("value"));
        //學期、學院、學系
        datas.put("ctl00$ContentPlaceHolder1$AcadSeme", ((Spinner) root.findViewById(R.id.queryCourse_AcadSeme)).getSelectedItem().toString().replaceAll("(學年第)|(學期)", ""));
        datas.put("ctl00$ContentPlaceHolder1$College", queryCourseViewModel.getCollegesCode(((Spinner) root.findViewById(R.id.queryCourse_College)).getSelectedItem().toString()));

        //----------爬蟲取得資料----------//
        Thread a = new Thread(){
            public void run(){
                Map result = MainActivity.connect.getQueryCourseCollege(datas);
                Map temp = (Map)result.get("dom");
                conObjectSub.put("dom", (Document)temp.get("document"));
            }
        };
        a.start();
        try{ a.join(); }
        catch(InterruptedException e) { e.printStackTrace(); }
        return conObjectSub;
    }

    //--------------------送出函式--------------------//
    private void queryFunc(final View root, final Document document){
        loadingQueryCourse(root);
        //隱藏選單
        LinearLayout bottomSheetLayout = root.findViewById(R.id.query_course_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        //填入資料
        Map<String, String> datas = new HashMap<>();
        //隱藏欄位
        datas.put("ctl00_ToolkitScriptManager1_HiddenField", ";;AjaxControlToolkit, Version=4.1.60919.0, Culture=neutral, PublicKeyToken=28f01b0e84b6d53e:zh-TW:ab75ae50-1505-49da-acca-8b96b908cb1a:de1feab2:f9cec9bc:35576c48:f2c8e708:720a52bf:589eaa30:a67c2700:ab09e3fe:87104b7c:8613aea7:3202a5a2:be6fb298;");
        datas.put("__LASTFOCUS", document.select("#__LASTFOCUS").first().attr("value"));
        datas.put("__EVENTTARGET", document.select("#__EVENTTARGET").first().attr("value"));
        datas.put("__EVENTARGUMENT", document.select("#__EVENTARGUMENT").first().attr("value"));
        datas.put("__LASTFOCUS", document.select("#__LASTFOCUS").first().attr("value"));
        datas.put("__VIEWSTATE", document.select("#__VIEWSTATE").first().attr("value"));
        datas.put("__VIEWSTATEGENERATOR", document.select("#__VIEWSTATEGENERATOR").first().attr("value"));
        datas.put("__EVENTVALIDATION", document.select("#__EVENTVALIDATION").first().attr("value"));
        //學期、學院、學系
        datas.put("ctl00$ContentPlaceHolder1$AcadSeme", ((Spinner) root.findViewById(R.id.queryCourse_AcadSeme)).getSelectedItem().toString().replaceAll("(學年第)|(學期)", ""));
        datas.put("ctl00$ContentPlaceHolder1$College", queryCourseViewModel.getCollegesCode(((Spinner) root.findViewById(R.id.queryCourse_College)).getSelectedItem().toString()));
        datas.put("ctl00$ContentPlaceHolder1$DeptCode", queryCourseViewModel.getDeptsCode(datas.get("ctl00$ContentPlaceHolder1$College"), ((Spinner) root.findViewById(R.id.queryCourse_DeptCode)).getSelectedItem().toString()));
        //日夜間
        if(((Chip)root.findViewById(R.id.queryCourse_DayNight_0)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$DayNight$0", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_DayNight_1)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$DayNight$1", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_DayNight_2)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$DayNight$2", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_DayNight_3)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$DayNight$3", "on");
        //學制
        if(((Chip)root.findViewById(R.id.queryCourse_EduSys_0)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$EduSys$0", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_EduSys_1)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$EduSys$1", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_EduSys_2)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$EduSys$2", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_EduSys_3)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$EduSys$3", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_EduSys_4)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$EduSys$4", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_EduSys_5)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$EduSys$5", "on");
        //類別
        if(((Chip)root.findViewById(R.id.queryCourse_MajOp_0)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$MajOp$0", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_MajOp_1)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$MajOp$1", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_MajOp_2)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$MajOp$2", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_MajOp_3)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$MajOp$3", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_MajOp_4)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$MajOp$4", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_MajOp_5)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$MajOp$5", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_MajOp_6)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$MajOp$6", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_MajOp_7)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$MajOp$7", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_MajOp_8)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$MajOp$8", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_MajOp_9)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$MajOp$9", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_MajOp_10)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$MajOp$10", "on");
        //課號、課名、教師
        datas.put("ctl00$ContentPlaceHolder1$CurrentSubj",((EditText)root.findViewById(R.id.queryCourse_CurrentSubj)).getEditableText().toString());
        datas.put("ctl00$ContentPlaceHolder1$SubjName",((EditText)root.findViewById(R.id.queryCourse_SubjName)).getEditableText().toString());
        datas.put("ctl00$ContentPlaceHolder1$Instructor",((EditText)root.findViewById(R.id.queryCourse_Instructor)).getEditableText().toString());
        //星期
        if(((Chip)root.findViewById(R.id.queryCourse_Weeks_0)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Weeks$0", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Weeks_1)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Weeks$1", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Weeks_2)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Weeks$2", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Weeks_3)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Weeks$3", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Weeks_4)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Weeks$4", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Weeks_5)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Weeks$5", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Weeks_6)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Weeks$6", "on");
        //節次
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_0)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$0", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_1)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$1", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_2)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$2", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_3)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$3", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_4)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$4", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_5)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$5", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_6)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$6", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_7)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$7", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_8)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$8", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_9)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$9", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_10)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$10", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_11)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$11", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_12)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$12", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_13)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$13", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_14)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$14", "on");
        if(((Chip)root.findViewById(R.id.queryCourse_Sections_15)).isChecked()) datas.put("ctl00$ContentPlaceHolder1$Sections$15", "on");
        datas.put("ctl00$ContentPlaceHolder1$TextBoxWatermarkExtender1_ClientState", "");
        datas.put("ctl00$ContentPlaceHolder1$TextBoxWatermarkExtender2_ClientState", "");
        datas.put("ctl00$ContentPlaceHolder1$TextBoxWatermarkExtender3_ClientState", "");
        //查詢鈕
        datas.put("ctl00$ContentPlaceHolder1$Submit","執行查詢");

        //----------爬蟲取得資料----------//
        Thread a = new Thread(){
            public void run(){
            ArrayList<Map<String, String>> resultRow = MainActivity.connect.queryCourseData(datas, 1);
            boolean next = (resultRow.get(resultRow.size()-1)).get("next").equals("true");
            resultRow.remove(resultRow.size()-1);
            getActivity().runOnUiThread(() -> {
                RecyclerView recyclerView = root.findViewById(R.id.queryCourseRecyView);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(new courseItemAdapter(getContext(), resultRow, next));
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        //當滾動事件停止時觸發一次
                        if(newState == RecyclerView.SCROLL_STATE_IDLE){
                            int lastVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition(); //取得最後一筆可見項目的位置
                            courseItemAdapter adapter = (courseItemAdapter)recyclerView.getAdapter();
                            //若位於最後一個項目
                            if(lastVisibleItem == adapter.getItemCount()-1 && adapter.getHasMore() && adapter.getHasNext())
                                adapter.updateRecyclerView();
                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                    }
                });
                loadedQueryCourse(root);
            });
            }
        };
        a.start();
    }

    //--------------------建構每個消息列--------------------//
    private class courseItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private ArrayList<Map<String, String>> itemRow;
        private boolean hasNext;    //是否有下一頁資料
        private boolean hasMore;    //是否有資料增加

        private int pageIndex = 1;
        private int NORMAL_ITEM = 0;
        private int FOOT_ITEM = 1;

        courseItemAdapter(Context context, ArrayList<Map<String, String>> itemRow, boolean hasNext){
            this.context = context;
            this.itemRow = itemRow;
            this.hasNext = hasNext;
            this.hasMore = itemRow.size()>0;
            pageIndex = 1;
        }

        //調用R.layout建立View，以此建立一個新的ViewHolder
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layout = viewType == NORMAL_ITEM? R.layout.recyclerview_qcoursecard_item: R.layout.recyclerview_newscard_foot;
            View view = LayoutInflater.from(context).inflate(layout, parent, false);
            return (viewType == NORMAL_ITEM)? (new NormalViewHolder(view)): (new FootViewHolder(view));
        }

        //透過position找到data，以此設置ViewHolder裡的View
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof NormalViewHolder) {
                final Map<String, String> member = itemRow.get(position);
                ((NormalViewHolder) holder).qcourseCard.setOnClickListener(new CoursesOnClick(member.get("Link")));
                ((NormalViewHolder) holder).qCourse_Serial_No.setText(member.get("Serial_No"));
                ((NormalViewHolder) holder).qCourse_Curriculum_No.setText(member.get("Curriculum_No"));
                ((NormalViewHolder) holder).qCourse_Course_Name_CHT.setText(member.get("Course_Name_CHT"));
                ((NormalViewHolder) holder).qCourse_Course_Name_ENG.setText(member.get("Course_Name_ENG"));
                ((NormalViewHolder) holder).qCourse_Class.setText(member.get("Class"));
                ((NormalViewHolder) holder).qCourse_Team.setText(member.get("Team"));
                ((NormalViewHolder) holder).qCourse_Required_Elective.setText(member.get("Required_Elective"));
                ((NormalViewHolder) holder).qCourse_Credits.setText(member.get("Credits"));
                ((NormalViewHolder) holder).qCourse_Schedule_Location.setText(member.get("Schedule_Location"));
                ((NormalViewHolder) holder).qCourse_Instructor.setText(member.get("Instructor"));
                ((NormalViewHolder) holder).qCourse_Sel.setText(member.get("Sel"));
                ((NormalViewHolder) holder).qCourse_Max.setText(member.get("Max"));
                ((NormalViewHolder) holder).qCourse_Remarks.setText(member.get("Remarks"));
            } else {
                ((FootViewHolder) holder).tips.setText(hasNext? "正在載入更多...": "沒有更多資料");
            }
        }

        //取得數量
        @Override
        public int getItemCount() {
            return itemRow.size() + 1;
        }

        //取得類型
        @Override
        public int getItemViewType(int position) {
            return (position == getItemCount()-1)? FOOT_ITEM: NORMAL_ITEM;
        }

        //一般資料ViewHolder
        class NormalViewHolder extends RecyclerView.ViewHolder{
            MaterialCardView qcourseCard;
            TextView qCourse_Serial_No, qCourse_Curriculum_No, qCourse_Course_Name_CHT, qCourse_Course_Name_ENG, qCourse_Class, qCourse_Team;
            TextView qCourse_Required_Elective, qCourse_Credits, qCourse_Schedule_Location, qCourse_Instructor, qCourse_Sel, qCourse_Max, qCourse_Remarks;
            NormalViewHolder(View itemView) {
                super(itemView);
                qcourseCard = itemView.findViewById(R.id.qcourseCard);
                qCourse_Serial_No = itemView.findViewById(R.id.qCourse_Serial_No);
                qCourse_Curriculum_No = itemView.findViewById(R.id.qCourse_Curriculum_No);
                qCourse_Course_Name_CHT = itemView.findViewById(R.id.qCourse_Course_Name_CHT);
                qCourse_Course_Name_ENG = itemView.findViewById(R.id.qCourse_Course_Name_ENG);
                qCourse_Class = itemView.findViewById(R.id.qCourse_Class);
                qCourse_Team = itemView.findViewById(R.id.qCourse_Team);
                qCourse_Required_Elective = itemView.findViewById(R.id.qCourse_Required_Elective);
                qCourse_Credits = itemView.findViewById(R.id.qCourse_Credits);
                qCourse_Schedule_Location = itemView.findViewById(R.id.qCourse_Schedule_Location);
                qCourse_Instructor = itemView.findViewById(R.id.qCourse_Instructor);
                qCourse_Sel = itemView.findViewById(R.id.qCourse_Sel);
                qCourse_Max = itemView.findViewById(R.id.qCourse_Max);
                qCourse_Remarks = itemView.findViewById(R.id.qCourse_Remarks);
            }
        }
        //頁尾資料ViewHolder
        class FootViewHolder extends RecyclerView.ViewHolder {
            TextView tips;
            public FootViewHolder(View itemView) {
                super(itemView);
                tips = itemView.findViewById(R.id.tips);
            }
        }

        //----------分頁資料功能函式----------//
        //更新資料內容，並設定hasMore值
        public void updateList(ArrayList<Map<String,String>> newItems, boolean hasNext) {
            if(newItems!=null) itemRow.addAll(newItems);
            this.hasMore = (newItems!=null);
            this.hasNext = hasNext;
            notifyDataSetChanged();
        }
        //回值hasMore
        public boolean getHasMore(){
            return hasMore;
        }
        public boolean getHasNext(){
            return hasNext;
        }

        //----------分頁資料----------//
        //更新子頁面RecyclerView
        public void updateRecyclerView() {
            hasMore = false;
            new Thread() {
                public void run() {
                    ArrayList<Map<String, String>> resultRow = MainActivity.connect.queryCourseData(null, ++pageIndex);
                    boolean next = (resultRow.get(resultRow.size()-1)).get("next").equals("true");
                    resultRow.remove(resultRow.size()-1);
                    getActivity().runOnUiThread(() -> updateList((resultRow.size()>0)? resultRow: null, next));
                }
            }.start();
        }
    }

    //--------------------監聽 課程點擊--------------------//
    public class CoursesOnClick implements View.OnClickListener {
        String url;
        CoursesOnClick(String u) { url = u; }

        @Override
        public void onClick(View v){
            if(!url.equals("")){
                Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        }
    }
}