package com.oulhafiane.gameball;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ResumeGame extends Activity implements View.OnClickListener {
    //Button buy;
    Button resume;
    TextView nbrBallText;
    TextView nbrTotalBallText;
    int nbrBall;
    int count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get FullScreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setFinishOnTouchOutside(false);
        setContentView(R.layout.activity_resume_game);

        //buy = (Button)findViewById(R.id.buttonBuy);
        resume = (Button)findViewById(R.id.buttonResume);
        nbrBallText = (TextView)findViewById(R.id.textBall);
        nbrTotalBallText = (TextView)findViewById(R.id.textTotalBall);
        nbrBall = loadNbrBall("NbrBalls");
        nbrTotalBallText.setText("Total Ball : "+nbrBall);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            count = extras.getInt("count");
            if(count==0){
                count = 1;
            }else {
                count = count * 2;
            }
            nbrBallText.setText(count+" x ");
            //buy.setOnClickListener(this);
            resume.setOnClickListener(this);
        }
    }

    private void saveNbrBall(String key,int value){
        SecurePreferences preferences = new SecurePreferences(this, "MyPreferences", "723xDBlablaHiho4957", true);
        preferences.put(key, String.valueOf(value));
    }

    private int loadNbrBall(String key){
        SecurePreferences preferences = new SecurePreferences(this, "MyPreferences", "723xDBlablaHiho4957", true);
        String value = preferences.getString(key);
        int nbr;
        if(value!=null){
            nbr = Integer.parseInt(value.toString());
        }else{
            nbr = 0;
        }
        return nbr;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //case R.id.buttonBuy:
            //    break;
            case R.id.buttonResume:
                Bundle bundle = new Bundle();
                if(nbrBall<0){
                    bundle.putBoolean("returnresult", false);
                }else if(nbrBall>=count){
                    bundle.putBoolean("returnresult", true);
                    saveNbrBall("NbrBalls",nbrBall-count);
                }else{
                    bundle.putBoolean("returnresult", false);
                }
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(RESULT_OK,intent);
                finish();
                break;
        }
    }
}
