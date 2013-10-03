package fourthline.mmlTools.parser;

public class MMLTempoEvent extends MMLEvent {

	private int tempo;
	
	public MMLTempoEvent(int tempo) {
		super(MMLEvent.TEMPO);
		
		this.tempo = tempo;
	}
	
	public int getTempo() {
		return this.tempo;
	}

	@Override
	public String toString() {
		return "[Tempo] " + tempo;
	}
	
}
