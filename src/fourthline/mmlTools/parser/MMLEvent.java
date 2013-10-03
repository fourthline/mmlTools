package fourthline.mmlTools.parser;


/**
 * MMLEvent
 * @author fourthline
 *
 */
public abstract class MMLEvent {
	
	public static final int NOTE = 1;
	public static final int TEMPO = 2;
	public static final int VELOCITY = 3;
	
	private int type;
	
	protected MMLEvent(int type) {
		this.type = type;
	}
	
	public int getEventType() {
		return type;
	}
	
	public abstract String toString();
}
