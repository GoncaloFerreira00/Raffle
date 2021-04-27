package com.example.company;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Register extends AppCompatActivity {

    private Button btnRegist;
    private EditText editName;
    MyBD bd = new MyBD(Register.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editName = findViewById(R.id.edit_name_register);
        btnRegist = findViewById(R.id.btn_regist_register);
    }

    @Override
    protected void onStart() {
        super.onStart();

        btnRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bd.createUser(editName.getText().toString());
                Intent it = new Intent(Register.this, MainActivity.class);
                startActivity(it);
                finish();
            }
        });
    }
}