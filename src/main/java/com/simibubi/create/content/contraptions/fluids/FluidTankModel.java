package com.simibubi.create.content.contraptions.fluids;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class FluidTankModel extends CTModel {

	private static ModelProperty<TankModelData> TANK_PROPERTY = new ModelProperty<>();
	private static ConnectedTextureBehaviour ctBehaviour =
		new FluidTankCTBehaviour(AllSpriteShifts.FLUID_TANK, AllSpriteShifts.COPPER_CASING);

	public FluidTankModel(IBakedModel model) {
		super(model, ctBehaviour);
	}

	@Override
	public IModelData getModelData(ILightReader world, BlockPos pos, BlockState state, IModelData tileData) {
		TankModelData data = new TankModelData();
		for (boolean top : Iterate.trueAndFalse)
			for (Direction d : Iterate.horizontalDirections)
				data.setCapFiller(d, top, FluidTankBlock.shouldDrawCapFiller(world, pos, state, d, top));
		for (boolean north : Iterate.trueAndFalse)
			for (boolean east : Iterate.trueAndFalse)
				data.setDiagonalFiller(north, east,
					FluidTankBlock.shouldDrawDiagonalFiller(world, pos, state, north, east));
		return new ModelDataMap.Builder().withInitial(CT_PROPERTY, createCTData(world, pos, state))
			.withInitial(TANK_PROPERTY, data)
			.build();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData data) {
		List<BakedQuad> quads = super.getQuads(state, side, rand, data);
		if (data instanceof ModelDataMap) {
			ModelDataMap modelDataMap = (ModelDataMap) data;
			if (modelDataMap.hasProperty(TANK_PROPERTY))
				addQuads(quads, state, side, rand, modelDataMap, modelDataMap.getData(TANK_PROPERTY));
		}
		return quads;
	}

	private void addQuads(List<BakedQuad> quads, BlockState state, Direction side, Random rand, IModelData data,
		TankModelData pipeData) {
		for (boolean top : Iterate.trueAndFalse)
			for (Direction d : Iterate.horizontalDirections)
				if (pipeData.getCapFiller(d, top))
					quads.addAll(AllBlockPartials.TANK_LID_FILLERS.get(Pair.of(top, d))
						.get()
						.getQuads(state, side, rand, data));
		for (boolean north : Iterate.trueAndFalse)
			for (boolean east : Iterate.trueAndFalse)
				if (pipeData.getDiagonalFiller(north, east))
					quads.addAll(AllBlockPartials.TANK_DIAGONAL_FILLERS.get(Pair.of(north, east))
						.get()
						.getQuads(state, side, rand, data));
	}

	private class TankModelData {
		boolean[] capFillers;
		boolean[] diagonalFillers;

		public TankModelData() {
			capFillers = new boolean[2 * 4];
			diagonalFillers = new boolean[2 * 2];
			Arrays.fill(capFillers, false);
			Arrays.fill(diagonalFillers, false);
		}

		public void setCapFiller(Direction face, boolean top, boolean filler) {
			capFillers[(top ? 0 : 4) + face.getHorizontalIndex()] = filler;
		}

		public void setDiagonalFiller(boolean north, boolean east, boolean filler) {
			diagonalFillers[(north ? 0 : 2) + (east ? 0 : 1)] = filler;
		}

		public boolean getCapFiller(Direction face, boolean top) {
			return capFillers[(top ? 0 : 4) + face.getHorizontalIndex()];
		}

		public boolean getDiagonalFiller(boolean north, boolean east) {
			return diagonalFillers[(north ? 0 : 2) + (east ? 0 : 1)];
		}
	}

}
