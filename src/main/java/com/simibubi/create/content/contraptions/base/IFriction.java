package com.simibubi.create.content.contraptions.base;

public interface IFriction {

    /**
     * Get Friction of Object ~Jayson.json
     */
    float getFriction();

    /**
     * Amount of how much Friction the Object generates,
     * a Mixer might Produce more than a normal Gear, as example
     * ~Jayson.json
     */
    float frictionGeneration();
    void setFriction(float friction);

    /**
     * Checks if Object has Friction, moving Objects should have Friction if they have a Neighbour ~Jayson.json
     */
    default boolean hasFriction() {
        return getFriction() > 0;
    }

    boolean frictionAble();

    float getLubricantAmount();
    void setLubricantAmount(float lubricant);

    default void increaseLubricantAmount(float increament) {
        setLubricantAmount(getLubricantAmount() + increament);
    }

    default void decreaseLubricantAmount(float decreament) {
        increaseLubricantAmount(decreament / -1);
    }
}
