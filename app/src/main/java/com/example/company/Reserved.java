package com.example.company;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tostas.Tostas;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Reserved extends AppCompatActivity {

    FirebaseDatabase mDataBase = FirebaseDatabase.getInstance();

    private ListView lst_oldRaffles;
    private Button btn_insert, btn_OldRaffles;
    private TextView txtMoney, txtDebt, txtUpdate;
    boolean lstVisible = true;
    Context ctx = Reserved.this;
    MyBD bd = new MyBD(ctx);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserved);

        btn_insert = findViewById(R.id.btn_insertnewraffle_reserved);
        lst_oldRaffles = findViewById(R.id.lst_oldRaffles);
        btn_OldRaffles = findViewById(R.id.btn_oldRaffles);

    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference getMoneyDebt = mDataBase.getReference("Debts");
        getMoneyDebt.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Object> keys = new ArrayList<>();
                ArrayList<Object> values = new ArrayList<>();

                for (DataSnapshot ds: snapshot.getChildren()) {
                    keys.add(ds.getKey());
                    values.add(ds.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        final DatabaseReference getSoldNumbers = mDataBase.getReference("Sold Numbers");
        getSoldNumbers.addValueEventListener(new ValueEventListener() {
            ArrayList<Object> soldnumbers = new ArrayList<>();

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                soldnumbers.clear();
                for (DataSnapshot ds : snapshot.getChildren()
                ) {
                    soldnumbers.add(ds.getValue());
                }

                btn_insert.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog alertDialog = new AlertDialog.Builder(Reserved.this).create();
                        final EditText input = new EditText(getApplicationContext());

                        alertDialog.setTitle("Confirmar");
                        alertDialog.setMessage("Digite 1234");
                        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_NUMBER);
                        alertDialog.setView(input);
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirmar",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        String auth = "1234";
                                        if (input.getText().toString().equals(auth.toString())) {
                                            //Preparing views
                                            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                            View layout = inflater.inflate(R.layout.dialog_new_raffle, (ViewGroup) findViewById(R.id.custom_toast_layout));
                                            final EditText ValueBlock = (EditText) layout.findViewById(R.id.valueofblock);
                                            final EditText ValueRaffle = (EditText) layout.findViewById(R.id.valueofsingleraffle);

                                            //Building dialog
                                            AlertDialog.Builder builder = new AlertDialog.Builder(Reserved.this);
                                            builder.setView(layout);
                                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (ValueBlock.getText().toString().matches("") || ValueRaffle.getText().toString().matches("")) {
                                                        Tostas.error(Reserved.this, "Preencher todos os campos", Toast.LENGTH_SHORT);
                                                    } else {
                                                        //insert number on sold list
                                                        final DatabaseReference myRef = mDataBase.getReference("Available Numbers");
                                                        getSoldNumbers.setValue(null);
                                                        myRef.setValue(null);

                                                        //insert 0-99 numbers to create a new raffle
                                                        for (int i = 0; i < 100; i++) {
                                                            DatabaseReference myRefput = mDataBase.getReference("Available Numbers").push();
                                                            myRefput.setValue(i);
                                                        }
                                                        //set null sold numbers value
                                                        mDataBase.getReference("Sold Numbers Value").setValue(null);
                                                        mDataBase.getReference("BlockValue").setValue(null);
                                                        mDataBase.getReference("RaffleValue").setValue(null);

                                                        //save value of block value and raffle value
                                                        mDataBase.getReference("BlockValue").push().setValue(ValueBlock.getText().toString());
                                                        mDataBase.getReference("RaffleValue").push().setValue(ValueRaffle.getText().toString());
                                                        mDataBase.getReference("OldRaffles").push().setValue(soldnumbers);
                                                        Tostas.success(Reserved.this, "Novo Sorteio Adicionado", Toast.LENGTH_LONG);

                                                    }
                                                }
                                            });
                                            builder.show();
                                        } else {
                                            Tostas.error(Reserved.this, "Errado", Toast.LENGTH_LONG);
                                        }
                                    }
                                });
                        alertDialog.show();

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btn_OldRaffles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!lstVisible) lstVisible = true;
                else lstVisible = false;

                if(lstVisible) lst_oldRaffles.setVisibility(View.VISIBLE);
                else lst_oldRaffles.setVisibility(View.GONE);

                mDataBase.getReference("OldRaffles").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<Object> rafflesKey = new ArrayList<>();
                        ArrayList<Object> rafflesvalue = new ArrayList<>();

                        ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(getApplicationContext(), R.layout.listview_items, rafflesKey);
                        lst_oldRaffles.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            rafflesKey.add(ds.getKey());
                            rafflesvalue.add(ds.getValue());
                        }

                        lst_oldRaffles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                AlertDialog alertDialog = new AlertDialog.Builder(Reserved.this).create();
                                final EditText input = new EditText(getApplicationContext());
                                alertDialog.setTitle("Números Vendidos");
                                alertDialog.setMessage(rafflesvalue.toString().replace(",", "\n\n").replace("[", "").replace("]]", "").replace(" ", ""));
                                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                alertDialog.setView(input);
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Fechar",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }

                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

}