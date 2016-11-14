package com.uu.slidelayout;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.uu.slidelayout.R.id.swipeLayout;

public class MainActivity extends Activity {

    private ListView lvMain;
    private ArrayList<String> names = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        for (int i = 0; i < 20; i++) {
            names.add("攻城狮" + i + "号");
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        lvMain = (ListView) findViewById(R.id.lv_main);
        lvMain.setAdapter(new MyAdapter());
        lvMain.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    //如果垂直滑动，则需要关闭已经打开的layout
                    SwipeLayoutManager.getInstance().closeCurrentLayout();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });

    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return names.size();
        }

        @Override
        public String getItem(int position) {
            return names.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(getApplicationContext(), R.layout.adapter_list, null);
                holder.swipeLayout = (SwipeLayout) convertView.findViewById(swipeLayout);
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                convertView.setTag(holder);


            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //添加数据
            holder.tvName.setText(names.get(position));
            //设置相应的事件
            holder.swipeLayout.setOnSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void open() {
                    Toast.makeText(getApplicationContext(), "open", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void close() {
                    Toast.makeText(getApplicationContext(), "close", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void swiping(float fraction) {
                   // Toast.makeText(getApplicationContext(), "fraction=" + fraction, Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;
        }
    }


    static class ViewHolder {
        public TextView tvName;
        public ImageView ivHead;
        public SwipeLayout swipeLayout;
    }
}
