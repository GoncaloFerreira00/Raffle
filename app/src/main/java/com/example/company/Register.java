package com.example.company;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.tostas.Tostas;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Register extends AppCompatActivity {
    FirebaseDatabase mDataBase = FirebaseDatabase.getInstance();

    private Button btnRegist;
    private EditText editName;
    private Switch swt;
    MyBD bd = new MyBD(Register.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editName = findViewById(R.id.edit_name_register);
        btnRegist = findViewById(R.id.btn_regist_register);
        swt = findViewById(R.id.switch_admin);
    }

    @Override
    protected void onStart() {
        super.onStart();

        btnRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (swt.isChecked()) {
                    AlertDialog alertDialog = new AlertDialog.Builder(Register.this).create();
                    final EditText input = new EditText(getApplicationContext());

                    alertDialog.setTitle("Para ser administrador,");
                    alertDialog.setMessage("Digite a palavra-chave: ");
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    alertDialog.setView(input);
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Entrar",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ArrayList<Object> Password = new ArrayList<>();
                                    mDataBase.getReference("PassAdmin").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds: snapshot.getChildren()) {
                                                Password.add(ds.getValue());
                                            }

                                            if (Password.get(0).equals(input.getText().toString())) {
                                                bd.createUser(editName.getText().toString(), 1);
                                                Intent it = new Intent(Register.this, MainActivity.class);
                                                startActivity(it);
                                                finish();
                                            } else {
                                                swt.setChecked(false);
                                                Tostas.error(Register.this, "Errado", Toast.LENGTH_SHORT);
                                                return;
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                    alertDialog.show();
                } else {
                    bd.createUser(editName.getText().toString(),0);
                    Intent it = new Intent(Register.this, MainActivity.class);
                    startActivity(it);
                    finish();
                }
            }
        });
    }

}