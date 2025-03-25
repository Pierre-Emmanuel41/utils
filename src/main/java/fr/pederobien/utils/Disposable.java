package fr.pederobien.utils;

import java.util.concurrent.atomic.AtomicBoolean;

public class Disposable implements IDisposable {
	private AtomicBoolean isDisposed;

	public Disposable() {
		isDisposed = new AtomicBoolean(false);
	}

	@Override
	public boolean dispose() {
		return isDisposed.compareAndSet(false, true);
	}

	@Override
	public boolean isDisposed() {
		return isDisposed.get();
	}

	@Override
	public void checkDisposed() {
		if (isDisposed())
			throw new IllegalStateException("Object disposed");
	}

}
