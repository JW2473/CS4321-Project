package visitor;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * UnsupportedException deals with exceptions returned by the unsupported expressions
 *
 */
public class UnsupportedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnsupportedException() {
		super();
	}

	public UnsupportedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public UnsupportedException(String arg0) {
		super(arg0);
	}

	public UnsupportedException(Throwable arg0) {
		super(arg0);
	}

}
