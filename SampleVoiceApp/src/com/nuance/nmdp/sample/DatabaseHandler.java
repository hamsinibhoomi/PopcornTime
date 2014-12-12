package com.nuance.nmdp.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;

public class DatabaseHandler extends SQLiteOpenHelper {



	private static final int DATABASE_VERSION = 1;

	SQLiteDatabase database;
	private final Context myContext;

	private static String DB_PATH = "/data/data/com.nuance.nmdp.sample/databases/";

	// Database Name
	static final String DATABASE_NAME = "PopcornTime";

	// Table Names
	private static final String TABLE_MOVIES = "movies";
	private static final String MOVIE_ID = "_id";
	private static final String MOVIE_NAME = "MovieName";
	private static final String MOVIE_DESC = "MovieDesc";

	private static final String TABLE_THEATRES = "theatres";
	private static final String THEATRE_ID = "_id1";
	private static final String THEATRE_NAME = "TheatreName";
	private static final String THEATRE_STREET = "TheatreStreet";
	private static final String THEATRE_CITY = "TheatreCity";


	private static final String TABLE_SHOW = "show";
	private static final String SHOW_ID ="_showid";
	private static final String SHOW_TIME ="ShowTime";
	private static final String SHOW_DATE ="ShowDate";
	private static final String SHOW_TICKETS ="tickets";
	private static final String SHOW_PRICE = "ShowPrice";

	private static final String TABLE_MTSJOIN = "movie_theatre_show";
	private static final String MTSJOIN_ID ="_mtsid";
	private static final String MTSJOIN_MID ="MovieId";
	private static final String MTSJOIN_TID ="TheatreId";
	private static final String MTSJOIN_SID ="ShowId";

	private String[] allMovieColumns = {MOVIE_ID, MOVIE_NAME,MOVIE_DESC };
	private String[] allTheatreColumns = { THEATRE_ID,
			THEATRE_NAME, THEATRE_STREET, THEATRE_CITY };
	private String[] allShowColumns = { SHOW_ID, SHOW_DATE,
			SHOW_TIME, SHOW_TICKETS,SHOW_PRICE };
	private String[] allJoinColumns = { MTSJOIN_ID, MTSJOIN_MID,
			MTSJOIN_TID, MTSJOIN_SID };
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.myContext = context;
		// TODO Auto-generated constructor stub
	} 

	//Creating Database
	@Override
	public void onCreate(SQLiteDatabase db) {

		Log.v("gtry", "onCreate Method");
		String MOVIE_TABLE_CREATE = "create table IF NOT EXISTS "
				+ TABLE_MOVIES + "(" + MOVIE_ID
				+ " integer primary key autoincrement, " + MOVIE_NAME+" text not null, "+MOVIE_DESC
				+ " text not null);";
		String THEATRE_TABLE_CREATE = "create table IF NOT EXISTS "
				+ TABLE_THEATRES + "(" + THEATRE_ID + " integer primary key autoincrement, "
				+ THEATRE_NAME + " text not null, " 
				+ THEATRE_STREET + " text not null, "
				+ THEATRE_CITY + " text not null);";
		String SHOW_TABLE_CREATE = "create table IF NOT EXISTS "
				+ TABLE_SHOW + "(" + SHOW_ID + " integer primary key autoincrement, "
				+ SHOW_DATE + " text not null, "+ SHOW_TIME+" text not null, "
				+ SHOW_TICKETS +" INTEGER , "+SHOW_PRICE+ " INTEGER);";
		String JOIN_TABLE_CREATE = "create table IF NOT EXISTS "
				+ TABLE_MTSJOIN + "(" + MTSJOIN_ID + " integer primary key , "
				+ MTSJOIN_MID + " integer, "+ MTSJOIN_TID+" integer, "
				+MTSJOIN_SID+ " INTEGER);";
		// TODO Auto-generated method stub
		db.execSQL(MOVIE_TABLE_CREATE);
		db.execSQL(THEATRE_TABLE_CREATE);
		db.execSQL(SHOW_TABLE_CREATE);
		db.execSQL(JOIN_TABLE_CREATE);
	}

	//Upgrading Database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_THEATRES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOW);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MTSJOIN);
		Log.v("gtry", "dropping tables");
		onCreate(db);

	}

	//If the database is open
	@Override
	public void onOpen(SQLiteDatabase db){
		super.onOpen(database);
		Log.v("gtry", "in onOpen");
	}

	//Opening Database
	public void open() throws SQLException {
		database = this.getWritableDatabase();
		Log.v("gtry","in open database");

	}

	//Closing Database
	public void close() {
		if(database != null)
			database.close();

		super.close();
		Log.v("gtry","in close database");
	}

	//Creating Movie Table and Inserting
	public MovieTable createMovie(String movie,String desc) {
		ContentValues values = new ContentValues();
		values.put(MOVIE_NAME, movie);
		values.put(MOVIE_DESC, desc);
		long insertId = database.insert(TABLE_MOVIES, null,
				values);
		Cursor cursor = database.query(TABLE_MOVIES,
				allMovieColumns, MOVIE_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		MovieTable newMovieTable = cursorToMovie(cursor);
		cursor.close();
		return newMovieTable;
	}

	//Deleting Database
	public void deleteMovie(MovieTable movieTable) {
		long id = movieTable.getId();
		System.out.println("Movie deleted with id: " + id);
		database.delete(TABLE_MOVIES, MOVIE_ID
				+ " = " + id, null);
	}

	//Movie List
	public List<MovieTable> getAllMovies() {
		List<MovieTable> movies = new ArrayList<MovieTable>();

		Cursor cursor = database.query(TABLE_MOVIES,
				allMovieColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			MovieTable movieTable = cursorToMovie(cursor);
			movies.add(movieTable);
			cursor.moveToNext();
		}
		cursor.close();
		return movies;
	}

	private MovieTable cursorToMovie(Cursor cursor) {
		MovieTable movie = new MovieTable();
		movie.setId(cursor.getLong(0));
		movie.setMovie(cursor.getString(cursor.getColumnIndex(MOVIE_NAME)));
		movie.setDesc(cursor.getString(cursor.getColumnIndex(MOVIE_DESC)));

		return movie;
	}

	//Creating Theatre table and inserting 
	public TheatreTable createTheatre(String theatreName, String theatreStreet, String theatreCity) {
		Log.v("gtry", "in create theatre method");
		ContentValues values = new ContentValues();
		values.put(THEATRE_NAME, theatreName);
		values.put(THEATRE_STREET, theatreStreet);
		values.put(THEATRE_CITY, theatreCity);
		long insertId = database.insert(TABLE_THEATRES, null,
				values);
		Cursor cursor = database.query(TABLE_THEATRES,
				allTheatreColumns, THEATRE_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		TheatreTable newTheatreTable = cursorToTheatre(cursor);
		cursor.close();
		return newTheatreTable;
	}

	//Deleting Theatre table
	public void deleteTheatre(TheatreTable theatreTable) {
		long id = theatreTable.getId();
		System.out.println("Theatre deleted with id: " + id);
		database.delete(TABLE_MOVIES, MOVIE_ID
				+ " = " + id, null);
	}

	// Theatre list from database
	public List<TheatreTable> getAllTheatre() {
		List<TheatreTable> theatres = new ArrayList<TheatreTable>();

		Cursor cursor = database.query(TABLE_THEATRES,
				allTheatreColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			TheatreTable theatreTable = cursorToTheatre(cursor);
			theatres.add(theatreTable);
			cursor.moveToNext();
		}

		cursor.close();
		return theatres;
	}

	private TheatreTable cursorToTheatre(Cursor cursor) {
		TheatreTable theatre = new TheatreTable();
		theatre.setId(cursor.getLong(cursor.getColumnIndex(THEATRE_ID)));
		theatre.setTheatreName(cursor.getString(cursor.getColumnIndex(THEATRE_NAME)));
		theatre.setTheatreStreet(cursor.getString(cursor.getColumnIndex(THEATRE_STREET)));
		theatre.setTheatreCity(cursor.getString(cursor.getColumnIndex(THEATRE_CITY)));
		return theatre;
	} 

	//Creating Shows table and inserting
	public ShowTable createShow(String ShowDate, String ShowTime, Integer tickets, Integer price) {
		Log.v("gtry", "in create show method");
		ContentValues values = new ContentValues();
		//values.put(SHOW_ID, ShowId);
		values.put(SHOW_DATE, ShowDate);
		values.put(SHOW_TIME, ShowTime);
		values.put(SHOW_TICKETS, tickets);
		values.put(SHOW_PRICE, price);

		long insertId = database.insert(TABLE_SHOW, null,
				values);
		Cursor cursor = database.query(TABLE_SHOW,
				allShowColumns,SHOW_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		ShowTable newShowEntry = cursorToShow(cursor);
		cursor.close();
		if(insertId==-1){
			Log.v("gtry", "show not inserted");
		}
		else{
			Log.v("gtry", "show inserted");

		}
		return newShowEntry;
	}

	//Deleting Show Table
	public void deleteShow(ShowTable show) {
		long id = show.getId();
		System.out.println("Show deleted with id: " + id);
		database.delete(TABLE_SHOW, SHOW_ID
				+ " = " + id, null);
	}

	//Listing all Shows from Database
	public List<ShowTable> getAllShows() {
		List<ShowTable> showsList = new ArrayList<ShowTable>();

		Cursor cursor = database.query(TABLE_SHOW,
				allShowColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ShowTable show = cursorToShow(cursor);
			showsList.add(show);
			cursor.moveToNext();
		}

		cursor.close();
		return showsList;
	}

	private ShowTable cursorToShow(Cursor cursor) {
		ShowTable show = new ShowTable();
		show.setId(cursor.getInt(0));
		show.setShowDate(cursor.getString(cursor.getColumnIndex(SHOW_DATE)));
		show.setShowTime(cursor.getString(cursor.getColumnIndex(SHOW_TIME)));
		show.setTickets(cursor.getInt(cursor.getColumnIndex(SHOW_TICKETS)));
		show.setPrice(cursor.getInt(cursor.getColumnIndex(SHOW_PRICE)));
		return show;
	}

	//Join Table
	public void insertJoin(String movieName,String TheatreName, long n) {
		Log.v("gtry", "in create join method");

		Integer movieId =getidFromTableAndName(TABLE_MOVIES, MOVIE_NAME  , movieName);
		Log.v("gtry", "movie id"+movieId);
		Integer theatreId =getidFromTableAndName(TABLE_THEATRES, THEATRE_NAME  , TheatreName);
		Log.v("gtry", "movie id"+movieId);

		ContentValues values = new ContentValues();
		values.put(MTSJOIN_MID,movieId );
		values.put(MTSJOIN_TID, theatreId);
		values.put(MTSJOIN_SID,n );

		long insertId = database.insert(TABLE_MTSJOIN, null,
				values);
		if(insertId==-1){
			Log.v("gtry", "Join not inserted");
		}
		else{
			Log.v("gtry", "Join inserted");

		}
	}

	Integer getidFromTableAndName(String tableName,String field,String name){

		String selectQuery = "SELECT * FROM " + tableName + " WHERE "
				+ field + "='" + name+"'";
		Log.v("dbquery",selectQuery );
		Cursor c = database.rawQuery(selectQuery, null);
		c.moveToFirst();
		if(!c.isAfterLast()){
			int c1 = c.getInt(0);
			Log.v("gtry", "id-->"+c1);
			c.close();
			return c1;
		}
		else{
			Log.v("gtry", "id-->");
			c.close();
			return null; 
		}
	}


	List<TheatreTable> getMovieTheatreList(String MovieName){

		List<TheatreTable> theatres = new ArrayList<TheatreTable>();
		Integer movieId =getidFromTableAndName(TABLE_MOVIES, MOVIE_NAME  , MovieName);
		Log.v("gtry", "movie id"+movieId);
		String selectQuery = "SELECT DISTINCT  t._id1 ,t.TheatreName ,t.TheatreStreet ,t.TheatreCity from theatres AS t INNER JOIN movie_theatre_show AS j "+
				"WHERE t._id1 = j.TheatreId  AND j.MovieId = "+movieId;
		Log.v("dbquery",selectQuery );
		Cursor cursor = database.rawQuery(selectQuery,null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.v("gtry","ging to cursor" );
			TheatreTable theatreTable = cursorToTheatre(cursor);
			theatres.add(theatreTable);
			cursor.moveToNext();
		}

		cursor.close();
		return theatres;


	}

	public void getAllJoin() {


		Cursor cursor = database.query(TABLE_MTSJOIN,
				allJoinColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.v("join id --",cursor.getInt(0)+"id in join"+cursor.getInt(1)+""+cursor.getInt(2)+""+cursor.getInt(3));
			cursor.moveToNext();
		}

		cursor.close();

	}

	List<ShowTable> getShowsDatesFromMovieAndTheatre(String movieName,String TheatreName){
		List<ShowTable> showsList = new ArrayList<ShowTable>();
		Integer movieId =getidFromTableAndName(TABLE_MOVIES, MOVIE_NAME  , movieName);
		Log.v("gtry", "movie id"+movieId);
		Integer theatreId =getidFromTableAndName(TABLE_THEATRES, THEATRE_NAME  , TheatreName);
		Log.v("gtry", "movie id"+movieId);
		String selectQuery = "SELECT DISTINCT * from show where _showid IN "+
				"(SELECT j.ShowId from  movie_theatre_show AS j WHERE MovieId ="+movieId+" AND j.TheatreId ="+ theatreId+")";
		Log.v("dbquery",selectQuery );
		Cursor cursor = database.rawQuery(selectQuery,null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.v("gtry","ging to cursor" );
			ShowTable show = cursorToShow(cursor);
			showsList.add(show);
			cursor.moveToNext();
		}
		cursor.close();
		return showsList;
	}

	List<ShowTable> getShowTimesFromMovieAndTheatreAndDate(String movieName,String TheatreName,String Date){
		List<ShowTable> showsList = new ArrayList<ShowTable>();
		Log.v("gtry","in get time method" );
		Integer movieId = getidFromTableAndName(TABLE_MOVIES, MOVIE_NAME  , movieName);
		Log.v("gtry", "movie id"+movieId);
		Integer theatreId = getidFromTableAndName(TABLE_THEATRES, THEATRE_NAME  , TheatreName);
		Log.v("gtry", "movie id"+movieId);
		String selectQuery = "SELECT * from show where _showid IN "+
				"(SELECT j.ShowId from  movie_theatre_show AS j WHERE MovieId = "+movieId+
				" AND j.TheatreId = "+ theatreId+") AND ShowDate ='"+Date+"'";
		Log.v("dbquery",selectQuery );
		Cursor cursor = database.rawQuery(selectQuery,null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.v("gtry","ging to cursor" );
			ShowTable show = cursorToShow(cursor);
			showsList.add(show);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return showsList;
	}

	ShowTable checkForTickets(String movieName,String TheatreName,String Date,String Time){
		ShowTable showsList = new ShowTable();
		Log.v("gtry","in get time method" );
		Integer movieId = getidFromTableAndName(TABLE_MOVIES, MOVIE_NAME  , movieName);
		Log.v("gtry", "movie id"+movieId);
		Integer theatreId = getidFromTableAndName(TABLE_THEATRES, THEATRE_NAME  , TheatreName);
		Log.v("gtry", "movie id"+movieId);
		String selectQuery = "SELECT * from show where _showid IN "+
				"(SELECT j.ShowId from  movie_theatre_show AS j WHERE MovieId ="+movieId+
				" AND j.TheatreId ="+ theatreId+") AND ShowDate ='"+Date+"' AND ShowTime ='"+Time+"'";
		Log.v("dbquery",selectQuery );
		Cursor cursor = database.rawQuery(selectQuery,null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.v("gtry","ging to cursor for ticket" );
			ShowTable show = cursorToShow(cursor);
			showsList= show;
			cursor.moveToNext();
		}

		cursor.close();
		return showsList;
	}

	boolean sellTicket(ShowTable selectedShowDetails , int ticket){

		Log.v("dbquery","in sell method" );
		int left = selectedShowDetails.getTickets()-ticket;
		ContentValues values = new ContentValues();
		values.put(SHOW_TICKETS,left);
		int sold= database.update(TABLE_SHOW, values, SHOW_ID + " = ?",
				new String[] { String.valueOf(selectedShowDetails.getId()) });
		if(sold >0){
			return true;
		}
		return false;
	}

	//Database copying and saving

	public void createDataBase() throws IOException{

		boolean dbExist = checkDataBase();

		if(dbExist){
			//do nothing - database already exist
		}else{

			//By calling this method and empty database will be created into the default system path
			//of your application so we are gonna be able to overwrite that database with our database.
			this.getReadableDatabase();

			try {

				copyDataBase();

			} catch (IOException e) {

				throw new Error("Error copying database");

			}
		}

	}

	private boolean checkDataBase(){

		SQLiteDatabase checkDB = null;

		try{
			String myPath = DB_PATH + DATABASE_NAME;
			
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

		}catch(SQLiteException e){

			//database does't exist yet.

		}

		if(checkDB != null){

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	private void copyDataBase() throws IOException{

		//Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DATABASE_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DATABASE_NAME;

		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0){
			myOutput.write(buffer, 0, length);
		}

		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void openDataBase() throws SQLException{

		//Open the database
		String myPath = DB_PATH + DATABASE_NAME;
		database = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

	}

}