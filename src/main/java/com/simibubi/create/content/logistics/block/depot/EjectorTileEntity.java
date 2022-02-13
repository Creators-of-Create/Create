package com.simibubi.create.content.logistics.block.depot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.funnel.AbstractFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.block.BlockState;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public class EjectorTileEntity extends KineticTileEntity {

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
	Pair<Vector3d, BlockPos> earlyTarget;
	float earlyTargetTime;
	// runtime stuff
	int scanCooldown;
	ItemStack trackedItem;

	public enum State {
		CHARGED, LAUNCHING, RETRACTING;
	}

	public EjectorTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		launcher = new EntityLauncher(1, 0);
		lidProgress = LerpedFloat.linear()
			.startWithValue(1);
		state = State.RETRACTING;
		launchedItems = new ArrayList<>();
		powered = false;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.add(depotBehaviour = new DepotBehaviour(this));

		maxStackSize = new ScrollValueBehaviour(Lang.translate("weighted_ejector.stack_size"), this, new EjectorSlot())
			.between(0, 64)
			.withFormatter(i -> i == 0 ? "*" : String.valueOf(i))
			.onlyActiveWhen(() -> state == State.CHARGED)
			.requiresWrench();
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
			level.getEntitiesOfClass(Entity.class, new AxisAlignedBB(worldPosition).inflate(-1 / 16f, 0, -1 / 16f));

		// Launch Items
		boolean doLogic = !level.isClientSide || isVirtual();
		if (doLogic)
			launchItems();

		// Launch Entities
		for (Entity entity : entities) {
			boolean isPlayerEntity = entity instanceof PlayerEntity;
			if (!entity.isAlive())
				continue;
			if (entity instanceof ItemEntity)
				continue;
			if (entity.getPistonPushReaction() == PushReaction.IGNORE)
				continue;

			entity.onGround = false;

			if (isPlayerEntity != level.isClientSide)
				continue;

			entity.setPos(worldPosition.getX() + .5f, worldPosition.getY() + 1, worldPosition.getZ() + .5f);
			launcher.applyMotion(entity, facing);

			if (!isPlayerEntity)
				continue;
			PlayerEntity playerEntity = (PlayerEntity) entity;
			if (!(playerEntity.getItemBySlot(EquipmentSlotType.CHEST)
				.getItem() instanceof ElytraItem))
				continue;

			playerEntity.yRot = facing.toYRot();
			playerEntity.xRot = -35;
			playerEntity.setDeltaMovement(playerEntity.getDeltaMovement()
				.scale(.75f));
			deployElytra(playerEntity);
			AllPackets.channel.sendToServer(new EjectorElytraPacket(worldPosition));
		}

		if (doLogic) {
			lidProgress.chase(1, .8f, Chaser.EXP);
			state = State.LAUNCHING;
			if (!level.isClientSide) {
				level.playSound(null, worldPosition, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, .35f, 1f);
				level.playSound(null, worldPosition, SoundEvents.CHEST_OPEN, SoundCategory.BLOCKS, .1f, 1.4f);
			}
		}
	}

	public void deployElytra(PlayerEntity playerEntity) {
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
				else if (!remainder.sameItem(heldItemStack))
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
				else if (!remainder.sameItem(stack))
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
			scanCooldown = AllConfigs.SERVER.kinetics.ejectorScanInterval.get();
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

				float value = MathHelper.clamp(lidProgress.getValue() - getWindUpSpeed(), 0, 1);
				lidProgress.setValue(value);

				int soundRate = (int) (1 / (getWindUpSpeed() * 5)) + 1;
				float volume = .125f;
				float pitch = 1.5f - lidProgress.getValue();
				if (((int) level.getGameTime()) % soundRate == 0 && doLogic)
					level.playSound(null, worldPosition, SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundCategory.BLOCKS,
						volume, pitch);
			}
		}

		if (state != prevState)
			notifyUpdate();
	}

	private boolean scanTrajectoryForObstacles(int time) {
		if (time <= 2)
			return false;

		Vector3d source = getLaunchedItemLocation(time);
		Vector3d target = getLaunchedItemLocation(time + 1);

		BlockRayTraceResult rayTraceBlocks =
			level.clip(new RayTraceContext(source, target, BlockMode.COLLIDER, FluidMode.NONE, null));
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

		Vector3d vec = rayTraceBlocks.getLocation();
		earlyTarget = Pair.of(vec.add(Vector3d.atLowerCornerOf(rayTraceBlocks.getDirection()
			.getNormal())
			.scale(.25f)), rayTraceBlocks.getBlockPos());
		earlyTargetTime = (float) (time + (source.distanceTo(vec) / source.distanceTo(target)));
		sendData();
		return true;
	}

	protected void nudgeEntities() {
		for (Entity entity : level.getEntitiesOfClass(Entity.class,
			new AxisAlignedBB(worldPosition).inflate(-1 / 16f, 0, -1 / 16f))) {
			if (!entity.isAlive())
				continue;
			if (entity.getPistonPushReaction() == PushReaction.IGNORE)
				continue;
			if (!(entity instanceof PlayerEntity))
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

		Vector3d ejectVec = earlyTarget != null ? earlyTarget.getFirst() : getLaunchedItemLocation(maxTime);
		Vector3d ejectMotionVec = getLaunchedItemMotion(maxTime);
		ItemEntity item = new ItemEntity(level, ejectVec.x, ejectVec.y, ejectVec.z, intAttached.getValue());
		item.setDeltaMovement(ejectMotionVec);
		item.setDefaultPickUpDelay();
		level.addFreshEntity(item);
	}

	public DirectBeltInputBehaviour getTargetOpenInv() {
		BlockPos targetPos = earlyTarget != null ? earlyTarget.getSecond()
			: worldPosition.above(launcher.getVerticalDistance())
				.relative(getFacing(), Math.max(1, launcher.getHorizontalDistance()));
		return TileEntityBehaviour.get(level, targetPos, DirectBeltInputBehaviour.TYPE);
	}

	public Vector3d getLaunchedItemLocation(float time) {
		return launcher.getGlobalPos(time, getFacing().getOpposite(), worldPosition);
	}

	public Vector3d getLaunchedItemMotion(float time) {
		return launcher.getGlobalVelocity(time, getFacing().getOpposite(), worldPosition)
			.scale(.5f);
	}

	public void dropFlyingItems() {
		for (IntAttached<ItemStack> intAttached : launchedItems) {
			Vector3d ejectVec = getLaunchedItemLocation(intAttached.getFirst());
			Vector3d ejectMotionVec = getLaunchedItemMotion(intAttached.getFirst());
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
			distanceFactor = 1 * MathHelper.sqrt(Math.pow(hd, 2) + Math.pow(vd, 2));
		return speedFactor / distanceFactor;
	}

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
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
			compound.put("EarlyTargetPos", NBTUtil.writeBlockPos(earlyTarget.getSecond()));
			compound.putFloat("EarlyTargetTime", earlyTargetTime);
		}
	}

	@Override
	public void writeSafe(CompoundNBT compound, boolean clientPacket) {
		super.writeSafe(compound, clientPacket);
		compound.putInt("HorizontalDistance", launcher.getHorizontalDistance());
		compound.putInt("VerticalDistance", launcher.getVerticalDistance());
	}

	@Override
	protected void fromTag(BlockState blockState, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(blockState, compound, clientPacket);
		int horizontalDistance = compound.getInt("HorizontalDistance");
		int verticalDistance = compound.getInt("VerticalDistance");

		if (launcher.getHorizontalDistance() != horizontalDistance
			|| launcher.getVerticalDistance() != verticalDistance) {
			launcher.set(horizontalDistance, verticalDistance);
			launcher.clamp(AllConfigs.SERVER.kinetics.maxEjectorDistance.get());
		}

		powered = compound.getBoolean("Powered");
		state = NBTHelper.readEnum(compound, "State", State.class);
		lidProgress.readNBT(compound.getCompound("Lid"), false);
		launchedItems = NBTHelper.readCompoundList(compound.getList("LaunchedItems", NBT.TAG_COMPOUND),
			nbt -> IntAttached.read(nbt, ItemStack::of));

		earlyTarget = null;
		earlyTargetTime = 0;
		if (compound.contains("EarlyTarget")) {
			earlyTarget = Pair.of(VecHelper.readNBT(compound.getList("EarlyTarget", NBT.TAG_DOUBLE)),
				NBTUtil.readBlockPos(compound.getCompound("EarlyTargetPos")));
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

	@Override
	public boolean shouldRenderNormally() {
		return true;
	}

	public float getLidProgress(float pt) {
		return lidProgress.getValue(pt);
	}

	public State getState() {
		return state;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getViewDistance() {
		return super.getViewDistance() * 16;
	}

	private static abstract class EntityHack extends Entity {

		public EntityHack(EntityType<?> p_i48580_1_, World p_i48580_2_) {
			super(p_i48580_1_, p_i48580_2_);
		}

		public static void setElytraFlying(Entity e) {
			EntityDataManager data = e.getEntityData();
			data.set(DATA_SHARED_FLAGS_ID, (byte) (data.get(DATA_SHARED_FLAGS_ID) | 1 << 7));
		}

	}

	private static class EjectorSlot extends ValueBoxTransform.Sided {

		@Override
		protected Vector3d getLocalOffset(BlockState state) {
			return new Vector3d(.5, 13 / 16f, .5).add(VecHelper.rotate(new Vector3d(0, 0, -.3), angle(state), Axis.Y));
		}

		@Override
		protected void rotate(BlockState state, MatrixStack ms) {
			MatrixTransformStack.of(ms)
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
			return direction == Direction.UP;
		}

		@Override
		protected float getScale() {
			return 0.2f;
		}

		@Override
		protected Vector3d getSouthLocation() {
			return Vector3d.ZERO;
		}

	}

}
