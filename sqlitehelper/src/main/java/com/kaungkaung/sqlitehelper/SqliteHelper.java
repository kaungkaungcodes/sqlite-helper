package com.kaungkaung.sqlitehelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SqliteHelper extends SQLiteOpenHelper {

    private final String createTableQuery;
    private final String tableName;

    public SqliteHelper(Context context, String table, String createTableQuery) {
        super(context, "KKSQLite.db", null, 1); // Incremented version
        this.createTableQuery = createTableQuery;
        this.tableName = table;
        createTableIfNeeded();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(createTableQuery);
            Log.d("DatabaseHelper", "Table created: " + tableName);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating table: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (tableExists(db, tableName)) {
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            onCreate(db);
        }
    }

    private boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private void createTableIfNeeded() {
        SQLiteDatabase db = this.getWritableDatabase();
        if (!tableExists(db, tableName)) {
            db.execSQL(createTableQuery);
        }
    }

    public long insertData(HashMap<String, Object> data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // Convert HashMap to ContentValues
        for (String key : data.keySet()) {
            Object value = data.get(key);
            if (value instanceof Integer) {
                contentValues.put(key, (Integer) value);
            } else if (value instanceof Float) {
                contentValues.put(key, (Float) value);
            } else if (value instanceof String) {
                contentValues.put(key, (String) value);
            } else if (value instanceof byte[]) {
                contentValues.put(key, (byte[]) value);
            } else {
                Log.e("DatabaseHelper", "Unsupported data type for key: " + key);
                return -1;
            }
        }

        long result = db.insert(tableName, null, contentValues);
        if (result == -1) {
            Log.e("DatabaseHelper", "Error inserting data.");
        } else {
            Log.d("DatabaseHelper", "Data inserted with id: " + result);
        }
        return result;
    }

    public List<HashMap<String, Object>> getAllData(String[] columns, boolean isDecending) {
        List<HashMap<String, Object>> dataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        if (tableExists(db, tableName)) {
            String sortOrder = isDecending ? "DESC" : "ASC";
            Cursor cursor = db.query(tableName, columns, null, null, null, null, "id " + sortOrder);

            while (cursor.moveToNext()) {
                HashMap<String, Object> dataMap = new HashMap<>();
                for (String column : columns) {
                    int columnIndex = cursor.getColumnIndex(column);
                    if (columnIndex != -1) {
                        switch (cursor.getType(columnIndex)) {
                            case Cursor.FIELD_TYPE_INTEGER:
                                dataMap.put(column, cursor.getInt(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                dataMap.put(column, cursor.getFloat(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                dataMap.put(column, cursor.getString(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_BLOB:
                                dataMap.put(column, cursor.getBlob(columnIndex));
                                break;
                        }
                    }
                }
                dataList.add(dataMap);
            }
            cursor.close();
        }
        return dataList;
    }

    public HashMap<String, Object> getData(int id, String[] columns) {
        HashMap<String, Object> dataMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        if (tableExists(db, tableName)) {
            Cursor cursor = db.query(tableName, columns, "id=?", new String[]{String.valueOf(id)}, null, null, null);

            if (cursor.moveToFirst()) {
                for (String column : columns) {
                    int columnIndex = cursor.getColumnIndex(column);
                    if (columnIndex != -1) {
                        switch (cursor.getType(columnIndex)) {
                            case Cursor.FIELD_TYPE_INTEGER:
                                dataMap.put(column, cursor.getInt(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                dataMap.put(column, cursor.getFloat(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                dataMap.put(column, cursor.getString(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_BLOB:
                                dataMap.put(column, cursor.getBlob(columnIndex));
                                break;
                        }
                    }
                }
            }
            cursor.close();
        }
        return dataMap;
    }

    public int getNextId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(id) FROM " + tableName, null);
        int nextId = 1; // Default to 1 if table is empty

        if (cursor.moveToFirst()) {
            int maxId = cursor.getInt(0);
            nextId = maxId + 1;
        }
        cursor.close();
        return nextId;
    }

    public List<HashMap<String, Object>> search(String column, String text, String orderBy, Boolean isDecending) {
        List<HashMap<String, Object>> resultList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        if (tableExists(db, tableName)) {
            String sortOrder = isDecending ? "DESC" : "ASC";
            String query = "SELECT * FROM " + tableName + " WHERE " + column + " LIKE ? ORDER BY " + orderBy + " " + sortOrder;
            Cursor cursor = db.rawQuery(query, new String[]{"%" + text + "%"});

            // Get column names
            String[] columns = cursor.getColumnNames();
            while (cursor.moveToNext()) {
                HashMap<String, Object> dataMap = new HashMap<>();
                for (String col : columns) {
                    int columnIndex = cursor.getColumnIndex(col);
                    if (columnIndex != -1) {
                        switch (cursor.getType(columnIndex)) {
                            case Cursor.FIELD_TYPE_INTEGER:
                                dataMap.put(col, cursor.getInt(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                dataMap.put(col, cursor.getFloat(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                dataMap.put(col, cursor.getString(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_BLOB:
                                dataMap.put(col, cursor.getBlob(columnIndex));
                                break;
                        }
                    }
                }
                resultList.add(dataMap);
            }
            cursor.close();
        }
        return resultList;
    }
}
