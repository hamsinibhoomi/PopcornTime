package com.nuance.nmdp.sample;

public class ShowTable {
	private long id;
	private String showDate;
	private String showTime;
	private int tickets;
	private int price;
	
	public long getId() {
		return id;
	}
	public int getPrice() {
		return price;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getShowDate() {
		return showDate;
	}
	public void setShowDate(String showDate) {
		this.showDate = showDate;
	}
	public String getShowTime() {
		return showTime;
	}
	public void setShowTime(String showTime) {
		this.showTime = showTime;
	}
	public int getTickets() {
		return tickets;
	}
	public void setTickets(int tickets) {
		this.tickets = tickets;
	}
	public void setPrice(int price) {
		this.price=price;
		
	}
	
	@Override
	  public String toString() {
	    return showDate + "\n" + showTime + "\n" + price;
	  }


	
}
