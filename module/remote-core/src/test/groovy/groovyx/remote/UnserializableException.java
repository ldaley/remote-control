package groovyx.remote;

public class UnserializableException extends Exception {
	private UnserializableThing thing = new UnserializableThing();

	public UnserializableThing getThing() {
		return thing;
	}
}

