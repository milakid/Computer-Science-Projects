package com.milesyedl.clipboardapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText input_text;
    TextView txt_data;
    ClipboardManager clipboardManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        input_text = (EditText)findViewById(R.id.txt_data);
        txt_data = (TextView) findViewById(R.id.txtData2);

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

    }

    public void btnCopy(View view) {
        String data = input_text.getText().toString().trim();

        if(!data.isEmpty()){
            ClipData clipData = ClipData.newPlainText("text",data);
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(this, "Copied to Clipboard !", Toast.LENGTH_SHORT).show();
        }


    }

    public void btnPaste(View view) {

        ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
        String pasteData = item.getText().toString();

        if(!pasteData.equals("")){

            txt_data.setText(pasteData);
        }
        else{
            Toast.makeText(this, "Clipboard empty", Toast.LENGTH_SHORT).show();
        }


    }
}
