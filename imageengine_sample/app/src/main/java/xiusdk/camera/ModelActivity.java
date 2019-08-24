package xiusdk.camera;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xiusdk.beautycamera.R;

import xiusdk.beautyface.BeautyfaceActivity;
import xiusdk.edit.EditorActivity;
import xiusdk.edit.FilterActivity;
import xiusdk.tools.GlobalDefinitions;
import xiusdk.tools.ScaleUtils;


public class ModelActivity  extends Activity{
	private RelativeLayout main = null;
	private ImageView mCameraBtn = null;
	private ImageView mEditBtn = null;
	private ImageView mFilterBtn = null;
	private ImageView mBeautyBtn = null;
	
	private ImageView mIcon = null;
	private int modelId = 6000;
	private final int RESULT_LOAD_IMAGE = 0;
	private String mImgPath = null;
	private int chooseModel = 0;//0-edit,1-filter,2-beauty
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		main = new RelativeLayout(this);
		main.setBackgroundResource(R.drawable.model_bac);
		setContentView(main);
		ScaleUtils.scaleInit(this,1280,720,320);
		initialUI();
		ResponseUI();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(GlobalDefinitions.TAG, "--CameraActivity onResume");

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	@Override 
	public void onBackPressed() { 
	super.onBackPressed(); 
	    finish();
	} 
	private void initialUI()
	{
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ScaleUtils.scale(200),ScaleUtils.scale(200));
		mIcon = new ImageView(this);
		mIcon.setId(modelId);
		mIcon.setImageResource(R.drawable.model_icon);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.topMargin = ScaleUtils.scaleX(150);
		main.addView(mIcon,params);
		
		mCameraBtn = new ImageView(this);
		mCameraBtn.setId(modelId+1);
		mCameraBtn.setClickable(true);
		mCameraBtn.setBackgroundResource(R.drawable.model_camerabtn);
		params = new RelativeLayout.LayoutParams(ScaleUtils.scale(230),ScaleUtils.scale(230));
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.leftMargin = ScaleUtils.scaleX(100);
		params.addRule(RelativeLayout.BELOW, mIcon.getId());
		params.topMargin = ScaleUtils.scaleX(50);
		main.addView(mCameraBtn,params);
		
		mEditBtn = new ImageView(this);
		mEditBtn.setId(modelId+2);
		mEditBtn.setClickable(true);
		mEditBtn.setBackgroundResource(R.drawable.model_editbtn);
		params = new RelativeLayout.LayoutParams(ScaleUtils.scale(230),ScaleUtils.scale(230));
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.rightMargin = ScaleUtils.scaleX(100);
		params.addRule(RelativeLayout.BELOW, mIcon.getId());
		params.topMargin = ScaleUtils.scaleX(50);
		main.addView(mEditBtn,params);
		
		mFilterBtn = new ImageView(this);
		mFilterBtn.setId(modelId+3);
		mFilterBtn.setClickable(true);
		mFilterBtn.setBackgroundResource(R.drawable.model_filterbtn);
		params = new RelativeLayout.LayoutParams(ScaleUtils.scale(230),ScaleUtils.scale(230));
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.leftMargin = ScaleUtils.scaleX(100);
		params.addRule(RelativeLayout.BELOW, mCameraBtn.getId());
		params.topMargin = ScaleUtils.scaleX(50);
		main.addView(mFilterBtn,params);
		
		mBeautyBtn= new ImageView(this);
		mBeautyBtn.setId(modelId+4);
		mBeautyBtn.setClickable(true);
		mBeautyBtn.setBackgroundResource(R.drawable.model_beautyfacebtn);
		params = new RelativeLayout.LayoutParams(ScaleUtils.scale(230),ScaleUtils.scale(230));
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.rightMargin = ScaleUtils.scaleX(100);
		params.addRule(RelativeLayout.BELOW, mEditBtn.getId());
		params.topMargin = ScaleUtils.scaleX(50);
		main.addView(mBeautyBtn,params);
	}
	private void ResponseUI()
	{
		mCameraBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				intentToCamera();
			}
		});
		mEditBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				chooseModel = 0;
				photoCrop();
			}
		});
		mFilterBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				chooseModel = 1;
				photoCrop();
			}
		});
		mBeautyBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				chooseModel = 2;
				photoCrop();
			}
		});
	}
	private void intentToCamera()
	{
		Intent intent = new Intent(ModelActivity.this,CameraActivity.class);
		startActivity(intent);
		//finish();
	}
	private void IntentToView(String path)
	{
		switch(chooseModel)
		{
		case 0:
			finish();
			IntentToEditView(path);
			break;
		case 1:
			finish();
			IntentToFilterView(path);
			break;
		case 2:
			finish();
			IntentToBeautyfaceView(path);
			break;
		default:
			break;
		}
	}
	private void IntentToBeautyfaceView(String path)
	{
		if(null!=path)
		{
			Intent intent = new Intent(ModelActivity.this,BeautyfaceActivity.class);
		    intent.setData(Uri.parse(path));
	 	    startActivity(intent); 
		}
	}
	 private void IntentToEditView(String path)
		{
			if(null!=path)
			{
				Intent intent = new Intent(ModelActivity.this,EditorActivity.class);
			    intent.setData(Uri.parse(path));
		 	    startActivity(intent); 
			}
		}
	 private void IntentToFilterView(String path)
		{
			if(null!=path)
			{
				Intent intent = new Intent(ModelActivity.this,FilterActivity.class);
			    intent.setData(Uri.parse(path));
		 	    startActivity(intent); 
			}
		}
	    public void photoCrop() {
			GlobalDefinitions.IMAGE_SAVE_DONE = false;     	
	      	    Intent intent = new Intent(
                 Intent.ACTION_PICK,
                 android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		        startActivityForResult(intent, RESULT_LOAD_IMAGE);
		        }

		 @Override
		    protected void onActivityResult(int requestCode, int resultCode, Intent data)
		    {
		    	super.onActivityResult(requestCode, resultCode, data);
		    	switch (requestCode)
		    	{
		    	case RESULT_LOAD_IMAGE:
		    		if (resultCode == RESULT_OK) {
		    			GlobalDefinitions.IMAGE_SAVE_DONE = true;
		    			GlobalDefinitions.IMAGE_OPEN_ALBUM = true;
		    			Uri selectedImage = data.getData();
		    			 String[] filePathColumn = { MediaStore.Images.Media.DATA };	    			 
		    	         Cursor cursor = getContentResolver().query(selectedImage,
		    	                    filePathColumn, null, null, null);
		    	         if(cursor!=null)
		    	         {
		    	        	 cursor.moveToFirst();	
		    	        	 int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			    	         String picturePath = cursor.getString(columnIndex);
			    	         cursor.close();
			    	         mImgPath = picturePath;
		    	         }
		    	         else
		    	        	 mImgPath = selectedImage.getPath();		    	           
		         		IntentToView(mImgPath);		         		
		    		}
		         	break;
		        default:
		        	mImgPath = null;
		            break;
		    	}
		    	

		    }
}
