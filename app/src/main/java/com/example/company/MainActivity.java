package com.example.company;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    MyBD bd = new MyBD(MainActivity.this);
    CheckInternetConnection checkInternetConnection = new CheckInternetConnection(MainActivity.this);
    Utils utils = new Utils();

    private Button btnCopy, btnAcess, btnCopySoldNum, btnReserved;
    private ImageButton btnDeleteName;
    private ScrollView scroll;
    private TextView txt;
    private EditText editname;
    private ArrayList<String> check = new ArrayList<String>();
    private static final String TAG = "Erro";
    private ListView lst;

    FirebaseDatabase mDataBase = FirebaseDatabase.getInstance();

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo PromptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkInternetConnection.execute();
        lst = findViewById(R.id.lst);
        btnCopy = findViewById(R.id.btn_copy_main);
        btnAcess = findViewById(R.id.btn_access_main);
        editname = findViewById(R.id.edit_name_main);
        btnCopySoldNum = findViewById(R.id.btn_soldNumbers);
        btnDeleteName = findViewById(R.id.button_deletename);
        btnReserved = findViewById(R.id.btn_reserved);
        txt = findViewById(R.id.txt_opcional);

        if(bd.getAdmin(10) == 1){
            btnReserved.setEnabled(true);
        } else {
            btnReserved.setEnabled(false);
            btnReserved.setVisibility(View.GONE);
        }

        btnReserved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), Reserved.class);
                startActivity(it);
            }
        });

        final DatabaseReference myRef = mDataBase.getReference("Available Numbers");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                final ArrayList<Object> num = new ArrayList<>();
                final ArrayList<String> keys = new ArrayList<>();
                final ArrayList<Object> numsIntegers = new ArrayList<>();

                //region Adapter
                ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(getApplicationContext(), R.layout.listview_items, num);
                lst.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                //endregion

                check.clear();
                for (DataSnapshot ds : snapshot.getChildren()
                ) {
                    int nume = Integer.parseInt(String.valueOf(ds.getValue()));
                    if (nume < 10) num.add("0" + ds.getValue());
                    else num.add(ds.getValue());

                    numsIntegers.add(ds.getValue());
                    keys.add(ds.getKey());
                    check.add(ds.getKey());
                }

                //region btnCopy
                btnCopy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String copy = "Números disponíveis: ";
                        for (int i = 0; i < num.size(); i++) {
                            copy += " " + num.get(i);
                        }

                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipdata = ClipData.newPlainText("label", copy);
                        clipboard.setPrimaryClip(clipdata);
                        Tostas.success(MainActivity.this, "Copiado para área de transferência", Toast.LENGTH_LONG);
                    }
                });
                //endregion

                lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {

                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("");
                        alertDialog.setMessage("Deseja vender o número " + num.get(position) + "?");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Sim",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //region Date
                                        SimpleDateFormat outputTimeFormatter = new SimpleDateFormat("hh:mm");
                                        outputTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                                        SimpleDateFormat outputDateFormatter = new SimpleDateFormat("dd/MM/yyyy");
                                        outputDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                                        Calendar calendar = Calendar.getInstance();
                                        //endregion

                                        if (check.contains(keys.get((int) id))) {
                                            String name = "";

                                            //insert number, date, name on sold list
                                            DatabaseReference myRefSold = mDataBase.getReference("Sold Numbers").push();
                                            myRefSold.setValue(bd.getUserName(name) + " N:" + num.get((int) id) + " D:" +
                                                    outputDateFormatter.format(calendar.getTime()) + " H:" + outputTimeFormatter.format(calendar.getTime()) + " | "
                                                    + editname.getText().toString());

                                            //remove number to available numbers
                                            myRef.child(keys.get((int) id)).removeValue();
                                            Tostas.success(MainActivity.this, "Vendido", Toast.LENGTH_LONG);

                                            //inserting number to sold numbers value
                                            DatabaseReference myRefSoldValue = mDataBase.getReference("Sold Numbers Value").push();
                                            myRefSoldValue.setValue(numsIntegers.get((int) id));

                                        } else {
                                            Tostas.error(MainActivity.this, "NÃO VENDIDO", Toast.LENGTH_LONG);
                                            final AlertDialog alertDialog_ = new AlertDialog.Builder(MainActivity.this).create();
                                            alertDialog_.setTitle("NÚMERO JÁ VENDIDO");
                                            alertDialog_.setCanceledOnTouchOutside(false);
                                            alertDialog_.setMessage("Não entregar rifa.");
                                            alertDialog_.setButton(AlertDialog.BUTTON_NEUTRAL, "Okay",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            alertDialog_.dismiss();
                                                        }
                                                    });

                                            MediaPlayer song = MediaPlayer.create(getApplicationContext(), R.raw.danger_alarm);
                                            song.start();
                                            alertDialog_.show();

                                        }
                                    }
                                });

                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Não",
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
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();

        final DatabaseReference getPass = mDataBase.getReference("Passe");
        getPass.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final ArrayList<Object> string = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    string.add(ds.getValue());
                }

                final Date currentTime = Calendar.getInstance().getTime();
                btnAcess.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        final EditText input = new EditText(getApplicationContext());

                        alertDialog.setTitle("Números Vendidos");
                        alertDialog.setMessage("Digite a palavra-chave: ");
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        alertDialog.setView(input);
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Entrar",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (string.get(0).equals(input.getText().toString())) {
                                            Intent it = new Intent(MainActivity.this, Admins.class);
                                            startActivity(it);
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

        DatabaseReference getSoldNumbers = mDataBase.getReference("Sold Numbers Value");
        getSoldNumbers.addValueEventListener(new ValueEventListener() {
            final ArrayList<Object> SoldNumbers = new ArrayList<>();
            final ArrayList<Integer> SoldNumbersOrder = new ArrayList<>();

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SoldNumbers.clear();
                SoldNumbersOrder.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    SoldNumbers.add(ds.getValue());
                }

                for(int x=0; x<SoldNumbers.size(); x++ ){
                    SoldNumbersOrder.add(Integer.parseInt(String.valueOf(SoldNumbers.get(x))));
                }

                Collections.sort(SoldNumbersOrder);

                btnCopySoldNum.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String SoldNums = "";
                        SoldNums += "Números Vendidos: \n";
                        for(int i=0; i<SoldNumbersOrder.size(); i++){
                            int nume = SoldNumbersOrder.get(i);
                            if(nume<10) SoldNums +=  "0" + SoldNumbersOrder.get(i) + "\n";
                            else SoldNums += SoldNumbersOrder.get(i) + "\n";
                        }

                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                        ClipData clip = ClipData.newPlainText("label", SoldNums);
                        clipboard.setPrimaryClip(clip);
                        Tostas.success(MainActivity.this, "Copiado para área de transferência", Toast.LENGTH_LONG);
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
                lst.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });

        lst.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        btnDeleteName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editname.setText("");
            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startActivity(getIntent());
        onBackPressed();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }
}