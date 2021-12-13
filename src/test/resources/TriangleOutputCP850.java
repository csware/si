import java.util.Scanner; /* notwendig, damit der Scanner funktioniert */

public class TriangleOutput {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in); /* �ffnet die Konsole */
		System.out.println("Bitte geben Sie die H�he des Dreiecks ein: ");
		int triangleSize = scanner.nextInt(); /* liest einen Integer von der Konsole */
		drawTriangle(triangleSize);
		scanner.close(); /* schlie�t die Konsole */
	}

	static void drawTriangle(int sizeOfTriangle) {
		for (int i = 0; i < sizeOfTriangle; i++) {
			for (int j = 0; j <= i; j++) {
				System.out.print("*");
			}
			System.out.println();
		}
	}
}
