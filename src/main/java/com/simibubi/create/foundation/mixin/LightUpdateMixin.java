package com.simibubi.create.foundation.mixin;

import com.simibubi.create.foundation.render.FastRenderDispatcher;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(ClientChunkProvider.class)
public abstract class LightUpdateMixin extends AbstractChunkProvider {

    /**
     * JUSTIFICATION: This method is called after a lighting tick once per subchunk where a
     * lighting change occurred that tick. On the client, Minecraft uses this method to inform
     * the rendering system that it needs to redraw a chunk. It does all that work asynchronously,
     * and we should too.
     */
    @Inject(at = @At("HEAD"), method = "markLightChanged")
    private void onLightUpdate(LightType type, SectionPos pos, CallbackInfo ci) {
        FastRenderDispatcher.notifyLightUpdate(((ClientChunkProvider) (Object) this), type, pos);
    }
}
