package com.simibubi.create.content.logistics.depot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.funnel.AbstractFunnelBlock;
import com.simibubi.create.content.logistics.funnel.FunnelBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.utility.IntAttached;
import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.Pair;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.animation.LerpedFloat.Chaser;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public class EjectorBlockEntity extends KineticBlockEntity {

	List<IntAttached<ItemStack>> launchedItems;
	ScrollValueBehaviour maxStackSize;
	DepotBehaviour depotBehaviour;
	EntityLauncher launcher;
	LerpedFloat lidProgress;
	boolean powered;
	boolean launch;
	State state;

	// item collision
	@Nullable
	Pair<Vec3, BlockPos> earlyTarget;
	float earlyTargetTime;
	// runtime stuff
	int scanCooldown;
	ItemStack trackedItem;

	public enum State {
		CHARGED, LAUNCHING, RETRACTING;
	}

	public EjectorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		launcher = new EntityLauncher(1, 0);
		lidProgress = LerpedFloat.linear()
			.startWithValue(1);
		this.state = State.RETRACTING;
		launchedItems = new ArrayList<>();
		powered = false;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.add(depotBehaviour = new DepotBehaviour(this));

		maxStackSize =
			new ScrollValueBehaviour(CreateLang.translateDirect("weighted_ejector.stack_size"), this, new EjectorSlot())
				.between(0, 64)
				.withFormatter(i -> i == 0 ? "*" : String.valueOf(i));
		behaviours.add(maxStackSize);

		depotBehaviour.maxStackSize = () -> maxStackSize.getValue();
		depotBehaviour.canAcceptItems = () -> state == State.CHARGED;
		depotBehaviour.canFunnelsPullFrom = side -> side != getFacing();
		depotBehaviour.enableMerging();
		depotBehaviour.addSubBehaviours(behaviours);
	}

	@Override
	public void initialize() {
		super.initialize();
		updateSignal();
	}

	public void activate() {
		launch = true;
		nudgeEntities();
	}

	protected boolean cannotLaunch() {
		return state != State.CHARGED && !(level.isClientSide && state == State.LAUNCHING);
	}

	public void activateDeferred() {
		if (cannotLaunch())
			return;
		Direction facing = getFacing();
		List<Entity> entities =
			level.getEntitiesOfClass(Entity.class, new AABB(worldPosition).inflate(-1 / 16f, 0, -1 / 16f));

		// Launch Items
		boolean doLogic = !level.isClientSide || isVirtual();
		if (doLogic)
			launchItems();

		// Launch Entities
		for (Entity entity : entities) {
			boolean isPlayerEntity = entity instanceof Player;
			if (!entity.isAlive())
				continue;
			if (entity instanceof ItemEntity)
				continue;
			if (entity.getPistonPushReaction() == PushReaction.IGNORE)
				continue;

			entity.setOnGround(false);

			if (isPlayerEntity != level.isClientSide)
				continue;

			entity.setPos(worldPosition.getX() + .5f, worldPosition.getY() + 1, worldPosition.getZ() + .5f);
			launcher.applyMotion(entity, facing);

			if (!isPlayerEntity)
				continue;

			Player playerEntity = (Player) entity;

			if (launcher.getHorizontalDistance() * launcher.getHorizontalDistance()
				+ launcher.getVerticalDistance() * launcher.getVerticalDistance() >= 25 * 25)
				AllPackets.getChannel()
					.sendToServer(new EjectorAwardPacket(worldPosition));

			if (!(playerEntity.getItemBySlot(EquipmentSlot.CHEST)
				.getItem() instanceof ElytraItem))
				continue;

			playerEntity.setXRot(-35);
			playerEntity.setYRot(facing.toYRot());
			playerEntity.setDeltaMovement(playerEntity.getDeltaMovement()
				.scale(.75f));
			deployElytra(playerEntity);
			AllPackets.getChannel()
				.sendToServer(new EjectorElytraPacket(worldPosition));
		}

		if (doLogic) {
			lidProgress.chase(1, .8f, Chaser.EXP);
			state = State.LAUNCHING;
			if (!level.isClientSide) {
				level.playSound(null, worldPosition, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, .35f, 1f);
				level.playSound(null, worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, .1f, 1.4f);
			}
		}
	}

	public void deployElytra(Player playerEntity) {
		EntityHack.setElytraFlying(playerEntity);
	}

	protected void launchItems() {
		ItemStack heldItemStack = depotBehaviour.getHeldItemStack();
		Direction funnelFacing = getFacing().getOpposite();

		if (AbstractFunnelBlock.getFunnelFacing(level.getBlockState(worldPosition.above())) == funnelFacing) {
			DirectBeltInputBehaviour directOutput = getBehaviour(DirectBeltInputBehaviour.TYPE);

			if (depotBehaviour.heldItem != null) {
				ItemStack remainder = directOutput.tryExportingToBeltFunnel(heldItemStack, funnelFacing, false);
				if (remainder == null)
					;
				else if (remainder.isEmpty())
					depotBehaviour.removeHeldItem();
				else if (remainder.getCount() != heldItemStack.getCount())
					depotBehaviour.heldItem.stack = remainder;
			}

			for (Iterator<TransportedItemStack> iterator = depotBehaviour.incoming.iterator(); iterator.hasNext();) {
				TransportedItemStack transportedItemStack = iterator.next();
				ItemStack stack = transportedItemStack.stack;
				ItemStack remainder = directOutput.tryExportingToBeltFunnel(stack, funnelFacing, false);
				if (remainder == null)
					;
				else if (remainder.isEmpty())
					iterator.remove();
				else if (!ItemStack.isSameItem(remainder, stack))
					transportedItemStack.stack = remainder;
			}

			ItemStackHandler outputs = depotBehaviour.processingOutputBuffer;
			for (int i = 0; i < outputs.getSlots(); i++) {
				ItemStack remainder =
					directOutput.tryExportingToBeltFunnel(outputs.getStackInSlot(i), funnelFacing, false);
				if (remainder != null)
					outputs.setStackInSlot(i, remainder);
			}
			return;
		}

		if (!level.isClientSide)
			for (Direction d : Iterate.directions) {
				BlockState blockState = level.getBlockState(worldPosition.relative(d));
				if (!(blockState.getBlock() instanceof ObserverBlock))
					continue;
				if (blockState.getValue(ObserverBlock.FACING) != d.getOpposite())
					continue;
				blockState.updateShape(d.getOpposite(), blockState, level, worldPosition.relative(d), worldPosition);
			}

		if (depotBehaviour.heldItem != null) {
			addToLaunchedItems(heldItemStack);
			depotBehaviour.removeHeldItem();
		}

		for (TransportedItemStack transportedItemStack : depotBehaviour.incoming)
			addToLaunchedItems(transportedItemStack.stack);
		depotBehaviour.incoming.clear();

		ItemStackHandler outputs = depotBehaviour.processingOutputBuffer;
		for (int i = 0; i < outputs.getSlots(); i++) {
			ItemStack extractItem = outputs.extractItem(i, 64, false);
			if (!extractItem.isEmpty())
				addToLaunchedItems(extractItem);
		}
	}

	protected boolean addToLaunchedItems(ItemStack stack) {
		if ((!level.isClientSide || isVirtual()) && trackedItem == null && scanCooldown == 0) {
			scanCooldown = AllConfigs.server().kinetics.ejectorScanInterval.get();
			trackedItem = stack;
		}
		return launchedItems.add(IntAttached.withZero(stack));
	}

	protected Direction getFacing() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.WEIGHTED_EJECTOR.has(blockState))
			return Direction.UP;
		Direction facing = blockState.getValue(EjectorBlock.HORIZONTAL_FACING);
		return facing;
	}

	@Override
	public void tick() {
		super.tick();

		boolean doLogic = !level.isClientSide || isVirtual();
		State prevState = state;
		float totalTime = Math.max(3, (float) launcher.getTotalFlyingTicks());

		if (scanCooldown > 0)
			scanCooldown--;

		if (launch) {
			launch = false;
			activateDeferred();
		}

		for (Iterator<IntAttached<ItemStack>> iterator = launchedItems.iterator(); iterator.hasNext();) {
			IntAttached<ItemStack> intAttached = iterator.next();
			boolean hit = false;
			if (intAttached.getSecond() == trackedItem)
				hit = scanTrajectoryForObstacles(intAttached.getFirst());
			float maxTime = earlyTarget != null ? Math.min(earlyTargetTime, totalTime) : totalTime;
			if (hit || intAttached.exceeds((int) maxTime)) {
				placeItemAtTarget(doLogic, maxTime, intAttached);
				iterator.remove();
			}
			intAttached.increment();
		}

		if (state == State.LAUNCHING) {
			lidProgress.chase(1, .8f, Chaser.EXP);
			lidProgress.tickChaser();
			if (lidProgress.getValue() > 1 - 1 / 16f && doLogic) {
				state = State.RETRACTING;
				lidProgress.setValue(1);
			}
		}

		if (state == State.CHARGED) {
			lidProgress.setValue(0);
			lidProgress.updateChaseSpeed(0);
			if (doLogic)
				ejectIfTriggered();
		}

		if (state == State.RETRACTING) {
			if (lidProgress.getChaseTarget() == 1 && !lidProgress.settled()) {
				lidProgress.tickChaser();
			} else {
				lidProgress.updateChaseTarget(0);
				lidProgress.updateChaseSpeed(0);
				if (lidProgress.getValue() == 0 && doLogic) {
					state = State.CHARGED;
					lidProgress.setValue(0);
					sendData();
				}

				float value = Mth.clamp(lidProgress.getValue() - getWindUpSpeed(), 0, 1);
				lidProgress.setValue(value);

				int soundRate = (int) (1 / (getWindUpSpeed() * 5)) + 1;
				float volume = .125f;
				float pitch = 1.5f - lidProgress.getValue();
				if (((int) level.getGameTime()) % soundRate == 0 && doLogic)
					level.playSound(null, worldPosition, SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundSource.BLOCKS,
						volume, pitch);
			}
		}

		if (state != prevState)
			notifyUpdate();
	}

	private boolean scanTrajectoryForObstacles(int time) {
		if (time <= 2)
			return false;

		Vec3 source = getLaunchedItemLocation(time);
		Vec3 target = getLaunchedItemLocation(time + 1);

		BlockHitResult rayTraceBlocks = level.clip(new ClipContext(source, target, Block.COLLIDER, Fluid.NONE, null));
		boolean miss = rayTraceBlocks.getType() == Type.MISS;

		if (!miss && rayTraceBlocks.getType() == Type.BLOCK) {
			BlockState blockState = level.getBlockState(rayTraceBlocks.getBlockPos());
			if (FunnelBlock.isFunnel(blockState) && blockState.hasProperty(FunnelBlock.EXTRACTING)
				&& blockState.getValue(FunnelBlock.EXTRACTING))
				miss = true;
		}

		if (miss) {
			if (earlyTarget != null && earlyTargetTime < time + 1) {
				earlyTarget = null;
				earlyTargetTime = 0;
			}
			return false;
		}

		Vec3 vec = rayTraceBlocks.getLocation();
		earlyTarget = Pair.of(vec.add(Vec3.atLowerCornerOf(rayTraceBlocks.getDirection()
			.getNormal())
			.scale(.25f)), rayTraceBlocks.getBlockPos());
		earlyTargetTime = (float) (time + (source.distanceTo(vec) / source.distanceTo(target)));
		sendData();
		return true;
	}

	protected void nudgeEntities() {
		for (Entity entity : level.getEntitiesOfClass(Entity.class,
			new AABB(worldPosition).inflate(-1 / 16f, 0, -1 / 16f))) {
			if (!entity.isAlive())
				continue;
			if (entity.getPistonPushReaction() == PushReaction.IGNORE)
				continue;
			if (!(entity instanceof Player))
				entity.setPos(entity.getX(), entity.getY() + .125f, entity.getZ());
		}
	}

	protected void ejectIfTriggered() {
		if (powered)
			return;
		int presentStackSize = depotBehaviour.getPresentStackSize();
		if (presentStackSize == 0)
			return;
		if (presentStackSize < maxStackSize.getValue())
			return;
		if (depotBehaviour.heldItem != null && depotBehaviour.heldItem.beltPosition < .49f)
			return;

		Direction funnelFacing = getFacing().getOpposite();
		ItemStack held = depotBehaviour.getHeldItemStack();
		if (AbstractFunnelBlock.getFunnelFacing(level.getBlockState(worldPosition.above())) == funnelFacing) {
			DirectBeltInputBehaviour directOutput = getBehaviour(DirectBeltInputBehaviour.TYPE);
			if (depotBehaviour.heldItem != null) {
				ItemStack tryFunnel = directOutput.tryExportingToBeltFunnel(held, funnelFacing, true);
				if (tryFunnel == null || !tryFunnel.isEmpty())
					return;
			}
		}

		DirectBeltInputBehaviour targetOpenInv = getTargetOpenInv();

		// Do not eject if target cannot accept held item
		if (targetOpenInv != null && depotBehaviour.heldItem != null
			&& targetOpenInv.handleInsertion(held, Direction.UP, true)
				.getCount() == held.getCount())
			return;

		activate();
		notifyUpdate();
	}

	protected void placeItemAtTarget(boolean doLogic, float maxTime, IntAttached<ItemStack> intAttached) {
		if (!doLogic)
			return;
		if (intAttached.getSecond() == trackedItem)
			trackedItem = null;

		DirectBeltInputBehaviour targetOpenInv = getTargetOpenInv();
		if (targetOpenInv != null) {
			ItemStack remainder = targetOpenInv.handleInsertion(intAttached.getValue(), Direction.UP, false);
			intAttached.setSecond(remainder);
		}

		if (intAttached.getValue()
			.isEmpty())
			return;

		Vec3 ejectVec = earlyTarget != null ? earlyTarget.getFirst() : getLaunchedItemLocation(maxTime);
		Vec3 ejectMotionVec = getLaunchedItemMotion(maxTime);
		ItemEntity item = new ItemEntity(level, ejectVec.x, ejectVec.y, ejectVec.z, intAttached.getValue());
		item.setDeltaMovement(ejectMotionVec);
		item.setDefaultPickUpDelay();
		level.addFreshEntity(item);
	}

	public DirectBeltInputBehaviour getTargetOpenInv() {
		BlockPos targetPos = earlyTarget != null ? earlyTarget.getSecond()
			: worldPosition.above(launcher.getVerticalDistance())
				.relative(getFacing(), Math.max(1, launcher.getHorizontalDistance()));
		return BlockEntityBehaviour.get(level, targetPos, DirectBeltInputBehaviour.TYPE);
	}

	public Vec3 getLaunchedItemLocation(float time) {
		return launcher.getGlobalPos(time, getFacing().getOpposite(), worldPosition);
	}

	public Vec3 getLaunchedItemMotion(float time) {
		return launcher.getGlobalVelocity(time, getFacing().getOpposite(), worldPosition)
			.scale(.5f);
	}

	@Override
	public void destroy() {
		super.destroy();
		dropFlyingItems();
	}

	public void dropFlyingItems() {
		for (IntAttached<ItemStack> intAttached : launchedItems) {
			Vec3 ejectVec = getLaunchedItemLocation(intAttached.getFirst());
			Vec3 ejectMotionVec = getLaunchedItemMotion(intAttached.getFirst());
			ItemEntity item = new ItemEntity(level, 0, 0, 0, intAttached.getValue());
			item.setPosRaw(ejectVec.x, ejectVec.y, ejectVec.z);
			item.setDeltaMovement(ejectMotionVec);
			item.setDefaultPickUpDelay();
			level.addFreshEntity(item);
		}
		launchedItems.clear();
	}

	public float getWindUpSpeed() {
		int hd = launcher.getHorizontalDistance();
		int vd = launcher.getVerticalDistance();

		float speedFactor = Math.abs(getSpeed()) / 256f;
		float distanceFactor;
		if (hd == 0 && vd == 0)
			distanceFactor = 1;
		else
			distanceFactor = 1 * Mth.sqrt(hd * hd + vd * vd);
		return speedFactor / distanceFactor;
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("HorizontalDistance", launcher.getHorizontalDistance());
		compound.putInt("VerticalDistance", launcher.getVerticalDistance());
		compound.putBoolean("Powered", powered);
		NBTHelper.writeEnum(compound, "State", state);
		compound.put("Lid", lidProgress.writeNBT());
		compound.put("LaunchedItems",
			NBTHelper.writeCompoundList(launchedItems, ia -> ia.serializeNBT(ItemStack::serializeNBT)));

		if (earlyTarget != null) {
			compound.put("EarlyTarget", VecHelper.writeNBT(earlyTarget.getFirst()));
			compound.put("EarlyTargetPos", NbtUtils.writeBlockPos(earlyTarget.getSecond()));
			compound.putFloat("EarlyTargetTime", earlyTargetTime);
		}
	}

	@Override
	public void writeSafe(CompoundTag compound) {
		super.writeSafe(compound);
		compound.putInt("HorizontalDistance", launcher.getHorizontalDistance());
		compound.putInt("VerticalDistance", launcher.getVerticalDistance());
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		int horizontalDistance = compound.getInt("HorizontalDistance");
		int verticalDistance = compound.getInt("VerticalDistance");

		if (launcher.getHorizontalDistance() != horizontalDistance
			|| launcher.getVerticalDistance() != verticalDistance) {
			launcher.set(horizontalDistance, verticalDistance);
			launcher.clamp(AllConfigs.server().kinetics.maxEjectorDistance.get());
		}

		powered = compound.getBoolean("Powered");
		state = NBTHelper.readEnum(compound, "State", State.class);
		lidProgress.readNBT(compound.getCompound("Lid"), false);
		launchedItems = NBTHelper.readCompoundList(compound.getList("LaunchedItems", Tag.TAG_COMPOUND),
			nbt -> IntAttached.read(nbt, ItemStack::of));

		earlyTarget = null;
		earlyTargetTime = 0;
		if (compound.contains("EarlyTarget")) {
			earlyTarget = Pair.of(VecHelper.readNBT(compound.getList("EarlyTarget", Tag.TAG_DOUBLE)),
				NbtUtils.readBlockPos(compound.getCompound("EarlyTargetPos")));
			earlyTargetTime = compound.getFloat("EarlyTargetTime");
		}

		if (compound.contains("ForceAngle"))
			lidProgress.startWithValue(compound.getFloat("ForceAngle"));
	}

	public void updateSignal() {
		boolean shoudPower = level.hasNeighborSignal(worldPosition);
		if (shoudPower == powered)
			return;
		powered = shoudPower;
		sendData();
	}

	public void setTarget(int horizontalDistance, int verticalDistance) {
		launcher.set(Math.max(1, horizontalDistance), verticalDistance);
		sendData();
	}

	public BlockPos getTargetPosition() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.WEIGHTED_EJECTOR.has(blockState))
			return worldPosition;
		Direction facing = blockState.getValue(EjectorBlock.HORIZONTAL_FACING);
		return worldPosition.relative(facing, launcher.getHorizontalDistance())
			.above(launcher.getVerticalDistance());
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (isItemHandlerCap(cap))
			return depotBehaviour.getItemCapability(cap, side);
		return super.getCapability(cap, side);
	}

	public float getLidProgress(float pt) {
		return lidProgress.getValue(pt);
	}

	public State getState() {
		return state;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AABB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	private static abstract class EntityHack extends Entity {

		public EntityHack(EntityType<?> p_i48580_1_, Level p_i48580_2_) {
			super(p_i48580_1_, p_i48580_2_);
		}

		public static void setElytraFlying(Entity e) {
			SynchedEntityData data = e.getEntityData();
			data.set(DATA_SHARED_FLAGS_ID, (byte) (data.get(DATA_SHARED_FLAGS_ID) | 1 << 7));
		}

	}

	private class EjectorSlot extends ValueBoxTransform.Sided {

		@Override
		public Vec3 getLocalOffset(BlockState state) {
			if (direction != Direction.UP)
				return super.getLocalOffset(state);
			return new Vec3(.5, 10.5 / 16f, .5).add(VecHelper.rotate(VecHelper.voxelSpace(0, 0, -5), angle(state), Axis.Y));
		}

		@Override
		public void rotate(BlockState state, PoseStack ms) {
			if (direction != Direction.UP) {
				super.rotate(state, ms);
				return;
			}
			TransformStack.cast(ms)
				.rotateY(angle(state))
				.rotateX(90);
		}

		protected float angle(BlockState state) {
			float horizontalAngle = AllBlocks.WEIGHTED_EJECTOR.has(state)
				? AngleHelper.horizontalAngle(state.getValue(EjectorBlock.HORIZONTAL_FACING))
				: 0;
			return horizontalAngle;
		}

		@Override
		protected boolean isSideActive(BlockState state, Direction direction) {
			return direction.getAxis() == state.getValue(EjectorBlock.HORIZONTAL_FACING)
				.getAxis()
				|| direction == Direction.UP && EjectorBlockEntity.this.state != EjectorBlockEntity.State.CHARGED;
		}

		@Override
		protected Vec3 getSouthLocation() {
			return direction == Direction.UP ? Vec3.ZERO : VecHelper.voxelSpace(8, 6, 15.5);
		}

	}

}
