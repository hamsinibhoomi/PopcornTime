package com.nuance.nmdp.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class SampleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sampleactivity);
		String movieName = getIntent().getStringExtra("movieName");
		Log.v("verbose","movie is -> " + movieName);
		EditText movieField = (EditText) findViewById(R.id.movieField);
		movieField.setText(movieName);
	}
}
