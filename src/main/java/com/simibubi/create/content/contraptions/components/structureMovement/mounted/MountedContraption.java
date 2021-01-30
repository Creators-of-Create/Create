package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import static com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock.RAIL_SHAPE;

import java.util.Queue;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.AllContraptionTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerTileEntity.CartMovementMode;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.inventory.IInventory;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.InvWrapper;

public class MountedContraption extends Contraption {

	public CartMovementMode rotationMode;
	public AbstractMinecartEntity connectedCart;

	public MountedContraption() {
		this(CartMovementMode.ROTATE);
	}

	public MountedContraption(CartMovementMode mode) {
		rotationMode = mode;
	}

	@Override
	protected AllContraptionTypes getType() {
		return AllContraptionTypes.MOUNTED;
	}
	
	@Override
	public boolean assemble(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (!state.has(RAIL_SHAPE))
			return false;
		if (!searchMovedStructure(world, pos, null))
			return false;
		
		Axis axis = state.get(RAIL_SHAPE) == RailShape.EAST_WEST ? Axis.X : Axis.Z;
		addBlock(pos, Pair.of(new BlockInfo(pos, AllBlocks.MINECART_ANCHOR.getDefaultState()
			.with(BlockStateProperties.HORIZONTAL_AXIS, axis), null), null));
		
		if (blocks.size() == 1)
			return false;
		
		return true;
	}
	
	@Override
	protected boolean addToInitialFrontier(World world, BlockPos pos, Direction direction, Queue<BlockPos> frontier) {
		frontier.clear();
		frontier.add(pos.up());
		return true;
	}

	@Override
	protected Pair<BlockInfo, TileEntity> capture(World world, BlockPos pos) {
		Pair<BlockInfo, TileEntity> pair = super.capture(world, pos);
		BlockInfo capture = pair.getKey();
		if (!AllBlocks.CART_ASSEMBLER.has(capture.state))
			return pair;

		Pair<BlockInfo, TileEntity> anchorSwap =
			Pair.of(new BlockInfo(pos, CartAssemblerBlock.createAnchor(capture.state), null), pair.getValue());
		if (pos.equals(anchor) || connectedCart != null)
			return anchorSwap;

		for (Axis axis : Iterate.axes) {
			if (axis.isVertical() || !VecHelper.onSameAxis(anchor, pos, axis))
				continue;
			for (AbstractMinecartEntity abstractMinecartEntity : world
				.getEntitiesWithinAABB(AbstractMinecartEntity.class, new AxisAlignedBB(pos))) {
				if (!CartAssemblerBlock.canAssembleTo(abstractMinecartEntity))
					break;
				connectedCart = abstractMinecartEntity;
				connectedCart.setPosition(pos.getX() + .5, pos.getY(), pos.getZ() + .5f);
			}
		}

		return anchorSwap;
	}

	@Override
	protected boolean movementAllowed(BlockState state, World world, BlockPos pos) {
		if (!pos.equals(anchor) && AllBlocks.CART_ASSEMBLER.has(state))
			return testSecondaryCartAssembler(world, state, pos);
		return super.movementAllowed(state, world, pos);
	}

	protected boolean testSecondaryCartAssembler(World world, BlockState state, BlockPos pos) {
		for (Axis axis : Iterate.axes) {
			if (axis.isVertical() || !VecHelper.onSameAxis(anchor, pos, axis))
				continue;
			for (AbstractMinecartEntity abstractMinecartEntity : world
				.getEntitiesWithinAABB(AbstractMinecartEntity.class, new AxisAlignedBB(pos))) {
				if (!CartAssemblerBlock.canAssembleTo(abstractMinecartEntity))
					break;
				return true;
			}
		}
		return false;
	}

	@Override
	public CompoundNBT writeNBT(boolean spawnPacket) {
		CompoundNBT tag = super.writeNBT(spawnPacket);
		NBTHelper.writeEnum(tag, "RotationMode", rotationMode);
		return tag;
	}

	@Override
	public void readNBT(World world, CompoundNBT nbt, boolean spawnData) {
		rotationMode = NBTHelper.readEnum(nbt, "RotationMode", CartMovementMode.class);
		super.readNBT(world, nbt, spawnData);
	}

	@Override
	protected boolean customBlockPlacement(IWorld world, BlockPos pos, BlockState state) {
		return AllBlocks.MINECART_ANCHOR.has(state);
	}

	@Override
	protected boolean customBlockRemoval(IWorld world, BlockPos pos, BlockState state) {
		return AllBlocks.MINECART_ANCHOR.has(state);
	}
	
	@Override
	protected boolean canAxisBeStabilized(Axis axis) {
		return true;
	}
	
	@Override
	public void addExtraInventories(Entity cart) {
		if (!(cart instanceof IInventory))
			return;
		IItemHandlerModifiable handlerFromInv = new InvWrapper((IInventory) cart);
		inventory = new CombinedInvWrapper(handlerFromInv, inventory);
	}
}
