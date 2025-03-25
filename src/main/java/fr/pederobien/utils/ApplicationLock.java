package fr.pederobien.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;

public class ApplicationLock {
	private String name;
	private Path path;
	private File lock;
	private FileChannel fileChannel;
	private FileLock fileLock;

	/**
	 * Creates an application lock associated to the given name. When the
	 * application tries to lock an instance, a file is created using the given name
	 * at the specified path. A ShutDownHook is register in order to delete the file
	 * in response of the Java virtual machine shut down.
	 * 
	 * <p>
	 * The Java virtual machine <i>shuts down</i> in response to two kinds of
	 * events:
	 *
	 * <ul>
	 *
	 * <li>The program <i>exits</i> normally, when the last non-daemon thread exits
	 * or when the <tt>{@link #exit exit}</tt> (equivalently,
	 * {@link System#exit(int) System.exit}) method is invoked, or
	 *
	 * <li>The virtual machine is <i>terminated</i> in response to a user interrupt,
	 * such as typing <tt>^C</tt>, or a system-wide event, such as user logoff or
	 * system shutdown.
	 *
	 * </ul>
	 * 
	 * @param name The application name.
	 * @param path The path to the underlying file.
	 */
	public ApplicationLock(String name, Path path) {
		this.name = name;
		this.path = path;
	}

	/**
	 * Lock an instance of the application. This method creates an underlying file
	 * locked until the lock is released.
	 * 
	 * @return True if a lock has been acquired, false otherwise.
	 */
	@SuppressWarnings("resource")
	public boolean lock() {
		lock = path.resolve(name).toFile();

		try {
			if (lock.exists() && !lock.delete() || !lock.createNewFile())
				return false;

			fileChannel = new RandomAccessFile(lock, "rw").getChannel();
			fileLock = fileChannel.tryLock();

			if (fileLock == null) {
				fileChannel.close();
				return false;
			}

			Runtime.getRuntime().addShutdownHook(new Thread(() -> unlock()));
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Unlock this application lock and delete the underlying file.
	 */
	public void unlock() {
		try {
			if (fileLock != null)
				fileLock.release();
			if (fileChannel != null)
				fileChannel.close();
			if (lock != null)
				lock.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
