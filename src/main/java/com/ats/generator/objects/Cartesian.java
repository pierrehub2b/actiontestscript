package com.ats.generator.objects;

public enum Cartesian {
	LEFT("left"),
	RIGHT("right"),
	TOP("top"),
	BOTTOM("bottom"),
	MIDDLE("middle"),
	CENTER("center")
	;

	private final String text;

	/**
	 * @param text
	 */
	private Cartesian(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
	
	public boolean equals(String value) {
		return text.equals(value);
	}
	
	public String getJavacode() {
		return this.getClass().getSimpleName() + "." + text.toUpperCase();
	}
}
