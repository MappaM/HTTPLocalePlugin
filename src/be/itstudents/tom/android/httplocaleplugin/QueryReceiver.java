package be.itstudents.tom.android.httplocaleplugin;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;


public final class QueryReceiver extends BroadcastReceiver
{


	class RequestTask extends AsyncTask<String, Integer, Integer>{

	    private Context context;
	    String url;

		public RequestTask(Context context) {
			super();
			this.context = context;
		}

		@Override
	    protected Integer doInBackground(String... uri) {
	    
	        
	            int msg_id;


	        		this.url = uri[0];
	        	HttpClient httpclient = new DefaultHttpClient();
	            HttpResponse response;
				try {
					response = httpclient.execute(new HttpHead(uri[0]));

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
				

	        return msg_id;
	    }

	    @Override
	    protected void onPostExecute(final Integer result) {
	        super.onPostExecute(result);
        	mainHandler.post(new Runnable() {
				
				@Override
				public void run() {
					 int duration = Toast.LENGTH_SHORT;
			            Toast toast = Toast.makeText(context, String.format(context.getString(result),url), duration);
			        	toast.show();
				}
			});
	    }
	}
	
	Handler mainHandler = new Handler();
	
    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        if (com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction()))
        {
        	
        	final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        	final String url = bundle.getString(Constants.BUNDLE_EXTRA_URL);
            
        	new RequestTask(context).execute(url);
        	
        }
    }
}
