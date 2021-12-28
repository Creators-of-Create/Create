package com.simibubi.create.content.contraptions.solver;

public enum SolveResult {
	OK, CONTRADICTION;

	public boolean isOk() { return this == OK; }
	public boolean isContradiction() { return this == CONTRADICTION; }
}
