package com.nuance.nmdp.sample;

import java.io.IOException;

import com.nuance.nmdp.speechkit.Prompt;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;
import com.nuance.nmdp.speechkit.Vocalizer;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainView extends Activity {

	static DatabaseHandler db;

	private DataCreation dc;

	private static SpeechKit _speechKit;

	//SpeechKit Instance
	static SpeechKit getSpeechKit()
	{
		return _speechKit;
	}

	//Creating Database
	public void createDb(){
		db = new DatabaseHandler(this);
		Log.v("gtry","database entrypoint");
				//dc = new DataCreation();
				//dc.createData();
		try {
			db.createDataBase();

		} catch (IOException ioe) {
			throw new Error("Unable to create database");
		}
		try {
			db.openDataBase();
		}catch(SQLException sqle){
			throw sqle;
		}
	}

	//Vocalizer Instance
	static Vocalizer getVocalizer()
	{
		return MainView.getSpeechKit().createVocalizerWithLanguage("en_US", MainView.createVocalizerListener(), new Handler());
	}

	static Vocalizer.Listener createVocalizerListener()
	{
		return new Vocalizer.Listener()
		{
			@Override
			public void onSpeakingBegin(Vocalizer vocalizer, String text, Object context) {

				android.util.Log.d("Nuance SampleVoiceApp", "Vocalizer.Listener.onSpeakingBegin: session id ["
						+ MainView.getSpeechKit().getSessionId() + "]");
			}

			@Override
			public void onSpeakingDone(Vocalizer vocalizer,
					String text, SpeechError error, Object context) 
			{
				android.util.Log.d("Nuance SampleVoiceApp", "Vocalizer.Listener.onSpeakingDone: session id ["
						+ MainView.getSpeechKit().getSessionId() + "]");
			}
		};
	}

	//Creating Activity
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//getApplicationContext().deleteDatabase(DatabaseHandler.DATABASE_NAME);
		createDb();
		_speechKit = (SpeechKit)getLastNonConfigurationInstance();
		if (_speechKit == null)
		{
			_speechKit = SpeechKit.initialize(getApplication().getApplicationContext(), AppInfo.SpeechKitAppId, AppInfo.SpeechKitServer, AppInfo.SpeechKitPort, AppInfo.SpeechKitSsl, AppInfo.SpeechKitApplicationKey);
			_speechKit.connect();

			Prompt beep = _speechKit.defineAudioPrompt(R.raw.beep);
			_speechKit.setDefaultRecognizerPrompts(beep, Prompt.vibration(100), null, null);
		}

		playBack("Welcome to Popcorn Time. Click Next to Proceed.");
		final Button dictationButton = (Button)findViewById(R.id.btn_dictation);

		Button.OnClickListener l = new Button.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				if (v == dictationButton)
				{
					Intent intent = new Intent(v.getContext(), MovieView.class);
					MainView.this.startActivity(intent);
				} 
			}
		};

		dictationButton.setOnClickListener(l);
	}

	//Destroying an Activity
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (_speechKit != null)
		{
			_speechKit.release();
			_speechKit = null;
		}
	}

	//Retaining Configuration of the Activity
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		SpeechKit sk = _speechKit;
		_speechKit = null; 
		return sk;
	}

	//System Response method
	private void playBack(String tts){

		if (tts != null)
		{
			Object _lastTtsContext = new Object();
			MainView.getVocalizer().speakString(tts, _lastTtsContext);
		}
	}
}