/*
 * Copyright (C) 2014 たんらる
 */

package jp.fourthline.mmlTools;

public final class Marker extends MMLEvent implements Comparable<Marker> {
	private static final long serialVersionUID = -1282880034361350447L;
	public static final int META = 0x06;
	private String name;

	public Marker(String name, int tickOffset) {
		super(tickOffset);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getMetaData() {
		return name.getBytes();
	}

	@Override
	public String toString() {
		return getTickOffset() + "=" + name;
	}

	@Override
	public int compareTo(Marker o) {
		if (this.getTickOffset() == o.getTickOffset()) {
			return this.name.compareTo(o.name);
		} else {
			return (this.getTickOffset() - o.getTickOffset());
		}
	}

	@Override
	public String toMMLString() {
		return "";
	}
}
