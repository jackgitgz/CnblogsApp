package com.zzq.cnblogs.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.zzq.cnblogs.bean.Article;
import com.zzq.cnblogs.utils.DBManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * Created by zzq on 2015/10/23.
 */
public class ArticleDao {

    private Context context;
    public static final int PAGE_ROW = 10;

    public ArticleDao(Context context) {
        this.context = context;
    }

    public void addArticleToDb(Article article) {
        DBManager dbManager = new DBManager(context);
        SQLiteDatabase sqLiteDatabase = dbManager.getWritableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("select count(*) from article where linkmd5id = ?", new String[]{article.getLinkmd5id()});
        cursor.moveToFirst();
        if (cursor.getInt(0) > 0 ) {
            return;
        }

        sqLiteDatabase.beginTransaction();
        String[] whereValue = {article.getTitle(), article.getLinkmd5id(), article.getDesc(),
                article.getLink(), "" + article.getView(), "" + article.getComment(), "" + article.getDiggnum()};
        sqLiteDatabase.execSQL("insert into article(title, linkmd5id, desc, link, view, " +
                "comment, diggnum) values(?,?,?,?,?,?,?)", whereValue);
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
        sqLiteDatabase.close();
        dbManager.close();
    }

    public long getArticleCount(String where, String[] whereArgs) {
        DBManager dbManager = new DBManager(context);
        SQLiteDatabase sqLiteDatabase = dbManager.getReadableDatabase();
        where = null == where ? " 1 = 1 " : where;
        Cursor cursor = sqLiteDatabase.rawQuery("select count(*) from article where 1 = 1 and " + where, whereArgs);
        cursor.moveToFirst();
        long tabCount = cursor.getLong(0);
        return tabCount;
    }

    public LinkedList<Article> getArticleListFromServer(int page) {
        String url = Article.ARTICLE_LIST_JSON_URL + "?p=" + page;
        final LinkedList<Article> data = new LinkedList<Article>();

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
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
                        data.add(article);
                    }

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

        return data;
    }

    public LinkedList<Article> getArticleListFromDb(String selection, String[] selectionArgs, int page) {
        final LinkedList<Article> data = new LinkedList<Article>();
        DBManager dbManager = new DBManager(context);
        SQLiteDatabase sqLiteDb = dbManager.getReadableDatabase();
        String limit = ((page - 1) * PAGE_ROW) + ", " + PAGE_ROW;

        Cursor c = sqLiteDb.query(true, "article", null, selection, selectionArgs, null, null, null, limit);
        Article article;
        while (c.moveToNext()) {
            article = new Article();
            article.setTitle(c.getString(c.getColumnIndex("title")));
            article.setLink(c.getString(c.getColumnIndex("link")));
            article.setLinkmd5id(c.getString(c.getColumnIndex("linkmd5id")));
            article.setDesc(c.getString(c.getColumnIndex("desc")));
            article.setIsFavorite(c.getInt(c.getColumnIndex("is_favorite")));
            data.add(article);
        }

        sqLiteDb.close();
        dbManager.close();
        return data;
    }
}
