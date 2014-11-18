package com.kbb.livedrive.profile;

public class CurrentPlayer extends Player {
	
	private double previousDriverScore;
	private double latestDriverScore;
	private double bestDriverScore;
	private String driverRank;
	
	private double previousMpgScore;
	private double latestMpgScore;
	private double bestMpgScore;
	private String mpgRank;
	
	
	public synchronized double getPreviousDriverScore() {
		return previousDriverScore;
	}
	
	public synchronized long getPreviousDriverScoreLong(){
		return Math.min(Math.max(Math.round(previousDriverScore), 50), 100);
	}
	
	public synchronized void setPreviousDriverScore(double previousDriverScore) {
		this.previousDriverScore = previousDriverScore;
	}
	
	public synchronized double getLatestDriverScore() {
		return latestDriverScore;
	}
	
	public synchronized long getLatestDriverScoreLong(){
		return Math.min(Math.max(Math.round(latestDriverScore), 50), 100);
	}
	
	public synchronized void setLatestDriverScore(double latestDriverScore) {
		this.latestDriverScore = latestDriverScore;
	}
	
	public synchronized double getBestDriverScore() {
		return bestDriverScore;
	}
	
	public synchronized long getBestDriverScoreLong(){
		return Math.min(Math.max(Math.round(bestDriverScore), 50), 100);
	}
	
	public synchronized void setBestDriverScore(double bestDriverScore) {
		this.bestDriverScore = bestDriverScore;
	}
	
	public synchronized String getDriverRank() {
		return driverRank;
	}
	
	public synchronized void setDriverRank(String driverRank) {
		this.driverRank = driverRank;
	}
	
	public synchronized double getPreviousMpgScore() {
		return previousMpgScore;
	}
	
	public synchronized long getPreviousMpgScoreLong(){
		return Math.min(Math.max(Math.round(previousMpgScore), 50), 100);
	}

	public synchronized void setPreviousMpgScore(double previousMpgScore) {
		this.previousMpgScore = previousMpgScore;
	}
	
	public synchronized double getLatestMpgScore() {
		return latestMpgScore;
	}

	public synchronized long getLatestMpgScoreLong(){
		return Math.min(Math.max(Math.round(latestMpgScore), 50), 100);
	}
	
	public synchronized void setLatestMpgScore(double latestMpgScore) {
		this.latestMpgScore = latestMpgScore;
	}
	
	public synchronized double getBestMpgScore() {
		return bestMpgScore;
	}
	public synchronized long getBestMpgScoreLong(){
		return Math.min(Math.max(Math.round(bestMpgScore), 50), 100);
	}
	
	public synchronized void setBestMpgScore(double bestMpgScore) {
		this.bestMpgScore = bestMpgScore;
	}
	
	public synchronized String getMpgRank() {
		return mpgRank;
	}
	
	public synchronized void setMpgRank(String mpgRank) {
		this.mpgRank = mpgRank;
	}
	
	
	public static CurrentPlayer createDummy(){
		
		CurrentPlayer player = new CurrentPlayer();
		player.setUserName("LiveDrive2014");
		player.setImageUrl("");
		
		player.setBestDriverScore(76);
		player.setLatestDriverScore(75);
		player.setPreviousDriverScore(68);
		player.setDriverRank("10");
		
		player.setBestMpgScore(86);
		player.setLatestMpgScore(80);
		player.setPreviousMpgScore(84);
		player.setMpgRank("5");
		
		return player;
	}
	

}
