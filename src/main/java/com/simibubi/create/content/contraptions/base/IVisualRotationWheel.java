package com.simibubi.create.content.contraptions.base;

public interface IVisualRotationWheel {
	void setForcedSpeed(float speed);
	void unsetForcedSpeed();
	default void setAngle(float angle) {}
	default float getAngle() {return 0f;}
	float getWheelRadius();
}
