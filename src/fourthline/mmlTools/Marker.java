/*
 * Copyright (C) 2014 たんらる
 */

package fourthline.mmlTools;

public final class Marker extends MMLEvent implements Comparable<Marker> {
	private static final long serialVersionUID = -1282880034361350447L;
	private String name;

	public Marker(int tickOffset, String name) {
		super(tickOffset);
		this.name = name;
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
