package com.simibubi.create.content.itemprocessing.specifics;

public interface ICanProcessInBasin {

	/**
	 * Tries to process inside a basin
	 * @param simulate whether this only simulates the process
	 * @return whether the process was successful or not
	 */
	boolean tryProcessInBasin(boolean simulate);

}
