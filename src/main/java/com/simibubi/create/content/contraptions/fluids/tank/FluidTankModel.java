package com.simibubi.create.content.contraptions.fluids.tank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class FluidTankModel extends CTModel {

	protected static ModelProperty<CullData> CULL_PROPERTY = new ModelProperty<>();

	public static FluidTankModel standard(IBakedModel originalModel) {
		return new FluidTankModel(originalModel, AllSpriteShifts.FLUID_TANK, AllSpriteShifts.COPPER_CASING);
	}
	
	public static FluidTankModel creative(IBakedModel originalModel) {
		return new FluidTankModel(originalModel, AllSpriteShifts.CREATIVE_FLUID_TANK, AllSpriteShifts.CREATIVE_CASING);
	}
	
	private FluidTankModel(IBakedModel originalModel, CTSpriteShiftEntry side, CTSpriteShiftEntry top) {
		super(originalModel, new FluidTankCTBehaviour(side, top));
	}

	@Override
	public IModelData getModelData(ILightReader world, BlockPos pos, BlockState state, IModelData tileData) {
		CullData cullData = new CullData();
		for (Direction d : Iterate.horizontalDirections)
			cullData.setCulled(d, FluidTankConnectivityHandler.isConnected(world, pos, pos.offset(d)));
		return getCTDataMapBuilder(world, pos, state).withInitial(CULL_PROPERTY, cullData)
			.build();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
		if (side != null)
			return Collections.emptyList();

		List<BakedQuad> quads = new ArrayList<>();
		for (Direction d : Iterate.directions) {
			if (extraData.hasProperty(CULL_PROPERTY) && extraData.getData(CULL_PROPERTY)
				.isCulled(d))
				continue;
			quads.addAll(super.getQuads(state, d, rand, extraData));
		}
		quads.addAll(super.getQuads(state, null, rand, extraData));
		return quads;
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
			culledFaces[face.getHorizontalIndex()] = cull;
		}

		boolean isCulled(Direction face) {
			if (face.getAxis()
				.isVertical())
				return false;
			return culledFaces[face.getHorizontalIndex()];
		}
	}

}
