package org.jakstab;

public class AnalysisProperties {

	private String name;
	private String description;
	private char shortHand;
	private boolean isExplicit;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public char getShortHand() {
		return shortHand;
	}
	
	public void setShortHand(char shortHand) {
		this.shortHand = shortHand;
	}
	
	public boolean isExplicit() {
		return isExplicit;
	}
	
	public void setExplicit(boolean isExplicit) {
		this.isExplicit = isExplicit;
	}
	
}
