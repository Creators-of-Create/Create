package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import static com.simibubi.create.AllBlocks.MECHANICAL_PISTON_HEAD;
import static com.simibubi.create.AllBlocks.PISTON_EXTENSION_POLE;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isExtensionPole;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isPiston;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isPistonHead;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isStickyPiston;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionType;
import com.simibubi.create.content.contraptions.components.structureMovement.TranslatingContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionLighter;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PistonContraption extends TranslatingContraption {

	protected int extensionLength;
	protected int initialExtensionProgress;
	protected Direction orientation;

	private AABB pistonExtensionCollisionBox;
	private boolean retract;

	@Override
	protected ContraptionType getType() {
		return ContraptionType.PISTON;
	}

	public PistonContraption() {}

	public PistonContraption(Direction direction, boolean retract) {
		orientation = direction;
		this.retract = retract;
	}

	@Override
	public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
		if (!collectExtensions(world, pos, orientation))
			return false;
		int count = blocks.size();
		if (!searchMovedStructure(world, anchor, retract ? orientation.getOpposite() : orientation))
			return false;
		if (blocks.size() == count) { // no new blocks added
			bounds = pistonExtensionCollisionBox;
		} else {
			bounds = bounds.minmax(pistonExtensionCollisionBox);
		}
		startMoving(world);
		return true;
	}

	private boolean collectExtensions(Level world, BlockPos pos, Direction direction) throws AssemblyException {
		List<StructureBlockInfo> poles = new ArrayList<>();
		BlockPos actualStart = pos;
		BlockState nextBlock = world.getBlockState(actualStart.relative(direction));
		int extensionsInFront = 0;
		BlockState blockState = world.getBlockState(pos);
		boolean sticky = isStickyPiston(blockState);

		if (!isPiston(blockState))
			return false;

		if (blockState.getValue(MechanicalPistonBlock.STATE) == PistonState.EXTENDED) {
			while (PistonExtensionPoleBlock.PlacementHelper.get().matchesAxis(nextBlock, direction.getAxis()) || isPistonHead(nextBlock) && nextBlock.getValue(FACING) == direction) {

				actualStart = actualStart.relative(direction);
				poles.add(new StructureBlockInfo(actualStart, nextBlock.setValue(FACING, direction), null));
				extensionsInFront++;

				if (isPistonHead(nextBlock))
					break;

				nextBlock = world.getBlockState(actualStart.relative(direction));
				if (extensionsInFront > MechanicalPistonBlock.maxAllowedPistonPoles())
					throw AssemblyException.tooManyPistonPoles();
			}
		}

		if (extensionsInFront == 0)
			poles.add(new StructureBlockInfo(pos, MECHANICAL_PISTON_HEAD.getDefaultState()
				.setValue(FACING, direction)
				.setValue(BlockStateProperties.PISTON_TYPE, sticky ? PistonType.STICKY : PistonType.DEFAULT), null));
		else
			poles.add(new StructureBlockInfo(pos, PISTON_EXTENSION_POLE.getDefaultState()
				.setValue(FACING, direction), null));

		BlockPos end = pos;
		nextBlock = world.getBlockState(end.relative(direction.getOpposite()));
		int extensionsInBack = 0;

		while (PistonExtensionPoleBlock.PlacementHelper.get().matchesAxis(nextBlock, direction.getAxis())) {
			end = end.relative(direction.getOpposite());
			poles.add(new StructureBlockInfo(end, nextBlock.setValue(FACING, direction), null));
			extensionsInBack++;
			nextBlock = world.getBlockState(end.relative(direction.getOpposite()));

			if (extensionsInFront + extensionsInBack > MechanicalPistonBlock.maxAllowedPistonPoles())
				throw AssemblyException.tooManyPistonPoles();
		}

		anchor = pos.relative(direction, initialExtensionProgress + 1);
		extensionLength = extensionsInBack + extensionsInFront;
		initialExtensionProgress = extensionsInFront;
		pistonExtensionCollisionBox = new AABB(
				BlockPos.ZERO.relative(direction, -1),
				BlockPos.ZERO.relative(direction, -extensionLength - 1)).expandTowards(1,
						1, 1);

		if (extensionLength == 0)
			throw AssemblyException.noPistonPoles();

		bounds = new AABB(0, 0, 0, 0, 0, 0);

		for (StructureBlockInfo pole : poles) {
			BlockPos relPos = pole.pos.relative(direction, -extensionsInFront);
			BlockPos localPos = relPos.subtract(anchor);
			getBlocks().put(localPos, new StructureBlockInfo(localPos, pole.state, null));
			//pistonExtensionCollisionBox = pistonExtensionCollisionBox.union(new AABB(localPos));
		}

		return true;
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pistonExtensionCollisionBox.contains(VecHelper.getCenterOf(pos.subtract(anchor)));
	}

	@Override
	protected boolean addToInitialFrontier(Level world, BlockPos pos, Direction direction, Queue<BlockPos> frontier) throws AssemblyException {
		frontier.clear();
		boolean sticky = isStickyPiston(world.getBlockState(pos.relative(orientation, -1)));
		boolean retracting = direction != orientation;
		if (retracting && !sticky)
			return true;
		for (int offset = 0; offset <= AllConfigs.server().kinetics.maxChassisRange.get(); offset++) {
			if (offset == 1 && retracting)
				return true;
			BlockPos currentPos = pos.relative(orientation, offset + initialExtensionProgress);
			if (retracting && world.isOutsideBuildHeight(currentPos))
				return true;
			if (!world.isLoaded(currentPos))
				throw AssemblyException.unloadedChunk(currentPos);
			BlockState state = world.getBlockState(currentPos);
			if (!BlockMovementChecks.isMovementNecessary(state, world, currentPos))
				return true;
			if (BlockMovementChecks.isBrittle(state) && !(state.getBlock() instanceof WoolCarpetBlock))
				return true;
			if (isPistonHead(state) && state.getValue(FACING) == direction.getOpposite())
				return true;
			if (!BlockMovementChecks.isMovementAllowed(state, world, currentPos))
				if (retracting)
					return true;
				else
					throw AssemblyException.unmovableBlock(currentPos, state);
			if (retracting && state.getPistonPushReaction() == PushReaction.PUSH_ONLY)
				return true;
			frontier.add(currentPos);
			if (BlockMovementChecks.isNotSupportive(state, orientation))
				return true;
		}
		return true;
	}

	@Override
	public void addBlock(BlockPos pos, Pair<StructureBlockInfo, BlockEntity> capture) {
		super.addBlock(pos.relative(orientation, -initialExtensionProgress), capture);
	}

	@Override
	public BlockPos toLocalPos(BlockPos globalPos) {
		return globalPos.subtract(anchor)
			.relative(orientation, -initialExtensionProgress);
	}

	@Override
	protected boolean customBlockPlacement(LevelAccessor world, BlockPos pos, BlockState state) {
		BlockPos pistonPos = anchor.relative(orientation, -1);
		BlockState pistonState = world.getBlockState(pistonPos);
		BlockEntity be = world.getBlockEntity(pistonPos);
		if (pos.equals(pistonPos)) {
			if (be == null || be.isRemoved())
				return true;
			if (!isExtensionPole(state) && isPiston(pistonState))
				world.setBlock(pistonPos, pistonState.setValue(MechanicalPistonBlock.STATE, PistonState.RETRACTED),
					3 | 16);
			return true;
		}
		return false;
	}

	@Override
	protected boolean customBlockRemoval(LevelAccessor world, BlockPos pos, BlockState state) {
		BlockPos pistonPos = anchor.relative(orientation, -1);
		BlockState blockState = world.getBlockState(pos);
		if (pos.equals(pistonPos) && isPiston(blockState)) {
			world.setBlock(pos, blockState.setValue(MechanicalPistonBlock.STATE, PistonState.MOVING), 66 | 16);
			return true;
		}
		return false;
	}

	@Override
	public void readNBT(Level world, CompoundTag nbt, boolean spawnData) {
		super.readNBT(world, nbt, spawnData);
		initialExtensionProgress = nbt.getInt("InitialLength");
		extensionLength = nbt.getInt("ExtensionLength");
		orientation = Direction.from3DDataValue(nbt.getInt("Orientation"));
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		tag.putInt("InitialLength", initialExtensionProgress);
		tag.putInt("ExtensionLength", extensionLength);
		tag.putInt("Orientation", orientation.get3DDataValue());
		return tag;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public ContraptionLighter<?> makeLighter() {
		return new PistonLighter(this);
	}
}
