package com.simibubi.create.content.kinetics.simpleRelays;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.jozufozu.flywheel.core.virtual.VirtualEmptyModelData;
import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
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
	public IModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, IModelData blockEntityData) {
		if (VirtualEmptyModelData.is(blockEntityData))
			return blockEntityData;
		BracketedModelData data = new BracketedModelData();
		BracketedBlockEntityBehaviour attachmentBehaviour =
			BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);
		if (attachmentBehaviour != null)
			data.putBracket(attachmentBehaviour.getBracket());
		return new ModelDataMap.Builder().withInitial(BRACKET_PROPERTY, data)
			.build();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData data) {
		if (!VirtualEmptyModelData.is(data)) {
			if (data.hasProperty(BRACKET_PROPERTY)) {
				BracketedModelData pipeData = data.getData(BRACKET_PROPERTY);
				BakedModel bracket = pipeData.getBracket();
				if (bracket != null)
					return bracket.getQuads(state, side, rand, data);
			}
			return Collections.emptyList();
		}
		return super.getQuads(state, side, rand, data);
	}

	private static class BracketedModelData {
		private BakedModel bracket;

		public void putBracket(BlockState state) {
			if (state != null) {
				this.bracket = Minecraft.getInstance()
					.getBlockRenderer()
					.getBlockModel(state);
			}
		}

		public BakedModel getBracket() {
			return bracket;
		}
	}

}
