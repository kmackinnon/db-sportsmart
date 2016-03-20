package com.company.option;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Option {
	protected String name;
	protected List<String> subOptionNames;
	protected Map<String, String> subOptionValues;

	protected Option(String name) {
		this.name = name;
		this.subOptionNames = null;
	}
	
	protected Option(String name, String[] subOptionNames) {
		this(name, Arrays.asList(subOptionNames));
	}

	protected Option(String name, List<String> subOptionNames) {
		this.name = name;
		this.subOptionNames = subOptionNames;
		this.subOptionValues = new HashMap<>();
	}

	public void setSubOptionValue(String subOption, String value) {
		this.subOptionValues.put(subOption, value);
	}

	public String getName() {
		return this.name;
	}

	public List<String> getSubOptionNames() {
		return this.subOptionNames;
	}
	
	public boolean hasSubOptions() {
		return this.subOptionNames != null && this.subOptionNames.size() > 0;
	}

	public abstract Result execute() throws ExecutionException;

	public static class Result {
		public List<String> columnNames;
		public List<List<String>> results;
		public String message;
	}
	
}
