package xiusdk.tools;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

public class MaskRead {

	public static byte[] GetExternalData (String mskName, int[] mskSize, Context context) {
		byte[] mskData = null;
		InputStream inputStream = null;
		try {
			inputStream = context.getAssets().open(mskName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		DataInputStream in;
		try {			
			in = new DataInputStream(inputStream);			
			byte imgInfo[] = new byte[12]; 

			in.read(imgInfo);

			mskSize[0] = ((imgInfo[3] & 0xFF) << 24) + ((imgInfo[2] & 0xFF) << 16) + ((imgInfo[1] & 0xFF) << 8) + (imgInfo[0] & 0xFF) ;
			mskSize[1] = ((imgInfo[7] & 0xFF) << 24) + ((imgInfo[6] & 0xFF) << 16) + ((imgInfo[5] & 0xFF) << 8) + (imgInfo[4] & 0xFF) ;
			mskSize[2] = ((imgInfo[11]& 0xFF) << 24) + ((imgInfo[10]& 0xFF) << 16) + ((imgInfo[9] & 0xFF) << 8) + (imgInfo[8] & 0xFF);

			mskData = new byte[mskSize[1]*mskSize[2]];
			in.read(mskData);	
			in.close();
			return mskData;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}	

	}

//        int[] noseMskSize = new int[3];
//        byte[]noseMskData = GetExternalData("effect3.data", noseMskSize);
}
