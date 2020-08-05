package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import static com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock.RAIL_SHAPE;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.AllContraptionTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerTileEntity.CartMovementMode;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class MountedContraption extends Contraption {

	public CartMovementMode rotationMode;
	public AbstractMinecartEntity connectedCart;

	public MountedContraption() {
		rotationMode = CartMovementMode.ROTATE;
	}

	@Override
	protected AllContraptionTypes getType() {
		return AllContraptionTypes.MOUNTED;
	}

	public static MountedContraption assembleMinecart(World world, BlockPos pos) {
		if (isFrozen())
			return null;

		BlockState state = world.getBlockState(pos);
		if (!state.has(RAIL_SHAPE))
			return null;

		MountedContraption contraption = new MountedContraption();
		if (!contraption.searchMovedStructure(world, pos, null))
			return null;

		Axis axis = state.get(RAIL_SHAPE) == RailShape.EAST_WEST ? Axis.X : Axis.Z;
		contraption.add(pos, Pair.of(new BlockInfo(pos, AllBlocks.MINECART_ANCHOR.getDefaultState()
			.with(BlockStateProperties.HORIZONTAL_AXIS, axis), null), null));
		contraption.removeBlocksFromWorld(world, BlockPos.ZERO);
		contraption.initActors(world);
		contraption.expandBoundsAroundAxis(Axis.Y);

		return contraption;
	}

	@Override
	protected boolean addToInitialFrontier(World world, BlockPos pos, Direction direction, List<BlockPos> frontier) {
		frontier.clear();
		frontier.add(pos.up());
		return true;
	}

	@Override
	protected Pair<BlockInfo, TileEntity> capture(World world, BlockPos pos) {
		Pair<BlockInfo, TileEntity> pair = super.capture(world, pos);
		BlockInfo capture = pair.getKey();
		if (AllBlocks.CART_ASSEMBLER.has(capture.state)) {
			if (!pos.equals(anchor)) {
				for (Axis axis : Iterate.axes) {
					if (axis.isVertical())
						continue;
					if (VecHelper.onSameAxis(anchor, pos, axis) && connectedCart == null) {
						for (AbstractMinecartEntity abstractMinecartEntity : world
							.getEntitiesWithinAABB(AbstractMinecartEntity.class, new AxisAlignedBB(pos))) {
							if (!CartAssemblerBlock.canAssembleTo(abstractMinecartEntity))
								break;
							connectedCart = abstractMinecartEntity;
						}
					}
				}
			}
			return Pair.of(new BlockInfo(pos, CartAssemblerBlock.createAnchor(capture.state), null), pair.getValue());
		}
		return pair;
	}

	@Override
	protected boolean movementAllowed(World world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		if (!pos.equals(anchor) && AllBlocks.CART_ASSEMBLER.has(blockState))
			return true;
		return super.movementAllowed(world, pos);
	}

	@Override
	public CompoundNBT writeNBT() {
		CompoundNBT writeNBT = super.writeNBT();
		NBTHelper.writeEnum(writeNBT, "RotationMode", rotationMode);
		return writeNBT;
	}

	@Override
	public void readNBT(World world, CompoundNBT nbt) {
		rotationMode = NBTHelper.readEnum(nbt, "RotationMode", CartMovementMode.class);
		super.readNBT(world, nbt);
	}

	@Override
	protected boolean customBlockPlacement(IWorld world, BlockPos pos, BlockState state) {
		return AllBlocks.MINECART_ANCHOR.has(state);
	}

	@Override
	protected boolean customBlockRemoval(IWorld world, BlockPos pos, BlockState state) {
		return pos.equals(anchor);
	}

}
