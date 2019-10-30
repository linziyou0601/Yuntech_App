package com.yuntechstudent.yuntechapp.ui.score;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.yuntechstudent.yuntechapp.MainActivity;
import com.yuntechstudent.yuntechapp.R;
import com.yuntechstudent.yuntechapp.ui.login.LoginViewModel;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yuntechstudent.yuntechapp.ui.login.LoginViewModel.AuthenticationState.AUTHENTICATED;

public class ScoreFragment extends Fragment {

    private ScoreViewModel scoreViewModel;
    private LoginViewModel loginViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------取得本視圖、側邊欄視圖、登入資料--------------------//
        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        scoreViewModel = ViewModelProviders.of(this).get(ScoreViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_score, container, false);
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
                createScore(seme, root, conObject);
                //loadedScore(root);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });


        //--------------------設定標題欄項目可見度、課表讀取視圖、清空spinner--------------------//
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        getActivity().findViewById(R.id.spinner_semester).setVisibility(View.VISIBLE);
        loadingScore(root);
        spinner.setAdapter(new ArrayAdapter(getActivity(), R.layout.spinner_item, scoreViewModel.getSemestersList()));


        //--------------------取得學期項目--------------------//
        //取得學年及連線資料
        Thread a = new Thread(){
            public void run(){
                if(MainActivity.connect.cookies == null)
                    MainActivity.connect.login(loginViewModel.getAccount().getValue(),
                                               loginViewModel.getPassword().getValue());
                Map result = MainActivity.connect.getSemester("https://webapp.yuntech.edu.tw/WebNewCAS/StudentFile/Score/");
                Map status = (Map)result.get("status");
                Map semesters = (Map)result.get("data");

                while(status.get("status").toString().equals("fail")){
                    try { Thread.sleep(3000); }
                    catch (Exception e) { System.out.println(e); }
                    result = MainActivity.connect.getSemester("https://webapp.yuntech.edu.tw/WebNewCAS/StudentFile/Score/");
                    status = (Map)result.get("status");
                    semesters = (Map)result.get("data");
                }

                Map temp = (Map)result.get("dom");
                conObject.put("dom", (Document)temp.get("document"));
                scoreViewModel.setSemestersList(semesters);

                final ArrayAdapter<String> semeList = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, scoreViewModel.getSemestersList());
                semeList.setDropDownViewResource(R.layout.spinner_dropdown_item);
                getActivity().runOnUiThread(() -> {
                    spinner.setAdapter(semeList);
                    String seme = scoreViewModel.getSemestersList().get(0).toString().replaceAll("(學年第)|(學期)", "");
                    createScore(seme, root, conObject);
                    loadedScore(root);
                });
            }
        };
        a.start();

        return root;
    }

    //--------------------叫出、關閉Loading--------------------//
    protected void loadingScore(View root){
        root.findViewById(R.id.score_loading).setVisibility(View.VISIBLE);
        root.findViewById(R.id.score_finishing).setVisibility(View.GONE);
    }
    protected void loadedScore(View root){
        root.findViewById(R.id.score_loading).setVisibility(View.GONE);
        root.findViewById(R.id.score_finishing).setVisibility(View.VISIBLE);
    }

    //--------------------取得及儲存課程內容--------------------//
    protected void createScore(final String seme, View root, final Map conObject) {

        //----------取得成績資料----------//
        final ArrayList<String> headerRow = new ArrayList<>();
        final ArrayList<Element> scoreRow = new ArrayList<>();
        //取得成績資料
        Thread b = new Thread(){
            public void run(){
                Elements el = MainActivity.connect.scoreData(seme, (Document)conObject.get("dom"));
                //headerRow
                Elements row = el.select("table").get(1).select("tr");
                for(Element k: row)
                    headerRow.add(k.select("td").get(1).text());
                //scoreRow
                row = el.select("table").get(0).select("tr[class$=\"Row\"]");
                for(Element k: row)
                    scoreRow.add(k);
            }
        };
        b.start();

        try{ b.join(); }
        catch(InterruptedException e) { e.printStackTrace(); }

        //----------建立成績內容----------//
        //Header Card
        List<Map<String, String>> scoreList = new ArrayList<>();
        Map<String, String> item = new HashMap<>();
        item.put("card_header_average", headerRow.get(4));
        item.put("card_header_rank", headerRow.get(5));
        item.put("card_header_credits1", headerRow.get(2));
        item.put("card_header_credits2", headerRow.get(3));
        item.put("card_header_behavior", headerRow.get(0));
        scoreList.add(item);
        //ScoreCard
        for(Element row: scoreRow){
            Elements td = row.select("td");
            item = new HashMap<>();
            item.put("card_scoreId", td.get(0).text());
            item.put("card_required", td.get(4).text());
            item.put("card_scoreTitle", td.get(2).select("a").text());
            item.put("card_teacher", td.get(6).text());
            item.put("card_credits", td.get(5).text());
            item.put("card_scoreNum", td.get(7).text());
            item.put("card_passDanger", td.get(9).text());
            scoreList.add(item);
        }
        RecyclerView recyclerView = root.findViewById(R.id.scoreRecyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new scoreItemAdapter(getContext(), scoreList));
    }

    //--------------------建構每個Card--------------------//
    private class scoreItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int HEADER_ITEM = 1;
        public static final int SCORE_ITEM = 2;

        private Context context;
        private List<Map<String, String>> scoreList;

        scoreItemAdapter(Context context, List<Map<String, String>> scoreList) {
            this.context = context;
            this.scoreList = scoreList;
        }

        //調用R.layout建立View，以此建立一個新的ViewHolder
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder = null;
            View view;
            if(HEADER_ITEM == viewType){
                view = LayoutInflater.from(context).inflate(R.layout.recyclerview_scorecard_header, parent, false);
                holder = new HeaderViewHolder(view);
            }else{
                view = LayoutInflater.from(context).inflate(R.layout.recyclerview_scorecard_item, parent, false);
                holder = new ScoreViewHolder(view);
            }
            return holder;
        }

        //透過position找到data，以此設置ViewHolder裡的View
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final Map<String, String> member = scoreList.get(position);
            if(holder instanceof HeaderViewHolder){
                ((HeaderViewHolder) holder).card_header_average.setText(member.get("card_header_average"));
                ((HeaderViewHolder) holder).card_header_rank.setText(member.get("card_header_rank"));
                ((HeaderViewHolder) holder).card_header_credits1.setText(member.get("card_header_credits1"));
                ((HeaderViewHolder) holder).card_header_credits2.setText(member.get("card_header_credits2"));
                ((HeaderViewHolder) holder).card_header_behavior.setText(member.get("card_header_behavior"));
            }else{
                ((ScoreViewHolder) holder).card_scoreId.setText(member.get("card_scoreId"));
                ((ScoreViewHolder) holder).card_required.setText(member.get("card_required"));
                ((ScoreViewHolder) holder).card_scoreTitle.setText(member.get("card_scoreTitle"));
                ((ScoreViewHolder) holder).card_teacher.setText(member.get("card_teacher"));
                ((ScoreViewHolder) holder).card_credits.setText(member.get("card_credits"));
                ((ScoreViewHolder) holder).card_scoreNum.setText(member.get("card_scoreNum"));
                ((ScoreViewHolder) holder).card_passDanger.setText(member.get("card_passDanger"));
            }
        }

        //取得數量
        @Override
        public int getItemCount() {
            return scoreList.size();
        }

        //定義ViewType
        @Override
        public int getItemViewType(int position) {
            if(position == 0) return HEADER_ITEM;
            else return SCORE_ITEM;
        }

        //在constructor裡存儲存自己用到的View。
        class HeaderViewHolder extends RecyclerView.ViewHolder{
            TextView card_header_average, card_header_rank, card_header_credits1, card_header_credits2, card_header_behavior;
            HeaderViewHolder(View itemView) {
                super(itemView);
                card_header_average = itemView.findViewById(R.id.card_header_average);
                card_header_rank = itemView.findViewById(R.id.card_header_rank);
                card_header_credits1 = itemView.findViewById(R.id.card_header_credits1);
                card_header_credits2 = itemView.findViewById(R.id.card_header_credits2);
                card_header_behavior = itemView.findViewById(R.id.card_header_behavior);
            }
        }
        class ScoreViewHolder extends RecyclerView.ViewHolder{
            TextView card_scoreId, card_required, card_scoreTitle, card_teacher, card_credits, card_scoreNum, card_passDanger;
            ScoreViewHolder(View itemView) {
                super(itemView);
                card_scoreId = itemView.findViewById(R.id.card_scoreId);
                card_required = itemView.findViewById(R.id.card_required);
                card_scoreTitle = itemView.findViewById(R.id.card_scoreTitle);
                card_teacher = itemView.findViewById(R.id.card_teacher);
                card_credits = itemView.findViewById(R.id.card_credits);
                card_scoreNum = itemView.findViewById(R.id.card_scoreNum);
                card_passDanger = itemView.findViewById(R.id.card_passDanger);
            }
        }
    }
}