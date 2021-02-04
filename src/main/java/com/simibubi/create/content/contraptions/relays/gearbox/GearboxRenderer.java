package com.simibubi.create.content.contraptions.relays.gearbox;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

import java.util.List;

public class GearboxRenderer extends KineticTileEntityRenderer {

	public GearboxRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
	}

}
