package com.simibubi.create.content.contraptions.components.saw;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.instancing.InstancedTileRenderRegistry;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Rotation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class SawInstance extends SingleRotatingInstance {
    public static void register(TileEntityType<? extends KineticTileEntity> type) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, SawInstance::new));
    }

    public SawInstance(InstancedTileRenderer modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
        if (lastState.get(FACING).getAxis().isHorizontal())
            return AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(modelManager, lastState.rotate(tile.getWorld(), tile.getPos(), Rotation.CLOCKWISE_180));
        else
            return rotatingMaterial().getModel(KineticTileEntityRenderer.KINETIC_TILE, shaft(getRotationAxis()));
    }
}
