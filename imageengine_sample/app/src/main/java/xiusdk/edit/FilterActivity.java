package xiusdk.edit;


import xiusdk.camera.ModelActivity;
import xiusdk.tools.GlobalDefinitions;
import xiusdk.tools.ImageUtils;
import xiusdk.tools.PhotoUtils;
import xiusdk.tools.ScaleUtils;
import xiusdk.tools.TitleView;
import xiusdk.tools.VerticalSeekBar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;

import com.xiusdk.beautycamera.R;
import com.xiusdk.imageengine.ImageEngine;

public class FilterActivity extends Activity{
    private RelativeLayout main = null;
    private TitleView mTitleBar = null;
    private final int FILTER_ACTIVITY_ID = 7000;
    private ImageView mCancelBtn = null;
    private ImageView mOkBtn = null;
    private RelativeLayout mImgLayout = null;
    private ImageView mImgView = null;
    private RelativeLayout mFilterLayout = null;
    private HorizontalScrollView mFilterScrollView = null;
    private LinearLayout mFilterScrollLayout = null;
    private Uri mImgUri = null;
    private int mFilterNum = 0;
    private final int FILTER_ID = 8000;
    private RelativeLayout curFilterLayout = null;
    private String[]filterTxt = null;
    private PhotoEditor mPhotoEdit = null;
    private ProgressBar mWaitBar = null;
    private ImageEngine zp = null;
    private VerticalSeekBar mFilterSB = null;
    private Bitmap tempBitmap = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        main = new RelativeLayout(this);
        main.setBackgroundColor(Color.BLACK);
        setContentView(main);
        ScaleUtils.scaleInit(this,1280,720,320);
        zp = new ImageEngine(this);
        InitialUI();
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
    private void InitialUI()
    {
        filterTxt = new String[]{getResources().getString(R.string.yuantu),getResources().getString(R.string.nuannuan),getResources().getString(R.string.qingxi),getResources().getString(R.string.baixi),getResources().getString(R.string.lengyan),getResources().getString(R.string.bingling),getResources().getString(R.string.dianya),getResources().getString(R.string.landiao),getResources().getString(R.string.luoli),getResources().getString(R.string.luokk),
                getResources().getString(R.string.nuanhuang),getResources().getString(R.string.qingchun),getResources().getString(R.string.rixi),getResources().getString(R.string.rouguang),getResources().getString(R.string.tianmei),getResources().getString(R.string.weimei),getResources().getString(R.string.liren),getResources().getString(R.string.guodong),getResources().getString(R.string.huayan),getResources().getString(R.string.luozhuang),
                getResources().getString(R.string.nenhong),getResources().getString(R.string.yishu),getResources().getString(R.string.meifu),getResources().getString(R.string.hongrun),getResources().getString(R.string.jingmei),getResources().getString(R.string.ziyun),getResources().getString(R.string.danya),getResources().getString(R.string.qingxin),getResources().getString(R.string.roumei),getResources().getString(R.string.wennuan),
                getResources().getString(R.string.nnyangguang),getResources().getString(R.string.tianmeikeren),getResources().getString(R.string.abaose),getResources().getString(R.string.langman),getResources().getString(R.string.qingtou),getResources().getString(R.string.zhenbai),getResources().getString(R.string.ziran), getResources().getString(R.string.download)};
        mFilterNum = filterTxt.length;
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,ScaleUtils.scale(100));
        mTitleBar = new TitleView(this,GlobalDefinitions.SCREEN_WIDTH,ScaleUtils.scale(100),R.drawable.title_back,R.drawable.title_ok, getString(R.string.filtertitle));
        mTitleBar.setId(FILTER_ACTIVITY_ID);
        param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        main.addView(mTitleBar,param);

        mCancelBtn = (ImageView)mTitleBar.findViewById(mTitleBar.getBackBtnId());
        mOkBtn = (ImageView)mTitleBar.findViewById(mTitleBar.getOkBtnId());

        //image layout
        mImgLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,GlobalDefinitions.SCREEN_HEIGHT-ScaleUtils.scale(100));
        params.addRule(RelativeLayout.BELOW,mTitleBar.getId());
        main.addView(mImgLayout,params);
        mImgView = new ImageView(this);
        mImgView.setId(FILTER_ACTIVITY_ID + 1);
        mImgView.setScaleType(ScaleType.FIT_CENTER);
        params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,GlobalDefinitions.SCREEN_HEIGHT-ScaleUtils.scale(250));
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mImgLayout.addView(mImgView,params);

        mWaitBar = new ProgressBar(this);
        mWaitBar.setBackgroundResource(R.drawable.wait_progressbar);
        params = new LayoutParams(ScaleUtils.scale(150),ScaleUtils.scale(150));
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mImgLayout.addView(mWaitBar, params);
        mWaitBar.setVisibility(View.INVISIBLE);
        //filter layout
        mFilterLayout = new RelativeLayout(this);
        mFilterLayout.setId(FILTER_ACTIVITY_ID + 2);
        mFilterLayout.setBackgroundColor(Color.rgb(36,40,45));
        params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,ScaleUtils.scale(150));
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mImgLayout.addView(mFilterLayout,params);
        mFilterScrollView = new HorizontalScrollView(this);
        mFilterScrollView.setHorizontalScrollBarEnabled(false);
        mFilterScrollView.setFillViewport(true);
        mFilterScrollView.setId(FILTER_ACTIVITY_ID + 3);
        params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mFilterLayout.addView(mFilterScrollView,params);
        mFilterScrollLayout = new LinearLayout(this);
        mFilterScrollLayout.setHorizontalScrollBarEnabled(true);
        mFilterScrollLayout.setId(FILTER_ACTIVITY_ID + 4);
        params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mFilterScrollView.addView(mFilterScrollLayout,params);
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
        mFilterScrollLayout.removeAllViews();
        params = null;

        Bitmap src = BitmapFactory.decodeResource(this.getResources(), R.drawable.filter_bac1);
        for(int i = 0; i < mFilterNum; i++)
        {
            RelativeLayout tempLayout = new RelativeLayout(this);
            tempLayout.setId(FILTER_ID + i);
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
            if(i == 0)
                tempImgView.setImageResource(R.drawable.filter_bac1);
            else if(i >0 && i < mFilterNum - 1)
            {
                FilterImgRefreshAsyncTask task = new FilterImgRefreshAsyncTask(tempImgView);
                task.execute(300 + i - 1);
            }
            else
            {
                tempImgView.setScaleType(ScaleType.CENTER_CROP);
                tempImgView.setImageResource(R.drawable.download);
            }
            //tempImgView.setImageResource(R.drawable.filter_bac);
            params = new RelativeLayout.LayoutParams(ScaleUtils.scale(100),
                    ScaleUtils.scale(134));
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            tempLayout.addView(tempImgView, params);

            TextView txtView = new TextView(this);

            txtView.setText(filterTxt[i]);
            txtView.setTextSize(ScaleUtils.getTextSize(8));
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
            final int id = FILTER_ID + i;
            final int filterID = 300 + i;
            final int last = i;
            tempLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    if(filterID==300)
                    {
                        if(curFilterLayout!=null)
                        {
                            curFilterLayout.setBackgroundColor(Color.TRANSPARENT);
                        }
                        curFilterLayout = (RelativeLayout)findViewById(id);
                        curFilterLayout.setBackgroundColor(Color.GREEN);
                        mFilterSB.setVisibility(View.INVISIBLE);
                        if(tempBitmap != null)
                        {
                            tempBitmap.recycle();
                            tempBitmap = null;
                        }
                        return;
                    }
                    if(last == mFilterNum - 1)
                    {
                        Uri uri = Uri.parse("http://www.xiusdk.cn");
                        Intent it = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(it);
                    }
                    else
                    {
                        if(curFilterLayout!=null)
                        {
                            curFilterLayout.setBackgroundColor(Color.TRANSPARENT);
                        }
                        curFilterLayout = (RelativeLayout)findViewById(id);
                        curFilterLayout.setBackgroundColor(Color.GREEN);
                        mImgView.setImageBitmap(mPhotoEdit.applyFilter(filterID-1));//change to 0-510
                        mFilterSB.setVisibility(View.VISIBLE);
                        mFilterSB.setProgress(100);
                    }
                }
            });
        }
        mFilterSB = new VerticalSeekBar(this);
        mFilterSB.setVisibility(View.INVISIBLE);
        mFilterSB.setProgress(100);
        mFilterSB.setThumb(getResources().getDrawable(R.drawable.edit_control_point));
        mFilterSB.setProgressDrawable(getResources().getDrawable(R.drawable.edit_brightness_seekbar));
        mFilterSB.setPadding(ScaleUtils.scale(20), ScaleUtils.scale(15),
                ScaleUtils.scale(20), ScaleUtils.scale(15));
        param = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,GlobalDefinitions.SCREEN_HEIGHT / 3);
        param.addRule(RelativeLayout.CENTER_IN_PARENT);
        param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        param.rightMargin = ScaleUtils.scale(25);
        main.addView(mFilterSB,param);
        InitialEngine();
        curFilterLayout = (RelativeLayout)findViewById(FILTER_ID);
        curFilterLayout.performClick();
        src.recycle();


    }
    private void InitialEngine()
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
    private void ResponseUI()
    {
        mCancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                finish();
                IntentToStartView();
            }
        });
        mOkBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                ImageSaveAsyncTask imgSaveTask = new ImageSaveAsyncTask();
                imgSaveTask.execute();
                String txt = "SAVE XIUIMAGE!";
                Toast.makeText(FilterActivity.this, txt, Toast.LENGTH_SHORT).show();
                finish();
                IntentToStartView();
            }
        });
        mFilterSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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
                tempBitmap = mPhotoEdit.mergeEffect(mPhotoEdit.getDisplayImage(),mPhotoEdit.getBaseImage(),seekBar.getProgress());
                mImgView.setImageBitmap(null);
                mImgView.setImageBitmap(tempBitmap);//change to 0-510
            }
        });
    }
    private void IntentToStartView()
    {
        Intent intent = new Intent(FilterActivity.this,ModelActivity.class);
        startActivity(intent);
    }

    class ImageSaveAsyncTask extends AsyncTask<Void,Integer,Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try{
                String ImgPath = ImageUtils.getImageSavePath(GlobalDefinitions.TAG);
                if(tempBitmap != null)
                    PhotoUtils.saveBitmap(tempBitmap, ImgPath,PhotoUtils.IMAGE_FORMAT_JPG, FilterActivity.this);
                else
                    PhotoUtils.saveBitmap(mPhotoEdit.getDisplayImage(), ImgPath,PhotoUtils.IMAGE_FORMAT_JPG, FilterActivity.this);
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

    class FilterImgRefreshAsyncTask extends AsyncTask<Integer,Integer,Bitmap>
    {
        private ImageView imageView;
        public FilterImgRefreshAsyncTask(ImageView imgView)
        {
            super();
            imageView = imgView;
        }
        @Override
        protected Bitmap doInBackground(Integer... params) {
            // TODO Auto-generated method stub
            Bitmap tmp = zp.XIUSDK_effectFilter(BitmapFactory.decodeResource(FilterActivity.this.getResources(), R.drawable.filter_bac1), (int)params[0]);
            return tmp;
        }
        @Override
        protected void onPostExecute(Bitmap result)
        {
            if(result!=null)
            {
                imageView.setScaleType(ScaleType.CENTER_CROP);
                imageView.setImageBitmap(result);
            }
            else
                this.cancel(true);
            super.onPostExecute(result);
        }
        @Override
        protected void onPreExecute()
        {
        }
        @Override
        protected void onProgressUpdate(Integer... values)
        {
        }


    }
}
