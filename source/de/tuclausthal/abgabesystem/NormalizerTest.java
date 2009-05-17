package de.tuclausthal.abgabesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import de.tuclausthal.abgabesystem.dupecheck.Normalizer;
import de.tuclausthal.abgabesystem.dupecheck.StackNormalizer;

public class NormalizerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		File juhu = new File("C:\\abgabesystem\\1\\2\\6\\SimpleForLoop.java");
		BufferedReader br = new BufferedReader(new FileReader(juhu));
		StringBuffer sb = new StringBuffer((int) juhu.length());
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
		Normalizer normalizer = new StackNormalizer();
		System.out.println(normalizer.normalize(sb));
	}
}
