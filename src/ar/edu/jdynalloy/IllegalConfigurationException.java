package ar.edu.jdynalloy;

class IllegalConfigurationException extends JDynAlloyException {

	private static final long serialVersionUID = 1L;

	public IllegalConfigurationException(String arg0) {
		super(arg0);
	}

	public IllegalConfigurationException(String string, Exception e) {
	    super(string, e);
	}

}
