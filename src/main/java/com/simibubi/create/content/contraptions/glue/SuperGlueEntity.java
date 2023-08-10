package com.simibubi.create.content.contraptions.glue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.BlockMovementChecks;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.contraptions.chassis.AbstractChassisBlock;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.schematics.requirement.ISpecialEntityItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class SuperGlueEntity extends Entity implements IEntityAdditionalSpawnData, ISpecialEntityItemRequirement {

	public static AABB span(BlockPos startPos, BlockPos endPos) {
		return new AABB(startPos, endPos).expandTowards(1, 1, 1);
	}

	public static boolean isGlued(LevelAccessor level, BlockPos blockPos, Direction direction,
		Set<SuperGlueEntity> cached) {
		BlockPos targetPos = blockPos.relative(direction);
		if (cached != null)
			for (SuperGlueEntity glueEntity : cached)
				if (glueEntity.contains(blockPos) && glueEntity.contains(targetPos))
					return true;
		for (SuperGlueEntity glueEntity : level.getEntitiesOfClass(SuperGlueEntity.class,
			span(blockPos, targetPos).inflate(16))) {
			if (!glueEntity.contains(blockPos) || !glueEntity.contains(targetPos))
				continue;
			if (cached != null)
				cached.add(glueEntity);
			return true;
		}
		return false;
	}

	public static List<SuperGlueEntity> collectCropped(Level level, AABB bb) {
		List<SuperGlueEntity> glue = new ArrayList<>();
		for (SuperGlueEntity glueEntity : level.getEntitiesOfClass(SuperGlueEntity.class, bb)) {
			AABB glueBox = glueEntity.getBoundingBox();
			AABB intersect = bb.intersect(glueBox);
			if (intersect.getXsize() * intersect.getYsize() * intersect.getZsize() == 0)
				continue;
			if (Mth.equal(intersect.getSize(), 1))
				continue;
			glue.add(new SuperGlueEntity(level, intersect));
		}
		return glue;
	}

	public SuperGlueEntity(EntityType<?> type, Level world) {
		super(type, world);
	}

	public SuperGlueEntity(Level world, AABB boundingBox) {
		this(AllEntityTypes.SUPER_GLUE.get(), world);
		setBoundingBox(boundingBox);
		resetPositionToBB();
	}

	public void resetPositionToBB() {
		AABB bb = getBoundingBox();
		setPosRaw(bb.getCenter().x, bb.minY, bb.getCenter().z);
	}

	@Override
	protected void defineSynchedData() {}

	public static boolean isValidFace(Level world, BlockPos pos, Direction direction) {
		BlockState state = world.getBlockState(pos);
		if (BlockMovementChecks.isBlockAttachedTowards(state, world, pos, direction))
			return true;
		if (!BlockMovementChecks.isMovementNecessary(state, world, pos))
			return false;
		if (BlockMovementChecks.isNotSupportive(state, direction))
			return false;
		return true;
	}

	public static boolean isSideSticky(Level world, BlockPos pos, Direction direction) {
		BlockState state = world.getBlockState(pos);
		if (AllBlocks.STICKY_MECHANICAL_PISTON.has(state))
			return state.getValue(DirectionalKineticBlock.FACING) == direction;

		if (AllBlocks.STICKER.has(state))
			return state.getValue(DirectionalBlock.FACING) == direction;

		if (state.getBlock() == Blocks.SLIME_BLOCK)
			return true;
		if (state.getBlock() == Blocks.HONEY_BLOCK)
			return true;

		if (AllBlocks.CART_ASSEMBLER.has(state))
			return Direction.UP == direction;

		if (AllBlocks.GANTRY_CARRIAGE.has(state))
			return state.getValue(DirectionalKineticBlock.FACING) == direction;

		if (state.getBlock() instanceof BearingBlock) {
			return state.getValue(DirectionalKineticBlock.FACING) == direction;
		}

		if (state.getBlock() instanceof AbstractChassisBlock) {
			BooleanProperty glueableSide = ((AbstractChassisBlock) state.getBlock()).getGlueableSide(state, direction);
			if (glueableSide == null)
				return false;
			return state.getValue(glueableSide);
		}

		return false;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (getBoundingBox().getXsize() == 0)
			discard();
	}

	@Override
	public void setPos(double x, double y, double z) {
		AABB bb = getBoundingBox();
		setPosRaw(x, y, z);
		Vec3 center = bb.getCenter();
		setBoundingBox(bb.move(-center.x, -bb.minY, -center.z)
			.move(x, y, z));
	}

	@Override
	public void move(MoverType typeIn, Vec3 pos) {
		if (!level.isClientSide && isAlive() && pos.lengthSqr() > 0.0D)
			discard();
	}

	@Override
	public void push(double x, double y, double z) {
		if (!level.isClientSide && isAlive() && x * x + y * y + z * z > 0.0D)
			discard();
	}

	@Override
	protected float getEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
		return 0.0F;
	}

	public void playPlaceSound() {
		AllSoundEvents.SLIME_ADDED.playFrom(this, 0.5F, 0.75F);
	}

	@Override
	public void push(Entity entityIn) {
		super.push(entityIn);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		return InteractionResult.PASS;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		Vec3 position = position();
		writeBoundingBox(compound, getBoundingBox().move(position.scale(-1)));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		Vec3 position = position();
		setBoundingBox(readBoundingBox(compound).move(position));
	}

	public static void writeBoundingBox(CompoundTag compound, AABB bb) {
		compound.put("From", VecHelper.writeNBT(new Vec3(bb.minX, bb.minY, bb.minZ)));
		compound.put("To", VecHelper.writeNBT(new Vec3(bb.maxX, bb.maxY, bb.maxZ)));
	}

	public static AABB readBoundingBox(CompoundTag compound) {
		Vec3 from = VecHelper.readNBT(compound.getList("From", Tag.TAG_DOUBLE));
		Vec3 to = VecHelper.readNBT(compound.getList("To", Tag.TAG_DOUBLE));
		return new AABB(from, to);
	}

	@Override
	protected boolean repositionEntityAfterLoad() {
		return false;
	}

	@Override
	public float rotate(Rotation transformRotation) {
		AABB bb = getBoundingBox().move(position().scale(-1));
		if (transformRotation == Rotation.CLOCKWISE_90 || transformRotation == Rotation.COUNTERCLOCKWISE_90)
			setBoundingBox(new AABB(bb.minZ, bb.minY, bb.minX, bb.maxZ, bb.maxY, bb.maxX).move(position()));
		return super.rotate(transformRotation);
	}

	@Override
	public float mirror(Mirror transformMirror) {
		return super.mirror(transformMirror);
	}

	@Override
	public void thunderHit(ServerLevel world, LightningBolt lightningBolt) {}

	@Override
	public void refreshDimensions() {}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<SuperGlueEntity> entityBuilder = (EntityType.Builder<SuperGlueEntity>) builder;
		return entityBuilder;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		CompoundTag compound = new CompoundTag();
		addAdditionalSaveData(compound);
		buffer.writeNbt(compound);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf additionalData) {
		readAdditionalSaveData(additionalData.readNbt());
	}

	@Override
	public ItemRequirement getRequiredItems() {
		return new ItemRequirement(ItemUseType.DAMAGE, AllItems.SUPER_GLUE.get());
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	public boolean contains(BlockPos pos) {
		return getBoundingBox().contains(Vec3.atCenterOf(pos));
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	@Override
	public PortalInfo findDimensionEntryPoint(ServerLevel pDestination) {
		portalEntrancePos = blockPosition();
		return super.findDimensionEntryPoint(pDestination);
	}

	public void spawnParticles() {
		AABB bb = getBoundingBox();
		Vec3 origin = new Vec3(bb.minX, bb.minY, bb.minZ);
		Vec3 extents = new Vec3(bb.getXsize(), bb.getYsize(), bb.getZsize());

		if (!(level instanceof ServerLevel slevel))
			return;

		for (Axis axis : Iterate.axes) {
			AxisDirection positive = AxisDirection.POSITIVE;
			double max = axis.choose(extents.x, extents.y, extents.z);
			Vec3 normal = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axis, positive)
				.getNormal());
			for (Axis axis2 : Iterate.axes) {
				if (axis2 == axis)
					continue;
				double max2 = axis2.choose(extents.x, extents.y, extents.z);
				Vec3 normal2 = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axis2, positive)
					.getNormal());
				for (Axis axis3 : Iterate.axes) {
					if (axis3 == axis2 || axis3 == axis)
						continue;
					double max3 = axis3.choose(extents.x, extents.y, extents.z);
					Vec3 normal3 = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axis3, positive)
						.getNormal());

					for (int i = 0; i <= max * 2; i++) {
						for (int o1 : Iterate.zeroAndOne) {
							for (int o2 : Iterate.zeroAndOne) {
								Vec3 v = origin.add(normal.scale(i / 2f))
									.add(normal2.scale(max2 * o1))
									.add(normal3.scale(max3 * o2));

								slevel.sendParticles(ParticleTypes.ITEM_SLIME, v.x, v.y, v.z, 1, 0, 0, 0, 0);

							}
						}
					}
					break;
				}
				break;
			}
		}
	}
}
