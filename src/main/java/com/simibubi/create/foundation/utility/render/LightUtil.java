package com.simibubi.create.foundation.utility.render;

import net.minecraft.client.renderer.LightTexture;

public class LightUtil {
    public static float getProperBlockLight(int packedLight) {
        return ((LightTexture.getBlockLightCoordinates(packedLight) + 1) / (float) 0xF);
    }

    public static float getProperSkyLight(int packedLight) {
        return ((LightTexture.getSkyLightCoordinates(packedLight) + 1) / (float) 0xF);
    }
}
