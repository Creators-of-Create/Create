package com.simibubi.create.content.contraptions.fluids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour.AttachmentTypes;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.create.foundation.block.connected.BakedModelWrapperWithData;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;
import net.minecraftforge.client.model.data.ModelProperty;

public class PipeAttachmentModel extends BakedModelWrapperWithData {

	private static ModelProperty<PipeModelData> PIPE_PROPERTY = new ModelProperty<>();

	public PipeAttachmentModel(IBakedModel template) {
		super(template);
	}

	@Override
	protected Builder gatherModelData(Builder builder, IBlockDisplayReader world, BlockPos pos, BlockState state) {
		PipeModelData data = new PipeModelData();
		FluidTransportBehaviour transport = TileEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
		BracketedTileEntityBehaviour bracket = TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);

		if (transport != null)
			for (Direction d : Iterate.directions)
				data.putRim(d, transport.getRenderedRimAttachment(world, pos, state, d));
		if (bracket != null)
			data.putBracket(bracket.getBracket());

		data.setEncased(FluidPipeBlock.shouldDrawCasing(world, pos, state));
		return builder.withInitial(PIPE_PROPERTY, data);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData data) {
		List<BakedQuad> quads = super.getQuads(state, side, rand, data);
		if (data instanceof ModelDataMap) {
			ModelDataMap modelDataMap = (ModelDataMap) data;
			if (modelDataMap.hasProperty(PIPE_PROPERTY)) {
				quads = new ArrayList<>(quads);
				addQuads(quads, state, side, rand, modelDataMap, modelDataMap.getData(PIPE_PROPERTY));
			}
		}
		return quads;
	}

	private void addQuads(List<BakedQuad> quads, BlockState state, Direction side, Random rand, IModelData data,
		PipeModelData pipeData) {
		for (Direction d : Iterate.directions)
			if (pipeData.hasRim(d))
				quads.addAll(AllBlockPartials.PIPE_ATTACHMENTS.get(pipeData.getRim(d))
					.get(d)
					.get()
					.getQuads(state, side, rand, data));
		if (pipeData.isEncased())
			quads.addAll(AllBlockPartials.FLUID_PIPE_CASING.get()
				.getQuads(state, side, rand, data));
		IBakedModel bracket = pipeData.getBracket();
		if (bracket != null)
			quads.addAll(bracket.getQuads(state, side, rand, data));
	}

	private class PipeModelData {
		AttachmentTypes[] rims;
		boolean encased;
		IBakedModel bracket;

		public PipeModelData() {
			rims = new AttachmentTypes[6];
			Arrays.fill(rims, AttachmentTypes.NONE);
		}

		public void putBracket(BlockState state) {
			this.bracket = Minecraft.getInstance()
				.getBlockRenderer()
				.getBlockModel(state);
		}

		public IBakedModel getBracket() {
			return bracket;
		}

		public void putRim(Direction face, AttachmentTypes rim) {
			rims[face.get3DDataValue()] = rim;
		}

		public void setEncased(boolean encased) {
			this.encased = encased;
		}

		public boolean hasRim(Direction face) {
			return rims[face.get3DDataValue()] != AttachmentTypes.NONE;
		}

		public AttachmentTypes getRim(Direction face) {
			return rims[face.get3DDataValue()];
		}

		public boolean isEncased() {
			return encased;
		}
	}

}
