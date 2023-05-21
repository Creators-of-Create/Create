package com.simibubi.create.content.trains.track;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrackVoxelShapes {

	public static VoxelShape orthogonal() {
		return Block.box(-14, 0, 0, 16 + 14, 4, 16);
	}

	public static VoxelShape longOrthogonalX() {
		return Block.box(-3.3, 0, -14, 19.3, 4, 16 + 14);
	}

	public static VoxelShape longOrthogonalZ() {
		return Block.box(-14, 0, -3.3, 16 + 14, 4, 19.3);
	}

	public static VoxelShape longOrthogonalZOffset() {
		return Block.box(-14, 0, 0, 16 + 14, 4, 24);
	}

	public static VoxelShape ascending() {
		VoxelShape shape = Block.box(-14, 0, 0, 16 + 14, 4, 4);
		VoxelShape[] shapes = new VoxelShape[6];
		for (int i = 0; i < 6; i++) {
			int off = (i + 1) * 2;
			shapes[i] = Block.box(-14, off, off, 16 + 14, 4 + off, 4 + off);
		}
		return Shapes.or(shape, shapes);
	}

	public static VoxelShape diagonal() {
		VoxelShape shape = Block.box(0, 0, 0, 16, 4, 16);
		VoxelShape[] shapes = new VoxelShape[12];
		int off = 0;

		for (int i = 0; i < 6; i++) {
			off = (i + 1) * 2;
			shapes[i * 2] = Block.box(off, 0, off, 16 + off, 4, 16 + off);
			shapes[i * 2 + 1] = Block.box(-off, 0, -off, 16 - off, 4, 16 - off);
		}

		shape = Shapes.or(shape, shapes);

		off = 10 * 2;
		shape = Shapes.join(shape, Block.box(off, 0, off, 16 + off, 4, 16 + off), BooleanOp.ONLY_FIRST);
		shape = Shapes.join(shape, Block.box(-off, 0, -off, 16 - off, 4, 16 - off), BooleanOp.ONLY_FIRST);

		off = 4 * 2;
		shape = Shapes.or(shape, Block.box(off, 0, off, 16 + off, 4, 16 + off));
		shape = Shapes.or(shape, Block.box(-off, 0, -off, 16 - off, 4, 16 - off));

		return shape.optimize();
	}

}
