package com.simibubi.create.content.contraptions.components.structureMovement;

import static com.simibubi.create.foundation.utility.AngleHelper.angleLerp;
import static com.simibubi.create.foundation.utility.AngleHelper.getShortestAngleDiff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.components.actors.SeatEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerTileEntity.CartMovementMode;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.MountedContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionSeatMappingPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.MinecartController;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

public class ContraptionEntity extends Entity implements IEntityAdditionalSpawnData {

	public static final IDataSerializer<Optional<Direction>> OPTIONAL_DIRECTION =
		new IDataSerializer<Optional<Direction>>() {

			public void write(PacketBuffer buffer, Optional<Direction> opt) {
				buffer.writeVarInt(opt.map(Direction::ordinal)
					.orElse(-1) + 1);
			}

			public Optional<Direction> read(PacketBuffer buffer) {
				int i = buffer.readVarInt();
				return i == 0 ? Optional.empty() : Optional.of(Direction.values()[i - 1]);
			}

			public Optional<Direction> copyValue(Optional<Direction> opt) {
				return Optional.ofNullable(opt.orElse(null));
			}
		};

	static {
		DataSerializers.registerSerializer(OPTIONAL_DIRECTION);
	}

	final List<Entity> collidingEntities = new ArrayList<>();

	protected Contraption contraption;
	protected BlockPos controllerPos;
	protected Vector3d motionBeforeStall;
	protected boolean forceAngle;
	protected boolean stationary;
	protected boolean initialized;
	private boolean isSerializingFurnaceCart;
	private boolean attachedExtraInventories;
	private boolean prevPosInvalid;

	private static final Ingredient FUEL_ITEMS = Ingredient.fromItems(Items.COAL, Items.CHARCOAL);

	private static final DataParameter<Boolean> STALLED =
		EntityDataManager.createKey(ContraptionEntity.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Optional<UUID>> COUPLING =
		EntityDataManager.createKey(ContraptionEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Optional<Direction>> INITIAL_ORIENTATION =
		EntityDataManager.createKey(ContraptionEntity.class, ContraptionEntity.OPTIONAL_DIRECTION);

	public float prevYaw;
	public float prevPitch;
	public float prevRoll;

	public float yaw;
	public float pitch;
	public float roll;

	// Mounted Contraptions
	public float targetYaw;
	public float targetPitch;

	public ContraptionEntity(EntityType<?> entityTypeIn, World worldIn) {
		super(entityTypeIn, worldIn);
		motionBeforeStall = Vector3d.ZERO;
		stationary = entityTypeIn == AllEntityTypes.STATIONARY_CONTRAPTION.get();
		isSerializingFurnaceCart = false;
		attachedExtraInventories = false;
		prevPosInvalid = true;
	}

	public static ContraptionEntity createMounted(World world, Contraption contraption,
		Optional<Direction> initialOrientation) {
		ContraptionEntity entity = new ContraptionEntity(AllEntityTypes.CONTRAPTION.get(), world);
		entity.contraptionCreated(contraption);
		initialOrientation.ifPresent(entity::setInitialOrientation);
		entity.startAtInitialYaw();
		return entity;
	}

	public static ContraptionEntity createStationary(World world, Contraption contraption) {
		ContraptionEntity entity = new ContraptionEntity(AllEntityTypes.STATIONARY_CONTRAPTION.get(), world);
		entity.contraptionCreated(contraption);
		return entity;
	}

	public void reOrientate(Direction newInitialAngle) {
		setInitialOrientation(newInitialAngle);
	}

	protected void contraptionCreated(Contraption contraption) {
		this.contraption = contraption;
		if (contraption == null)
			return;
		if (world.isRemote)
			return;
		contraption.gatherStoredItems();
	}

	protected void contraptionInitialize() {
		if (!world.isRemote)
			contraption.mountPassengers(this);
		initialized = true;
	}

	public <T extends TileEntity & IControlContraption> ContraptionEntity controlledBy(T controller) {
		this.controllerPos = controller.getPos();
		return this;
	}

	private IControlContraption getController() {
		if (controllerPos == null)
			return null;
		if (!world.isBlockPresent(controllerPos))
			return null;
		TileEntity te = world.getTileEntity(controllerPos);
		if (!(te instanceof IControlContraption))
			return null;
		return (IControlContraption) te;
	}

	public boolean collisionEnabled() {
		return true;
	}

	@Override
	protected void addPassenger(Entity passenger) {
		super.addPassenger(passenger);
	}

	public void addSittingPassenger(Entity passenger, int seatIndex) {
		passenger.startRiding(this, true);
		if (world.isRemote)
			return;
		contraption.getSeatMapping()
			.put(passenger.getUniqueID(), seatIndex);
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionSeatMappingPacket(getEntityId(), contraption.getSeatMapping()));
	}

	@Override
	public void remove() {
		super.remove();
	}

	@Override
	protected void removePassenger(Entity passenger) {
		Vector3d transformedVector = getPassengerPosition(passenger);
		super.removePassenger(passenger);
		if (world.isRemote)
			return;
		if (transformedVector != null)
			passenger.getPersistentData()
				.put("ContraptionDismountLocation", VecHelper.writeNBT(transformedVector));
		contraption.getSeatMapping()
			.remove(passenger.getUniqueID());
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionSeatMappingPacket(getEntityId(), contraption.getSeatMapping()));
	}

	@Override
	protected void updatePassengerPosition(Entity passenger, IMoveCallback callback) {
		if (!isPassenger(passenger))
			return;
		Vector3d transformedVector = getPassengerPosition(passenger);
		if (transformedVector == null)
			return;
		callback.accept(passenger, transformedVector.x, transformedVector.y, transformedVector.z);
	}

	protected Vector3d getPassengerPosition(Entity passenger) {
		AxisAlignedBB bb = passenger.getBoundingBox();
		double ySize = bb.getYSize();
		BlockPos seat = contraption.getSeat(passenger.getUniqueID());
		if (seat == null)
			return null;
		Vector3d transformedVector = toGlobalVector(Vector3d.of(seat).add(.5, passenger.getYOffset() + ySize - .15f, .5), 1)
			.add(VecHelper.getCenterOf(BlockPos.ZERO))
			.subtract(0.5, ySize, 0.5);
		return transformedVector;
	}

	@Override
	protected boolean canFitPassenger(Entity p_184219_1_) {
		return getPassengers().size() < contraption.getSeats()
			.size();
	}

	public boolean handlePlayerInteraction(PlayerEntity player, BlockPos localPos, Direction side,
		Hand interactionHand) {
		int indexOfSeat = contraption.getSeats()
			.indexOf(localPos);
		if (indexOfSeat == -1)
			return false;

		// Eject potential existing passenger
		Entity toDismount = null;
		for (Entry<UUID, Integer> entry : contraption.getSeatMapping()
			.entrySet()) {
			if (entry.getValue() != indexOfSeat)
				continue;
			for (Entity entity : getPassengers()) {
				if (!entry.getKey()
					.equals(entity.getUniqueID()))
					continue;
				if (entity instanceof PlayerEntity)
					return false;
				toDismount = entity;
			}
		}

		if (toDismount != null && !world.isRemote) {
			Vector3d transformedVector = getPassengerPosition(toDismount);
			toDismount.stopRiding();
			if (transformedVector != null)
				toDismount.setPositionAndUpdate(transformedVector.x, transformedVector.y, transformedVector.z);
		}

		if (world.isRemote)
			return true;
		addSittingPassenger(player, indexOfSeat);
		return true;
	}

	public Vector3d toGlobalVector(Vector3d localVec, float partialTicks) {
		Vector3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		localVec = localVec.subtract(rotationOffset);
		localVec = applyRotation(localVec, partialTicks);
		localVec = localVec.add(rotationOffset)
			.add(getAnchorVec());
		return localVec;
	}


	public Vector3d toLocalVector(Vector3d globalVec, float partialTicks) {
		Vector3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		globalVec = globalVec.subtract(getAnchorVec())
			.subtract(rotationOffset);
		globalVec = reverseRotation(globalVec, partialTicks);
		globalVec = globalVec.add(rotationOffset);
		return globalVec;
	}

	@Override
	public void tick() {
		if (contraption == null) {
			remove();
			return;
		}

		prevPosX = getX();
		prevPosY = getY();
		prevPosZ = getZ();
		prevPosInvalid = false;

		if (!initialized)
			contraptionInitialize();
		checkController();

		Entity mountedEntity = getRidingEntity();
		if (mountedEntity != null) {
			tickAsPassenger(mountedEntity);
			super.tick();
			return;
		}

		if (getMotion().length() < 1 / 4098f)
			setMotion(Vector3d.ZERO);

		move(getMotion().x, getMotion().y, getMotion().z);
		if (ContraptionCollider.collideBlocks(this))
			getController().collided();

		tickActors();

		prevYaw = yaw;
		prevPitch = pitch;
		prevRoll = roll;

		super.tick();

	}

	public void tickAsPassenger(Entity e) {
		boolean rotationLock = false;
		boolean pauseWhileRotating = false;
		boolean rotating = false;
		boolean wasStalled = isStalled();
		if (contraption instanceof MountedContraption) {
			MountedContraption mountedContraption = (MountedContraption) contraption;
			rotationLock = mountedContraption.rotationMode == CartMovementMode.ROTATION_LOCKED;
			pauseWhileRotating = mountedContraption.rotationMode == CartMovementMode.ROTATE_PAUSED;
		}

		Entity riding = e;
		while (riding.getRidingEntity() != null)
			riding = riding.getRidingEntity();

		boolean isOnCoupling = false;
		UUID couplingId = getCouplingId();
		isOnCoupling = couplingId != null && riding instanceof AbstractMinecartEntity;

		if (!attachedExtraInventories) {
			attachInventoriesFromRidingCarts(riding, isOnCoupling, couplingId);
			attachedExtraInventories = true;
		}
/*
<<<<<<< HEAD
		boolean isOnCoupling = false;
		if (contraption instanceof MountedContraption) {
			MountedContraption mountedContraption = (MountedContraption) contraption;
			UUID couplingId = getCouplingId();
			isOnCoupling = couplingId != null && riding instanceof AbstractMinecartEntity;
			if (isOnCoupling) {
				MinecartCoupling coupling = MinecartCouplingHandler.getCoupling(world, couplingId);
				if (coupling != null && coupling.areBothEndsPresent()) {
					boolean notOnMainCart = !coupling.getId()
						.equals(riding.getUniqueID());
					Vector3d positionVec = coupling.asCouple()
						.get(notOnMainCart)
						.getPositionVec();
					prevYaw = yaw;
					prevPitch = pitch;
					double diffZ = positionVec.z - riding.getZ();
					double diffX = positionVec.x - riding.getX();
					yaw = (float) (MathHelper.atan2(diffZ, diffX) * 180 / Math.PI);
					pitch = (float) (Math.atan2(positionVec.y - getY(), Math.sqrt(diffX * diffX + diffZ * diffZ)) * 180
						/ Math.PI);

					if (notOnMainCart) {
						yaw += 180;
					}
=======*/
		if (isOnCoupling) {
			Couple<MinecartController> coupledCarts = getCoupledCartsIfPresent();
			if (coupledCarts != null) {

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

				if (couplingId.equals(riding.getUniqueID())) {
					pitch *= -1;
					yaw += 180;
				}

			}
		} else if (!wasStalled) {
			Vector3d movementVector = riding.getMotion();
			if (!(riding instanceof AbstractMinecartEntity))
				movementVector = getPositionVec().subtract(prevPosX, prevPosY, prevPosZ);
			Vector3d motion = movementVector.normalize();

			if (!dataManager.get(INITIAL_ORIENTATION)
				.isPresent() && !world.isRemote) {
				if (motion.length() > 0) {
					Direction facingFromVector = Direction.getFacingFromVector(motion.x, motion.y, motion.z);
					if (facingFromVector.getAxis()
						.isHorizontal())
						setInitialOrientation(facingFromVector);
				}
			}

			if (!rotationLock) {
				if (motion.length() > 0) {
					targetYaw = yawFromVector(motion);
					if (targetYaw < 0)
						targetYaw += 360;
					if (yaw < 0)
						yaw += 360;
				}

				prevYaw = yaw;
				yaw = angleLerp(0.4f, yaw, targetYaw);
				if (Math.abs(AngleHelper.getShortestAngleDiff(yaw, targetYaw)) < 1f)
					yaw = targetYaw;
				else
					rotating = true;
			}
		}

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

	public Vector3d applyRotation(Vector3d localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, getRoll(partialTicks), Axis.X);
		localPos = VecHelper.rotate(localPos, getInitialYaw(), Axis.Y);
		localPos = VecHelper.rotate(localPos, getPitch(partialTicks), Axis.Z);
		localPos = VecHelper.rotate(localPos, getYaw(partialTicks), Axis.Y);
		return localPos;
	}

	public Vector3d reverseRotation(Vector3d localPos, float partialTicks) {
		localPos = VecHelper.rotate(localPos, -getYaw(partialTicks), Axis.Y);
		localPos = VecHelper.rotate(localPos, -getPitch(partialTicks), Axis.Z);
		localPos = VecHelper.rotate(localPos, -getInitialYaw(), Axis.Y);
		localPos = VecHelper.rotate(localPos, -getRoll(partialTicks), Axis.X);
		return localPos;
	}

	public void tickActors() {
		boolean stalledPreviously = contraption.stalled;

		if (!world.isRemote)
			contraption.stalled = false;

		for (MutablePair<BlockInfo, MovementContext> pair : contraption.actors) {
			MovementContext context = pair.right;
			BlockInfo blockInfo = pair.left;
			MovementBehaviour actor = Contraption.getMovement(blockInfo.state);
			Vector3d actorPosition = toGlobalVector(VecHelper.getCenterOf(blockInfo.pos)
				.add(actor.getActiveAreaOffset(context)), 1);
			boolean newPosVisited = false;
			BlockPos gridPosition = new BlockPos(actorPosition);
			Vector3d oldMotion = context.motion;

			if (!context.stall) {
				Vector3d previousPosition = context.position;
				if (previousPosition != null) {
					context.motion = actorPosition.subtract(previousPosition);
					Vector3d relativeMotion = context.motion;
					relativeMotion = reverseRotation(relativeMotion, 1);
					context.relativeMotion = relativeMotion;
					newPosVisited = !new BlockPos(previousPosition).equals(gridPosition)
						|| context.relativeMotion.length() > 0 && context.firstMovement;
				}

				if (getContraption() instanceof BearingContraption) {
					BearingContraption bc = (BearingContraption) getContraption();
					Direction facing = bc.getFacing();
					Vector3d activeAreaOffset = actor.getActiveAreaOffset(context);
					if (activeAreaOffset.mul(VecHelper.axisAlingedPlaneOf(Vector3d.of(facing.getDirectionVec())))
						.equals(Vector3d.ZERO)) {
						if (VecHelper.onSameAxis(blockInfo.pos, BlockPos.ZERO, facing.getAxis())) {
							context.motion = Vector3d.of(facing.getDirectionVec()).scale(facing.getAxis()
								.getCoordinate(roll - prevRoll, yaw - prevYaw, pitch - prevPitch));
							context.relativeMotion = context.motion;
							int timer = context.data.getInt("StationaryTimer");
							if (timer > 0) {
								context.data.putInt("StationaryTimer", timer - 1);
							} else {
								context.data.putInt("StationaryTimer", 20);
								newPosVisited = true;
							}
						}
					}
				}
			}

			context.rotation = v -> applyRotation(v, 1);
			context.position = actorPosition;

			if (actor.isActive(context)) {
				if (newPosVisited && !context.stall) {
					actor.visitNewPosition(context, gridPosition);
					context.firstMovement = false;
				}
				if (!oldMotion.equals(context.motion))
					actor.onSpeedChanged(context, oldMotion, context.motion);
				actor.tick(context);
				contraption.stalled |= context.stall;
			}
		}

		if (!world.isRemote) {
			if (!stalledPreviously && contraption.stalled) {
				setMotion(Vector3d.ZERO);
				if (getController() != null)
					getController().onStall();
				AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
					new ContraptionStallPacket(getEntityId(), getX(), getY(), getZ(), yaw, pitch, roll));
			}
			dataManager.set(STALLED, contraption.stalled);
		} else {
			contraption.stalled = isStalled();
		}
	}

	public void move(double x, double y, double z) {
		setPosition(x + getX(), getY() + y, getZ() + z);
	}

	private Vector3d getAnchorVec() {
		if (contraption != null && contraption.getType() == AllContraptionTypes.MOUNTED)
			return new Vector3d(getX() - .5, getY(), getZ() - .5);
		return getPositionVec();
	}

	public void rotateTo(double roll, double yaw, double pitch) {
		rotate(getShortestAngleDiff(this.roll, roll), getShortestAngleDiff(this.yaw, yaw),
			getShortestAngleDiff(this.pitch, pitch));
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (key == INITIAL_ORIENTATION)
			startAtInitialYaw();
	}

	public void rotate(double roll, double yaw, double pitch) {
		this.yaw += yaw;
		this.pitch += pitch;
		this.roll += roll;
	}

	@Override
	public void setPosition(double x, double y, double z) {
		super.setPosition(x, y, z);
		if (contraption != null) {
			AxisAlignedBB cbox = contraption.getBoundingBox();
			if (cbox != null) {
				Vector3d actualVec = getAnchorVec();
				this.setBoundingBox(cbox.offset(actualVec));
			}
		}
	}

	@Override
	public void stopRiding() {
		if (!world.isRemote)
			if (isAlive())
				disassemble();
		super.stopRiding();
	}

	public static float yawFromVector(Vector3d vec) {
		return (float) ((3 * Math.PI / 2 + Math.atan2(vec.z, vec.x)) / Math.PI * 180);
	}

	public static float pitchFromVector(Vector3d vec) {
		return (float) ((Math.acos(vec.y)) / Math.PI * 180);
	}

	public float getYaw(float partialTicks) {
		return (getRidingEntity() == null ? 1 : -1)
			* (partialTicks == 1.0F ? yaw : angleLerp(partialTicks, prevYaw, yaw));
	}

	public float getPitch(float partialTicks) {
		return partialTicks == 1.0F ? pitch : angleLerp(partialTicks, prevPitch, pitch);
	}

	public float getRoll(float partialTicks) {
		return partialTicks == 1.0F ? roll : angleLerp(partialTicks, prevRoll, roll);
	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<ContraptionEntity> entityBuilder = (EntityType.Builder<ContraptionEntity>) builder;
		return entityBuilder.size(1, 1);
	}

	@Override
	protected void registerData() {
		this.dataManager.register(STALLED, false);
		this.dataManager.register(COUPLING, Optional.empty());
		this.dataManager.register(INITIAL_ORIENTATION, Optional.empty());
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		initialized = compound.getBoolean("Initialized");
		contraption = Contraption.fromNBT(world, compound.getCompound("Contraption"));
		dataManager.set(STALLED, compound.getBoolean("Stalled"));

		if (compound.contains("InitialOrientation"))
			setInitialOrientation(NBTHelper.readEnum(compound, "InitialOrientation", Direction.class));
		if (compound.contains("ForceYaw"))
			startAtYaw(compound.getFloat("ForceYaw"));

		ListNBT vecNBT = compound.getList("CachedMotion", 6);
		if (!vecNBT.isEmpty()) {
			motionBeforeStall = new Vector3d(vecNBT.getDouble(0), vecNBT.getDouble(1), vecNBT.getDouble(2));
			if (!motionBeforeStall.equals(Vector3d.ZERO))
				targetYaw = prevYaw = yaw += yawFromVector(motionBeforeStall);
			setMotion(Vector3d.ZERO);
		}

		if (compound.contains("Controller"))
			controllerPos = NBTUtil.readBlockPos(compound.getCompound("Controller"));
		setCouplingId(
			compound.contains("OnCoupling") ? NBTUtil.readUniqueId(NBTHelper.getINBT(compound, "OnCoupling")) : null);
	}

	public void startAtInitialYaw() {
		startAtYaw(getInitialYaw());
	}

	public void startAtYaw(float yaw) {
		targetYaw = this.yaw = prevYaw = yaw;
		forceAngle = true;
	}

	public void checkController() {
		if (controllerPos == null)
			return;
		if (!world.isBlockPresent(controllerPos))
			return;
		IControlContraption controller = getController();
		if (controller == null) {
			remove();
			return;
		}
		if (controller.isAttachedTo(this))
			return;
		controller.attach(this);
		if (world.isRemote)
			setPosition(getX(), getY(), getZ());
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		if (contraption != null)
			compound.put("Contraption", contraption.writeNBT());
		if (!stationary && motionBeforeStall != null)
			compound.put("CachedMotion",
				newDoubleNBTList(motionBeforeStall.x, motionBeforeStall.y, motionBeforeStall.z));
		if (controllerPos != null)
			compound.put("Controller", NBTUtil.writeBlockPos(controllerPos));

		Optional<Direction> optional = dataManager.get(INITIAL_ORIENTATION);
		if (optional.isPresent())
			NBTHelper.writeEnum(compound, "InitialOrientation", optional.get());
		if (forceAngle) {
			compound.putFloat("ForceYaw", yaw);
			forceAngle = false;
		}

		compound.putBoolean("Stalled", isStalled());
		compound.putBoolean("Initialized", initialized);

		if (getCouplingId() != null)
			compound.put("OnCoupling", NBTUtil.fromUuid(getCouplingId()));
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		CompoundNBT compound = new CompoundNBT();
		writeAdditional(compound);
		buffer.writeCompoundTag(compound);
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		readAdditional(additionalData.readCompoundTag());
	}

	public void disassemble() {
		if (!isAlive())
			return;
		if (getContraption() != null) {
			remove();
			BlockPos offset = new BlockPos(getAnchorVec().add(.5, .5, .5));
			Vector3d rotation = getRotationVec().add(0, getInitialYaw(), 0);
			StructureTransform transform = new StructureTransform(offset, rotation);
			contraption.addBlocksToWorld(world, transform);
			contraption.addPassengersToWorld(world, transform, getPassengers());
			removePassengers();

			for (Entity entity : collidingEntities) {
				Vector3d positionVec = getPositionVec();
				Vector3d localVec = entity.getPositionVec()
					.subtract(positionVec);
				localVec = VecHelper.rotate(localVec, rotation.scale(-1));
				Vector3d transformed = transform.apply(localVec);
				entity.setPositionAndUpdate(transformed.x, transformed.y, transformed.z);
			}

		}
	}

	@Override
	public void onKillCommand() {
		removePassengers();
		super.onKillCommand();
	}

	@Override
	protected void outOfWorld() {
		removePassengers();
		super.outOfWorld();
	}

	@Override
	public void onRemovedFromWorld() {
		super.onRemovedFromWorld();
		if (world != null && world.isRemote)
			return;
		getPassengers().forEach(Entity::remove);
	}

	@Override
	protected void doWaterSplashEffect() {}

	@SuppressWarnings("deprecation")
	@Override
	public CompoundNBT writeWithoutTypeId(CompoundNBT nbt) {
		if (isSerializingFurnaceCart)
			return nbt;

		Vector3d vec = getPositionVec();
		List<Entity> passengers = getPassengers();

		for (Entity entity : passengers) {
			// setPos has world accessing side-effects when removed == false
			entity.removed = true;

			// Gather passengers into same chunk when saving
			Vector3d prevVec = entity.getPositionVec();
			entity.setPos(vec.x, prevVec.y, vec.z);

			// Super requires all passengers to not be removed in order to write them to the
			// tag
			entity.removed = false;
		}

		CompoundNBT tag = super.writeWithoutTypeId(nbt);
		return tag;
	}

	public Contraption getContraption() {
		return contraption;
	}

	public boolean isStalled() {
		return dataManager.get(STALLED);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch,
		int posRotationIncrements, boolean teleport) {
		// Stationary Anchors are responsible for keeping position and motion in sync
		// themselves.
		if (stationary)
			return;
		super.setPositionAndRotationDirect(x, y, z, yaw, pitch, posRotationIncrements, teleport);
	}

	@OnlyIn(Dist.CLIENT)
	static void handleStallPacket(ContraptionStallPacket packet) {
		Entity entity = Minecraft.getInstance().world.getEntityByID(packet.entityID);
		if (!(entity instanceof ContraptionEntity))
			return;
		ContraptionEntity ce = (ContraptionEntity) entity;
		if (ce.getRidingEntity() == null) {
			ce.setPos(packet.x, packet.y, packet.z);
		}
		ce.yaw = packet.yaw;
		ce.pitch = packet.pitch;
		ce.roll = packet.roll;
	}

	@Override
	// Make sure nothing can move contraptions out of the way
	public void setMotion(Vector3d motionIn) {}

	@Override
	public void setPositionAndUpdate(double x, double y, double z) {
		if (!stationary)
			super.setPositionAndUpdate(x, y, z);
	}

	@Override
	public PushReaction getPushReaction() {
		return PushReaction.IGNORE;
	}

	public void setContraptionMotion(Vector3d vec) {
		super.setMotion(vec);
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return false;
	}

	public void setInitialOrientation(Direction direction) {
		dataManager.set(INITIAL_ORIENTATION, Optional.of(direction));
	}

	public Optional<Direction> getInitialOrientation() {
		return dataManager.get(INITIAL_ORIENTATION);
	}

	public float getInitialYaw() {
		return dataManager.get(INITIAL_ORIENTATION)
			.orElse(Direction.SOUTH)
			.getHorizontalAngle();
	}

	public Vector3d getRotationVec() {
		return new Vector3d(getRoll(1), getYaw(1), getPitch(1));
	}

	public Vector3d getPrevRotationVec() {
		return new Vector3d(getRoll(0), getYaw(0), getPitch(0));
	}

	public Vector3d getPrevPositionVec() {
		return new Vector3d(prevPosX, prevPosY, prevPosZ);
	}

	public Vector3d getContactPointMotion(Vector3d globalContactPoint) {
		if (prevPosInvalid)
			return Vector3d.ZERO;
		Vector3d contactPoint = toGlobalVector(toLocalVector(globalContactPoint, 0), 1);
		return contactPoint.subtract(globalContactPoint)
			.add(getPositionVec().subtract(getPrevPositionVec()));
	}

	public boolean canCollideWith(Entity e) {
		if (e instanceof PlayerEntity && e.isSpectator())
			return false;
		if (e.noClip)
			return false;
		if (e instanceof HangingEntity)
			return false;
		if (e instanceof AbstractMinecartEntity)
			return false;
		if (e instanceof SuperGlueEntity)
			return false;
		if (e instanceof SeatEntity)
			return false;
		if (e instanceof ProjectileEntity)
			return false;
		if (e.getRidingEntity() != null)
			return false;

		Entity riding = this.getRidingEntity();
		while (riding != null) {
			if (riding == e)
				return false;
			riding = riding.getRidingEntity();
		}

		return e.getPushReaction() == PushReaction.NORMAL;
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
	public boolean isOnePlayerRiding() {
		return false;
	}

}
