package xiusdk.camera;


import xiusdk.tools.GlobalDefinitions;
import xiusdk.tools.ScaleUtils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.xiusdk.beautycamera.R;


public class StartPageActivity extends Activity {
private RelativeLayout main = null;
private Handler mHandler = null;
private final int MSG_START_APP = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);


		main = new RelativeLayout(this);
		main.setBackgroundColor(Color.WHITE);
		//main.setBackgroundResource(R.drawable.startpage_bg);
		setContentView(main);

		ScaleUtils.scaleInit(this, 1280, 720, 360);
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		GlobalDefinitions.SCREEN_WIDTH = display.getWidth();
		GlobalDefinitions.SCREEN_HEIGHT = display.getHeight();
		ImageView bgView = new ImageView(this);
		RelativeLayout.LayoutParams params = new android.widget.RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		main.addView(bgView, params);
		bgView.setImageResource(R.drawable.startpage_bg);
		bgView.setScaleType(ScaleType.CENTER_CROP);
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_START_APP:
					if(isFristInstall())
					{
						intentToGuidePage();
					}
					else
					{
						intentToModel();
					}
					break;
				default:
					break;
				}
			}
		};
		mHandler.sendEmptyMessageDelayed(MSG_START_APP,1000);
		
	}
    private boolean isFristInstall()
    {
    	SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE); 
    	boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true); 
    	Editor editor = sharedPreferences.edit(); 
    	boolean res = true;
    	if (isFirstRun) 
    	{ 
    	Log.d(GlobalDefinitions.APP_NAME, "Welcome to use our app!"); 
    	res = isFirstRun;
    	editor.putBoolean("isFirstRun", false); 
    	editor.commit(); 
    	} else 
    	{ 
    	Log.d(GlobalDefinitions.APP_NAME, "It's not the frist time to install our app."); 
    	res = false;
    	} 
    	return res;
    }
	private void intentToGuidePage()
	{
		Intent intent = new Intent(StartPageActivity.this,GuidePageView.class);
		startActivity(intent);
		finish();
	}
	private void intentToModel()
	{
		Intent intent = new Intent(StartPageActivity.this,ModelActivity.class);
		startActivity(intent);
		finish();
	}

}
