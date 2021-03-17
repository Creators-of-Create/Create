package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.RenderedContraption;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class DrillInstance extends SingleRotatingInstance {

    public DrillInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
        return AllBlockPartials.DRILL_HEAD.renderOnDirectionalSouthRotating(modelManager, tile.getBlockState());
    }
}
