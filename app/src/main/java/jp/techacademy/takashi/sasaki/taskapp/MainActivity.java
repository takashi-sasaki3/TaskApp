package jp.techacademy.takashi.sasaki.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_TASK = "jp.techacademy.takashi.sasaki.taskapp.TASK";

    private ListView taskListView;
    private Spinner categorySpinner;

    private TaskAdapter taskAdapter;
    private CategoryAdapter categoryAdapter;

    private Category selectedCategory;

    private Realm realm;

    private void prepareRealm() {
        realm = Realm.getDefaultInstance();

        // 0:未分類作成
        if (realm.where(Category.class).equalTo("id", 0).findAll().size() == 0) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Category category = realm.createObject(Category.class, 0);
                    category.setName("未分類");
                }
            });
        }

        // 起動時は未分類タスクを表示
        selectedCategory = realm.where(Category.class).equalTo("id", 0).findFirst();

        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                reloadAllList();
            }
        });
    }

    private void clearRealm() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Task.class).findAll().deleteAllFromRealm();
                realm.where(Category.class).findAll().deleteAllFromRealm();
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

        taskListView = findViewById(R.id.taskListView);
        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = (Task) parent.getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());
                startActivity(intent);
            }
        });
        taskListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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

                        reloadAllList();
                    }
                });
                builder.setNegativeButton("CANCEL", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        });

        categorySpinner = findViewById(R.id.categorySpinner);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    Spinner spinner = (Spinner) parent;
                    selectedCategory = (Category) spinner.getSelectedItem();
                    reloadTaskList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        reloadAllList();
    }

    private void reloadAllList() {
        reloadTaskList();
        reloadCategoryList();
    }

    private void reloadTaskList() {
        RealmResults<Task> results = realm.where(Task.class)
                .equalTo("category.id", selectedCategory.getId())
                .findAll().sort("date", Sort.DESCENDING);
        taskAdapter.setTaskList(realm.copyFromRealm(results));
        taskListView.setAdapter(taskAdapter);
        taskAdapter.notifyDataSetChanged();
    }

    private void reloadCategoryList() {
        RealmResults<Category> results = realm.where(Category.class)
                .findAll().sort("name", Sort.DESCENDING);
        categoryAdapter.setCategories(realm.copyFromRealm(results));
        categorySpinner.setAdapter(categoryAdapter);
        categoryAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
