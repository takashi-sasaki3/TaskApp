package jp.techacademy.takashi.sasaki.taskapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import io.realm.Realm;
import io.realm.RealmResults;

public class CategoryInputActivity extends AppCompatActivity {

    private EditText nameEditText;

    private View.OnClickListener onDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = addCategory();
            Intent intent = new Intent();
            intent.putExtra("categoryId", id);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_input);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nameEditText = findViewById(R.id.nameEditText);
        findViewById(R.id.doneButton).setOnClickListener(onDoneClickListener);
    }

    private int addCategory() {
        Realm realm = Realm.getDefaultInstance();
        int id = -1;
        try {
            Category category = new Category();
            category.setName(nameEditText.getText().toString());
            RealmResults<Category> results = realm.where(Category.class).findAll();
            if (results.max("id") != null) {
                category.setId(results.max("id").intValue() + 1);
            } else {
                category.setId(0);
            }
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(category);
            realm.commitTransaction();
            id = category.getId();
        } catch (Exception e) {
            realm.cancelTransaction();
        } finally {
            realm.close();
            return id;
        }
    }
}
