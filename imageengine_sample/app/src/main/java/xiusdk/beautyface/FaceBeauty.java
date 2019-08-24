package xiusdk.beautyface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.xiusdk.imageengine.ImageEngine;

import java.io.File;

public class FaceBeauty {
    private Context mContext = null;
    private String sourcePath = null;
    private Bitmap baseBitmap = null;
    private Bitmap curBitmap = null;
    private int width = 0;
    private int height = 0;
    private ImageEngine imageEngine = null;
    private boolean hasFace = false;
    private int[] mFacePointsAll = null;

    public FaceBeauty(Context mContext,String path)
    {
        this.mContext = mContext;
        sourcePath = path;
        baseBitmap = getOriginalImage(sourcePath);
        width = baseBitmap.getWidth();
        height = baseBitmap.getHeight();
        curBitmap = Bitmap.createBitmap(baseBitmap);

        imageEngine = new ImageEngine(mContext);
    }

    public void setFacePoints(int[]facePointsAll)
    {
        hasFace = true;
        mFacePointsAll = facePointsAll.clone();
    }


    public int getWidth()
    {
        return width;
    }
    public int getHeight()
    {
        return height;
    }
    public Bitmap getOriginalImage()
    {
        return getOriginalImage(sourcePath);
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
            options.inSampleSize = 1;
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

    public Bitmap BFSoftskin(int softRatio, int whiteRatio) {

        curBitmap = imageEngine.XIUSDK_SoftSkin(baseBitmap, softRatio, whiteRatio);
        return curBitmap;
    }

    public Bitmap BFEyeWarp(int radius, int ratio) {

        curBitmap = imageEngine.XIUSDK_EyeWarp(baseBitmap, mFacePointsAll, radius, ratio);
        return curBitmap;
    }
    public Bitmap BFEyeBagRemoval(int ratio) {

        curBitmap = imageEngine.XIUSDK_EyeBagRemoval(baseBitmap, mFacePointsAll, ratio);
        return curBitmap;
    }
    public Bitmap BFLightEye(int ratio) {

        curBitmap = imageEngine.XIUSDK_LightEye(baseBitmap, mFacePointsAll, ratio);
        return curBitmap;
    }
    public Bitmap BFHighnose(int ratio) {

        curBitmap = imageEngine.XIUSDK_Highnose(baseBitmap,  mFacePointsAll, ratio);
        return curBitmap;
    }
    public Bitmap BFFaceLift(int ratio, float roll, float pitch, float yaw) {

        curBitmap = imageEngine.XIUSDK_FaceLift(baseBitmap, mFacePointsAll, ratio,roll,pitch,yaw);
        return curBitmap;
    }
    public Bitmap BFDefreckleAuto() {

        curBitmap = imageEngine.XIUSDK_DefreckleAuto(baseBitmap,mFacePointsAll);
        return curBitmap;
    }
}
