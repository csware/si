package de.tuclausthal.abgabesystem;

public class CheckThread extends Thread {
	private Process process;

	public CheckThread(Process process) {
		this.process = process;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			interrupt();
		}
		if (!interrupted()) {
			process.destroy();
		}
	}
}
