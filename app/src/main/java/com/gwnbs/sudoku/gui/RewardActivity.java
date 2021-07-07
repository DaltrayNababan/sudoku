package com.gwnbs.sudoku.gui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.gwnbs.sudoku.R;
import com.gwnbs.sudoku.utils.Val;

public class RewardActivity extends ThemedActivity {

    private RewardedAd rewardedAd;
    public static RewardItem rewardItem;
    private LinearLayout layoutRewardLoading;
    private boolean reload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);
        layoutRewardLoading = findViewById(R.id.layoutRewardLoading);
        loadAd();
    }

    private void loadAd() {
        layoutRewardLoading.setVisibility(View.VISIBLE);
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd videoAd) {
                        super.onAdLoaded(rewardedAd);
                        rewardedAd = videoAd;
                        showAd();
                    }
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        rewardedAd = null;
                        if (!reload) {
                            loadAd();
                            reload = true;
                        } else {
                            SudokuPlayActivity.toastM(getApplicationContext(), getString(R.string.ads_network_problem));
                            onBackPressed();
                        }
                    }
                });
    }

    private void showAd() {
        if (rewardedAd !=null) {
            layoutRewardLoading.setVisibility(View.GONE);
            rewardedAd.show(RewardActivity.this, reward -> {
                rewardItem = reward;
                PreferenceManager.getDefaultSharedPreferences(RewardActivity.this)
                        .edit().putInt(Val.HINT, 10).apply();
            });
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    if (rewardItem == null) {
                        SudokuPlayActivity.toastM(getApplicationContext(), getString(R.string.faile_get_hint));
                    }
                    onBackPressed();
                }
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    rewardedAd = null;
                    loadAd();
                }
                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    rewardedAd = null;
                }
            });
        }
    }
}
