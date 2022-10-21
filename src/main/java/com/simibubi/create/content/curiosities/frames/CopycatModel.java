package com.simibubi.create.content.curiosities.frames;

import static com.simibubi.create.content.curiosities.frames.CopycatTileEntity.MATERIAL_PROPERTY;
import static com.simibubi.create.foundation.block.render.SpriteShiftEntry.getUnInterpolatedU;
import static com.simibubi.create.foundation.block.render.SpriteShiftEntry.getUnInterpolatedV;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.connected.BakedModelWrapperWithData;
import com.simibubi.create.foundation.block.render.QuadHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;
import net.minecraftforge.client.model.data.ModelProperty;

public abstract class CopycatModel extends BakedModelWrapperWithData {

	private static final ModelProperty<IModelData> WRAPPED_DATA_PROPERTY = new ModelProperty<>();
	private static final ModelProperty<OcclusionData> OCCLUSION_PROPERTY = new ModelProperty<>();

	public CopycatModel(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	protected Builder gatherModelData(Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state,
		IModelData tileData) {
		BlockState wrappedState = getMaterial(world, pos, state);

		if (wrappedState == null)
			return builder;
		if (tileData instanceof ModelDataMap mdm && mdm.hasProperty(MATERIAL_PROPERTY))
			builder.withInitial(MATERIAL_PROPERTY, mdm.getData(MATERIAL_PROPERTY));

		OcclusionData occlusionData = new OcclusionData();
		if (state.getBlock()instanceof CopycatBlock ufb) {
			MutableBlockPos mutablePos = new MutableBlockPos();
			for (Direction face : Iterate.directions)
				if (ufb.canFaceBeOccluded(state, face))
					if (!Block.shouldRenderFace(wrappedState, world, pos, face, mutablePos.setWithOffset(pos, face)))
						occlusionData.occlude(face);
			builder.withInitial(OCCLUSION_PROPERTY, occlusionData);
		}

		IModelData modelData = getModelOf(wrappedState).getModelData(world, pos, wrappedState, EmptyModelData.INSTANCE);
		return builder.withInitial(WRAPPED_DATA_PROPERTY, modelData);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
		List<BakedQuad> quads = super.getQuads(state, side, rand, extraData);

		BlockState material = getMaterial(extraData);
		IModelData wrappedData = extraData.getData(WRAPPED_DATA_PROPERTY);

		if (material == null)
			return quads;
		
		RenderType renderType = MinecraftForgeClient.getRenderType();
		if (renderType != null && !ItemBlockRenderTypes.canRenderInLayer(material, renderType))
			return quads;
		if (wrappedData == null)
			wrappedData = EmptyModelData.INSTANCE;

		OcclusionData occlusionData = extraData.getData(OCCLUSION_PROPERTY);
		if (occlusionData != null && occlusionData.isOccluded(side))
			return quads;

		return getCroppedQuads(state, side, rand, material, wrappedData);
	}

	protected abstract List<BakedQuad> getCroppedQuads(BlockState state, Direction side, Random rand,
		BlockState material, IModelData wrappedData);

	public static boolean cropAndMove(BakedQuad quad, AABB crop, Vec3 move) {
		int[] vertexData = quad.getVertices();

		Vec3 xyz0 = QuadHelper.getXYZ(vertexData, 0);
		Vec3 xyz1 = QuadHelper.getXYZ(vertexData, 1);
		Vec3 xyz2 = QuadHelper.getXYZ(vertexData, 2);
		Vec3 xyz3 = QuadHelper.getXYZ(vertexData, 3);

		Vec3 uAxis = xyz3.add(xyz2)
			.scale(.5);
		Vec3 vAxis = xyz1.add(xyz2)
			.scale(.5);
		Vec3 center = xyz3.add(xyz2)
			.add(xyz0)
			.add(xyz1)
			.scale(.25);

		float u0 = QuadHelper.getU(vertexData, 0);
		float u3 = QuadHelper.getU(vertexData, 3);
		float v0 = QuadHelper.getV(vertexData, 0);
		float v1 = QuadHelper.getV(vertexData, 1);

		TextureAtlasSprite sprite = quad.getSprite();

		float uScale = (float) Math
			.round((getUnInterpolatedU(sprite, u3) - getUnInterpolatedU(sprite, u0)) / xyz3.distanceTo(xyz0));
		float vScale = (float) Math
			.round((getUnInterpolatedV(sprite, v1) - getUnInterpolatedV(sprite, v0)) / xyz1.distanceTo(xyz0));

		if (uScale == 0) {
			float v3 = QuadHelper.getV(vertexData, 3);
			float u1 = QuadHelper.getU(vertexData, 1);
			uAxis = xyz1.add(xyz2)
				.scale(.5);
			vAxis = xyz3.add(xyz2)
				.scale(.5);
			uScale = (float) Math
				.round((getUnInterpolatedU(sprite, u1) - getUnInterpolatedU(sprite, u0)) / xyz1.distanceTo(xyz0));
			vScale = (float) Math
				.round((getUnInterpolatedV(sprite, v3) - getUnInterpolatedV(sprite, v0)) / xyz3.distanceTo(xyz0));
			
		}

		uAxis = uAxis.subtract(center)
			.normalize();
		vAxis = vAxis.subtract(center)
			.normalize();

		Vec3 min = new Vec3(crop.minX, crop.minY, crop.minZ);
		Vec3 max = new Vec3(crop.maxX, crop.maxY, crop.maxZ);

		for (int vertex = 0; vertex < 4; vertex++) {
			Vec3 xyz = QuadHelper.getXYZ(vertexData, vertex);
			Vec3 newXyz = VecHelper.componentMin(max, VecHelper.componentMax(xyz, min));
			Vec3 diff = newXyz.subtract(xyz);

			if (diff.lengthSqr() > 0) {
				float u = QuadHelper.getU(vertexData, vertex);
				float v = QuadHelper.getV(vertexData, vertex);
				float uDiff = (float) uAxis.dot(diff) * uScale;
				float vDiff = (float) vAxis.dot(diff) * vScale;
				QuadHelper.setU(vertexData, vertex, sprite.getU(getUnInterpolatedU(sprite, u) + uDiff));
				QuadHelper.setV(vertexData, vertex, sprite.getV(getUnInterpolatedV(sprite, v) + vDiff));
			}

			QuadHelper.setXYZ(vertexData, vertex, newXyz.add(move));
		}

		return true;
	}

	@Override
	public TextureAtlasSprite getParticleIcon(IModelData data) {
		BlockState material = getMaterial(data);
		IModelData wrappedData = data.getData(WRAPPED_DATA_PROPERTY);

		if (wrappedData == null)
			wrappedData = EmptyModelData.INSTANCE;
		if (material != null)
			return getModelOf(material).getParticleIcon(wrappedData);

		return super.getParticleIcon(data);
	}

	@Nullable
	public BlockState getMaterial(IModelData extraData) {
		BlockState material = extraData.getData(MATERIAL_PROPERTY);
		return material == null ? AllBlocks.COPYCAT_BASE.getDefaultState() : material;
	}

	@Nullable
	public BlockState getMaterial(BlockAndTintGetter world, BlockPos pos, BlockState state) {
		if (!(state.getBlock()instanceof CopycatBlock ufb))
			return null;
		return ufb.getTileEntityOptional(world, pos)
			.map(CopycatTileEntity::getMaterial)
			.orElse(null);
	}

	public BakedModel getModelOf(BlockState wrappedState) {
		return Minecraft.getInstance()
			.getBlockRenderer()
			.getBlockModel(wrappedState);
	}

	private static class OcclusionData {
		private final boolean[] occluded;

		public OcclusionData() {
			occluded = new boolean[6];
			Arrays.fill(occluded, false);
		}

		public void occlude(Direction face) {
			occluded[face.get3DDataValue()] = true;
		}

		public boolean isOccluded(Direction face) {
			return face == null ? false : occluded[face.get3DDataValue()];
		}
	}

}
