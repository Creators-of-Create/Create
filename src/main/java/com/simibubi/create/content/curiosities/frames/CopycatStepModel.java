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
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.IModelData;

public class CopycatStepModel extends CopycatModel {

	public CopycatStepModel(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	protected List<BakedQuad> getCroppedQuads(BlockState state, Direction side, Random rand, BlockState material,
		IModelData wrappedData) {
		Direction facing = state.getOptionalValue(CopycatStepBlock.FACING)
			.orElse(Direction.SOUTH);
		boolean upperHalf = state.getOptionalValue(CopycatStepBlock.HALF)
			.orElse(Half.BOTTOM) == Half.TOP;

		Vec3 zero = Vec3.ZERO;
		Vec3 up = new Vec3(0, 1, 0);
		Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());

		BakedModel model = getModelOf(material);
		List<BakedQuad> templateQuads = model.getQuads(material, side, rand, wrappedData);
		int size = templateQuads.size();
		AABB cube = new AABB(BlockPos.ZERO);

		List<BakedQuad> quads = new ArrayList<>();

		// 4 Pieces
		for (boolean top : Iterate.trueAndFalse) {
			for (boolean front : Iterate.trueAndFalse) {

				for (int i = 0; i < size; i++) {
					BakedQuad quad = templateQuads.get(i);
					Direction direction = quad.getDirection();

					if (front && direction == facing)
						continue;
					if (!front && direction == facing.getOpposite())
						continue;
					if (!top && direction == Direction.UP)
						continue;
					if (top && direction == Direction.DOWN)
						continue;

					AABB bb = cube.contract(-normal.x * .75, .75, -normal.z * .75);

					if (front)
						bb = bb.move(normal.scale(-.75));
					if (top)
						bb = bb.move(up.scale(.75));

					Vec3 offset = zero;

					if (front)
						offset = offset.add(normal.scale(.5));
					if (top != upperHalf)
						offset = offset.add(up.scale(upperHalf ? .5 : -.5));

					BakedQuad newQuad = QuadHelper.clone(quad);
					if (cropAndMove(newQuad, bb, offset))
						quads.add(newQuad);
				}

			}
		}

		return quads;
	}

}
