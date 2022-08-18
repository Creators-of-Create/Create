package com.simibubi.create.content.contraptions.fluids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour.AttachmentTypes;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour.AttachmentTypes.ComponentPartials;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.create.foundation.block.connected.BakedModelWrapperWithData;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;
import net.minecraftforge.client.model.data.ModelProperty;

public class PipeAttachmentModel extends BakedModelWrapperWithData {

	private static final ModelProperty<PipeModelData> PIPE_PROPERTY = new ModelProperty<>();

	public PipeAttachmentModel(BakedModel template) {
		super(template);
	}

	@Override
	protected Builder gatherModelData(Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state) {
		PipeModelData data = new PipeModelData();
		FluidTransportBehaviour transport = TileEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
		BracketedTileEntityBehaviour bracket = TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);

		if (transport != null)
			for (Direction d : Iterate.directions)
				data.putAttachment(d, transport.getRenderedRimAttachment(world, pos, state, d));
		if (bracket != null)
			data.putBracket(bracket.getBracket());

		data.setEncased(FluidPipeBlock.shouldDrawCasing(world, pos, state));
		return builder.withInitial(PIPE_PROPERTY, data);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData data) {
		List<BakedQuad> quads = super.getQuads(state, side, rand, data);
		if (data.hasProperty(PIPE_PROPERTY)) {
			PipeModelData pipeData = data.getData(PIPE_PROPERTY);
			quads = new ArrayList<>(quads);
			addQuads(quads, state, side, rand, data, pipeData);
		}
		return quads;
	}

	private void addQuads(List<BakedQuad> quads, BlockState state, Direction side, Random rand, IModelData data,
		PipeModelData pipeData) {
		BakedModel bracket = pipeData.getBracket();
		if (bracket != null)
			quads.addAll(bracket.getQuads(state, side, rand, data));
		for (Direction d : Iterate.directions) {
			AttachmentTypes type = pipeData.getAttachment(d);
			for (ComponentPartials partial : type.partials) {
				quads.addAll(AllBlockPartials.PIPE_ATTACHMENTS.get(partial)
					.get(d)
					.get()
					.getQuads(state, side, rand, data));
			}
		}
		if (pipeData.isEncased())
			quads.addAll(AllBlockPartials.FLUID_PIPE_CASING.get()
				.getQuads(state, side, rand, data));
	}

	private static class PipeModelData {
		private AttachmentTypes[] attachments;
		private boolean encased;
		private BakedModel bracket;

		public PipeModelData() {
			attachments = new AttachmentTypes[6];
			Arrays.fill(attachments, AttachmentTypes.NONE);
		}

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

		public void putAttachment(Direction face, AttachmentTypes rim) {
			attachments[face.get3DDataValue()] = rim;
		}

		public AttachmentTypes getAttachment(Direction face) {
			return attachments[face.get3DDataValue()];
		}

		public void setEncased(boolean encased) {
			this.encased = encased;
		}

		public boolean isEncased() {
			return encased;
		}
	}

}
