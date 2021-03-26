package com.simibubi.create.content.logistics.block.depot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.funnel.AbstractFunnelBlock;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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
		return state != State.CHARGED && !(world.isRemote && state == State.LAUNCHING);
	}

	public void activateDeferred() {
		if (cannotLaunch())
			return;
		Direction facing = getFacing();
		List<Entity> entities =
			world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos).grow(-1 / 16f, 0, -1 / 16f));

		// Launch Items
		if (!world.isRemote)
			launchItems();

		// Launch Entities
		for (Entity entity : entities) {
			boolean isPlayerEntity = entity instanceof PlayerEntity;
			if (!entity.isAlive())
				continue;
			if (entity instanceof ItemEntity)
				continue;

			entity.onGround = false;

			if (isPlayerEntity != world.isRemote)
				continue;

			entity.setPosition(pos.getX() + .5f, pos.getY() + 1, pos.getZ() + .5f);
			launcher.applyMotion(entity, facing);

			if (!isPlayerEntity)
				continue;
			PlayerEntity playerEntity = (PlayerEntity) entity;
			if (!(playerEntity.getItemStackFromSlot(EquipmentSlotType.CHEST)
				.getItem() instanceof ElytraItem))
				continue;

			playerEntity.rotationYaw = facing.getHorizontalAngle();
			playerEntity.rotationPitch = -35;
			playerEntity.setMotion(playerEntity.getMotion()
				.scale(.75f));
			deployElytra(playerEntity);
			AllPackets.channel.sendToServer(new EjectorElytraPacket(pos));
		}

		if (!world.isRemote) {
			lidProgress.chase(1, .8f, Chaser.EXP);
			state = State.LAUNCHING;
			world.playSound(null, pos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, .35f, 1f);
			world.playSound(null, pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, .1f, 1.4f);
		}
	}

	public void deployElytra(PlayerEntity playerEntity) {
		EntityHack.setElytraFlying(playerEntity);
	}

	protected void launchItems() {
		ItemStack heldItemStack = depotBehaviour.getHeldItemStack();
		Direction funnelFacing = getFacing().getOpposite();

		if (AbstractFunnelBlock.getFunnelFacing(world.getBlockState(pos.up())) == funnelFacing) {
			DirectBeltInputBehaviour directOutput = getBehaviour(DirectBeltInputBehaviour.TYPE);

			if (depotBehaviour.heldItem != null) {
				ItemStack remainder = directOutput.tryExportingToBeltFunnel(heldItemStack, funnelFacing, false);
				if (remainder == null)
					;
				else if (remainder.isEmpty())
					depotBehaviour.removeHeldItem();
				else if (!remainder.isItemEqual(heldItemStack))
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
				else if (!remainder.isItemEqual(stack))
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

		if (!world.isRemote)
			world.markAndNotifyBlock(pos, world.getChunkAt(pos), getBlockState(), getBlockState(), 0);

		if (depotBehaviour.heldItem != null) {
			launchedItems.add(IntAttached.withZero(heldItemStack));
			depotBehaviour.removeHeldItem();
		}

		for (TransportedItemStack transportedItemStack : depotBehaviour.incoming)
			launchedItems.add(IntAttached.withZero(transportedItemStack.stack));
		depotBehaviour.incoming.clear();

		ItemStackHandler outputs = depotBehaviour.processingOutputBuffer;
		for (int i = 0; i < outputs.getSlots(); i++) {
			ItemStack extractItem = outputs.extractItem(i, 64, false);
			if (!extractItem.isEmpty())
				launchedItems.add(IntAttached.withZero(extractItem));
		}
	}

	protected Direction getFacing() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.WEIGHTED_EJECTOR.has(blockState))
			return Direction.UP;
		Direction facing = blockState.get(EjectorBlock.HORIZONTAL_FACING);
		return facing;
	}

	@Override
	public void tick() {
		super.tick();

		boolean doLogic = !world.isRemote || isVirtual();
		State prevState = state;
		float maxTime = Math.max(3, (float) launcher.getTotalFlyingTicks());

		if (launch) {
			launch = false;
			activateDeferred();
		}

		for (Iterator<IntAttached<ItemStack>> iterator = launchedItems.iterator(); iterator.hasNext();) {
			IntAttached<ItemStack> intAttached = iterator.next();
			if (intAttached.exceeds((int) maxTime)) {
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
			if (((int) world.getGameTime()) % soundRate == 0 && doLogic)
				world.playSound(null, pos, SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, volume,
					pitch);
		}

		if (state != prevState)
			notifyUpdate();
	}

	protected void nudgeEntities() {
		for (Entity entity : world.getEntitiesWithinAABB(Entity.class,
			new AxisAlignedBB(pos).grow(-1 / 16f, 0, -1 / 16f)))
			if (!(entity instanceof PlayerEntity))
				entity.setPosition(entity.getX(), entity.getY() + .125f, entity.getZ());
	}

	protected void ejectIfTriggered() {
		if (powered)
			return;
		int presentStackSize = depotBehaviour.getPresentStackSize();
		if (presentStackSize == 0)
			return;
		if (presentStackSize < maxStackSize.getValue())
			return;

		Direction funnelFacing = getFacing().getOpposite();
		ItemStack held = depotBehaviour.getHeldItemStack();
		if (AbstractFunnelBlock.getFunnelFacing(world.getBlockState(pos.up())) == funnelFacing) {
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

		DirectBeltInputBehaviour targetOpenInv = getTargetOpenInv();
		if (targetOpenInv != null) {
			ItemStack remainder = targetOpenInv.handleInsertion(intAttached.getValue(), Direction.UP, false);
			intAttached.setSecond(remainder);
		}

		if (intAttached.getValue()
			.isEmpty())
			return;

		Vec3d ejectVec = getLaunchedItemLocation(maxTime);
		Vec3d ejectMotionVec = getLaunchedItemMotion(maxTime);
		ItemEntity item = new ItemEntity(world, ejectVec.x, ejectVec.y, ejectVec.z, intAttached.getValue());
		item.setMotion(ejectMotionVec);
		item.setDefaultPickupDelay();
		world.addEntity(item);
	}

	public DirectBeltInputBehaviour getTargetOpenInv() {
		return TileEntityBehaviour.get(world, pos.up(launcher.getVerticalDistance())
			.offset(getFacing(), Math.max(1, launcher.getHorizontalDistance())), DirectBeltInputBehaviour.TYPE);
	}

	public Vec3d getLaunchedItemLocation(float time) {
		return launcher.getGlobalPos(time, getFacing().getOpposite(), pos);
	}

	public Vec3d getLaunchedItemMotion(float time) {
		return launcher.getGlobalVelocity(time, getFacing().getOpposite(), pos);
	}

	public void dropFlyingItems() {
		for (IntAttached<ItemStack> intAttached : launchedItems) {
			Vec3d ejectVec = getLaunchedItemLocation(intAttached.getFirst());
			Vec3d ejectMotionVec = getLaunchedItemMotion(intAttached.getFirst());
			ItemEntity item = new ItemEntity(world, 0, 0, 0, intAttached.getValue());
			item.setPos(ejectVec.x, ejectVec.y, ejectVec.z);
			item.setMotion(ejectMotionVec);
			item.setDefaultPickupDelay();
			world.addEntity(item);
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
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		int horizontalDistance = compound.getInt("HorizontalDistance");
		int verticalDistance = compound.getInt("VerticalDistance");

		if (launcher == null || launcher.getHorizontalDistance() != horizontalDistance
			|| launcher.getVerticalDistance() != verticalDistance) {
			launcher.set(horizontalDistance, verticalDistance);
			launcher.clamp(AllConfigs.SERVER.kinetics.maxEjectorDistance.get());
		}

		powered = compound.getBoolean("Powered");
		state = NBTHelper.readEnum(compound, "State", State.class);
		lidProgress.readNBT(compound.getCompound("Lid"), clientPacket);
		launchedItems = NBTHelper.readCompoundList(compound.getList("LaunchedItems", NBT.TAG_COMPOUND),
			nbt -> IntAttached.read(nbt, ItemStack::read));
	}

	public void updateSignal() {
		boolean shoudPower = world.isBlockPowered(pos);
		if (shoudPower == powered)
			return;
		powered = shoudPower;
		sendData();
	}

	public void setTarget(int horizontalDistance, int verticalDistance) {
		launcher.set(Math.max(1, horizontalDistance), verticalDistance);
		if (horizontalDistance == 0 && verticalDistance == 0) {
			state = State.CHARGED;
			lidProgress.startWithValue(0);
		}
		sendData();
	}

	public BlockPos getTargetPosition() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.WEIGHTED_EJECTOR.has(blockState))
			return pos;
		Direction facing = blockState.get(EjectorBlock.HORIZONTAL_FACING);
		return pos.offset(facing, launcher.getHorizontalDistance())
			.up(launcher.getVerticalDistance());
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (isItemHandlerCap(cap))
			return depotBehaviour.getItemCapability(cap, side);
		return super.getCapability(cap, side);
	}

	@Override
	public boolean shouldRenderAsTE() {
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
	public double getMaxRenderDistanceSquared() {
		return super.getMaxRenderDistanceSquared() * 16;
	}

	private static abstract class EntityHack extends Entity {

		public EntityHack(EntityType<?> p_i48580_1_, World p_i48580_2_) {
			super(p_i48580_1_, p_i48580_2_);
		}

		public static void setElytraFlying(Entity e) {
			EntityDataManager data = e.getDataManager();
			data.set(FLAGS, (byte) (data.get(FLAGS) | 1 << 7));
		}

	}

	private static class EjectorSlot extends ValueBoxTransform.Sided {

		@Override
		protected Vec3d getLocalOffset(BlockState state) {
			return new Vec3d(.5, 13 / 16f, .5).add(VecHelper.rotate(new Vec3d(0, 0, -.3), angle(state), Axis.Y));
		}

		@Override
		protected void rotate(BlockState state, MatrixStack ms) {
			MatrixStacker.of(ms)
				.rotateY(angle(state))
				.rotateX(90);
		}

		protected float angle(BlockState state) {
			float horizontalAngle = AllBlocks.WEIGHTED_EJECTOR.has(state)
				? AngleHelper.horizontalAngle(state.get(EjectorBlock.HORIZONTAL_FACING))
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
		protected Vec3d getSouthLocation() {
			return Vec3d.ZERO;
		}

	}

}
