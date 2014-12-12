package com.nuance.nmdp.sample;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
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
import android.widget.SimpleAdapter;

import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.Vocalizer;

public class ShowDateView extends Activity{
	private static final int LISTENING_DIALOG2 = 0;
	private Handler _handler2 = null;
	private final Recognizer.Listener _listener2;
	private Recognizer _currentRecognizer2;
	private ListeningDialog _listeningDialog2;
	private ArrayAdapter<String> _arrayAdapter2;
	private boolean _destroyed2;
	private Vocalizer _vocalizer2;
	List<ShowTable> showDateResult;
	String selectedMovie;
	String selectedTheatre;
	String selectedShowDate;
	Set<String> uniqueDate = new HashSet<String>();
	Calendar today = Calendar.getInstance(TimeZone.getTimeZone("EST"));
	int month = today.get(Calendar.MONTH) + 1;
	int year = today.get(Calendar.YEAR);
	int day = today.get(Calendar.DAY_OF_MONTH);
	int daysFromToday = 0;

	private class SavedState
	{
		String DialogText2;
		String DialogLevel2;
		boolean DialogRecording2;
		Recognizer Recognizer2;
		Handler Handler2;
		Vocalizer Vocalizer2;
		String selectedMovie;
		String selectedTheatre;
		List<ShowTable> showDateResult;
	}

	public ShowDateView()
	{
		super();
		_listener2 = createListener();
		_currentRecognizer2 = null;
		_listeningDialog2 = null;
		_destroyed2 = true;
		_vocalizer2 = MainView.getVocalizer();
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		switch(id)
		{
		case LISTENING_DIALOG2:
			_listeningDialog2.prepare(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					if (_currentRecognizer2 != null)
					{
						_currentRecognizer2.stopRecording();
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
		case LISTENING_DIALOG2:
			return _listeningDialog2;
		}
		return null;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState){
		savedInstanceState.putString("savedSelectedMovie", selectedMovie);
		savedInstanceState.putString("savedSelectedTheatre", selectedTheatre);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v("verbose","inside show date view activity");
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null){
			selectedMovie = savedInstanceState.getString("savedSelectedMovie");
			selectedTheatre = savedInstanceState.getString("savedSelectedTheatre");
			showDateResult = MainView.db.getShowsDatesFromMovieAndTheatre(selectedMovie, selectedTheatre);
		}
		else {
			selectedMovie = getIntent().getStringExtra("movieName");
			selectedTheatre = getIntent().getStringExtra("theatreName");
			setVolumeControlStream(AudioManager.STREAM_MUSIC); // So that the 'Media Volume' applies to this activity
			setContentView(R.layout.showdate);
			Log.v("gtry", "retrieving show date list");
			MainView.db.open();
			showDateResult = MainView.db.getShowsDatesFromMovieAndTheatre(selectedMovie, selectedTheatre);
			MainView.db.close();

			_destroyed2 = false;

			playBack("Which day would you like to see the movie?");

			//display show date list
			Log.v("verbose", showDateResult.toString());
			ArrayAdapter<String> datesAdapter = new ArrayAdapter<String>(this, R.layout.listitem);
			Iterator<ShowTable> itr = showDateResult.iterator();
			while(itr.hasNext()){
				String date = itr.next().getShowDate();
				if(!(uniqueDate.contains(date))){
					uniqueDate.add(date);
				}
				Log.v("gtry", "uniques list is " +uniqueDate);
			}
			datesAdapter.addAll(uniqueDate);
			ListView dates = (ListView) findViewById(R.id.list_dates);
			dates.setAdapter(datesAdapter);

			final Button ShowDateButton = (Button)findViewById(R.id.btn_startShowDate);
			Button.OnClickListener startListener = new Button.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					_listeningDialog2.setText("Initializing...");

					showDialog(LISTENING_DIALOG2);

					_listeningDialog2.setStoppable(false);

					setResults(new Recognition.Result[0]);

					if (v == ShowDateButton)
						_currentRecognizer2 = MainView.getSpeechKit().createRecognizer(Recognizer.RecognizerType.Dictation, Recognizer.EndOfSpeechDetection.Long, "en_US", _listener2, _handler2);
					else
						_currentRecognizer2 = MainView.getSpeechKit().createRecognizer(Recognizer.RecognizerType.Search, Recognizer.EndOfSpeechDetection.Short, "en_US", _listener2, _handler2);
					_currentRecognizer2.start();
				}
			};
			ShowDateButton.setOnClickListener(startListener);
			
			Button button = (Button)findViewById(R.id.btn_startTts2);
			button.setOnClickListener(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v) {

					EditText t = (EditText)findViewById(R.id.text_ShowDateResult);     
					_vocalizer2 = MainView.getVocalizer();

					String tts = t.getText().toString();
					if (tts != null)
					{
						Object _lastTtsContext = new Object();
						_vocalizer2.speakString(tts, _lastTtsContext);
					}

				}
			});

			ListView list = (ListView)findViewById(R.id.list_results2);
			_arrayAdapter2 = new ArrayAdapter<String>(list.getContext(), R.layout.listitem)
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
							EditText t = (EditText)findViewById(R.id.text_ShowDateResult);

							String text = b.getText().toString();
							int startIndex = text.indexOf("]: ");
							t.setText(text.substring(startIndex > 0 ? (startIndex + 3) : 0));

						}
					});
					return b;
				}   
					};
					list.setAdapter(_arrayAdapter2);

					// Initialize the listening dialog
					createListeningDialog();

					SavedState savedState = (SavedState)getLastNonConfigurationInstance();
					if (savedState == null)
					{
						// Initialize the handler, for access to this application's message queue
						_handler2 = new Handler();
					} else
					{
						
						_currentRecognizer2 = savedState.Recognizer2;
						_listeningDialog2.setText(savedState.DialogText2);
						_listeningDialog2.setLevel(savedState.DialogLevel2);
						_listeningDialog2.setRecording(savedState.DialogRecording2);
						_handler2 = savedState.Handler2;

						if (savedState.DialogRecording2)
						{
							// Simulate onRecordingBegin() to start animation
							_listener2.onRecordingBegin(_currentRecognizer2);
						}

						_currentRecognizer2.setListener(_listener2);
					}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		_destroyed2 = true;
		if (_currentRecognizer2 !=  null)
		{
			_currentRecognizer2.cancel();
			_currentRecognizer2 = null;
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		if (_listeningDialog2.isShowing() && _currentRecognizer2 != null)
		{
			
			SavedState savedState = new SavedState();
			savedState.Recognizer2 = _currentRecognizer2;
			savedState.DialogText2 = _listeningDialog2.getText();
			savedState.DialogLevel2 = _listeningDialog2.getLevel();
			savedState.DialogRecording2 = _listeningDialog2.isRecording();
			savedState.Handler2 = _handler2;
			savedState.Vocalizer2 = _vocalizer2;
			savedState.selectedMovie = selectedMovie;
			savedState.selectedTheatre = selectedTheatre;
			savedState.showDateResult = showDateResult;
			_currentRecognizer2 = null; // Prevent onDestroy() from canceling
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
				_listeningDialog2.setText("Recording...");
				_listeningDialog2.setStoppable(true);
				_listeningDialog2.setRecording(true);

				// Create a repeating task to update the audio level
				Runnable r = new Runnable()
				{
					public void run()
					{
						if (_listeningDialog2 != null && _listeningDialog2.isRecording() && _currentRecognizer2 != null)
						{
							_listeningDialog2.setLevel(Float.toString(_currentRecognizer2.getAudioLevel()));
							_handler2.postDelayed(this, 500);
						}
					}
				};
				r.run();
			}

			@Override
			public void onRecordingDone(Recognizer recognizer) 
			{
				_listeningDialog2.setText("Processing...");
				_listeningDialog2.setLevel("");
				_listeningDialog2.setRecording(false);
				_listeningDialog2.setStoppable(false);


			}

			@Override
			public void onError(Recognizer recognizer, SpeechError error) 
			{
				if (recognizer != _currentRecognizer2) return;
				if (_listeningDialog2.isShowing()) dismissDialog(LISTENING_DIALOG2);
				_currentRecognizer2 = null;
				_listeningDialog2.setRecording(false);

				
				String detail = error.getErrorDetail();
				String suggestion = error.getSuggestion();
				if (suggestion == null) suggestion = "";
				setResult(detail + "\n" + suggestion);
				
				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onError: session id ["
						+ MainView.getSpeechKit().getSessionId() + "]");
			}

			@Override
			public void onResults(Recognizer recognizer, Recognition results) {
				if (_listeningDialog2.isShowing()) dismissDialog(LISTENING_DIALOG2);
				_currentRecognizer2 = null;
				_listeningDialog2.setRecording(false);
				int count = results.getResultCount();
				Recognition.Result [] rs = new Recognition.Result[count];
				for (int i = 0; i < count; i++)
				{
					rs[i] = results.getResult(i);
				}
				setResults(rs);

				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onResults: session id ["
						+ MainView.getSpeechKit().getSessionId() + "]");
				if(selectedShowDate.matches("([A|a]n)?[o|O]ther [T|t]heatre|[G|g]o [t|T]o [T|t]heatre|([A|a]n)?[o|O]ther [T|t]heater|[G|g]o [t|T]o [T|t]heater|([C|c]hange) [T|t]heat[er|re]|[T|t]heat[er|re]")){
					ShowDateView.this.finish();
				}
				else if(selectedShowDate.matches("([A|a]n)?[o|O]ther [M|m]ovie|[S|s]omething [E|e]lse|[G|g]o [t|T]o [M|m]ovie|([C|c]hange)? [M|m]ovie")){
					Intent i = new Intent(ShowDateView.this, MovieView.class);
					i.setFlags(i.FLAG_ACTIVITY_CLEAR_TOP|i.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(i);
				}

				else if(containsShowDate()){
					Intent i = new Intent(ShowDateView.this, TimeView.class);
					i.putExtra("movieName", selectedMovie);
					i.putExtra("theatreName", selectedTheatre);
					i.putExtra("ShowDateName", selectedShowDate);
					startActivity(i);
				}
				else if(selectedShowDate.equalsIgnoreCase("Back")){
					ShowDateView.this.finish();
				}

				else{
					playBack("There aren't shows on this date. Please find another date from the list.");

				}
			}
		};
	}

	private void setResult(String result)
	{
		EditText t = (EditText)findViewById(R.id.text_ShowDateResult);
		if (t != null){
			t.setText(result);
			selectedShowDate = result;
			Log.v("PopcornTime", result);
		}
	}

	private void setResults(Recognition.Result[] results)
	{

		_arrayAdapter2.clear();
		if (results.length > 0)
		{
			setResult(results[0].getText());

			for (int i = 0; i < results.length; i++)
				_arrayAdapter2.add("[" + results[i].getScore() + "]: " + results[i].getText());
		}  else
		{
			setResult("");
		}
	}

	private void createListeningDialog()
	{
		_listeningDialog2 = new ListeningDialog(this);
		_listeningDialog2.setOnDismissListener(new OnDismissListener()
		{
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (_currentRecognizer2 != null) 
				{
					_currentRecognizer2.cancel();
					_currentRecognizer2 = null;
				}

				if (!_destroyed2)
				{
					
					ShowDateView.this.removeDialog(LISTENING_DIALOG2);
					createListeningDialog();
				}
			}
		});
	}
	private void playBack(String tts){

		if (tts != null)
		{
			Object _lastTtsContext = new Object();
			_vocalizer2.speakString(tts, _lastTtsContext);
		}
	}


	private boolean containsShowDate(){

		Iterator<ShowTable> itr = showDateResult.iterator();
		Log.v("gtry", selectedShowDate);
		String unprocessedDate = selectedShowDate; //user utterance before processing
		String date = "";
		int userMonth = 0;
		int userDay = 0;
		int userYear = 0;
		Pattern pattern = Pattern.compile("[1-2]?[0-9](st|th|nd)?|[3][0-1](st|th)?");
		Matcher matcher = pattern.matcher(unprocessedDate);
		Pattern monthPattern = Pattern.compile("[J|j]anuary|[F|f]ebruary|[M|m]arch|[A|a]pril|[M|m]ay|[J|j]une|[J|j]uly|[A|a]ugust|[S|s]eptember|[O|o]ctober|[N|n]ovember|[D|d]ecember");
		Matcher monthMatcher = monthPattern.matcher(unprocessedDate);

		if (unprocessedDate.toLowerCase().contains("today")) {
			date = month + "-" +  day + "-"+ year;
		}
		else if (unprocessedDate.toLowerCase().contains("tomorrow")) {
			if ((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10) && day == 31)
				date = "1-" + (month + 1) + "-" + year;
			else if ((month == 12) && day == 31)
				date = "1-1-" + year;
			else if ((month == 2) && day == 28)
				date = "3-1-" + year;
			else if ((month ==  4 || month == 6 || month == 9 || month == 11) && day == 30)
				date = (month + 1) + "-1-" + year;
			else
				date = month + "-" + (day + 1) + "-" + year;
		}
		else if (unprocessedDate.toLowerCase().contains("christmas") || unprocessedDate.toLowerCase().contains("christmas day")) {
			date = "12-25-2014";	
		}
		else if (unprocessedDate.toLowerCase().contains("christmas eve")) {
			date = "12-24-2014";
		}
		else if (unprocessedDate.toLowerCase().contains("new years") || unprocessedDate.toLowerCase().contains("new years day")) {
			date = "1-1-2015";
		}
		else{
			while (monthMatcher.find()){
				//Checks for month in user utterance
				if (monthMatcher.group().equalsIgnoreCase("january"))
					userMonth = 1;
				else if (monthMatcher.group().equalsIgnoreCase("february"))
					userMonth = 2;
				else if (monthMatcher.group().equalsIgnoreCase("march"))
					userMonth = 3;
				else if (monthMatcher.group().equalsIgnoreCase("april"))
					userMonth = 4;
				else if (monthMatcher.group().equalsIgnoreCase("may"))
					userMonth = 5;
				else if (monthMatcher.group().equalsIgnoreCase("june"))
					userMonth = 6;
				else if (monthMatcher.group().equalsIgnoreCase("july"))
					userMonth = 7;
				else if (monthMatcher.group().equalsIgnoreCase("august"))
					userMonth = 8;
				else if (monthMatcher.group().equalsIgnoreCase("september"))
					userMonth = 9;
				else if (monthMatcher.group().equalsIgnoreCase("october"))
					userMonth = 10;
				else if (monthMatcher.group().equalsIgnoreCase("november"))
					userMonth = 11;
				else if (monthMatcher.group().equalsIgnoreCase("december"))
					userMonth = 12;
				else
					userMonth = 12;
				//Checks for day in user utterance
				while (matcher.find()) {
					if (matcher.group().contains("th") || matcher.group().contains("nd") || matcher.group().contains("st"))
						userDay = Integer.parseInt(unprocessedDate.substring(matcher.start(), matcher.end()-2));
					else 
						userDay = Integer.parseInt(unprocessedDate.substring(matcher.start(), matcher.end()));
				}
				//checks for year in user utterance
				if (unprocessedDate.equals("2015"))
					userYear = 2015;
				else
					userYear = 2014;
			}
			date = userMonth + "-" + userDay + "-" + userYear;
		}

		if (date.equals(""))
			return false;
		Log.v("gtry", date);

		//check to see if the date matches any of the dates in the database
		while(itr.hasNext()){
			ShowTable itr1 = itr.next();
			Log.v("verbose", itr1.getShowDate());
			if(date.equalsIgnoreCase(itr1.getShowDate())){
				selectedShowDate = itr1.getShowDate();
				return true;
			}

		}
		return false;
	}

}
