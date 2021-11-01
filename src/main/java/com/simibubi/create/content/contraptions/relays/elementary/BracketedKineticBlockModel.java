package com.simibubi.create.content.contraptions.relays.elementary;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jozufozu.flywheel.util.VirtualEmptyModelData;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class BracketedKineticBlockModel extends BakedModelWrapper<BakedModel> {

	private static final ModelProperty<BracketedModelData> BRACKET_PROPERTY = new ModelProperty<>();

	public BracketedKineticBlockModel(BakedModel template) {
		super(template);
	}

	@Override
	public IModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, IModelData tileData) {
		if (tileData == VirtualEmptyModelData.INSTANCE)
			return tileData;
		BracketedModelData data = new BracketedModelData();
		BracketedTileEntityBehaviour attachmentBehaviour =
			TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);
		if (attachmentBehaviour != null)
			data.putBracket(attachmentBehaviour.getBracket());
		return new ModelDataMap.Builder().withInitial(BRACKET_PROPERTY, data)
			.build();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData data) {
		if (data instanceof ModelDataMap) {
			List<BakedQuad> quads = new ArrayList<>();
			ModelDataMap modelDataMap = (ModelDataMap) data;
			if (modelDataMap.hasProperty(BRACKET_PROPERTY)) {
				quads = new ArrayList<>(quads);
				addQuads(quads, state, side, rand, modelDataMap, modelDataMap.getData(BRACKET_PROPERTY));
			}
			return quads;
		}
		return super.getQuads(state, side, rand, data);
	}

	private void addQuads(List<BakedQuad> quads, BlockState state, Direction side, Random rand, IModelData data,
		BracketedModelData pipeData) {
		BakedModel bracket = pipeData.getBracket();
		if (bracket == null)
			return;
		quads.addAll(bracket.getQuads(state, side, rand, data));
	}

	private class BracketedModelData {
		BakedModel bracket;

		public void putBracket(BlockState state) {
			this.bracket = Minecraft.getInstance()
				.getBlockRenderer()
				.getBlockModel(state);
		}

		public BakedModel getBracket() {
			return bracket;
		}

	}

}
