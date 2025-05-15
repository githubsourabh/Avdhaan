package com.avdhaan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import com.avdhaan.R;

import android.widget.Button;
import android.widget.TextView;

public class BlockScreenActivity extends Activity {

    public static boolean isShowing = false;

    @Override
    protected void onStart() {
        super.onStart();
        isShowing = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isShowing = false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            getApplication().registerActivityLifecycleCallbacks(new android.app.Application.ActivityLifecycleCallbacks() {
                @Override public void onActivityResumed(android.app.Activity activity) {
                    if (activity instanceof BlockScreenActivity) {
                        BlockScreenActivity.isShowing = true;
                    }
                }
                @Override public void onActivityPaused(android.app.Activity activity) {
                    if (activity instanceof BlockScreenActivity) {
                        BlockScreenActivity.isShowing = false;
                    }
                }
                @Override public void onActivityCreated(android.app.Activity a, Bundle b) {}
                @Override public void onActivityStarted(android.app.Activity a) {}
                @Override public void onActivityStopped(android.app.Activity a) {}
                @Override public void onActivitySaveInstanceState(android.app.Activity a, Bundle b) {}
                @Override public void onActivityDestroyed(android.app.Activity a) {}
            });
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_block_screen);

        // Make it full screen and appear on top
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        Button exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
           });

    }

    @Override
    public void onBackPressed() {
        // Prevent exiting by back button
    }
}

