package fr.pederobien.utils;

public interface IPausable {

	/**
	 * Starts the execution.
	 */
	void start();

	/**
	 * Stops the execution.
	 */
	void stop();

	/**
	 * Pauses the execution.
	 */
	void pause();

	/**
	 * Resumes the execution
	 */
	void resume();
}
