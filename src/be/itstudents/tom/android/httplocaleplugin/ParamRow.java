package be.itstudents.tom.android.httplocaleplugin;

import java.util.Map.Entry;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableRow;

public class ParamRow extends TableRow {

	public interface OnChangeListener {
		void changed();
		
	}
	
	private OnChangeListener mOnChanged;
	
	public ParamRow(Context context) {
		super(context);

	}

	public ParamRow(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	private EditText mArg;
	private EditText mValue;

	public static ParamRow newInstance(Context ctx, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(ctx);
		final ParamRow row = (ParamRow) inflater.inflate(R.layout.params_layout, null);

        row.mArg = (EditText)row.findViewById(R.id.arg_text);
        row.mValue = (EditText)row.findViewById(R.id.value_text);
        
        TextWatcher textWatcher = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Stub de la méthode généré automatiquement
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Stub de la méthode généré automatiquement
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (row.mOnChanged != null)
					row.mOnChanged.changed();
				
			}
		};
		row.mArg.addTextChangedListener(textWatcher);
		row.mValue.addTextChangedListener(textWatcher);
		return row;
	}
	
	public void setParam(Entry<String,String> param) {
		mArg.setText(param.getKey());
		mValue.setText(param.getValue());
	}
	
	public String getArg() {
		return mArg.getText().toString();
	}

	public void setmOnChanged(OnChangeListener mOnChanged) {
		this.mOnChanged = mOnChanged;
	}

	public String getValue() {
		return mValue.getText().toString();
	}
	
		
}