package com.simibubi.create.foundation.utility.animation;

public interface Force {

    float get(float mass, float value, float speed);

    boolean finished();

    class Drag implements Force {
        final float dragFactor;

        public Drag(float dragFactor) {
            this.dragFactor = dragFactor;
        }

        @Override
        public float get(float mass, float value, float speed) {
            return -speed * dragFactor;
        }

        @Override
        public boolean finished() {
            return false;
        }
    }

    class Zeroing implements Force {
        final float g;

        public Zeroing(float g) {
            this.g = g / 20;
        }

        @Override
        public float get(float mass, float value, float speed) {
            return -Math.signum(value) * g * mass;
        }

        @Override
        public boolean finished() {
            return false;
        }
    }

    class Impulse implements Force {

        float force;

        public Impulse(float force) {
            this.force = force;
        }

        @Override
        public float get(float mass, float value, float speed) {
            return force;
        }

        @Override
        public boolean finished() {
            return true;
        }
    }

    class OverTime implements Force {
        int timeRemaining;
        float f;

        public OverTime(int time, float totalAcceleration) {
            this.timeRemaining = time;
            this.f = totalAcceleration / (float) time;
        }

        @Override
        public float get(float mass, float value, float speed) {
            timeRemaining--;
            return f;
        }

        @Override
        public boolean finished() {
            return timeRemaining <= 0;
        }
    }

    class Static implements Force {
        float force;

        public Static(float force) {
            this.force = force;
        }

        @Override
        public float get(float mass, float value, float speed) {
            return force;
        }

        @Override
        public boolean finished() {
            return false;
        }
    }
}
