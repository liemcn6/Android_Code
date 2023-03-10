package com.example.dictionary;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<English> listEnglish;
    English_Adapter adapter;
    Map<String, String> mapEnglish;
    FloatingActionButton actionButton;
    Database database;
    EditText edtSearch;
    ImageButton imgSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Anhxa();
        createDatabase();
        adapter = new English_Adapter(this, R.layout.line_english, listEnglish);
        readData();
        listView.setAdapter(adapter);
        Click();
        Search();
    }

    private void createDatabase() {
        database = new Database(this, "Dictionary.sqlite", null, 1);
        String table = "CREATE TABLE IF NOT EXISTS TuDien(" +
                       "Vocabulary VARCHAR(200) PRIMARY KEY," +
                       "wordMean VARCHAR(200))";
        database.QueryData(table);

    }

    private void openDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add);
        dialog.setCanceledOnTouchOutside(false);

        final EditText edtTuVung = dialog.findViewById(R.id.dialog_addTu);
        final EditText edtNghia = dialog.findViewById(R.id.dialog_addNghia);
        Button btnHuy = dialog.findViewById(R.id.dialog_huy);
        Button btnThem = dialog.findViewById(R.id.dialog_them);

        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearch.setHint("Nh???p t??? c???n t??m...");
                String tu = edtTuVung.getText().toString();
                String nghia = edtNghia.getText().toString();
                if(tu.equals("")|| nghia.equals("")){
                    Toast.makeText(MainActivity.this, "Kh??ng ???????c ????? tr???ng!", Toast.LENGTH_SHORT).show();
                }else{
                    if(mapEnglish.containsKey(tu) == false){
                        mapEnglish.put(tu,nghia);
                        database.QueryData("INSERT INTO TuDien VALUES('"+ tu + "','" + nghia + "')");
                        Toast.makeText(MainActivity.this, "Th??m th??nh c??ng!", Toast.LENGTH_SHORT).show();
                        Cursor data = database.getData("SELECT * FROM TuDien");
                        listEnglish.clear(); // x??a h???t ?????c t??? ?????u
                        while(data.moveToNext()){
                            String vocabulary = data.getString(0);
                            String wordMean = data.getString(1);
                            listEnglish.add(new English(vocabulary,wordMean));
                        }
                        Collections.sort(listEnglish, new sortVocabulary());
                        adapter.notifyDataSetChanged();
                        listView.setAdapter(adapter);
                    }else{
                        Toast.makeText(MainActivity.this, "T??? ???? t???n t???i", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void readData() {
        Cursor data = database.getData("SELECT * FROM TuDien");
        if(data.getCount() != 0){
            listEnglish.clear(); // x??a h???t ?????c t??? ?????u
            while(data.moveToNext()){
                String nghiacuatu = data.getString(1);
                String tuvung = data.getString(0);
                listEnglish.add(new English(tuvung,nghiacuatu));
                mapEnglish.put(tuvung,nghiacuatu);
            }
            Collections.sort(listEnglish, new sortVocabulary());
            //adapter.notifyDataSetChanged();
            // D??ng n??y b??? x??a do m???i ?????u khi kh???i ?????ng m??y ch??? ?????c list v??o v?? ch??a t???n t???i adaptor n??n ko c???n notifysetChange

        }else{
            data.close();
        }
    }

    private void Anhxa() {
        listView = findViewById(R.id.listView);
        listEnglish = new ArrayList<>();
        mapEnglish = new HashMap<>();
        actionButton = findViewById(R.id.floatingActionButton);
        imgSearch = findViewById(R.id.buttonSearch);
        edtSearch = findViewById(R.id.search);
    }

    private void Click(){
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tu = edtSearch.getText().toString();
                String nghia = "";
                if(mapEnglish.containsKey(tu)){
                    nghia = mapEnglish.get(tu);
                    ArrayList<English> englishes = new ArrayList<>();
                    englishes.add(new English(tu,nghia));
                    English_Adapter adapterSearch = new English_Adapter(MainActivity.this, R.layout.line_english,englishes);
                    listView.setAdapter(adapterSearch);
                }else{
                    Toast.makeText(MainActivity.this, "Kh??ng c?? t??? n??y", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void Search(){
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.equals("")){
                    adapter = new English_Adapter(MainActivity.this, R.layout.line_english,listEnglish);
                    listView.setAdapter(adapter);
                }
                else{

                    // Bi???n ?????i chu???i vi???t hoa ch??? c??i ?????u
                    String str = s.toString();
                    // T??m ki???m g???n ????ng
                    String regex = ".*" + str; // t??m t??? b???t ?????u b???ng str
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher;
                    ArrayList<English> newList = new ArrayList<>();
                    for(int i = 0; i < listEnglish.size(); i++){
                        matcher = pattern.matcher(listEnglish.get(i).getVocabulary());
                        if(matcher.find()){
                            newList.add(listEnglish.get(i));

                        }
                    }
                    English_Adapter adapterNew = new English_Adapter(MainActivity.this,R.layout.line_english,newList);
                    adapterNew.notifyDataSetChanged();
                    listView.setAdapter(adapterNew);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void Delete(final String Vocabulary){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Th??ng b??o!");
        alert.setMessage("X??c nh???n x??a " + Vocabulary);
        alert.setPositiveButton("C??", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                database.QueryData("DELETE FROM TuDien WHERE Vocabulary = '" + Vocabulary + "'");
                Toast.makeText(MainActivity.this, "Delete th??nh c??ng", Toast.LENGTH_SHORT).show();
                Cursor data = database.getData("SELECT * FROM TuDien");
                listEnglish.clear(); // x??a h???t ?????c t??? ?????u
                mapEnglish.clear();
                while(data.moveToNext()){
                    String vocabulary = data.getString(0);
                    String wordMean = data.getString(1);
                    listEnglish.add(new English(vocabulary,wordMean));
                    mapEnglish.put(vocabulary,wordMean);
                }
                Collections.sort(listEnglish, new sortVocabulary());
                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
            }
        });
        alert.setNegativeButton("Kh??ng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alert.show();
    }
}
