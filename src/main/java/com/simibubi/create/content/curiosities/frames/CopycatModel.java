package com.simibubi.create.content.curiosities.frames;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelData.Builder;
import net.minecraftforge.client.model.data.ModelProperty;

public abstract class CopycatModel extends BakedModelWrapperWithData {

	public static final ModelProperty<BlockState> MATERIAL_PROPERTY = new ModelProperty<>();
	private static final ModelProperty<OcclusionData> OCCLUSION_PROPERTY = new ModelProperty<>();
	private static final ModelProperty<ModelData> WRAPPED_DATA_PROPERTY = new ModelProperty<>();

	public CopycatModel(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	protected Builder gatherModelData(Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state,
		ModelData blockEntityData) {
		BlockState material = getMaterial(blockEntityData);
		if (material == null)
			return builder;

		builder.with(MATERIAL_PROPERTY, material);

		if (!(state.getBlock() instanceof CopycatBlock copycatBlock))
			return builder;

		OcclusionData occlusionData = new OcclusionData();
		gatherOcclusionData(world, pos, state, material, occlusionData, copycatBlock);
		builder.with(OCCLUSION_PROPERTY, occlusionData);

		ModelData wrappedData = getModelOf(material).getModelData(
			new FilteredBlockAndTintGetter(world,
				targetPos -> copycatBlock.canConnectTexturesToward(world, pos, targetPos, state)),
			pos, material, ModelData.EMPTY);
		return builder.with(WRAPPED_DATA_PROPERTY, wrappedData);
	}

	private void gatherOcclusionData(BlockAndTintGetter world, BlockPos pos, BlockState state, BlockState material,
		OcclusionData occlusionData, CopycatBlock copycatBlock) {
		MutableBlockPos mutablePos = new MutableBlockPos();
		for (Direction face : Iterate.directions) {

			// Rubidium: Run an additional IForgeBlock.hidesNeighborFace check because it
			// seems to be missing in Block.shouldRenderFace
			MutableBlockPos neighbourPos = mutablePos.setWithOffset(pos, face);
			BlockState neighbourState = world.getBlockState(neighbourPos);
			if (state.supportsExternalFaceHiding()
				&& neighbourState.hidesNeighborFace(world, neighbourPos, state, face.getOpposite())) {
				occlusionData.occlude(face);
				continue;
			}

			if (!copycatBlock.canFaceBeOccluded(state, face))
				continue;
			if (!Block.shouldRenderFace(material, world, pos, face, neighbourPos))
				occlusionData.occlude(face);
		}
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType renderType) {

		// Rubidium: see below
		if (side != null && state.getBlock() instanceof CopycatBlock ccb && ccb.shouldFaceAlwaysRender(state, side))
			return Collections.emptyList();

		BlockState material = getMaterial(data);

		if (material == null)
			return super.getQuads(state, side, rand, data, renderType);

		OcclusionData occlusionData = data.get(OCCLUSION_PROPERTY);
		if (occlusionData != null && occlusionData.isOccluded(side))
			return super.getQuads(state, side, rand, data, renderType);

		ModelData wrappedData = data.get(WRAPPED_DATA_PROPERTY);
		if (wrappedData == null)
			wrappedData = ModelData.EMPTY;
		if (renderType != null && !Minecraft.getInstance()
			.getBlockRenderer()
			.getBlockModel(material)
			.getRenderTypes(material, rand, wrappedData)
			.contains(renderType))
			return super.getQuads(state, side, rand, data, renderType);

		List<BakedQuad> croppedQuads = getCroppedQuads(state, side, rand, material, wrappedData, renderType);

		// Rubidium: render side!=null versions of the base material during side==null,
		// to avoid getting culled away
		if (side == null && state.getBlock() instanceof CopycatBlock ccb)
			for (Direction nonOcclusionSide : Iterate.directions)
				if (ccb.shouldFaceAlwaysRender(state, nonOcclusionSide))
					croppedQuads.addAll(getCroppedQuads(state, nonOcclusionSide, rand, material, wrappedData, renderType));

		return croppedQuads;
	}

	protected abstract List<BakedQuad> getCroppedQuads(BlockState state, Direction side, RandomSource rand,
		BlockState material, ModelData wrappedData, RenderType renderType);

	@Override
	public TextureAtlasSprite getParticleIcon(ModelData data) {
		BlockState material = getMaterial(data);

		if (material == null)
			return super.getParticleIcon(data);

		ModelData wrappedData = data.get(WRAPPED_DATA_PROPERTY);
		if (wrappedData == null)
			wrappedData = ModelData.EMPTY;

		return getModelOf(material).getParticleIcon(wrappedData);
	}

	@Nullable
	public static BlockState getMaterial(ModelData data) {
		BlockState material = data.get(MATERIAL_PROPERTY);
		return material == null ? AllBlocks.COPYCAT_BASE.getDefaultState() : material;
	}

	public static BakedModel getModelOf(BlockState state) {
		return Minecraft.getInstance()
			.getBlockRenderer()
			.getBlockModel(state);
	}

	private static class OcclusionData {
		private final boolean[] occluded;

		public OcclusionData() {
			occluded = new boolean[6];
		}

		public void occlude(Direction face) {
			occluded[face.get3DDataValue()] = true;
		}

		public boolean isOccluded(Direction face) {
			return face == null ? false : occluded[face.get3DDataValue()];
		}
	}

}
