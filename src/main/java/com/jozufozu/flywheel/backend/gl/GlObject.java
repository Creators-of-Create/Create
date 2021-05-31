package com.jozufozu.flywheel.backend.gl;

// Utility class for safely dealing with gl object handles.
public abstract class GlObject {
	private static final int INVALID_HANDLE = Integer.MIN_VALUE;

	private int handle = INVALID_HANDLE;

	protected final void setHandle(int handle) {
		this.handle = handle;
	}

	public final int handle() {
		this.checkHandle();

		return this.handle;
	}

	protected final void checkHandle() {
		if (!this.isHandleValid()) {
			throw new IllegalStateException("Handle is not valid");
		}
	}

	protected final boolean isHandleValid() {
		return this.handle != INVALID_HANDLE;
	}

	protected final void invalidateHandle() {
		this.handle = INVALID_HANDLE;
	}

	public void delete() {
		if (!isHandleValid()) {
			throw new IllegalStateException("Handle already deleted.");
		}

		deleteInternal(handle);
		invalidateHandle();
	}

	protected abstract void deleteInternal(int handle);
}
