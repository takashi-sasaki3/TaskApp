package jp.techacademy.takashi.sasaki.taskapp;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject implements Serializable {

    @PrimaryKey
    private int id;

    private String title;

    private String contents;

    private Date date;

    private Category category;

    public Task() {
        super();
    }

    public Task(int id, String title, String contents) {
        this.id = id;
        this.title = title;
        this.contents = contents;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "{id:" + id + "},{title:" + title + "},{date:"
                + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE).format(date)
                + "},{category:" + category.getId() + " " + category.getName() + "}";
    }
}
