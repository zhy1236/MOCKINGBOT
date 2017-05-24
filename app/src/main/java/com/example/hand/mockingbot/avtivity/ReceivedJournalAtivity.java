package com.example.hand.mockingbot.avtivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.hand.mockingbot.R;
import com.example.hand.mockingbot.adapter.ListAdapter;
import com.example.hand.mockingbot.datamanage.HttpManager;
import com.example.hand.mockingbot.entity.AddDailySeeEntivy;
import com.example.hand.mockingbot.entity.LoginEntity;
import com.example.hand.mockingbot.entity.ReceivedJournalEntity;
import com.example.hand.mockingbot.utils.CommonValues;
import com.example.hand.mockingbot.utils.HandApp;
import com.example.hand.mockingbot.view.SimpleListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhy on 2017/5/5.
 */

public class ReceivedJournalAtivity extends BasicActivity implements AdapterView.OnItemClickListener, SimpleListView.OnRefreshListener {

    private Toolbar toolbar;
    private boolean hasMore = true;
    private SimpleListView lv;
    private int index = 1;
    private ReceivedJournalEntity receivedJournalEntity;
    private List<ReceivedJournalEntity.ResultBean.DataBean> list = new ArrayList<>();
    private ListAdapter<ReceivedJournalEntity.ResultBean.DataBean> listAdapter = new ListAdapter<ReceivedJournalEntity.ResultBean.DataBean>(list, R.layout.journal_item) {
        @Override
        public void bindView(ViewHolder holder, ReceivedJournalEntity.ResultBean.DataBean obj) {
            if (obj.getRealname().equals(HandApp.getLoginEntity().getResult().getData().getRealname())){
                if (HandApp.getPhotoUri() != null){
                    ImageView iv = holder.getView(R.id.journal_item_imv);
                    iv.setImageURI(HandApp.getPhotoUri());
                }
            }else {
                holder.setImageResource(R.id.journal_item_imv,R.mipmap.ic_head_portrait);
            }
            if (obj.getFinishWork() != null){
                holder.setText(R.id.journal_item_tv_finish, obj.getFinishWork());
            }else {
                holder.setText(R.id.journal_item_tv_finish, "");
            }
            if (obj.getUnfinishWork() != null){
                holder.setText(R.id.journal_item_tv_unfinish, obj.getUnfinishWork());
            }else {
                holder.setText(R.id.journal_item_tv_unfinish, "");
            }
            if (obj.getCoordinationWork() != null){
                holder.setText(R.id.journal_item_tv_nedhellp, obj.getCoordinationWork());
            }else {
                holder.setText(R.id.journal_item_tv_nedhellp, "");
            }
            if (obj.getRemark() != null){
                if (!obj.getRemark().isEmpty()){
                    holder.setText(R.id.journal_item_tv_remark, obj.getRemark());
                    holder.setVisibility(R.id.journal_item_tv_remark,View.VISIBLE);
                    holder.setVisibility(R.id.journal_item_remark,View.VISIBLE);
                }else {
                    holder.setVisibility(R.id.journal_item_tv_remark,View.GONE);
                    holder.setVisibility(R.id.journal_item_remark,View.GONE);
                }
            }
            String realname = obj.getRealname();
            String time = getTime(obj.getSubmitDate());
            holder.setText(R.id.journal_item_tv_name,realname);
            holder.setText(R.id.journal_item_tv_time,time);

        }
    };
    private RelativeLayout pb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_journal);
        initToolbar();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(index);
    }

    private void loadData(final int index) {
        LoginEntity.ResultBean.DataBean data = HandApp.getLoginEntity().getResult().getData();
        String url = CommonValues.JOURNAL_LIST + "userId=" + data.getId() + "&dailyTime=" + getData(true) + "&state=0&pageNo=" + index + "&pageSize=10";
        HttpManager.getInstance().get(url, ReceivedJournalEntity.class, new HttpManager.ResultCallback<ReceivedJournalEntity>() {
            @Override
            public void onSuccess(String json, ReceivedJournalEntity receivedJournal) throws InterruptedException {
                receivedJournalEntity = receivedJournal;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pb.setVisibility(View.GONE);
                        if (index == 1){
                            list.clear();
                        }
                        list.addAll(receivedJournalEntity.getResult().getData());
                        hasMore = receivedJournalEntity.getResult().getData().size() > 0;
                        if (list.size() < receivedJournalEntity.getResult().getPage().getTotal_elements()){
                            hasMore = true;
                        }else {
                            hasMore = false;
                        }
                        listAdapter.notifyDataSetChanged();
                        lv.completeRefresh();
                    }
                });
            }

            @Override
            public void onFailure(String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pb.setVisibility(View.GONE);
                        lv.completeRefresh();
                    }
                });
            }
        });
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setTitle("");
//        设置左侧图标和点击事件
        toolbar.setNavigationIcon(R.mipmap.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void initView() {
        lv = (SimpleListView) findViewById(R.id.journal_receiver_lv);
        lv.setOnRefreshListener(this);
        lv.setAdapter(listAdapter);
        lv.setOnItemClickListener(this);
        pb = (RelativeLayout) findViewById(R.id.journal_receiver_pb);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (lv.isPullRefreshed()){
            return;
        }else {
            pb.setVisibility(View.VISIBLE);
            adddailySee(list.get(i-1).getDailyId());
            toLookUpJournal(i-1);
        }
    }


    private void toLookUpJournal(int i) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),LookUpJournalActivity.class);
        intent.putExtra("dailyId",list.get(i).getDailyId());
        intent.putExtra("time",getTime(list.get(i).getSubmitDate()));
        intent.putExtra("name",list.get(i).getRealname());
        intent.putExtra("focus",list.get(i).getFocus());
        intent.putExtra("my",false);
        startActivity(intent);
    }

    @Override
    public void onPullRefresh() {
        hasMore = true;
        index = 1;
        loadData(index);
        pb.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadingMore() {
        if (hasMore) {
            index += 1;
            loadData(index);
        } else {
            lv.completeRefresh();
        }
    }

    @Override
    public void onScrollOutside() {

    }

    private void adddailySee(int id) {
        String url = CommonValues.ADD_DAILYSEE + "userId=" + HandApp.getLoginEntity().getResult().getData().getId() + "&dailyId=" + id;
        HttpManager.getInstance().get(url, AddDailySeeEntivy.class, new HttpManager.ResultCallback<AddDailySeeEntivy>() {
            @Override
            public void onSuccess(String json, AddDailySeeEntivy addDailySeeEntivy) throws InterruptedException {

            }

            @Override
            public void onFailure(String msg) {

            }
        });
    }
}
