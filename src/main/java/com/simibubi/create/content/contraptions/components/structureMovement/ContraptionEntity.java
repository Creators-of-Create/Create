package com.simibubi.create.content.contraptions.components.structureMovement;

import static com.simibubi.create.foundation.utility.AngleHelper.angleLerp;
import static com.simibubi.create.foundation.utility.AngleHelper.getShortestAngleDiff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.projectile.ProjectileEntity;
import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.components.actors.SeatEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerTileEntity.CartMovementMode;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.MountedContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionSeatMappingPacket;
import com.simibubi.create.content.contraptions.components.structureMovement.train.MinecartCoupling;
import com.simibubi.create.content.contraptions.components.structureMovement.train.MinecartCouplingHandler;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ProjectileEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

public class ContraptionEntity extends Entity implements IEntityAdditionalSpawnData {

	protected Contraption contraption;
	protected float initialAngle;
	protected float forcedAngle;
	protected BlockPos controllerPos;
	protected Vector3d motionBeforeStall;
	protected boolean stationary;
	protected boolean initialized;
	final List<Entity> collidingEntities = new ArrayList<>();
	private boolean isSerializingFurnaceCart;
	private boolean attachedExtraInventories;

	private static final Ingredient FUEL_ITEMS = Ingredient.fromItems(Items.COAL, Items.CHARCOAL);
	private static final DataParameter<Boolean> STALLED =
		EntityDataManager.createKey(ContraptionEntity.class, DataSerializers.BOOLEAN);

	private static final DataParameter<Optional<UUID>> COUPLING =
		EntityDataManager.createKey(ContraptionEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Optional<UUID>> COUPLED_CART =
		EntityDataManager.createKey(ContraptionEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);

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
		forcedAngle = -1;
	}

	public static ContraptionEntity createMounted(World world, Contraption contraption, float initialAngle) {
		ContraptionEntity entity = new ContraptionEntity(AllEntityTypes.CONTRAPTION.get(), world);
		entity.contraptionCreated(contraption);
		entity.initialAngle = initialAngle;
		entity.forceYaw(initialAngle);
		return entity;
	}

	public static ContraptionEntity createMounted(World world, Contraption contraption, float initialAngle,
		Direction facing) {
		ContraptionEntity entity = createMounted(world, contraption, initialAngle);
		entity.forcedAngle = facing.getHorizontalAngle();
		entity.forceYaw(entity.forcedAngle);
		return entity;
	}

	public static ContraptionEntity createStationary(World world, Contraption contraption) {
		ContraptionEntity entity = new ContraptionEntity(AllEntityTypes.STATIONARY_CONTRAPTION.get(), world);
		entity.contraptionCreated(contraption);
		return entity;
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
	public void updatePassengerPosition(Entity passenger, IMoveCallback callback) {
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
		Vector3d transformedVector = toGlobalVector(Vector3d.of(seat).add(.5, passenger.getYOffset() + ySize - .15f, .5))
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

	public Vector3d toGlobalVector(Vector3d localVec) {
		Vector3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		localVec = localVec.subtract(rotationOffset);
		localVec = VecHelper.rotate(localVec, getRotationVec());
		localVec = localVec.add(rotationOffset)
			.add(getAnchorVec());
		return localVec;
	}

	public Vector3d toLocalVector(Vector3d globalVec) {
		Vector3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		globalVec = globalVec.subtract(getAnchorVec())
			.subtract(rotationOffset);
		globalVec = VecHelper.rotate(globalVec, getRotationVec().scale(-1));
		globalVec = globalVec.add(rotationOffset);
		return globalVec;
	}

	@Override
	public void tick() {
		if (contraption == null) {
			remove();
			return;
		}

		if (!initialized)
			contraptionInitialize();

		checkController();

		Entity mountedEntity = getRidingEntity();
		if (mountedEntity != null) {
			tickAsPassenger(mountedEntity);
			return;
		}

		if (getMotion().length() < 1 / 4098f)
			setMotion(Vector3d.ZERO);

		move(getMotion().x, getMotion().y, getMotion().z);
		if (ContraptionCollider.collideBlocks(this))
			getController().collided();

		tickActors(getPositionVec().subtract(prevPosX, prevPosY, prevPosZ));

		prevYaw = yaw;
		prevPitch = pitch;
		prevRoll = roll;

		super.tick();
	}

	public void tickAsPassenger(Entity e) {
		boolean rotationLock = false;
		boolean pauseWhileRotating = false;
		boolean rotating = false;

		Entity riding = e;
		while (riding.getRidingEntity() != null)
			riding = riding.getRidingEntity();
		if (!attachedExtraInventories) {
			contraption.addExtraInventories(riding);
			attachedExtraInventories = true;
		}

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
				}
			}

			rotationLock = mountedContraption.rotationMode == CartMovementMode.ROTATION_LOCKED;
			pauseWhileRotating = mountedContraption.rotationMode == CartMovementMode.ROTATE_PAUSED;
		}

		Vector3d movementVector = riding.getMotion();
		if (!isOnCoupling) {
			if (riding instanceof BoatEntity)
				movementVector = getPositionVec().subtract(prevPosX, prevPosY, prevPosZ);
			Vector3d motion = movementVector.normalize();

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

		boolean wasStalled = isStalled();
		if (!rotating || !pauseWhileRotating)
			tickActors(movementVector);
		if (isStalled()) {
			if (!wasStalled)
				motionBeforeStall = riding.getMotion();
			riding.setMotion(0, 0, 0);
		}

		if (wasStalled && !isStalled()) {
			riding.setMotion(motionBeforeStall);
			motionBeforeStall = Vector3d.ZERO;
		}

		if (!isStalled() && (riding instanceof FurnaceMinecartEntity)) {
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

		super.tick();
	}

	public void tickActors(Vector3d movementVector) {
		Vector3d rotationVec = getRotationVec();
		Vector3d reversedRotationVec = rotationVec.scale(-1);
		Vector3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		boolean stalledPreviously = contraption.stalled;

		if (!world.isRemote)
			contraption.stalled = false;

		for (MutablePair<BlockInfo, MovementContext> pair : contraption.actors) {
			MovementContext context = pair.right;
			BlockInfo blockInfo = pair.left;
			MovementBehaviour actor = Contraption.getMovement(blockInfo.state);

			Vector3d actorPosition = Vector3d.of(blockInfo.pos);
			actorPosition = actorPosition.add(actor.getActiveAreaOffset(context));
			actorPosition = VecHelper.rotate(actorPosition, rotationVec);
			actorPosition = actorPosition.add(rotationOffset)
				.add(getAnchorVec());

			boolean newPosVisited = false;
			BlockPos gridPosition = new BlockPos(actorPosition);
			Vector3d oldMotion = context.motion;

			if (!context.stall) {
				Vector3d previousPosition = context.position;
				if (previousPosition != null) {
					context.motion = actorPosition.subtract(previousPosition);
					Vector3d relativeMotion = context.motion;
					relativeMotion = VecHelper.rotate(relativeMotion, reversedRotationVec);
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

			context.rotation = rotationVec;
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
			* (partialTicks == 1.0F ? yaw : angleLerp(partialTicks, prevYaw, yaw)) + initialAngle;
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
		this.dataManager.register(COUPLED_CART, Optional.empty());
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		initialized = compound.getBoolean("Initialized");
		contraption = Contraption.fromNBT(world, compound.getCompound("Contraption"));
		initialAngle = compound.getFloat("InitialAngle");
		forceYaw(compound.contains("ForcedYaw") ? compound.getFloat("ForcedYaw") : initialAngle);
		dataManager.set(STALLED, compound.getBoolean("Stalled"));
		ListNBT vecNBT = compound.getList("CachedMotion", 6);
		if (!vecNBT.isEmpty()) {
			motionBeforeStall = new Vector3d(vecNBT.getDouble(0), vecNBT.getDouble(1), vecNBT.getDouble(2));
			if (!motionBeforeStall.equals(Vector3d.ZERO))
				targetYaw = prevYaw = yaw += yawFromVector(motionBeforeStall);
			setMotion(Vector3d.ZERO);
		}
		if (compound.contains("Controller"))
			controllerPos = NBTUtil.readBlockPos(compound.getCompound("Controller"));

		if (compound.contains("OnCoupling")) {
			setCouplingId(NBTUtil.readUniqueId(compound.getCompound("OnCoupling")));
			setCoupledCart(NBTUtil.readUniqueId(compound.getCompound("CoupledCart")));
		} else {
			setCouplingId(null);
			setCoupledCart(null);
		}
	}

	public void forceYaw(float forcedYaw) {
		targetYaw = yaw = prevYaw = forcedYaw;
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
		if (forcedAngle != -1)
			compound.putFloat("ForcedYaw", forcedAngle);

		compound.putFloat("InitialAngle", initialAngle);
		compound.putBoolean("Stalled", isStalled());
		compound.putBoolean("Initialized", initialized);

		if (getCouplingId() != null) {
			compound.put("OnCoupling", NBTUtil.fromUuid(getCouplingId()));
			compound.put("CoupledCart", NBTUtil.fromUuid(getCoupledCart()));
		}
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
			Vector3d rotation = getRotationVec();
			StructureTransform transform = new StructureTransform(offset, rotation);
			contraption.addBlocksToWorld(world, transform);
			contraption.addPassengersToWorld(world, transform, getPassengers());
			removePassengers();

			for (Entity entity : collidingEntities) {
				Vector3d positionVec = getPositionVec();
				Vector3d localVec = entity.getPositionVec()
					.subtract(positionVec);
				localVec = VecHelper.rotate(localVec, getRotationVec().scale(-1));
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

	public float getInitialAngle() {
		return initialAngle;
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
		Vector3d positionVec = getPositionVec();
		Vector3d conMotion = positionVec.subtract(getPrevPositionVec());
		Vector3d conAngularMotion = getRotationVec().subtract(getPrevRotationVec());
		Vector3d contraptionCentreOffset = stationary ? VecHelper.getCenterOf(BlockPos.ZERO) : Vector3d.ZERO.add(0, 0.5, 0);
		Vector3d contactPoint = globalContactPoint.subtract(contraptionCentreOffset)
			.subtract(positionVec);
		contactPoint = VecHelper.rotate(contactPoint, conAngularMotion.x, conAngularMotion.y, conAngularMotion.z);
		contactPoint = contactPoint.add(positionVec)
			.add(contraptionCentreOffset)
			.add(conMotion);
		return contactPoint.subtract(globalContactPoint);
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

	@Nullable
	public UUID getCoupledCart() {
		Optional<UUID> uuid = dataManager.get(COUPLED_CART);
		return uuid.isPresent() ? uuid.get() : null;
	}

	public void setCoupledCart(UUID id) {
		dataManager.set(COUPLED_CART, Optional.ofNullable(id));
	}

}
