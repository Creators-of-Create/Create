package com.simibubi.create.content.contraptions.solver;

public class KineticNodeState {
	private KineticConnections connections;
	private float generatedSpeed;

	public KineticNodeState(KineticConnections connections, float generatedSpeed) {
		this.connections = connections;
		this.generatedSpeed = generatedSpeed;
	}

	public KineticConnections getConnections() {
		return connections;
	}

	public float getGeneratedSpeed() {
		return generatedSpeed;
	}

	public void setConnections(KineticConnections connections) {
		this.connections = connections;
	}

	public void setGeneratedSpeed(float generatedSpeed) {
		this.generatedSpeed = generatedSpeed;
	}
}
