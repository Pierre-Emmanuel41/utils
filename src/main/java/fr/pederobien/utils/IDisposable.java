package fr.pederobien.utils;

public interface IDisposable {

	/**
	 * Dispose this object, it cannot be used anymore and will throw an
	 * IllegalStateException.
	 * 
	 * @return True if this was object was not disposed and is now disposed.
	 */
	boolean dispose();

	/**
	 * @return True if this object has been disposed.
	 */
	boolean isDisposed();

	/**
	 * If this object has been disposed then an IllegalStateException is thrown,
	 * otherwise do nothing.
	 */
	void checkDisposed();
}
