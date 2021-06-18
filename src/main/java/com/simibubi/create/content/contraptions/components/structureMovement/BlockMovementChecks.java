package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.actors.AttachedActorBlock;
import com.simibubi.create.content.contraptions.components.actors.HarvesterBlock;
import com.simibubi.create.content.contraptions.components.actors.PortableStorageInterfaceBlock;
import com.simibubi.create.content.contraptions.components.crank.HandCrankBlock;
import com.simibubi.create.content.contraptions.components.fan.NozzleBlock;
import com.simibubi.create.content.contraptions.components.flywheel.engine.EngineBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.ClockworkBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.ClockworkBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.SailBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.WindmillBearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.WindmillBearingTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.AbstractChassisBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyTileEntity;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankConnectivityHandler;
import com.simibubi.create.content.logistics.block.redstone.RedstoneLinkBlock;

import com.simibubi.create.foundation.config.AllConfigs;

import com.simibubi.create.foundation.config.CKinetics;

import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.BellAttachment;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMovementChecks {

	private static final List<MovementNecessaryCheck> MOVEMENT_NECESSARY_CHECKS = new ArrayList<>();
	private static final List<MovementAllowedCheck> MOVEMENT_ALLOWED_CHECKS = new ArrayList<>();
	private static final List<BrittleCheck> BRITTLE_CHECKS = new ArrayList<>();
	private static final List<AttachedCheck> ATTACHED_CHECKS = new ArrayList<>();
	private static final List<NotSupportiveCheck> NOT_SUPPORTIVE_CHECKS = new ArrayList<>();
	public static final ResourceLocation NON_MOVABLE = new ResourceLocation(Create.ID, "non_movable");

	// Registration
	// Add new checks to the front instead of the end

	public static void registerMovementNecessaryCheck(MovementNecessaryCheck check) {
		MOVEMENT_NECESSARY_CHECKS.add(0, check);
	}

	public static void registerMovementAllowedCheck(MovementAllowedCheck check) {
		MOVEMENT_ALLOWED_CHECKS.add(0, check);
	}

	public static void registerBrittleCheck(BrittleCheck check) {
		BRITTLE_CHECKS.add(0, check);
	}

	public static void registerAttachedCheck(AttachedCheck check) {
		ATTACHED_CHECKS.add(0, check);
	}

	public static void registerNotSupportiveCheck(NotSupportiveCheck check) {
		NOT_SUPPORTIVE_CHECKS.add(0, check);
	}

	public static void registerAllChecks(AllChecks checks) {
		registerMovementNecessaryCheck(checks);
		registerMovementAllowedCheck(checks);
		registerBrittleCheck(checks);
		registerAttachedCheck(checks);
		registerNotSupportiveCheck(checks);
	}

	// Actual check methods

	public static boolean isMovementNecessary(BlockState state, World world, BlockPos pos) {
		for (MovementNecessaryCheck check : MOVEMENT_NECESSARY_CHECKS) {
			CheckResult result = check.isMovementNecessary(state, world, pos);
			if (result != CheckResult.PASS) {
				return result.toBoolean();
			}
		}
		return isMovementNecessaryFallback(state, world, pos);
	}

	public static boolean isMovementAllowed(BlockState state, World world, BlockPos pos) {
		for (MovementAllowedCheck check : MOVEMENT_ALLOWED_CHECKS) {
			CheckResult result = check.isMovementAllowed(state, world, pos);
			if (result != CheckResult.PASS) {
				return result.toBoolean();
			}
		}
		return isMovementAllowedFallback(state, world, pos);
	}

	/**
	 * Brittle blocks will be collected first, as they may break when other blocks
	 * are removed before them
	 */
	public static boolean isBrittle(BlockState state) {
		for (BrittleCheck check : BRITTLE_CHECKS) {
			CheckResult result = check.isBrittle(state);
			if (result != CheckResult.PASS) {
				return result.toBoolean();
			}
		}
		return isBrittleFallback(state);
	}

	/**
	 * Attached blocks will move if blocks they are attached to are moved
	 */
	public static boolean isBlockAttachedTowards(BlockState state, World world, BlockPos pos,
		Direction direction) {
		for (AttachedCheck check : ATTACHED_CHECKS) {
			CheckResult result = check.isBlockAttachedTowards(state, world, pos, direction);
			if (result != CheckResult.PASS) {
				return result.toBoolean();
			}
		}
		return isBlockAttachedTowardsFallback(state, world, pos, direction);
	}

	/**
	 * Non-Supportive blocks will not continue a chain of blocks picked up by e.g. a
	 * piston
	 */
	public static boolean isNotSupportive(BlockState state, Direction facing) {
		for (NotSupportiveCheck check : NOT_SUPPORTIVE_CHECKS) {
			CheckResult result = check.isNotSupportive(state, facing);
			if (result != CheckResult.PASS) {
				return result.toBoolean();
			}
		}
		return isNotSupportiveFallback(state, facing);
	}

	// Fallback checks

	private static boolean isMovementNecessaryFallback(BlockState state, World world, BlockPos pos) {
		if (isBrittle(state))
			return true;
		if (state.getBlock() instanceof FenceGateBlock)
			return true;
		if (state.getMaterial()
			.isReplaceable())
			return false;
		if (state.getCollisionShape(world, pos)
			.isEmpty())
			return false;
		return true;
	}

	private static boolean isMovementAllowedFallback(BlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		if (block instanceof AbstractChassisBlock)
			return true;
		if (state.getBlockHardness(world, pos) == -1)
			return false;
		if (state.getBlock().getTags().contains(NON_MOVABLE))
			return false;
		if (AllConfigs.SERVER.kinetics.spawnerMovement.get() == CKinetics.SpawnerMovementSetting.UNMOVABLE
			&& block instanceof SpawnerBlock)
			return false;

		// Move controllers only when they aren't moving
		if (block instanceof MechanicalPistonBlock && state.get(MechanicalPistonBlock.STATE) != PistonState.MOVING)
			return true;
		if (block instanceof MechanicalBearingBlock) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof MechanicalBearingTileEntity)
				return !((MechanicalBearingTileEntity) te).isRunning();
		}
		if (block instanceof WindmillBearingBlock) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof WindmillBearingTileEntity)
				return !((WindmillBearingTileEntity) te).isRunning();
		}
		if (block instanceof ClockworkBearingBlock) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof ClockworkBearingTileEntity)
				return !((ClockworkBearingTileEntity) te).isRunning();
		}
		if (block instanceof PulleyBlock) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof PulleyTileEntity)
				return !((PulleyTileEntity) te).running;
		}

		if (AllBlocks.BELT.has(state))
			return true;
		if (state.getBlock() instanceof GrindstoneBlock)
			return true;
		return state.getPushReaction() != PushReaction.BLOCK;
	}

	private static boolean isBrittleFallback(BlockState state) {
		Block block = state.getBlock();
		if (state.contains(BlockStateProperties.HANGING))
			return true;

		if (block instanceof LadderBlock)
			return true;
		if (block instanceof TorchBlock)
			return true;
		if (block instanceof AbstractSignBlock)
			return true;
		if (block instanceof AbstractPressurePlateBlock)
			return true;
		if (block instanceof HorizontalFaceBlock && !(block instanceof GrindstoneBlock))
			return true;
		if (block instanceof CartAssemblerBlock)
			return false;
		if (block instanceof AbstractRailBlock)
			return true;
		if (block instanceof RedstoneDiodeBlock)
			return true;
		if (block instanceof RedstoneWireBlock)
			return true;
		if (block instanceof CarpetBlock)
			return true;
		return AllBlockTags.BRITTLE.tag.contains(block);
	}

	private static boolean isBlockAttachedTowardsFallback(BlockState state, World world, BlockPos pos,
		Direction direction) {
		Block block = state.getBlock();
		if (block instanceof LadderBlock)
			return state.get(LadderBlock.FACING) == direction.getOpposite();
		if (block instanceof WallTorchBlock)
			return state.get(WallTorchBlock.HORIZONTAL_FACING) == direction.getOpposite();
		if (block instanceof WallSignBlock)
			return state.get(WallSignBlock.FACING) == direction.getOpposite();
		if (block instanceof StandingSignBlock)
			return direction == Direction.DOWN;
		if (block instanceof AbstractPressurePlateBlock)
			return direction == Direction.DOWN;
		if (block instanceof DoorBlock) {
			if (state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER && direction == Direction.UP)
				return true;
			return direction == Direction.DOWN;
		}
		if (block instanceof BedBlock) {
			Direction facing = state.get(BedBlock.HORIZONTAL_FACING);
			if (state.get(BedBlock.PART) == BedPart.HEAD)
				facing = facing.getOpposite();
			return direction == facing;
		}
		if (block instanceof RedstoneLinkBlock)
			return direction.getOpposite() == state.get(RedstoneLinkBlock.FACING);
		if (block instanceof FlowerPotBlock)
			return direction == Direction.DOWN;
		if (block instanceof RedstoneDiodeBlock)
			return direction == Direction.DOWN;
		if (block instanceof RedstoneWireBlock)
			return direction == Direction.DOWN;
		if (block instanceof CarpetBlock)
			return direction == Direction.DOWN;
		if (block instanceof RedstoneWallTorchBlock)
			return state.get(RedstoneWallTorchBlock.FACING) == direction.getOpposite();
		if (block instanceof TorchBlock)
			return direction == Direction.DOWN;
		if (block instanceof HorizontalFaceBlock) {
			AttachFace attachFace = state.get(HorizontalFaceBlock.FACE);
			if (attachFace == AttachFace.CEILING)
				return direction == Direction.UP;
			if (attachFace == AttachFace.FLOOR)
				return direction == Direction.DOWN;
			if (attachFace == AttachFace.WALL)
				return direction.getOpposite() == state.get(HorizontalFaceBlock.HORIZONTAL_FACING);
		}
		if (state.contains(BlockStateProperties.HANGING))
			return direction == (state.get(BlockStateProperties.HANGING) ? Direction.UP : Direction.DOWN);
		if (block instanceof AbstractRailBlock)
			return direction == Direction.DOWN;
		if (block instanceof AttachedActorBlock)
			return direction == state.get(HarvesterBlock.HORIZONTAL_FACING)
				.getOpposite();
		if (block instanceof HandCrankBlock)
			return direction == state.get(HandCrankBlock.FACING)
				.getOpposite();
		if (block instanceof NozzleBlock)
			return direction == state.get(NozzleBlock.FACING)
				.getOpposite();
		if (block instanceof EngineBlock)
			return direction == state.get(EngineBlock.HORIZONTAL_FACING)
				.getOpposite();
		if (block instanceof BellBlock) {
			BellAttachment attachment = state.get(BlockStateProperties.BELL_ATTACHMENT);
			if (attachment == BellAttachment.FLOOR)
				return direction == Direction.DOWN;
			if (attachment == BellAttachment.CEILING)
				return direction == Direction.UP;
			return direction == state.get(HorizontalBlock.HORIZONTAL_FACING);
		}
		if (state.getBlock() instanceof SailBlock)
			return direction.getAxis() != state.get(SailBlock.FACING)
				.getAxis();
		if (state.getBlock() instanceof FluidTankBlock)
			return FluidTankConnectivityHandler.isConnected(world, pos, pos.offset(direction));
		if (AllBlocks.STICKER.has(state) && state.get(StickerBlock.EXTENDED)) {
			return direction == state.get(StickerBlock.FACING)
				&& !isNotSupportive(world.getBlockState(pos.offset(direction)), direction.getOpposite());
		}
		return false;
	}

	private static boolean isNotSupportiveFallback(BlockState state, Direction facing) {
		if (AllBlocks.MECHANICAL_DRILL.has(state))
			return state.get(BlockStateProperties.FACING) == facing;
		if (AllBlocks.MECHANICAL_BEARING.has(state))
			return state.get(BlockStateProperties.FACING) == facing;
		if (AllBlocks.CART_ASSEMBLER.has(state))
			return Direction.DOWN == facing;
		if (AllBlocks.MECHANICAL_SAW.has(state))
			return state.get(BlockStateProperties.FACING) == facing;
		if (AllBlocks.PORTABLE_STORAGE_INTERFACE.has(state))
			return state.get(PortableStorageInterfaceBlock.FACING) == facing;
		if (state.getBlock() instanceof AttachedActorBlock)
			return state.get(BlockStateProperties.HORIZONTAL_FACING) == facing;
		if (AllBlocks.ROPE_PULLEY.has(state))
			return facing == Direction.DOWN;
		if (state.getBlock() instanceof CarpetBlock)
			return facing == Direction.UP;
		if (state.getBlock() instanceof SailBlock)
			return facing.getAxis() == state.get(SailBlock.FACING)
				.getAxis();
		if (AllBlocks.PISTON_EXTENSION_POLE.has(state))
			return facing.getAxis() != state.get(BlockStateProperties.FACING)
				.getAxis();
		if (AllBlocks.MECHANICAL_PISTON_HEAD.has(state))
			return facing.getAxis() != state.get(BlockStateProperties.FACING)
				.getAxis();
		if (AllBlocks.STICKER.has(state) && !state.get(StickerBlock.EXTENDED))
			return facing == state.get(StickerBlock.FACING);
		return isBrittle(state);
	}

	// Check classes

	public static interface MovementNecessaryCheck {
		public CheckResult isMovementNecessary(BlockState state, World world, BlockPos pos);
	}

	public static interface MovementAllowedCheck {
		public CheckResult isMovementAllowed(BlockState state, World world, BlockPos pos);
	}

	public static interface BrittleCheck {
		/**
		 * Brittle blocks will be collected first, as they may break when other blocks
		 * are removed before them
		 */
		public CheckResult isBrittle(BlockState state);
	}

	public static interface AttachedCheck {
		/**
		 * Attached blocks will move if blocks they are attached to are moved
		 */
		public CheckResult isBlockAttachedTowards(BlockState state, World world, BlockPos pos, Direction direction);
	}

	public static interface NotSupportiveCheck {
		/**
		 * Non-Supportive blocks will not continue a chain of blocks picked up by e.g. a
		 * piston
		 */
		public CheckResult isNotSupportive(BlockState state, Direction direction);
	}

	public static interface AllChecks extends MovementNecessaryCheck, MovementAllowedCheck, BrittleCheck, AttachedCheck, NotSupportiveCheck {
	}

	public static enum CheckResult {
		SUCCESS,
		FAIL,
		PASS;

		public Boolean toBoolean() {
			return this == PASS ? null : (this == SUCCESS ? true : false);
		}

		public static CheckResult of(boolean b) {
			return b ? SUCCESS : FAIL;
		}

		public static CheckResult of(Boolean b) {
			return b == null ? PASS : (b ? SUCCESS : FAIL);
		}
	}

}
