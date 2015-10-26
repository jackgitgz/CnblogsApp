package com.zzq.cnblogs.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.zzq.cnblogs.MainActivity;
import com.zzq.cnblogs.R;
import com.zzq.cnblogs.bean.Article;
import com.zzq.cnblogs.dao.ArticleDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private ArrayAdapter<Article> adapter;
    private LinkedList<Article> listData = new LinkedList<Article>();
    private PullToRefreshListView pullToRefreshView;
    private static final int REFRESH_TYPE_UP = 0;//上拉刷新
    private static final int REFRESH_TYPE_DOWN = 1;//下拉刷新
    private static int curPage = 1;
    private ArticleDao articleDao;
    private String where = null;
    private String[] whereArgs = null;
    private ViewPager viewPager;
    private LinearLayout point_group;
    private TextView image_desc;
    // 图片资源id
    private final int[] images = {R.drawable.a, R.drawable.b, R.drawable.c,
            R.drawable.d, R.drawable.e};
    // 图片标题集合
    private final String[] imageDescriptions = new String[5];

    private ArrayList<ImageView> imageList;
    // 上一个页面的位置
    protected int lastPosition = 0;

    // 判断是否自动滚动viewPager
    private boolean isRunning = true;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            // 执行滑动到下一个页面
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);

            if (isRunning) {
                // 在发一个handler延时
                handler.sendEmptyMessageDelayed(0, 5000);
            }
        }
    };

    private Handler handlerListView = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = (Bundle) msg.obj;
            int refreshType = (int) bundle.get("refreshType");
            LinkedList<Article> list = (LinkedList<Article>) bundle.get("article");
            for (Article article : list){
                if (REFRESH_TYPE_UP == refreshType){
                    listData.add(article);
                }else if (REFRESH_TYPE_DOWN == refreshType){
                    listData.add(0, article);
                }
            }

            pullToRefreshView.onRefreshComplete();
            adapter.notifyDataSetChanged();
        }
    };

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final int position = getArguments().getInt(ARG_SECTION_NUMBER);
        curPage = 1;
        articleDao = new ArticleDao(getActivity());

        if (2 == position) {
            where = " is_favorite = ? ";
            whereArgs = new String[]{"1"};
        }

        pullToRefreshView = (PullToRefreshListView) rootView.findViewById(R.id.pull_to_refresh_listview);
        pullToRefreshView.setMode(PullToRefreshBase.Mode.BOTH);//两端刷新
//        pullToRefreshView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);//上拉刷新
//        pullToRefreshView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);//下拉刷新
        pullToRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                refreshView.getLoadingLayoutProxy().setRefreshingLabel(getString(R.string.loading));
                refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.downnloadmore));
                refreshView.getLoadingLayoutProxy().setReleaseLabel(getString(R.string.startload));
//                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后加载时间:");
                if (1 == position) {
                    getArticleList(1, REFRESH_TYPE_DOWN);
                } else if (2 == position) {
                    new GetDataTask().execute(1, REFRESH_TYPE_DOWN);
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                refreshView.getLoadingLayoutProxy().setRefreshingLabel(getString(R.string.loading));
                refreshView.getLoadingLayoutProxy().setPullLabel(getString(R.string.uploadmore));
                refreshView.getLoadingLayoutProxy().setReleaseLabel(getString(R.string.startload));
//                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel("最后加载时间:");
                if (1 == position) {
                    int localPage = (int) (articleDao.getArticleCount(where, whereArgs) / ArticleDao.PAGE_ROW);
                    if (localPage > curPage) {
                        new GetDataTask().execute(curPage + 1, REFRESH_TYPE_UP);
                    } else {
                        getArticleList(curPage + 1, REFRESH_TYPE_UP);//如果本地没有则从服务器中获取
                    }
                } else if (2 == position) {
                    new GetDataTask().execute(curPage + 1, REFRESH_TYPE_UP);
                }
            }
        });

        listData = articleDao.getArticleListFromDb(where, whereArgs, 1);//从本地数据库中获取
        if (1 == position && listData.size() <= 0) {
            getArticleList(1, REFRESH_TYPE_DOWN);//从服务器中获取
        }
        adapter = new ArrayAdapter<Article>(this.getActivity(),
                android.R.layout.simple_list_item_1, listData);
        pullToRefreshView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        pullToRefreshView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Article article = adapter.getItem(position - 1);

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.container, ArticleConententFragment.newInstance(article))
                        .commit();
            }
        });

        //轮播图片
        viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        point_group = (LinearLayout) rootView.findViewById(R.id.point_group);
        image_desc = (TextView) rootView.findViewById(R.id.image_desc);
        for (int i = 0; listData.size() > i && i < 5; i++) {
            imageDescriptions[i] = listData.get(i).getTitle();
        }
        image_desc.setText(imageDescriptions[0]);

        // 初始化图片资源
        imageList = new ArrayList<ImageView>();
        for (int i : images) {
            // 初始化图片资源
            ImageView imageView = new ImageView(getActivity());
            imageView.setBackgroundResource(i);
            imageList.add(imageView);

            // 添加指示小点
            ImageView point = new ImageView(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100,
                    15);
            params.rightMargin = 20;
            params.bottomMargin = 10;
            point.setLayoutParams(params);
            if (i == R.drawable.a) {
                //默认聚焦在第一张
                point.setBackgroundResource(R.drawable.point_bg_focus2);
                point.setEnabled(true);
            } else {
                point.setBackgroundResource(R.drawable.point_bg);
                point.setEnabled(false);
            }

            point_group.addView(point);
        }

        viewPager.setAdapter(new MyPageAdapter());
        // 设置当前viewPager的位置
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2
                - (Integer.MAX_VALUE / 2 % imageList.size()));
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // 页面切换后调用， position是新的页面位置

                // 实现无限制循环播放
                position %= imageList.size();

                image_desc.setText(imageDescriptions[position]);

                // 把当前点设置为true,将上一个点设为false；并设置point_group图标
                point_group.getChildAt(position).setEnabled(true);
                point_group.getChildAt(position).setBackgroundResource(R.drawable.point_bg_focus2);
                point_group.getChildAt(lastPosition).setEnabled(false);
                point_group.getChildAt(lastPosition).setBackgroundResource(R.drawable.point_bg);
                lastPosition = position;

            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
                // 页面正在滑动时间回调

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 当pageView 状态发生改变的时候，回调

            }
        });

        /**
         * 自动循环： 1.定时器：Timer 2.开子线程：while true循环 3.ClockManger
         * 4.用Handler发送延时信息，实现循环，最简单最方便
         *
         */

        handler.sendEmptyMessageDelayed(0, 3000);

        return rootView;
    }

    private class GetDataTask extends AsyncTask<Integer, Void, String[]> {
        private LinkedList<Article> newsArticleList = new LinkedList<Article>();
        private LinkedList<Article> addList = new LinkedList<Article>();
        private int waitTimes = 10;
        int refreshType;

        @Override
        protected String[] doInBackground(Integer... params) {
            try {
                int page = params[0];
                refreshType = params[1];
                if (REFRESH_TYPE_DOWN == refreshType) {
                    /*newsArticleList = articleDao.getArticleListFromServer(page);//用此种方式需要另外控制线程的返回时间，相当于线程又启动了另外一个线程。
                    while (newsArticleList.size() < 1 && waitTimes > 0) {
                        Thread.sleep(1000);
                        curPage = page + 1;
                        waitTimes -= waitTimes;
                    }
                    System.out.println("----getArticleListFromServer---333---" + newsArticleList.size());
                    for (Article article : newsArticleList) {
                        boolean flag = false;
                        for (Article articleLocal : listData) {
                            if (article.getLinkmd5id().equals(articleLocal.getLinkmd5id())) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            articleDao.addArticleToDb(article);
                            listData.add(0, article);
                        }
                    }*/
                } else if (REFRESH_TYPE_UP == refreshType) {
                    LinkedList<Article> arclist = articleDao.getArticleListFromDb(where, whereArgs, page);
                    for (Article acl : arclist) {
                        boolean flag = false;
                        for (Article articleLocal : listData) {
                            if (acl.getLinkmd5id().equals(articleLocal.getLinkmd5id())) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            addList.add(acl);
                        }
                    }
                    curPage = page;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new String[0];
        }

        @Override
        protected void onPostExecute(String[] result) {
            // Call onRefreshComplete when the list has been refreshed.
            super.onPostExecute(result);

//            pullToRefreshView.onRefreshComplete();
//            adapter.notifyDataSetChanged();

            //handlerListView.sendEmptyMessage(0);
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putInt("refreshType", refreshType);
            bundle.putSerializable("article", addList);
            msg.obj = bundle;
            handlerListView.sendMessage(msg);
        }
    }

    /**
     * @param page        页数
     * @param refreshType 类型：0表示从前面插入最新内容（即下拉刷新），1表示从后面插入内容（即上拉刷新）
     */
    private void getArticleList(final int page, final int refreshType) {
        final LinkedList<Article> newsArticleList = new LinkedList<Article>();
        final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        String url = Article.ARTICLE_LIST_JSON_URL + "?p=" + page;
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                int length = response.length();
                try {
                    JSONObject jsonObject;
                    Article article;
                    for (int i = 0; i < length; i++) {
                        jsonObject = response.getJSONObject(i);
                        article = new Article();
                        article.setTitle(jsonObject.getString("title"));
                        article.setLink(jsonObject.getString("link"));
                        article.setLinkmd5id(jsonObject.getString("linkmd5id"));
                        article.setDesc(jsonObject.getString("desc"));
                        article.setView(jsonObject.getInt("view"));
                        article.setComment(jsonObject.getInt("comment"));
                        article.setDiggnum(jsonObject.getInt("diggnum"));
                        newsArticleList.add(article);
                    }

                    for (Article acl : newsArticleList) {
                        boolean flag = false;
                        for (Article articleLocal : listData) {
                            if (acl.getLinkmd5id().equals(articleLocal.getLinkmd5id())) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            articleDao.addArticleToDb(acl);
                            if (REFRESH_TYPE_DOWN == refreshType) {
                                listData.add(0, acl);
                            } else if (REFRESH_TYPE_UP == refreshType) {
                                listData.add(acl);
                            }
                        }
                    }

                    pullToRefreshView.onRefreshComplete();
                    //刷新数据
                    adapter.notifyDataSetChanged();
                    curPage = page;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonArrayRequest);

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDestroy() {
        // 停止滚动
        isRunning = false;
        super.onDestroy();
    }

    private class MyPageAdapter extends PagerAdapter {
        // 需要实现以下四个方法

        @Override
        public int getCount() {
            // 获得页面的总数
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            // 判断view和Object对应是否有关联关系
            if (view == object) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // 获得相应位置上的view； container view的容器，其实就是viewpage自身,
            // position: viewpager上的位置
            // 给container添加内容
            container.addView(imageList.get(position % imageList.size()));

            return imageList.get(position % imageList.size());
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // 销毁对应位置上的Object
            // super.destroyItem(container, position, object);
            container.removeView((View) object);
            object = null;
        }

    }
}

