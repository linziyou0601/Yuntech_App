package com.yuntechstudent.yuntechapp.ui.news;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.yuntechstudent.yuntechapp.MainActivity;
import com.yuntechstudent.yuntechapp.R;
import com.yuntechstudent.yuntechapp.ui.login.LoginViewModel;

import java.util.ArrayList;
import java.util.Map;

import static com.yuntechstudent.yuntechapp.ui.login.LoginViewModel.AuthenticationState.AUTHENTICATED;

public class NewsFragment extends Fragment {

    private NewsViewModel newsViewModel;
    private LoginViewModel loginViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------取得本視圖、側邊欄視圖、登入資料--------------------//
        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        newsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_news, container, false);
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


        //--------------------設定ViewPager--------------------//
        ViewPager2 viewPager2 = root.findViewById(R.id.view_pager);
        TabLayout tabLayout = root.findViewById(R.id.tabs);


        //--------------------設定標題欄項目可見度、課表讀取視圖--------------------//
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        getActivity().findViewById(R.id.spinner_semester).setVisibility(View.GONE);
        loadingNews(root);

        Thread a = new Thread() {
            public void run() {
                String[] TAB_TYPE = {"最新消息", "焦點消息"};
                ArrayList<Map<String, String>> newsRow = MainActivity.connect.newsData(0);
                ArrayList<Map<String, String>> spotRow = MainActivity.connect.spotlightData();
                ArrayList<ArrayList> pageNews = new ArrayList<>();
                pageNews.add(newsRow);
                pageNews.add(spotRow);

                getActivity().runOnUiThread(() -> {
                    loadedNews(root);
                    viewPager2.setAdapter(new ViewPagerAdapter(getContext(), pageNews));

                    TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, true, (tab, position) -> tab.setText(TAB_TYPE[position]));
                    tabLayoutMediator.attach();
                });
            }
        };
        a.start();

        return root;
    }

    //--------------------叫出、關閉Loading--------------------//
    protected void loadingNews(View root){
        root.findViewById(R.id.news_loading).setVisibility(View.VISIBLE);
        root.findViewById(R.id.news_finishing).setVisibility(View.GONE);
    }
    protected void loadedNews(View root){
        root.findViewById(R.id.news_loading).setVisibility(View.GONE);
        root.findViewById(R.id.news_finishing).setVisibility(View.VISIBLE);
    }

    //--------------------建構每個消息列--------------------//
    private class newsItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private ArrayList<Map<String, String>> itemRow;
        private int TAB_TYPE;

        private int NEWS_ITEM = 0;
        private int FOOT_ITEM = 1;

        private boolean hasMore;    //是否有資料增加

        newsItemAdapter(ArrayList<Map<String, String>> itemRow, Context context, boolean hasMore, int TAB_TYPE){
            this.context = context;
            this.itemRow = itemRow;
            this.hasMore = hasMore;
            this.TAB_TYPE = TAB_TYPE;
        }

        //調用R.layout建立View，以此建立一個新的ViewHolder
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            RecyclerView.ViewHolder holder;
            if (viewType == NEWS_ITEM) {
                view = LayoutInflater.from(context).inflate(R.layout.recyclerview_newscard_item, parent, false);
                holder = new NewsViewHolder(view);
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.recyclerview_newscard_foot, parent, false);
                holder = new FootViewHolder(view);
            }
            return holder;
        }

        //透過position找到data，以此設置ViewHolder裡的View
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof NewsViewHolder) {
                final Map<String, String> member = itemRow.get(position);
                ((NewsViewHolder) holder).card_newsTitle.setText(member.get("title"));
                ((NewsViewHolder) holder).card_newsLayout.setOnClickListener(new NewsOnClick(member.get("link")));
                if(TAB_TYPE == 0){
                    ((NewsViewHolder) holder).card_newsUnit.setText(member.get("unit"));
                    ((NewsViewHolder) holder).card_newsDate.setText(member.get("date"));
                }else{
                    ((NewsViewHolder) holder).card_newsContent.setVisibility(View.GONE);
                }
            } else {
                ((FootViewHolder) holder).tips.setText(hasMore? "正在載入更多...": "沒有更多資料");
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
            if(position == getItemCount() - 1)
                return FOOT_ITEM;
            else
                return NEWS_ITEM;
        }

        //一般資料ViewHolder
        class NewsViewHolder extends RecyclerView.ViewHolder{
            LinearLayout card_newsLayout;
            TextView card_newsTitle, card_newsUnit, card_newsDate;
            LinearLayout card_newsContent;
            NewsViewHolder(View itemView) {
                super(itemView);
                card_newsLayout = itemView.findViewById(R.id.card_newsLayout);
                card_newsTitle = itemView.findViewById(R.id.card_newsTitle);
                card_newsUnit = itemView.findViewById(R.id.card_newsUnit);
                card_newsDate = itemView.findViewById(R.id.card_newsDate);
                card_newsContent = itemView.findViewById(R.id.card_newsContent);
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
        //取得末列位置（不含載入更多）
        public int getRealLastPosition() {
            return itemRow.size();
        }
        //更新資料內容，並設定hasMore值
        public void updateList(ArrayList<Map<String,String>> newItems, boolean hasMore) {
            if(hasMore) itemRow.addAll(newItems);
            this.hasMore = hasMore;
            notifyDataSetChanged();
        }
    }

    //--------------------建構子頁面--------------------//
    private class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.PageViewHolder> {

        private Context context;
        private ArrayList<ArrayList> pageDatas;

        private int lastVisibleItem = 0;       //最後一筆可見資料項目位置
        private final int PAGE_COUNT = 16;     //每頁項目數
        private newsItemAdapter[] adapter;     //adapter
        private

        ViewPagerAdapter(Context context, ArrayList pageDatas) {
            this.context = context;
            this.pageDatas = pageDatas;
            adapter = new newsItemAdapter[pageDatas.size()];
        }

        //調用R.layout建立View，以此建立一個新的ViewHolder
        @Override
        public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.viewpager_item_page, parent, false);
            return new PageViewHolder(view);
        }

        //透過position找到data，以此設置ViewHolder裡的View
        @Override
        public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
            //----------於子頁面建立最新消息內容----------//
            //設定data、layout、adapter
            ArrayList<Map> data = pageDatas.get(position);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
            adapter[position] = new newsItemAdapter(pageDatas.get(position), context,
                                                    pageDatas.get(position).size() > 0 ? true : false, position);

            //layout、adapter設定入recyclerView
            holder.recyclerView.setLayoutManager(mLayoutManager);
            holder.recyclerView.addItemDecoration(new DividerItemDecoration(holder.recyclerView.getContext(), DividerItemDecoration.VERTICAL));
            holder.recyclerView.setAdapter(adapter[position]);


            //----------監聽子頁面RecyclerView的滑動----------//
            holder.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    //當滾動事件停止時觸發一次
                    if(newState == RecyclerView.SCROLL_STATE_IDLE)
                        if (lastVisibleItem + 1 == adapter[position].getItemCount())
                            updateRecyclerView(adapter[position].getRealLastPosition(), position);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    lastVisibleItem = mLayoutManager.findLastVisibleItemPosition(); //滑動完成後，取得最後一筆可見項目的位置
                }
            });

        }

        //取得數量
        @Override
        public int getItemCount() {
            return pageDatas.size();
        }

        //子頁面ViewHolder
        class PageViewHolder extends RecyclerView.ViewHolder {
            RecyclerView recyclerView;
            public PageViewHolder(View itemView) {
                super(itemView);
                recyclerView = itemView.findViewById(R.id.newsRecyView);
            }
        }

        //----------分頁資料----------//
        //更新子頁面RecyclerView
        private void updateRecyclerView(int startIndex, int position) {
            new Thread() {
                public void run() {
                    ArrayList<Map<String, String>> newItems = (position==0)? MainActivity.connect.newsData(startIndex): new ArrayList<>();
                    getActivity().runOnUiThread(() -> adapter[position].updateList((newItems.size()>0)? newItems: null, newItems.size()>0));
                }
            }.start();
        }
    }

    //--------------------監聽 消息點擊--------------------//
    public class NewsOnClick implements View.OnClickListener {
        String url;
        NewsOnClick(String u) { url = u; }

        @Override
        public void onClick(View v){
            Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }
}