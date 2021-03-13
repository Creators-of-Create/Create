package com.simibubi.create.foundation.utility;

import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

public class PhysicalFloat {

    float previousValue;
    float value;

    float speed;

    private final ArrayList<Force> forces = new ArrayList<>();

    public static PhysicalFloat create() {
        return new PhysicalFloat();
    }

    public PhysicalFloat startAt(double value) {
        previousValue = this.value = (float) value;
        return this;
    }

    public PhysicalFloat withDrag(double drag) {
        return addForce(new DragForce((float) drag));
    }

    public PhysicalFloat zeroing(double g) {
        return addForce(new ZeroingForce((float) g));
    }

    public void tick() {
        previousValue = value;

        for (Force force : forces)
            speed = force.apply(speed, value);

        forces.removeIf(Force::finished);

        value += speed;
    }

    public PhysicalFloat addForce(Force f) {
        forces.add(f);
        return this;
    }

    public PhysicalFloat bump(double force) {
        return addForce(new Impulse((float) force));
    }

    public PhysicalFloat bump(int time, double force) {
        return addForce(new ForceOverTime(time, (float) force));
    }

    public float getValue() {
        return getValue(1);
    }

    public float getValue(float partialTicks) {
        return MathHelper.lerp(partialTicks, previousValue, value);
    }

    public interface Force {

        float apply(float speed, float value);

        boolean finished();
    }

    public static class DragForce implements Force {
        final float dragFactor;

        public DragForce(float dragFactor) {
            this.dragFactor = dragFactor;
        }

        @Override
        public float apply(float speed, float value) {
            return speed * dragFactor;
        }

        @Override
        public boolean finished() {
            return false;
        }
    }

    public static class ZeroingForce implements Force {
        final float g;

        public ZeroingForce(float g) {
            this.g = g;
        }

        @Override
        public float apply(float speed, float value) {
            return speed - MathHelper.clamp(g * Math.signum(value), -speed, speed);
        }

        @Override
        public boolean finished() {
            return false;
        }
    }

    public static class Impulse implements Force {

        float force;

        public Impulse(float force) {
            this.force = force;
        }

        @Override
        public float apply(float speed, float value) {
            return speed + force;
        }

        @Override
        public boolean finished() {
            return true;
        }
    }

    public static class ForceOverTime implements Force {
        int timeRemaining;
        float accel;

        public ForceOverTime(int time, float totalAcceleration) {
            this.timeRemaining = time;
            this.accel = totalAcceleration / (float) time;
        }

        @Override
        public float apply(float speed, float value) {
            timeRemaining--;
            return speed + accel;
        }

        @Override
        public boolean finished() {
            return timeRemaining <= 0;
        }
    }
}
