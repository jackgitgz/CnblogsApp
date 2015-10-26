package com.zzq.cnblogs.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zzq.cnblogs.R;
import com.zzq.cnblogs.bean.Article;
import com.zzq.cnblogs.utils.DBManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ArticleConententFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ArticleConententFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArticleConententFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TITLE = "title";
    private static final String ARG_LINK = "link";
    private static final String ARG_DESC = "desc";
    private static final String ARG_ARTICLE = "article";

    // TODO: Rename and change types of parameters
    private Article mArticle;

    private OnFragmentInteractionListener mListener;
    private int isFavorite;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param article Article 1
     * @return A new instance of fragment ArticleConententFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ArticleConententFragment newInstance(Article article) {
        ArticleConententFragment fragment = new ArticleConententFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ARTICLE, article);

        fragment.setArguments(args);
        return fragment;
    }

    public ArticleConententFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArticle = new Article();
        if (getArguments() != null) {
//            mArticle.setTitle(getArguments().getString(ARG_TITLE));
//            mArticle.setLink(getArguments().getString(ARG_LINK));
//            mArticle.setDesc(getArguments().getString(ARG_DESC));
//            mArticle = (Article) getArguments().getSerializable(ARG_ARTICLE);
            mArticle = (Article) getArguments().getParcelable(ARG_ARTICLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_article_conentent, container, false);

        TextView textView = (TextView) view.findViewById(R.id.article_content);
        textView.setText(String.format("%s \n %s", mArticle.getTitle(), mArticle.getDesc()));
//        Toast.makeText(getActivity(), String.format("%d , %d", textView.getLineHeight(), textView.getLineCount()), Toast.LENGTH_SHORT).show();
//        textView.setHeight(textView.getLineHeight() * textView.getLineCount());

        Button button = (Button) view.findViewById(R.id.open_webpage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mArticle.getLink()));
                intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                getActivity().startActivity(intent);
            }
        });


        final Button btnFavorite = (Button) view.findViewById(R.id.favorite);
        DBManager dbManager = new DBManager(getActivity());
        SQLiteDatabase sqLiteDb2 = dbManager.getReadableDatabase();
        String[] columnVal = {mArticle.getLinkmd5id()};
        Cursor c = sqLiteDb2.query("article", null, "linkmd5id = ?", columnVal, null, null, "1");
        if(c.getCount() > 0) {
            c.moveToPosition(0);
            isFavorite = c.getInt(c.getColumnIndex("is_favorite"));
            String btnFavoriteText = 1 == isFavorite ? "取消收藏" : "收藏";
            btnFavorite.setText(btnFavoriteText);
        }
        sqLiteDb2.close();
        dbManager.close();

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBManager dbManager = new DBManager(getActivity());
                SQLiteDatabase sqLiteDb = dbManager.getWritableDatabase();
                sqLiteDb.beginTransaction();
                isFavorite = 1 == isFavorite ? 0 : 1;
                String[] whereColumn = {"" + isFavorite, mArticle.getLinkmd5id()};
//                ContentValues contentValues = new ContentValues();
//                contentValues.put("is_favorite", 1);
//                int result = sqLiteDb.update("article", contentValues, "linkmd5id = ?", whereColumn);
                sqLiteDb.execSQL("update article set is_favorite = ? where linkmd5id = ?", whereColumn);
//                System.out.println("update result===========" + result + "----------））" + mArticle.getLinkmd5id());
                sqLiteDb.setTransactionSuccessful();
                sqLiteDb.endTransaction();

                sqLiteDb.close();

                dbManager.close();

                String btnFavoriteText = 1 == isFavorite ? "取消收藏" : "收藏";
                btnFavorite.setText(btnFavoriteText);
                String toastText = 1 == isFavorite ? "收藏" : "取消收藏";
                Toast.makeText(getActivity(), "已" + toastText, Toast.LENGTH_SHORT).show();

            }
        });


        WebView webView = (WebView) view.findViewById(R.id.webView);
        //加载服务器上的页面
        webView.loadUrl(mArticle.getLink());
        //不以浏览器的方式打开
        webView.setWebViewClient(new WebViewClient());
        //获取浏览器设置
        WebSettings webSettings = webView.getSettings();
        //允许javascript
        webSettings.setJavaScriptEnabled(true);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
}
