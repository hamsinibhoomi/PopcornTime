package com.nuance.nmdp.sample;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.shortdistance.LevenshteinDistance;

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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.Vocalizer;

public class TheatreView extends Activity{
	private static final int LISTENING_DIALOG1 = 0;
	private Handler _handler1 = null;
	private final Recognizer.Listener _listener1;
	private Recognizer _currentRecognizer1;
	private ListeningDialog _listeningDialog1;
	private ArrayAdapter<String> _arrayAdapter1;
	private boolean _destroyed1;
	private Vocalizer _vocalizer1;
	List<TheatreTable> theatreResult;
	String selectedTheatre;
	String selectedMovie;
	String savedSelectedMovie = "savedselectedmovie";
	List theatresList;

	//Saved State
	private class SavedState
	{
		String DialogText1;
		String DialogLevel1;
		boolean DialogRecording1;
		Recognizer Recognizer1;
		Handler Handler1;
		Vocalizer Vocalizer1;
		String selectedMovie;
		List<TheatreTable> theatreResult;
	}

	//Constructor
	public TheatreView()
	{
		super();
		_listener1 = createListener();
		_currentRecognizer1 = null;
		_listeningDialog1 = null;
		_destroyed1 = true;
		_vocalizer1 = MainView.getVocalizer();
	}

	//Preparing Dialog
	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		switch(id)
		{
		case LISTENING_DIALOG1:
			_listeningDialog1.prepare(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					if (_currentRecognizer1 != null)
					{
						_currentRecognizer1.stopRecording();
					}
				}
			});
			break;
		}
	}
	
	//Saved State of the Activity
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState){
		savedInstanceState.putString(savedSelectedMovie, selectedMovie);
		super.onSaveInstanceState(savedInstanceState);
	}
	
	//Creating Dialog
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id)
		{
		case LISTENING_DIALOG1:
			return _listeningDialog1;
		}
		return null;
	}

	//Creating Activity
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null){
			selectedMovie = savedInstanceState.getString(savedSelectedMovie);
			MainView.db.open();
			theatreResult = MainView.db.getMovieTheatreList(selectedMovie);
			MainView.db.close();
		}
		else {

			selectedMovie=getIntent().getStringExtra("movieName");
			Log.v("verbose","movie is -> " + selectedMovie);
			setVolumeControlStream(AudioManager.STREAM_MUSIC); 
			setContentView(R.layout.theatre);

			Log.v("gtry", "retrieving theatre list");
			MainView.db.open();
			theatreResult = MainView.db.getMovieTheatreList(selectedMovie);
			if(theatreResult.size()>0){
				Log.v("db", "list --"+theatreResult.get(0).getTheatreName());
			}
			else{
				playBack(selectedMovie+" is not playing currently at nearby cinemas..Please try another movie");
				TheatreView.this.finish();
			}
			MainView.db.close();

			_destroyed1 = false;

			playBack(selectedMovie+" is playing at the following theatres.");

			//display theatre list
			ArrayAdapter<TheatreTable> theatreAdapter = new ArrayAdapter<TheatreTable>(this, R.layout.listitem, theatreResult);
			ListView theatres = (ListView) findViewById(R.id.list_theatres);
			theatres.setAdapter(theatreAdapter);

			final Button theatreButton = (Button)findViewById(R.id.btn_startTheatre);
			Button.OnClickListener startListener = new Button.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					_listeningDialog1.setText("Initializing...");

					showDialog(LISTENING_DIALOG1);

					_listeningDialog1.setStoppable(false);

					setResults(new Recognition.Result[0]);

					if (v == theatreButton)
						_currentRecognizer1 = MainView.getSpeechKit().createRecognizer(Recognizer.RecognizerType.Dictation, Recognizer.EndOfSpeechDetection.Long, "en_US", _listener1, _handler1);
					else
						_currentRecognizer1 = MainView.getSpeechKit().createRecognizer(Recognizer.RecognizerType.Search, Recognizer.EndOfSpeechDetection.Short, "en_US", _listener1, _handler1);
					_currentRecognizer1.start();
				}
			};
			theatreButton.setOnClickListener(startListener);
			Button button = (Button)findViewById(R.id.btn_startTts1);
			button.setOnClickListener(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v) {

					EditText t = (EditText)findViewById(R.id.text_TheatreResult);     
					_vocalizer1 = MainView.getVocalizer();

					String tts = t.getText().toString();
					if (tts != null)
					{
						Object _lastTtsContext = new Object();
						_vocalizer1.speakString(tts, _lastTtsContext);
					}

				}
			});

			ListView list = (ListView)findViewById(R.id.list_results1);
			_arrayAdapter1 = new ArrayAdapter<String>(list.getContext(), R.layout.listitem)
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
							EditText t = (EditText)findViewById(R.id.text_TheatreResult);

							// Copy the text (without the [score]) into the edit box
							String text = b.getText().toString();
							int startIndex = text.indexOf("]: ");
							t.setText(text.substring(startIndex > 0 ? (startIndex + 3) : 0));

						}
					});
					return b;
				}   
					};
					list.setAdapter(_arrayAdapter1);

					createListeningDialog();

					SavedState savedState = (SavedState)getLastNonConfigurationInstance();
					if (savedState == null)
					{
						_handler1 = new Handler();
					} else
					{
						_currentRecognizer1 = savedState.Recognizer1;
						_listeningDialog1.setText(savedState.DialogText1);
						_listeningDialog1.setLevel(savedState.DialogLevel1);
						_listeningDialog1.setRecording(savedState.DialogRecording1);
						_handler1 = savedState.Handler1;

						if (savedState.DialogRecording1)
						{
							
							_listener1.onRecordingBegin(_currentRecognizer1);
						}

						_currentRecognizer1.setListener(_listener1);
					}

		}

	}

	//Destroying an activity
	@Override
	protected void onDestroy() {
		super.onDestroy();
		_destroyed1 = true;
		if (_currentRecognizer1 !=  null)
		{
			_currentRecognizer1.cancel();
			_currentRecognizer1 = null;
		}
	}

	//Retaining Activity 
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		if (_listeningDialog1.isShowing() && _currentRecognizer1 != null)
		{
			// If a recognition is in progress, save it, because the activity
			// is about to be destroyed and recreated
			SavedState savedState = new SavedState();
			savedState.Recognizer1 = _currentRecognizer1;
			savedState.DialogText1 = _listeningDialog1.getText();
			savedState.DialogLevel1 = _listeningDialog1.getLevel();
			savedState.DialogRecording1 = _listeningDialog1.isRecording();
			savedState.Handler1 = _handler1;
			savedState.Vocalizer1 = _vocalizer1;
			savedState.selectedMovie = selectedMovie;
			savedState.theatreResult = theatreResult;

			_currentRecognizer1 = null; // Prevent onDestroy() from canceling
			return savedState;
		}
		return null;
	}

	private Recognizer.Listener createListener()
	{
		return new Recognizer.Listener()
		{            
			@Override
			public void onRecordingBegin(Recognizer recognizer) 
			{
				_listeningDialog1.setText("Recording...");
				_listeningDialog1.setStoppable(true);
				_listeningDialog1.setRecording(true);

				// Create a repeating task to update the audio level
				Runnable r = new Runnable()
				{
					public void run()
					{
						if (_listeningDialog1 != null && _listeningDialog1.isRecording() && _currentRecognizer1 != null)
						{
							_listeningDialog1.setLevel(Float.toString(_currentRecognizer1.getAudioLevel()));
							_handler1.postDelayed(this, 500);
						}
					}
				};
				r.run();
			}

			@Override
			public void onRecordingDone(Recognizer recognizer) 
			{
				_listeningDialog1.setText("Processing...");
				_listeningDialog1.setLevel("");
				_listeningDialog1.setRecording(false);
				_listeningDialog1.setStoppable(false);


			}

			@Override
			public void onError(Recognizer recognizer, SpeechError error) 
			{
				if (recognizer != _currentRecognizer1) return;
				if (_listeningDialog1.isShowing()) dismissDialog(LISTENING_DIALOG1);
				_currentRecognizer1 = null;
				_listeningDialog1.setRecording(false);

				String detail = error.getErrorDetail();
				String suggestion = error.getSuggestion();
				if (suggestion == null) suggestion = "";
				setResult(detail + "\n" + suggestion);
				
				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onError: session id ["
						+ MainView.getSpeechKit().getSessionId() + "]");
			}

			@Override
			public void onResults(Recognizer recognizer, Recognition results) {
				if (_listeningDialog1.isShowing()) dismissDialog(LISTENING_DIALOG1);
				_currentRecognizer1 = null;
				_listeningDialog1.setRecording(false);
				int count = results.getResultCount();
				Recognition.Result [] rs = new Recognition.Result[count];
				for (int i = 0; i < count; i++)
				{
					rs[i] = results.getResult(i);
				}
				setResults(rs);

				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onResults: session id ["
						+ MainView.getSpeechKit().getSessionId() + "]");
				Log.v("verbose", selectedTheatre);
				if(containsTheatre()){
					Intent i = new Intent(TheatreView.this, ShowDateView.class);
					i.putExtra("movieName", selectedMovie);
					i.putExtra("theatreName", selectedTheatre);
					startActivity(i);
				}
				else if(selectedTheatre.equalsIgnoreCase("Back")||selectedTheatre.matches("([A|a]n)?[o|O]ther [M|m]ovie|[S|s]omething [E|e]lse|[G|g]o [t|T]o [M|m]ovie|([C|c]hange)? [M|m]ovie")){
					TheatreView.this.finish();
				}
				else{
					playBack("Could not find the theatre. Please try another theatre");

				}
			}
		};
	}

	private void setResult(String result)
	{
		EditText t = (EditText)findViewById(R.id.text_TheatreResult);
		if (t != null){
			t.setText(result);
			selectedTheatre = result;
			Log.v("PopcornTime", result);
		}
	}
	private boolean containsTheatre(){
		Iterator<TheatreTable> itr = theatreResult.iterator();
		Log.v("gtry", selectedTheatre);
		while(itr.hasNext()){
			String dbTheatre = itr.next().getTheatreName();
			if(selectedTheatre.equalsIgnoreCase(dbTheatre)){
				selectedTheatre = dbTheatre;
				return true;
			}
		}
			
		return false;
	}
	private void setResults(Recognition.Result[] results)
	{

		_arrayAdapter1.clear();
		if (results.length > 0)
		{
			setResult(results[0].getText());

			for (int i = 0; i < results.length; i++)
				_arrayAdapter1.add("[" + results[i].getScore() + "]: " + results[i].getText());
		}  else
		{
			setResult("");
		}
	}

	private void createListeningDialog()
	{
		_listeningDialog1 = new ListeningDialog(this);
		_listeningDialog1.setOnDismissListener(new OnDismissListener()
		{
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (_currentRecognizer1 != null)
				{
					_currentRecognizer1.cancel();
					_currentRecognizer1 = null;
				}

				if (!_destroyed1)
				{
					TheatreView.this.removeDialog(LISTENING_DIALOG1);
					createListeningDialog();
				}
			}
		});
	}
	private void playBack(String tts){

		if (tts != null)
		{
			Object _lastTtsContext = new Object();
			_vocalizer1.speakString(tts, _lastTtsContext);
		}
	}

}
