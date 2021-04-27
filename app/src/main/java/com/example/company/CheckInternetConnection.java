package com.example.company;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.tostas.Tostas;

public class CheckInternetConnection extends AsyncTask<Activity, Boolean, Boolean> {
    Utils utils = new Utils();
    boolean check;
    Context ctx;

    public CheckInternetConnection(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected Boolean doInBackground(Activity... activities) {
        while (utils.isOnline(ctx)) check = true;
        check = false;

        return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (!check) {
            AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setTitle("Sem conexão");
            alertDialog.setMessage("Volte a estabelecer ligação");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Retry",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                           if(!utils.isOnline(ctx)){
                               onPostExecute(true);
                           }else {
                               Intent it = new Intent(ctx, MainActivity.class);
                               ctx.startActivity(it);
                               ((Activity) ctx).finish();
                           }

                        }
                    });
            alertDialog.show();

            super.onPostExecute(aBoolean);
        }

    }

}
