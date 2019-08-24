package xiusdk.tools;


//import com.example.timageviewtest.R;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiusdk.beautycamera.R;

//ID:100-199
public class TitleView extends RelativeLayout {

	private Context mContext = null;
	private RelativeLayout mTitleLayout = null;
	private ImageView mBackBtn = null;
	private ImageView mOkBtn = null;
	private int mLayoutHeight = 100;
	private final int ID_BACK = 100;
	private final int ID_OK = 101;
	private final int ID_TITLE = 102;
	private final int ID_LAYOUT = 103;
	private TextView mTitle = null;
	private int mBackIconId = 0;
	private int mOkIconId = 0;
	private String title = null;
    public TitleView(Context context,int w,int lh,int id_a,int id_b, String title) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		mLayoutHeight = lh;
		mBackIconId = id_a;
		mOkIconId = id_b;
		this.title = title;
        initialUI();		
	}
    public void setBackground(int red, int green, int blue)
    {
    	if(mTitleLayout!=null)
    	mTitleLayout.setBackgroundColor(Color.rgb(red, green, blue));
    	
    }
	private void initialUI()
	{
		mTitleLayout = new RelativeLayout(mContext);
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
	    param.addRule(RelativeLayout.CENTER_IN_PARENT);
	    mTitleLayout.setId(ID_LAYOUT);
	    mTitleLayout.setBackgroundResource(R.drawable.camera_title);
	    this.addView(mTitleLayout,param);
		mBackBtn = new ImageView(mContext);
		mOkBtn = new ImageView(mContext);
		mTitle = new TextView(mContext);
		mBackBtn.setId(ID_BACK);
		param = new RelativeLayout.LayoutParams(mLayoutHeight,mLayoutHeight);
		param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		param.addRule(RelativeLayout.CENTER_VERTICAL);
		this.addView(mBackBtn,param);
		mBackBtn.setBackgroundResource(mBackIconId);
		mBackBtn.setClickable(true);
		mOkBtn.setId(ID_OK);
		param = new RelativeLayout.LayoutParams(mLayoutHeight,mLayoutHeight);
		param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		param.addRule(RelativeLayout.CENTER_VERTICAL);
		this.addView(mOkBtn,param);
		mOkBtn.setBackgroundResource(mOkIconId);
		mOkBtn.setClickable(true);
		mTitle.setId(ID_TITLE);
		mTitle.setBackgroundColor(Color.TRANSPARENT);
		mTitle.setTextColor(Color.WHITE);
		mTitle.setText(title);
		mTitle.setTextSize(ScaleUtils.getTextSize(23));
		mTitle.setPadding(0, 0, 0, 0);
		param = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		param.addRule(RelativeLayout.CENTER_IN_PARENT);
		param.addRule(RelativeLayout.CENTER_VERTICAL);
		this.addView(mTitle,param);

	}
	public int getBackBtnId()
	{
		return mBackBtn.getId();
	}
	public int getOkBtnId()
	{
		return mOkBtn.getId();
	}
	public int getTitleId()
	{
		return mTitle.getId();
	}
	public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
		return (int) (dpValue * scale + 0.5f);  
    }  
  

	public static int px2dip(Context context, float pxValue) {  
		final float scale = context.getResources().getDisplayMetrics().density;  
		return (int) (pxValue / scale + 0.5f);  
	} 

}
