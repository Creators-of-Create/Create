package com.simibubi.create.foundation.mixin;

import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.backend.light.ILightListener;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

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
        ClientChunkProvider thi = ((ClientChunkProvider) (Object) this);

        Chunk chunk = thi.getChunk(pos.getSectionX(), pos.getSectionZ(), false);

        int sectionY = pos.getSectionY();

        if (chunk != null) {
            chunk.getTileEntityMap()
                 .entrySet()
                 .stream()
                 .filter(entry -> SectionPos.toChunk(entry.getKey().getY()) == sectionY)
                 .map(Map.Entry::getValue)
                 .filter(tile -> tile instanceof ILightListener)
                 .map(tile -> (ILightListener) tile)
                 .forEach(ILightListener::onChunkLightUpdate);
        }

        ContraptionRenderDispatcher.notifyLightUpdate((ILightReader) thi.getWorld(), type, pos);
    }
}
