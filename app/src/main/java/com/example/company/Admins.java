package com.example.company;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tostas.Tostas;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Admins extends AppCompatActivity {
    Context ctx = Admins.this;
    Utils utils = new Utils();
    CheckInternetConnection checkInternetConnection = new CheckInternetConnection(ctx);
    private ListView lst_sold;
    private TextView txtTotal, txtProfit;
    private Button btninsert;
    private ScrollView scroll;
    FirebaseDatabase mDataBase = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admins);
        getSupportActionBar().setTitle("Administração");

        checkInternetConnection.execute();
        lst_sold = findViewById(R.id.list_admin);
        txtTotal = findViewById(R.id.txt_solds_admin);
        btninsert = findViewById(R.id.btn_insertnewraffle_admin);
        txtProfit = findViewById(R.id.txt_profit);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        btninsert.setEnabled(false);
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

                //region adapter
                ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, soldnumbers);
                lst_sold.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                txtTotal.setText("Total números vendidos: " + soldnumbers.size());
                //endregion

                btninsert.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog alertDialog = new AlertDialog.Builder(Admins.this).create();
                        final EditText input = new EditText(getApplicationContext());

                        alertDialog.setTitle("Confirmar");
                        alertDialog.setMessage("Digite 1234");
                        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_NUMBER);
                        alertDialog.setView(input);
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirmar",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        String auth = "1234";
                                        if (input.getText().toString().equals(auth)) {
                                            //Preparing views
                                            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                            View layout = inflater.inflate(R.layout.dialog_new_raffle, (ViewGroup) findViewById(R.id.custom_toast_layout));
                                            final EditText ValueBlock = (EditText) layout.findViewById(R.id.valueofblock);
                                            final EditText ValueRaffle = (EditText) layout.findViewById(R.id.valueofsingleraffle);

                                            //Building dialog
                                            AlertDialog.Builder builder = new AlertDialog.Builder(Admins.this);
                                            builder.setView(layout);
                                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                        if (ValueBlock.getText().toString().matches("") || ValueRaffle.getText().toString().matches("")) {
                                                        Tostas.error(Admins.this, "Preencher todos os campos", Toast.LENGTH_SHORT);
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
                                                        Tostas.success(Admins.this, "Novo Sorteio Adicionado", Toast.LENGTH_LONG);

                                                    }
                                                }
                                            });


                                            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            AlertDialog dialog_ = builder.create();
                                            dialog_.show();

                                        } else {
                                            Tostas.error(Admins.this, "Errado", Toast.LENGTH_LONG);
                                        }
                                    }
                                });
                        alertDialog.show();

                    }
                });

                //get values of block and raffle
                DatabaseReference ValueRaffle = mDataBase.getReference("RaffleValue");
                ValueRaffle.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<Object> rafflevalue = new ArrayList<>();
                        for (DataSnapshot ds: snapshot.getChildren()
                             ) {
                            rafflevalue.add(ds.getValue());
                        }

                        float valueraffle_c = Float.parseFloat(String.valueOf(rafflevalue.get(0)));
                        txtProfit.setText("Total: " + valueraffle_c*soldnumbers.size() + " Euros");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        scroll = findViewById(R.id.parent_scroll);
        scroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                lst_sold.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });

        lst_sold.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

}