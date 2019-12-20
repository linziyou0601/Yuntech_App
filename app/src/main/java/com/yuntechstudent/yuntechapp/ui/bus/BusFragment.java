package com.yuntechstudent.yuntechapp.ui.bus;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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

public class BusFragment extends Fragment {

    private BusViewModel BusViewModel;
    private LoginViewModel loginViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------取得本視圖、側邊欄視圖、登入資料--------------------//
        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        BusViewModel = ViewModelProviders.of(this).get(BusViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_bus, container, false);
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


        //--------------------設定標題欄項目可見度、公車表讀取視圖--------------------//
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        getActivity().findViewById(R.id.spinner_semester).setVisibility(View.GONE);
        loadingNews(root);

        Thread a = new Thread() {
            public void run() {
                String[] TAB_TYPE = {"往車站", "往雲科大"};
                ArrayList<ArrayList> pageBus = new ArrayList<>();

                ArrayList<Map<String, String>> stationRow = MainActivity.connect.toStationData();
                pageBus.add(stationRow);

                ArrayList<Map<String, String>> yuntechRow = MainActivity.connect.toYuntechData();
                pageBus.add(yuntechRow);

                getActivity().runOnUiThread(() -> {
                    loadedNews(root);
                    viewPager2.setAdapter(new ViewPagerAdapter(getContext(), pageBus));

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
        root.findViewById(R.id.bus_loading).setVisibility(View.VISIBLE);
        root.findViewById(R.id.bus_finishing).setVisibility(View.GONE);
    }
    protected void loadedNews(View root){
        root.findViewById(R.id.bus_loading).setVisibility(View.GONE);
        root.findViewById(R.id.bus_finishing).setVisibility(View.VISIBLE);
    }

    //--------------------建構每個消息列--------------------//
    private class busItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private ArrayList<Map<String, String>> itemRow;

        private int HEAD_ITEM = 0;
        private int BUS_ITEM = 1;

        busItemAdapter(ArrayList<Map<String, String>> itemRow, Context context){
            this.context = context;
            this.itemRow = itemRow;
        }

        //調用R.layout建立View，以此建立一個新的ViewHolder
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layout = viewType == HEAD_ITEM? R.layout.recyclerview_buscard_head: R.layout.recyclerview_buscard_item;
            View view = LayoutInflater.from(context).inflate(layout, parent, false);
            return (viewType == HEAD_ITEM)? (new HeadViewHolder(view)): (new BusViewHolder(view));
        }

        //透過position找到data，以此設置ViewHolder裡的View
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final Map<String, String> member = itemRow.get(position);
            if (holder instanceof BusViewHolder) {
                String color = member.get("company").equals("高鐵快捷公車")? "#03969e": "#8a0c2c";

                ((BusViewHolder) holder).bus_route.setText(member.get("company")+" "+member.get("number"));
                ((BusViewHolder) holder).bus_route.setTextColor(Color.parseColor(color));
                ((BusViewHolder) holder).bus_origin.setText(member.get("origin"));
                ((BusViewHolder) holder).bus_dest.setText(member.get("dest"));
                ((BusViewHolder) holder).bus_depart.setText(member.get("depart"));
                ((BusViewHolder) holder).bus_depart.setTextColor(Color.parseColor(color));

                LayerDrawable layerDrawable = (LayerDrawable)((BusViewHolder) holder).bus_relLayout.getBackground();
                ((GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.gradientDrawble)).setColor(Color.parseColor(color));

                if(!member.get("remark").equals(""))
                    ((BusViewHolder) holder).bus_remark.setText(member.get("remark"));
                else
                    ((BusViewHolder) holder).bus_remark.setVisibility(View.GONE);
            } else {
                ((HeadViewHolder) holder).originSpot.setText(member.get("originSpot"));
                ((HeadViewHolder) holder).madeTime.setText(member.get("madeTime"));
            }
        }

        //取得數量
        @Override
        public int getItemCount() {
            return itemRow.size();
        }

        //取得類型
        @Override
        public int getItemViewType(int position) {
            if(position == 0)
                return HEAD_ITEM;
            else
                return BUS_ITEM;
        }

        //一般資料ViewHolder
        class BusViewHolder extends RecyclerView.ViewHolder{
            RelativeLayout bus_relLayout;
            TextView bus_route, bus_origin, bus_dest, bus_depart, bus_remark;
            BusViewHolder(View itemView) {
                super(itemView);
                bus_relLayout = itemView.findViewById(R.id.bus_relLayout);
                bus_route = itemView.findViewById(R.id.bus_route);
                bus_origin = itemView.findViewById(R.id.bus_origin);
                bus_dest = itemView.findViewById(R.id.bus_dest);
                bus_depart = itemView.findViewById(R.id.bus_depart);
                bus_remark = itemView.findViewById(R.id.bus_remark);
            }
        }
        //頁首資料ViewHolder
        class HeadViewHolder extends RecyclerView.ViewHolder {
            TextView originSpot, madeTime;
            public HeadViewHolder(View itemView) {
                super(itemView);
                originSpot = itemView.findViewById(R.id.originSpot);
                madeTime = itemView.findViewById(R.id.madeTime);
            }
        }
    }

    //--------------------建構子頁面--------------------//
    private class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.PageViewHolder> {

        private Context context;
        private ArrayList<ArrayList> pageDatas;

        ViewPagerAdapter(Context context, ArrayList pageDatas) {
            this.context = context;
            this.pageDatas = pageDatas;
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
            //layout、adapter設定入recyclerView
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
            holder.recyclerView.setAdapter(new busItemAdapter(pageDatas.get(position), context));
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
                recyclerView = itemView.findViewById(R.id.recyView);
            }
        }
    }
}