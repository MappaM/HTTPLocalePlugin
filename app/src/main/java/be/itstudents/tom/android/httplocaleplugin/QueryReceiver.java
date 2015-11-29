package be.itstudents.tom.android.httplocaleplugin;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


public final class QueryReceiver extends BroadcastReceiver
{

	private static final String TAG = "HTTPLocalePlugin.QR";



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

		OkHttpClient client = new OkHttpClient();

		@Override
		public void run() {

			int msg_id;

			if (!isNetworkAvailable()) {
				msg_id = R.string.url_nonetwork;
			} else {



				Request request = new Request.Builder()
						.url(url)
						.build();


				try {
					Response response = client.newCall(request).execute();

					if(response.code() == 200){
						msg_id = R.string.url_called;
					} else{
						msg_id = R.string.url_error;

					}
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
				final int current_delay = delay;
				delay*=2;
				retry++;
				threadHandler.postDelayed(this,current_delay);

			}
		}



	}

	Handler mainHandler = new Handler();
	Handler threadHandler;
	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		if (com.twofortyfouram.locale.api.Intent.ACTION_FIRE_SETTING.equals(intent.getAction()))
		{

			final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE);
			final String url = bundle.getString(Constants.BUNDLE_EXTRA_URL);

			HandlerThread thread = new HandlerThread(TAG);
			thread.start();
			threadHandler = new Handler(thread.getLooper());
			threadHandler.post(new RequestTask(context,url,3000,1,5));

		}
	}
}
