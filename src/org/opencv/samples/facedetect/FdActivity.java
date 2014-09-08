package org.opencv.samples.facedetect;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Map;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.samples.facedetect.R;

import Utils.CompleteListener;
import Utils.FaceClass;
import Utils.Global;
import Utils.NwkResponse;
import Utils.SocketClient;
import Utils.Tracker;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class FdActivity extends Activity implements CvCameraViewListener2, CompleteListener {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;
    private static int state = 1; // 0: tracking state, 1: transmit
    
    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemType;

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;   

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private CameraBridgeViewBase   mOpenCvCameraView;
    
    private SocketClient 		   socketClient;
    private NwkResponse 		   nwkReponse;
    private Map<Integer, String> labels = new Hashtable<Integer, String>();	
	
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                                            
                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    mOpenCvCameraView.enableView();
                    nwkReponse = new NwkResponse();
                    loadClasses();
                    socketClient = new SocketClient("128.30.79.156", 8888);
                    socketClient.connectWithServer();
                    
                   
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";
               
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setCvCameraViewListener(this);
        //long maxMemory = Runtime.getRuntime().maxMemory();
        //Log.e(TAG, maxMemory + "");
    }
    
    
    public void loadClasses(){
    	BufferedReader reader = null;		
		try {						
			reader = new BufferedReader(new InputStreamReader(
				getAssets().open("final_classes_v2.txt"), "UTF-8")); 			  
			String mLine = reader.readLine();
			int id = 0;
			while (mLine != null) {
				labels.put(id, mLine);
		   	//	System.out.println(id + "," + mLine);
				++id;		    	
				mLine = reader.readLine();		        
			}
			Global.CLASS_NUMBER = id;
		} catch (IOException e) {
			//log the exception
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					//log the exception
				}
			}
		}
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
        try{
        	socketClient.goodBye();
        } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        socketClient.disConnectWithServer();
        
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
    
        // compress //        
        Bitmap bmp = Bitmap.createBitmap(mGray.cols(), mGray.rows(), Bitmap.Config.RGB_565);
           
        Utils.matToBitmap(mGray, bmp);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        long t = System.nanoTime();	
        bmp.compress(Bitmap.CompressFormat.JPEG, 30, stream);
            
        double detectionTime = (System.nanoTime() - t)/ 1000000.0;
        System.out.println("Compression time:" + detectionTime + "height:" + bmp.getHeight() + "width" + bmp.getWidth());        
        byte[] byteArray = stream.toByteArray();
        
        try {     
        	socketClient.sendProcessFrameHeader(0, 0, byteArray.length, mGray.width(), mGray.height());
            socketClient.sendEntireFrame(byteArray, this);		
        } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
        return mRgba;
    }

    
	public void responseCallback(String response) {
		// parse results
		nwkReponse.parseResponse(response);
		System.out.println(response);
		
		/**
		Tracker curTracker = new Tracker();
		if (nwkReponse.faces.size() > 0){
			
			curTracker.init(nwkReponse.frame, nwkReponse.faces.get(0));
			curTracker.track(mGray, nwkReponse.faces.get(0).faceRect);    	
			curTracker.fc = nwkReponse.faces.get(0);
		}
    	**/
		
		if (nwkReponse.faces.size() > 0){
			renderBoundingBox(nwkReponse.faces.get(0));
		}else{
			
		}
	}
    
    public void renderBoundingBox(FaceClass fc){
    	  System.out.println("called");
    	  Core.rectangle(mRgba, fc.faceRect.tl(), fc.faceRect.br(), FACE_RECT_COLOR,3);      	  
      	  Core.putText(mRgba, labels.get(fc.label) , new org.opencv.core.Point(fc.faceRect.tl().x, fc.faceRect.tl().y-20) , 
      				  1, 5,FACE_RECT_COLOR);      	     	   
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemType   = menu.add(mDetectorName[mDetectorType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        else if (item == mItemType) {
            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            setDetectorType(tmpDetectorType);
        }
        return true;
    }

    private void setMinFaceSize(float faceSize) {
       
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                
            } else {
                Log.i(TAG, "Cascade detector enabled");
                
            }
        }
    }
}
