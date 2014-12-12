package com.nuance.nmdp.sample;

import java.util.Calendar;
import java.util.TimeZone;

import android.util.Log;

public class DataCreation {
	Calendar today = Calendar.getInstance(TimeZone.getTimeZone("EST"));
	int day = today.get(Calendar.DAY_OF_MONTH);
	int month = today.get(Calendar.MONTH) + 1;
	int year = today.get(Calendar.YEAR);
	String[] movieList;
	String[] descList;
	MovieTable movie;
	String[] theatreList;
	String[] streetList;
	TheatreTable theatre;
	String [] showDateList;
	ShowTable show;
	void  createData(){
		MainView.db.open();
		Log.v("gtry","database opened");
		movieList = new String[]{"Interstellar", "Gone Girl","John Wick","The Hunger Games MockingJay Part One", "Horrible Bosses Two","Penguins of Madagascar","Big Hero Six","Dumb and Dumber To","Beyond the Lights","Saint Vincent","The Theory of Everything","The Judge","The Maze Runner","Guardians of the Galaxy","The Boxtrolls","The Book of Life","Dolphin Tale Two","Left Behind", "No Good Deed","Maleficent","Addicted","Teanage Mutant Ninja Turtles","Lucy","Annabelle","Ouija","The Equalizer"};
		descList = new String[]{"2hr 49min - Rated PG-13 - Action/Adventure", "2hr 25min - Rated R - Suspense/Thriller", "1hr 36min - Rated R - Action/Adventure/Suspense/Thriller","2hr 3min - Rated PG-13 - Action/Adventure/Scifi/Fantasy/Drama","1hr 48min - Rated R - Comedy","1hr 32min - Rated PG - Animation/Comedy","1hr 45min - Rated PG - Animation","1hr 50min - Rated PG-13 - Comedy","1hr 56min - Rated PG-13 - Drama","1hr 43min - Rated PG-13 - Comedy","2hr 3min - Rated PG-13 - Drama","2hr 21min - Rated R - Drama","1hr 53min - Rated PG-13 - Action/Adventure/Suspense/Thriller","2hr 2min - Rated PG-13 - Action/Adventure/Scifi/Fantasy","1hr 40min - Rated PG - Animation","1hr 35min - Rated PG - Animation","1hr 47min - Rated PG - Drama","1hr 45min - Rated PG-13 - Action/Adventure/Scifi/Fantasy/Suspense/Thriller","1hr 24min - Rated PG-13 - Suspense/Thriller","1hr 37min - Rated PG - Action/Adventure","1hr 45min - Rated R - Drama","1hr 41min - Rated PG-13 - Action/Adventure","1hr 29min - Rated R - Action/Adventure","1hr 35min - Rated R - Horror","1hr 30min - Rated PG-13 - Action/Adventure/Suspense/Thriller","2hr 11min - Rated R - Action/Adventure/Suspense/Thriller"};
		for(int i = 0; i < movieList.length - 1; i++){
			Log.v("gtry", movieList[i]);
			Log.v("gtry", descList[i]);
			movie = MainView.db.createMovie(movieList[i],descList[i]);	
		}
		theatreList = new String[]{"Rialto Theater", "Mission Valley Cinema", "Regal North Hills Stadium 14","Carmike Blue Ridge 14 Cinema","Colony Theatrer", "Carmike 15","Raleighwood Cinema Grill","Six Forks Station Cinema", "Carolina Cinemas Raleigh Grande 16"};
		streetList = new String[]{"1620 Glenwood Avenue","2109-124 Avent Ferry Road","4150 Main", "600 Blue Ridge Road","5438 Six Forks Road","5501 Atlantic Springs Road","6609 Falls of Neuse Rd","9500 Forum Drive"};
		for(int i = 0; i < 4; i++){
			Log.v("gtry",theatreList[i]);
			theatre = MainView.db.createTheatre(theatreList[i],streetList[i],"Raleigh, NC");
		}

		showDateList = new String[7];
		for(int i = 0; i <showDateList.length; i++){
			showDateList[i] = month + "-" + (day + i) + "-" + year;
		}


		for (int j = 0; j < movieList.length - 1;j++) {
			for (int k = 0; k < theatreList.length - 1;k++) {
				for (int i = 0; i < showDateList.length - 1; i++) {

					show = MainView.db.createShow(showDateList[i],"10:30",4,14);
					MainView.db.insertJoin(movieList[j], theatreList[k], show.getId());

					show = MainView.db.createShow(showDateList[i],"12:15",12,6);
					MainView.db.insertJoin(movieList[j], theatreList[k], show.getId());

					show = MainView.db.createShow( showDateList[i],"14:45",50,8);
					MainView.db.insertJoin(movieList[j], theatreList[k], show.getId());

					show = MainView.db.createShow(showDateList[i],"22:30",6,6);
					MainView.db.insertJoin(movieList[j], theatreList[k], show.getId());

					show = MainView.db.createShow(showDateList[i],"23:00",48,7);
					MainView.db.insertJoin(movieList[j], theatreList[k], show.getId());

					show = MainView.db.createShow(showDateList[i],"1:30",49,7);
					MainView.db.insertJoin(movieList[j], theatreList[k], show.getId());
				}
			}
		}
		MainView.db.getAllJoin();
		MainView.db.close();
	}

}
