package org.jakstab;

public class Option<T> {

	private final String name;
	private final String paramName;
	private final T defaultValue;
	private final String description;
	private T value;
	
	public static <T> Option<T> create(String name, String paramName, T defaultValue, String description) {
		return new Option<T>(name, paramName, defaultValue, description);
	}
	
	public static Option<Boolean> create(String name, String description) {
		return new Option<Boolean>(name, "", Boolean.FALSE, description);
	}

	private Option(String name, String paramName, T defaultValue, String description) {
		super();
		assert (!name.startsWith("-")) : "Option names should be defined without dashes";
		if (name.length() == 1) {
			this.name = "-" + name;
		} else {
			this.name = "--" + name;
		}
		if (paramName != null && paramName != "")
			this.paramName = paramName;
		else 
			this.paramName = null;
		this.defaultValue = defaultValue;
		this.description = description;
		this.value = defaultValue;
		Options.addOption(this);
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the paramName
	 */
	public String getParamName() {
		return paramName;
	}

	/**
	 * @return the paramType
	 */
	public T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the value
	 */
	public T getValue() {
		return value;
	}
	
	/**
	 * @param value the value to set
	 */
	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		this.value = (T)value;
	}

	@Override
	public String toString() {
		return "Option [name=" + name + ", paramName=" + paramName
				+ ", defaultValue=" + defaultValue + ", description="
				+ description + ", value=" + value + "]";
	}
	
}
