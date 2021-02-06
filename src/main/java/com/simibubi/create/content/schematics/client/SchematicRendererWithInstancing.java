package com.simibubi.create.content.schematics.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.FastRenderDispatcher;
import com.simibubi.create.foundation.render.contraption.ContraptionKineticRenderer;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLClientLaunchProvider;

public class SchematicRendererWithInstancing extends SchematicRenderer {
    public final ContraptionKineticRenderer tiles;

    public SchematicRendererWithInstancing() {
        this.tiles = new ContraptionKineticRenderer();
    }

    @Override
    protected void redraw(Minecraft minecraft) {
        super.redraw(minecraft);

        tiles.invalidate();

        schematic.getRenderedTileEntities().forEach(tiles::add);
    }

    @Override
    public void render(MatrixStack ms, SuperRenderTypeBuffer buffer) {
        super.render(ms, buffer);

        //tiles.render(RenderType.getCutoutMipped(), FastRenderDispatcher.getProjectionMatrix(), );
    }
}
