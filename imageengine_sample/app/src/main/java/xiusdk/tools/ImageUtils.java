package xiusdk.tools;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class ImageUtils {
	public static String getImageSavePath(String docName)
    {
    	Date date = new Date();
 		String filename = null;
 		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String local_file = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + String.valueOf(docName);
        File f = new File(local_file);
        if(!f.exists()){
            f.mkdirs();
        }

 		filename = "IMAGEXIU_Res"+format.format(date) + ".jpg";
 		return f.getAbsolutePath() + "/" + filename;
    }
	public static void saveBitmapOriginal(byte[]data,String docName, String mImgPath)
	{
 		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) 
 		{
 			File dir = new File(Environment.getExternalStorageDirectory(),String.valueOf(docName));
 			if (!dir.exists()) 
 				dir.mkdir();
 		}
        File pictureFile = new File(mImgPath);
        if (pictureFile == null){ 
            Log.d(GlobalDefinitions.TAG, "Error creating media file, check storage permissions. "); 
            
        } 
        try { 
            FileOutputStream fos = new FileOutputStream(pictureFile); 
            fos.write(data); 
            fos.close(); 
        } catch (FileNotFoundException e) { 
            Log.d(GlobalDefinitions.TAG, "File not found: " + e.getMessage()); 
        } catch (IOException e) { 
            Log.d(GlobalDefinitions.TAG, "Error accessing file: " + e.getMessage()); 
        } 
	}
}
