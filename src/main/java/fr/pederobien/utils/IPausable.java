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

	/**
	 * @return The state of this pausable object.
	 */
	PausableState getState();

	public enum PausableState {
		/**
		 * When the object is instantiated but not started or has been stopped.
		 */
		NOT_STARTED,

		/**
		 * When the object is started, but neither paused nor stopped.
		 */
		STARTED,

		/**
		 * When the object is paused but not resumed.
		 */
		PAUSED
	}
}
