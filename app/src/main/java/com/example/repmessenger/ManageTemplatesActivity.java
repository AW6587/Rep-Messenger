package com.example.repmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

public class ManageTemplatesActivity extends AppCompatActivity {
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private boolean clicked = false;

    TemplateAdapter adapter;
    RecyclerView templateItems;
    FloatingActionButton fabNew, fabNewEmail, fabNewTweet;

    ArrayList<TemplateItems> items = new ArrayList<>();
    ArrayList<String> templateFilesE;
    ArrayList<String> templateFilesT;

    public static final int NEW_EMAIL = 1;
    public static final int NEW_TWEET = 2;
    public static final int EDIT_TEMPLATE = 3;
    public static final String EXTRA_TEMPLATE_NAME = "com.example.android.repmessenger.extra.TEMPLATE_NAME";
    public static final String EXTRA_TEMPLATE_MESSAGE = "com.example.android.repmessenger.extra.TEMPLATE_MESSAGE";
    public static final String EXTRA_TEMPLATE_POSITION = "com.example.android.repmessenger.extra.TEMPLATE_POSITION";
    public static final String EXTRA_EMAIL_TEMPLATES = "com.example.android.repmessenger.extra.EMAIL_TEMPLATES";
    public static final String EXTRA_TWITTER_TEMPLATES = "com.example.android.repmessenger.extra.TWITTER_TEMPLATES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_templates);

        // get directory to email and twitter templates
        File mainDirectory = getApplicationContext().getFilesDir();
        File emailDirectory = new File(mainDirectory, "emailTemplates");
        File twitterDirectory = new File(mainDirectory, "twitterTemplates");

        // set up list of template files
        templateFilesE = new ArrayList(Arrays.asList(emailDirectory.list()));
        templateFilesT = new ArrayList(Arrays.asList(twitterDirectory.list()));

        // iterate over email files, add templateName and message to items list
        for (int i = 0; i < templateFilesE.size(); i++){
            try {
                String templateName = (String) templateFilesE.get(i);

                byte[] bytes = new byte[1024];
                File file = new File(emailDirectory, templateName);

                //read file contents into byte[]
                FileInputStream inputStream = new FileInputStream(file);
                inputStream.read(bytes);
                inputStream.close();

                //remove empty elements from byte array
                int j;
                for (j = 0; j < bytes.length && bytes[j] != 0; j++){}
                String string = new String(bytes, 0, j, Charset.defaultCharset());

                //split email contents by deliminator "||" and get just the message
                //format: subject||message||signature
                String[] fileContents = string.split("\\|\\|");

                //make sure file has a message saved
                String message = null;
                if( fileContents.length > 1){
                    message = truncateMessage(fileContents[1]);
                }

                //remove .txt from templateName
                templateName = removeExtension(templateName);
                templateFilesE.set(i, templateName);

                //add email template to items list
                items.add(new TemplateItems("email", templateName, message));
            }
            catch (FileNotFoundException e){
                Log.e("EMAIL_ERROR", e.toString());
            } catch (IOException e){
                Log.e("EMAIL_ERROR", e.toString());
            } catch (NullPointerException e) {
                Log.e("EMAIL_ERROR", e.toString());
            }
        }

        // iterate over twitter files, add templateName and message to items list
        for (int i = 0; i < templateFilesT.size(); i++){
            try {
                String templateName = (String) templateFilesT.get(i);

                byte[] bytes = new byte[1024];
                File file = new File(twitterDirectory, templateName);

                //read file contents into byte[]
                FileInputStream inputStream = new FileInputStream(file);
                inputStream.read(bytes);
                inputStream.close();

                //remove empty elements from byte array
                int j;
                for (j = 0; j < bytes.length && bytes[j] != 0; j++){}
                String string = new String(bytes, 0, j, Charset.defaultCharset());

                //split twitter contents by deliminator "||" and get just the message
                //format: message||hashtags
                String[] fileContents = string.split("\\|\\|");
                String message = "";
                if (fileContents.length != 0) {
                    message = truncateMessage(fileContents[0]);
                }

                //remove .txt from templateName
                templateName = removeExtension(templateName);
                templateFilesT.set(i, templateName);

                //add twitter template to items list
                items.add(new TemplateItems("twitter", templateName, message));
            }
            catch (FileNotFoundException e){
                Log.e("TWITTER_ERROR", e.toString());
            } catch (IOException e){
                Log.e("TWITTER_ERROR", e.toString());
            } catch (NullPointerException e) {
                Log.e("TWITTER_ERROR", e.toString());
            } catch (ArrayIndexOutOfBoundsException e){
                Log.e("TWITTER_ERROR", "array out of bounds exception");
            }
        }

        // Set up recycler view from activity_manage_templates and initialize with list of template items
        templateItems = (RecyclerView) findViewById(R.id.recyclerView_templates);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(ManageTemplatesActivity.this);
        templateItems.setLayoutManager(mLayoutManager);
        adapter = new TemplateAdapter(items, templateFilesE, templateFilesT, ManageTemplatesActivity.this);
        templateItems.setAdapter(adapter);

        //animate new template FAB
        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);

        fabNew = (FloatingActionButton) findViewById(R.id.fab_new);
        fabNewEmail = (FloatingActionButton) findViewById(R.id.fab_new_email);
        fabNewTweet = (FloatingActionButton) findViewById(R.id.fab_new_tweet);

        //new template fab is clicked, initiate showing or hiding of 2ndary template fabs
        fabNew.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onNewButtonClicked();
            }
        });

        //create new email template
        fabNewEmail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide 2ndary fabs, then launch new template page
                onNewButtonClicked();
                Intent intent = new Intent(ManageTemplatesActivity.this, NewEmailTemplateActivity.class);
                startActivityForResult(intent, NEW_EMAIL);
            }
        });

        //create new twitter template
        fabNewTweet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide 2ndary fabs, then launch new template page
                onNewButtonClicked();
                Intent intent = new Intent(ManageTemplatesActivity.this, NewTwitterTemplateActivity.class);
                startActivityForResult(intent, NEW_TWEET);
            }
        });

    }

    //when new template fab is clicked, show or hide 2ndary template fabs
    private void onNewButtonClicked() {
        setVisibility();
        setAnimation();
        setClickable();
        clicked = !clicked;
    }

    //show or hide 2ndary fab buttons
    private void setVisibility() {
        if (!clicked){
            fabNewEmail.show();
            fabNewTweet.show();
        } else {
            fabNewEmail.hide();
            fabNewTweet.hide();
        }
    }
    //set animation on the appearance or disappearance of 2ndary fab buttons
    private void setAnimation() {
        if (!clicked){
            fabNewEmail.startAnimation(fromBottom);
            fabNewTweet.startAnimation(fromBottom);
            fabNew.startAnimation(rotateOpen);
        } else {
            fabNewEmail.startAnimation(toBottom);
            fabNewTweet.startAnimation(toBottom);
            fabNew.startAnimation(rotateClose);
        }
    }

    //make 2ndary fab buttons not clickable when hidden
    private void setClickable() {
        if (!clicked){
            fabNewEmail.setClickable(true);
            fabNewTweet.setClickable(true);
        } else {
            fabNewEmail.setClickable(false);
            fabNewTweet.setClickable(false);
        }
    }

    // utility method removes file extensions from template names
    public static String removeExtension(String s){
        String fileName = s;

        // remove file extension (ie .txt)
        int extensionIndex = fileName.lastIndexOf(".");
        if (extensionIndex == -1){
            return fileName;
        }
        return fileName.substring(0, extensionIndex);
    }

    //receives response from startActivityForResult();
    //position identifies which recyclerView item request returned from to handle intent data correctly
    //resultCode can be RESULT_OK, RESULT_CANCELED, or RESULT_FIRST_USER (user defined codes)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if template was edited...
        if (resultCode == RESULT_OK) {
            String templateName;
            String message;
            int itemPosition;
            switch (requestCode){
                case NEW_EMAIL:
                    // insert new email template at end of email templates
                    itemPosition = templateFilesE.size();
                    templateName = removeExtension(data.getStringExtra(EXTRA_TEMPLATE_NAME));
                    message = truncateMessage(data.getStringExtra(EXTRA_TEMPLATE_MESSAGE));
                    items.add(itemPosition, new TemplateItems("email", templateName, message));
                    //adapter.notifyItemInserted(itemPosition);
                    templateFilesE.add(templateName);
                    break;
                case NEW_TWEET:
                    // insert new twitter template at end of twitter templates (ie end of items list)
                    itemPosition = items.size();
                    templateName = removeExtension(data.getStringExtra(EXTRA_TEMPLATE_NAME));
                    message = truncateMessage(data.getStringExtra(EXTRA_TEMPLATE_MESSAGE));
                    items.add(itemPosition, new TemplateItems("twitter", templateName, message));
                    //adapter.notifyItemInserted(itemPosition);
                    templateFilesT.add(templateName);
                    break;
                case EDIT_TEMPLATE:
                    itemPosition = data.getIntExtra(EXTRA_TEMPLATE_POSITION, 0);
                    TemplateItems item = items.get(itemPosition);
                    message = truncateMessage(data.getStringExtra(EXTRA_TEMPLATE_MESSAGE));
                    item.setMessage(message);
                    items.set(itemPosition, item);
                    //adapter.notifyDataSetChanged();
                    break;
            }

            adapter.notifyDataSetChanged();

        }
    }

    //if message is over 40char, truncate and add "..."
    private String truncateMessage(String message) {
        if (message.length() > 40){
            message = message.substring(0, 38) + "...";
        }
        return message;
    }

    //when back button is pressed, put deleted file names as intent extras and set result
    @Override
    public void onBackPressed(){
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(EXTRA_EMAIL_TEMPLATES, templateFilesE);
        bundle.putStringArrayList(EXTRA_TWITTER_TEMPLATES, templateFilesT);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }
}