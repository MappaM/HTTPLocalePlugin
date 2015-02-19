package be.itstudents.tom.android.httplocaleplugin;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import be.itstudents.tom.android.httplocaleplugin.ParamRow.OnChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class EditActivity extends ActionBarActivity {

	private EditText url;
	private EditText name;
	private TableLayout tableParams;
	private static final String TAG = "HttpLocalePlugin.EditActivity";

	
	private Map<String,ParamRow> params_map;
	
	/**
	 * 
	 */
	private Map<String,String> getQueryParameter(Uri uri) {
	    if (uri.isOpaque()) {
	    	return Collections.emptyMap();
	    }

	    String query = uri.getEncodedQuery();
	    if (query == null) {
	        return Collections.emptyMap();
	    }

	    Map<String,String> parameters = new LinkedHashMap<String,String>();
	    int start = 0;
	    do {
	        int next = query.indexOf('&', start);
	        int end = (next == -1) ? query.length() : next;

	        int separator = query.indexOf('=', start);
	        if (separator > end || separator == -1) {
	            separator = end;
	        }

	        String name = query.substring(start, separator);
	        String value;
	        if (separator < end)
	        	value = query.substring(separator + 1, end);
	        else
	        	value = "";
	        
	        parameters.put(Uri.decode(name),Uri.decode(value));

	        // Move start to end of name.
	        start = end + 1;
	    } while (start < query.length());

	    return Collections.unmodifiableMap(parameters);
	}
	
	
	private OnChangeListener onParamChanged = new OnChangeListener() {

			
				@Override
				public void changed() {
					if (!autoUpdate) {
						autoUpdate = true;
						Log.v(TAG,"Param changed");
						Uri uri = Uri.parse(url.getText().toString());
						Uri.Builder builder = uri.buildUpon();
						builder.query("");
						for (Entry <String,ParamRow> param : params_map.entrySet()) {
							builder.appendQueryParameter(param.getValue().getArg(), param.getValue().getValue());
						}
						url.setText(builder.build().toString());
						autoUpdate = false;
					}
				
					
				}
	
	};
	protected boolean autoUpdate = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		url = (EditText)findViewById(R.id.url_text);
		name = (EditText)findViewById(R.id.name_text);
		tableParams = (TableLayout)findViewById(R.id.table_params);
		params_map = new LinkedHashMap<String,ParamRow>();
		url.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
								
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			

			@Override
			public void afterTextChanged(Editable s) {
				
				if (!autoUpdate) {
				autoUpdate = true;
				
				Uri uri = Uri.parse(url.getText().toString());
				//tableParams.removeAllViews();
			
				List<ParamRow> found = new LinkedList<ParamRow>();
				List<Entry<String,String>> unfound = new LinkedList<Entry<String,String>>();
				
				for (Entry<String,String> param : getQueryParameter(uri).entrySet()) {
					ParamRow row;
					
					if (params_map.containsKey(param.getKey())) {
						Log.v(TAG,param.getKey() + " FOUND");
						row = params_map.get(param.getKey());
						params_map.remove(param.getKey());
						found.add(row);
						row.setParam(param);
					} else {
						Log.v(TAG,param.getKey() + " UNFOUND");
						unfound.add(param);
					}					
				}
				
				Stack<ParamRow> toRemove = new Stack<ParamRow>();
				for (Entry<String, ParamRow> entry : params_map.entrySet()) {
					Log.v(TAG,entry.getKey() + " REMOVE");
					toRemove.add(entry.getValue());
				}
				params_map.clear();
				
				for (Entry<String,String> param : unfound) {
					ParamRow row;
					if (toRemove.size() > 0) {
						row = toRemove.pop();
					} else {
						row = ParamRow.newInstance(EditActivity.this,tableParams);
						row.setmOnChanged(onParamChanged);
						tableParams.addView(row);
						url.requestFocus();
					}
					row.setParam(param);
					found.add(row);
				}
				
				for (ParamRow row : found) {
					params_map.put(row.getArg(), row);
				}
				
				//Remove params still there
				while (toRemove.size() > 0) {
					ParamRow row = toRemove.pop();
					tableParams.removeView(row);
				}
				
				autoUpdate = false;
				}
				
			}
		});
		
		
		final Bundle localeBundle = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
		
		if (localeBundle != null) {
			url.setText(localeBundle.getString(Constants.BUNDLE_EXTRA_URL));
			name.setText(localeBundle.getString(Constants.BUNDLE_EXTRA_NAME));
			
		} else {
			url.setText("");
			name.setText("");
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
			case R.id.action_save :
				final Intent resultIntent = new Intent();

		        /*
		         * This extra is the data to ourselves: either for the Activity or the BroadcastReceiver. Note
		         * that anything placed in this Bundle must be available to Locale's class loader. So storing
		         * String, int, and other standard objects will work just fine. Parcelable objects are not
		         * acceptable, unless they also implement Serializable. Serializable objects must be standard
		         * Android platform objects (A Serializable class private to this plug-in's APK cannot be
		         * stored in the Bundle, as Locale's classloader will not recognize it).
		         */
		        final Bundle resultBundle = new Bundle();
		        resultBundle.putString(Constants.BUNDLE_EXTRA_URL, url.getText().toString());
		        resultBundle.putString(Constants.BUNDLE_EXTRA_NAME, name.getText().toString());
		        
		        resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, resultBundle);


		        /*
		         * The blurb is concise status text to be displayed in the host's UI.
		         */
		        resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, name.getText().toString());

		        setResult(RESULT_OK, resultIntent);
		        finish();
				
		}
		return super.onOptionsItemSelected(item);
	}
}
