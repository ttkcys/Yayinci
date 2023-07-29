package com.yusudhanyayinci.yaynci;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button goLiveBtn;
    TextInputEditText liveIdInput,nameInput;

    String liveID,name,userID;
    SharedPreferences sharedPreferences;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        setAds();

        sharedPreferences = getSharedPreferences("name_pref",MODE_PRIVATE);

        goLiveBtn = findViewById(R.id.go_live_button);
        liveIdInput = findViewById(R.id.live_id_input);
        nameInput = findViewById(R.id.name_input);

        nameInput.setText(sharedPreferences.getString("name",""));

        liveIdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                liveID = liveIdInput.getText().toString();

                if(liveID.length() == 0){
                    goLiveBtn.setText("Yayına Başla");
                }else{
                    goLiveBtn.setText("Yayına Katıl");
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        goLiveBtn.setOnClickListener((v)->{

            if(mInterstitialAd != null){
                mInterstitialAd.show(MainActivity.this);
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        startActivity(new Intent(MainActivity.this,LiveActivity.class));
                        mInterstitialAd = null;
                        setAds();
                    }
                });
            }else{
                startActivity(new Intent(MainActivity.this,LiveActivity.class));
            }

            name = nameInput.getText().toString();

            if(name.isEmpty()){
                nameInput.setError("Kullanıcı Adı Gerekli");
                nameInput.requestFocus();
                return;
            }

            liveID = liveIdInput.getText().toString();

            if(liveID.length() > 0 && liveID.length() != 5){
                liveIdInput.setError("Geçersiz Yayın Numarası");
                liveIdInput.requestFocus();
                return;
            }
            startMeeting();
        });

    }
    void startMeeting(){

        sharedPreferences.edit().putString("name",name).apply();
        boolean isHost = true;

        if(liveID.length() == 5)
            isHost = false;
        else
            liveID = genereteLiveID();

        userID = UUID.randomUUID().toString();

        Intent intent   = new Intent(MainActivity.this,LiveActivity.class);
        intent.putExtra("user_id",userID);
        intent.putExtra("name",name);
        intent.putExtra("live_id",liveID);
        intent.putExtra("host",isHost);
        startActivity(intent);
    }

    String genereteLiveID(){
        StringBuilder id = new StringBuilder();

        while (id.length() != 5){
            int random = new Random().nextInt(10);
            id.append(random);
        }
        return id.toString();
    }

    @Override
    public void onBackPressed() {
        // Uygulamayı tamamen kapatmak için
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public void setAds(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-5060997619686611~6913052501", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;
                    }
                });
    }
}