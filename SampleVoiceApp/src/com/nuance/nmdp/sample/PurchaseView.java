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
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class PurchaseView extends Activity  {

	int numOfTicket;
	String selectedMovie;
	String selectedTheatre;
	String selectedShowDate;
	String selectedShowTime;
	private ShowTable SelectedShowDetails;
	private boolean bought;

	private Vocalizer _vocalizer5;
	double price;

	private class SavedState
	{
		String DialogText5;
		String DialogLevel5;
		Recognizer Recognizer5;
		Handler Handler5;
		Vocalizer Vocalizer5;
	}

	public PurchaseView()
	{
		super();
		_vocalizer5 = MainView.getVocalizer();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selectedMovie = getIntent().getStringExtra("movieName");
		selectedTheatre = getIntent().getStringExtra("theatreName");
		selectedShowDate = getIntent().getStringExtra("ShowDateName");
		selectedShowTime = getIntent().getStringExtra("ShowTimeName");
		numOfTicket = getIntent().getIntExtra("ticketNumber", 0);
		setVolumeControlStream(AudioManager.STREAM_MUSIC); 
		setContentView(R.layout.purchase);
		MainView.db.open();
		SelectedShowDetails = MainView.db.checkForTickets(selectedMovie,selectedTheatre,selectedShowDate,selectedShowTime);
		if(SelectedShowDetails!=null){
			price=(numOfTicket*SelectedShowDetails.getPrice());
		}
		else{
			playBack("Error occured.Try again.");
			PurchaseView.this.finish();
		}
		MainView.db.close();
		String orderSummary="ORDER SUMMARY";
		String summary = "Movie : " + selectedMovie+
				"\nTheatre : " + selectedTheatre+
				"\nShow Date : " + selectedShowDate+
				"\nShow Time : " + selectedShowTime+
				"\nNumber of Tickets  : " + numOfTicket+
				"\nTotal Cost : $"+ price;
		playBack("You movie is " + selectedMovie+ "," +
				" at the theatre " + selectedTheatre+ "," +
				" on " + selectedShowDate+ "," +
				" at " + selectedShowTime+ "," +
				" Number of Tickets are " + numOfTicket+ "," +
				" and Total Cost is $"+ price+"." +" Press to confirm and proceed to payment.");

		TextView t1 = (TextView) findViewById(R.id.textViewP1); 
		t1.setTextSize(30);
		t1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
		t1.setText(orderSummary);
		TextView t2 = (TextView) findViewById(R.id.textViewP2); 
		t2.setTextSize(20);
		t2.setTextAlignment(View.TEXT_ALIGNMENT_INHERIT);
		t2.setText(summary);
		final TextView t3 = (TextView) findViewById(R.id.textViewP3);
		final TextView t6 = (TextView) findViewById(R.id.textViewP6);
		t6.setText("\n\n\n\nSESSION EXPIRES IN" + "\n");
		t6.setTextSize(20);
		final Button b = (Button) findViewById(R.id.btn_startPurchase);
		Button.OnClickListener startListener = new Button.OnClickListener()
		{
			@Override
			public void onClick(View v) {

				MainView.db.open();
				bought = MainView.db.sellTicket(SelectedShowDetails,numOfTicket);
				MainView.db.close();
				if(bought){
					Intent i = new Intent(PurchaseView.this,FinalView.class);
					startActivity(i);
				}
				else{
					playBack("Transaction Failure. Please try again");
					PurchaseView.this.finish();
				}

			}
		};
		b.setOnClickListener(startListener);


		new CountDownTimer(200000, 1000) {

			public void onTick(long millisUntilFinished) {

				t3.setText("" + (millisUntilFinished / 1000));
				t3.setTextSize(30);
			}

			public void onFinish() {
				finish();
			}
		}.start();
	}
	private void playBack(String tts){

		if (tts != null)
		{
			Object _lastTtsContext = new Object();
			_vocalizer5.speakString(tts, _lastTtsContext);
		}
	}
}

