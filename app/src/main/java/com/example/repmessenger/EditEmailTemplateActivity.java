package com.example.repmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class EditEmailTemplateActivity extends AppCompatActivity {
    private TextView tv_templateName;
    private EditText et_subject, et_message, et_signature;
    private File file;
    private String folderPath;
    private String fileName, fileContents;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_email_template);

        tv_templateName = findViewById(R.id.textView_template_name);
        et_subject = findViewById(R.id.editText_subject);
        et_message = findViewById(R.id.editText_message);
        et_signature = findViewById(R.id.editText_signature);

        fileName = getIntent().getStringExtra(ManageTemplatesActivity.EXTRA_TEMPLATE_NAME);
        position = getIntent().getIntExtra(ManageTemplatesActivity.EXTRA_TEMPLATE_POSITION, 0);
        tv_templateName.setText(fileName);
        fileName = fileName + ".txt";

        //load saved file information into editText fields
        byte[] bytes = new byte[1024];
        folderPath = EditEmailTemplateActivity.this.getFilesDir().getAbsolutePath() + File.separator + "emailTemplates";
        file = new File(folderPath, fileName);

        try {
            //read file contents into byte[]
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.read(bytes);
            inputStream.close();

            //remove empty elements from byte array
            int i;
            for (i = 0; i < bytes.length && bytes[i] != 0; i++) { }
            String string = new String(bytes, 0, i, Charset.defaultCharset());

            //split contents into email subject and message by deliminator "||"
            String[] fileContents = string.split("\\|\\|");
            int length = fileContents.length;
            et_subject.setText(length != 0 ? fileContents[0] : "");
            et_message.setText(length > 1 ? fileContents[1] : "");
            et_signature.setText(length > 2 ? fileContents[2] : "");


        } catch (FileNotFoundException e){
            Log.e("EMAIL_ERROR", e.toString());
        } catch (IOException e){
            Log.e("EMAIL_ERROR", e.toString());
        } catch (NullPointerException e){
            Log.e("EMAIL_ERROR", e.toString());
        }
    }

    public void saveEmail(View view) {
        //put together contents with "||" as a delimiter
        String signature = et_signature.getText().toString();
        String message = et_message.getText().toString();
        if (TextUtils.isEmpty(signature)){
            fileContents = et_subject.getText().toString() + "||" + message;
        } else {
            fileContents = et_subject.getText().toString() + "||" + message + "||" + signature;
        }
        Log.i("Email", fileContents);

        //save new file
        try {
            file.createNewFile();
        } catch (IOException e){
            Toast.makeText(EditEmailTemplateActivity.this, "File " + fileName + " creation failed", Toast.LENGTH_SHORT).show();
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(fileContents.getBytes(Charset.forName("UTF-8")));
            fileOutputStream.close();

            //send edited template message back to ManageTemplatesActivity
            Intent replyIntent = new Intent();
            replyIntent.putExtra(ManageTemplatesActivity.EXTRA_TEMPLATE_MESSAGE, message);
            replyIntent.putExtra(ManageTemplatesActivity.EXTRA_TEMPLATE_POSITION, position);
            setResult(RESULT_OK, replyIntent);
            finish();

        } catch(IOException e){
            Toast.makeText(EditEmailTemplateActivity.this, "File " + fileName + " write failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelEmail(View view) {
        //send result that template edit was canceled
        Intent replyIntent = new Intent();
        setResult(RESULT_CANCELED, replyIntent);
        finish();
    }
}