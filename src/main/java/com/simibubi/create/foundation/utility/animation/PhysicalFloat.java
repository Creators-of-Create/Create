package com.simibubi.create.foundation.utility.animation;

import java.util.ArrayList;

import net.minecraft.util.Mth;

public class PhysicalFloat {

    float previousValue;
    float value;

    float previousSpeed;
    float speed;
    float limit = Float.NaN;

    float mass;

    private final ArrayList<Force> forces = new ArrayList<>();

    public static PhysicalFloat create() {
        return new PhysicalFloat(1);
    }

    public static PhysicalFloat create(float mass) {
        return new PhysicalFloat(mass);
    }

    public PhysicalFloat(float mass) {
        this.mass = mass;
    }

    public PhysicalFloat startAt(double value) {
        previousValue = this.value = (float) value;
        return this;
    }

    public PhysicalFloat withDrag(double drag) {
        return addForce(new Force.Drag((float) drag));
    }

    public PhysicalFloat zeroing(double g) {
        return addForce(new Force.Zeroing((float) g));
    }

    public PhysicalFloat withLimit(float limit) {
    	this.limit = limit;
    	return this;
    }

    public void tick() {
        previousSpeed = speed;
        previousValue = value;

        float totalImpulse = 0;
        for (Force force : forces)
            totalImpulse += force.get(mass, value, speed) / mass;

        speed += totalImpulse;

        forces.removeIf(Force::finished);

        if (Float.isFinite(limit)) {
        	speed = Mth.clamp(speed, -limit, limit);
        }

        value += speed;
    }

    public PhysicalFloat addForce(Force f) {
        forces.add(f);
        return this;
    }

    public PhysicalFloat bump(double force) {
        return addForce(new Force.Impulse((float) force));
    }

    public PhysicalFloat bump(int time, double force) {
        return addForce(new Force.OverTime(time, (float) force));
    }

    public float getValue() {
        return getValue(1);
    }

    public float getValue(float partialTicks) {
        return Mth.lerp(partialTicks, previousValue, value);
    }

}
