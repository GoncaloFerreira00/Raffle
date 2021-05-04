package com.example.company;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.example.tostas.Tostas;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.Executor;

public class SplashScreen extends AppCompatActivity {

    MyBD bd = new MyBD(SplashScreen.this);
    Utils utils = new Utils();
    FirebaseDatabase mDataBase = FirebaseDatabase.getInstance();

    public int id;
    public Boolean check = true;
    public Context context = SplashScreen.this;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo PromptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        FirebaseApp.initializeApp(context);
        MyTask task = new MyTask();
        task.execute();


        executor = ContextCompat.getMainExecutor(SplashScreen.this);
        biometricPrompt = new BiometricPrompt(SplashScreen.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Tostas.info(SplashScreen.this, "Impossível Entrar", Toast.LENGTH_LONG);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                maincall();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Tostas.error(SplashScreen.this, "Tente Novamente", Toast.LENGTH_LONG);
            }
        });

        PromptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authenticate")
                .setNegativeButtonText("Cancel")
                .setConfirmationRequired(false)
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public class MyTask extends AsyncTask<Activity, Boolean, Boolean> {

        @Override
        protected Boolean doInBackground(Activity... activities) {
            while (!utils.isOnline(SplashScreen.this)) check = false;
            check = true;

            return null;
        }

        @Override
        protected void onPreExecute() {
            if(!utils.isOnline(SplashScreen.this)){
                Tostas.error(SplashScreen.this, "Sem ligação", Toast.LENGTH_LONG);
            }
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            buttonAuthenticate();

            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            super.onProgressUpdate(values);
        }
    }

    public void maincall(){
        if(check){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final int id_ = bd.getUserId(id);
                    if(id_ == 1){
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Intent intent = new Intent(SplashScreen.this, Register.class);
                        startActivity(intent);
                    }

                }
            }, 1000);

        }else{
            Tostas.error(context, "Estabelecer Conexão", Toast.LENGTH_LONG);
        }
    }

    public void buttonAuthenticate(){
        BiometricManager biometricManager = BiometricManager.from(this);
        if(biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS){
            Tostas.warn(SplashScreen.this, "Sem Impressão Digital", Toast.LENGTH_LONG);
            return;
        }
        biometricPrompt.authenticate(PromptInfo);
    }

}