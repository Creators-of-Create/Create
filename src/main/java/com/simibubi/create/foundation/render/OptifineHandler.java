package com.simibubi.create.foundation.render;

import com.simibubi.create.foundation.render.gl.backend.Backend;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.Optional;

public class OptifineHandler {
    private static Package optifine;
    private static OptifineHandler handler;

    public final boolean usingShaders;

    public OptifineHandler(boolean usingShaders) {
        this.usingShaders = usingShaders;
    }

    public static Optional<OptifineHandler> get() {
        return Optional.ofNullable(handler);
    }

    public static void init() {
        optifine = Package.getPackage("net.optifine");

        if (optifine == null) {
            Backend.log.info("Optifine not detected.");
        } else {
            Backend.log.info("Optifine detected.");

            refresh();
        }
    }

    public static void refresh() {
        if (optifine == null) return;

        File dir = Minecraft.getInstance().gameDir;

        File shaderOptions = new File(dir, "optionsshaders.txt");

        boolean shadersOff = true;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(shaderOptions));

            shadersOff = reader.lines().anyMatch(it -> it.replaceAll("\\s", "").equals("shaderPack=OFF"));
        } catch (FileNotFoundException e) {
            Backend.log.info("No shader config found.");
        }

        handler = new OptifineHandler(!shadersOff);
    }

    public boolean isUsingShaders() {
        return usingShaders;
    }
}
