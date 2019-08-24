package xiusdk.tools;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoUtils {
	public static int IMAGE_FORMAT_PNG = 0;
	public static int IMAGE_FORMAT_JPG = 1;

	public static String getShareImageSavePath(String docName,int imageFormat)
    {
    	Date date = new Date();
 		String filename = null;
 		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
 		if(imageFormat == IMAGE_FORMAT_PNG)
 		    filename = "FD_Share"+format.format(date) + ".png";
 		else
 			filename = "FD_Share"+format.format(date) + ".jpg";
 		return Environment.getExternalStorageDirectory().getPath() + "/" + String.valueOf(docName) + "/" + filename;
    }

    public static int readPictureDegree(String path) {
        int degree  = 0;
        try {
                ExifInterface exifInterface = new ExifInterface(path);
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
        } catch (IOException e) {
                e.printStackTrace();
        }
        return degree;
    }

   public static Bitmap rotaingImageView(int angle , Bitmap bitmap) {  

       Matrix matrix = new Matrix();;  
       matrix.postRotate(angle);  
       System.out.println("angle2=" + angle);  

       Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,  
               bitmap.getWidth(), bitmap.getHeight(), matrix, true);  
       return resizedBitmap;  
   }
	public static boolean saveBitmap(Bitmap bmp, String path,int imageFormat, Context context)
	{			
		File file = new File(path);
		FileOutputStream os = null;		
		try
		{
			if(file.exists())
				file.delete();
			if( !file.exists() )
			{
				if( !file.createNewFile() )
					return false;
			}
			os = new FileOutputStream(file);
		}
		catch( FileNotFoundException e )
		{
			
		}
		catch(IOException e)
		{
			
		}
		finally 
		{
		}		
		boolean bSuccess =false;
		if(imageFormat == IMAGE_FORMAT_PNG)
			bSuccess = bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
		else
			bSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 100, os);
		if( !bSuccess )
		{
			file.delete();
		}		
		try {
     	   String fileName = String.copyValueOf(path.toCharArray(), path.length()-18, 18);
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
    				file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
		return bSuccess;
	}
	
	public static Bitmap getBitmapFromResource(Context context,int id)
	{
		Resources res = context.getResources();
	    Bitmap bmp = BitmapFactory.decodeResource(res, id);
	    return bmp;
	}
}
