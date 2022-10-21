package com.simibubi.create.content.curiosities.frames;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.simibubi.create.foundation.block.render.QuadHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.IModelData;

public class CopycatPanelModel extends CopycatModel {

	public CopycatPanelModel(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	protected List<BakedQuad> getCroppedQuads(BlockState state, Direction side, Random rand, BlockState material,
		IModelData wrappedData) {
		Direction facing = state.getOptionalValue(CopycatPanelBlock.FACING)
			.orElse(Direction.UP);

		Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
		AABB cube = new AABB(BlockPos.ZERO);

		BakedModel model = getModelOf(material);
		List<BakedQuad> templateQuads = model.getQuads(material, side, rand, wrappedData);
		int size = templateQuads.size();

		List<BakedQuad> quads = new ArrayList<>();

		// 2 Pieces
		for (boolean front : Iterate.trueAndFalse) {

			for (int i = 0; i < size; i++) {
				BakedQuad quad = templateQuads.get(i);
				Direction direction = quad.getDirection();

				if (front && direction == facing)
					continue;
				if (!front && direction == facing.getOpposite())
					continue;

				float contract = 16 - (front ? 1 : 2);
				AABB bb = cube.contract(normal.x * contract / 16, normal.y * contract / 16, normal.z * contract / 16);

				if (!front)
					bb = bb.move(normal.scale(14 / 16f));

				BakedQuad newQuad = QuadHelper.clone(quad);
				if (cropAndMove(newQuad, bb, normal.scale(front ? 0 : -13 / 16f)));
					quads.add(newQuad);
			}

		}

		return quads;
	}

}
