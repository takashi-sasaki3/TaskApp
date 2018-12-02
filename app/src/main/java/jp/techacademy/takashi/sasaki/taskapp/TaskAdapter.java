package jp.techacademy.takashi.sasaki.taskapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;

    private List<Task> taskList;

    public TaskAdapter(Context context) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null);
        }

        TextView textView1 = convertView.findViewById(android.R.id.text1);
        TextView textView2 = convertView.findViewById(android.R.id.text2);

        textView1.setText(taskList.get(position).getTitle());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE);
        Date date = taskList.get(position).getDate();
        textView2.setText(simpleDateFormat.format(date));

        return convertView;
    }
}
