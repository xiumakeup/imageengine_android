package xiusdk.edit;

import xiusdk.camera.ModelActivity;
import xiusdk.tools.BottomView;
import xiusdk.tools.GlobalDefinitions;
import xiusdk.tools.ImageUtils;
import xiusdk.tools.PhotoUtils;
import xiusdk.tools.ScaleUtils;
import xiusdk.tools.TitleView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.xiusdk.beautycamera.R;

public class EditorActivity extends Activity {
	private RelativeLayout main = null;
	private RelativeLayout mEditLayout = null;
	private TitleView mTitleBar = null;
	private BottomView mBottomBar = null;
	private ImageView mCancelBtn = null;
	private ImageView mOkBtn = null;
	private Uri mImgUri = null;
	private RelativeLayout mImgLayout = null;
	private ImageView mImgView = null;
	private String mTitleName = null;
	private int mEditLayoutHeight = 200;
	private PhotoEditor mPhotoEdit = null;
	public static boolean mFilterImageSaveDone = false;
	private Handler mHandler = null;
	private final int MSG_CHECKED_IMAGESAVE_STATE = 0X001;
	private ProgressBar mWaitBar = null;
	private RelativeLayout popWindow = null;
	private ImageView mBottomOkBtn = null;
	private ImageView mBottomCancelBtn = null;
	private int mRotateNum = 90;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		main = new RelativeLayout(this);
		main.setBackgroundColor(Color.BLACK);
		setContentView(main);
		ScaleUtils.scaleInit(this,1280,720,320);
		constructTitleBottomUI();
		//initialHandler();
		intialEngine();
	}
	private void initialHandler()
	{
		if(mHandler!=null)
		{
			mHandler.removeMessages(MSG_CHECKED_IMAGESAVE_STATE);
			mHandler = null;
		}
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what)
				{
					case MSG_CHECKED_IMAGESAVE_STATE :
						if(mFilterImageSaveDone)
						{
							intialEngine();
							mFilterImageSaveDone = false;
							mHandler.removeMessages(MSG_CHECKED_IMAGESAVE_STATE);
						}
						else
							mHandler.sendEmptyMessageDelayed(MSG_CHECKED_IMAGESAVE_STATE, 5);
				}
			}
		};
		mHandler.sendEmptyMessage(MSG_CHECKED_IMAGESAVE_STATE);
	}
	private void intialEngine()
	{
		Intent intent = getIntent();
		mImgUri = intent.getData();
		if(null!=mImgUri)
		{
			mImgView.setImageBitmap(null);
			//mImgView.setImageURI(mImgUri);
			mPhotoEdit = new PhotoEditor(this,mImgUri.toString());
			mImgView.setImageBitmap(mPhotoEdit.getDisplayImage());
		}
		else
		{
			mImgView.setImageBitmap(null);
		}

	}
	private void constructTitleBottomUI()
	{
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,ScaleUtils.scale(100));
		mTitleBar = new TitleView(this,GlobalDefinitions.SCREEN_WIDTH,ScaleUtils.scale(100),R.drawable.title_back,R.drawable.title_ok, getString(R.string.edittitle));
		mTitleBar.setId(GlobalDefinitions.ID_TITLE);
		param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		main.addView(mTitleBar,param);
		String[] names = new String[]{getString(R.string.bottom_bc),getString(R.string.bottom_colortempeature),getString(R.string.bottom_rotate),
				getString(R.string.bottom_exposure),getString(R.string.bottom_saturation),getString(R.string.bottom_sharpen),getString(R.string.bottom_light),getString(R.string.bottom_highlight),getString(R.string.bottom_shadow),getString(R.string.bottom_moscia),getString(R.string.bottom_hue),getString(R.string.bottom_gray)};
		param = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,ScaleUtils.scale(110));
		mBottomBar = new BottomView(this,GlobalDefinitions.SCREEN_WIDTH,ScaleUtils.scale(110),names,ScaleUtils.getTextSize(18));
		mBottomBar.setId(GlobalDefinitions.ID_BOTTOM);
		param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		main.addView(mBottomBar,param);
		popWindow = new RelativeLayout(this);
		popWindow.setId(GlobalDefinitions.ID_BOTTOM_POPWINDOW);
		popWindow.setBackgroundResource(R.drawable.edit_bottombar_bg);
		param = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,ScaleUtils.scale(110));
		param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		popWindow.setVisibility(View.INVISIBLE);
		main.addView(popWindow,param);
		mBottomOkBtn = new ImageView(this);
		mBottomOkBtn.setBackgroundResource(R.drawable.edit_okbtn);
		mBottomOkBtn.setClickable(true);
		param = new RelativeLayout.LayoutParams(GlobalDefinitions.SCREEN_WIDTH / 2-2,ScaleUtils.scale(110));
		param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		popWindow.addView(mBottomOkBtn,param);
		mBottomCancelBtn = new ImageView(this);
		mBottomCancelBtn.setBackgroundResource(R.drawable.edit_cancelbtn);
		mBottomCancelBtn.setClickable(true);
		param = new RelativeLayout.LayoutParams(GlobalDefinitions.SCREEN_WIDTH / 2-2,ScaleUtils.scale(110));
		param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		popWindow.addView(mBottomCancelBtn,param);
		//
		//image layout
		mImgLayout = new RelativeLayout(this);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,GlobalDefinitions.SCREEN_HEIGHT-ScaleUtils.scale(210));
		params.addRule(RelativeLayout.BELOW,mTitleBar.getId());
		main.addView(mImgLayout,params);
		mImgView = new ImageView(this);
		mImgView.setScaleType(ScaleType.FIT_CENTER);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT );
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		mImgLayout.addView(mImgView,params);
		//edit layout
		mEditLayout = new RelativeLayout(this);
		mEditLayout.setId(GlobalDefinitions.ID_EDIT_LAYOUT);
		//mEditLayout.setBackgroundColor(Color.rgb(36,40,45));
		mEditLayout.setBackgroundColor(Color.TRANSPARENT);

		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,ScaleUtils.scale(mEditLayoutHeight));
		params.addRule(RelativeLayout.ABOVE,mBottomBar.getId());
		main.addView(mEditLayout,params);

		mCancelBtn = (ImageView)mTitleBar.findViewById(mTitleBar.getBackBtnId());
		mOkBtn = (ImageView)mTitleBar.findViewById(mTitleBar.getOkBtnId());
		final int[]mBtnId = mBottomBar.getBtnId();
		for(int i =0;i<mBtnId.length;i++)
		{
			TextView txt = (TextView)mBottomBar.findViewById(mBtnId[i]);
			final int len = i;
			txt.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mBottomBar.pressButton(v.getId());
					switch(len)
					{
						case 0:
							constructBCUI();
							mEditLayout.setVisibility(View.VISIBLE);
							mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
							break;
						case 1:
							constructColorTempeatureUI();
							mEditLayout.setVisibility(View.VISIBLE);
							mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
							break;
						case 2:
							constructRotateUI();
							mEditLayout.setVisibility(View.VISIBLE);
							mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
							break;
						case 3:
							constructExposureUI();
							mEditLayout.setVisibility(View.VISIBLE);
							mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
							break;
						case 4:
							constructSaturationUI();
							mEditLayout.setVisibility(View.VISIBLE);
							mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
							break;
						case 5:
							constructSharpenUI();
							mEditLayout.setVisibility(View.VISIBLE);
							mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
							break;
						case 6:
							constructLightnessUI();
							mEditLayout.setVisibility(View.VISIBLE);
							mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
							break;
						case 7:
							constructHighlightUI();
							mEditLayout.setVisibility(View.VISIBLE);
							mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
							break;
						case 8:
							constructShadowUI();
							mEditLayout.setVisibility(View.VISIBLE);
							mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
							break;
						case 9:
							constructMosciaUI();
							mEditLayout.setVisibility(View.VISIBLE);
							mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
							break;
						case 10:
							constructHueUI();
							mEditLayout.setVisibility(View.VISIBLE);
							mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
							break;
						case 11:
							constructGrayUI();
							mEditLayout.setVisibility(View.VISIBLE);
							mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
							break;
						case 12:
							mEditLayout.setVisibility(View.VISIBLE);
							mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
							break;
						default:
							mEditLayout.setBackgroundColor(Color.TRANSPARENT);
							break;
					}
				}
			});
		}
		mOkBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//PhotoUtils.saveBitmap(mPhotoEdit.getDisplayImage(), mImgUri.toString(),PhotoUtils.IMAGE_FORMAT_PNG);
				ImageSaveAsyncTask imgSaveTask = new ImageSaveAsyncTask();
				imgSaveTask.execute();
				String txt = "SAVE XIUIMAGE!";
				Toast.makeText(EditorActivity.this, txt, Toast.LENGTH_SHORT).show();
				finish();
				IntentToStartView();
			}
		});
		mCancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
				IntentToStartView();
			}
		});
		mImgView.setClickable(true);
		mImgView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				// TODO Auto-generated method stub
				switch(event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
						mImgView.setImageBitmap(null);
						mImgView.setImageBitmap(mPhotoEdit.getOriginalImage());
						break;
					case MotionEvent.ACTION_UP:
						mImgView.setImageBitmap(null);
						mImgView.setImageBitmap(mPhotoEdit.getDisplayImage());
						break;
					default:
						break;
				}
				return false;
			}
		});
		//mBottomBar.pressButton(mBtnId[0]);
		//constructBCUI();
		mWaitBar = new ProgressBar(this);
		mWaitBar.setBackgroundResource(R.drawable.wait_progressbar);
		params = new LayoutParams(ScaleUtils.scale(150),ScaleUtils.scale(150));
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mImgLayout.addView(mWaitBar, params);
		mWaitBar.setVisibility(View.INVISIBLE);
		mBottomOkBtn.setVisibility(View.VISIBLE);
		mBottomCancelBtn.setVisibility(View.VISIBLE);
		mBottomOkBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				popWindow.setVisibility(View.INVISIBLE);
				mBottomBar.setVisibility(View.VISIBLE);
				mEditLayout.setVisibility(View.INVISIBLE);
				mPhotoEdit.updateImage(true);
				mImgView.setImageBitmap(null);
				mImgView.setImageBitmap(mPhotoEdit.getDisplayImage());
				if(mRotated)
					mRotateNum = 90;
				else
					mRotated = false;
			}
		});
		mBottomCancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				popWindow.setVisibility(View.INVISIBLE);
				mBottomBar.setVisibility(View.VISIBLE);
				mEditLayout.setVisibility(View.INVISIBLE);
				mPhotoEdit.updateImage(false);
				if(mRotated)
				{
					mImgView.setImageBitmap(null);
					mImgView.setImageBitmap(mPhotoEdit.getBaseImage());
					mRotateNum = 90;
					mRotated = false;
				}
				else
				{
					mImgView.setImageBitmap(null);
					mImgView.setImageBitmap(mPhotoEdit.getDisplayImage());
				}


			}
		});
	}
	private void IntentToStartView()
	{
		Intent intent = new Intent(EditorActivity.this,ModelActivity.class);
		startActivity(intent);
	}
	private boolean mRotated = false;
	private void constructSaturationUI()
	{
		mEditLayout.removeAllViews();
		//value txt
		final TextView mValueTxt = new TextView(this);
		mValueTxt.setText(R.string.initial_txt);
		mValueTxt.setTextSize(ScaleUtils.getTextSize(14));
		mValueTxt.setTextColor(Color.WHITE);
		mValueTxt.setPadding(0, 0, 0, 0);
		mValueTxt.setBackgroundResource(R.drawable.edit_bg_seek_value);
		mValueTxt.setGravity(Gravity.CENTER|Gravity.TOP);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//		int v = 1000*mPhotoEdit.getSaturation()/510;
//		params.leftMargin = (GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240))*v/1000+ScaleUtils.scale(83);
		params.topMargin = ScaleUtils.scale(40);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mEditLayout.addView(mValueTxt, params);
		//progress bar
		SeekBar mSaturationBar = new SeekBar(this);
		mSaturationBar.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		mSaturationBar.setMax(1000);
		mSaturationBar.setProgress(500);
		mSaturationBar.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mSaturationBar.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(15),
				ScaleUtils.scale(20), ScaleUtils.scale(15));
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mEditLayout.addView(mSaturationBar, params);
		mSaturationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				// TODO Auto-generated method stub
				int value = seekBar.getProgress();
				LayoutParams paramsStrength = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
				paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
				paramsStrength.topMargin = ScaleUtils.scale(40);
				mValueTxt.setLayoutParams(paramsStrength);
				int res = (value/5);
				mValueTxt.setText(String.valueOf(res-100));//display -100-100
				mImgView.setImageBitmap(null);
				mImgView.setImageBitmap(mPhotoEdit.applySaturation(res*510/200));//change to 0-510
			}
		});
		popWindow.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.INVISIBLE);
	}
	private void constructBCUI()
	{
		mEditLayout.removeAllViews();
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		int seekLayoutHeight = ScaleUtils.scale(220);
		RelativeLayout mSeekLayout = new RelativeLayout(this);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.height = seekLayoutHeight;
		params.width = GlobalDefinitions.SCREEN_WIDTH;
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mEditLayout.addView(mSeekLayout,params);
		RelativeLayout mSeekLayoutA = new RelativeLayout(this);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.width = GlobalDefinitions.SCREEN_WIDTH;
		params.height = seekLayoutHeight/2;
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		mEditLayout.addView(mSeekLayoutA,params);
		RelativeLayout mSeekLayoutB = new RelativeLayout(this);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.width = GlobalDefinitions.SCREEN_WIDTH;
		params.height = seekLayoutHeight/2;
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mEditLayout.addView(mSeekLayoutB,params);
		TextView brightTxt = new TextView(this);
		brightTxt.setTextSize(ScaleUtils.getTextSize(16));
		brightTxt.setPadding(ScaleUtils.scale(10), 0, ScaleUtils.scale(5), 0);
		brightTxt.setGravity(Gravity.CENTER_VERTICAL);
		brightTxt.setText(R.string.bright);
		brightTxt.setTextColor(Color.WHITE);
		params= new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.width = ScaleUtils.scale(130);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		mSeekLayoutA.addView(brightTxt,params);
		TextView contrastTxt = new TextView(this);
		contrastTxt.setTextSize(ScaleUtils.getTextSize(16));
		contrastTxt.setPadding(ScaleUtils.scale(10), 0, ScaleUtils.scale(5), 0);
		contrastTxt.setGravity(Gravity.CENTER_VERTICAL);
		contrastTxt.setText(R.string.contrast);
		contrastTxt.setTextColor(Color.WHITE);
		params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		params.width = ScaleUtils.scale(130);
		mSeekLayoutB.addView(contrastTxt,params);
		final SeekBar mBSeekBar = new SeekBar(this);
		mBSeekBar.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		final int aUint = 200;
		final int bUint = 200;
		mBSeekBar.setMax(aUint*10);
		mBSeekBar.setProgress(aUint*5);
		mBSeekBar.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mBSeekBar.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(5),
				ScaleUtils.scale(20), ScaleUtils.scale(5));
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		params.leftMargin = ScaleUtils.scale(140);
		params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
		mSeekLayoutA.addView(mBSeekBar,params);
		final SeekBar mCSeekBar = new SeekBar(this);
		mCSeekBar.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		mCSeekBar.setMax(bUint*10);
		mCSeekBar.setProgress(bUint*5);
		mCSeekBar.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mCSeekBar.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(5),
				ScaleUtils.scale(20), ScaleUtils.scale(5));
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		params.leftMargin = ScaleUtils.scale(140);
		params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
		mSeekLayoutB.addView(mCSeekBar,params);
		final TextView mBTextView = new TextView(this);
		mBTextView.setText(R.string.initial_txt);
		mBTextView.setTextSize(ScaleUtils.getTextSize(14));
		mBTextView.setPadding(0, 0, 0, 0);
		mBTextView.setTextColor(Color.WHITE);
		mBTextView.setGravity(Gravity.CENTER|Gravity.TOP);
		mBTextView.setBackgroundResource(R.drawable.edit_bg_seek_value);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.leftMargin = (int) (ScaleUtils.scale(123)+(GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240))/2);
		mSeekLayoutA.addView(mBTextView,params);
		final TextView mCTextView = new TextView(this);
		mCTextView.setText(R.string.initial_txt);
		mCTextView.setTextSize(ScaleUtils.getTextSize(14));
		mCTextView.setPadding(0, 0, 0, 0);
		mCTextView.setTextColor(Color.WHITE);
		mCTextView.setGravity(Gravity.CENTER|Gravity.TOP);
		mCTextView.setBackgroundResource(R.drawable.edit_bg_seek_value);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.leftMargin = (int) (ScaleUtils.scale(123)+(GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240))/2);
		mSeekLayoutB.addView(mCTextView,params);
		mBSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				// TODO Auto-generated method stub
				int value = seekBar.getProgress();
				LayoutParams paramsStrength = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
				paramsStrength.leftMargin = (int) (ScaleUtils.scale(123)+s*value/(aUint*10));
				mBTextView.setLayoutParams(paramsStrength);
				s = (value-aUint*5)/10;
				mBTextView.setText(String.valueOf(s));
				mImgView.setImageBitmap(null);
				mImgView.setImageBitmap(mPhotoEdit.applyBC(s, (mCSeekBar.getProgress()-bUint*5)/10));
			}
		});
		mCSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				// TODO Auto-generated method stub
				int value = seekBar.getProgress();
				LayoutParams paramsStrength = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
				paramsStrength.leftMargin = (int) (ScaleUtils.scale(123)+s*value/(bUint*10));
				mCTextView.setLayoutParams(paramsStrength);
				s = (value-bUint*5)/10;
				mCTextView.setText(String.valueOf(s));
				mImgView.setImageBitmap(null);
				mImgView.setImageBitmap(mPhotoEdit.applyBC((mBSeekBar.getProgress()-bUint*5)/10,s));
			}
		});
		popWindow.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.INVISIBLE);
	}
	private void constructColorTempeatureUI()
	{
		mEditLayout.removeAllViews();
		//value txt
		final TextView mValueTxt = new TextView(this);
		mValueTxt.setText(R.string.initial_txt);
		mValueTxt.setTextSize(ScaleUtils.getTextSize(14));
		mValueTxt.setTextColor(Color.WHITE);
		mValueTxt.setPadding(0, 0, 0, 0);
		mValueTxt.setBackgroundResource(R.drawable.edit_bg_seek_value);
		mValueTxt.setGravity(Gravity.CENTER|Gravity.TOP);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.leftMargin = ScaleUtils.scale(83);
		params.topMargin = ScaleUtils.scale(40);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mEditLayout.addView(mValueTxt, params);
		//progress bar
		SeekBar mCorTempeatureBar = new SeekBar(this);
		mCorTempeatureBar.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		mCorTempeatureBar.setMax(1000);
		mCorTempeatureBar.setProgress(500);
		mCorTempeatureBar.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mCorTempeatureBar.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(15),
				ScaleUtils.scale(20), ScaleUtils.scale(15));
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mEditLayout.addView(mCorTempeatureBar, params);

		mCorTempeatureBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				// TODO Auto-generated method stub
				int value = seekBar.getProgress();
				LayoutParams paramsStrength = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
				paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
				paramsStrength.topMargin = ScaleUtils.scale(40);
				mValueTxt.setLayoutParams(paramsStrength);
				int res = (value/5-100);
				mValueTxt.setText(String.valueOf(res));
				mImgView.setImageBitmap(null);
				mImgView.setImageBitmap(mPhotoEdit.applyColorTeamperature(res/2));//change to -100-100
			}
		});
		popWindow.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.INVISIBLE);
	}
	private void constructSharpenUI()
	{
		mEditLayout.removeAllViews();
		//value txt
		final TextView mValueTxt = new TextView(this);
		mValueTxt.setText(R.string.initial_txt);
		mValueTxt.setTextSize(ScaleUtils.getTextSize(14));
		mValueTxt.setTextColor(Color.WHITE);
		mValueTxt.setPadding(0, 0, 0, 0);
		mValueTxt.setBackgroundResource(R.drawable.edit_bg_seek_value);
		mValueTxt.setGravity(Gravity.CENTER|Gravity.TOP);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.leftMargin = ScaleUtils.scale(83);
		params.topMargin = ScaleUtils.scale(40);
		//params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mEditLayout.addView(mValueTxt, params);
		//progress bar
		SeekBar mUSMSharpenBar = new SeekBar(this);
		mUSMSharpenBar.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		mUSMSharpenBar.setMax(1000);
		mUSMSharpenBar.setProgress(0);
		mUSMSharpenBar.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mUSMSharpenBar.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(15),
				ScaleUtils.scale(20), ScaleUtils.scale(15));
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mEditLayout.addView(mUSMSharpenBar, params);

		mUSMSharpenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				mImgView.setImageBitmap(null);
				mImgView.setImageBitmap(mPhotoEdit.applySharpen(Math.abs(seekBar.getProgress()/10)));//change to 0-100
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				// TODO Auto-generated method stub
				int value = seekBar.getProgress();
				LayoutParams paramsStrength = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
				paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
				paramsStrength.topMargin = ScaleUtils.scale(40);
				mValueTxt.setLayoutParams(paramsStrength);
				int res = (value/10);
				mValueTxt.setText(String.valueOf(res));

			}
		});
		popWindow.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.INVISIBLE);
	}
	private void constructLightnessUI()
	{
		mEditLayout.removeAllViews();
		//value txt
		final TextView mValueTxt = new TextView(this);
		mValueTxt.setText(R.string.initial_txt);
		mValueTxt.setTextSize(ScaleUtils.getTextSize(14));
		mValueTxt.setTextColor(Color.WHITE);
		mValueTxt.setPadding(0, 0, 0, 0);
		mValueTxt.setBackgroundResource(R.drawable.edit_bg_seek_value);
		mValueTxt.setGravity(Gravity.CENTER|Gravity.TOP);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.leftMargin = ScaleUtils.scale(83);
		params.topMargin = ScaleUtils.scale(40);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mEditLayout.addView(mValueTxt, params);
		//progress bar
		SeekBar mLightBar = new SeekBar(this);
		mLightBar.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		mLightBar.setMax(1000);
		mLightBar.setProgress(500);
		mLightBar.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mLightBar.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(15),
				ScaleUtils.scale(20), ScaleUtils.scale(15));
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mEditLayout.addView(mLightBar, params);

		mLightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				// TODO Auto-generated method stub
				int value = seekBar.getProgress();
				LayoutParams paramsStrength = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
				paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
				paramsStrength.topMargin = ScaleUtils.scale(40);
				mValueTxt.setLayoutParams(paramsStrength);
				int res = (value/5 - 100);
				mValueTxt.setText(String.valueOf(res));
				mImgView.setImageBitmap(null);
				mImgView.setImageBitmap(mPhotoEdit.applyLightness(res));//change to -100-100
			}
		});
		popWindow.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.INVISIBLE);
	}
	private void constructRotateUI()
	{
		mEditLayout.removeAllViews();
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		int dis = (GlobalDefinitions.SCREEN_WIDTH - 3*ScaleUtils.scale(120))/4;
		final ImageButton roBtn = new ImageButton(this);
		roBtn.setBackgroundResource(R.drawable.edit_rotate_ro);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.width = ScaleUtils.scale(120);
		params.height = ScaleUtils.scale(120);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mEditLayout.addView(roBtn, params);

		roBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//mPhotoEdit.updateImage(true);
//				if(mImgView != null)
//				{
//					mImgView.setImageBitmap(null);
//				}
				mImgView.setImageBitmap(null);
				mRotated = true;
				mImgView.setImageBitmap(mPhotoEdit.applyRotate(mRotateNum));
				mRotateNum+=90;
				if(mRotateNum==360)
					mRotateNum = 0;
			}
		});

		popWindow.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.INVISIBLE);
	}
	private void constructExposureUI()
	{
		mEditLayout.removeAllViews();
		//value txt
		final TextView mValueTxt = new TextView(this);
		mValueTxt.setText(R.string.initial_txt);
		mValueTxt.setTextSize(ScaleUtils.getTextSize(14));
		mValueTxt.setTextColor(Color.WHITE);
		mValueTxt.setPadding(0, 0, 0, 0);
		mValueTxt.setBackgroundResource(R.drawable.edit_bg_seek_value);
		mValueTxt.setGravity(Gravity.CENTER|Gravity.TOP);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//		params.leftMargin = ScaleUtils.scale(83);
		params.topMargin = ScaleUtils.scale(40);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mEditLayout.addView(mValueTxt, params);
		//progress bar
		SeekBar mExposureBar = new SeekBar(this);
		mExposureBar.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		mExposureBar.setMax(1000);
		mExposureBar.setProgress(500);
		mExposureBar.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mExposureBar.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(15),
				ScaleUtils.scale(20), ScaleUtils.scale(15));
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mEditLayout.addView(mExposureBar, params);
		mExposureBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				// TODO Auto-generated method stub
				int value = seekBar.getProgress();
				LayoutParams paramsStrength = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
				paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
				paramsStrength.topMargin = ScaleUtils.scale(40);
				mValueTxt.setLayoutParams(paramsStrength);
				int res = (value/5-100);
				mValueTxt.setText(String.valueOf(res));
				mImgView.setImageBitmap(null);
				mImgView.setImageBitmap(mPhotoEdit.applyExposure(value/10));//change to -100-100
			}
		});
		popWindow.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.INVISIBLE);
	}
	private void constructHighlightUI()
	{
		mEditLayout.removeAllViews();
		//value txt
		final TextView mValueTxt = new TextView(this);
		mValueTxt.setText(R.string.initial_txt);
		mValueTxt.setTextSize(ScaleUtils.getTextSize(14));
		mValueTxt.setTextColor(Color.WHITE);
		mValueTxt.setPadding(0, 0, 0, 0);
		mValueTxt.setBackgroundResource(R.drawable.edit_bg_seek_value);
		mValueTxt.setGravity(Gravity.CENTER|Gravity.TOP);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.leftMargin = ScaleUtils.scale(83);
		params.topMargin = ScaleUtils.scale(40);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mEditLayout.addView(mValueTxt, params);
		//progress bar
		SeekBar mHighlightBar = new SeekBar(this);
		mHighlightBar.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		mHighlightBar.setMax(1000);
		mHighlightBar.setProgress(500);
		mHighlightBar.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mHighlightBar.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(15),
				ScaleUtils.scale(20), ScaleUtils.scale(15));
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mEditLayout.addView(mHighlightBar, params);

		mHighlightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				// TODO Auto-generated method stub
				int value = seekBar.getProgress();
				LayoutParams paramsStrength = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
				paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
				paramsStrength.topMargin = ScaleUtils.scale(40);
				mValueTxt.setLayoutParams(paramsStrength);
				int res = (value/5-100);
				mValueTxt.setText(String.valueOf(res));
				mImgView.setImageBitmap(null);
				mImgView.setImageBitmap(mPhotoEdit.applyHighlight(res));//change to -100-100
			}
		});
		popWindow.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.INVISIBLE);
	}
	private void constructShadowUI()
	{
		mEditLayout.removeAllViews();
		//value txt
		final TextView mValueTxt = new TextView(this);
		mValueTxt.setText(R.string.initial_txt);
		mValueTxt.setTextSize(ScaleUtils.getTextSize(14));
		mValueTxt.setTextColor(Color.WHITE);
		mValueTxt.setPadding(0, 0, 0, 0);
		mValueTxt.setBackgroundResource(R.drawable.edit_bg_seek_value);
		mValueTxt.setGravity(Gravity.CENTER|Gravity.TOP);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.leftMargin = ScaleUtils.scale(83);
		params.topMargin = ScaleUtils.scale(40);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mEditLayout.addView(mValueTxt, params);
		//progress bar
		SeekBar mShadowBar = new SeekBar(this);
		mShadowBar.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		mShadowBar.setMax(1000);
		mShadowBar.setProgress(500);
		mShadowBar.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mShadowBar.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(15),
				ScaleUtils.scale(20), ScaleUtils.scale(15));
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mEditLayout.addView(mShadowBar, params);

		mShadowBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				// TODO Auto-generated method stub
				int value = seekBar.getProgress();
				LayoutParams paramsStrength = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
				paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
				paramsStrength.topMargin = ScaleUtils.scale(40);
				mValueTxt.setLayoutParams(paramsStrength);
				int res = (value/5-100);
				mValueTxt.setText(String.valueOf(res));
				mImgView.setImageBitmap(null);
				mImgView.setImageBitmap(mPhotoEdit.applyShadow(res));//change to -100-100
			}
		});
		popWindow.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.INVISIBLE);
	}
	private void constructMosciaUI()
	{
		mEditLayout.removeAllViews();
		//value txt
		final TextView mValueTxt = new TextView(this);
		mValueTxt.setText(R.string.initial_txt);
		mValueTxt.setTextSize(ScaleUtils.getTextSize(14));
		mValueTxt.setTextColor(Color.WHITE);
		mValueTxt.setPadding(0, 0, 0, 0);
		mValueTxt.setBackgroundResource(R.drawable.edit_bg_seek_value);
		mValueTxt.setGravity(Gravity.CENTER|Gravity.TOP);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.leftMargin = ScaleUtils.scale(83);
		params.topMargin = ScaleUtils.scale(40);
		//params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mEditLayout.addView(mValueTxt, params);
		//progress bar
		SeekBar mHaloBar = new SeekBar(this);
		mHaloBar.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		mHaloBar.setMax(1000);
		mHaloBar.setProgress(0);
		mHaloBar.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mHaloBar.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(15),
				ScaleUtils.scale(20), ScaleUtils.scale(15));
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mEditLayout.addView(mHaloBar, params);

		mHaloBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				// TODO Auto-generated method stub
				int value = seekBar.getProgress();
				LayoutParams paramsStrength = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
				paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
				paramsStrength.topMargin = ScaleUtils.scale(40);
				mValueTxt.setLayoutParams(paramsStrength);
				int res = (value/10);
				mValueTxt.setText(String.valueOf(res));
				mImgView.setImageBitmap(null);
				mImgView.setImageBitmap(mPhotoEdit.applyMoscia(res));//change to 0-100
			}
		});
		popWindow.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.INVISIBLE);
	}
	private void constructHueUI()
	{
		mEditLayout.removeAllViews();
		//value txt
		final TextView mValueTxt = new TextView(this);
		mValueTxt.setText(R.string.initial_txt);
		mValueTxt.setTextSize(ScaleUtils.getTextSize(14));
		mValueTxt.setTextColor(Color.WHITE);
		mValueTxt.setPadding(0, 0, 0, 0);
		mValueTxt.setBackgroundResource(R.drawable.edit_bg_seek_value);
		mValueTxt.setGravity(Gravity.CENTER|Gravity.TOP);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.leftMargin = ScaleUtils.scale(83);
		params.topMargin = ScaleUtils.scale(40);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mEditLayout.addView(mValueTxt, params);
		//progress bar
		SeekBar mHueBar = new SeekBar(this);
		mHueBar.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		mHueBar.setMax(1000);
		mHueBar.setProgress(500);
		mHueBar.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mHueBar.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(15),
				ScaleUtils.scale(20), ScaleUtils.scale(15));
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mEditLayout.addView(mHueBar, params);

		mHueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				int value = seekBar.getProgress();
				int res = (value/5-100);
				mImgView.setImageBitmap(null);
				mImgView.setImageBitmap(mPhotoEdit.applyHue(res * 9 / 5));//change to -180-180
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				// TODO Auto-generated method stub
				int value = seekBar.getProgress();
				LayoutParams paramsStrength = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
				paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
				paramsStrength.topMargin = ScaleUtils.scale(40);
				mValueTxt.setLayoutParams(paramsStrength);
				int res = (value/5-100);
				mValueTxt.setText(String.valueOf(res * 9 / 5));
			}
		});
		popWindow.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.INVISIBLE);
	}
	private void constructGrayUI()
	{
		mEditLayout.removeAllViews();
		//value txt
		final TextView mValueTxt = new TextView(this);
		mValueTxt.setText(R.string.initial_txt);
		mValueTxt.setTextSize(ScaleUtils.getTextSize(14));
		mValueTxt.setTextColor(Color.WHITE);
		mValueTxt.setPadding(0, 0, 0, 0);
		mValueTxt.setBackgroundResource(R.drawable.edit_bg_seek_value);
		mValueTxt.setGravity(Gravity.CENTER|Gravity.TOP);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.leftMargin = ScaleUtils.scale(83);
		params.topMargin = ScaleUtils.scale(40);
		//params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mEditLayout.addView(mValueTxt, params);
		//progress bar
		SeekBar mGrayBar = new SeekBar(this);
		mGrayBar.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		mGrayBar.setMax(1000);
		mGrayBar.setProgress(0);
		mGrayBar.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mGrayBar.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(15),
				ScaleUtils.scale(20), ScaleUtils.scale(15));
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mEditLayout.addView(mGrayBar, params);

		mGrayBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				// TODO Auto-generated method stub
				int value = seekBar.getProgress();
				LayoutParams paramsStrength = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
				paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
				paramsStrength.topMargin = ScaleUtils.scale(40);
				mValueTxt.setLayoutParams(paramsStrength);
				int res = (value/10);
				mValueTxt.setText(String.valueOf(res));
				mImgView.setImageBitmap(null);
				mImgView.setImageBitmap(mPhotoEdit.applyGray(res));//change to 0-100
			}
		});
		popWindow.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.INVISIBLE);
	}

	class ImageSaveAsyncTask extends AsyncTask<Void,Integer,Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try{
				String ImgPath = ImageUtils.getImageSavePath(GlobalDefinitions.TAG);
				PhotoUtils.saveBitmap(mPhotoEdit.getDisplayImage(), ImgPath,PhotoUtils.IMAGE_FORMAT_JPG, EditorActivity.this);
				GlobalDefinitions.IMAGE_EDIT_DONE = true;

			}
			catch(Exception e)
			{
				return false;
			}
			return true;
		}
		@Override
		protected void onPostExecute(Boolean result)
		{
			mWaitBar.setVisibility(View.INVISIBLE);
			if(result)
				finish();
		}
		@Override
		protected void onPreExecute()
		{
			mWaitBar.setVisibility(View.VISIBLE);
		}
		@Override
		protected void onProgressUpdate(Integer... values)
		{
			mWaitBar.setProgress(values[0]);
		}

	}


}
