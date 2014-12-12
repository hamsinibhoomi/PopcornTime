package com.nuance.nmdp.sample;
import android.util.Log;

import java.io.Serializable;
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
import android.widget.TextView;

public class TicketView extends Activity  {

	private static final int LISTENING_DIALOG4 = 0;
	private Handler _handler4 = null;
	private final Recognizer.Listener _listener4;
	private Recognizer _currentRecognizer4;
	private ListeningDialog _listeningDialog4;
	private ArrayAdapter<String> _arrayAdapter4;
	private boolean _destroyed4;
	private Vocalizer _vocalizer4;
	List<ShowTable> ticketResult;
	String selectedTicket;
	String selectedMovie;
	String selectedTheatre;
	String selectedShowDate;
	String selectedShowTime;
	private ShowTable SelectedShowDetails;
	private boolean bought;
	private boolean trans=true;
	int numOfTickets;
	private boolean tryagain;
	private boolean notfound;

	private class SavedState
	{
		String DialogText4;
		String DialogLevel4;
		boolean DialogRecording4;
		Recognizer Recognizer4;
		Handler Handler4;
		Vocalizer Vocalizer4;
	}

	public TicketView()
	{
		super();
		_listener4 = createListener();
		_currentRecognizer4 = null;
		_listeningDialog4 = null;
		_destroyed4 = true; 
		_vocalizer4 = MainView.getVocalizer();
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		switch(id)
		{
		case LISTENING_DIALOG4:
			_listeningDialog4.prepare(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					if (_currentRecognizer4 != null)
					{
						_currentRecognizer4.stopRecording();
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
		case LISTENING_DIALOG4:
			return _listeningDialog4;
		}
		return null;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState){
		savedInstanceState.putString("savedSelectedMovie", selectedMovie);
		savedInstanceState.putString("savedSelectedTheatre", selectedTheatre);
		savedInstanceState.putString("savedSelectedDate", selectedShowDate);
		savedInstanceState.putString("savedSelectedTime", selectedShowTime);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null){
			selectedMovie = savedInstanceState.getString("savedSelectedMovie");
			selectedTheatre = savedInstanceState.getString("savedSelectedTheatre");
			selectedShowDate = savedInstanceState.getString("savedSelectedDate");
			selectedShowTime = savedInstanceState.getString("savedSelectedTime");
			ticketResult = MainView.db.getAllShows();
		}
		else {
			selectedMovie = getIntent().getStringExtra("movieName");
			selectedTheatre = getIntent().getStringExtra("theatreName");
			selectedShowDate = getIntent().getStringExtra("ShowDateName");
			selectedShowTime = getIntent().getStringExtra("ShowTimeName");
			setVolumeControlStream(AudioManager.STREAM_MUSIC); // So that the 'Media Volume' applies to this activity
			setContentView(R.layout.ticket);

			MainView.db.open();
			ticketResult = MainView.db.getAllShows();
			MainView.db.close();

			_destroyed4 = false;

			playBack("Please tell the number of tickets you would like to purchase");

			//display all details
			TextView t5 = (TextView) findViewById(R.id.textView5); 
			t5.setText("TICKET SELECTION");
			t5.setTextSize(30);
			TextView t1 = (TextView) findViewById(R.id.textView1); 
			t1.setTextSize(20);
			t1.setText("Movie is " + selectedMovie);
			TextView t2 = (TextView) findViewById(R.id.textView2); 
			t2.setTextSize(20);
			t2.setText("Theatre is " + selectedTheatre);
			TextView t3 = (TextView) findViewById(R.id.textView3);
			t3.setTextSize(20);
			t3.setText("Show Date is " + selectedShowDate);
			TextView t4 = (TextView) findViewById(R.id.textView4); 
			t4.setTextSize(20);
			t4.setText("Show Time is " + selectedShowTime);

			// Use the same handler for both buttons
			final Button ticketButton = (Button)findViewById(R.id.btn_startTicket);

			Button.OnClickListener startListener = new Button.OnClickListener()
			{
				@Override
				public void onClick(View v) {

					_listeningDialog4.setText("Initializing...");

					showDialog(LISTENING_DIALOG4);

					_listeningDialog4.setStoppable(false);

					setResults(new Recognition.Result[0]);

					if (v == ticketButton){

						_currentRecognizer4 = MainView.getSpeechKit().createRecognizer(Recognizer.RecognizerType.Dictation, Recognizer.EndOfSpeechDetection.Long, "en_US", _listener4, _handler4);
					}else{

						_currentRecognizer4 = MainView.getSpeechKit().createRecognizer(Recognizer.RecognizerType.Search, Recognizer.EndOfSpeechDetection.Short, "en_US", _listener4, _handler4);
					}	
					_currentRecognizer4.start();
				}
			};
			ticketButton.setOnClickListener(startListener);

			Log.v("gtry", "set dictation button");
			// TTS button will switch to the TtsView for the displayed text
			Button button = (Button)findViewById(R.id.btn_startTts4);
			button.setOnClickListener(new Button.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					// Create Vocalizer listener

					EditText t = (EditText)findViewById(R.id.text_TicketResult);
					Log.v("gtry", "starting vocalizer listener");
					_vocalizer4 = MainView.getVocalizer();
					// Check for text from the intent (present if we came from DictationView)
					String tts = t.getText().toString();
					if (tts != null)
					{
						Object _lastTtsContext = new Object();
						_vocalizer4.speakString(tts, _lastTtsContext);
					}

				}
			});


			// Set up the list to display multiple results
			ListView list = (ListView)findViewById(R.id.list_results4);
			_arrayAdapter4 = new ArrayAdapter<String>(list.getContext(), R.layout.listitem)
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
							EditText t = (EditText)findViewById(R.id.text_TicketResult);

							// Copy the text (without the [score]) into the edit box
							String text = b.getText().toString();
							int startIndex = text.indexOf("]: ");
							t.setText(text.substring(startIndex > 0 ? (startIndex + 3) : 0));

						}
					});
					return b;
				}   
					};
					list.setAdapter(_arrayAdapter4);

					// Initialize the listening dialog
					createListeningDialog();
					Log.v("gtry", "initializing listening dialog");
					SavedState savedState = (SavedState)getLastNonConfigurationInstance();
					if (savedState == null)
					{
						// Initialize the handler, for access to this application's message queue
						_handler4 = new Handler();
					} else
					{
						// There was a recognition in progress when the OS destroyed/
						// recreated this activity, so restore the existing recognition
						_currentRecognizer4 = savedState.Recognizer4;
						_listeningDialog4.setText(savedState.DialogText4);
						_listeningDialog4.setLevel(savedState.DialogLevel4);
						_listeningDialog4.setRecording(savedState.DialogRecording4);
						_handler4 = savedState.Handler4;

						if (savedState.DialogRecording4)
						{
							// Simulate onRecordingBegin() to start animation
							Log.v("gtry", "calling onRecordingBegun()");
							_listener4.onRecordingBegin(_currentRecognizer4);
							Log.v("gtry", "called onRecordingBegun()");
						}

						_currentRecognizer4.setListener(_listener4);
						Log.v("gtry", "setListener");
					}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		_destroyed4 = true;
		if (_currentRecognizer4 !=  null)
		{
			_currentRecognizer4.cancel();
			_currentRecognizer4 = null;
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		if (_listeningDialog4.isShowing() && _currentRecognizer4 != null)
		{
			// If a recognition is in progress, save it, because the activity
			// is about to be destroyed and recreated
			SavedState savedState = new SavedState();
			savedState.Recognizer4 = _currentRecognizer4;
			savedState.DialogText4 = _listeningDialog4.getText();
			savedState.DialogLevel4 = _listeningDialog4.getLevel();
			savedState.DialogRecording4 = _listeningDialog4.isRecording();
			savedState.Handler4 = _handler4;
			savedState.Vocalizer4 = _vocalizer4;
			_currentRecognizer4 = null; // Prevent onDestroy() from canceling
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
				Log.v("gtry", "begun recording");
				_listeningDialog4.setText("Recording...");
				_listeningDialog4.setStoppable(true);
				_listeningDialog4.setRecording(true);

				// Create a repeating task to update the audio level
				Runnable r = new Runnable()
				{
					public void run()
					{	
						if (_listeningDialog4 != null && _listeningDialog4.isRecording() && _currentRecognizer4 != null)
						{
							_listeningDialog4.setLevel(Float.toString(_currentRecognizer4.getAudioLevel()));
							_handler4.postDelayed(this, 500);
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
				_listeningDialog4.setText("Processing...");
				_listeningDialog4.setLevel("");
				_listeningDialog4.setRecording(false);
				_listeningDialog4.setStoppable(false);

				Log.v("verbose", "inside on recording done");
			}

			@Override
			public void onError(Recognizer recognizer, SpeechError error) 
			{
				if (recognizer != _currentRecognizer4) return;
				if (_listeningDialog4.isShowing()) dismissDialog(LISTENING_DIALOG4);
				_currentRecognizer4 = null;
				_listeningDialog4.setRecording(false);

				// Display the error + suggestion in the edit box
				String detail = error.getErrorDetail();
				String suggestion = error.getSuggestion();
				if (suggestion == null) suggestion = "";
				setResult(detail + "\n" + suggestion);
				// for debugging purpose: printing out the speechkit session id
				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onError: session id ["
						+ MainView.getSpeechKit().getSessionId() + "]");
				Log.v("verbose", "inside on error");
			}

			@Override
			public void onResults(Recognizer recognizer, Recognition results) {
				setResult("Ok this is here now");
				if (_listeningDialog4.isShowing()) dismissDialog(LISTENING_DIALOG4);
				_currentRecognizer4 = null;
				_listeningDialog4.setRecording(false);
				int count = results.getResultCount();
				Recognition.Result [] rs = new Recognition.Result[count];
				for (int i = 0; i < count; i++)
				{
					rs[i] = results.getResult(i);
				}
				setResults(rs);

				// for debugging purpose: printing out the speechkit session id
				android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onResults: session id ["
						+ MainView.getSpeechKit().getSessionId() + "]");
				Log.v("verbose", "inside on results");
				checkForAvailTickets(selectedTicket);
				Log.v("verbose", "inside on results - selectedTicket " + selectedTicket);
				if(selectedTicket.matches("([A|a]n)?[o|O]ther [T|t]heatre|[G|g]o [t|T]o [T|t]heatre|([A|a]n)?[o|O]ther [T|t]heater|[G|g]o [t|T]o [T|t]heater|[C|c]hange [T|t]heat[er|re]|[T|t]heat[er|re]")){
					Intent i = new Intent(TicketView.this, TheatreView.class);
					i.setFlags(i.FLAG_ACTIVITY_CLEAR_TOP|i.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(i);
				}
				else if(selectedTicket.matches("([A|a]n)?[o|O]ther [M|m]ovie|[S|s]omething [E|e]lse|[G|g]o [t|T]o [M|m]ovie|[M|m]ovie|([C|c]hange)? [M|m]ovie")){
					Intent i = new Intent(TicketView.this, MovieView.class);
					i.setFlags(i.FLAG_ACTIVITY_CLEAR_TOP|i.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(i);
				}
				else if(selectedTicket.matches("([A|a]n)?[o|O]ther [D|d]ay|[G|g]o [t|T]o [D|d]ate|([A|a]n)?[o|O]ther [D|d]ate|[C|c]hange [D|d]ate|[D|d]a[y]|[D|d]a[te]|[C|c]hange [D|d]ay")){
					Intent i = new Intent(TicketView.this, ShowDateView.class);
					i.setFlags(i.FLAG_ACTIVITY_CLEAR_TOP|i.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(i);
				}
				else if(selectedTicket.toLowerCase().contentEquals("back")||selectedTicket.matches("([A|a]n)?[o|O]ther [D|d]ay|[G|g]o [t|T]o [T|t]ime|([A|a]n)?[o|O]ther [T|t]ime|[C|c]hange [T|t]ime")){
					TicketView.this.finish();
				}
				// CALL ACTIVITY HERE
				
				else if(bought&!tryagain){
					playBack("Review your order");
					Intent i = new Intent(TicketView.this, PurchaseView.class);
					i.putExtra("movieName", selectedMovie);
					i.putExtra("theatreName", selectedTheatre);
					i.putExtra("ShowDateName", selectedShowDate);
					i.putExtra("ShowTimeName", selectedShowTime);
					i.putExtra("ticketNumber", numOfTickets);

					startActivity(i);
				}
				else if(!bought&&tryagain&&!notfound){
					playBack("Try valid number of tickets");
				}

				else if (!bought&!notfound){
					//playBack("Sufficient tickets not available.Try another Show");
					TicketView.this.finish();

				}
				else{
					playBack("Pardon me");
				}
			}
		};
	}

	private void setResult(String result)
	{
		EditText t = (EditText)findViewById(R.id.text_TicketResult);
		if (t != null){
			t.setText(result);
			selectedTicket=result;
			Log.v("PopcornTime", result);

		}
	}

	private void setResults(Recognition.Result[] results)
	{
		_arrayAdapter4.clear();
		if (results.length > 0)
		{
			setResult(results[0].getText());

			for (int i = 0; i < results.length; i++)
				_arrayAdapter4.add("[" + results[i].getScore() + "]: " + results[i].getText());
		}  else
		{
			setResult("");
		}
	}

	private void createListeningDialog()
	{
		_listeningDialog4 = new ListeningDialog(this);
		_listeningDialog4.setOnDismissListener(new OnDismissListener()
		{
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (_currentRecognizer4 != null) // Cancel the current recognizer
				{
					_currentRecognizer4.cancel();
					_currentRecognizer4 = null;
				}

				if (!_destroyed4)
				{
					// Remove the dialog so that it will be recreated next time.
					// This is necessary to avoid a bug in Android >= 1.6 where the 
					// animation stops working.
					TicketView.this.removeDialog(LISTENING_DIALOG4);
					createListeningDialog();
				}
			}
		});
	}
	private void playBack(String tts){

		if (tts != null)
		{
			Object _lastTtsContext = new Object();
			_vocalizer4.speakString(tts, _lastTtsContext);
		}
	}

	public void checkForAvailTickets(String selectedTicket ){

		Log.v("gtry", selectedTicket);

		try{
			numOfTickets=	Integer.parseInt(selectedTicket);
			Log.v("gtry", " " +numOfTickets);

		}
		catch(NumberFormatException e){
			if(selectedTicket.equalsIgnoreCase("to")||selectedTicket.equalsIgnoreCase("too")||selectedTicket.equalsIgnoreCase("Do")){
				numOfTickets=2;
			}
			else if(selectedTicket.equalsIgnoreCase("for")){
				numOfTickets=4;
			}
			else{
				numOfTickets = StringtoInt.inNumerals(selectedTicket);
			}
			Log.v("gtry", "numOfTickets---"+numOfTickets);

		}

		if(numOfTickets<=0){
			Log.v("gtry", "in if");
			bought = false;
			tryagain=true;
			Log.v("gtry", "numOfTickets---"+numOfTickets);
		}
		else{
			Log.v("gtry", "in else");
			MainView.db.open();
			SelectedShowDetails =	MainView.db.checkForTickets(selectedMovie,selectedTheatre,selectedShowDate,selectedShowTime);
			if(SelectedShowDetails!=null){
				Log.v("gtry", SelectedShowDetails.toString());
				Integer availTickets = SelectedShowDetails.getTickets();
				Log.v("gtry", "availTickets---"+availTickets);
				if(numOfTickets> availTickets){
					playBack(selectedTicket+" not available.Try another show.");
					bought= false;
					notfound=true;
				}
				else{
					bought=true;
					tryagain=false;
				}


			}
			MainView.db.close();
		}
	}
}