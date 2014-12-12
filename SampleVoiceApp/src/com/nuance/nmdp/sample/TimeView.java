package com.nuance.nmdp.sample;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class TimeView extends Activity{
	private static final int LISTENING_DIALOG3 = 0;
	private Handler _handler3 = null;
	private final Recognizer.Listener _listener3;
	private Recognizer _currentRecognizer3;
	private ListeningDialog _listeningDialog3;
	private ArrayAdapter<String> _arrayAdapter3;
	private boolean _destroyed3;
	private Vocalizer _vocalizer3;
	List<ShowTable> showTimeResult;
	String selectedShowTime;
	String selectedMovie;
	String selectedTheatre;
	String selectedShowDate;
	private String dbShowTime;

	private class SavedState
	{
		String DialogText3;
		String DialogLevel3;
		boolean DialogRecording3;
		Recognizer Recognizer3;
		Handler Handler3;
		Vocalizer Vocalizer3;
	}

	public TimeView()
	{
		super();
		_listener3 = createListener();
		_currentRecognizer3 = null;
		_listeningDialog3 = null;
		_destroyed3 = true;
		_vocalizer3 = MainView.getVocalizer();
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		switch(id)
		{
		case LISTENING_DIALOG3:
			_listeningDialog3.prepare(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					if (_currentRecognizer3 != null)
					{
						_currentRecognizer3.stopRecording();
					}
				}
			});
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id)
		{
		case LISTENING_DIALOG3:
			return _listeningDialog3;
		}
		return null;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState){
		savedInstanceState.putString("savedSelectedMovie", selectedMovie);
		savedInstanceState.putString("savedSelectedTheatre", selectedTheatre);
		savedInstanceState.putString("savedSelectedDate", selectedShowDate);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v("verbose","inside show time view activity");
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null){
			selectedMovie = savedInstanceState.getString("savedSelectedMovie");
			selectedTheatre = savedInstanceState.getString("savedSelectedTheatre");
			selectedShowDate = savedInstanceState.getString("savedSelectedDate");
			showTimeResult = MainView.db.getShowTimesFromMovieAndTheatreAndDate(selectedMovie,selectedTheatre,selectedShowDate);
		}
		else {
			selectedMovie = getIntent().getStringExtra("movieName");
			selectedTheatre = getIntent().getStringExtra("theatreName");
			selectedShowDate = getIntent().getStringExtra("ShowDateName");
			setVolumeControlStream(AudioManager.STREAM_MUSIC); // So that the 'Media Volume' applies to this activity
			setContentView(R.layout.time);
			Log.v("gtry", "retrieving show time list");
			MainView.db.open();
			showTimeResult = MainView.db.getShowTimesFromMovieAndTheatreAndDate(selectedMovie,selectedTheatre,selectedShowDate);
			MainView.db.close();

			_destroyed3 = false;

			playBack("Show times on" +selectedShowDate);

			ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(this, R.layout.listitem);
			Iterator<ShowTable> itr = showTimeResult.iterator();
			while(itr.hasNext()){
				String time = itr.next().getShowTime();
				timeAdapter.add(time);
			}
			ListView dates = (ListView) findViewById(R.id.list_times);
			dates.setAdapter(timeAdapter);

			// Use the same handler for both buttons
			final Button ShowTimeButton = (Button)findViewById(R.id.btn_startTime);
			Button.OnClickListener startListener = new Button.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					_listeningDialog3.setText("Initializing...");

					showDialog(LISTENING_DIALOG3);

					_listeningDialog3.setStoppable(false);

					setResults(new Recognition.Result[0]);

					if (v == ShowTimeButton)
						_currentRecognizer3 = MainView.getSpeechKit().createRecognizer(Recognizer.RecognizerType.Dictation, Recognizer.EndOfSpeechDetection.Long, "en_US", _listener3, _handler3);
					else
						_currentRecognizer3 = MainView.getSpeechKit().createRecognizer(Recognizer.RecognizerType.Search, Recognizer.EndOfSpeechDetection.Short, "en_US", _listener3, _handler3);
					_currentRecognizer3.start();
				}
			};
			ShowTimeButton.setOnClickListener(startListener);

			Button button = (Button)findViewById(R.id.btn_startTts3);
			button.setOnClickListener(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					// Create Vocalizer listener

					EditText t = (EditText)findViewById(R.id.text_TimeResult);     
					_vocalizer3 = MainView.getVocalizer();

					// Check for text from the intent (present if we came from DictationView)
					String tts = t.getText().toString();
					if (tts != null)
					{
						Object _lastTtsContext = new Object();
						_vocalizer3.speakString(tts, _lastTtsContext);
					}

				}
			});

			// Set up the list to display multiple results
			ListView list = (ListView)findViewById(R.id.list_results3);
			_arrayAdapter3 = new ArrayAdapter<String>(list.getContext(), R.layout.listitem)
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
							EditText t = (EditText)findViewById(R.id.text_TimeResult);

							// Copy the text (without the [score]) into the edit box
							String text = b.getText().toString();
							int startIndex = text.indexOf("]: ");
							t.setText(text.substring(startIndex > 0 ? (startIndex + 3) : 0));

						}
					});
					return b;
				}   
					};
					list.setAdapter(_arrayAdapter3);

					// Initialize the listening dialog
					createListeningDialog();

					SavedState savedState = (SavedState)getLastNonConfigurationInstance();
					if (savedState == null)
					{
						// Initialize the handler, for access to this application's message queue
						_handler3 = new Handler();
					} else
					{

						_currentRecognizer3 = savedState.Recognizer3;
						_listeningDialog3.setText(savedState.DialogText3);
						_listeningDialog3.setLevel(savedState.DialogLevel3);
						_listeningDialog3.setRecording(savedState.DialogRecording3);
						_handler3 = savedState.Handler3;

						if (savedState.DialogRecording3)
						{
							// Simulate onRecordingBegin() to start animation
							_listener3.onRecordingBegin(_currentRecognizer3);
						}

						_currentRecognizer3.setListener(_listener3);
					}
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		_destroyed3 = true;
		if (_currentRecognizer3 !=  null)
		{
			_currentRecognizer3.cancel();
			_currentRecognizer3 = null;
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		if (_listeningDialog3.isShowing() && _currentRecognizer3 != null)
		{

			SavedState savedState = new SavedState();
			savedState.Recognizer3 = _currentRecognizer3;
			savedState.DialogText3 = _listeningDialog3.getText();
			savedState.DialogLevel3 = _listeningDialog3.getLevel();
			savedState.DialogRecording3 = _listeningDialog3.isRecording();
			savedState.Handler3 = _handler3;
			savedState.Vocalizer3 = _vocalizer3;
			_currentRecognizer3 = null; 
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
				_listeningDialog3.setText("Recording...");
				_listeningDialog3.setStoppable(true);
				_listeningDialog3.setRecording(true);

				// Create a repeating task to update the audio level
				Runnable r = new Runnable()
				{
					public void run()
					{
						if (_listeningDialog3 != null && _listeningDialog3.isRecording() && _currentRecognizer3 != null)
						{
							_listeningDialog3.setLevel(Float.toString(_currentRecognizer3.getAudioLevel()));
							_handler3.postDelayed(this, 500);
						}
					}
				};
				r.run();
			}

			@Override
			public void onRecordingDone(Recognizer recognizer) 
			{
				_listeningDialog3.setText("Processing...");
				_listeningDialog3.setLevel("");
				_listeningDialog3.setRecording(false);
				_listeningDialog3.setStoppable(false);


			}

			@Override
			public void onError(Recognizer recognizer, SpeechError error) 
			{
				if (recognizer != _currentRecognizer3) return;
				if (_listeningDialog3.isShowing()) dismissDialog(LISTENING_DIALOG3);
				_currentRecognizer3 = null;
				_listeningDialog3.setRecording(false);

				// Display the error + suggestion in the edit box
				String detail = error.getErrorDetail();
				String suggestion = error.getSuggestion();
				if (suggestion == null) suggestion = "";
				setResult(detail + "\n" + suggestion);

				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onError: session id ["
						+ MainView.getSpeechKit().getSessionId() + "]");
			}

			@Override
			public void onResults(Recognizer recognizer, Recognition results) {
				if (_listeningDialog3.isShowing()) dismissDialog(LISTENING_DIALOG3);
				_currentRecognizer3 = null;
				_listeningDialog3.setRecording(false);
				int count = results.getResultCount();
				Recognition.Result [] rs = new Recognition.Result[count];
				for (int i = 0; i < count; i++)
				{
					rs[i] = results.getResult(i);
				}
				setResults(rs);

				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onResults: session id ["
						+ MainView.getSpeechKit().getSessionId() + "]");
				if(selectedShowDate.matches("([A|a]n)?[o|O]ther [T|t]heatre|[G|g]o [t|T]o [T|t]heatre|([A|a]n)?[o|O]ther [T|t]heater|[G|g]o [t|T]o [T|t]heater|[C|c]hange [T|t]heat[er|re]|[T|t]heat[er|re]")){
					Intent i = new Intent(TimeView.this, TheatreView.class);
					i.setFlags(i.FLAG_ACTIVITY_CLEAR_TOP|i.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(i);
				}
				else if(selectedShowTime.matches("([A|a]n)?[o|O]ther [M|m]ovie|[S|s]omething [E|e]lse|[G|g]o [t|T]o [M|m]ovie|([C|c]hange)? [M|m]ovie")){
					Intent i = new Intent(TimeView.this, MovieView.class);
					i.setFlags(i.FLAG_ACTIVITY_CLEAR_TOP|i.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(i);
				}
				else if(selectedShowTime.matches("([A|a]n)?[o|O]ther [D|d]ay|[G|g]o [t|T]o [D|d]ate|([A|a]n)?[o|O]ther [D|d]a[y|te]|[C|c]hange [D|d]ate|[C|c]hange [D|d]ay")){
					TimeView.this.finish();
				}
				else if(containsShowTime()){
					Intent i = new Intent(TimeView.this, TicketView.class);
					i.putExtra("movieName", selectedMovie);
					i.putExtra("theatreName", selectedTheatre);
					i.putExtra("ShowDateName",selectedShowDate );
					i.putExtra("ShowTimeName", dbShowTime);
					startActivity(i); 
				}
				else if(selectedShowTime.equalsIgnoreCase("Back")){
					TimeView.this.finish();
				}
				else{
					playBack("Could not find the show time. Please try another show time or say back.");
				}
			}
		};
	}

	private void setResult(String result)
	{
		EditText t = (EditText)findViewById(R.id.text_TimeResult);
		if (t != null){
			t.setText(result);
			selectedShowTime = result;
			Log.v("PopcornTime", result);
		}
	}

	private void setResults(Recognition.Result[] results)
	{

		_arrayAdapter3.clear();
		if (results.length > 0)
		{
			setResult(results[0].getText());

			for (int i = 0; i < results.length; i++)
				_arrayAdapter3.add("[" + results[i].getScore() + "]: " + results[i].getText());
		}  else
		{
			setResult("");
		}
	}

	private void createListeningDialog()
	{
		_listeningDialog3 = new ListeningDialog(this);
		_listeningDialog3.setOnDismissListener(new OnDismissListener()
		{
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (_currentRecognizer3 != null) // Cancel the current recognizer
				{
					_currentRecognizer3.cancel();
					_currentRecognizer3 = null;
				}

				if (!_destroyed3)
				{
					// Remove the dialog so that it will be recreated next time.
					// This is necessary to avoid a bug in Android >= 1.6 where the 
					// animation stops working.
					TimeView.this.removeDialog(LISTENING_DIALOG3);
					createListeningDialog();
				}
			}
		});
	}
	private void playBack(String tts){

		if (tts != null)
		{
			Object _lastTtsContext = new Object();
			_vocalizer3.speakString(tts, _lastTtsContext);
		}
	}


	private boolean containsShowTime(){

		Iterator<ShowTable> itr = showTimeResult.iterator();
		Log.v("gtry", selectedShowTime);
		String time;

		String unprocessedShowTime = selectedShowTime;
		if (unprocessedShowTime.toLowerCase().contains("midnight")) {
			time = "0:00";
		}
		else if (unprocessedShowTime.toLowerCase().contains("noon")) {
			time = "12:00";
		}
		else {
			time = processUtterance(unprocessedShowTime);
		}

		if (time.equals(""))
			return false;

		while(itr.hasNext()){
			String dbTime = itr.next().getShowTime();
			if(time.equalsIgnoreCase(dbTime)){
				dbShowTime = dbTime;
				return true;
			}

		}
		return false;
	}

	private String processUtterance(String unprocessedShowTime) {
		//for formats such as 10:00 or 5:00
		Pattern patternTime = Pattern.compile("\\b[0-1]?[0-9](:)[0-9][0-9]\\b|\\b[2]?[0-3](:)[0-9][0-9]\\b");
		Matcher matcherTime = patternTime.matcher(unprocessedShowTime);

		//for formats such as 1000 or 500
		Pattern patternNoColon = Pattern.compile("\\b[0-2]?[0-9][0-9][0-9]\\b");
		Matcher matcherNoColon = patternNoColon.matcher(unprocessedShowTime);

		Pattern pattern = Pattern.compile("(o clock)|(o'clock)|(oclock)|(quarter to)|(half past)|(quarter after)|thirty");
		Matcher matcher = pattern.matcher(unprocessedShowTime);

		Pattern patternAmPm = Pattern.compile("am|pm|a.m.|p.m.|AM|PM|A.M.|P.M.");
		Matcher matcherAmPm = patternAmPm.matcher(unprocessedShowTime);

		Pattern patternNum = Pattern.compile("\\b[0-1]?[0-9]\\b");
		Matcher matcherNum;
		Pattern patternSingle = Pattern.compile("\\b[0-2]?[0-9]\\b");
		Matcher matcherSingle = patternSingle.matcher(unprocessedShowTime);
		String time = "";
		int add = 0;
		String substring;

		Log.v("gtry", unprocessedShowTime);

		while (matcherAmPm.find()) {
			if (matcherAmPm.group().equalsIgnoreCase("pm") || matcherAmPm.group().equalsIgnoreCase("p.m.")) {
				add = 12;
				Log.v("gtry", "in if statement");
			}
		}

		if (matcherTime.find()) {
			Log.v("gtry", "in MatcherFind.find() if");
			while (matcherAmPm.find()) {
				String time1 = matcherTime.group();
				String[] number = time1.split(":");
				Log.v("gtry", "number is " +number);
				if (matcherAmPm.group().equalsIgnoreCase("pm")||matcherAmPm.group().equalsIgnoreCase("p.m.")) {
					Log.v("gtry", "pm condition");
					number[0] += add;
					time = number[0] + ":" + number[1];
					Log.v("gtry", "Time is " +time);
				}
				else {
					time = matcherTime.group();
				}
			}
		}
		else if (matcherSingle.find()) {
			time = (Integer.parseInt(unprocessedShowTime.substring(matcherSingle.start(),matcherSingle.end())) + add) + ":00";
			Log.v("gtry", "in matcherSingle");
		}
		else if (matcherNoColon.find()) {
			Log.v("gtry", "in matcherNoColon");
			if (matcherNoColon.group().length() == 3)
				time = matcherNoColon.group().substring(0,1) + ":" + matcherNoColon.group().substring(1,3);
			else
				time = matcherNoColon.group().substring(0,2) + ":" + matcherNoColon.group().substring(2,4);
		}
		else {
			Log.v("gtry", "in other matcher");
			while (matcher.find()) {
				if (matcher.group().equalsIgnoreCase("quarter to")) {
					substring = unprocessedShowTime.substring(matcher.end(), unprocessedShowTime.length());
					matcherNum = patternNum.matcher(substring);
					if (substring.length() > 2)
						time = (Integer.parseInt(unprocessedShowTime.substring(matcher.end() + 1,matcher.end() + 3)) - 1 + add) + ":45";
					else {
						if (Integer.parseInt(unprocessedShowTime.substring(matcher.end() + 1,matcher.end() + 2)) == 1)
							time = "12:45";
						else
							time = (Integer.parseInt(unprocessedShowTime.substring(matcher.end() + 1,matcher.end() + 2)) - 1 + add) + ":45";	    		
					}
				}
				else if (matcher.group().equalsIgnoreCase("half past")) {
					substring = unprocessedShowTime.substring(matcher.end(), unprocessedShowTime.length());
					matcherNum = patternNum.matcher(substring);
					if (substring.length() > 2)
						time = (Integer.parseInt(unprocessedShowTime.substring(matcher.end() + 1,matcher.end() + 3)) + add) + ":30";
					else {
						time = (Integer.parseInt(unprocessedShowTime.substring(matcher.end() + 1,matcher.end() + 2)) + add) + ":30";
					}
				}
				else if (matcher.group().equalsIgnoreCase("quarter after")) {
					substring = unprocessedShowTime.substring(matcher.end(), unprocessedShowTime.length());
					matcherNum = patternNum.matcher(substring);
					if (substring.length() > 2)
						time = (Integer.parseInt(unprocessedShowTime.substring(matcher.end() + 1,matcher.end() + 3)) + add) + ":15";
					else {
						time = (Integer.parseInt(unprocessedShowTime.substring(matcher.end() + 1,matcher.end() + 2)) + add) + ":15";
					}
				}
				else if (matcher.group().equalsIgnoreCase("o clock") || matcher.group().equalsIgnoreCase("o'clock")) {
					time = (Integer.parseInt(unprocessedShowTime.substring(matcher.start() - 2, matcher.start() - 1)) + add) + ":00";
				}
				else if (matcher.group().equalsIgnoreCase("thirty")) {
					time =  (Integer.parseInt(unprocessedShowTime.substring(matcher.start() - 2,matcher.start() - 1)) + add) + ":30";
				}
				else
					time = "";
			}
		}

		Log.v("gtry", time);

		return time;
	}
}
