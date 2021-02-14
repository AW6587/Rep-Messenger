package com.example.repmessenger;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity{

    ArrayList<ResultItems> items;
    ArrayList<String> templateFilesE, templateFilesT;
    ArrayList<String> levelFilters = new ArrayList<>();
    File emailDirectory, twitterDirectory;
    ResultAdapter adapter;
    RecyclerView recView;
    EditText editSearchZipcode;
    String city, name, office, party, phone, email, twitter;

    public static final int EMAIL = 1;
    public static final int TWEET = 2;
    public static final int EDIT_TEMPLATES = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up recycler view from activity_results to have cardView items displayed by LinearLayoutManager
        recView = (RecyclerView) findViewById(R.id.recyclerView_items);
        recView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recView.setLayoutManager(llm);

        // check if there is a previous saved state
        if (savedInstanceState != null){
            // set item arrayList to saved data
            items = savedInstanceState.getParcelableArrayList("key");
            // if there are items in arrayList, hide splash screen
            if (!items.isEmpty()){
                hideSplashScreen();
            }
        } else {
            // else create a new arrayList
            items = new ArrayList<>();
        }

        // local directory for app
        File mainDirectory = getApplicationContext().getFilesDir();
        // email and twitter template directories, make them if they don't exist already
        emailDirectory = new File(mainDirectory, "emailTemplates");
        if (!emailDirectory.exists()){
            emailDirectory.mkdir();
        }
        twitterDirectory = new File(mainDirectory, "twitterTemplates");
        if (!twitterDirectory.exists()){
            twitterDirectory.mkdir();
        }

        // set up lists of template files
        templateFilesE = new ArrayList(Arrays.asList(emailDirectory.list()));
        templateFilesT = new ArrayList(Arrays.asList(twitterDirectory.list()));

        // remove extensions from file names
        ListIterator itr = templateFilesE.listIterator();
        while ( itr.hasNext()){
            String s = (String)itr.next();
            itr.set(removeExtension(s));
        }
        itr = templateFilesT.listIterator();
        while (itr.hasNext()){
            String s = (String)itr.next();
            itr.set(removeExtension(s));
        }

        // create adapter for recylerView and set it
        adapter = new ResultAdapter(MainActivity.this, items, templateFilesE, templateFilesT);
        recView.setAdapter(adapter);

        // set up search bar listener for if keyboard action button is used instead of tapping search button
        editSearchZipcode = (EditText) findViewById(R.id.editSearch_zipcode);
        editSearchZipcode.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    closeKeyBoard();
                    searchZip();
                    return true;
                }
                return false;
            }
        });
    }

    // save items data when screen rotates
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("key", items);
        super.onSaveInstanceState(outState);
    }

    // shows/inflates menu items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_results, menu);
        return true;
    }

    // handles action bar / menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            //filter option
            case R.id.action_filter_results:

                AlertDialog.Builder myAlertBuilder = new AlertDialog.Builder(MainActivity.this);

                //set the dialog title
                myAlertBuilder.setTitle("Filter Search Results");

                final String[] filterOptions = MainActivity.this.getResources().getStringArray(R.array.array_filter_levels);
                final ArrayList<String> selectedFilters = new ArrayList<>();
                final boolean[] checkedItems = new boolean[3];

                for(int i = 0; i < filterOptions.length; i++){
                    if (levelFilters.contains(filterOptions[i])){
                        checkedItems[i] = true;
                        selectedFilters.add(filterOptions[i]);
                    }
                }


                //specify multi-choice display strings, the items to be selected by default (null for none),
                //and the listener through which to receive callbacks when items are selected
                myAlertBuilder.setMultiChoiceItems(R.array.array_filter_locality, checkedItems, new OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {

                        if (isChecked){
                            //if the user checked the item, add it to the selected items
                            selectedFilters.add(filterOptions[which]);
                        }
                        else if (selectedFilters.contains(filterOptions[which])){
                            //else if the item is already in the array, remove it
                            selectedFilters.remove(filterOptions[which]);
                        }
                    }
                });

                //set dialog action buttons
                myAlertBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //user clicked Apply button
                        //TO DO: process selectedFilters array and filter shown results
                        levelFilters.clear();
                        levelFilters.addAll(selectedFilters);
                        searchZip();
                    }
                });

                myAlertBuilder.setNegativeButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //user clicked Clear button
                        //remove all level filters
                        levelFilters.clear();
                        searchZip();
                    }
                });

                //create and show the filter AlertDialog
                myAlertBuilder.show();

                return true;

            //manage message templates option
            case R.id.action_manage_templates:
                Intent intentTemplates = new Intent(MainActivity.this, ManageTemplatesActivity.class);
                startActivityForResult(intentTemplates, EDIT_TEMPLATES);
                return true;

            //about page option
            case R.id.action_about:
                Intent intentAbout = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intentAbout);
                return true;

            default:
                //do nothing
                return super.onOptionsItemSelected(item);
        }

    }

    //user enters zip code, presses search button, call search method
    public void searchBtnClicked(View view){
        searchZip();
    }
    //search civic info api and display results in recycler view
    public void searchZip() {
        String zipCode = editSearchZipcode.getText().toString();

        //check field isn't empty
        if (!TextUtils.isEmpty(zipCode)){
            int zip = Integer.parseInt(zipCode);

            //check if zip is five digits long
            if (isFiveDigits(zip)) {
                // check if there's internet available
                if (networkAvailable()) {

                    String key = BuildConfig.CALL_KEY;
                    String url;
                    if (levelFilters.isEmpty()) {
                        url = "https://civicinfo.googleapis.com/civicinfo/v2/representatives?address=" + zip + "&key=" + key;
                    } else {
                        //add government levels to filter results, default is all levels
                        String levels = "";
                        for (String level : levelFilters) {
                            levels = levels + "&levels=" + level;
                        }
                        url = "https://civicinfo.googleapis.com/civicinfo/v2/representatives?address=" + zip + levels + "&key=" + key;
                    }

                    //Ion.getDefault(this).getConscryptMiddleware().enable(false);

                    // Api call to Google Civic Information
                    Ion.with(this)
                            .load("GET", url)
                            .setLogging("IonLogs", Log.DEBUG)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {

                                    // check if results were returned
                                    if (result.has("divisions")) {

                                        // hide the splash screen
                                        hideSplashScreen();

                                        // get Json object for the city associated with the searched zipcode
                                        JsonObject normalizedInput = result.get("normalizedInput").getAsJsonObject();
                                        city = normalizedInput.get("city").toString();
                                        city = city.substring(1, city.length() - 1);
                                        // get Json array of offices and officials
                                        JsonArray offices = result.get("offices").getAsJsonArray();
                                        JsonArray officials = result.get("officials").getAsJsonArray();

                                        //clear items array first of previous search data
                                        items.clear();

                                        // get officials data and add to java items array
                                        for (int i = 0; i < offices.size(); i++) {

                                            JsonObject officeObj = offices.get(i).getAsJsonObject();
                                            JsonArray officialIndices = officeObj.get("officialIndices").getAsJsonArray();

                                            // some offices have multiple representatives
                                            for (int j = 0; j < officialIndices.size(); j++) {
                                                // get official related to office role
                                                int index = officialIndices.get(j).getAsInt();
                                                JsonObject officialObj = officials.get(index).getAsJsonObject();

                                                //assign data for given official object
                                                name = officialObj.get("name").toString();
                                                name = name.substring(1, name.length() - 1);

                                                office = officeObj.get("name").toString();
                                                office = office.substring(1, office.length() - 1);

                                                party = officialObj.get("party").toString();
                                                party = party.substring(1, party.length() - 1);

                                                // the following are string arrays, only interested in 1 from each
                                                if (officialObj.has("phones")) {
                                                    JsonArray phones = officialObj.get("phones").getAsJsonArray();
                                                    phone = phones.get(0).toString();
                                                    phone = phone.substring(1, phone.length() - 1);
                                                } else {
                                                    phone = null;
                                                }

                                                if (officialObj.has("emails")) {
                                                    JsonArray emails = officialObj.get("emails").getAsJsonArray();
                                                    email = emails.get(0).toString();
                                                    email = email.substring(1, email.length() - 1);
                                                } else {
                                                    email = null;
                                                }

                                                //find if they have a twitter handle
                                                if (officialObj.has("channels")) {
                                                    JsonArray channels = officialObj.get("channels").getAsJsonArray();
                                                    for (int k = 0; k < channels.size(); k++) {
                                                        JsonObject channel = channels.get(k).getAsJsonObject();
                                                        String type = channel.get("type").getAsString();
                                                        if (type.equals("Twitter")) {
                                                            twitter = channel.get("id").toString();
                                                            twitter = twitter.substring(1, twitter.length() - 1);
                                                            twitter = "@" + twitter;
                                                            break;
                                                        } else {
                                                            twitter = null;
                                                        }
                                                    }
                                                } else {
                                                    twitter = null;
                                                }

                                                //add new set of items to list for cardview use
                                                items.add(new ResultItems(city, name, office, party, phone, email, twitter));
                                            }


                                        }

                                        //tell adapter to update
                                        adapter.notifyDataSetChanged();

                                        Log.e("Result", "success");
                                    }
                                    // got an error instead of results, display error and prompt user to try again
                                    else {
                                        if (result.has("error")) {
                                            JsonObject error = result.get("error").getAsJsonObject();
                                            int errCode = error.get("code").getAsInt();
                                            String message = error.get("message").toString();

                                            switch (errCode) {
                                                case 400:   // address failed
                                                    makeToast("Invalid zipcode");
                                                    break;
                                                case 404:   // no information for address
                                                    makeToast("No results found for given zipcode");
                                                    break;
                                                case 409:   // conflicting information found for this address
                                                    makeToast("Conflicting information found for this address, try a different zipcode");
                                                    break;
                                                case 503:   // backend Error, try again
                                                    makeToast("Backend error, try again");
                                                    break;
                                                default:    //other errors, try again. shouldn't be any other kinds though
                                                    makeToast("unknown issue, try again");
                                                    break;
                                            }
                                            Log.e("Result", "error " + Integer.toString(errCode) + ": " + message);
                                        } else {
                                            makeToast("No results found");
                                            Log.e("Result", "error, unknown");
                                        }
                                    }
                                }
                            });
                } else {
                    makeToast("Please check your network connection and try again");
                }
            } else {
                makeToast("enter a valid zip code");
            }
        } else {
            makeToast("enter a valid zip code");
        }

    }

    //hide splash screen image
    private void hideSplashScreen() {
        ConstraintLayout splashLayout = findViewById(R.id.splash_layout);
        splashLayout.setVisibility(View.GONE);
        recView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorSecondary));
    }

    //utility method checks if a number is five digits ie. a zip code's length
    private boolean isFiveDigits(int n){
        if (n == 0) return false;
        boolean isFive = false;
        n = Math.abs(n);
        int len;
        for (len = 0; n > 0; ++len){
            if (len > 5) break;
            n /= 10;
        }
        if (len == 5) isFive = true;
        return isFive;
    }

    // utility method checks if phone has an internet connection
    public boolean networkAvailable() {
        boolean hasNetwork = false;
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr != null) {
            // check the build version
            // build >= 23 uses NetworkCapabilities getNetworkCapabilities
            if (VERSION.SDK_INT >= VERSION_CODES.M) {
                NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork());
                if (capabilities != null) {
                    hasNetwork = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                }
            }
            // else use deprecated NetworkInfo getActiveNetworkInfo
            else {
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null) {
                    hasNetwork = networkInfo.isConnected();
                }
            }
        }

        return hasNetwork;
    }

    //create a new email message template
    public void createEmailTemplate(View view) {
        Intent intent = new Intent(MainActivity.this, NewEmailTemplateActivity.class);
        startActivityForResult(intent, EMAIL);
    }

    //create a new twitter message template
    public void createTwitterTemplate(View view) {
        Intent intent = new Intent(MainActivity.this, NewTwitterTemplateActivity.class);
        startActivityForResult(intent, TWEET);
    }

    //receives response from startActivityForResult();
    //requestCode identifies which activity returned from to handle intent data correctly
    //resultCode can be RESULT_OK, RESULT_CANCELED, or RESULT_FIRST_USER (user defined codes)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

        //if new template was saved...
        if (resultCode == RESULT_OK){
            String fileName;
            switch (requestCode){
                case EMAIL:
                    //get new template name from NewEmailTemplateActivity and add to list
                    fileName = removeExtension(intent.getStringExtra(NewEmailTemplateActivity.EXTRA_TEMPLATE_NAME));
                    templateFilesE.add(fileName);
                    break;
                case TWEET:
                    //get new template name from NewEmailTemplateActivity and add to list
                    fileName = removeExtension(intent.getStringExtra(NewTwitterTemplateActivity.EXTRA_TEMPLATE_NAME));
                    templateFilesT.add(fileName);
                    break;
                case EDIT_TEMPLATES:
                    //update file names from bundle extra for email and twitter template lists
                    templateFilesE.clear();
                    templateFilesT.clear();
                    templateFilesE.addAll(intent.getStringArrayListExtra(ManageTemplatesActivity.EXTRA_EMAIL_TEMPLATES));
                    templateFilesT.addAll(intent.getStringArrayListExtra(ManageTemplatesActivity.EXTRA_TWITTER_TEMPLATES));
                    break;
            }


            //tell adapter to update so new file shows in spinner
            adapter.notifyDataSetChanged();
        }
    }

    //display Toast message
    private void makeToast(String message){
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    //utility method to hide keyboard
    private void closeKeyBoard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    // if screen tapped outside of (editText)editSearchZipcode hide keyboard and clear focus
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

}
