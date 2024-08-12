package com.kaungkaung.sqlite;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kaungkaung.sqlitehelper.SqliteHelper;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {
    EditText edtName, edtAddress;
    Button btnAdd;
    ListView lv;
    SqliteHelper Friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtName = findViewById(R.id.edt_name);
        edtAddress  = findViewById(R.id.edt_address);
        btnAdd = findViewById(R.id.btn_add);
        lv = findViewById(R.id.lv);

        String qurey = "CREATE TABLE IF NOT EXISTS Friend (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, address TEXT, phone TEXT);";
         Friend = new SqliteHelper(this, "Friend", qurey);

        loadData();

        btnAdd.setOnClickListener( (v -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("name", edtName.getText().toString());
            data.put("address", edtAddress.getText().toString());
            long result = Friend.insertData(data);
            Toast.makeText(this, "" + result, Toast.LENGTH_SHORT).show();
            clearText();
            loadData();
        }));
    }

    private void loadData() {
        String[] columns = {"id", "name", "address"};
        List<HashMap<String, Object>> listData = Friend.getAllData(columns, true);
        lv.setAdapter(new MyAdaper(MainActivity.this, listData));
    }

    private void clearText() {
        edtName.setText("");
        edtAddress.setText("");
    }

    class MyAdaper extends BaseAdapter {
        Context context;
        List<HashMap<String, Object>> listData;

        public MyAdaper(Context context, List<HashMap<String, Object>> listData){
            this.context = context;
            this.listData = listData;
        }
        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
            }

            final TextView name = convertView.findViewById(R.id.name);
            final TextView address = convertView.findViewById(R.id.address);

            HashMap<String, Object> data = listData.get(position);
            name.setText(data.get("name").toString());
            address.setText(data.get("address").toString());

            return convertView;
        }
    }

}