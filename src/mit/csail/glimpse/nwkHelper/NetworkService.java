package mit.csail.glimpse.nwkHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mit.csail.glimpse.utility.FrameClass;
import mit.csail.glimpse.utility.ObjectClass;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class NetworkService {


    private static final String TAG = "NetworkService";
    private static final String SERVER_URL = "http://192.168.5.30:8888";
    private SendAsync sp;
    
    // HttpClient used for the requests.
    private final DefaultHttpClient mHttpClient;

    public NetworkService(Activity act){
        mHttpClient = new DefaultHttpClient();
        sp = new SendAsync(act);
    }

    public void sendFrame(Mat mGray){ 
  	  
    	sp.execute(mGray);
  }
    
    public JSONObject CreateRequest(Mat mGray) throws JSONException {
    	
    	JSONObject requestObject = new JSONObject();
    	// compress mat to bitmap//
    	Bitmap bmp = Bitmap.createBitmap(mGray.cols(), mGray.rows(), Bitmap.Config.ARGB_8888);           
    	Utils.matToBitmap(mGray, bmp);
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();    
    	bmp.compress(Bitmap.CompressFormat.JPEG, 70, stream);  
    	
    	// encode bitmap to string using base64 //
    	byte[] stream_bytes = stream.toByteArray();
    	String bitmapBase64 = Base64.encodeToString(stream_bytes, Base64.NO_WRAP);
    	
    	// send frame //
    	JSONObject jsonResponse = null;
    	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); 
    	try {        
    		
    		// wrap request into json //
    		requestObject.put("image", bitmapBase64);
            requestObject.put("filename", timeStamp + ".jpg");
            requestObject.put("w", bmp.getWidth());
            requestObject.put("h", bmp.getHeight());
            
            return requestObject;
            
    	} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return null;
    }

    public void PostAync(JSONObject requestObj) {
    	
    }
    
    private class SendAsync extends AsyncTask<Mat, Void, FrameClass> {
    	
    	private CompleteListener callback;

		public SendAsync(Activity act){  		
      		this.callback = (CompleteListener)act;
      	}
    	
        @Override
        protected FrameClass doInBackground(Mat... data) {
        	Mat mGray = data[0];
        	
        	
        	
			try {
				JSONObject requestObject = CreateRequest(mGray);
				if (requestObject != null){
					
	        		// post the json object
					JSONObject jsonResponse = Post(requestObject); 
					FrameClass frameResponse = parseResponse(jsonResponse); 
	        		return frameResponse;
	        	}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
            return null;
        }

        protected void onPostExecute(FrameClass response){
        	 
        	 callback.responseCallback(response);  
        }
    }

    public JSONObject Post(JSONObject requestObj) {

        HttpPost httpPost = new HttpPost(SERVER_URL);

        if(requestObj != null) {
            String jsonObjectString = requestObj.toString();
            
            try {
                httpPost.setEntity(new StringEntity(jsonObjectString, "UTF-8"));
                
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unable to encode JSON data", e);
            }
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
        }
        return executeHttpMethod(httpPost);
    }
    
    private FrameClass parseResponse(JSONObject jsonResponse){
    	
    	FrameClass fc = new FrameClass();
		try {
			int n_obj = jsonResponse.getInt("objnum");
			if (n_obj > 0) {
				
	    		int label = jsonResponse.getInt("label");
	    		int w = jsonResponse.getInt("w");
	    		int h = jsonResponse.getInt("h");
	    		int x = jsonResponse.getInt("x");
	    		int y = jsonResponse.getInt("y");
	    		double conf = jsonResponse.getDouble("conf");
	    		String featurePts_str = jsonResponse.getString("featurePts");
	    		
	    		
	    		
	    		List<org.opencv.core.Point> featurePts = new ArrayList<org.opencv.core.Point>();
				String[] segs = featurePts_str.split(",");
				for (int i = 0; i < segs.length; ++i){
					String[] pts = segs[i].split(";");
					featurePts.add(new Point(Integer.parseInt(pts[0]), Integer.parseInt(pts[1])));
				}
	    		
	    		ObjectClass oc = new ObjectClass(label, x, y, w, h, featurePts, conf);
	    		fc.push(oc);
	    	} 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		return fc;
    	
    }

    /**
     * This method executes the httpMethod and checks the response. It also evaluates the Status field
     * sent by the Web service.
     *
     * @param httpMethod
     * @return
     */
    private JSONObject executeHttpMethod(final HttpRequestBase httpMethod){

        JSONObject resultJson = null;
        try {
            HttpResponse httpResponse = mHttpClient.execute(httpMethod);
            int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
         
            if(httpStatusCode != HttpStatus.SC_OK) { // 200
                checkHttpStatus(httpMethod, httpStatusCode);
            }
            String json_str = EntityUtils.toString(httpResponse.getEntity());
            resultJson = new JSONObject(json_str);

        } catch(IOException e) {
        	
            Log.e(TAG, "Unable to execute HTTP Method.", e);
            
        } catch (JSONException e) {
        	Log.e(TAG, "Unable to parse JSON response", e);
			
		} finally {
            httpMethod.abort();
        }
        return resultJson;
    }

    /**
     * Checks the HttpStatus code of the Response and throws an appropriate exception, the upper
     * layer is able to catch the exception and notify the user.
     *
     * @param httpMethod
     * @param httpStatusCode
     *
     */
    private void checkHttpStatus(HttpRequestBase httpMethod, int httpStatusCode) {
       
    	if(httpStatusCode == HttpStatus.SC_FORBIDDEN) {
            Log.e(TAG, "Access denied.");
           // throw new AccessDeniedException(requestedPath);
        }
        if(httpStatusCode == HttpStatus.SC_NOT_FOUND) {
            Log.e(TAG, "Resource not found.");
           // throw new ResourceNotFoundException(requestedPath);
        }
        if(httpStatusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            Log.e(TAG, "Internal Server Error.");
          //  throw new InternalServerErrorException(requestedPath);
        }
    }


}
