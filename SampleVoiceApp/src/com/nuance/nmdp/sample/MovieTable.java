package com.nuance.nmdp.sample;

public class MovieTable {
	private long id;
	private String name;
	private String desc;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMovie() {
		return name;
	}

	public void setMovie(String movie) {
		this.name = movie;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return name + "\n" + desc;
	}
}
