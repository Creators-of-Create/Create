package com.simibubi.create.content.contraptions.base;

import net.minecraft.util.text.TextFormatting;

public interface IHeat {

    /**
     * Get Text Color based on Heat Amount,
     * Over Half: {@link TextFormatting} DARK_RED
     * Under Half: {@link TextFormatting} RED
     * ~Jayson.json
     */
    default TextFormatting getHeatTextColor() {
        return getHeat() > (getMaxHeat() / 2) ? TextFormatting.DARK_RED : TextFormatting.RED;
    }

    /**
     * Checks if given Object has a heat value greater than 0
     * ~Jayson.json
     */
    default boolean hasHeat() {
        return getHeat() > 0;
    }

    /**
     * If the given Object should even be heatable
     * ~Jayson.json
     */
    boolean heatAble();

    /**
     * Returns the Heat value as a float (°C)
     * ~Jayson.json
     */
    float getHeat();

    /**
     * Max heat of Object, before it breaks ~Jayson.json
     */
    float getMaxHeat();

    /**
     * Amount of how much Heat the Object generates,
     * a Furnace might produce more than a Dirt Block as example
     * ~Jayson.json
     */
    float heatGeneration();
}
