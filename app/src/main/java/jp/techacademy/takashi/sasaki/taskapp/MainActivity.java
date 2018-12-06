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
import android.widget.ListView;
import android.widget.Spinner;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_TASK = "jp.techacademy.takashi.sasaki.taskapp.TASK";
    public final static String EXTRA_CATEGORY = "jp.techacademy.takashi.sasaki.taskapp.CATEGORY";

    private ListView taskListView;
    private Spinner categorySpinner;

    private TaskAdapter taskAdapter;
    private CategoryAdapter categoryAdapter;

    private Category selectedCategory;

    private int categorySelection = -1;

    private Realm realm;

    private AdapterView.OnItemSelectedListener onCategorySelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.d("TaskApp", ":: onCategorySelectedListener#onItemSelected :::::::::::::::");
            if (view != null) {
                selectedCategory = (Category) ((Spinner) parent).getSelectedItem();
                categorySelection = position;
                Log.d("TaskApp", "category selection:" + categorySelection);
                Log.d("TaskApp", "selected category:" + selectedCategory.getId() + " " + selectedCategory.getName());

                taskAdapter.setTasks(getTasks());
                taskListView.setAdapter(taskAdapter);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TaskApp", ":: MainActivity#onCreate :::::::::::::::::::::::::::::::::::");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TaskInputActivity.class);
                intent.putExtra(EXTRA_CATEGORY, selectedCategory.getId());
                startActivity(intent);
            }
        });

        realm = Realm.getDefaultInstance();

        taskAdapter = new TaskAdapter(MainActivity.this);
        categoryAdapter = new CategoryAdapter(MainActivity.this);

        taskListView = findViewById(R.id.taskListView);
        taskListView.setEmptyView(findViewById(R.id.emptyTextView));
        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = (Task) parent.getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, TaskInputActivity.class);
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

                        taskAdapter.setTasks(getTasks());
                        taskListView.setAdapter(taskAdapter);
                        taskAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("CANCEL", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        });

        categorySpinner = findViewById(R.id.categorySpinner);
    }

    @Override
    protected void onResume() {
        Log.d("TaskApp", ":: MainActivity#onResume :::::::::::::::::::::::::::::::::::");
        super.onResume();

        if (selectedCategory == null) {
            selectedCategory = realm.where(Category.class).equalTo("id", 0).findFirst();
        }
        Log.d("TaskApp", "category selection:" + categorySelection);
        Log.d("TaskApp", "selected category:" + selectedCategory.getId() + " " + selectedCategory.getName());

        categoryAdapter.setCategories(getCategories());
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(null);
        categorySpinner.setSelection(categoryAdapter.getSelection(selectedCategory.getId()), false);
        categorySpinner.setOnItemSelectedListener(onCategorySelectedListener);
        categoryAdapter.notifyDataSetChanged();

        taskAdapter.setTasks(getTasks());
        taskListView.setAdapter(taskAdapter);
        taskAdapter.notifyDataSetChanged();
    }

    private List<Category> getCategories() {
        RealmResults<Category> results = realm.where(Category.class)
                .findAll().sort("name", Sort.DESCENDING);
        return realm.copyFromRealm(results);
    }

    private List<Task> getTasks() {
        RealmResults<Task> results = realm.where(Task.class)
                .equalTo("category.id", selectedCategory.getId())
                .findAll().sort("date", Sort.DESCENDING);
        return realm.copyFromRealm(results);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
