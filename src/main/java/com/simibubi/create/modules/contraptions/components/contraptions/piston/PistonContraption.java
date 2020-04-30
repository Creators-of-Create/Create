package com.simibubi.create.modules.contraptions.components.contraptions.piston;

import static com.simibubi.create.AllBlocks.MECHANICAL_PISTON_HEAD;
import static com.simibubi.create.AllBlocks.PISTON_POLE;
import static com.simibubi.create.AllBlocks.STICKY_MECHANICAL_PISTON;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.AllContraptionTypes;
import com.simibubi.create.modules.contraptions.components.contraptions.BlockMovementTraits;
import com.simibubi.create.modules.contraptions.components.contraptions.Contraption;
import com.simibubi.create.modules.contraptions.components.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.MechanicalPistonBlock.PistonState;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.PistonType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class PistonContraption extends Contraption {

	protected AxisAlignedBB pistonExtensionCollisionBox;

	protected int extensionLength;
	protected int initialExtensionProgress;
	protected Direction orientation;

	@Override
	protected AllContraptionTypes getType() {
		return AllContraptionTypes.PISTON;
	}

	public static PistonContraption movePistonAt(World world, BlockPos pos, Direction direction, boolean retract) {
		if (isFrozen())
			return null;
		PistonContraption construct = new PistonContraption();
		construct.orientation = direction;
		if (!construct.collectExtensions(world, pos, direction))
			return null;
		if (!construct.searchMovedStructure(world, construct.anchor, retract ? direction.getOpposite() : direction))
			return null;
		construct.initActors(world);
		return construct;
	}

	private boolean collectExtensions(World world, BlockPos pos, Direction direction) {
		List<BlockInfo> poles = new ArrayList<>();
		BlockPos actualStart = pos;
		BlockState nextBlock = world.getBlockState(actualStart.offset(direction));
		int extensionsInFront = 0;
		BlockState blockState = world.getBlockState(pos);
		boolean sticky = STICKY_MECHANICAL_PISTON.typeOf(blockState);

		if (!(blockState.getBlock() instanceof MechanicalPistonBlock))
			return false;

		if (blockState.get(MechanicalPistonBlock.STATE) == PistonState.EXTENDED) {
			while (PISTON_POLE.typeOf(nextBlock) && nextBlock.get(FACING).getAxis() == direction.getAxis()
					|| MECHANICAL_PISTON_HEAD.typeOf(nextBlock) && nextBlock.get(FACING) == direction) {

				actualStart = actualStart.offset(direction);
				poles.add(new BlockInfo(actualStart, nextBlock.with(FACING, direction), null));
				extensionsInFront++;

				if (MECHANICAL_PISTON_HEAD.typeOf(nextBlock))
					break;

				nextBlock = world.getBlockState(actualStart.offset(direction));
				if (extensionsInFront > MechanicalPistonBlock.maxAllowedPistonPoles())
					return false;
			}
		}

		if (extensionsInFront == 0)
			poles.add(
					new BlockInfo(pos,
							MECHANICAL_PISTON_HEAD.get().getDefaultState().with(FACING, direction).with(
									BlockStateProperties.PISTON_TYPE, sticky ? PistonType.STICKY : PistonType.DEFAULT),
							null));
		else
			poles.add(new BlockInfo(pos, PISTON_POLE.get().getDefaultState().with(FACING, direction), null));

		BlockPos end = pos;
		nextBlock = world.getBlockState(end.offset(direction.getOpposite()));
		int extensionsInBack = 0;

		while (PISTON_POLE.typeOf(nextBlock)) {
			end = end.offset(direction.getOpposite());
			poles.add(new BlockInfo(end, nextBlock.with(FACING, direction), null));
			extensionsInBack++;
			nextBlock = world.getBlockState(end.offset(direction.getOpposite()));

			if (extensionsInFront + extensionsInBack > MechanicalPistonBlock.maxAllowedPistonPoles())
				return false;
		}

		anchor = pos.offset(direction, initialExtensionProgress + 1);
		extensionLength = extensionsInBack + extensionsInFront;
		initialExtensionProgress = extensionsInFront;
		pistonExtensionCollisionBox = new AxisAlignedBB(end.offset(direction, -extensionsInFront).subtract(anchor));

		if (extensionLength == 0)
			return false;

		bounds = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

		for (BlockInfo pole : poles) {
			BlockPos relPos = pole.pos.offset(direction, -extensionsInFront);
			BlockPos localPos = relPos.subtract(anchor);
			blocks.put(localPos, new BlockInfo(localPos, pole.state, null));
			pistonExtensionCollisionBox = pistonExtensionCollisionBox.union(new AxisAlignedBB(localPos));
		}

		return true;
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pistonExtensionCollisionBox.contains(VecHelper.getCenterOf(pos.subtract(anchor)));
	}

	@Override
	protected boolean addToInitialFrontier(World world, BlockPos pos, Direction direction, List<BlockPos> frontier) {
		frontier.clear();
		boolean sticky = STICKY_MECHANICAL_PISTON.typeOf(world.getBlockState(pos.offset(orientation, -1)));
		boolean retracting = direction != orientation;
		if (retracting && !sticky)
			return true;
		for (int offset = 0; offset <= AllConfigs.SERVER.kinetics.maxChassisRange.get(); offset++) {
			if (offset == 1 && retracting)
				return true;
			BlockPos currentPos = pos.offset(orientation, offset + initialExtensionProgress);
			if (!world.isBlockPresent(currentPos))
				return false;
			if (!BlockMovementTraits.movementNecessary(world, currentPos))
				return true;
			BlockState state = world.getBlockState(currentPos);
			if (BlockMovementTraits.isBrittle(state))
				return true;
			if (AllBlocks.MECHANICAL_PISTON_HEAD.typeOf(state) && state.get(FACING) == direction.getOpposite())
				return true;
			if (!BlockMovementTraits.movementAllowed(world, currentPos))
				return retracting;
			frontier.add(currentPos);
			if (BlockMovementTraits.notSupportive(state, orientation))
				return true;
		}
		return true;
	}

	@Override
	public void add(BlockPos pos, Pair<BlockInfo, TileEntity> capture) {
		super.add(pos.offset(orientation, -initialExtensionProgress), capture);
	}

	@Override
	public void addGlue(SuperGlueEntity entity) {
		BlockPos pos = entity.getHangingPosition();
		Direction direction = entity.getFacingDirection();
		BlockPos localPos = pos.subtract(anchor).offset(orientation, -initialExtensionProgress);
		this.superglue.add(Pair.of(localPos, direction));
		glueToRemove.add(entity);
	}

	@Override
	public void addBlocksToWorld(World world, BlockPos offset, Vec3d rotation) {
		super.addBlocksToWorld(world, offset, rotation, (pos, state) -> {
			BlockPos pistonPos = anchor.offset(orientation, -1);
			BlockState pistonState = world.getBlockState(pistonPos);
			TileEntity te = world.getTileEntity(pistonPos);
			if (pos.equals(pistonPos)) {
				if (te == null || te.isRemoved())
					return true;
				if (!AllBlocks.PISTON_POLE.typeOf(state) && pistonState.getBlock() instanceof MechanicalPistonBlock)
					world.setBlockState(pistonPos, pistonState.with(MechanicalPistonBlock.STATE, PistonState.RETRACTED),
							3 | 16);
				return true;
			}
			return false;
		});
	}

	@Override
	public void removeBlocksFromWorld(IWorld world, BlockPos offset) {
		super.removeBlocksFromWorld(world, offset, (pos, state) -> {
			BlockPos pistonPos = anchor.offset(orientation, -1);
			BlockState blockState = world.getBlockState(pos);
			if (pos.equals(pistonPos) && blockState.getBlock() instanceof MechanicalPistonBlock) {
				world.setBlockState(pos, blockState.with(MechanicalPistonBlock.STATE, PistonState.MOVING), 66 | 16);
				return true;
			}
			return false;
		});
	}

	@Override
	public void readNBT(World world, CompoundNBT nbt) {
		super.readNBT(world, nbt);
		extensionLength = nbt.getInt("ExtensionLength");
		initialExtensionProgress = nbt.getInt("InitialLength");
		orientation = Direction.byIndex(nbt.getInt("Orientation"));
		if (nbt.contains("BoundsBack"))
			pistonExtensionCollisionBox = NBTHelper.readAABB(nbt.getList("BoundsBack", 5));
	}

	@Override
	public CompoundNBT writeNBT() {
		CompoundNBT nbt = super.writeNBT();

		if (pistonExtensionCollisionBox != null) {
			ListNBT bb = NBTHelper.writeAABB(pistonExtensionCollisionBox);
			nbt.put("BoundsBack", bb);
		}
		nbt.putInt("InitialLength", initialExtensionProgress);
		nbt.putInt("ExtensionLength", extensionLength);
		nbt.putInt("Orientation", orientation.getIndex());

		return nbt;
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return super.getBoundingBox().union(pistonExtensionCollisionBox);
	}
}
