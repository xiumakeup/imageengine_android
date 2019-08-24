
package xiusdk.camera;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.xiusdk.beautycamera.R;
import com.xiusdk.imageengine.ImageEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import xiusdk.tools.GlobalDefinitions;
import xiusdk.tools.ImageUtils;
import xiusdk.tools.ScaleUtils;


public class CameraActivity extends Activity implements Callback,
PreviewCallback, Camera.PictureCallback,AutoFocusCallback {
	// Camera Views
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private Camera mCamera;
	private GLSurfaceView glSurfaceView;
	private CameraGLRender mRender;
	private ImageView mSwitchCameraBtn = null;
	private boolean hasSurface = false;
	private int WIDTH_RES = 1280;
	private int HEIGHT_RES = 720;
	private Handler mHandler = null;
	private TextView mSkinWhiteTxt = null;
	private TextView mSoftSkinTxt = null;
	private SeekBar mSkinSoftSB = null;
	private SeekBar mSkinSB = null;
	private TextView pixTxt = null;
	public static TextView fpsTxt = null;
	private RelativeLayout main = null;
	private ImageButton[] filterBtn = null;
	private RelativeLayout mFilterLayout = null;
	private HorizontalScrollView mFilterScrollView = null;
	private LinearLayout mFilterScrollLayout = null;
	private RelativeLayout barlayout = null;
	private int[] filterImgId = null;
	private String[] filterText = null;
	private ImageView captureBtn = null;
	private ImageView mCameraFocusImg = null;
	private ImageView mAlbumBtn = null;
	private ImageView mFilterBtn = null;
	private String mImgPath = null;
	private int mFilterNum = 0;
	private int CAMERA_ID = 1000;
	private int CAMERA_FILTER_ID = 1100;
	private RelativeLayout curFilterLayout = null;
	private int cameraId = CameraInfo.CAMERA_FACING_FRONT;
	private boolean permission = false;
	private ImageEngine mImageEngine = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		GlobalDefinitions.SCREEN_PROTRAIT = true;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		int permission = ContextCompat.checkSelfPermission( this.getApplicationContext(), Manifest.permission.CAMERA);
		if( permission!= android.content.pm.PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission( this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
			this.permission = false;
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
					1);
		} else {
			this.permission = true;
			initialUI();
			ResponseUI();
		}
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (surfaceHolder != null) {
			if (!hasSurface)
				surfaceHolder.addCallback(this);
			else {
				startCamera();
			}
			glSurfaceView.onResume();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		stopCamera();
		if( glSurfaceView != null) {
			glSurfaceView.onPause();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if( 1 == requestCode)
		{
			for( int i = 0; i < grantResults.length; i++)
			{
				if(grantResults[i] != android.content.pm.PackageManager.PERMISSION_GRANTED)
				{
					return;
				}
			}
			this.permission = true;
			initialUI();
			ResponseUI();
			startCamera();
		}
	}

	private void initialUI()
	{
		ScaleUtils.scaleInit(this,1280,720,320);
		filterBtn = new ImageButton[37];
		setContentView(R.layout.mainactivity);

		main = (RelativeLayout)findViewById(R.id.glview_layout);
		glSurfaceView = (GLSurfaceView) findViewById(R.id.glview);
		surfaceView = (SurfaceView) findViewById(R.id.surface);
		mSwitchCameraBtn = (ImageView)findViewById(R.id.camera_switch);
		mSwitchCameraBtn.setVisibility(View.VISIBLE);
		mCameraFocusImg = (ImageView)findViewById(R.id.cameraFocus);
		mAlbumBtn = (ImageView)findViewById(R.id.camera_album);
		mFilterBtn = (ImageView)findViewById(R.id.camera_filter);
		captureBtn = (ImageView)findViewById(R.id.camera);
		barlayout = (RelativeLayout)findViewById(R.id.barlayout);
		pixTxt = (TextView)findViewById(R.id.pixTxt);
		fpsTxt = (TextView)findViewById(R.id.fpsTxt);
		mSkinSB = (SeekBar)findViewById(R.id.skBar);
		mSkinSB.setProgress(GlobalDefinitions.whiteRatio);
		mSkinWhiteTxt = (TextView)findViewById(R.id.skinWhiteTxt);
		mSkinWhiteTxt.setText("whiten(40)");
		mSkinWhiteTxt.setTextColor(Color.WHITE);
		mSkinWhiteTxt.setTextSize(18);
		mSkinWhiteTxt.setPadding(0, 0, 0, 0);

		mSkinSB.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		mSkinSB.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mSkinSB.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(15),ScaleUtils.scale(20), ScaleUtils.scale(15));

		mSkinSoftSB = (SeekBar)findViewById(R.id.softskinBar);
		mSkinSoftSB.setProgress(GlobalDefinitions.softRatio);			
		mSkinSoftSB.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
		mSkinSoftSB.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
		mSkinSoftSB.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(15),ScaleUtils.scale(20), ScaleUtils.scale(15));



		mSoftSkinTxt  = (TextView)findViewById(R.id.softskinTxt);
		mSoftSkinTxt.setText("beauty(80)");
		//mSoftSkinTxt.setText("");
		mSoftSkinTxt.setTextColor(Color.WHITE);
		mSoftSkinTxt.setTextSize(18);
		mSkinWhiteTxt.setPadding(0, 0, 0, 0);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mRender = new CameraGLRender(this);			
		fpsTxt.setVisibility(View.INVISIBLE);
		pixTxt.setVisibility(View.INVISIBLE);
		
		mSkinSB.setVisibility(View.INVISIBLE);
		mSkinSoftSB.setVisibility(View.INVISIBLE);
		mSoftSkinTxt.setVisibility(View.INVISIBLE);
		mSkinWhiteTxt.setVisibility(View.INVISIBLE);

		glSurfaceView.setEGLContextClientVersion(2);
		glSurfaceView.setRenderer(mRender);
		glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		glSurfaceView.setZOrderMediaOverlay(true);

		mImageEngine = new ImageEngine(this);
		try {
			openCamera();

			if (mCamera != null) {
				Size size = mCamera.getParameters().getPreviewSize();
				WIDTH_RES = size.width;
				HEIGHT_RES = size.height;
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}	
		if(mHandler!=null)
		{
			mHandler.removeMessages(0);
			mHandler = null;
		}
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				case 0:
					glSurfaceView.setVisibility(View.VISIBLE);
					break;
				default:
					break;
				}
			}
		};	
		////Filter
		filterText = new String[]{getResources().getString(R.string.yuantu),getResources().getString(R.string.qingxi),getResources().getString(R.string.baixi),getResources().getString(R.string.lengyan),getResources().getString(R.string.bingling),getResources().getString(R.string.dianya),getResources().getString(R.string.landiao),getResources().getString(R.string.luoli),getResources().getString(R.string.luokk),
				getResources().getString(R.string.nuanhuang),getResources().getString(R.string.qingchun),getResources().getString(R.string.rixi),getResources().getString(R.string.rouguang),getResources().getString(R.string.tianmei),getResources().getString(R.string.weimei),getResources().getString(R.string.liren),getResources().getString(R.string.guodong),getResources().getString(R.string.huayan),getResources().getString(R.string.luozhuang),
				getResources().getString(R.string.nenhong),getResources().getString(R.string.yishu),getResources().getString(R.string.meifu),getResources().getString(R.string.hongrun),getResources().getString(R.string.jingmei),getResources().getString(R.string.ziyun),getResources().getString(R.string.danya),getResources().getString(R.string.qingxin),getResources().getString(R.string.roumei),getResources().getString(R.string.wennuan),
				getResources().getString(R.string.nnyangguang),getResources().getString(R.string.tianmeikeren),getResources().getString(R.string.abaose),getResources().getString(R.string.langman),getResources().getString(R.string.qingtou),getResources().getString(R.string.zhenbai),getResources().getString(R.string.ziran),getResources().getString(R.string.nuannuan)};
		mFilterNum = filterText.length;
		filterImgId = new int[]{R.drawable.filter_bac1,R.drawable.f_2qingxi,R.drawable.f_3baixi,R.drawable.f_4lengyan,R.drawable.f_5bingling,R.drawable.f_6dianya,R.drawable.f_7landiao,R.drawable.f_8luoli,R.drawable.f_9luokek,R.drawable.f_10nuanhuang,R.drawable.f_11qingchun,R.drawable.f_12rixi,R.drawable.f_13rouguang,R.drawable.f_14tianmei,R.drawable.f_15weimei,R.drawable.f_16liri,R.drawable.f_17guodong,R.drawable.f_18huayan,R.drawable.f_19luozhuang,
				R.drawable.f_20nenhong,R.drawable.f_21yishu,R.drawable.f_22meifu,R.drawable.f_23hongrun,R.drawable.f_24jingmei,R.drawable.f_25ziyun,R.drawable.f_26danya,R.drawable.f_27qingxin,R.drawable.f_28roumei,R.drawable.f_29nuanchun,R.drawable.f_30nuannuanyangguang,R.drawable.f_31keren,R.drawable.f_32abaose,R.drawable.f_33langman,R.drawable.f_34qingtou,R.drawable.f_35zhenbai,R.drawable.f_36ziran,R.drawable.f_1nuannuan};
		mFilterLayout = new RelativeLayout(this);
		mFilterLayout.setId(CAMERA_ID);
		mFilterLayout.setBackgroundColor(Color.argb(128, 208,120,144));
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,ScaleUtils.scaleX(138));
		//params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ABOVE,barlayout.getId());

		main.addView(mFilterLayout,params);
		mFilterScrollView = new HorizontalScrollView(this);
		mFilterScrollView.setHorizontalScrollBarEnabled(false);
		mFilterScrollView.setFillViewport(true);
		mFilterScrollView.setId(CAMERA_ID+1);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mFilterLayout.addView(mFilterScrollView,params);
		mFilterScrollLayout = new LinearLayout(this);
		mFilterScrollLayout.setHorizontalScrollBarEnabled(true);
		mFilterScrollLayout.setId(CAMERA_ID+2);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mFilterScrollView.addView(mFilterScrollLayout,params);
		//mFilterScrollLayout.removeAllViews();   
		Bitmap src = BitmapFactory.decodeResource(this.getResources(), R.drawable.filter_bac1);
		for(int i = 0; i < mFilterNum; i++)
		{
			RelativeLayout tempLayout = new RelativeLayout(this);
			tempLayout.setId(CAMERA_FILTER_ID + i);
			tempLayout.setBackgroundResource(0);
			tempLayout.setBackgroundColor(Color.TRANSPARENT);
			params = new RelativeLayout.LayoutParams(ScaleUtils.scale(104),
					ScaleUtils.scale(138));				
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			mFilterScrollLayout.addView(tempLayout, params);      
			ImageView tempImgView = new ImageView(this);
			tempImgView.setScaleType(ScaleType.CENTER);
			tempImgView.setBackgroundColor(Color.BLACK);
			tempImgView.setScaleType(ScaleType.FIT_CENTER);

			tempImgView.setImageResource(filterImgId[i]);
			//tempImgView.setImageResource(R.drawable.filter_bac);
			params = new RelativeLayout.LayoutParams(ScaleUtils.scale(100),
					ScaleUtils.scale(134));
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			tempLayout.addView(tempImgView, params);

			TextView txtView = new TextView(this);

			txtView.setText(filterText[i]);
			txtView.setTextSize(ScaleUtils.getTextSize(7));
			txtView.setTextColor(Color.WHITE);
			txtView.setGravity(Gravity.CENTER);
			txtView.setPadding(0, 0, 0, 0);
			txtView.setBackgroundColor(Color.argb(128, 128, 128, 128));
			params = new RelativeLayout.LayoutParams(ScaleUtils.scale(100),
					ScaleUtils.scale(27));
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.bottomMargin = ScaleUtils.scale(0);
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			tempLayout.addView(txtView, params);
			final int id = CAMERA_FILTER_ID + i;
			final int filterID = i;
			tempLayout.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(curFilterLayout!=null)
					{
						curFilterLayout.setBackgroundColor(Color.TRANSPARENT);
					}
					curFilterLayout = (RelativeLayout)findViewById(id);
					curFilterLayout.setBackgroundColor(Color.GREEN);
					GlobalDefinitions.filterId = filterID;
				}
			});
		}   
		curFilterLayout = (RelativeLayout)findViewById(CAMERA_FILTER_ID);
		curFilterLayout.performClick();
		src.recycle();

		captureBtn.setClickable(true);
		mCameraFocusImg.setVisibility(View.INVISIBLE);
		mFilterBtn.setClickable(true);
		mAlbumBtn.setClickable(true);
		barlayout.setVisibility(View.INVISIBLE);
		mFilterLayout.setVisibility(View.INVISIBLE);
		captureBtn.setVisibility(View.VISIBLE);


		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			//
			File sdcardDir =Environment.getExternalStorageDirectory();
			String path=sdcardDir.getPath()+"/XIUIMAGE";
			File path1 = new File(path);
			if (!path1.exists()) {
				path1.mkdirs();
			}
		}
	}
	private void ResponseUI()
	{
		glSurfaceView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(barlayout.getVisibility()==View.VISIBLE)
				{
					barlayout.setVisibility(View.INVISIBLE);
					mFilterLayout.setVisibility(View.INVISIBLE);
					captureBtn.setVisibility(View.VISIBLE);
				}
			}
		});

		mFilterBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(barlayout.getVisibility()==View.VISIBLE)
				{
					barlayout.setVisibility(View.INVISIBLE);
					mFilterLayout.setVisibility(View.INVISIBLE);
					captureBtn.setVisibility(View.VISIBLE);
				}
				else
				{
					barlayout.setVisibility(View.VISIBLE);
					mFilterLayout.setVisibility(View.VISIBLE);
					captureBtn.setVisibility(View.INVISIBLE);
				}
			}
		});

		mAlbumBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		captureBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);  

				//if(!GlobalDefinitions.CAMERA_FRONT)
				if(cameraId == CameraInfo.CAMERA_FACING_BACK)
				{
					mCameraFocusImg.setVisibility(View.VISIBLE);
					mCamera.autoFocus(CameraActivity.this);
					mCameraFocusImg.setVisibility(View.INVISIBLE);
				}
				else
					mCamera.takePicture(mShutterCallback, null, CameraActivity.this);	
			}
		});

		mSwitchCameraBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(Camera.getNumberOfCameras() > 1 )
				{
					if( cameraId == CameraInfo.CAMERA_FACING_BACK)
					{
						cameraId = CameraInfo.CAMERA_FACING_FRONT;
					}
					else
					{
						cameraId = CameraInfo.CAMERA_FACING_BACK;
					}

					startCamera();
				}

			}
		});

		mSkinSB.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				GlobalDefinitions.whiteRatio = arg0.getProgress();
				mSkinWhiteTxt.setText("whiten("+String.valueOf(GlobalDefinitions.whiteRatio)+")");
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

		});

		mSkinSoftSB.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				GlobalDefinitions.softRatio = arg0.getProgress();
				mSoftSkinTxt.setText("beauty("+String.valueOf(GlobalDefinitions.softRatio)+")");
				//mSoftSkinTxt.setText("");
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

		});
	}
	//private int fpsCount = 0;
	@Override
	public  void onPreviewFrame(byte[] data, Camera camera) {
		mRender.onPreviewFrame(data, camera);
		glSurfaceView.requestRender();
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {				
			openCamera();
			if (mCamera != null) {	
				//Camera.Size size= mCamera.getParameters().getPreviewSize();


				setupCamera();
				mCamera.setPreviewCallbackWithBuffer(this);	
				mCamera.setPreviewDisplay(surfaceHolder);
				mCamera.startPreview();	

				hasSurface = true;
			}
		} catch (Exception e) {
		}
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		hasSurface = false;
	}	

	private void openCamera()
	{
		if(null == mCamera)
		{
			try {
				mCamera = Camera.open(cameraId);
			} catch (Exception e) {
				// sometimes TV needs it
				mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
				cameraId = CameraInfo.CAMERA_FACING_BACK;
			}

			if (mCamera != null)
			{
				Camera.Parameters parameters = mCamera.getParameters();				
				Size preivewSize = getOptimalPreviewSize(WIDTH_RES, HEIGHT_RES);

				parameters.setPreviewSize(preivewSize.width, preivewSize.height);
				WIDTH_RES = preivewSize.width;
				HEIGHT_RES = preivewSize.height;
				String fbl = "分辨率(" + preivewSize.width + "x" + preivewSize.height + ")";
				pixTxt.setText(fbl);

				try {
					mCamera.setParameters(parameters);
				} catch (Exception e) {
					parameters = mCamera.getParameters();
				}					

				try {
					parameters.setPreviewFormat(ImageFormat.NV21);
					mCamera.setParameters(parameters);
				} catch (Exception e) {
				}

				//mCamera.setPreviewCallback(this);
			}
		}
	}

	private void setupCamera()
	{
		Camera.Size size = mCamera.getParameters().getPreviewSize();
		mCamera.addCallbackBuffer( new byte[size.width*size.height*3/2]);
		mCamera.addCallbackBuffer( new byte[size.width*size.height*3/2]);
		mCamera.addCallbackBuffer( new byte[size.width*size.height*3/2]);

		int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch(rotation)
		{
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		case Surface.ROTATION_0:
		default:
			degrees = 0;
			break;
		}
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		if( cameraId == CameraInfo.CAMERA_FACING_FRONT )
		{
			degrees = (info.orientation + degrees)%360;
		}
		else
		{
			degrees = (info.orientation + 360 - degrees)%360;
		}
		int orientation = 0;
		switch(degrees)
		{
		case 90:
			orientation = 1;
			break;
		case 180:
			orientation = 2;
			break;
		case 270:
			orientation = 3;
			break;
		default:
			orientation = 0;
			break;
		}
		mRender.setupCamera(orientation, cameraId == CameraInfo.CAMERA_FACING_FRONT);
	}



	private void startCamera()
	{
		if(!this.permission) {
			return;
		}

		stopCamera();
		openCamera();
		if (mCamera != null) 
		{										
			try {					
				mCamera.setPreviewCallbackWithBuffer(CameraActivity.this);
				setupCamera();
				mCamera.setPreviewDisplay(surfaceHolder);
				mCamera.startPreview();
			} catch (IOException e) {
				e.printStackTrace();
			}
			hasSurface = true;
		}
	}

	private void stopCamera()
	{
		if( mCamera != null)
		{
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;				
		}

	}



	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		boolean sucess = false;
		if(null != data){  
			Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);

			mImgPath = ImageUtils.getImageSavePath(GlobalDefinitions.TAG);
			Bitmap temp = mImageEngine.XIUSDK_SoftSkin(bm, GlobalDefinitions.softRatio, GlobalDefinitions.whiteRatio);
			bm.recycle();
			bm = null;
			File file = new File(mImgPath);
			FileOutputStream  fileOutStream=null;
			try {
				fileOutStream=new FileOutputStream(file);
				temp.compress(CompressFormat.JPEG, 100, fileOutStream);
				fileOutStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			temp.recycle();
			temp = null;

			try {
				String fileName = String.copyValueOf(mImgPath.toCharArray(), mImgPath.length()-18, 18);
				MediaStore.Images.Media.insertImage(this.getContentResolver(),
						file.getAbsolutePath(), fileName, null);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + mImgPath)));
			camera.stopPreview();  
			sucess = true;
		}  
		//camera.startPreview();   
		camera.setPreviewCallback(CameraActivity.this);
		try {
			camera.setPreviewDisplay(surfaceHolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		camera.startPreview();
		mCameraFocusImg.setImageResource(R.drawable.focus_normal);
		if(sucess)
		{
			finish();
			IntentToSaveView(mImgPath);

		}
	}
	ShutterCallback mShutterCallback = new ShutterCallback()   
	{  
		public void onShutter() {  
			// TODO Auto-generated method stub  
			Log.i(GlobalDefinitions.TAG, "myShutterCallback:onShutter...");  
		}  
	}; 

	private void IntentToSaveView(String path)
	{
		if(null!=path)
		{
			Intent intent = new Intent(CameraActivity.this,SaveActivity.class);
			intent.setData(Uri.parse(path));
			startActivity(intent); 
		}
	}

	private Size getOptimalPreviewSize(int w, int h) {
		Camera.Parameters params = mCamera.getParameters();
		List<Size> sizes = params.getSupportedPreviewSizes();
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}


	@Override
	public void onAutoFocus(boolean sucess, Camera camera) {
		// TODO Auto-generated method stub
		if(sucess)
		{
			mCameraFocusImg.setImageResource(R.drawable.focus_success);
			mCamera.takePicture(mShutterCallback, null, CameraActivity.this);				
		}
		else
		{
			mCameraFocusImg.setImageResource(R.drawable.focus_failed);
		}
	}

}

