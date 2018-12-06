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
            Intent intent = new Intent();
            intent.putExtra("categoryId", addCategory());
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TaskApp", ":: CategoryInputActivity#onCreate ::::::::::::::::::::::::::");
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
        Log.d("TaskApp", ":: CategoryInputActivity#addCategory :::::::::::::::::::::::");
        Realm realm = Realm.getDefaultInstance();
        int id = -1;
        try {
            Category category = new Category();
            RealmResults<Category> results = realm.where(Category.class).findAll();
            category.setId((results.max("id") != null ? results.max("id").intValue() + 1 : 0));
            category.setName(nameEditText.getText().toString());

            realm.beginTransaction();
            realm.copyToRealmOrUpdate(category);
            realm.commitTransaction();

            Log.d("TaskApp", category.toString());

            id = category.getId();
        } catch (Exception e) {
            realm.cancelTransaction();
        } finally {
            realm.close();
            return id;
        }
    }
}
