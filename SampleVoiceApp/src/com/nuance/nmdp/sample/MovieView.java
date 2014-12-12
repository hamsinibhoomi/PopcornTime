package com.nuance.nmdp.sample;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.Vocalizer;
import com.nuance.nmdp.sample.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MovieView extends Activity  {

	private static final int LISTENING_DIALOG = 0;
	private Handler _handler = null;
	private final Recognizer.Listener _listener;
	private Recognizer _currentRecognizer;
	private ListeningDialog _listeningDialog;
	private ArrayAdapter<String> _arrayAdapter;
	private boolean _destroyed;
	private Vocalizer _vocalizer;
	List<MovieTable> movieResult;
	String selectedMovie;

	//Saved State Class
	private class SavedState
	{
		String DialogText;
		String DialogLevel;
		boolean DialogRecording;
		Recognizer Recognizer;
		Handler Handler;
		Vocalizer Vocalizer;
	}

	//Constructor
	public MovieView()
	{
		super();
		_listener = createListener();
		_currentRecognizer = null;
		_listeningDialog = null;
		_destroyed = true; 
		_vocalizer = MainView.getVocalizer();
	}

	//Dialog Start
	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		switch(id)
		{
		case LISTENING_DIALOG:
			_listeningDialog.prepare(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					if (_currentRecognizer != null)
					{
						_currentRecognizer.stopRecording();
					}
				}
			});
			break;
		}
	}

	//Creating Dialog
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id)
		{
		case LISTENING_DIALOG:
			return _listeningDialog;
		}
		return null;
	}

	//Creating Activity
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setVolumeControlStream(AudioManager.STREAM_MUSIC); 
		setContentView(R.layout.dictation);
		MainView.db.open();
		movieResult = MainView.db.getAllMovies();
		MainView.db.close();

		_destroyed = false;

		playBack("What movie are you in for?");

		Log.v("verbose", movieResult.toString());
		ArrayAdapter<MovieTable> moviesAdapter = new ArrayAdapter<MovieTable>(this, R.layout.listitem, movieResult);
		ListView movies = (ListView) findViewById(R.id.list_movies);
		movies.setAdapter(moviesAdapter);

		final Button dictationButton = (Button)findViewById(R.id.btn_startDictation);

		Button.OnClickListener startListener = new Button.OnClickListener()
		{
			@Override
			public void onClick(View v) {

				_listeningDialog.setText("Initializing...");

				showDialog(LISTENING_DIALOG);

				_listeningDialog.setStoppable(false);

				setResults(new Recognition.Result[0]);

				if (v == dictationButton){

					_currentRecognizer = MainView.getSpeechKit().createRecognizer(Recognizer.RecognizerType.Dictation, Recognizer.EndOfSpeechDetection.Long, "en_US", _listener, _handler);
				}else{

					_currentRecognizer = MainView.getSpeechKit().createRecognizer(Recognizer.RecognizerType.Search, Recognizer.EndOfSpeechDetection.Short, "en_US", _listener, _handler);
				}	
				_currentRecognizer.start();
			}
		};
		dictationButton.setOnClickListener(startListener);

		Button button = (Button)findViewById(R.id.btn_startTts);
		button.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v) {

				EditText t = (EditText)findViewById(R.id.text_DictationResult);
				Log.v("gtry", "starting vocalizer listener");
				_vocalizer = MainView.getVocalizer();

				String tts = t.getText().toString();
				if (tts != null)
				{
					Object _lastTtsContext = new Object();
					_vocalizer.speakString(tts, _lastTtsContext);
				}

			}
		});

		ListView list = (ListView)findViewById(R.id.list_results);
		_arrayAdapter = new ArrayAdapter<String>(list.getContext(), R.layout.listitem)
				{
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				Button b = (Button)super.getView(position, convertView, parent);
				b.setBackgroundColor(Color.GREEN);
				b.setOnClickListener(new Button.OnClickListener()
				{
					@Override
					public void onClick(View v) {
						Button b = (Button)v;
						EditText t = (EditText)findViewById(R.id.text_DictationResult);

						String text = b.getText().toString();
						int startIndex = text.indexOf("]: ");
						t.setText(text.substring(startIndex > 0 ? (startIndex + 3) : 0));

					}
				});
				return b;
			}   
				};
				list.setAdapter(_arrayAdapter);

				createListeningDialog();
				Log.v("gtry", "initializing listening dialog");
				SavedState savedState = (SavedState)getLastNonConfigurationInstance();
				if (savedState == null)
				{
					_handler = new Handler();
				} else {
					_currentRecognizer = savedState.Recognizer;
					_listeningDialog.setText(savedState.DialogText);
					_listeningDialog.setLevel(savedState.DialogLevel);
					_listeningDialog.setRecording(savedState.DialogRecording);
					_handler = savedState.Handler;

					if (savedState.DialogRecording)
					{
						Log.v("gtry", "calling onRecordingBegun()");
						_listener.onRecordingBegin(_currentRecognizer);
						Log.v("gtry", "called onRecordingBegun()");
					}

					_currentRecognizer.setListener(_listener);
					Log.v("gtry", "setListener");
				}

	}

	//Destroying Activity
	@Override
	protected void onDestroy() {
		super.onDestroy();
		_destroyed = true;
		if (_currentRecognizer !=  null)
		{
			_currentRecognizer.cancel();
			_currentRecognizer = null;
		}
	}

	//Retaining configuration of the Activity
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		if (_listeningDialog.isShowing() && _currentRecognizer != null)
		{
			// If a recognition is in progress, save it, because the activity
			// is about to be destroyed and recreated
			SavedState savedState = new SavedState();
			savedState.Recognizer = _currentRecognizer;
			savedState.DialogText = _listeningDialog.getText();
			savedState.DialogLevel = _listeningDialog.getLevel();
			savedState.DialogRecording = _listeningDialog.isRecording();
			savedState.Handler = _handler;
			savedState.Vocalizer = _vocalizer;
			_currentRecognizer = null; // Prevent onDestroy() from canceling
			return savedState;
		}
		return null;
	}

	//Recognizer
	private Recognizer.Listener createListener()
	{
		return new Recognizer.Listener()
		{            
			@Override
			public void onRecordingBegin(Recognizer recognizer) 
			{
				Log.v("gtry", "begun recording");
				_listeningDialog.setText("Recording...");
				_listeningDialog.setStoppable(true);
				_listeningDialog.setRecording(true);

				// Create a repeating task to update the audio level
				Runnable r = new Runnable()
				{
					public void run()
					{	
						if (_listeningDialog != null && _listeningDialog.isRecording() && _currentRecognizer != null)
						{
							_listeningDialog.setLevel(Float.toString(_currentRecognizer.getAudioLevel()));
							_handler.postDelayed(this, 500);
						}
						Log.v("gtry","Thread ends");
					}
				};
				r.run();
				Log.v("verbose", "inside on begin");
			}

			@Override
			public void onRecordingDone(Recognizer recognizer) 
			{
				_listeningDialog.setText("Processing...");
				_listeningDialog.setLevel("");
				_listeningDialog.setRecording(false);
				_listeningDialog.setStoppable(false);

				Log.v("verbose", "inside on recording done");
			}

			@Override
			public void onError(Recognizer recognizer, SpeechError error) 
			{
				if (recognizer != _currentRecognizer) return;
				if (_listeningDialog.isShowing()) dismissDialog(LISTENING_DIALOG);
				_currentRecognizer = null;
				_listeningDialog.setRecording(false);

				String detail = error.getErrorDetail();
				String suggestion = error.getSuggestion();
				if (suggestion == null) suggestion = "";
				setResult(detail + "\n" + suggestion);
				
				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onError: session id ["
						+ MainView.getSpeechKit().getSessionId() + "]");
				Log.v("verbose", "inside on error");
			}

			@Override
			public void onResults(Recognizer recognizer, Recognition results) {
				setResult("Ok this is here now");
				if (_listeningDialog.isShowing()) dismissDialog(LISTENING_DIALOG);
				_currentRecognizer = null;
				_listeningDialog.setRecording(false);
				int count = results.getResultCount();
				Recognition.Result [] rs = new Recognition.Result[count];
				for (int i = 0; i < count; i++)
				{
					rs[i] = results.getResult(i);
				}
				setResults(rs);

				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onResults: session id ["
						+ MainView.getSpeechKit().getSessionId() + "]");
				Log.v("verbose", "inside on results");

				Log.v("verbose", "inside on results - selected movie " + selectedMovie);
			
				if(containsMovie()){
					Intent i = new Intent(MovieView.this, TheatreView.class);
					i.putExtra("movieName", selectedMovie);
					startActivity(i);
				}
				else if(selectedMovie.equalsIgnoreCase("Back")){
					MovieView.this.finish();
				}
				else {
					playBack("Could not find the movie. Please try another movie");
				}
			}
		};
	}

	//Taking result from the edit text box
	private void setResult(String result)
	{
		EditText t = (EditText)findViewById(R.id.text_DictationResult);
		if (t != null){
			t.setText(result);
			selectedMovie=result;
			Log.v("PopcornTime", result);

		}
	}

	//Setting results to the value from edit text box
	private void setResults(Recognition.Result[] results)
	{
		_arrayAdapter.clear();
		if (results.length > 0)
		{
			setResult(results[0].getText());

			for (int i = 0; i < results.length; i++)
				_arrayAdapter.add("[" + results[i].getScore() + "]: " + results[i].getText());
		}  else
		{
			setResult("");
		}
	}

	//Checking for the movie in the database
	private boolean containsMovie(){
		Iterator<MovieTable> itr = movieResult.iterator();
		Log.v("gtry", selectedMovie);
		while(itr.hasNext()){
			String dbMovieName=itr.next().getMovie();
			if(selectedMovie.equalsIgnoreCase(dbMovieName)){
				selectedMovie=dbMovieName;
				return true;
			}

		}
		return false;
	}
	
	//Dialog Listener
	private void createListeningDialog()
	{
		_listeningDialog = new ListeningDialog(this);
		_listeningDialog.setOnDismissListener(new OnDismissListener()
		{
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (_currentRecognizer != null) // Cancel the current recognizer
				{
					_currentRecognizer.cancel();
					_currentRecognizer = null;
				}

				if (!_destroyed)
				{
					MovieView.this.removeDialog(LISTENING_DIALOG);
					createListeningDialog();
				}
			}
		});
	}
	
	//System Response method
	private void playBack(String tts){

		if (tts != null)
		{
			Object _lastTtsContext = new Object();
			_vocalizer.speakString(tts, _lastTtsContext);
		}
	}

}
