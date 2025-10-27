package com.simibubi.create.content.logistics.tableCloth;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelData.Builder;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class TableClothModel extends BakedModelWrapperWithData {

	private static final ModelProperty<CullData> CULL_PROPERTY = new ModelProperty<>();

	private static final Map<TableClothBlock, List<List<BakedQuad>>> CORNERS = new HashMap<>();

	public TableClothModel(BakedModel originalModel) {
		super(originalModel);
	}

	public static void reload() {
		CORNERS.clear();
	}

	@Override
	public boolean useAmbientOcclusion() {
		return false;
	}

	private List<BakedQuad> getCorner(TableClothBlock block, int corner, @NotNull RandomSource rand,
		@Nullable RenderType renderType) {
		if (!CORNERS.containsKey(block)) {
			TextureAtlasSprite targetSprite = getParticleIcon(ModelData.EMPTY);
			List<List<BakedQuad>> list = new ArrayList<>();

			for (PartialModel pm : List.of(AllPartialModels.TABLE_CLOTH_SW, AllPartialModels.TABLE_CLOTH_NW,
				AllPartialModels.TABLE_CLOTH_NE, AllPartialModels.TABLE_CLOTH_SE))
				list.add(getCornerQuads(rand, renderType, targetSprite, pm));

			CORNERS.put(block, list);
		}

		return CORNERS.get(block)
			.get(corner);
	}

	private List<BakedQuad> getCornerQuads(RandomSource rand, RenderType renderType, TextureAtlasSprite targetSprite,
		PartialModel pm) {
		List<BakedQuad> quads = new ArrayList<>();

		for (BakedQuad quad : pm.get()
			.getQuads(null, null, rand, ModelData.EMPTY, renderType)) {
			TextureAtlasSprite original = quad.getSprite();
			BakedQuad newQuad = BakedQuadHelper.clone(quad);
			int[] vertexData = newQuad.getVertices();
			for (int vertex = 0; vertex < 4; vertex++) {
				BakedQuadHelper.setU(vertexData, vertex, targetSprite
					.getU(SpriteShiftEntry.getUnInterpolatedU(original, BakedQuadHelper.getU(vertexData, vertex))));
				BakedQuadHelper.setV(vertexData, vertex, targetSprite
					.getV(SpriteShiftEntry.getUnInterpolatedV(original, BakedQuadHelper.getV(vertexData, vertex))));
			}
			quads.add(newQuad);
		}

		return quads;
	}

	@Override
	protected Builder gatherModelData(Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state,
									  ModelData blockEntityData) {
		List<Direction> culledSides = new ArrayList<>();
		for (Direction side : Iterate.horizontalDirections)
			if (!Block.shouldRenderFace(state, world, pos, side, pos.relative(side)))
				culledSides.add(side);
		if (culledSides.isEmpty())
			return builder;
		return builder.with(CULL_PROPERTY, new CullData(EnumSet.copyOf(culledSides)));
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
											 @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
		@NotNull
		List<BakedQuad> mainQuads = super.getQuads(state, side, rand, extraData, renderType);
		if (side == null || side.getAxis() == Axis.Y)
			return mainQuads;

		CullData cullData = extraData.get(CULL_PROPERTY);
		if (cullData != null && cullData.culled()
			.contains(side.getClockWise()))
			return mainQuads;
		if (state == null || !(state.getBlock() instanceof TableClothBlock dcb))
			return mainQuads;

		List<BakedQuad> copyOf = new ArrayList<>(mainQuads);
		copyOf.addAll(getCorner(dcb, side.get2DDataValue(), rand, renderType));
		return copyOf;
	}

	private static record CullData(EnumSet<Direction> culled) {
	}

}
