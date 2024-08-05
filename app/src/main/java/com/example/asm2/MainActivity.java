package com.example.asm2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText edtId, edtName, edtAmount;
    Button btnInsert, btnDelete, btnQuery, btnUpdate, btnUploadImage;
    ListView lv;
    ArrayList<String> myList;
    ArrayAdapter<String> myAdapter;
    SQLiteDatabase db;
    ImageView imageView;

    private static final int PICK_IMAGE_REQUEST = 1; // Request code for picking an image
    private Uri imageUri; // Uri for the selected image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        edtId = findViewById(R.id.edtId);
        edtName = findViewById(R.id.edtName);
        edtAmount = findViewById(R.id.edtAmount);
        btnInsert = findViewById(R.id.btnInsert);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnQuery = findViewById(R.id.btnQuery);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        imageView = findViewById(R.id.imageView);

        lv = findViewById(R.id.lv);
        myList = new ArrayList<>();
        myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, myList);
        lv.setAdapter(myAdapter);

        // Create and use SQLite
        db = openOrCreateDatabase("cem.db", MODE_PRIVATE, null);
        // Create Table
        try {
            String sql = "CREATE TABLE cem(Id TEXT primary key, Name TEXT, Amount INTEGER)";
            db.execSQL(sql);
        } catch (Exception e) {
            Log.e("Error", "Table already exists");
        }

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = edtId.getText().toString();
                String name = edtName.getText().toString();
                int amount = Integer.parseInt(edtAmount.getText().toString());
                // Inserting data into the database
                ContentValues myValues = new ContentValues();
                myValues.put("Id", id);
                myValues.put("Name", name);
                myValues.put("Amount", amount);
                String msg = "";
                if (db.insert("cem", null, myValues) == -1) {
                    msg = "Fail to Insert Record";
                } else {
                    msg = "Insert record Successfully";
                }
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                long result = db.insert("cem", null, myValues);
                if (result == -1) {
                    msg = "Fail to Insert Record";
                } else {
                    msg = "Insert record Successfully";
                }
                Log.d("Database", "Insert result: " + result);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = edtId.getText().toString(); // Get the ID to delete
                int n = db.delete("cem", "Id =?", new String[]{id});
                String msg = "";
                if (n == 0) {
                    msg = "No record to Delete";
                } else {
                    msg = n + " record(s) deleted";
                }
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int amount = Integer.parseInt(edtAmount.getText().toString());
                String id = edtId.getText().toString();
                ContentValues myValues = new ContentValues();
                myValues.put("Amount", amount);
                int n = db.update("cem", myValues, "Id = ?", new String[]{id});
                String msg = "";
                if (n == 0) {
                    msg = "No record to Update";
                } else {
                    msg = "Record updated";
                }
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myList.clear();
                Cursor c = db.query("cem", null, null, null, null, null, null);
                c.moveToFirst();
                String data;
                while (!c.isAfterLast()) {
                    data = c.getString(0) + " - " + c.getString(1) + " - " + c.getString(2);
                    myList.add(data);
                    c.moveToNext();
                }
                c.close();
                myAdapter.notifyDataSetChanged();
            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to pick an image from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
