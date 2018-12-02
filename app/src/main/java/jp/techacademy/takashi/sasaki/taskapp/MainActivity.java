package jp.techacademy.takashi.sasaki.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_TASK = "jp.techacademy.takashi.sasaki.taskapp.TASK";

    private ListView listView;

    private Spinner spinner;

    private TaskAdapter taskAdapter;

    private CategoryAdapter categoryAdapter;

    private Realm realm;

    private Category defaultCategory;

    private Category selectedCategory;

    private int selectedCategoryPosition = 0;

    private void prepareRealm() {
        realm = Realm.getDefaultInstance();

        // 未分類作成
        if (realm.where(Category.class).equalTo("id", 0).findAll().size() == 0) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Category category = realm.createObject(Category.class, 0);
                    category.setName("未分類");
                }
            });
        }

//        RealmResults<Category> results = realm.where(Category.class).findAll();
//        for (Category category : results) {
//            Log.d("TaskApp", "" + category.getId() + "/" + category.getName());
//        }
//        Log.d("TaskApp", "size:" + results.size());

        // 起動時は未分類タスクを表示
        defaultCategory = realm.where(Category.class).equalTo("id", 0).findFirst();
        selectedCategory = defaultCategory;

//        // 一旦全削除
//        realm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                realm.where(Task.class).findAll().deleteAllFromRealm();
//            }
//        });


        // testタスク登録
        defaultCategory = realm.where(Category.class).equalTo("id", 0).findFirst();

//        realm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                Task task = realm.createObject(Task.class, 0);
//                task.setTitle("タスク1");
//                task.setContents("タスク1");
//                task.setDate(new Date());
//                task.setCategory(defaultCategory);
//            }
//        });

//        realm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                Task task = realm.createObject(Task.class, 1);
//                task.setTitle("タスク2");
//                task.setContents("タスク2");
//                task.setDate(new Date());
//                task.setCategory(realm.where(Category.class).equalTo("id", 1).findFirst());
//            }
//        });

        RealmResults<Task> results = realm.where(Task.class).findAll();
        for (Task task : results) {
            Log.d("TaskApp", "" + task.getId() + "/" + task.getTitle() + "/" + task.getContents() + "/" + task.getCategory().getName());
        }
        Log.d("TaskApp", "size:" + results.size());

        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                reloadView();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        prepareRealm();

        taskAdapter = new TaskAdapter(MainActivity.this);
        categoryAdapter = new CategoryAdapter(MainActivity.this);

        listView = findViewById(R.id.listView1);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = (Task) parent.getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Task task = (Task) parent.getAdapter().getItem(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RealmResults<Task> taskRealmResults = realm.where(Task.class).equalTo("id", task.getId()).findAll();
                        realm.beginTransaction();
                        taskRealmResults.deleteAllFromRealm();
                        realm.commitTransaction();

                        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(pendingIntent);

                        reloadView();
                    }
                });
                builder.setNegativeButton("CANCEL", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        });

        spinner = findViewById(R.id.categorySpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    Spinner spinner = (Spinner) parent;
                    selectedCategory = (Category) spinner.getSelectedItem();
                    Log.d("TaskApp", "selected:" + selectedCategory.getId() + "" + selectedCategory.getName());
                    Log.d("TaskApp", "position:" + position);

                    reloadTaskList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        reloadView();
    }

    private void reloadView() {
        reloadTaskList();
        reloadCategoryList();
    }

    private void reloadTaskList() {
        RealmResults<Task> taskResults = realm.where(Task.class).equalTo("category.id", selectedCategory.getId()).findAll().sort("date", Sort.DESCENDING);
        taskAdapter.setTaskList(realm.copyFromRealm(taskResults));
        listView.setAdapter(taskAdapter);
        taskAdapter.notifyDataSetChanged();
    }

    private void reloadCategoryList() {
        RealmResults<Category> categoryResults = realm.where(Category.class).findAll().sort("name", Sort.DESCENDING);
        categoryAdapter.setCategories(realm.copyFromRealm(categoryResults));
        spinner.setAdapter(categoryAdapter);
        categoryAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
