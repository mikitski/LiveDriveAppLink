package com.kbb.livedrive.profile;

import android.os.Parcel;
import android.os.Parcelable;

public class Player implements Parcelable {
	
	private String userName;
	private String imageUrl;
	private String rank;
	private String score;
	
	
	public Player(){

	}
	
	public synchronized String getImageUrl() {
		return imageUrl;
	}
	
	public synchronized void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	public synchronized String getUserName() {
		return userName;
	}
	
	public synchronized void setUserName(String userName) {
		this.userName = userName;
	}
	
	public synchronized String getRank() {
		return rank;
	}
	
	public synchronized void setRank(String leaderboardPosition) {
		this.rank = leaderboardPosition;
	}
	
	public synchronized String getScore() {
		return score;
	}
	
	public synchronized void setScore(String score) {
		this.score = score;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int arg1) {
		parcel.writeString(userName);
		parcel.writeString(imageUrl);
		parcel.writeString(rank);
		parcel.writeString(score);
		
	}
	
	public Player(Parcel in){
		
		this.userName = in.readString();
		this.imageUrl = in.readString();
		this.rank = in.readString();
		this.score = in.readString();
	}
	
	public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {
		public Player createFromParcel(Parcel in) {
			return new Player(in);
		}

		public Player[] newArray(int size) {
			return new Player[size];
		}
	};

}
