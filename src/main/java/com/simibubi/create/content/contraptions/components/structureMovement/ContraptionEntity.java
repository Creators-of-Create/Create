package com.simibubi.create.content.contraptions.components.structureMovement;

import static com.simibubi.create.foundation.utility.AngleHelper.angleLerp;
import static com.simibubi.create.foundation.utility.AngleHelper.getShortestAngleDiff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.ImmutableSet;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.components.actors.SeatEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerTileEntity.CartMovementMode;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.MountedContraption;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.HangingEntity;
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
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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
	protected Vec3d motionBeforeStall;
	protected boolean stationary;
	protected boolean initialized;
	final List<Entity> collidingEntities = new ArrayList<>();

	private static final Ingredient FUEL_ITEMS = Ingredient.fromItems(Items.COAL, Items.CHARCOAL);
	private static final DataParameter<Boolean> STALLED =
		EntityDataManager.createKey(ContraptionEntity.class, DataSerializers.BOOLEAN);

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
		motionBeforeStall = Vec3d.ZERO;
		stationary = entityTypeIn == AllEntityTypes.STATIONARY_CONTRAPTION.get();
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
		contraption.seatMapping.put(passenger.getUniqueID(), seatIndex);
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionSeatMappingPacket(getEntityId(), contraption.seatMapping));
	}

	@Override
	protected void removePassenger(Entity passenger) {
		Vec3d transformedVector = getPassengerPosition(passenger);
		super.removePassenger(passenger);
		if (world.isRemote)
			return;
		if (transformedVector != null)
			passenger.getPersistentData()
				.put("ContraptionDismountLocation", VecHelper.writeNBT(transformedVector));
		contraption.seatMapping.remove(passenger.getUniqueID());
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
			new ContraptionSeatMappingPacket(getEntityId(), contraption.seatMapping));
	}

	@Override
	public void updatePassengerPosition(Entity passenger, IMoveCallback callback) {
		if (!isPassenger(passenger))
			return;
		Vec3d transformedVector = getPassengerPosition(passenger);
		if (transformedVector == null)
			return;
		callback.accept(passenger, transformedVector.x, transformedVector.y, transformedVector.z);
	}

	protected Vec3d getPassengerPosition(Entity passenger) {
		AxisAlignedBB bb = passenger.getBoundingBox();
		double ySize = bb.getYSize();
		BlockPos seat = contraption.getSeat(passenger.getUniqueID());
		if (seat == null)
			return null;
		Vec3d transformedVector = toGlobalVector(new Vec3d(seat).add(.5, passenger.getYOffset() + ySize - .15f, .5))
			.add(VecHelper.getCenterOf(BlockPos.ZERO))
			.subtract(0.5, ySize, 0.5);
		return transformedVector;
	}

	@Override
	protected boolean canFitPassenger(Entity p_184219_1_) {
		return getPassengers().size() < contraption.seats.size();
	}

	public boolean handlePlayerInteraction(PlayerEntity player, BlockPos localPos, Direction side,
		Hand interactionHand) {
		int indexOfSeat = contraption.seats.indexOf(localPos);
		if (indexOfSeat == -1)
			return false;

		// Eject potential existing passenger
		for (Entry<UUID, Integer> entry : contraption.seatMapping.entrySet()) {
			if (entry.getValue() != indexOfSeat)
				continue;
			for (Entity entity : getPassengers()) {
				if (!entry.getKey().equals(entity.getUniqueID()))
					continue;
				if (entity instanceof PlayerEntity)
					return false;
				if (!world.isRemote) {
					Vec3d transformedVector = getPassengerPosition(entity);
					entity.stopRiding();
					if (transformedVector != null)
						entity.setPositionAndUpdate(transformedVector.x, transformedVector.y, transformedVector.z);
				}

			}
		}

		if (world.isRemote)
			return true;
		addSittingPassenger(player, indexOfSeat);
		return true;
	}

	public Vec3d toGlobalVector(Vec3d localVec) {
		Vec3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		localVec = localVec.subtract(rotationOffset);
		localVec = VecHelper.rotate(localVec, getRotationVec());
		localVec = localVec.add(rotationOffset)
			.add(getAnchorVec());
		return localVec;
	}

	public Vec3d toLocalVector(Vec3d globalVec) {
		Vec3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
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
			setMotion(Vec3d.ZERO);

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

		if (contraption instanceof MountedContraption) {
			rotationLock = ((MountedContraption) contraption).rotationMode == CartMovementMode.ROTATION_LOCKED;
			pauseWhileRotating = ((MountedContraption) contraption).rotationMode == CartMovementMode.ROTATE_PAUSED;
		}

		Entity riding = e;
		while (riding.getRidingEntity() != null)
			riding = riding.getRidingEntity();
		Vec3d movementVector = riding.getMotion();
		if (riding instanceof BoatEntity)
			movementVector = getPositionVec().subtract(prevPosX, prevPosY, prevPosZ);
		Vec3d motion = movementVector.normalize();
		boolean rotating = false;

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
			motionBeforeStall = Vec3d.ZERO;
		}

		if (!isStalled() && (riding instanceof FurnaceMinecartEntity)) {
			FurnaceMinecartEntity furnaceCart = (FurnaceMinecartEntity) riding;
			CompoundNBT nbt = furnaceCart.serializeNBT();
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

	public void tickActors(Vec3d movementVector) {
		Vec3d rotationVec = getRotationVec();
		Vec3d reversedRotationVec = rotationVec.scale(-1);
		Vec3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		boolean stalledPreviously = contraption.stalled;

		if (!world.isRemote)
			contraption.stalled = false;

		for (MutablePair<BlockInfo, MovementContext> pair : contraption.actors) {
			MovementContext context = pair.right;
			BlockInfo blockInfo = pair.left;
			MovementBehaviour actor = Contraption.getMovement(blockInfo.state);

			Vec3d actorPosition = new Vec3d(blockInfo.pos);
			actorPosition = actorPosition.add(actor.getActiveAreaOffset(context));
			actorPosition = VecHelper.rotate(actorPosition, rotationVec);
			actorPosition = actorPosition.add(rotationOffset)
				.add(getAnchorVec());

			boolean newPosVisited = false;
			BlockPos gridPosition = new BlockPos(actorPosition);

			if (!context.stall) {
				Vec3d previousPosition = context.position;
				if (previousPosition != null) {
					context.motion = actorPosition.subtract(previousPosition);
					Vec3d relativeMotion = context.motion;
					relativeMotion = VecHelper.rotate(relativeMotion, reversedRotationVec);
					context.relativeMotion = relativeMotion;
					newPosVisited = !new BlockPos(previousPosition).equals(gridPosition)
						|| context.relativeMotion.length() > 0 && context.firstMovement;
				}

				if (getContraption() instanceof BearingContraption) {
					BearingContraption bc = (BearingContraption) getContraption();
					Direction facing = bc.getFacing();
					Vec3d activeAreaOffset = actor.getActiveAreaOffset(context);
					if (activeAreaOffset.mul(VecHelper.planeByNormal(new Vec3d(facing.getDirectionVec())))
						.equals(Vec3d.ZERO)) {
						if (VecHelper.onSameAxis(blockInfo.pos, BlockPos.ZERO, facing.getAxis())) {
							context.motion = new Vec3d(facing.getDirectionVec()).scale(facing.getAxis()
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
				actor.tick(context);
				contraption.stalled |= context.stall;
			}
		}

		if (!world.isRemote) {
			if (!stalledPreviously && contraption.stalled) {
				setMotion(Vec3d.ZERO);
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

	private Vec3d getAnchorVec() {
		if (contraption != null && contraption.getType() == AllContraptionTypes.MOUNTED)
			return new Vec3d(getX() - .5, getY(), getZ() - .5);
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
				Vec3d actualVec = getAnchorVec();
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

	public static float yawFromVector(Vec3d vec) {
		return (float) ((3 * Math.PI / 2 + Math.atan2(vec.z, vec.x)) / Math.PI * 180);
	}

	public static float pitchFromVector(Vec3d vec) {
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
			motionBeforeStall = new Vec3d(vecNBT.getDouble(0), vecNBT.getDouble(1), vecNBT.getDouble(2));
			if (!motionBeforeStall.equals(Vec3d.ZERO))
				targetYaw = prevYaw = yaw += yawFromVector(motionBeforeStall);
			setMotion(Vec3d.ZERO);
		}
		if (compound.contains("Controller"))
			controllerPos = NBTUtil.readBlockPos(compound.getCompound("Controller"));
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
			Vec3d rotation = getRotationVec();
			setBoundingBox(new AxisAlignedBB(0, 300, 0, 0, 300, 0));
			contraption.addBlocksToWorld(world, offset, rotation, getPassengers());
//			preventMovedEntitiesFromGettingStuck();
		}
	}

	@Override
	protected void doWaterSplashEffect() {}

	public void preventMovedEntitiesFromGettingStuck() {
		Vec3d stuckTest = new Vec3d(0, -2, 0);
		for (Entity e : collidingEntities) {
			e.fallDistance = 0;
			e.onGround = true;

			Vec3d vec = stuckTest;
			AxisAlignedBB axisalignedbb = e.getBoundingBox()
				.offset(0, 2, 0);
			ISelectionContext iselectioncontext = ISelectionContext.forEntity(this);
			VoxelShape voxelshape = e.world.getWorldBorder()
				.getShape();
			Stream<VoxelShape> stream =
				VoxelShapes.compare(voxelshape, VoxelShapes.create(axisalignedbb.shrink(1.0E-7D)), IBooleanFunction.AND)
					? Stream.empty()
					: Stream.of(voxelshape);
			Stream<VoxelShape> stream1 =
				this.world.getEmptyCollisionShapes(e, axisalignedbb.expand(vec), ImmutableSet.of());
			ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(stream1, stream));
			Vec3d vec3d = vec.lengthSquared() == 0.0D ? vec
				: collideBoundingBoxHeuristically(this, vec, axisalignedbb, e.world, iselectioncontext,
					reuseablestream);
			boolean flag = vec.x != vec3d.x;
			boolean flag1 = vec.y != vec3d.y;
			boolean flag2 = vec.z != vec3d.z;
			boolean flag3 = e.onGround || flag1 && vec.y < 0.0D;
			if (this.stepHeight > 0.0F && flag3 && (flag || flag2)) {
				Vec3d vec3d1 = collideBoundingBoxHeuristically(e, new Vec3d(vec.x, (double) e.stepHeight, vec.z),
					axisalignedbb, e.world, iselectioncontext, reuseablestream);
				Vec3d vec3d2 = collideBoundingBoxHeuristically(e, new Vec3d(0.0D, (double) e.stepHeight, 0.0D),
					axisalignedbb.expand(vec.x, 0.0D, vec.z), e.world, iselectioncontext, reuseablestream);
				if (vec3d2.y < (double) e.stepHeight) {
					Vec3d vec3d3 = collideBoundingBoxHeuristically(e, new Vec3d(vec.x, 0.0D, vec.z),
						axisalignedbb.offset(vec3d2), e.world, iselectioncontext, reuseablestream).add(vec3d2);
					if (horizontalMag(vec3d3) > horizontalMag(vec3d1)) {
						vec3d1 = vec3d3;
					}
				}

				if (horizontalMag(vec3d1) > horizontalMag(vec3d)) {
					vec3d = vec3d1.add(collideBoundingBoxHeuristically(e, new Vec3d(0.0D, -vec3d1.y + vec.y, 0.0D),
						axisalignedbb.offset(vec3d1), e.world, iselectioncontext, reuseablestream));
				}
			}

			vec = vec3d.subtract(stuckTest);
			if (vec.equals(Vec3d.ZERO))
				continue;
			e.setPosition(e.getX() + vec.x, e.getY() + vec.y, e.getZ() + vec.z);
		}
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
	public void setMotion(Vec3d motionIn) {}

	@Override
	public void setPositionAndUpdate(double x, double y, double z) {
		if (!stationary)
			super.setPositionAndUpdate(x, y, z);
	}

	@Override
	public PushReaction getPushReaction() {
		return PushReaction.IGNORE;
	}

	public void setContraptionMotion(Vec3d vec) {
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

	public Vec3d getRotationVec() {
		return new Vec3d(getRoll(1), getYaw(1), getPitch(1));
	}

	public Vec3d getPrevRotationVec() {
		return new Vec3d(getRoll(0), getYaw(0), getPitch(0));
	}

	public Vec3d getPrevPositionVec() {
		return new Vec3d(prevPosX, prevPosY, prevPosZ);
	}

	public boolean canCollideWith(Entity e) {
		if (e instanceof PlayerEntity && e.isSpectator())
			return false;
		if (e.noClip)
			return false;
		if (e instanceof HangingEntity)
			return false;
		if (e instanceof SuperGlueEntity)
			return false;
		if (e instanceof SeatEntity)
			return false;
		if (e instanceof IProjectile)
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

}
