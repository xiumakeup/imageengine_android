package xiusdk.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;


public class VerticalSeekBar extends SeekBar {
	
	private OnSeekBarChangeListener mOnSeekBarChangeListener;
	
	public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    /* (non-Javadoc)
     * @see android.widget.AbsSeekBar#onMeasure(int, int)
     */
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
        /**
         * when the layout is changed.
         * we also should changed the thumb's position
         */
        super.onSizeChanged(getMeasuredHeight(), getMeasuredWidth(), 0, 0);
    }

    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(),0);

        super.onDraw(c);
    }

    /* (non-Javadoc)
	 * @see android.widget.SeekBar#setOnSeekBarChangeListener(android.widget.SeekBar.OnSeekBarChangeListener)
	 */
	@Override
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
		// TODO Auto-generated method stub
		super.setOnSeekBarChangeListener(l);
		mOnSeekBarChangeListener = l;
	}

	@Override
    public boolean onTouchEvent(MotionEvent event) {
    	
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	if (mOnSeekBarChangeListener != null) {
            		mOnSeekBarChangeListener.onStartTrackingTouch(this);
            	}
            	int i = getMax() - (int) (getMax() * event.getY() / getHeight());
                setProgress(i);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
            	break;
            case MotionEvent.ACTION_MOVE:
            	int j = getMax() - (int) (getMax() * event.getY() / getHeight());
                setProgress(j);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;
            case MotionEvent.ACTION_UP:
            	if (mOnSeekBarChangeListener != null) {
            		mOnSeekBarChangeListener.onStopTrackingTouch(this);
            	}
            	int k = getMax() - (int) (getMax() * event.getY() / getHeight());
                setProgress(k);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;
            case MotionEvent.ACTION_CANCEL:
            	if (mOnSeekBarChangeListener != null) {
            		mOnSeekBarChangeListener.onStopTrackingTouch(this);
            	}
                break;
        }
        return true;
    }
    
	/* (non-Javadoc)
	 * @see android.widget.ProgressBar#setProgress(int)
	 */
	@Override
	public synchronized void setProgress(int progress) {
		// TODO Auto-generated method stub
		super.setProgress(progress);
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}

	/* (non-Javadoc)
	 * @see android.widget.AbsSeekBar#setMax(int)
	 */
	@Override
	public synchronized void setMax(int max) {
		// TODO Auto-generated method stub
		super.setMax(max);
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}
	
	
}