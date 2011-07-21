package org.jakstab;

import java.util.Map;
import java.util.Set;

public class AnalysisProperties {

	private String name;
	private String description;
	private char shortHand;
	private boolean isExplicit;
	private Map<String, Class<?>> options;

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
	
	public void addOption(String name, String paramName, String description, Class<?> clazz) {
		options.put(name, clazz);
	}
	
	public void addOption(String name, String description) {
		addOption(name, description, null, Boolean.class);
	}
	
	public Set<Map.Entry<String, Class<?>>> getOptions() {
		return options.entrySet();
	}
}
