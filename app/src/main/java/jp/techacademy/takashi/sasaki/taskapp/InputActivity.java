package jp.techacademy.takashi.sasaki.taskapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

import static jp.techacademy.takashi.sasaki.taskapp.MainActivity.EXTRA_TASK;

public class InputActivity extends AppCompatActivity {

    private int year, month, day, hour, minute;
    private Button dateButton, timeButton;
    private EditText titleEditText, contentEditText;
    private Spinner categorySpinner;

    private Task task;
    private Category selectedCategory;
    private CategoryAdapter categoryAdapter;

    private int newCategoryId;

    private Realm realm;

    private RealmChangeListener<Realm> realmChangeListener = new RealmChangeListener<Realm>() {
        @Override
        public void onChange(Realm realm) {
            List<Category> categories = getAllCategories();
            categoryAdapter.setCategories(categories);
            categorySpinner.setAdapter(categoryAdapter);
            categoryAdapter.notifyDataSetChanged();
        }
    };

    private View.OnClickListener onDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    InputActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int y, int m, int d) {
                            year = y;
                            month = m;
                            day = d;
                            dateButton.setText(year + "/" + String.format("%02d", (month + 1)) + "/" + String.format("%02d", day));
                        }
                    }, year, month, day
            );
            datePickerDialog.show();
        }
    };

    private View.OnClickListener onTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    InputActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int h, int m) {
                            hour = h;
                            minute = m;
                            timeButton.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute));
                        }
                    }, hour, minute, false
            );
            timePickerDialog.show();
        }
    };

    private View.OnClickListener onDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addTask();
            finish();
        }
    };

    private View.OnClickListener onAddCategoryClickLister = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(InputActivity.this, CategoryInputActivity.class);
            startActivityForResult(intent, 101);
        }
    };

    private AdapterView.OnItemSelectedListener onCategorySelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Spinner spinner = (Spinner) parent;
            selectedCategory = (Category) spinner.getSelectedItem();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                newCategoryId = data.getIntExtra("categoryId", -1);
                Log.d("TaskApp", "newCategoryId:" + newCategoryId);
                categorySpinner.setOnItemSelectedListener(null);
                List<Category> categories = categoryAdapter.getCategories();
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getId() == newCategoryId) {
                        categorySpinner.setSelection(i, false);
                        break;
                    }
                }
                categorySpinner.setOnItemSelectedListener(onCategorySelectedListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dateButton = findViewById(R.id.date_button);
        dateButton.setOnClickListener(onDateClickListener);
        timeButton = findViewById(R.id.times_button);
        timeButton.setOnClickListener(onTimeClickListener);
        findViewById(R.id.done_button).setOnClickListener(onDoneClickListener);
        findViewById(R.id.addCategoryButton).setOnClickListener(onAddCategoryClickLister);
        titleEditText = findViewById(R.id.title_edit_text);
        contentEditText = findViewById(R.id.content_edit_text);
        categorySpinner = findViewById(R.id.categorySpinner);

        Intent intent = getIntent();
        int taskId = intent.getIntExtra(EXTRA_TASK, -1);

        realm = Realm.getDefaultInstance();
        realm.addChangeListener(realmChangeListener);

        task = realm.where(Task.class).equalTo("id", taskId).findFirst();
        selectedCategory = realm.where(Category.class).equalTo("id", 0).findFirst();

        if (task == null) {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        } else {
            titleEditText.setText(task.getTitle());
            contentEditText.setText(task.getContents());
            selectedCategory = task.getCategory();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(task.getDate());
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);

            dateButton.setText(year + "/" + String.format("%02d", (month + 1)) + "/" + String.format("%02d", day));
            timeButton.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute));
        }

        categoryAdapter = new CategoryAdapter(InputActivity.this);
        List<Category> categories = getAllCategories();
        categoryAdapter.setCategories(categories);
        categorySpinner.setAdapter(categoryAdapter);
        categoryAdapter.notifyDataSetChanged();
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == selectedCategory.getId()) {
                categorySpinner.setSelection(i, false);
                break;
            }
        }
        categorySpinner.setOnItemSelectedListener(onCategorySelectedListener);
    }

    private List<Category> getAllCategories() {
        RealmResults<Category> results = realm.where(Category.class).findAll().sort("name", Sort.DESCENDING);
        return realm.copyFromRealm(results);
    }

    private void reloadCategoryList() {
    }

    private void addTask() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        if (task == null) {
            task = new Task();
            RealmResults<Task> taskRealmResults = realm.where(Task.class).findAll();
            int identifier;
            if (taskRealmResults.max("id") != null) {
                identifier = taskRealmResults.max("id").intValue() + 1;
            } else {
                identifier = 0;
            }
            task.setId(identifier);
        }

        GregorianCalendar calendar = new GregorianCalendar(year, month, day, hour, minute);
        task.setTitle(titleEditText.getText().toString());
        task.setContents(contentEditText.getText().toString());
        task.setDate(calendar.getTime());
        task.setCategory(realm.where(Category.class).equalTo("id", selectedCategory.getId()).findFirst());

        realm.copyToRealmOrUpdate(task);
        realm.commitTransaction();
        realm.close();

        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
        resultIntent.putExtra(MainActivity.EXTRA_TASK, task.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                task.getId(),
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
