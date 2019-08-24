package xiusdk.tools;

import java.util.ArrayList;
import java.util.List;
//
//import com.example.timageviewtest.R;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiusdk.beautycamera.R;

//ID:200-299
//Available:200-290
public class BottomView extends RelativeLayout{

	private Context mContext = null;
	private RelativeLayout mBottomLayout = null;
	private int mWidth = 0;
	private int mlHeight = 0;
	private String[] mNames = null;
	private List<TextView> mButtonArray = null;
	private int mTxtSize = 0;
	private final int ID_BTN = 200;
	private final int ID_LAYOUT = 299;
	private final int ID_HSLAYOUT = 298;
	private final int ID_BTLAYOUT = 800;
	private int mBtnCount = 0;
    private int[] mBtnId = null;
    private int mBtnWidth = 120;
	public BottomView(Context context,int w,int lh,String[]names,int txtSize) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		mWidth = w;
		mlHeight = lh;
		mBtnWidth = w/5;
		mNames = names;
		mTxtSize = txtSize;
		initialUI();
	}
    private void initialUI()
    {
    	mBottomLayout = new RelativeLayout(mContext);
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
	    param.addRule(RelativeLayout.CENTER_IN_PARENT);
	    mBottomLayout.setId(ID_LAYOUT);
	    mBottomLayout.setBackgroundResource(R.drawable.edit_bottombar_bg);
	    this.addView(mBottomLayout,param);
	    HorizontalScrollView hsLayout = new HorizontalScrollView(mContext);
	    hsLayout.setHorizontalScrollBarEnabled(false);
	    hsLayout.setFillViewport(true);
	    hsLayout.setId(ID_HSLAYOUT);
	    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mBottomLayout.addView(hsLayout,params);
        LinearLayout mBtnScrollLayout = new LinearLayout(mContext);
        mBtnScrollLayout.setHorizontalScrollBarEnabled(true);
        mBtnScrollLayout.setId(ID_BTLAYOUT);
        params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        hsLayout.addView(mBtnScrollLayout,params);

	    mBtnCount = mNames.length;
	    mButtonArray = new ArrayList<TextView>(mBtnCount); 
	    mBtnId = new int[mBtnCount];
	    for(int i = 0;i<mBtnCount;i++)
	    {
	    	TextView tBtn = new TextView(mContext);
	    	param = new RelativeLayout.LayoutParams(mBtnWidth,mlHeight);
		    param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		    param.leftMargin = i*(mWidth/mBtnCount);
		    tBtn.setText(mNames[i]);
		    tBtn.setTextColor(Color.WHITE);
		    tBtn.setTextSize(mTxtSize);
		    tBtn.setId(ID_BTN+i);
		    mBtnId[i] = ID_BTN+i;
		    tBtn.setGravity(Gravity.CENTER);
		    mBtnScrollLayout.addView(tBtn,param);
		    mButtonArray.add(tBtn);
	    }
    }
    public int[] getBtnId()
    {
    	return mBtnId;
    }
    public void pressButton(int id)
    {
    	for(int i =0;i<mBtnCount;i++)
    	{
    		if(mButtonArray.get(i).getId()==id)
    		{
    			mButtonArray.get(i).setBackgroundColor(Color.BLACK);
    			mButtonArray.get(i).setTextColor(Color.GREEN);
    		}
    		else
    		{
    			mButtonArray.get(i).setBackgroundColor(Color.TRANSPARENT);
    			mButtonArray.get(i).setTextColor(Color.WHITE);
    		}
    	}
    }
}
