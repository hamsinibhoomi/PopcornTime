package com.nuance.nmdp.sample;

import com.nuance.nmdp.speechkit.Vocalizer;

import android.app.Activity;
import android.drm.DrmStore.Playback;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class FinalView extends Activity {

	private Vocalizer _vocalizer5;
	public FinalView()
	{
		super();
		_vocalizer5 = MainView.getVocalizer();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finalview);
		TextView movieField = (TextView) findViewById(R.id.movieField);
		movieField.setText("Thank you for your purchase!! Enjoy your Movie!!!");
		playBack("Enjoy your Movie!!! Thank you for using Popcorn time!!!");

	}

	private void playBack(String tts){

		if (tts != null)
		{
			Object _lastTtsContext = new Object();
			_vocalizer5.speakString(tts, _lastTtsContext);
		}
	}
}
