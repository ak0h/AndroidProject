package com.example.naval.navalmionina;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ListShow extends AppCompatActivity{
    ListView listV;
    ArrayAdapter arrayA;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "This button is just for fun actualy ;p", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // Récupération de l'intent envoyé et consultation du fichier
        Intent i = getIntent();
        String nameFic = i.getExtras().getString("ficName");
        File file = new File(getBaseContext().getFilesDir(), nameFic);
        ArrayList <String> list = new ArrayList<String>();

        try {
            BufferedReader in = new BufferedReader(new FileReader(file.getAbsolutePath()));
            String str;

            while((str = in.readLine()) != null ){
                list.add(str);
            }
            listV = (ListView) findViewById(R.id.listview);
            listV.setClickable(true);
            arrayA = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,list);
            listV.setAdapter(arrayA);
            listV.setOnItemClickListener(new ListHandler());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public class ListHandler implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String text = (String)parent.getItemAtPosition(position);

            Intent intent = new Intent(getBaseContext(), InMap.class);
            intent.putExtra("item", text);
            startActivity(intent);

        }
    }


}
