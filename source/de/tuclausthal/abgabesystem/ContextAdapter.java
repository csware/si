package de.tuclausthal.abgabesystem;

import java.io.File;

import javax.servlet.ServletContext;

public class ContextAdapter {
	private ServletContext context;

	public ContextAdapter(ServletContext context) {
		this.context = context;
	}

	public File getDataPath() {
		String datapath = context.getInitParameter("datapath");
		if (datapath == null) {
			throw new RuntimeException("datapath not specified");
		}
		File path = new File(datapath);
		if (path.isFile()) {
			throw new RuntimeException("datapath must not be a file");
		}
		if (path.exists() == false) {
			if (!path.mkdirs()) {
				throw new RuntimeException("could not create datapath");
			}
		}
		return new File(datapath);
	}
}
