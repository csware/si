package de.tuclausthal.abgabesystem.dupecheck;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;

public class JupdiTest {
	public static void main(String[] args) {
		List<File> javaFiles = new LinkedList<File>();
		//javaFiles.add(new File("C:\\abgabesystem\\1\\2\\6\\Kopie von SimpleForLoop.java"));
		javaFiles.add(new File("C:\\abgabesystem\\1\\2\\6\\SimpleForLoop.java"));
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

		// prepare the source file(s) to compile
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(javaFiles);
		CompilationTask ctask = compiler.getTask(null, fileManager, null, null, null, compilationUnits);
		JavacTask jt = (JavacTask) ctask;
		try {
			Iterable<? extends CompilationUnitTree> trees = jt.parse();
			Iterator<? extends CompilationUnitTree> myiterator = trees.iterator();
			while (myiterator.hasNext()) {
				CompilationUnitTree porn = myiterator.next();
				//System.out.println(porn.toString()); // Ausgabe ohne Kommentare
				JCTree.JCCompilationUnit pornCUT = (JCTree.JCCompilationUnit) porn;
				JCTree pordd = (JCTree) porn;
				//System.out.println(porn.getClass().getSuperclass());
				//System.out.println(bla);
			}
			//... do something else here....
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*boolean a = ctask.call();
		try {
			fileManager.close();
		} catch (IOException e) {
		}*/
	}
}
