package xiusdk.camera;
import xiusdk.tools.GlobalDefinitions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import com.xiusdk.beautycamera.R;
import com.xiusdk.imageengine.ImageEngine;


import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class CameraGLRender implements GLSurfaceView.Renderer
{

	static final float[][] s_texture = {
		//0.
		{
			0.f, 1.f,
			1.f, 1.f,
			0.f, 0.f,
			1.f, 0.f
		},
		//90
		{
			1.f, 1.f,
			1.f, 0.f,
			0.f, 1.f,
			0.f, 0.f,
		},
		//180
		{
			1.f, 0.f,
			0.f, 0.f,
			1.f, 1.f,
			0.f, 1.f,
		},
		//270
		{
			0.f, 0.f,
			0.f, 1.f,
			1.f, 0.f,
			1.f, 1.f,
		}
	};	

	static final float vertices[] = { -1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f }; //0	


	FloatBuffer mVerticesBuffer;
	FloatBuffer mTextureCoordinateBuffer;

	private int mPositionHandler;
	private int mTextureCoordinateHandler;
	private int mGLUniformTexture;
	private int mGLUniformUvTexture;

	private int mProgramHandler = 0;
	private int mTextureId = 0;
	private int mUvTextureId = 0;

	Context mContext;
	private int mOutWidth;
	private int mOutHeight;
	private int mImageWidth;
	private int mImageHeight;
	private int mOrientation = 3;
	private boolean mFlip = true;
	private boolean mCrop = true;

	private byte[] y = null;
	private byte[] uv = null;
	private final Queue<Runnable> mRunOnDraw;

	private ImageEngine mImageEngine = null;


	public CameraGLRender(Context context) 
	{
		mRunOnDraw = new LinkedList<Runnable>();
		mContext = context;

		mImageEngine = new ImageEngine(mContext);
	}


	private void adjustTextureScaling()
	{
		if(mImageWidth == 0 || mImageHeight == 0 || mOutWidth == 0 || mOutHeight == 0)
		{			
			return;	
		}

		if(mOrientation > 3)  mOrientation = 0;
		if(mOrientation < 0 ) mOrientation = 0;

		int width  =  mOutWidth;
		int height = mOutHeight;
		int outputWidth = mImageWidth;
		int outputHeight = mImageHeight;
		if( mOrientation == 1 || mOrientation == 3 )
		{
			outputWidth  =  mImageHeight;
			outputHeight = mImageWidth;
		}

		float ratio1 = (float)(width*1./outputWidth);
		float ratio2 = (float)(height*1./outputHeight);
		ratio1 = ratio1 > ratio2 ? ratio2 : ratio1;
		outputWidth  = (int)(outputWidth*ratio1+0.5);
		outputHeight = (int)(outputHeight*ratio1+0.5);
		float ratioWidth = 1.f;
		float ratioHeight = 1.f;

		if( outputWidth != width )
		{
			ratioWidth = (outputWidth*1.f/width);
		}
		else if( outputHeight != height)
		{
			ratioHeight = (outputHeight*1.f/height);
		}	

		float[] texture = new float[8];
		System.arraycopy(s_texture[mOrientation], 0, texture, 0, s_texture[mOrientation].length);

		if(mFlip)
		{
			for( int i = 1; i < 8; i+=2)
			{
				texture[i] = 1.f - texture[i]; // exchange 0. and 1.
			}
		}


		if( mCrop )
		{
			float distHorizontal = (1.f/ ratioWidth - 1.f) / 2.f;
			float disVertical    = (1.f/ ratioHeight - 1.f) / 2.f;
			for( int i = 0; i < 8; i+=2)
			{			
				texture[i]   = (texture[i])   < 0.0001 ? (distHorizontal) : (1.f - (distHorizontal));
				texture[i+1] = (texture[i+1]) < 0.0001 ? (disVertical) : (1.f - (disVertical));
			}
		}
		else
		{
			for( int i = 0; i < 8; i+=2)
			{
				vertices[i]   = vertices[i] * ratioWidth;
				vertices[i+1] = vertices[i+1] * ratioHeight;
			}
		}		

		mVerticesBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mVerticesBuffer.put(vertices).position(0);

		mTextureCoordinateBuffer = ByteBuffer.allocateDirect(texture.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTextureCoordinateBuffer.put(texture).position(0);

	}


	private void OpenglGLESDestroy()
	{	
		int[] textures = new int[2];
		if(0 != mTextureId || 0 != mUvTextureId)
		{
			textures[0] = mTextureId;
			textures[1] = mUvTextureId;            
			GLES20.glDeleteTextures(2, textures, 0);	
			mTextureId = 0;	
			mUvTextureId = 0;
		}
		if(0 != mProgramHandler)
		{
			GLES20.glDeleteProgram(mProgramHandler);
			mProgramHandler = 0;
		}		

	}


	private int LoadShader(int type, String shaderSrc)
	{
		int shaderHandle = GLES20.glCreateShader(type);
		if (shaderHandle != 0)
		{
			GLES20.glShaderSource(shaderHandle, shaderSrc);
			GLES20.glCompileShader(shaderHandle);
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			if (compileStatus[0] == 0) 
			{				
				Log.d("OpenGLES", "Compilation\n" + GLES20.glGetShaderInfoLog(shaderHandle));
				GLES20.glDeleteShader(shaderHandle);
				shaderHandle = 0;
			}
		}		
		return shaderHandle;
	}


	private void OpenglGLESInitWidthShader()
	{
		OpenglGLESDestroy();
		int vertexShaderHandle = 0;
		int fragmentShaderHandle = 0;

		vertexShaderHandle   = LoadShader(GLES20.GL_VERTEX_SHADER, loadStringFromRaw(mContext, R.raw.filter_vs));
		fragmentShaderHandle = LoadShader(GLES20.GL_FRAGMENT_SHADER, loadStringFromRaw(mContext, R.raw.filter_fs));

		if(0 == mProgramHandler)
		{			
			int programHandle = GLES20.glCreateProgram();
			if (programHandle != 0) 
			{
				GLES20.glAttachShader(programHandle, vertexShaderHandle);
				GLES20.glAttachShader(programHandle, fragmentShaderHandle);
				GLES20.glBindAttribLocation(programHandle, 0, "position");
				GLES20.glBindAttribLocation(programHandle, 1, "inputTextureCoordinate");
				GLES20.glLinkProgram(programHandle);
				final int[] linkStatus = new int[1];
				GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS,	linkStatus, 0);
				if (linkStatus[0] == 0) 
				{
					GLES20.glDeleteProgram(programHandle);
					programHandle = 0;
				}
			}
			if (programHandle == 0) {
				throw new RuntimeException("Error creating program.");
			}
			mProgramHandler = programHandle;

			mPositionHandler = GLES20.glGetAttribLocation(mProgramHandler, "position");
			mTextureCoordinateHandler = GLES20.glGetAttribLocation(mProgramHandler,"inputTextureCoordinate");
			mGLUniformTexture   = GLES20.glGetUniformLocation(mProgramHandler,"inputImageTexture");
			mGLUniformUvTexture = GLES20.glGetUniformLocation(mProgramHandler,"uvTexture");			

		}

		GLES20.glDeleteShader(vertexShaderHandle);
		GLES20.glDeleteShader(fragmentShaderHandle);
	}


	public void onSurfaceCreated(GL10 gl, EGLConfig config) {		
	}


	public void onDrawFrame(GL10 gl) {
		synchronized(mRunOnDraw)
		{
			while(!mRunOnDraw.isEmpty())
			{
				mRunOnDraw.poll().run();
			}
		}		

	}

	public void onSurfaceChanged(GL10 gl, int width, int height) { //screen		

		OpenglGLESInitWidthShader();

		if( mOutWidth != width || mOutHeight != height)
		{
			mOutWidth  = width;
			mOutHeight = height;
			adjustTextureScaling();
		}

	}

	public String loadStringFromRaw(Context context, int rawId) 
	{
		StringBuilder sb = new StringBuilder();
		String l;
		try {
			InputStream is = context.getResources().openRawResource(rawId);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while ((l = br.readLine()) != null) 
			{
				sb.append(l);				
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	public void setupCamera(final int orientation, final boolean flip)
	{
		runOnDraw( new Runnable(){
			@Override
			public void run(){
				mOrientation = orientation;
				mFlip = flip;
				adjustTextureScaling();				
			}
		});
	}


	public void runOnDraw(final Runnable runnable)
	{
		synchronized(mRunOnDraw)
		{
			mRunOnDraw.add(runnable);
		}
	}


	public void OpenglGLESRenderFrame(int width, int height, byte[] data)
	{
		if( mImageWidth != width || mImageHeight != height)
		{
			mImageWidth = width;
			mImageHeight = height;
			adjustTextureScaling();
		}	

		int[] textures = new int[2];
		if(0 == mTextureId || 0 == mUvTextureId)
		{
			GLES20.glGenTextures(2, textures, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,	GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,	GLES20.GL_CLAMP_TO_EDGE);


			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			mTextureId   = textures[0];
			mUvTextureId = textures[1];

		}


		int l_size =  width * height;
		int length = l_size * 3 / 2;
		if(data.length==length)
		{
			int uv_size =  length - l_size;				
			if( y == null || y.length != l_size)
			{
				y  = new byte[l_size];
				uv = new byte[uv_size];
			}

			System.arraycopy(data, 0, y, 0, l_size);
			System.arraycopy(data, l_size, uv, 0, uv_size);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width, height, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE,ByteBuffer.wrap(y));
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUvTextureId);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0,  GLES20.GL_LUMINANCE_ALPHA, width / 2, height / 2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE,ByteBuffer.wrap(uv));

		}



		GLES20.glUseProgram(mProgramHandler);
		GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		GLES20.glClearColor(0, 0, 0, 0);
		GLES20.glViewport(0, 0, mOutWidth, mOutHeight);

		GLES20.glEnableVertexAttribArray(mPositionHandler);
		GLES20.glEnableVertexAttribArray(mTextureCoordinateHandler);

		GLES20.glVertexAttribPointer(mPositionHandler, 3, GLES20.GL_FLOAT, false, 0, mVerticesBuffer);		
		GLES20.glVertexAttribPointer(mTextureCoordinateHandler, 2, GLES20.GL_FLOAT, false, 0, mTextureCoordinateBuffer);		

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
		GLES20.glUniform1i(mGLUniformTexture, 0);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUvTextureId);
		GLES20.glUniform1i(mGLUniformUvTexture, 1);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		GLES20.glDisableVertexAttribArray(mPositionHandler);
		GLES20.glDisableVertexAttribArray(mTextureCoordinateHandler);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

	}

	public void onPreviewFrame(final byte[] data, final Camera camera) 
	{		
		final int width = camera.getParameters().getPreviewSize().width;
		final int height = camera.getParameters().getPreviewSize().height;
		runOnDraw(new Runnable(){
			@Override
			public void run(){

				mImageEngine.XIUSDK_SoftenSkin(data, width, height, GlobalDefinitions.softRatio, GlobalDefinitions.whiteRatio,
						GlobalDefinitions.filterId, GlobalDefinitions.filterRatio);

				OpenglGLESRenderFrame(width, height, data);
				try	{
					camera.addCallbackBuffer(data);
				}
				catch(Exception ex)	{
				}
			}
		});
	}		


}
