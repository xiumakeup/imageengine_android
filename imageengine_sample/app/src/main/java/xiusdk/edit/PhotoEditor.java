package xiusdk.edit;

import java.io.File;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import com.xiusdk.imageengine.ImageEngine;

public class PhotoEditor {

	private Context mContext = null;
	private String sourcePath = null;
	private Bitmap baseBitmap = null;
	private Bitmap curBitmap = null;
	private int width = 0;
	private int height = 0;
	private int mSaturation = 255;
	private int mColorTemperature = 0;
	private int mExposure = 0;
	private int mBrightness = 0;
	private int mContrast = 0;
	private int mLightness = 0;
	private int mSharpenRatio = 0;
	private int mHighlightRatio = 0;
	private int mShadowRatio = 0;
	private int mMosciaSize = 0;
	private int mHue = 0;
	private int mGray = 0;
	private int mGamma = 0;

	private ImageEngine imageEngine = null;
	public PhotoEditor(Context mContext, String path)
	{
		this.mContext = mContext;
		sourcePath = path;
		baseBitmap = getOriginalImage(sourcePath);
		width = baseBitmap.getWidth();
		height = baseBitmap.getHeight();
		curBitmap = Bitmap.createBitmap(baseBitmap);
		imageEngine = new ImageEngine(mContext);

	}
	public int getGray()
	{
		return mGray;
	}
	public int getGamma()
	{
		return mGamma;
	}
	public int getHue()
	{
		return mHue;
	}
	public int getWidth()
	{
		return width;
	}
	public int getHeight()
	{
		return height;
	}
	public int getSaturation()
	{
		return mSaturation;
	}
	public int getExposure()
	{
		return mExposure;
	}
	public int getColorTemperature()
	{
		return mColorTemperature;
	}
	public int getLightness()
	{
		return mLightness;
	}
	public int getSharpenRatio()
	{
		return mSharpenRatio;
	}
	public int getBrightness()
	{
		return mBrightness;
	}
	public int getContrast()
	{
		return mContrast;
	}
	public int getMosciaSize()
	{
		return mMosciaSize;
	}
	public int getHighlightRatio()
	{
		return mHighlightRatio;
	}

	public Bitmap getDisplayImage()
	{
		if(curBitmap==null)
			return baseBitmap;
		return curBitmap;
	}
	public Bitmap getBaseImage()
	{
		if(baseBitmap!=null)
			return baseBitmap;
		else return null;

	}
	public Bitmap getOriginalImage(String path)
	{
		File mFile=new File(path);
		//
		if (mFile.exists()) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path,options);
			options.inJustDecodeBounds = false;
			int max = Math.max(options.outWidth, options.outHeight);
			if(max>1280&&max <= 2560)
				options.inSampleSize = 2;
			else if(max > 2560 && max <= 3840)
				options.inSampleSize = 3;
			else if(max <= 1280)
				options.inSampleSize = 1;
			else
				options.inSampleSize = 4;
			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeFile(path,options);
			}catch (Exception e) {
				// TODO: handle exception
				bitmap = null;
			}
			return bitmap;
		}
		else
			return null;
	}
	public Bitmap getOriginalImage()
	{
	    return getOriginalImage(sourcePath);
	}
	public Bitmap applyBC(int bright,int contrast)
	{
		mBrightness = bright;
		mContrast = contrast;
		curBitmap = imageEngine.BrightContrastAdjust(baseBitmap, bright, contrast, 128);
		return curBitmap;
	}
	public Bitmap applyHue(int hue)
	{
		mHue = hue;
		curBitmap = imageEngine.HueAndSaturationAdjust(baseBitmap, mHue, 0);
		return curBitmap;
	}
	public Bitmap applySaturation(int saturation)
	{
		mSaturation = saturation;
		curBitmap = imageEngine.SaturationAdjust(baseBitmap, saturation);
		return curBitmap;
	}
	public Bitmap applySharpen(int ratio)
	{
		mSharpenRatio = ratio;
		curBitmap = imageEngine.SharpenAdjust(baseBitmap, ratio);
		return curBitmap;
	}
	public Bitmap applyLightness(int lightness)
	{
		mLightness = lightness;
		curBitmap = imageEngine.LightnessAdjust(baseBitmap, lightness);
		return curBitmap;
	}
	public Bitmap applyColorTeamperature(int intensity)
	{
		mColorTemperature = intensity;
		curBitmap = imageEngine.ColorTemperatureAdjust(baseBitmap, intensity);
		return curBitmap;
	}
	public Bitmap applyRotate(int degree)
	{
		curBitmap = imgToRotateEffect(baseBitmap, degree);
		return curBitmap;
	}
	public Bitmap applyFlip(boolean flipXY,boolean flipTB)
	{
		curBitmap = imgToFlipEffect(baseBitmap, flipXY, flipTB);
		return curBitmap;
	}
	public Bitmap applyExposure(int intensity)
	{
		mExposure = intensity;
		curBitmap = imageEngine.ExposureAdjust(baseBitmap, intensity);
		return curBitmap;
	}
	public Bitmap applyHighlight(int ratio)
	{
		mHighlightRatio = ratio;
		curBitmap = imageEngine.HighlightShadowAdjust(baseBitmap, mHighlightRatio,mShadowRatio);
		return curBitmap;
	}
	public Bitmap applyShadow(int ratio)
	{
		mShadowRatio = ratio;
		curBitmap = imageEngine.HighlightShadowAdjust(baseBitmap, mHighlightRatio,mShadowRatio);
		return curBitmap;
	}
	public Bitmap applyMoscia(int size)
	{
		mMosciaSize = size;
		curBitmap = imageEngine.Mosaic(baseBitmap, mMosciaSize);
		return curBitmap;
	}
	public Bitmap applyGray(int ratio)
	{
		mGray = ratio;
		curBitmap = imageEngine.Desaturate(baseBitmap, mGray);
		return curBitmap;
	}
	public Bitmap applyGamma(int gamma)
	{
		mGamma = gamma;
		curBitmap = imageEngine.GammaCorrect(baseBitmap, mGamma);
		return curBitmap;
	}
	public Bitmap applyFilter(int filterId)
	{
		curBitmap = imageEngine.XIUSDK_effectFilter(baseBitmap, filterId);
		return curBitmap;
	}
	public void updateImage(boolean update)
	{
		if(curBitmap==null)
			return;
		if(update)
		{
			baseBitmap = Bitmap.createBitmap(curBitmap);
		}
		else
			curBitmap = Bitmap.createBitmap(baseBitmap);
	}
	public void destroy()
	{
		if(null!=curBitmap)
		{
			curBitmap.recycle();
			curBitmap = null;
		}
		if(null!=baseBitmap)
		{
			baseBitmap.recycle();
			baseBitmap = null;
		}
		sourcePath = null;
	}
	/**
	 * Flip transform of an image.
	 * @param curBitmap One bitmap to be processed
	 * @param flipXY A boolean to flip at horizontal direction
	 * @param flipTB A boolean to flip at vertical direction
	 * @return Effected bitmap
	 */
	public static Bitmap imgToFlipEffect(Bitmap curBitmap,boolean flipXY,boolean flipTB)
	{
		Matrix matrix = new Matrix();
		if(flipXY)
			matrix.postScale(-1, 1);
		else
			matrix.postScale(1, 1);
		if(flipTB)
			matrix.postScale(1, -1);
		else
			matrix.postScale(1, 1);
		Bitmap tBitmap = Bitmap.createBitmap(curBitmap,0,0,curBitmap.getWidth(),curBitmap.getHeight(),matrix,true);
		return tBitmap;
	}
	/**
	 * Rotate transform of an image.
	 * @param curBitmap One bitmap to be processed
	 * @param degree The rotate value
	 * @return Effected bitmap
	 */
	public Bitmap imgToRotateEffect(Bitmap curBitmap,int degree)
	{
		curBitmap = imageEngine.ImageTransformation(curBitmap,degree,1.0f,0);
		return curBitmap;
	}
	/**Merge process between two bitmaps.
	 * @param curBitmap One bitmap to be merged
	 * @param baseBitmap One bitmap to be merged
	 * @param ratio The value of merged ratio for the process
	 * @return Effected bitmap
	 */
	public Bitmap mergeEffect(Bitmap curBitmap,Bitmap baseBitmap,int ratio)
	{
		long t0=System.currentTimeMillis();
		Bitmap kBitmap = Bitmap.createBitmap(baseBitmap.getWidth(),baseBitmap.getHeight(),Config.ARGB_8888);
		if(curBitmap!=null)
		{
			Canvas canvas = new Canvas(kBitmap);
			Paint paint = new Paint();
			canvas.drawBitmap(curBitmap, 0, 0,paint);
			paint.setAlpha(255-(ratio>>1)-(ratio<<1));//(255-ratio*255/100)
			canvas.drawBitmap(baseBitmap, 0, 0, paint);
			long t1=System.currentTimeMillis();
			Log.e("mergeEffect timecost:", String.valueOf(t1-t0));
			return kBitmap;
		}
		else
			return null;
	}
}
