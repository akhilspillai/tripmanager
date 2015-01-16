package com.trip.utils;


public class DistributionBean1 {

	private long distributionId;
	private long fromId;
	private long toId;
	private long tripId;
	private String amount;
	private String paid;
	private String synced;
	private String creationDate;
	
	public long getFromId() {
		return fromId;
	}
	public void setFromId(long fromId) {
		this.fromId = fromId;
	}
	public long getDistributionId() {
		return distributionId;
	}
	public void setDistributionId(long distributionId) {
		this.distributionId = distributionId;
	}
	public long getToId() {
		return toId;
	}
	public void setToId(long toId) {
		this.toId = toId;
	}
	public long getTripId() {
		return tripId;
	}
	public void setTripId(long tripId) {
		this.tripId = tripId;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getPaid() {
		return paid;
	}
	public void setPaid(String paid) {
		this.paid = paid;
	}
	public String getSynced() {
		return synced;
	}
	public void setSynced(String synced) {
		this.synced = synced;
	}
	public String getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	
	
}
