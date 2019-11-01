package com.simibubi.create.modules.contraptions.receivers.constructs;

import static com.simibubi.create.CreateConfig.parameters;
import static com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalPistonBlock.STATE;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.Create;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.receivers.constructs.IHaveMovementBehavior.MovementContext;
import com.simibubi.create.modules.contraptions.receivers.constructs.IHaveMovementBehavior.MoverType;
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalPistonBlock.PistonState;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MechanicalPistonTileEntity extends KineticTileEntity {

	protected PistonContraption movedContraption;
	protected float offset;
	protected boolean running;
	protected boolean assembleNextTick;
	protected boolean hadCollisionWithOtherPiston;

	public MechanicalPistonTileEntity() {
		super(AllTileEntities.MECHANICAL_PISTON.type);
	}

	@Override
	public void onSpeedChanged() {
		super.onSpeedChanged();
		assembleNextTick = true;
	}

	@Override
	public void remove() {
		this.removed = true;
		if (!world.isRemote)
			disassembleConstruct();
		super.remove();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return super.getMaxRenderDistanceSquared() * 16;
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putBoolean("Running", running);
		tag.putFloat("Offset", offset);
		if (running && !PistonContraption.isFrozen())
			tag.put("Construct", movedContraption.writeNBT());

		return super.write(tag);
	}

	@Override
	public void read(CompoundNBT tag) {
		running = tag.getBoolean("Running");
		offset = tag.getFloat("Offset");
		if (running && !PistonContraption.isFrozen()) {
			movedContraption = new PistonContraption();
			movedContraption.readNBT(tag.getCompound("Construct"));
			for (MutablePair<BlockInfo, MovementContext> pair : movedContraption.getActors()) {
				MovementContext context = new MovementContext(pair.left.state, MoverType.PISTON);
				context.world = world;
				Direction direction = getBlockState().get(BlockStateProperties.FACING);
				context.motion = new Vec3d(direction.getDirectionVec()).scale(getMovementSpeed()).normalize();
				context.currentGridPos = pair.left.pos.offset(direction, getModulatedOffset(offset));
				pair.setRight(context);
			}
		}

		super.read(tag);
	}

	protected void onBlockVisited(float newOffset) {
		if (PistonContraption.isFrozen())
			return;

		Direction direction = getBlockState().get(BlockStateProperties.FACING);

		for (MutablePair<BlockInfo, MovementContext> pair : movedContraption.getActors()) {
			BlockInfo block = pair.left;
			MovementContext context = pair.right;

			BlockPos newPos = block.pos.offset(direction, getModulatedOffset(newOffset));
			context.currentGridPos = newPos;

			IHaveMovementBehavior actor = (IHaveMovementBehavior) block.state.getBlock();
			actor.visitPosition(context);
		}

	}

	public void assembleConstruct() {
		Direction direction = getBlockState().get(BlockStateProperties.FACING);

		// Collect Construct
		movedContraption = PistonContraption.movePistonAt(world, pos, direction, getMovementSpeed() < 0);
		if (movedContraption == null)
			return;

		// Check if not at limit already
		float resultingOffset = movedContraption.initialExtensionProgress + getMovementSpeed();
		if (resultingOffset <= 0 || resultingOffset >= movedContraption.extensionLength) {
			movedContraption = null;
			return;
		}
		if (hasBlockCollisions(resultingOffset + .5f)) {
			movedContraption = null;
			return;
		}

		// Run
		running = true;
		offset = movedContraption.initialExtensionProgress;
		if (!world.isRemote)
			Create.constructHandler.add(this);

		sendData();
		getWorld().setBlockState(pos, getBlockState().with(MechanicalPistonBlock.STATE, PistonState.MOVING), 66);
		for (BlockInfo block : movedContraption.blocks.values()) {
			BlockPos startPos = block.pos.offset(direction, movedContraption.initialExtensionProgress);
			if (startPos.equals(pos))
				continue;
			getWorld().setBlockState(startPos, Blocks.AIR.getDefaultState(), 67);
		}

		for (MutablePair<BlockInfo, MovementContext> pair : movedContraption.getActors()) {
			MovementContext context = new MovementContext(pair.left.state, MoverType.PISTON);
			context.world = world;
			context.motion = new Vec3d(direction.getDirectionVec()).scale(getMovementSpeed()).normalize();
			context.currentGridPos = pair.left.pos.offset(direction, getModulatedOffset(offset));
			pair.setRight(context);
		}

		onBlockVisited(offset);
	}

	public void disassembleConstruct() {
		if (!running)
			return;

		Direction direction = getBlockState().get(BlockStateProperties.FACING);
		if (!removed)
			getWorld().setBlockState(pos, getBlockState().with(MechanicalPistonBlock.STATE, PistonState.EXTENDED), 3);
		movedContraption.disassemble(world, BlockPos.ZERO.offset(direction, getModulatedOffset(offset)),
				(targetPos, state) -> {
					if (targetPos.equals(pos)) {
						if (!AllBlocks.PISTON_POLE.typeOf(state) && !removed)
							world.setBlockState(pos, getBlockState().with(STATE, PistonState.RETRACTED), 3);
						return true;
					}
					return false;
				});
		running = false;
		if (!world.isRemote)
			Create.constructHandler.remove(this);
		movedContraption = null;
		sendData();

		if (removed)
			AllBlocks.MECHANICAL_PISTON.get().onBlockHarvested(world, pos, getBlockState(), null);
	}

	@Override
	public void tick() {
		super.tick();
		
		if (!world.isRemote && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				if (speed == 0)
					disassembleConstruct();
				else {
					for (MutablePair<BlockInfo, MovementContext> pair : movedContraption.getActors())
						pair.right.motion = new Vec3d(
								getBlockState().get(BlockStateProperties.FACING).getDirectionVec())
										.scale(getMovementSpeed());
					sendData();
				}
				return;
			}
			assembleConstruct();
			return;
		}

		if (!running)
			return;

		float movementSpeed = getMovementSpeed();
		Direction movementDirection = getBlockState().get(BlockStateProperties.FACING);
		float newOffset = offset + movementSpeed;

		MovingConstructHandler.moveEntities(this, movementSpeed, movementDirection, newOffset);

		if (world.isRemote) {
			offset = newOffset;
			return;
		}

		if (getModulatedOffset(newOffset) != getModulatedOffset(offset)) {
			onBlockVisited(newOffset);
		}

		float movement = .5f + (movementSpeed < 0 ? -1f : 0);
		if (getModulatedOffset(newOffset + movement) != getModulatedOffset(offset + movement)) {
			if (hasBlockCollisions(newOffset + movement)) {
				disassembleConstruct();
				if (hadCollisionWithOtherPiston)
					hadCollisionWithOtherPiston = false;
				else if (movementSpeed > 0)
					assembleNextTick = true;
				return;
			}
		}

		offset = newOffset;

		if (offset <= 0 || offset >= movedContraption.extensionLength) {
			disassembleConstruct();
			return;
		}
	}

	private boolean hasBlockCollisions(float newOffset) {
		if (PistonContraption.isFrozen())
			return true;

		Direction movementDirection = getBlockState().get(BlockStateProperties.FACING);
		BlockPos relativePos = BlockPos.ZERO.offset(movementDirection, getModulatedOffset(newOffset));

		// Other moving Pistons
		int maxPossibleRange = parameters.maxPistonPoles.get() + parameters.maxChassisRange.get()
				+ parameters.maxChassisForTranslation.get();
		Iterator<MechanicalPistonTileEntity> iterator = Create.constructHandler.getOtherMovingPistonsInWorld(this)
				.iterator();
		pistonLoop: while (iterator.hasNext()) {
			MechanicalPistonTileEntity otherPiston = iterator.next();

			if (otherPiston == this)
				continue;
			if (!otherPiston.running || otherPiston.movedContraption == null) {
				iterator.remove();
				continue;
			}
			if (otherPiston.pos.manhattanDistance(pos) > maxPossibleRange * 2)
				continue;

			Direction otherMovementDirection = otherPiston.getBlockState().get(BlockStateProperties.FACING);
			BlockPos otherRelativePos = BlockPos.ZERO.offset(otherMovementDirection,
					getModulatedOffset(otherPiston.offset));

			for (AxisAlignedBB tBB : Arrays.asList(movedContraption.constructCollisionBox,
					movedContraption.pistonCollisionBox)) {
				for (AxisAlignedBB oBB : Arrays.asList(otherPiston.movedContraption.constructCollisionBox,
						otherPiston.movedContraption.pistonCollisionBox)) {
					if (tBB == null || oBB == null)
						continue;

					boolean frontalCollision = otherMovementDirection == movementDirection.getOpposite();
					BlockPos thisColliderOffset = relativePos.offset(movementDirection,
							frontalCollision ? (getMovementSpeed() > 0 ? 1 : -1) : 0);
					AxisAlignedBB thisBB = tBB.offset(thisColliderOffset);
					AxisAlignedBB otherBB = oBB.offset(otherRelativePos);

					if (thisBB.intersects(otherBB)) {
						boolean actuallyColliding = false;
						for (BlockPos colliderPos : movedContraption.getColliders(world, movementDirection)) {
							colliderPos = colliderPos.add(thisColliderOffset).subtract(otherRelativePos);
							if (!otherPiston.movedContraption.blocks.containsKey(colliderPos))
								continue;
							actuallyColliding = true;
						}
						if (!actuallyColliding)
							continue pistonLoop;
						hadCollisionWithOtherPiston = true;
						return true;
					}

				}
			}

		}

		if (!running)
			return false;

		// Other Blocks in world
		for (BlockPos pos : movedContraption.getColliders(world,
				getMovementSpeed() > 0 ? movementDirection : movementDirection.getOpposite())) {
			BlockPos colliderPos = pos.add(relativePos);

			if (!world.isBlockPresent(colliderPos))
				return true;
			if (!world.getBlockState(colliderPos).getMaterial().isReplaceable()
					&& !world.getBlockState(colliderPos).getCollisionShape(world, colliderPos).isEmpty())
				return true;
		}

		return false;
	}

	private int getModulatedOffset(float offset) {
		return MathHelper.clamp((int) (offset + .5f), 0, movedContraption.extensionLength);
	}

	public float getMovementSpeed() {
		Direction pistonDirection = getBlockState().get(BlockStateProperties.FACING);
		int movementModifier = pistonDirection.getAxisDirection().getOffset()
				* (pistonDirection.getAxis() == Axis.Z ? -1 : 1);
		return getSpeed() * -movementModifier / 1024f;
	}

	public Vec3d getConstructOffset(float partialTicks) {
		float interpolatedOffset = MathHelper.clamp(offset + (partialTicks - .5f) * getMovementSpeed(), 0,
				movedContraption.extensionLength);
		return new Vec3d(getBlockState().get(BlockStateProperties.FACING).getDirectionVec()).scale(interpolatedOffset);
	}

}
