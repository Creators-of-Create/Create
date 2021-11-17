package com.simibubi.create.content.contraptions.fluids.tank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.utility.Iterate;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class FluidTankModel extends CTModel {

	public static FluidTankModel standard(BakedModel originalModel) {
		return new FluidTankModel(originalModel, AllSpriteShifts.FLUID_TANK, AllSpriteShifts.COPPER_CASING);
	}

	public static FluidTankModel creative(BakedModel originalModel) {
		return new FluidTankModel(originalModel, AllSpriteShifts.CREATIVE_FLUID_TANK, AllSpriteShifts.CREATIVE_CASING);
	}

	private FluidTankModel(BakedModel originalModel, CTSpriteShiftEntry side, CTSpriteShiftEntry top) {
		super(originalModel, new FluidTankCTBehaviour(side, top));
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		CullData cullData = new CullData();
		for (Direction d : Iterate.horizontalDirections)
			cullData.setCulled(d, FluidTankConnectivityHandler.isConnected(blockView, pos, pos.relative(d)));

		context.pushTransform(quad -> {
			Direction cullFace = quad.cullFace();
			if (cullFace != null && cullData.isCulled(cullFace)) {
				return false;
			}
			quad.cullFace(null);
			return true;
		});
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();
	}

	private class CullData {
		boolean[] culledFaces;

		public CullData() {
			culledFaces = new boolean[4];
			Arrays.fill(culledFaces, false);
		}

		void setCulled(Direction face, boolean cull) {
			if (face.getAxis()
				.isVertical())
				return;
			culledFaces[face.get2DDataValue()] = cull;
		}

		boolean isCulled(Direction face) {
			if (face.getAxis()
				.isVertical())
				return false;
			return culledFaces[face.get2DDataValue()];
		}
	}

}
