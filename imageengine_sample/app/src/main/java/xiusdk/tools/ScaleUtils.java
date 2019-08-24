package xiusdk.tools;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class ScaleUtils {
	public static int SCREEN_WIDTH 		= 800;
	public static int SCREEN_HEIGHT 	= 480;
	public static int SCREEN_DPI		= 240;
	public static int SCALE_BASE_X 		= 800;
	public static int SCALE_BASE_Y 		= 480;
	public static int SCALE_BASE_DPI	= 240;
	
	public static void scaleInit(Activity activity, int baseWidth, int baseHeight, int baseDPI){
		/** Get screen width and height */
		WindowManager windowManager = activity.getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		
		int w = display.getWidth();
		int h = display.getHeight();
		/**
		 * Make sure w > h.
		 * When launch this activity, user push 'HOME' key,
		 * w maybe less than h.
		 */
		if (w < h){
			int tmp = w;
			w = h;
			h = tmp;
		}
		
		DisplayMetrics dm = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(dm);
		scaleInit(w, h, dm.densityDpi, baseWidth, baseHeight, baseDPI);
	}
	
	private static void scaleInit(int scrWidth, int scrHeight, int scrDPI, int baseWidth, int baseHeight, int baseDPI){		
		SCREEN_WIDTH = scrWidth;
		SCREEN_HEIGHT = scrHeight;
		SCREEN_DPI = scrDPI;
		SCALE_BASE_X = baseWidth;
		SCALE_BASE_Y = baseHeight;
		SCALE_BASE_DPI = baseDPI;
	}
	
	public static float getXScale(){
		return (float)SCREEN_WIDTH / SCALE_BASE_X;
	}
	
	public static int scaleX(int x){
		return (x * SCREEN_WIDTH / SCALE_BASE_X);
	}

	public static int scaleXWithDPI(int x){
		return (x * SCREEN_WIDTH * SCALE_BASE_DPI / SCALE_BASE_X / SCREEN_DPI);
	}
	
	public static float getYScale(){
		return (float)SCREEN_HEIGHT / SCALE_BASE_Y;
	}
	
	public static int scaleY(int y){
		return (y * SCREEN_HEIGHT / SCALE_BASE_Y);
	}
	
	public static int scaleYWithDPI(int y){
		return (y * SCREEN_HEIGHT * SCALE_BASE_DPI / SCALE_BASE_Y / SCREEN_DPI);
	}
	
	public static int scale(int base){
		if(SCREEN_WIDTH * SCALE_BASE_Y >= SCREEN_HEIGHT * SCALE_BASE_X)
			return scaleY(base);
		else
			return scaleX(base);
	}
	
	public static int getTextSize(int textSize)
	{
		float scale = getXScale() >= getYScale() ? getYScale() : getXScale();
		if(scale == 1 || scale >= 1.5)
			return textSize;
		return textSize-2;
	}
	
	public static float getScale()
	{
		float scale = getXScale() >= getYScale() ? getYScale() : getXScale();
		return scale;
	}
}
