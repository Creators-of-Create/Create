package com.simibubi.create.content.contraptions.components.structureMovement;

import static com.simibubi.create.foundation.utility.AngleHelper.angleLerp;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.StabilizedContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlockEntity.CartMovementMode;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.MountedContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.train.MinecartSim2020;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Ex: Minecarts, Couplings <br>
 * Oriented Contraption Entities can rotate freely around two axes
 * simultaneously.
 */
public class OrientedContraptionEntity extends AbstractContraptionEntity {

	private static final Ingredient FUEL_ITEMS = Ingredient.of(Items.COAL, Items.CHARCOAL);

	private static final EntityDataAccessor<Optional<UUID>> COUPLING =
		SynchedEntityData.defineId(OrientedContraptionEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Direction> INITIAL_ORIENTATION =
		SynchedEntityData.defineId(OrientedContraptionEntity.class, EntityDataSerializers.DIRECTION);

	protected Vec3 motionBeforeStall;
	protected boolean forceAngle;
	private boolean isSerializingFurnaceCart;
	private boolean attachedExtraInventories;
	private boolean manuallyPlaced;

	public float prevYaw;
	public float yaw;
	public float targetYaw;

	public float prevPitch;
	public float pitch;

	public int nonDamageTicks;

	public OrientedContraptionEntity(EntityType<?> type, Level world) {
		super(type, world);
		motionBeforeStall = Vec3.ZERO;
		attachedExtraInventories = false;
		isSerializingFurnaceCart = false;
		nonDamageTicks = 10;
	}

	public static OrientedContraptionEntity create(Level world, Contraption contraption, Direction initialOrientation) {
		OrientedContraptionEntity entity =
			new OrientedContraptionEntity(AllEntityTypes.ORIENTED_CONTRAPTION.get(), world);
		entity.setContraption(contraption);
		entity.setInitialOrientation(initialOrientation);
		entity.startAtInitialYaw();
		return entity;
	}

	public static OrientedContraptionEntity createAtYaw(Level world, Contraption contraption,
		Direction initialOrientation, float initialYaw) {
		OrientedContraptionEntity entity = create(world, contraption, initialOrientation);
		entity.startAtYaw(initialYaw);
		entity.manuallyPlaced = true;
		return entity;
	}

	public void setInitialOrientation(Direction direction) {
		entityData.set(INITIAL_ORIENTATION, direction);
	}

	public Direction getInitialOrientation() {
		return entityData.get(INITIAL_ORIENTATION);
	}

	@Override
	public float getYawOffset() {
		return getInitialYaw();
	}

	public float getInitialYaw() {
		return (isInitialOrientationPresent() ? entityData.get(INITIAL_ORIENTATION) : Direction.SOUTH).toYRot();
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(COUPLING, Optional.empty());
		entityData.define(INITIAL_ORIENTATION, Direction.UP);
	}

	@Override
	public ContraptionRotationState getRotationState() {
		ContraptionRotationState crs = new ContraptionRotationState();

		float yawOffset = getYawOffset();
		crs.zRotation = pitch;
		crs.yRotation = -yaw + yawOffset;

		if (pitch != 0 && yaw != 0) {
			crs.secondYRotation = -yaw;
			crs.yRotation = yawOffset;
		}

		return crs;
	}

	@Override
	public void stopRiding() {
		if (!level.isClientSide && isAlive())
			disassemble();
		super.stopRiding();
	}

	@Override
	protected void readAdditional(CompoundTag compound, boolean spawnPacket) {
		super.readAdditional(compound, spawnPacket);

		if (compound.contains("InitialOrientation"))
			setInitialOrientation(NBTHelper.readEnum(compound, "InitialOrientation", Direction.class));

		yaw = compound.getFloat("Yaw");
		pitch = compound.getFloat("Pitch");
		manuallyPlaced = compound.getBoolean("Placed");

		if (compound.contains("ForceYaw"))
			startAtYaw(compound.getFloat("ForceYaw"));

		ListTag vecNBT = compound.getList("CachedMotion", 6);
		if (!vecNBT.isEmpty()) {
			motionBeforeStall = new Vec3(vecNBT.getDouble(0), vecNBT.getDouble(1), vecNBT.getDouble(2));
			if (!motionBeforeStall.equals(Vec3.ZERO))
				targetYaw = prevYaw = yaw += yawFromVector(motionBeforeStall);
			setDeltaMovement(Vec3.ZERO);
		}

		setCouplingId(compound.contains("OnCoupling") ? compound.getUUID("OnCoupling") : null);
	}

	@Override
	protected void writeAdditional(CompoundTag compound, boolean spawnPacket) {
		super.writeAdditional(compound, spawnPacket);

		if (motionBeforeStall != null)
			compound.put("CachedMotion", newDoubleList(motionBeforeStall.x, motionBeforeStall.y, motionBeforeStall.z));

		Direction optional = entityData.get(INITIAL_ORIENTATION);
		if (optional.getAxis()
			.isHorizontal())
			NBTHelper.writeEnum(compound, "InitialOrientation", optional);
		if (forceAngle) {
			compound.putFloat("ForceYaw", yaw);
			forceAngle = false;
		}

		compound.putBoolean("Placed", manuallyPlaced);
		compound.putFloat("Yaw", yaw);
		compound.putFloat("Pitch", pitch);

		if (getCouplingId() != null)
			compound.putUUID("OnCoupling", getCouplingId());
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (INITIAL_ORIENTATION.equals(key) && isInitialOrientationPresent() && !manuallyPlaced)
			startAtInitialYaw();
	}

	public boolean isInitialOrientationPresent() {
		return entityData.get(INITIAL_ORIENTATION)
			.getAxis()
			.isHorizontal();
	}

	public void startAtInitialYaw() {
		startAtYaw(getInitialYaw());
	}

	public void startAtYaw(float yaw) {
		targetYaw = this.yaw = prevYaw = yaw;
		forceAngle = true;
	}

	@Override
	public Vec3 applyRotation(Vec3 localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, getInitialYaw(), Axis.Y);
		localPos = VecHelper.rotate(localPos, getViewXRot(partialTicks), Axis.Z);
		localPos = VecHelper.rotate(localPos, getViewYRot(partialTicks), Axis.Y);
		return localPos;
	}

	@Override
	public Vec3 reverseRotation(Vec3 localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, -getViewYRot(partialTicks), Axis.Y);
		localPos = VecHelper.rotate(localPos, -getViewXRot(partialTicks), Axis.Z);
		localPos = VecHelper.rotate(localPos, -getInitialYaw(), Axis.Y);
		return localPos;
	}

	public float getViewYRot(float partialTicks) {
		return -(partialTicks == 1.0F ? yaw : angleLerp(partialTicks, prevYaw, yaw));
	}

	public float getViewXRot(float partialTicks) {
		return partialTicks == 1.0F ? pitch : angleLerp(partialTicks, prevPitch, pitch);
	}

	@Override
	protected void tickContraption() {
		if (nonDamageTicks > 0)
			nonDamageTicks--;
		Entity e = getVehicle();
		if (e == null)
			return;

		boolean rotationLock = false;
		boolean pauseWhileRotating = false;
		boolean wasStalled = isStalled();
		if (contraption instanceof MountedContraption) {
			MountedContraption mountedContraption = (MountedContraption) contraption;
			rotationLock = mountedContraption.rotationMode == CartMovementMode.ROTATION_LOCKED;
			pauseWhileRotating = mountedContraption.rotationMode == CartMovementMode.ROTATE_PAUSED;
		}

		Entity riding = e;
		while (riding.getVehicle() != null && !(contraption instanceof StabilizedContraption))
			riding = riding.getVehicle();

		boolean isOnCoupling = false;
		UUID couplingId = getCouplingId();
		isOnCoupling = couplingId != null && riding instanceof AbstractMinecart;

		if (!attachedExtraInventories) {
			attachInventoriesFromRidingCarts(riding, isOnCoupling, couplingId);
			attachedExtraInventories = true;
		}

		boolean rotating = updateOrientation(rotationLock, wasStalled, riding, isOnCoupling);
		if (!rotating || !pauseWhileRotating)
			tickActors();
		boolean isStalled = isStalled();

		LazyOptional<MinecartController> capability =
			riding.getCapability(CapabilityMinecartController.MINECART_CONTROLLER_CAPABILITY);
		if (capability.isPresent()) {
			if (!level.isClientSide())
				capability.orElse(null)
					.setStalledExternally(isStalled);
		} else {
			if (isStalled) {
				if (!wasStalled)
					motionBeforeStall = riding.getDeltaMovement();
				riding.setDeltaMovement(0, 0, 0);
			}
			if (wasStalled && !isStalled) {
				riding.setDeltaMovement(motionBeforeStall);
				motionBeforeStall = Vec3.ZERO;
			}
		}

		if (level.isClientSide)
			return;

		if (!isStalled()) {
			if (isOnCoupling) {
				Couple<MinecartController> coupledCarts = getCoupledCartsIfPresent();
				if (coupledCarts == null)
					return;
				coupledCarts.map(MinecartController::cart)
					.forEach(this::powerFurnaceCartWithFuelFromStorage);
				return;
			}
			powerFurnaceCartWithFuelFromStorage(riding);
		}
	}

	protected boolean updateOrientation(boolean rotationLock, boolean wasStalled, Entity riding, boolean isOnCoupling) {
		if (isOnCoupling) {
			Couple<MinecartController> coupledCarts = getCoupledCartsIfPresent();
			if (coupledCarts == null)
				return false;

			Vec3 positionVec = coupledCarts.getFirst()
				.cart()
				.position();
			Vec3 coupledVec = coupledCarts.getSecond()
				.cart()
				.position();

			double diffX = positionVec.x - coupledVec.x;
			double diffY = positionVec.y - coupledVec.y;
			double diffZ = positionVec.z - coupledVec.z;

			prevYaw = yaw;
			prevPitch = pitch;
			yaw = (float) (Mth.atan2(diffZ, diffX) * 180 / Math.PI);
			pitch = (float) (Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)) * 180 / Math.PI);

			if (getCouplingId().equals(riding.getUUID())) {
				pitch *= -1;
				yaw += 180;
			}
			return false;
		}

		if (contraption instanceof StabilizedContraption) {
			if (!(riding instanceof OrientedContraptionEntity))
				return false;
			StabilizedContraption stabilized = (StabilizedContraption) contraption;
			Direction facing = stabilized.getFacing();
			if (facing.getAxis()
				.isVertical())
				return false;
			OrientedContraptionEntity parent = (OrientedContraptionEntity) riding;
			prevYaw = yaw;
			yaw = -parent.getViewYRot(1);
			return false;
		}

		prevYaw = yaw;
		if (wasStalled)
			return false;

		boolean rotating = false;
		Vec3 movementVector = riding.getDeltaMovement();
		Vec3 locationDiff = riding.position()
			.subtract(riding.xo, riding.yo, riding.zo);
		if (!(riding instanceof AbstractMinecart))
			movementVector = locationDiff;
		Vec3 motion = movementVector.normalize();

		if (!rotationLock) {
			if (riding instanceof AbstractMinecart) {
				AbstractMinecart minecartEntity = (AbstractMinecart) riding;
				BlockPos railPosition = minecartEntity.getCurrentRailPosition();
				BlockState blockState = level.getBlockState(railPosition);
				if (blockState.getBlock() instanceof BaseRailBlock) {
					BaseRailBlock abstractRailBlock = (BaseRailBlock) blockState.getBlock();
					RailShape railDirection =
						abstractRailBlock.getRailDirection(blockState, level, railPosition, minecartEntity);
					motion = VecHelper.project(motion, MinecartSim2020.getRailVec(railDirection));
				}
			}

			if (motion.length() > 0) {
				targetYaw = yawFromVector(motion);
				if (targetYaw < 0)
					targetYaw += 360;
				if (yaw < 0)
					yaw += 360;
			}

			prevYaw = yaw;
			float maxApproachSpeed = (float) (motion.length() * 12f / (Math.max(1, getBoundingBox().getXsize() / 6f)));
			float yawHint = AngleHelper.getShortestAngleDiff(yaw, yawFromVector(locationDiff));
			float approach = AngleHelper.getShortestAngleDiff(yaw, targetYaw, yawHint);
			approach = Mth.clamp(approach, -maxApproachSpeed, maxApproachSpeed);
			yaw += approach;
			if (Math.abs(AngleHelper.getShortestAngleDiff(yaw, targetYaw)) < 1f)
				yaw = targetYaw;
			else
				rotating = true;
		}
		return rotating;
	}

	protected void powerFurnaceCartWithFuelFromStorage(Entity riding) {
		if (!(riding instanceof MinecartFurnace))
			return;
		MinecartFurnace furnaceCart = (MinecartFurnace) riding;

		// Notify to not trigger serialization side-effects
		isSerializingFurnaceCart = true;
		CompoundTag nbt = furnaceCart.serializeNBT();
		isSerializingFurnaceCart = false;

		int fuel = nbt.getInt("Fuel");
		int fuelBefore = fuel;
		double pushX = nbt.getDouble("PushX");
		double pushZ = nbt.getDouble("PushZ");

		int i = Mth.floor(furnaceCart.getX());
		int j = Mth.floor(furnaceCart.getY());
		int k = Mth.floor(furnaceCart.getZ());
		if (furnaceCart.level.getBlockState(new BlockPos(i, j - 1, k))
			.is(BlockTags.RAILS))
			--j;

		BlockPos blockpos = new BlockPos(i, j, k);
		BlockState blockstate = this.level.getBlockState(blockpos);
		if (furnaceCart.canUseRail() && blockstate.is(BlockTags.RAILS))
			if (fuel > 1)
				riding.setDeltaMovement(riding.getDeltaMovement()
					.normalize()
					.scale(1));
		if (fuel < 5 && contraption != null) {
			ItemStack coal = ItemHelper.extract(contraption.getSharedInventory(), FUEL_ITEMS, 1, false);
			if (!coal.isEmpty())
				fuel += 3600;
		}

		if (fuel != fuelBefore || pushX != 0 || pushZ != 0) {
			nbt.putInt("Fuel", fuel);
			nbt.putDouble("PushX", 0);
			nbt.putDouble("PushZ", 0);
			furnaceCart.deserializeNBT(nbt);
		}
	}

	@Nullable
	public Couple<MinecartController> getCoupledCartsIfPresent() {
		UUID couplingId = getCouplingId();
		if (couplingId == null)
			return null;
		MinecartController controller = CapabilityMinecartController.getIfPresent(level, couplingId);
		if (controller == null || !controller.isPresent())
			return null;
		UUID coupledCart = controller.getCoupledCart(true);
		MinecartController coupledController = CapabilityMinecartController.getIfPresent(level, coupledCart);
		if (coupledController == null || !coupledController.isPresent())
			return null;
		return Couple.create(controller, coupledController);
	}

	protected void attachInventoriesFromRidingCarts(Entity riding, boolean isOnCoupling, UUID couplingId) {
		if (!(contraption instanceof MountedContraption mc))
			return;
		if (!isOnCoupling) {
			mc.addExtraInventories(riding);
			return;
		}
		Couple<MinecartController> coupledCarts = getCoupledCartsIfPresent();
		if (coupledCarts == null)
			return;
		coupledCarts.map(MinecartController::cart)
			.forEach(mc::addExtraInventories);
	}

	@Override
	public CompoundTag saveWithoutId(CompoundTag nbt) {
		return isSerializingFurnaceCart ? nbt : super.saveWithoutId(nbt);
	}

	@Nullable
	public UUID getCouplingId() {
		Optional<UUID> uuid = entityData.get(COUPLING);
		return uuid == null ? null : uuid.isPresent() ? uuid.get() : null;
	}

	public void setCouplingId(UUID id) {
		entityData.set(COUPLING, Optional.ofNullable(id));
	}

	@Override
	public Vec3 getAnchorVec() {
		Vec3 anchorVec = super.getAnchorVec();
		return anchorVec.subtract(.5, 0, .5);
	}
	
	@Override
	public Vec3 getPrevAnchorVec() {
		Vec3 prevAnchorVec = super.getPrevAnchorVec();
		return prevAnchorVec.subtract(.5, 0, .5);
	}

	@Override
	protected StructureTransform makeStructureTransform() {
		BlockPos offset = new BlockPos(getAnchorVec().add(.5, .5, .5));
		return new StructureTransform(offset, 0, -yaw + getInitialYaw(), 0);
	}

	@Override
	protected float getStalledAngle() {
		return yaw;
	}

	@Override
	protected void handleStallInformation(double x, double y, double z, float angle) {
		yaw = angle;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void applyLocalTransforms(PoseStack matrixStack, float partialTicks) {
		float angleInitialYaw = getInitialYaw();
		float angleYaw = getViewYRot(partialTicks);
		float anglePitch = getViewXRot(partialTicks);

		matrixStack.translate(-.5f, 0, -.5f);

		Entity ridingEntity = getVehicle();
		if (ridingEntity instanceof AbstractMinecart)
			repositionOnCart(matrixStack, partialTicks, ridingEntity);
		else if (ridingEntity instanceof AbstractContraptionEntity) {
			if (ridingEntity.getVehicle() instanceof AbstractMinecart)
				repositionOnCart(matrixStack, partialTicks, ridingEntity.getVehicle());
			else
				repositionOnContraption(matrixStack, partialTicks, ridingEntity);
		}

		TransformStack.cast(matrixStack)
			.nudge(getId())
			.centre()
			.rotateY(angleYaw)
			.rotateZ(anglePitch)
			.rotateY(angleInitialYaw)
			.unCentre();
	}

	@OnlyIn(Dist.CLIENT)
	private void repositionOnContraption(PoseStack matrixStack, float partialTicks, Entity ridingEntity) {
		Vec3 pos = getContraptionOffset(partialTicks, ridingEntity);
		matrixStack.translate(pos.x, pos.y, pos.z);
	}

	// Minecarts do not always render at their exact location, so the contraption
	// has to adjust aswell
	@OnlyIn(Dist.CLIENT)
	private void repositionOnCart(PoseStack matrixStack, float partialTicks, Entity ridingEntity) {
		Vec3 cartPos = getCartOffset(partialTicks, ridingEntity);

		if (cartPos == Vec3.ZERO)
			return;

		matrixStack.translate(cartPos.x, cartPos.y, cartPos.z);
	}

	@OnlyIn(Dist.CLIENT)
	private Vec3 getContraptionOffset(float partialTicks, Entity ridingEntity) {
		AbstractContraptionEntity parent = (AbstractContraptionEntity) ridingEntity;
		Vec3 passengerPosition = parent.getPassengerPosition(this, partialTicks);
		double x = passengerPosition.x - Mth.lerp(partialTicks, this.xOld, this.getX());
		double y = passengerPosition.y - Mth.lerp(partialTicks, this.yOld, this.getY());
		double z = passengerPosition.z - Mth.lerp(partialTicks, this.zOld, this.getZ());

		return new Vec3(x, y, z);
	}

	@OnlyIn(Dist.CLIENT)
	private Vec3 getCartOffset(float partialTicks, Entity ridingEntity) {
		AbstractMinecart cart = (AbstractMinecart) ridingEntity;
		double cartX = Mth.lerp(partialTicks, cart.xOld, cart.getX());
		double cartY = Mth.lerp(partialTicks, cart.yOld, cart.getY());
		double cartZ = Mth.lerp(partialTicks, cart.zOld, cart.getZ());
		Vec3 cartPos = cart.getPos(cartX, cartY, cartZ);

		if (cartPos != null) {
			Vec3 cartPosFront = cart.getPosOffs(cartX, cartY, cartZ, (double) 0.3F);
			Vec3 cartPosBack = cart.getPosOffs(cartX, cartY, cartZ, (double) -0.3F);
			if (cartPosFront == null)
				cartPosFront = cartPos;
			if (cartPosBack == null)
				cartPosBack = cartPos;

			cartX = cartPos.x - cartX;
			cartY = (cartPosFront.y + cartPosBack.y) / 2.0D - cartY;
			cartZ = cartPos.z - cartZ;

			return new Vec3(cartX, cartY, cartZ);
		}

		return Vec3.ZERO;
	}

	@OnlyIn(Dist.CLIENT)
	public static void handleRelocationPacket(ContraptionRelocationPacket packet) {
		if (Minecraft.getInstance().level.getEntity(packet.entityID) instanceof OrientedContraptionEntity oce)
			oce.nonDamageTicks = 10;
	}
}
