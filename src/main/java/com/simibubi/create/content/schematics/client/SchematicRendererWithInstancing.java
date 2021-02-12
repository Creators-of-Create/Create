package com.simibubi.create.content.schematics.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionKineticRenderer;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;

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
