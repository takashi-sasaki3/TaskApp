package jp.techacademy.takashi.sasaki.taskapp;

import android.app.Application;
import android.util.Log;

import io.realm.Realm;

public class TaskApp extends Application {

    @Override
    public void onCreate() {
        Log.d("TaskApp", ":: TaskApp#onCreate :::::::::::::::::::::::::::::::::::::::::");
        super.onCreate();

        Realm.init(this);

        //clearRealm();

        Realm realm = Realm.getDefaultInstance();
        if (realm.where(Category.class).equalTo("id", 0).findAll().size() == 0) {
            Log.d("TaskApp", "create default category");
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Category category = realm.createObject(Category.class, 0);
                    category.setName("未分類"); // 0:未分類作成
                }
            });
        }
        realm.close();
    }

    private void clearRealm() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Task.class).findAll().deleteAllFromRealm();
                realm.where(Category.class).findAll().deleteAllFromRealm();
            }
        });
        realm.close();
    }

}
