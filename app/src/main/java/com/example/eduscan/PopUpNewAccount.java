package com.example.eduscan;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class PopUpNewAccount extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_new_account);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int heigth = dm.heightPixels;

        getWindow().setLayout((int)(width*.4), (int) (heigth*.4));
    }


}
