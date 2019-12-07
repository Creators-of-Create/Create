package com.simibubi.create.modules.contraptions.receivers.contraptions.mounted;

import static com.simibubi.create.modules.contraptions.receivers.contraptions.mounted.CartAssemblerBlock.RAIL_SHAPE;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.receivers.contraptions.Contraption;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class MountedContraption extends Contraption {

	public static Contraption assembleMinecart(World world, BlockPos pos, AbstractMinecartEntity cart) {
		if (isFrozen())
			return null;

		BlockState state = world.getBlockState(pos);
		if (!state.has(RAIL_SHAPE))
			return null;

		Contraption contraption = new MountedContraption();
		Vec3d vec = cart.getMotion();
		if (!contraption.searchMovedStructure(world, pos, Direction.getFacingFromVector(vec.x, vec.y, vec.z)))
			return null;

		Axis axis = state.get(RAIL_SHAPE) == RailShape.EAST_WEST ? Axis.X : Axis.Z;
		contraption.add(pos, new BlockInfo(pos,
				AllBlocks.MINECART_ANCHOR.block.getDefaultState().with(BlockStateProperties.HORIZONTAL_AXIS, axis),
				null));
		contraption.removeBlocksFromWorld(world, BlockPos.ZERO);
		contraption.initActors(world);

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

	@Override
	public void removeBlocksFromWorld(IWorld world, BlockPos offset) {
		super.removeBlocksFromWorld(world, offset, (pos, state) -> pos.equals(anchor));
	}

	@Override
	public void disassemble(IWorld world, BlockPos offset, float yaw, float pitch) {
		super.disassemble(world, offset, yaw, pitch, (pos, state) -> AllBlocks.MINECART_ANCHOR.typeOf(state));
	}

}
