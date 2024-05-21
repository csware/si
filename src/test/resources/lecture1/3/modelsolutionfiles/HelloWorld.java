/**
 * Class that prints out a greeting to the console
 * @author Sven Strickroth
 * @version 2.0
 */
public class HelloWorld {
	/**
	 * Prints the greeting message
	 * @param args Command line parameters, not used
	 */
	public static void main(String[] args) {
		System.out.println(greeting());
	}

	/**
	 * Returns the greeting to print out.
	 * @return the greeting
	 */
	private static String greeting() {
		return "Hello World!";
	}
}
