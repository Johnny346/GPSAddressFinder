package com.example.gpsaddressfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

/**
 * Report Activity here is the code for reports of running activitys to be shown
 * */
public class Reports extends AppCompatActivity {
    /**
     * The database instance is used here to get all the latest activitys
     * */
    DBHelper mydb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        mydb = new DBHelper(this);
        /**
         * Display to the listview all the previous activitys generated
         * */
        final ListView obj;
        ArrayList array_list = mydb.getAllShoppingList();
        obj  = findViewById(R.id.list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array_list);
        obj.setAdapter(arrayAdapter);

    }
}
