package com.simibubi.create.content.contraptions.components.structureMovement;

import static com.simibubi.create.foundation.utility.AngleHelper.angleLerp;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.StabilizedContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerTileEntity.CartMovementMode;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.MountedContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.train.MinecartSim2020;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Ex: Minecarts, Couplings <br>
 * Oriented Contraption Entities can rotate freely around two axes
 * simultaneously.
 */
public class OrientedContraptionEntity extends AbstractContraptionEntity {

	private static final Ingredient FUEL_ITEMS = Ingredient.fromItems(Items.COAL, Items.CHARCOAL);

	private static final DataParameter<Optional<UUID>> COUPLING =
		EntityDataManager.createKey(OrientedContraptionEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Direction> INITIAL_ORIENTATION =
		EntityDataManager.createKey(OrientedContraptionEntity.class, DataSerializers.DIRECTION);

	protected Vector3d motionBeforeStall;
	protected boolean forceAngle;
	private boolean isSerializingFurnaceCart;
	private boolean attachedExtraInventories;
	private boolean manuallyPlaced;

	public float prevYaw;
	public float yaw;
	public float targetYaw;

	public float prevPitch;
	public float pitch;
	public float targetPitch;

	// When placed using a contraption item
	private float initialYawOffset;

	public OrientedContraptionEntity(EntityType<?> type, World world) {
		super(type, world);
		motionBeforeStall = Vector3d.ZERO;
		attachedExtraInventories = false;
		isSerializingFurnaceCart = false;
		initialYawOffset = -1;
	}

	public static OrientedContraptionEntity create(World world, Contraption contraption, Direction initialOrientation) {
		OrientedContraptionEntity entity =
			new OrientedContraptionEntity(AllEntityTypes.ORIENTED_CONTRAPTION.get(), world);
		entity.setContraption(contraption);
		entity.setInitialOrientation(initialOrientation);
		entity.startAtInitialYaw();
		return entity;
	}

	public static OrientedContraptionEntity createAtYaw(World world, Contraption contraption,
		Direction initialOrientation, float initialYaw) {
		OrientedContraptionEntity entity = create(world, contraption, initialOrientation);
		entity.startAtYaw(initialYaw);
		entity.manuallyPlaced = true;
		return entity;
	}

	public void setInitialOrientation(Direction direction) {
		dataManager.set(INITIAL_ORIENTATION, direction);
	}

	public Direction getInitialOrientation() {
		return dataManager.get(INITIAL_ORIENTATION);
	}

	public void deferOrientation(Direction newInitialAngle) {
		dataManager.set(INITIAL_ORIENTATION, Direction.UP);
		yaw = initialYawOffset = newInitialAngle.getHorizontalAngle();
	}

	@Override
	public float getYawOffset() {
		return getInitialYaw();
	}

	public float getInitialYaw() {
		return (isInitialOrientationPresent() ? dataManager.get(INITIAL_ORIENTATION) : Direction.SOUTH)
			.getHorizontalAngle();
	}

	@Override
	protected void registerData() {
		super.registerData();
		dataManager.register(COUPLING, Optional.empty());
		dataManager.register(INITIAL_ORIENTATION, Direction.UP);
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
		if (!world.isRemote && isAlive())
			disassemble();
		super.stopRiding();
	}

	@Override
	protected void readAdditional(CompoundNBT compound, boolean spawnPacket) {
		super.readAdditional(compound, spawnPacket);

		if (compound.contains("InitialOrientation"))
			setInitialOrientation(NBTHelper.readEnum(compound, "InitialOrientation", Direction.class));

		yaw = compound.getFloat("Yaw");
		pitch = compound.getFloat("Pitch");
		manuallyPlaced = compound.getBoolean("Placed");

		if (compound.contains("ForceYaw"))
			startAtYaw(compound.getFloat("ForceYaw"));

		ListNBT vecNBT = compound.getList("CachedMotion", 6);
		if (!vecNBT.isEmpty()) {
			motionBeforeStall = new Vector3d(vecNBT.getDouble(0), vecNBT.getDouble(1), vecNBT.getDouble(2));
			if (!motionBeforeStall.equals(Vector3d.ZERO))
				targetYaw = prevYaw = yaw += yawFromVector(motionBeforeStall);
			setMotion(Vector3d.ZERO);
		}

		setCouplingId(compound.contains("OnCoupling") ? compound.getUniqueId("OnCoupling") : null);
	}

	@Override
	protected void writeAdditional(CompoundNBT compound, boolean spawnPacket) {
		super.writeAdditional(compound, spawnPacket);

		if (motionBeforeStall != null)
			compound.put("CachedMotion",
				newDoubleNBTList(motionBeforeStall.x, motionBeforeStall.y, motionBeforeStall.z));

		Direction optional = dataManager.get(INITIAL_ORIENTATION);
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
			compound.putUniqueId("OnCoupling", getCouplingId());
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (key == INITIAL_ORIENTATION && isInitialOrientationPresent() && !manuallyPlaced)
			startAtInitialYaw();
	}

	public boolean isInitialOrientationPresent() {
		return dataManager.get(INITIAL_ORIENTATION)
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
	public Vector3d applyRotation(Vector3d localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, getInitialYaw(), Axis.Y);
		localPos = VecHelper.rotate(localPos, getPitch(partialTicks), Axis.Z);
		localPos = VecHelper.rotate(localPos, getYaw(partialTicks), Axis.Y);
		return localPos;
	}

	@Override
	public Vector3d reverseRotation(Vector3d localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, -getYaw(partialTicks), Axis.Y);
		localPos = VecHelper.rotate(localPos, -getPitch(partialTicks), Axis.Z);
		localPos = VecHelper.rotate(localPos, -getInitialYaw(), Axis.Y);
		return localPos;
	}

	public float getYaw(float partialTicks) {
		return -(partialTicks == 1.0F ? yaw : angleLerp(partialTicks, prevYaw, yaw));
	}

	public float getPitch(float partialTicks) {
		return partialTicks == 1.0F ? pitch : angleLerp(partialTicks, prevPitch, pitch);
	}

	@Override
	protected void tickContraption() {
		Entity e = getRidingEntity();
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
		while (riding.getRidingEntity() != null && !(contraption instanceof StabilizedContraption))
			riding = riding.getRidingEntity();

		boolean isOnCoupling = false;
		UUID couplingId = getCouplingId();
		isOnCoupling = couplingId != null && riding instanceof AbstractMinecartEntity;

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
			if (!world.isRemote())
				capability.orElse(null)
					.setStalledExternally(isStalled);
		} else {
			if (isStalled) {
				if (!wasStalled)
					motionBeforeStall = riding.getMotion();
				riding.setMotion(0, 0, 0);
			}
			if (wasStalled && !isStalled) {
				riding.setMotion(motionBeforeStall);
				motionBeforeStall = Vector3d.ZERO;
			}
		}

		if (world.isRemote)
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

			Vector3d positionVec = coupledCarts.getFirst()
				.cart()
				.getPositionVec();
			Vector3d coupledVec = coupledCarts.getSecond()
				.cart()
				.getPositionVec();

			double diffX = positionVec.x - coupledVec.x;
			double diffY = positionVec.y - coupledVec.y;
			double diffZ = positionVec.z - coupledVec.z;

			prevYaw = yaw;
			prevPitch = pitch;
			yaw = (float) (MathHelper.atan2(diffZ, diffX) * 180 / Math.PI);
			pitch = (float) (Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)) * 180 / Math.PI);

			if (getCouplingId().equals(riding.getUniqueID())) {
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
			yaw = -parent.getYaw(1);
			return false;
		}

		prevYaw = yaw;
		if (wasStalled)
			return false;

		boolean rotating = false;
		Vector3d movementVector = riding.getMotion();
		Vector3d locationDiff = riding.getPositionVec()
			.subtract(riding.prevPosX, riding.prevPosY, riding.prevPosZ);
		if (!(riding instanceof AbstractMinecartEntity))
			movementVector = locationDiff;
		Vector3d motion = movementVector.normalize();

		if (!rotationLock) {
			if (riding instanceof AbstractMinecartEntity) {
				AbstractMinecartEntity minecartEntity = (AbstractMinecartEntity) riding;
				BlockPos railPosition = minecartEntity.getCurrentRailPosition();
				BlockState blockState = world.getBlockState(railPosition);
				if (blockState.getBlock() instanceof AbstractRailBlock) {
					AbstractRailBlock abstractRailBlock = (AbstractRailBlock) blockState.getBlock();
					RailShape railDirection =
						abstractRailBlock.getRailDirection(blockState, world, railPosition, minecartEntity);
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
			float maxApproachSpeed = (float) (motion.length() * 12f / (Math.max(1, getBoundingBox().getXSize() / 6f)));
			float approach = AngleHelper.getShortestAngleDiff(yaw, targetYaw);
			approach = MathHelper.clamp(approach, -maxApproachSpeed, maxApproachSpeed);
			yaw += approach;
			if (Math.abs(AngleHelper.getShortestAngleDiff(yaw, targetYaw)) < 1f)
				yaw = targetYaw;
			else
				rotating = true;
		}
		return rotating;
	}

	protected void powerFurnaceCartWithFuelFromStorage(Entity riding) {
		if (!(riding instanceof FurnaceMinecartEntity))
			return;
		FurnaceMinecartEntity furnaceCart = (FurnaceMinecartEntity) riding;

		// Notify to not trigger serialization side-effects
		isSerializingFurnaceCart = true;
		CompoundNBT nbt = furnaceCart.serializeNBT();
		isSerializingFurnaceCart = false;

		int fuel = nbt.getInt("Fuel");
		int fuelBefore = fuel;
		double pushX = nbt.getDouble("PushX");
		double pushZ = nbt.getDouble("PushZ");

		int i = MathHelper.floor(furnaceCart.getX());
		int j = MathHelper.floor(furnaceCart.getY());
		int k = MathHelper.floor(furnaceCart.getZ());
		if (furnaceCart.world.getBlockState(new BlockPos(i, j - 1, k))
			.isIn(BlockTags.RAILS))
			--j;

		BlockPos blockpos = new BlockPos(i, j, k);
		BlockState blockstate = this.world.getBlockState(blockpos);
		if (furnaceCart.canUseRail() && blockstate.isIn(BlockTags.RAILS))
			if (fuel > 1)
				riding.setMotion(riding.getMotion()
					.normalize()
					.scale(1));
		if (fuel < 5 && contraption != null) {
			ItemStack coal = ItemHelper.extract(contraption.inventory, FUEL_ITEMS, 1, false);
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
		MinecartController controller = CapabilityMinecartController.getIfPresent(world, couplingId);
		if (controller == null || !controller.isPresent())
			return null;
		UUID coupledCart = controller.getCoupledCart(true);
		MinecartController coupledController = CapabilityMinecartController.getIfPresent(world, coupledCart);
		if (coupledController == null || !coupledController.isPresent())
			return null;
		return Couple.create(controller, coupledController);
	}

	protected void attachInventoriesFromRidingCarts(Entity riding, boolean isOnCoupling, UUID couplingId) {
		if (isOnCoupling) {
			Couple<MinecartController> coupledCarts = getCoupledCartsIfPresent();
			if (coupledCarts == null)
				return;
			coupledCarts.map(MinecartController::cart)
				.forEach(contraption::addExtraInventories);
			return;
		}
		contraption.addExtraInventories(riding);
	}

	@Override
	public CompoundNBT writeWithoutTypeId(CompoundNBT nbt) {
		return isSerializingFurnaceCart ? nbt : super.writeWithoutTypeId(nbt);
	}

	@Nullable
	public UUID getCouplingId() {
		Optional<UUID> uuid = dataManager.get(COUPLING);
		return uuid == null ? null : uuid.isPresent() ? uuid.get() : null;
	}

	public void setCouplingId(UUID id) {
		dataManager.set(COUPLING, Optional.ofNullable(id));
	}

	@Override
	public Vector3d getAnchorVec() {
		return new Vector3d(getX() - .5, getY(), getZ() - .5);
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
	protected void handleStallInformation(float x, float y, float z, float angle) {
		yaw = angle;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void doLocalTransforms(float partialTicks, MatrixStack[] matrixStacks) {
		float angleInitialYaw = getInitialYaw();
		float angleYaw = getYaw(partialTicks);
		float anglePitch = getPitch(partialTicks);

		for (MatrixStack stack : matrixStacks)
			stack.translate(-.5f, 0, -.5f);

		Entity ridingEntity = getRidingEntity();
		if (ridingEntity instanceof AbstractMinecartEntity)
			repositionOnCart(partialTicks, matrixStacks, ridingEntity);
		else if (ridingEntity instanceof AbstractContraptionEntity) {
			if (ridingEntity.getRidingEntity() instanceof AbstractMinecartEntity)
				repositionOnCart(partialTicks, matrixStacks, ridingEntity.getRidingEntity());
			else
				repositionOnContraption(partialTicks, matrixStacks, ridingEntity);
		}

		for (MatrixStack stack : matrixStacks)
			MatrixStacker.of(stack)
				.nudge(getEntityId())
				.centre()
				.rotateY(angleYaw)
				.rotateZ(anglePitch)
				.rotateY(angleInitialYaw)
				.unCentre();
	}

	@OnlyIn(Dist.CLIENT)
	private void repositionOnContraption(float partialTicks, MatrixStack[] matrixStacks, Entity ridingEntity) {
		Vector3d pos = getContraptionOffset(partialTicks, ridingEntity);
		for (MatrixStack stack : matrixStacks)
			stack.translate(pos.x, pos.y, pos.z);
	}

	// Minecarts do not always render at their exact location, so the contraption
	// has to adjust aswell
	@OnlyIn(Dist.CLIENT)
	private void repositionOnCart(float partialTicks, MatrixStack[] matrixStacks, Entity ridingEntity) {
		Vector3d cartPos = getCartOffset(partialTicks, ridingEntity);

		if (cartPos == Vector3d.ZERO)
			return;

		for (MatrixStack stack : matrixStacks)
			stack.translate(cartPos.x, cartPos.y, cartPos.z);
	}

	@OnlyIn(Dist.CLIENT)
	private Vector3d getContraptionOffset(float partialTicks, Entity ridingEntity) {
		AbstractContraptionEntity parent = (AbstractContraptionEntity) ridingEntity;
		Vector3d passengerPosition = parent.getPassengerPosition(this, partialTicks);
		double x = passengerPosition.x - MathHelper.lerp(partialTicks, this.lastTickPosX, this.getX());
		double y = passengerPosition.y - MathHelper.lerp(partialTicks, this.lastTickPosY, this.getY());
		double z = passengerPosition.z - MathHelper.lerp(partialTicks, this.lastTickPosZ, this.getZ());

		return new Vector3d(x, y, z);
	}

	@OnlyIn(Dist.CLIENT)
	private Vector3d getCartOffset(float partialTicks, Entity ridingEntity) {
		AbstractMinecartEntity cart = (AbstractMinecartEntity) ridingEntity;
		double cartX = MathHelper.lerp(partialTicks, cart.lastTickPosX, cart.getX());
		double cartY = MathHelper.lerp(partialTicks, cart.lastTickPosY, cart.getY());
		double cartZ = MathHelper.lerp(partialTicks, cart.lastTickPosZ, cart.getZ());
		Vector3d cartPos = cart.getPos(cartX, cartY, cartZ);

		if (cartPos != null) {
			Vector3d cartPosFront = cart.getPosOffset(cartX, cartY, cartZ, (double) 0.3F);
			Vector3d cartPosBack = cart.getPosOffset(cartX, cartY, cartZ, (double) -0.3F);
			if (cartPosFront == null)
				cartPosFront = cartPos;
			if (cartPosBack == null)
				cartPosBack = cartPos;

			cartX = cartPos.x - cartX;
			cartY = (cartPosFront.y + cartPosBack.y) / 2.0D - cartY;
			cartZ = cartPos.z - cartZ;

			return new Vector3d(cartX, cartY, cartZ);
		}

		return Vector3d.ZERO;
	}
}
