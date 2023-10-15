package com.simibubi.create.content.contraptions.elevator;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.elevator.ElevatorColumn.ColumnCoords;
import com.simibubi.create.content.contraptions.pulley.PulleyBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ElevatorPulleyBlockEntity extends PulleyBlockEntity {

	private float prevSpeed;
	private boolean arrived;
	private int clientOffsetTarget;
	private boolean initialOffsetReceived;

	public ElevatorPulleyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		prevSpeed = 0;
		arrived = true;
		initialOffsetReceived = false;
	}

	private int getTargetOffset() {
		if (level.isClientSide)
			return clientOffsetTarget;
		if (movedContraption == null || !(movedContraption.getContraption()instanceof ElevatorContraption ec))
			return (int) offset;

		Integer target = ec.getCurrentTargetY(level);
		if (target == null)
			return (int) offset;

		return worldPosition.getY() - target + ec.contactYOffset - 1;
	}

	@Override
	public void attach(ControlledContraptionEntity contraption) {
		super.attach(contraption);
		if (offset >= 0)
			resetContraptionToOffset();
		if (level.isClientSide) {
			AllPackets.getChannel().sendToServer(new ElevatorFloorListPacket.RequestFloorList(contraption));
			return;
		}

		if (contraption.getContraption()instanceof ElevatorContraption ec)
			ElevatorColumn.getOrCreate(level, ec.getGlobalColumn())
				.setActive(true);
	}

	@Override
	public void tick() {
		boolean wasArrived = arrived;
		super.tick();

		if (movedContraption == null)
			return;
		if (!(movedContraption.getContraption()instanceof ElevatorContraption ec))
			return;
		if (level.isClientSide())
			ec.setClientYTarget(worldPosition.getY() - clientOffsetTarget + ec.contactYOffset - 1);

		waitingForSpeedChange = false;
		ec.arrived = wasArrived;

		if (!arrived)
			return;

		double y = movedContraption.getY();
		int targetLevel = Mth.floor(0.5f + y) + ec.contactYOffset;

		Integer ecCurrentTargetY = ec.getCurrentTargetY(level);
		if (ecCurrentTargetY != null)
			targetLevel = ecCurrentTargetY;
		if (level.isClientSide())
			targetLevel = ec.clientYTarget;
		if (!wasArrived && !level.isClientSide()) {
			triggerContact(ec, targetLevel - ec.contactYOffset);
			AllSoundEvents.CONTRAPTION_DISASSEMBLE.play(level, null, worldPosition.below((int) offset), 0.75f, 0.8f);
		}

		double diff = targetLevel - y - ec.contactYOffset;
		if (Math.abs(diff) > 1f / 128)
			diff *= 0.25f;
		movedContraption.setPos(movedContraption.position()
			.add(0, diff, 0));
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (level.isClientSide() || !arrived)
			return;
		if (movedContraption == null || !movedContraption.isAlive())
			return;
		if (!(movedContraption.getContraption()instanceof ElevatorContraption ec))
			return;
		if (getTargetOffset() != (int) offset)
			return;

		double y = movedContraption.getY();
		int targetLevel = Mth.floor(0.5f + y);
		triggerContact(ec, targetLevel);
	}

	private void triggerContact(ElevatorContraption ec, int targetLevel) {
		ColumnCoords coords = ec.getGlobalColumn();
		ElevatorColumn column = ElevatorColumn.get(level, coords);
		if (column == null)
			return;

		BlockPos contactPos = column.contactAt(targetLevel + ec.contactYOffset);
		if (!level.isLoaded(contactPos))
			return;
		BlockState contactState = level.getBlockState(contactPos);
		if (!AllBlocks.ELEVATOR_CONTACT.has(contactState))
			return;
		if (contactState.getValue(ElevatorContactBlock.POWERING))
			return;

		ElevatorContactBlock ecb = AllBlocks.ELEVATOR_CONTACT.get();
		ecb.withBlockEntityDo(level, contactPos, be -> be.activateBlock = true);
		ecb.scheduleActivation(level, contactPos);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		if (clientPacket)
			compound.putInt("ClientTarget", clientOffsetTarget);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		if (!clientPacket)
			return;

		clientOffsetTarget = compound.getInt("ClientTarget");
		if (initialOffsetReceived)
			return;

		offset = compound.getFloat("Offset");
		initialOffsetReceived = true;
		resetContraptionToOffset();
	}

	@Override
	public float getMovementSpeed() {
		int currentTarget = getTargetOffset();

		if (!level.isClientSide() && currentTarget != clientOffsetTarget) {
			clientOffsetTarget = currentTarget;
			sendData();
		}

		float diff = currentTarget - offset;
		float movementSpeed = Mth.clamp(convertToLinear(getSpeed() * 2), -1.99f, 1.99f);
		float rpmLimit = Math.abs(movementSpeed);

		float configacc = Mth.lerp(Math.abs(movementSpeed), 0.0075f, 0.0175f);
		float decelleration = (float) Math.sqrt(2 * Math.abs(diff) * configacc);

		float speed = diff;
		speed = Mth.clamp(speed, -rpmLimit, rpmLimit);
		speed = Mth.clamp(speed, prevSpeed - configacc, prevSpeed + configacc);
		speed = Mth.clamp(speed, -decelleration, decelleration);

		arrived = Math.abs(diff) < 0.5f;

		if (speed > 1 / 1024f && !level.isClientSide())
			setChanged();

		return prevSpeed = speed;
	}

	@Override
	protected boolean shouldCreateRopes() {
		return false;
	}

	@Override
	public void disassemble() {
		if (movedContraption != null && movedContraption.getContraption()instanceof ElevatorContraption ec) {
			ElevatorColumn column = ElevatorColumn.get(level, ec.getGlobalColumn());
			if (column != null)
				column.setActive(false);
		}

		super.disassemble();
		offset = -1;
		sendData();
	}

	public void clicked() {
		if (isPassive() && level.getBlockEntity(mirrorParent)instanceof ElevatorPulleyBlockEntity parent) {
			parent.clicked();
			return;
		}

		if (running)
			disassemble();
		else
			assembleNextTick = true;
	}

	@Override
	protected boolean moveAndCollideContraption() {
		if (arrived)
			return false;
		super.moveAndCollideContraption();
		return false;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		registerAwardables(behaviours, AllAdvancements.CONTRAPTION_ACTORS);
	}

	@Override
	protected void assemble() throws AssemblyException {
		if (!(level.getBlockState(worldPosition)
			.getBlock() instanceof ElevatorPulleyBlock))
			return;
		if (getSpeed() == 0)
			return;

		int maxLength = AllConfigs.server().kinetics.maxRopeLength.get();
		int i = 1;
		while (i <= maxLength) {
			BlockPos ropePos = worldPosition.below(i);
			BlockState ropeState = level.getBlockState(ropePos);
			if (!ropeState.getCollisionShape(level, ropePos)
				.isEmpty()
				&& !ropeState.getMaterial()
					.isReplaceable()) {
				break;
			}
			++i;
		}

		offset = i - 1;
		forceMove = true;

		// Collect Construct
		if (!level.isClientSide && mirrorParent == null) {
			needsContraption = false;
			BlockPos anchor = worldPosition.below(Mth.floor(offset + 1));
			offset = Mth.floor(offset);
			ElevatorContraption contraption = new ElevatorContraption((int) offset);

			float offsetOnSucess = offset;
			offset = 0;

			boolean canAssembleStructure = contraption.assemble(level, anchor);
			if (!canAssembleStructure && getSpeed() > 0)
				return;

			if (!contraption.getBlocks()
				.isEmpty()) {
				offset = offsetOnSucess;
				contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
				movedContraption = ControlledContraptionEntity.create(level, this, contraption);
				movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
				contraption.maxContactY = worldPosition.getY() + contraption.contactYOffset - 1;
				contraption.minContactY = contraption.maxContactY - maxLength;
				level.addFreshEntity(movedContraption);
				forceMove = true;
				needsContraption = true;

				if (contraption.containsBlockBreakers())
					award(AllAdvancements.CONTRAPTION_ACTORS);

				for (BlockPos pos : contraption.createColliders(level, Direction.UP)) {
					if (pos.getY() != 0)
						continue;
					pos = pos.offset(anchor);
					if (level.getBlockEntity(new BlockPos(pos.getX(), worldPosition.getY(),
						pos.getZ())) instanceof ElevatorPulleyBlockEntity pbe)
						pbe.startMirroringOther(worldPosition);
				}

				ElevatorColumn column = ElevatorColumn.getOrCreate(level, contraption.getGlobalColumn());
				int target = (int) (worldPosition.getY() + contraption.contactYOffset - 1 - offset);
				column.target(target);
				column.gatherAll();
				column.setActive(true);
				column.markDirty();

				contraption.broadcastFloorData(level, column.contactAt(target));
				clientOffsetTarget = column.getTargetedYLevel();
				arrived = true;
			}
		}

		clientOffsetDiff = 0;
		running = true;
		sendData();
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		setChanged();
	}

	@Override
	protected MovementMode getMovementMode() {
		return MovementMode.MOVE_NEVER_PLACE;
	}

}
