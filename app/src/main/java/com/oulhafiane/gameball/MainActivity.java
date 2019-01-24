package com.oulhafiane.gameball;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    TextView scoreText;
    TextView bestScoreText;
    long score;
    String username = "";
    String password = "";
    String name = "";
    int nbrFriends = 0;

    //Facebook Champs
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private MediaPlayer win,lose;
    ArrayList<String> friends = new ArrayList<>();
    ListView listView;
    ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Facebook Code
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);
        score = loadBestScore("BESTSCORE");

        new VersionAsync().execute();
        checkLogin();
        if(score==0){
            firstRun();
        }
        //Log.d("NBRFRIENDS",getInfo("NBRFRIENDS"));
        //Log.d("FRIEND0", getInfo("FRIEND0"));
        new ScoresAsync().execute();
        getListFriends();

        //Facebook Code
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getListFriendsFacebook(loginResult);
                SetInfoFacebook(loginResult);
            }

            @Override
            public void onCancel() {
                Toast.makeText(getBaseContext(), "Login attempt canceled.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(getBaseContext(), "Login attempt failed.", Toast.LENGTH_SHORT).show();
            }
        });
        //*************

        //Get Score from GameView
        bestScoreText = (TextView)findViewById(R.id.BestScore);
        scoreText = (TextView)findViewById(R.id.Score);
        bestScoreText.setText("Best score : " + score);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            long value = extras.getLong("score");
            if(score>=value){
                if(value>0){
                    scoreText.setText("Your score is : "+value);
                    scoreText.setVisibility(View.VISIBLE);
                    lose = MediaPlayer.create(this,R.raw.lose);
                    lose.start();
                }
            }else{
                saveBestScore("BESTSCORE", value);
                scoreText.setText("Congratulation \n your new score is : " + value);
                scoreText.setVisibility(View.VISIBLE);
                score = value;
                bestScoreText.setText("Best score : " + score);
                win = MediaPlayer.create(this,R.raw.win);
                win.start();
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();*/
                    startActivity(new Intent(getBaseContext(), GameView.class));
                    finish();
                }
            });
        }
    }

    //Methods

    public void firstRun(){
        saveBestScore("NbrBalls", 30);
    }

    public void checkLogin(){
        if(getInfo("ID")==""){
            Toast.makeText(getBaseContext(),"You must login with facebook to share your score with your friends!",Toast.LENGTH_LONG).show();
        }
    }

    //AsyncTasks Methods Get And Save Scores
    public class RegisterAsync extends AsyncTask<String,String,JSONObject>{
        JSONParser jsonParser = new JSONParser();
        private static final String REGISTER_URL = "http://www.oulhafiane.com/gameball/register.php";
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected JSONObject doInBackground(String... args) {
            String id = args[0];
            StringBuilder pass = new StringBuilder(id+args[1]+"GameBall");
            String alph = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.";
            StringBuilder key = new StringBuilder("eaBlG6dmofVR4HYLUJZWM0ITXhyPNOj3b1C9pvr8D5.nsqKE7zcwSQugAk2Fitx");

            int ide = Character.getNumericValue(id.charAt(0));
            int idc = Character.getNumericValue(id.charAt(ide));
            for(int i=0;i<key.length();i++){
                key.setCharAt(i,(char)((int)key.charAt(i)+idc));
            }
            for(int i=0;i<pass.length();i++){
                for(int j=0;j<alph.length();j++){
                    if(pass.charAt(i)==alph.charAt(j)){
                        pass.setCharAt(i,key.charAt(j));
                        break;
                    }
                }
            }

            try{
                HashMap<String,String> params = new HashMap<>();
                params.put("username",args[0]);
                params.put("password",pass.toString());
                params.put("name",args[2]);
                params.put("score",args[3]);
                JSONObject json = jsonParser.makeHttpRequest(REGISTER_URL,"POST",params);
                if(json!=null){
                    return json;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String message = "";
            if(json!=null){
                //Toast.makeText(getBaseContext(),json.toString(),Toast.LENGTH_LONG).show();
                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(success==1){
                Toast.makeText(getBaseContext(),"Your Score Has Been Updated",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(getBaseContext(),"Cannot update your info : "+message,Toast.LENGTH_LONG).show();
            }
        }
    }

    public static class LoginAsync extends AsyncTask<String,String,JSONObject> {
        JSONParser jsonParser = new JSONParser();
        private static final String LOGIN_URL = "http://www.oulhafiane.com/gameball/login.php";
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";
        private static final String TAG_SCORE = "score";

        public interface AsyncResponse {
            void processFinish(int success,String message,long score);
        }

        public AsyncResponse delegate = null;

        public LoginAsync(AsyncResponse delegate){
            this.delegate = delegate;
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String id = args[0];
            StringBuilder pass = new StringBuilder(id + args[1] + "GameBall");
            String alph = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.";
            StringBuilder key = new StringBuilder("eaBlG6dmofVR4HYLUJZWM0ITXhyPNOj3b1C9pvr8D5.nsqKE7zcwSQugAk2Fitx");

            int ide = Character.getNumericValue(id.charAt(0));
            int idc = Character.getNumericValue(id.charAt(ide));
            for (int i = 0; i < key.length(); i++) {
                key.setCharAt(i, (char) ((int) key.charAt(i) + idc));
            }
            for (int i = 0; i < pass.length(); i++) {
                for (int j = 0; j < alph.length(); j++) {
                    if (pass.charAt(i) == alph.charAt(j)) {
                        pass.setCharAt(i, key.charAt(j));
                        break;
                    }
                }
            }

            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("username", args[0]);
                params.put("password", pass.toString());
                params.put("usernamee", args[2]);
                JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
                if (json != null) {
                    return json;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String message = "";
            long score = 0;
            if (json != null) {
                //Log.d("LoginAsync",json.toString());
                //Toast.makeText(MainActivity.this, json.toString(), Toast.LENGTH_LONG).show();
                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                    score = json.getLong(TAG_SCORE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            delegate.processFinish(success,message,score);
        }
    }

    public class ScoresAsync extends AsyncTask<String,String,JSONObject>{
        JSONParser jsonParser = new JSONParser();
        private static final String SCORE_URL = "http://www.oulhafiane.com/gameball/scores.php";
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";
        private static final String TAG_SCORE = "score";
        @Override
        protected JSONObject doInBackground(String... args) {
            try{
                HashMap<String,String> params = new HashMap<>();
                JSONObject json = jsonParser.makeHttpRequest(SCORE_URL,"GET",params);
                if(json!=null){
                    return json;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String message = "";
            long score = 0;
            if(json!=null){
                //Toast.makeText(getBaseContext(),json.toString(),Toast.LENGTH_LONG).show();
                try {
                    success = json.getInt("success");
                    message = json.getString("message");
                    score = json.getLong("score");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(success==1){
                    JSONArray friendlist = null;
                    ArrayList<String> top = new ArrayList<>();
                    ArrayAdapter adapterTop;
                    ListView listTop = (ListView)findViewById(R.id.listTop);
                    try {
                        friendlist = json.getJSONArray("scores");
                        for (int i = 0; i < friendlist.length(); i++) {
                            String name = friendlist.getJSONObject(i).getString("name");
                            String scoree = friendlist.getJSONObject(i).getString("score");
                            top.add(i+1+") "+name+" \n "+scoree);
                            adapterTop = new ArrayAdapter<String>(getBaseContext(), R.layout.aligned_right, top);
                            listTop.setAdapter(adapterTop);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Toast.makeText(getBaseContext(),"SUCCESS",Toast.LENGTH_LONG).show();
                }else {
                    //Toast.makeText(getBaseContext(),"FAILURE",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class VersionAsync extends AsyncTask<String,String,JSONObject>{
        JSONParser jsonParser = new JSONParser();
        private static final String SCORE_URL = "http://www.oulhafiane.com/gameball/version.php";
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";
        private static final String TAG_VERSION = "version";
        @Override
        protected JSONObject doInBackground(String... args) {
            try{
                HashMap<String,String> params = new HashMap<>();
                JSONObject json = jsonParser.makeHttpRequest(SCORE_URL,"GET",params);
                if(json!=null){
                    return json;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String message = "";
            long score = 0;
            if(json!=null){
                //Toast.makeText(getBaseContext(),json.toString(),Toast.LENGTH_LONG).show();
                try {
                    success = json.getInt("success");
                    message = json.getString("message");
                    score = json.getLong("score");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(success==1){
                    try {
                        String version = json.getString(TAG_VERSION);
                        String oldVersion = getVersion();
                        if(!version.equals(oldVersion)){
                            notifyUpdate();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Toast.makeText(getBaseContext(),"SUCCESS",Toast.LENGTH_LONG).show();
                }else {
                    //Toast.makeText(getBaseContext(),"FAILURE",Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    /*////////////////////////////////////*/

    private String getVersion(){
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        return version;
    }

    private void notifyUpdate(){
        Runnable showUpdate = new Runnable(){
            public void run(){
                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Update available")
                        .setMessage("An update for Game Ball is available on Play Store.")
                        .setNegativeButton("Update now", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            /* User clicked OK so do some stuff */
                                //easyTracker.send(MapBuilder.createEvent("App update",
                                //        "Update now", " ", null).build());
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.oulhafiane.gameball"));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("Later", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            /* User clicked Cancel */
                                //easyTracker.send(MapBuilder.createEvent("Update_Later",
                                 //       "Update later", " ", null).build());
                            }
                        })
                        .show();
            }
        };
        showUpdate.run();
    }

    public String cryptScore(long score){
        String id = username;
        StringBuilder pass = new StringBuilder(id+String.valueOf(score)+"gameBall");
        String alph = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz.";
        StringBuilder key = new StringBuilder("eaBlG6dmofVR4HYLUJZWM0ITXhyPNOj3b1C9pvr8D5.nsqKE7zcwSQugAk2Fitx");

        int ide = Character.getNumericValue(id.charAt(0));
        int idc = Character.getNumericValue(id.charAt(ide));
        for (int i = 0; i < key.length(); i++) {
            key.setCharAt(i, (char) ((int) key.charAt(i) + idc));
        }
        for (int i = 0; i < pass.length(); i++) {
            for (int j = 0; j < alph.length(); j++) {
                if (pass.charAt(i) == alph.charAt(j)) {
                    pass.setCharAt(i, key.charAt(j));
                    break;
                }
            }
        }
        return pass.toString();
    }

    //Get Info Facebook when logged
    private void SetInfoFacebook(LoginResult loginResult){
        final AccessToken accessToken = loginResult.getAccessToken();

        GraphRequestAsyncTask request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                String namee = "";
                String id = "";
                try {
                    namee = user.getString("name");
                    id = user.getString("id");
                    String idr = getInfo("ID");
                    String key = "eaBlG6dmofVR4HYLUJZWM0ITXhyPNOj3b1C9pvr8D5.nsqKE7zcwSQugAk2Fitx";
                    StringBuilder pass = new StringBuilder();
                    for(int i=0;i<id.length();i++){
                        pass.append(key.charAt(i));
                    }
                    saveInfo("ID", id);
                    saveInfo("NAME",namee);
                    saveInfo("PASS",pass.toString());
                    username = id;
                    password = pass.toString();
                    name = namee;
                    if(idr!=""){
                        //Already registred
                        //Log.d("Info Facebook","Already register ID");
                        if(idr!=id){
                            //changed User
                            //Log.d("Info Facebook", "Changed user ID");
                            //Log.d("Registre1", id + " " + pass + " " + name + " " + String.valueOf(score));
                            new LoginAsync(new LoginAsync.AsyncResponse() {
                                @Override
                                public void processFinish(int success, String message, long scoreF) {
                                    if (success == 1) {
                                        if (scoreF < score) {
                                            //Log.d("Registre11",username+" "+password+" "+name+" "+String.valueOf(score));
                                            new RegisterAsync().execute(username, password, name, cryptScore(score));
                                            getListFriends();
                                        }
                                    }else{
                                        //Log.d("Registre12",username+" "+password + " " +name+" "+String.valueOf(score));
                                        new RegisterAsync().execute(username, password, name, cryptScore(score));
                                        getListFriends();
                                    }
                                }
                            }).execute(username, password, username);
                        }
                    } else {
                        //New User
                        //Log.d("Info Facebook", "New User ID");
                        //Log.d("Registre2", id + " " + pass + " " + name + " " + String.valueOf(score));
                        new LoginAsync(new LoginAsync.AsyncResponse() {
                            @Override
                            public void processFinish(int success, String message, long scoreF) {
                                if (success == 1) {
                                    //Log.d("Success 1 register11",message);
                                    if (scoreF < score) {
                                        //Log.d("Registre21",username+" "+password+" "+name+" "+String.valueOf(score));
                                        new RegisterAsync().execute(username, password, name, cryptScore(score));
                                        getListFriends();
                                    }
                                }else{
                                    //Log.d("Success 1 register22",message);
                                    //Log.d("Registre22",username+" "+password+" "+name+" "+String.valueOf(score));
                                    new RegisterAsync().execute(username, password, name, cryptScore(score));
                                    getListFriends();
                                }
                            }
                        }).execute(username, password, username);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).executeAsync();
    }

    public void makeInfo(){
        username = getInfo("ID");
        password = getInfo("PASS");
        name = getInfo("NAME");
        if(username!="") {
            if(getInfo("NBRFRIENDS")==""){
                nbrFriends = 0;
            }else {
                nbrFriends = Integer.parseInt(getInfo("NBRFRIENDS"));
            }
            new LoginAsync(new LoginAsync.AsyncResponse() {
                @Override
                public void processFinish(int success, String message, long scoreF) {
                    if (success == 1) {
                        if (scoreF < score) {
                            //Log.d("Registre3",username+" "+password+" "+name+" "+String.valueOf(score));
                            new RegisterAsync().execute(username, password, name, cryptScore(score));
                        }
                    }
                }
            }).execute(username, password, username);
        }
    }

    //Get List Friends from Preferences
    private void getListFriends(){
        makeInfo();
        friends.clear();
        listView = (ListView) findViewById(R.id.listFriends);
        for (int i = 0; i < nbrFriends; i++){
            final String usernamee = getInfo("FRIEND"+i);
            final String namee = getInfo("FRIENDNAME" + i);
            new LoginAsync(new LoginAsync.AsyncResponse() {
                @Override
                public void processFinish(int success,String message,long scoreF) {
                    if(success==1){
                        friends.add(namee + " : " + scoreF);
                        adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, friends);
                        listView.setAdapter(adapter);
                    }
                }
            }).execute(username, password, usernamee);
        }
    }

    //Get List friends playing the game from Facebook
    private void getListFriendsFacebook(LoginResult loginResult) {
        final AccessToken accessToken = loginResult.getAccessToken();
        if(accessToken!=null){
            new GraphRequest(
                    accessToken, "/me/friends", null, HttpMethod.GET, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    try {
                        JSONArray friendlist = response.getJSONObject().getJSONArray("data");
                        for (int i = 0; i < friendlist.length(); i++) {
                            saveInfo("FRIEND" + i, friendlist.getJSONObject(i).getString("id"));
                            saveInfo("FRIENDNAME" + i, friendlist.getJSONObject(i).getString("name"));
                        }
                        saveInfo("NBRFRIENDS", String.valueOf(friendlist.length()));
                        getListFriends();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }
            }).executeAsync();
        }
    }

    private String getInfo(String key){
        SecurePreferences preferences = new SecurePreferences(this, "MyPreferences", "723xDBlablaHiho4957", true);
        String value = preferences.getString(key);
        String info;
        if(value!=null){
            info = value;
        }else{
            info = "";
        }
        return info;
    }

    private void saveInfo(String key,String value){
        SecurePreferences preferences = new SecurePreferences(this, "MyPreferences", "723xDBlablaHiho4957", true);
        preferences.put(key, value);
    }

    private void saveBestScore(String key,long value){
        /*SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key,value);
        editor.commit();*/
        SecurePreferences preferences = new SecurePreferences(this, "MyPreferences", "723xDBlablaHiho4957", true);
        preferences.put(key, String.valueOf(value));
    }

    private long loadBestScore(String key){
        /*SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        long score = sp.getLong(key,0);*/
        SecurePreferences preferences = new SecurePreferences(this, "MyPreferences", "723xDBlablaHiho4957", true);
        String value = preferences.getString(key);
        long score;
        if(value!=null){
            score = Long.parseLong(value);
        }else{
            score = 0;
        }
        return score;
    }

    public void startGame(View v){
        startActivity(new Intent(this,GameView.class));
        finish();
    }

    //Menu Methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            startActivity(new Intent(this,About.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    //Get ActivityResult for Facebook
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}