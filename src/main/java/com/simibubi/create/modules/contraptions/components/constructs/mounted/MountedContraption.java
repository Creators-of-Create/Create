package com.simibubi.create.modules.contraptions.components.constructs.mounted;

import static com.simibubi.create.modules.contraptions.components.constructs.mounted.CartAssemblerBlock.RAIL_SHAPE;

import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.components.constructs.Contraption;
import com.simibubi.create.modules.contraptions.components.constructs.IHaveMovementBehavior.MovementContext;
import com.simibubi.create.modules.contraptions.components.constructs.IHaveMovementBehavior.MoverType;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class MountedContraption extends Contraption {

	public static MountedContraption assembleMinecart(World world, BlockPos pos, AbstractMinecartEntity cart) {
		if (isFrozen())
			return null;

		BlockState state = world.getBlockState(pos);
		if (!state.has(RAIL_SHAPE))
			return null;

		MountedContraption contraption = new MountedContraption();
		Vec3d vec = cart.getMotion();
		if (!contraption.searchMovedStructure(world, pos, Direction.getFacingFromVector(vec.x, vec.y, vec.z)))
			return null;

		Axis axis = state.get(RAIL_SHAPE) == RailShape.EAST_WEST ? Axis.X : Axis.Z;
		contraption.add(pos, new BlockInfo(pos,
				AllBlocks.MINECART_ANCHOR.block.getDefaultState().with(BlockStateProperties.HORIZONTAL_AXIS, axis),
				null));

		for (BlockInfo block : contraption.blocks.values()) {
			BlockPos startPos = pos;
			if (startPos.equals(block.pos))
				continue;
			world.setBlockState(block.pos, Blocks.AIR.getDefaultState(), 67);
		}

		for (MutablePair<BlockInfo, MovementContext> pair : contraption.getActors()) {
			MovementContext context = new MovementContext(pair.left.state, MoverType.MINECART);
			context.world = world;
			context.motion = vec;
			context.currentGridPos = pair.left.pos;
			pair.setRight(context);
		}

		return contraption;
	}

	@Override
	protected boolean addToInitialFrontier(World world, BlockPos pos, Direction direction, List<BlockPos> frontier) {
		frontier.clear();
		frontier.add(pos.up());
		BlockState state = world.getBlockState(pos);
		if (!AllBlocks.CART_ASSEMBLER.typeOf(state))
			return false;
		Axis axis = state.get(CartAssemblerBlock.RAIL_SHAPE) == RailShape.EAST_WEST ? Axis.Z : Axis.X;
		for (AxisDirection axisDirection : AxisDirection.values())
			frontier.add(pos.offset(Direction.getFacingFromAxis(axisDirection, axis)));
		return true;
	}

	@Override
	protected BlockInfo capture(World world, BlockPos pos) {
		BlockInfo capture = super.capture(world, pos);
		if (AllBlocks.CART_ASSEMBLER.typeOf(capture.state))
			return new BlockInfo(capture.pos, CartAssemblerBlock.createAnchor(capture.state), null);
		return capture;
	}

	public BlockPos getAnchor() {
		return anchor;
	}

}
