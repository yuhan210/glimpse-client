package mit.csail.glimpse.activity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mit.csail.glimpse.dp.ActiveCache;
import mit.csail.glimpse.dp.CachedFrame;
import mit.csail.glimpse.dp.DPFrameSelection;
import mit.csail.glimpse.nwkHelper.CompleteListener;
import mit.csail.glimpse.nwkHelper.NetworkService;
import mit.csail.glimpse.utility.ObjectClass;
import mit.csail.glimpse.utility.FrameClass;
import mit.csail.glimpse.utility.FrameDifferencing;
import mit.csail.glimpse.utility.Global;
import mit.csail.glimpse.utility.Tracker;
import mit.csail.glimpse.utility.Tracking;

import org.json.JSONException;
import org.json.JSONObject;
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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import java.util.Date;

public class Main extends Activity implements CvCameraViewListener2, CompleteListener {

    private static final String    TAG                 = "Glimpse";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 0, 255, 0);
    public static final int        NOACTIVECACHE       = 0;
    public static final int        ACTIVECACHE     = 1;
    private static int state = 1; // 0: tracking state, 1: transmit
    
    private MenuItem               mItemType;

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;   

    private int                    mSchemeType       = ACTIVECACHE;
    private String[]               mSchemeName;

    private CameraBridgeViewBase   mOpenCvCameraView;
    
    //private SocketClient 		   socketClient;
    //private NwkResponse 		   nwkReponse;
    private NetworkService 		   nwkService;
    
    // active cache
    private ActiveCache            activeCache;
    
    private FrameClass             trackers; 
    private Mat 				   prevFrame;
    private Map<Integer, String> labels = new Hashtable<Integer, String>();	
	
    int frame_count  = 0;
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
                    loadClasses();
                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Main() {
    	
    	mSchemeName = new String[2];
    	mSchemeName[NOACTIVECACHE] = "No active cache";
    	mSchemeName[ACTIVECACHE] = "Active cache";
               
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        
        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback))
        {
        	Log.e(TAG, "OpenCV manager laoding failed");
        }
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        Global.token = Global.TOKENLIMIT;
        
        activeCache = new ActiveCache();
        trackers = new FrameClass();
        nwkService = new NetworkService();
        
        setContentView(R.layout.face_detect_surface_view);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(640, 480);
        //long maxMemory = Runtime.getRuntime().maxMemory();
        //Log.e(TAG, maxMemory + "");
    }
    
    
    public void loadClasses(){
    	BufferedReader reader = null;		
		try {						
			reader = new BufferedReader(new InputStreamReader(
				getAssets().open("label.txt"), "UTF-8")); 			  
			String mLine = reader.readLine();
			int id = 0;
			while (mLine != null) {
				labels.put(id, mLine);		  						    
				mLine = reader.readLine();
				++id;
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
    
  

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

  
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	
    	++frame_count;
    	long captureTime = System.nanoTime();
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        
         
        // Update trackers
        /**
        if (!prevFrame.empty()){
        	for (int i = trackers.getObjNum(); i >= 0 ; ++i){
        		ObjectClass oc = Tracking.run(prevFrame, mGray, trackers.objects.get(i));
        		if (Tracking.doesTrackingWork(oc)){
        			trackers.objects.set(i, oc);
        		}else{
        			trackers.objects.remove(i);
        		}
        	}
        }**/
         
       
      
        if (Global.token > 0){     
        	--Global.token;      	
        	nwkService.sendFrame(this, mGray);
        }
        
        
        return mRgba;
    }
    

    /**
     * Response comes back from the server
     */
	public void responseCallback(FrameClass response) {	
		
		++Global.token;
		System.out.println("callback called: " + Global.token);

		// Replay frames in cache
		
		
		
		// Merge trackers
		int[] foundInd = new int[response.objects.size()];
		for (int i = 0; i < trackers.getObjNum(); ++i){
			
			Boolean found = false;
			for (int j = 0; j < response.objects.size(); ++j){
				if (trackers.objects.get(i).label == response.objects.get(j).label){
					
					// update (TODO: Kalman filter, soft merge label, wrong label)
					trackers.objects.set(i, response.objects.get(j));
					foundInd[j] = 1;
					found = true;
					break;
				}
			}
			
			// do tracking for not found object
 			if (!found){
 				//trackers.get(i).track(mGray, );
 				
			}
		}
		
		// insert new object
		for(int i = 0; i < foundInd.length; ++i){
			if (foundInd[i] == 0){				
				/**
				Tracker t = new Tracker();
				t.init(mGray, response.objects.get(i));
				trackers.add(t);
				**/
			}
		}
		
		
		// render results		
		if (response.getObjNum() > 0){
			renderBoundingBox(response.objects.get(0));
		}else{
			
		}
	}
    
    public void renderBoundingBox(ObjectClass fc){
    	Core.rectangle(mRgba, fc.faceRect.tl(), fc.faceRect.br(), FACE_RECT_COLOR,3);      	  
    	Core.putText(mRgba, labels.get(fc.label) , new org.opencv.core.Point(fc.faceRect.tl().x, fc.faceRect.tl().y-20) , 
      				  1, 3,FACE_RECT_COLOR);      	     	   
    }
    
    
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mItemType   = menu.add(mSchemeName[mSchemeType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mItemType) {
            int tmpDetectorType = (mSchemeType + 1) % mSchemeName.length;
            item.setTitle(mSchemeName[tmpDetectorType]);
            setDetectorType(tmpDetectorType);
        }
        return true;
    }

   
    private void setDetectorType(int type) {
        if (mSchemeType != type) {
        	mSchemeType = type;
            if (type == NOACTIVECACHE) {
                Log.i(TAG, "No active cache enabled");
                
            } else {
                Log.i(TAG, "Active cache enabled");                
            }
        }
    }

    
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }
	
}
