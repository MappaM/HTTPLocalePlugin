package be.itstudents.tom.android.httplocaleplugin;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;


public final class QueryReceiver extends BroadcastReceiver
{

	private static final String TAG = "HTTPLocalePlugin.QueryReceiver";


	
	class RequestTask implements Runnable {

	    private Context context;
	    
	    String url;
	    int delay = 6000;
	    int max_retry = 5;
	    int retry = 1;

		private boolean isNetworkAvailable() {
		    ConnectivityManager connectivityManager 
		          = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		}
		
		public RequestTask(Context context, String url, int delay, int retry, int max_retry) {
			super();
			this.context = context;
			this.delay = delay;
			this.retry = retry;
			this.max_retry = max_retry;
			this.url = url;
		}
		

		@Override
	    public void run() {
			Log.i(TAG,String.format("Trying to call %s",url));
	        
	            int msg_id;

	            if (!isNetworkAvailable()) {
	            	msg_id = R.string.url_nonetwork;
	            } else {
		        	HttpClient httpclient = new DefaultHttpClient();
		            HttpResponse response;
					try {
						response = httpclient.execute(new HttpHead(url));
	
			            StatusLine statusLine = response.getStatusLine();
			            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
			            	msg_id = R.string.url_called;
			            } else{
			            	msg_id = R.string.url_error;
		
			            }
		            
					} catch (ClientProtocolException e) {
						msg_id = R.string.url_exception;
						e.printStackTrace();
					} catch (IOException e) {
						msg_id = R.string.url_exception;
						
						e.printStackTrace();
					}				
	            }
	            
				if (msg_id == R.string.url_called || retry >= max_retry) {
					final String text_msg = context.getString(msg_id);
			    	mainHandler.post(new Runnable() {					
						@Override
						public void run() {
							 int duration = Toast.LENGTH_SHORT;
					            Toast toast = Toast.makeText(context, String.format(text_msg,url), duration);
					        	toast.show();
						}
					});
		        } else {
		        	Log.w(TAG,String.format("Call to url %s failed %d times. Next retry in %d ms",url,retry,delay));
		        	threadHandler.postDelayed(new RequestTask(context,url,delay*2,retry + 1,max_retry),delay);
		        }
	    }

	        
	    
	}
	
	Handler mainHandler = new Handler();
	Handler threadHandler;
    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        if (com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction()))
        {
        	
        	final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        	final String url = bundle.getString(Constants.BUNDLE_EXTRA_URL);
            
        	HandlerThread thread = new HandlerThread(TAG);
        	thread.start();
        	threadHandler = new Handler(thread.getLooper());
        	threadHandler.post(new RequestTask(context,url,6000,1,5));
        	
        }
    }
}
