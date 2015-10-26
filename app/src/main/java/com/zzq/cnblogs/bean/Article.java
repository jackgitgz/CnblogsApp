package com.zzq.cnblogs.bean;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zzq on 2015/10/21.
 */
//public class Article implements Serializable {
public class Article implements Parcelable {
    public static final String ARTICLE_LIST_JSON_URL = "http://152.123.55.102:8080/index.php";
    private String title;
    private String linkmd5id;
    private String link;
    private String desc;
    private int view;
    private int diggnum;
    private int comment;
    private int isFavorite;

    public int getView() {
        return view;
    }

    public void setView(int view) {
        this.view = view;
    }

    public int getDiggnum() {
        return diggnum;
    }

    public void setDiggnum(int diggnum) {
        this.diggnum = diggnum;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }


    public int getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(int isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getLinkmd5id() {
        return linkmd5id;
    }

    public void setLinkmd5id(String linkmd5id) {
        this.linkmd5id = linkmd5id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return this.title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString("title", getTitle());
        bundle.putString("desc", getDesc());
        bundle.putString("link", getLink());
        bundle.putString("linkmd5id", getLinkmd5id());
        dest.writeBundle(bundle);
    }

    public static final Creator<Article> CREATOR = new Creator<Article>() {
        @Override
        public Article createFromParcel(Parcel source) {
            Bundle bundle = source.readBundle();
            Article article = new Article();
            article.setTitle(bundle.getString("title"));
            article.setDesc(bundle.getString("desc"));
            article.setLink(bundle.getString("link"));
            article.setLinkmd5id(bundle.getString("linkmd5id"));
            return article;
        }

        @Override
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };
}
