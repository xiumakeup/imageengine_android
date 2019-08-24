package xiusdk.beautyface;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xiusdk.beautycamera.R;
import com.xiusdk.ynfacedetect.YNFace101;
import com.xiusdk.ynfacedetect.YNFaceDetect;

import xiusdk.camera.ModelActivity;
import xiusdk.tools.BottomView;
import xiusdk.tools.GlobalDefinitions;
import xiusdk.tools.ImageUtils;
import xiusdk.tools.PhotoUtils;
import xiusdk.tools.ScaleUtils;
import xiusdk.tools.SwitchButton;
import xiusdk.tools.SwitchButton.OnChangeListener;
import xiusdk.tools.TitleView;


public class BeautyfaceActivity extends Activity {
    private RelativeLayout main = null;
    private RelativeLayout mEditLayout = null;
    private BottomView mBottomBar = null;
    private ImageView mImgView = null;
    private RelativeLayout popWindow = null;
    private FaceBeauty mFaceEditor = null;

    private boolean faceDetectDone = false;
    private int[] featurePointsAll = new int[204];

    // the source image
    private Bitmap srcBitmap = null;
    // the current result image
    private int width = 0;
    private int height = 0;

    private double faceCenterX = 0, faceCenterY = 0;


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
        intialEngine();

    }
    private void intialEngine()
    {
        Intent intent = getIntent();
        Uri mImgUri = intent.getData();
        if(null!= mImgUri)
        {
            mImgView.setImageBitmap(null);
            mFaceEditor = new FaceBeauty(this, mImgUri.toString());
            mImgView.setImageBitmap(mFaceEditor.getDisplayImage());
            width = mFaceEditor.getWidth();
            height = mFaceEditor.getHeight();
            //face detection
            Options options = new Options();
            options.inJustDecodeBounds = true;
            // path of image
            String picturePath = mImgUri.toString();

            //else
            options.inSampleSize = 1;
            options.inSampleSize = Math.max(1, (int)Math.ceil(Math.max((double)options.outWidth / 1024f, (double)options.outHeight / 1024f)));
            options.inJustDecodeBounds = false;
            srcBitmap = BitmapFactory.decodeFile(picturePath, options);

            FaceDetection();
        }
        else
        {
            mImgView.setImageBitmap(null);
        }

    }
    private void constructTitleBottomUI()
    {
        LayoutParams param = new LayoutParams(LayoutParams.FILL_PARENT,ScaleUtils.scale(100));
        TitleView mTitleBar = new TitleView(this, GlobalDefinitions.SCREEN_WIDTH, ScaleUtils.scale(100), R.drawable.title_back, R.drawable.title_ok, getString(R.string.beautyface_title));
        mTitleBar.setId(GlobalDefinitions.ID_TITLE);
        param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        //mTitleBar.setBackground(246,90,175);
        main.addView(mTitleBar,param);
        String[] names = new String[]{getString(R.string.bottom_mopi),getString(R.string.bottom_meibai),getString(R.string.bottom_dayan),getString(R.string.bottom_shoulian),getString(R.string.bottom_qudou),getString(R.string.bottom_quyandai),getString(R.string.bottom_liangyan),getString(R.string.bottom_gaobiliang)};
        param = new LayoutParams(LayoutParams.FILL_PARENT,ScaleUtils.scale(110));
        mBottomBar = new BottomView(this,GlobalDefinitions.SCREEN_WIDTH,ScaleUtils.scale(110),names,ScaleUtils.getTextSize(18));
        mBottomBar.setId(GlobalDefinitions.ID_BOTTOM);
        param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        main.addView(mBottomBar,param);

        popWindow = new RelativeLayout(this);
        popWindow.setId(GlobalDefinitions.ID_BOTTOM_POPWINDOW);
        popWindow.setBackgroundResource(R.drawable.edit_bottombar_bg);
        param = new LayoutParams(LayoutParams.FILL_PARENT,ScaleUtils.scale(110));
        param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        popWindow.setVisibility(View.INVISIBLE);
        main.addView(popWindow,param);
        ImageView mBottomOkBtn = new ImageView(this);
        mBottomOkBtn.setBackgroundResource(R.drawable.edit_okbtn);
        mBottomOkBtn.setClickable(true);
        param = new LayoutParams(GlobalDefinitions.SCREEN_WIDTH / 2-2,ScaleUtils.scale(110));
        param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        popWindow.addView(mBottomOkBtn,param);
        ImageView mBottomCancelBtn = new ImageView(this);
        mBottomCancelBtn.setBackgroundResource(R.drawable.edit_cancelbtn);
        mBottomCancelBtn.setClickable(true);
        param = new LayoutParams(GlobalDefinitions.SCREEN_WIDTH / 2-2,ScaleUtils.scale(110));
        param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        popWindow.addView(mBottomCancelBtn,param);

        //image layout
        RelativeLayout mImgLayout = new RelativeLayout(this);
        LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,GlobalDefinitions.SCREEN_HEIGHT-ScaleUtils.scale(210));
        params.addRule(RelativeLayout.BELOW, mTitleBar.getId());
        main.addView(mImgLayout,params);
        mImgView = new ImageView(this);
        mImgView.setScaleType(ScaleType.FIT_CENTER);
        params = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT );
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mImgLayout.addView(mImgView,params);
        //edit layout
        mEditLayout = new RelativeLayout(this);
        mEditLayout.setId(GlobalDefinitions.ID_EDIT_LAYOUT);
        //mEditLayout.setBackgroundColor(Color.rgb(36,40,45));
        mEditLayout.setBackgroundColor(Color.TRANSPARENT);

        int mEditLayoutHeight = 200;
        params = new LayoutParams(LayoutParams.FILL_PARENT,ScaleUtils.scale(mEditLayoutHeight));
        params.addRule(RelativeLayout.ABOVE,mBottomBar.getId());
        main.addView(mEditLayout,params);

        ImageView mCancelBtn = (ImageView) mTitleBar.findViewById(mTitleBar.getBackBtnId());
        ImageView mOkBtn = (ImageView) mTitleBar.findViewById(mTitleBar.getOkBtnId());
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
                    if(len >= 2) {
                        if (!faceDetectDone) {
                            Toast.makeText(BeautyfaceActivity.this, "facedetect fail!", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            mFaceEditor.setFacePoints(featurePointsAll);
                        }
                    }
                    switch(len)
                    {
                        case 0:
                            constructMOPIUI();
                            mEditLayout.setVisibility(View.VISIBLE);
                            mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
                            break;
                        case 1:
                            constructMEIBAIUI();
                            mEditLayout.setVisibility(View.VISIBLE);
                            mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
                            break;
                        case 2:
                            constructDAYANUI();
                            mEditLayout.setVisibility(View.VISIBLE);
                            mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
                            break;
                        case 3:
                            constructSHOULIANUI();
                            mEditLayout.setVisibility(View.VISIBLE);
                            mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
                            break;
                        case 4:
                            constructQUBANUI();
                            mEditLayout.setVisibility(View.VISIBLE);
                            mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
                            break;
                        case 5:
                            constructQUYANDAIUI();
                            mEditLayout.setVisibility(View.VISIBLE);
                            mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
                            break;
                        case 6:
                            constructLIANGYANUI();
                            mEditLayout.setVisibility(View.VISIBLE);
                            mEditLayout.setBackgroundColor(Color.argb(128, 36,40,45));
                            break;
                        case 7:
                            constructGAOBILIANGUI();
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
                ImageSaveAsyncTask imgSaveTask = new ImageSaveAsyncTask();
                imgSaveTask.execute();
                String txt = "save to XIUIMAGE!";
                Toast.makeText(BeautyfaceActivity.this, txt, Toast.LENGTH_SHORT).show();
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
                        mImgView.setImageBitmap(mFaceEditor.getOriginalImage());
                        break;
                    case MotionEvent.ACTION_UP:
                        mImgView.setImageBitmap(null);
                        mImgView.setImageBitmap(mFaceEditor.getDisplayImage());
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        mBottomOkBtn.setVisibility(View.VISIBLE);
        mBottomCancelBtn.setVisibility(View.VISIBLE);
        mBottomOkBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                popWindow.setVisibility(View.INVISIBLE);
                mBottomBar.setVisibility(View.VISIBLE);
                mEditLayout.setVisibility(View.INVISIBLE);
                mFaceEditor.updateImage(true);
                mImgView.setImageBitmap(null);
                mImgView.setImageBitmap(mFaceEditor.getDisplayImage());
            }
        });
        mBottomCancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                popWindow.setVisibility(View.INVISIBLE);
                mBottomBar.setVisibility(View.VISIBLE);
                mEditLayout.setVisibility(View.INVISIBLE);
                mFaceEditor.updateImage(false);

                mImgView.setImageBitmap(null);
                mImgView.setImageBitmap(mFaceEditor.getDisplayImage());


            }
        });
    }
    private void IntentToStartView()
    {
        Intent intent = new Intent(BeautyfaceActivity.this,ModelActivity.class);
        startActivity(intent);
    }
    private void constructMOPIUI()
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
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
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
        params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mEditLayout.addView(mUSMSharpenBar, params);

        mUSMSharpenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                mImgView.setImageBitmap(null);
                //softskin
                mImgView.setImageBitmap(mFaceEditor.BFSoftskin(Math.abs(seekBar.getProgress()/10), GlobalDefinitions.whiteRatio));//));//change to 0-100

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
                LayoutParams paramsStrength = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
    private void constructMEIBAIUI()
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
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
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
        params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mEditLayout.addView(mUSMSharpenBar, params);

        mUSMSharpenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                //mImgView.setImageBitmap(null);
                //skin white
                //mImgView.setImageBitmap(mPhotoEdit.applySharpen(Math.abs(seekBar.getProgress()/10)));//change to 0-100
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
                LayoutParams paramsStrength = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
                paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
                paramsStrength.topMargin = ScaleUtils.scale(40);
                mValueTxt.setLayoutParams(paramsStrength);
                int res = (value/10);
                mValueTxt.setText(String.valueOf(res));
                mImgView.setImageBitmap(null);
                //skin white
                mImgView.setImageBitmap(mFaceEditor.BFSoftskin(GlobalDefinitions.softRatio, Math.abs(seekBar.getProgress()/10)));//));//change to 0-100

            }
        });
        popWindow.setVisibility(View.VISIBLE);
        mBottomBar.setVisibility(View.INVISIBLE);
    }
    private void constructDAYANUI()
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
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
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
        params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mEditLayout.addView(mUSMSharpenBar, params);


        mUSMSharpenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                //mImgView.setImageBitmap(null);
                //big eye
                //mImgView.setImageBitmap(mPhotoEdit.applySharpen(Math.abs(seekBar.getProgress()/10)));//change to 0-100
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
                LayoutParams paramsStrength = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
                paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
                paramsStrength.topMargin = ScaleUtils.scale(40);
                mValueTxt.setLayoutParams(paramsStrength);
                int res = (value/10);
                mValueTxt.setText(String.valueOf(res));

                //big eye
                mImgView.setImageBitmap(null);
                mImgView.setImageBitmap(mFaceEditor.BFEyeWarp(15,seekBar.getProgress()/10));//change to 0-100
//                int val = seekBar.getProgress();
//                Log.e("eyewarp", "eyewarp: "+val);
            }
        });
        popWindow.setVisibility(View.VISIBLE);
        mBottomBar.setVisibility(View.INVISIBLE);
    }
    private void constructSHOULIANUI()
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
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
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
        params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mEditLayout.addView(mUSMSharpenBar, params);

        mUSMSharpenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                //mImgView.setImageBitmap(null);
                //face lift
                //mImgView.setImageBitmap(mPhotoEdit.applySharpen(Math.abs(seekBar.getProgress()/10)));//change to 0-100
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
                LayoutParams paramsStrength = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
                paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
                paramsStrength.topMargin = ScaleUtils.scale(40);
                mValueTxt.setLayoutParams(paramsStrength);
                int res = (value/10);
                mValueTxt.setText(String.valueOf(res));
                mImgView.setImageBitmap(null);
                //shou lian
                mImgView.setImageBitmap(mFaceEditor.BFFaceLift(seekBar.getProgress()/10,0,0,0));//));//change to 0-100

            }
        });
        popWindow.setVisibility(View.VISIBLE);
        mBottomBar.setVisibility(View.INVISIBLE);
    }
    private SwitchButton qubanSBtn = null;
    private void constructQUBANUI()
    {
        mEditLayout.removeAllViews();
        //value txt
        final TextView mValueTxt = new TextView(this);
        mValueTxt.setText(R.string.beautyface_quban_txt);
        mValueTxt.setTextSize(ScaleUtils.getTextSize(20));
        mValueTxt.setTextColor(Color.WHITE);
        mValueTxt.setPadding(0, 0, 0, 0);
        mValueTxt.setBackgroundResource(R.drawable.edit_bg_seek_value);
        mValueTxt.setGravity(Gravity.CENTER);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.leftMargin = ScaleUtils.scale(83);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        //params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mEditLayout.addView(mValueTxt, params);
        //Swtich button
        qubanSBtn = new SwitchButton(this);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mEditLayout.addView(qubanSBtn, params);
        qubanSBtn.setOnChangeListener(new OnChangeListener() {

            @Override
            public void onChange(SwitchButton sb, boolean state) {
                // TODO Auto-generated method stub
                Log.d("switchButton", state ? "锟斤拷":"锟斤拷");
                mImgView.setImageBitmap(null);
                //defreckle
                if(qubanSBtn.isSwitchOn())
                    mImgView.setImageBitmap(mFaceEditor.BFDefreckleAuto());//));
                else
                    mImgView.setImageBitmap(mFaceEditor.getBaseImage());
            }
        });
        popWindow.setVisibility(View.VISIBLE);
        mBottomBar.setVisibility(View.INVISIBLE);
    }
    private void constructQUYANDAIUI()
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
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
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
        params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mEditLayout.addView(mUSMSharpenBar, params);

        mUSMSharpenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                //mImgView.setImageBitmap(null);
                //eyebag remove
                //mImgView.setImageBitmap(mPhotoEdit.applySharpen(Math.abs(seekBar.getProgress()/10)));//change to 0-100
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
                LayoutParams paramsStrength = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
                paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
                paramsStrength.topMargin = ScaleUtils.scale(40);
                mValueTxt.setLayoutParams(paramsStrength);
                int res = (value/10);
                mValueTxt.setText(String.valueOf(res));
                mImgView.setImageBitmap(null);
                //eye bag remove
                mImgView.setImageBitmap(mFaceEditor.BFEyeBagRemoval(seekBar.getProgress()/10));//));//change to 0-100
            }
        });
        popWindow.setVisibility(View.VISIBLE);
        mBottomBar.setVisibility(View.INVISIBLE);
    }
    private void constructLIANGYANUI()
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
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
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
        params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mEditLayout.addView(mUSMSharpenBar, params);

        mUSMSharpenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                //mImgView.setImageBitmap(null);
                //light eye
                //mImgView.setImageBitmap(mPhotoEdit.applySharpen(Math.abs(seekBar.getProgress()/10)));//change to 0-100
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
                LayoutParams paramsStrength = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
                paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
                paramsStrength.topMargin = ScaleUtils.scale(40);
                mValueTxt.setLayoutParams(paramsStrength);
                int res = (value/10);
                mValueTxt.setText(String.valueOf(res));
                mImgView.setImageBitmap(null);
                //light eye
                mImgView.setImageBitmap(mFaceEditor.BFLightEye(seekBar.getProgress()/10));//));//change to 0-100
            }
        });
        popWindow.setVisibility(View.VISIBLE);
        mBottomBar.setVisibility(View.INVISIBLE);
    }
    private void constructGAOBILIANGUI()
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
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
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
        params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.width = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(200);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mEditLayout.addView(mUSMSharpenBar, params);
        mUSMSharpenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                //mImgView.setImageBitmap(null);
                //high nose
                //mImgView.setImageBitmap(mPhotoEdit.applySharpen(Math.abs(seekBar.getProgress()/10)));//change to 0-100
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
                LayoutParams paramsStrength = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                int s = GlobalDefinitions.SCREEN_WIDTH-ScaleUtils.scale(240);
                paramsStrength.leftMargin = (int) (ScaleUtils.scale(83)+s*value/1000);
                paramsStrength.topMargin = ScaleUtils.scale(40);
                mValueTxt.setLayoutParams(paramsStrength);
                int res = (value/10);
                mValueTxt.setText(String.valueOf(res));
                mImgView.setImageBitmap(null);
                //high nose
                mImgView.setImageBitmap(mFaceEditor.BFHighnose(seekBar.getProgress()/10));//));//change to 0-100

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
                PhotoUtils.saveBitmap(mFaceEditor.getDisplayImage(), ImgPath,PhotoUtils.IMAGE_FORMAT_JPG, BeautyfaceActivity.this);
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
            //mWaitBar.setVisibility(View.INVISIBLE);
            if(result)
                finish();
        }
        @Override
        protected void onPreExecute()
        {
            //mWaitBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            //mWaitBar.setProgress(values[0]);
        }

    }


    private void FaceDetection()
    {
        if(srcBitmap == null)
            return;

        faceDetectDone = false;
        YNFaceDetect faceDetect = new YNFaceDetect(this, 0);
        YNFace101[] faces = faceDetect.detect(srcBitmap, 0);
        if( faces != null && faces.length > 0 )
        {
            for (int i = 0; i < 1; ++i) {
                YNFaceDetect.YNRect face_rectangle = faces[i].rect;
                float left = face_rectangle.left;
                float top = face_rectangle.top;

                float w = face_rectangle.right - face_rectangle.left;
                float h = face_rectangle.bottom - face_rectangle.top;
                faceCenterX = left + w * 0.5f;
                faceCenterY = top + h * 0.5f;
            }
            GetFaceFeaturePoints(faces[0].getPointsArray(), width, height);
        }
        else
        {
            Toast.makeText(BeautyfaceActivity.this, R.string.facedetectfail, Toast.LENGTH_SHORT).show();
        }
    }

    private void GetFaceFeaturePoints(PointF[] points, int w, int h)
    {
        double x, y;
        int j = 0;
        for(int i = 0; i < 101;i++)
        {
            x = points[i].x;
            y = points[i].y;
            featurePointsAll[j++] = (int)(width * x / w);
            featurePointsAll[j++] = (int)(height * y / h);
        }
        featurePointsAll[j++] = (int)(faceCenterX * width / w);
        featurePointsAll[j++] = (int)(faceCenterY * height / h);

        faceDetectDone = true;
        //锟斤拷示锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
        boolean displayFacePoints = true;
        if(displayFacePoints)
        {
            Paint paint = new Paint();
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(Math.max(srcBitmap.getWidth(), srcBitmap.getHeight()) / 100f);
            Bitmap bitmap = mFaceEditor.getDisplayImage().copy(srcBitmap.getConfig(), true);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bitmap, new Matrix(), null);

            for(int i = 0; i < 101;i++)
            {
                canvas.drawCircle(Math.abs(featurePointsAll[i*2]-1), Math.abs(featurePointsAll[i*2+1]-1), 2, paint);
                canvas.drawText(String.valueOf(i), featurePointsAll[i*2], featurePointsAll[i*2+1], paint);
            }
            ShowImage(bitmap);
        }
    }


    public void ShowImage(Bitmap bitmap) {
        if (bitmap != null) {
            mImgView.setImageBitmap(null);
            mImgView.setImageBitmap(bitmap);

        } else {

        }
    }

}
