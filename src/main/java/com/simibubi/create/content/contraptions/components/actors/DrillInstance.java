package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.render.contraption.RenderedContraption;
import com.simibubi.create.foundation.render.instancing.*;
import com.simibubi.create.foundation.render.instancing.actors.StaticRotatingActorData;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class DrillInstance extends SingleRotatingInstance {
    public static void register(TileEntityType<? extends KineticTileEntity> type) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, DrillInstance::new));    }

    public DrillInstance(InstancedTileRenderer modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    public static void addInstanceForContraption(RenderedContraption contraption, MovementContext context) {
        RenderMaterial<?, InstancedModel<StaticRotatingActorData>> renderMaterial = contraption.getActorMaterial();

        BlockState state = context.state;
        InstancedModel<StaticRotatingActorData> model = renderMaterial.getModel(AllBlockPartials.DRILL_HEAD, state);

        model.setupInstance(data -> {
            Direction facing = state.get(DrillBlock.FACING);
            float eulerX = AngleHelper.verticalAngle(facing) + ((facing.getAxis() == Direction.Axis.Y) ? 180 : 0);
            float eulerY = facing.getHorizontalAngle();
            data.setPosition(context.localPos)
                .setBlockLight(contraption.renderWorld.getLightLevel(LightType.BLOCK, context.localPos))
                .setRotationOffset(0)
                .setRotationAxis(0, 0, 1)
                .setLocalRotation(eulerX, eulerY, 0);
        });
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
        return AllBlockPartials.DRILL_HEAD.renderOnDirectionalSouthRotating(modelManager, tile.getBlockState());
    }
}
