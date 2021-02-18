package com.simibubi.create.foundation.mixin;

import com.simibubi.create.foundation.render.backend.OptifineHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class ShaderCloseMixin {

    @Shadow @Nullable public Screen currentScreen;

    @Inject(at = @At("HEAD"), method = "displayGuiScreen")
    private void whenScreenChanges(Screen screen, CallbackInfo info) {
        if (OptifineHandler.optifineInstalled() && screen instanceof VideoSettingsScreen) {
            Screen old = this.currentScreen;
            if (old != null && old.getClass().getName().startsWith(OptifineHandler.SHADER_PACKAGE)) {
                OptifineHandler.refresh();
            }
        }
    }
}
