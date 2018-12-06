package jp.techacademy.takashi.sasaki.taskapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CategoryAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;

    private List<Category> categories;

    public CategoryAdapter(Context context) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(android.R.layout.simple_spinner_item, null);
        }
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(categories.get(position).getName());
        return convertView;
    }

    public int getSelection(int id) {
        for (int i = 0; i < getCount(); i++) {
            if (((Category) getItem(i)).getId() == id) {
                return i;
            }
        }
        return 0;
    }
}
