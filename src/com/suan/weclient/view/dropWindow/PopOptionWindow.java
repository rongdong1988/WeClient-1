package com.suan.weclient.view.dropWindow;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.suan.weclient.R;
import com.suan.weclient.util.data.DataManager;
import com.suan.weclient.util.data.bean.FansGroupBean;
import com.suan.weclient.util.data.holder.FansHolder;
import com.suan.weclient.util.net.WechatManager;
import com.suan.weclient.view.ptr.PTRListview;

import java.util.ArrayList;

public class PopOptionWindow extends PopupWindow {

    private DataManager mDataManager;
    private ListView contentListView;
    private ArrayList<String> list = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private Context mContext;

    public PopOptionWindow(DataManager dataManager, Context context, View contentView, int width, int height,
                           boolean focusable) {

        super(contentView, width, height, focusable);
        mContext = context;
        mDataManager = dataManager;
        initListener();
        contentListView = (ListView) contentView
                .findViewById(R.id.drop_down_list);

        adapter = new ArrayAdapter<String>(
                contentView.getContext(),
                R.layout.drop_down_item, R.id.drop_down_item_text, getData());
        contentListView.setAdapter(adapter);
        contentListView.setOnItemClickListener(new ItemClickListener());

    }

    private void initListener() {
        mDataManager.addFansListChangeListener(new DataManager.FansListChangeListener() {
            @Override
            public void onFansGet(int mode) {
                getData();

            }
        });
    }

    public class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                //all
                mDataManager.getCurrentFansHolder().setCurrentGroupIndex(-1);
                mDataManager.getWechatManager().getFansList(0, mDataManager.getCurrentPosition(), mDataManager.getCurrentFansHolder().getCurrentGroupId(), new WechatManager.OnActionFinishListener() {
                    @Override
                    public void onFinish(int code, Object object) {
                        mDataManager.doFansGet(PTRListview.PTR_MODE_REFRESH);

                    }
                });

            } else {
                mDataManager.getCurrentFansHolder().setCurrentGroupIndex(position - 1);
                mDataManager.getWechatManager().getFansList(0, mDataManager.getCurrentPosition(), mDataManager.getCurrentFansHolder().getCurrentGroupId(), new WechatManager.OnActionFinishListener() {
                    @Override
                    public void onFinish(int code, Object object) {

                        mDataManager.doFansGet(PTRListview.PTR_MODE_REFRESH);
                    }
                });

            }


        }

    }

    private ArrayList<String> getData() {

        list.clear();
        FansHolder currentFansHolder = mDataManager.getCurrentFansHolder();
        ArrayList<FansGroupBean> fansGroupBeans = currentFansHolder.getFansGroupBeans();
        list.add(mContext.getResources().getString(R.string.all_user));
        for (int i = 0; i < fansGroupBeans.size(); i++) {
            list.add(fansGroupBeans.get(i).getGroupName());
        }
        return list;
    }


}
