package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementTraits;
import com.simibubi.create.content.schematics.ISpecialEntityItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

public class SuperGlueEntity extends Entity implements IEntityAdditionalSpawnData, ISpecialEntityItemRequirement {

	private int validationTimer;
	protected BlockPos hangingPosition;
	protected Direction facingDirection = Direction.SOUTH;

	public SuperGlueEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	public SuperGlueEntity(World world, BlockPos pos, Direction direction) {
		this(AllEntityTypes.SUPER_GLUE.get(), world);
		hangingPosition = pos;
		facingDirection = direction;
		updateFacingWithBoundingBox();
	}

	@Override
	protected void registerData() {}

	public int getWidthPixels() {
		return 12;
	}

	public int getHeightPixels() {
		return 12;
	}

	public void onBroken(@Nullable Entity breaker) {
		playSound(SoundEvents.ENTITY_SLIME_SQUISH_SMALL, 1.0F, 1.0F);
		if (onValidSurface()) {
			AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
				new GlueEffectPacket(getHangingPosition(), getFacingDirection().getOpposite(), false));
			playSound(AllSoundEvents.SLIME_ADDED.get(), 0.5F, 0.5F);
		}
	}

	public void playPlaceSound() {
		playSound(AllSoundEvents.SLIME_ADDED.get(), 0.5F, 0.75F);
	}

	protected void updateFacingWithBoundingBox() {
		Validate.notNull(getFacingDirection());
		if (getFacingDirection().getAxis()
			.isHorizontal()) {
			this.rotationPitch = 0.0F;
			this.rotationYaw = getFacingDirection().getHorizontalIndex() * 90;
		} else {
			this.rotationPitch = -90 * getFacingDirection().getAxisDirection()
				.getOffset();
			this.rotationYaw = 0.0F;
		}

		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;
		this.updateBoundingBox();
	}

	protected void updateBoundingBox() {
		if (this.getFacingDirection() != null) {
			double offset = 0.5 - 1 / 256d;
			double x = hangingPosition.getX() + 0.5 - facingDirection.getXOffset() * offset;
			double y = hangingPosition.getY() + 0.5 - facingDirection.getYOffset() * offset;
			double z = hangingPosition.getZ() + 0.5 - facingDirection.getZOffset() * offset;
			this.setPos(x, y, z);
			double w = getWidthPixels();
			double h = getHeightPixels();
			double l = getWidthPixels();
			Axis axis = this.getFacingDirection()
				.getAxis();
			double depth = 2 - 1 / 128f;

			switch (axis) {
			case X:
				w = depth;
				break;
			case Y:
				h = depth;
				break;
			case Z:
				l = depth;
			}

			w = w / 32.0D;
			h = h / 32.0D;
			l = l / 32.0D;
			this.setBoundingBox(new AxisAlignedBB(x - w, y - h, z - l, x + w, y + h, z + l));
		}
	}

	@Override
	public void tick() {
		if (this.validationTimer++ == 10 && !this.world.isRemote) {
			this.validationTimer = 0;
			if (isAlive() && !this.onValidSurface()) {
				remove();
				onBroken(null);
			}
		}

	}

	public boolean isVisible() {
		if (!isAlive())
			return false;
		BlockPos pos = hangingPosition;
		BlockPos pos2 = pos.offset(getFacingDirection().getOpposite());
		return isValidFace(world, pos2, getFacingDirection()) != isValidFace(world, pos,
			getFacingDirection().getOpposite());
	}

	public boolean onValidSurface() {
		BlockPos pos = hangingPosition;
		BlockPos pos2 = hangingPosition.offset(getFacingDirection().getOpposite());
		if (!world.isAreaLoaded(pos, 0) || !world.isAreaLoaded(pos2, 0))
			return true;
		if (!isValidFace(world, pos2, getFacingDirection())
			&& !isValidFace(world, pos, getFacingDirection().getOpposite()))
			return false;
		return world.getEntitiesInAABBexcluding(this, getBoundingBox(), e -> e instanceof SuperGlueEntity)
			.isEmpty();
	}

	public static boolean isValidFace(World world, BlockPos pos, Direction direction) {
		BlockState state = world.getBlockState(pos);
		if (BlockMovementTraits.isBlockAttachedTowards(state, direction))
			return true;
		if (!BlockMovementTraits.movementNecessary(world, pos))
			return false;
		if (BlockMovementTraits.notSupportive(state, direction))
			return false;
		return true;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean hitByEntity(Entity entity) {
		return entity instanceof PlayerEntity
			? attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) entity), 0)
			: false;
	}

	@Override
	public Direction getHorizontalFacing() {
		return this.getFacingDirection();
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source))
			return false;
		if (isAlive() && !world.isRemote && isVisible()) {
			remove();
			markVelocityChanged();
			onBroken(source.getTrueSource());
		}

		return true;
	}

	@Override
	public void move(MoverType typeIn, Vec3d pos) {
		if (!world.isRemote && isAlive() && pos.lengthSquared() > 0.0D) {
			remove();
			onBroken(null);
		}
	}

	@Override
	public void addVelocity(double x, double y, double z) {
		if (!world.isRemote && isAlive() && x * x + y * y + z * z > 0.0D) {
			remove();
			onBroken(null);
		}
	}

	@Override
	protected float getEyeHeight(Pose poseIn, EntitySize sizeIn) {
		return 0.0F;
	}

	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		return AllItems.SUPER_GLUE.asStack();
	}

	@Override
	public void applyEntityCollision(Entity entityIn) {
		super.applyEntityCollision(entityIn);
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			triggerPlaceBlock(player, hand);
		});
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	private void triggerPlaceBlock(PlayerEntity player, Hand hand) {
		if (player instanceof ClientPlayerEntity && player.world instanceof ClientWorld) {
			ClientPlayerEntity cPlayer = (ClientPlayerEntity) player;
			Minecraft mc = Minecraft.getInstance();
			RayTraceResult ray =
				cPlayer.pick(mc.playerController.getBlockReachDistance(), mc.getRenderPartialTicks(), false);
			if (ray instanceof BlockRayTraceResult) {
				for (Hand handIn : Hand.values()) {
					ItemStack itemstack = cPlayer.getHeldItem(handIn);
					int countBefore = itemstack.getCount();
					ActionResultType actionResultType = mc.playerController.func_217292_a(cPlayer,
						(ClientWorld) cPlayer.world, handIn, (BlockRayTraceResult) ray);
					if (actionResultType == ActionResultType.SUCCESS) {
						cPlayer.swingArm(handIn);
						if (!itemstack.isEmpty()
							&& (itemstack.getCount() != countBefore || mc.playerController.isInCreativeMode()))
							mc.gameRenderer.itemRenderer.resetEquippedProgress(handIn);
						return;
					}
				}
			}
		}
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		compound.putByte("Facing", (byte) this.getFacingDirection()
			.getIndex());
		BlockPos blockpos = this.getHangingPosition();
		compound.putInt("TileX", blockpos.getX());
		compound.putInt("TileY", blockpos.getY());
		compound.putInt("TileZ", blockpos.getZ());
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		this.hangingPosition =
			new BlockPos(compound.getInt("TileX"), compound.getInt("TileY"), compound.getInt("TileZ"));
		this.facingDirection = Direction.byIndex(compound.getByte("Facing"));
		updateFacingWithBoundingBox();
	}

	@Override
	public ItemEntity entityDropItem(ItemStack stack, float yOffset) {
		float xOffset = (float) this.getFacingDirection()
			.getXOffset() * 0.15F;
		float zOffset = (float) this.getFacingDirection()
			.getZOffset() * 0.15F;
		ItemEntity itementity =
			new ItemEntity(this.world, this.getX() + xOffset, this.getY() + yOffset, this.getZ() + zOffset, stack);
		itementity.setDefaultPickupDelay();
		this.world.addEntity(itementity);
		return itementity;
	}

	@Override
	protected boolean shouldSetPosAfterLoading() {
		return false;
	}

	@Override
	public void setPosition(double x, double y, double z) {
		hangingPosition = new BlockPos(x, y, z);
		updateBoundingBox();
		isAirBorne = true;
	}

	@Override
	public float getRotatedYaw(Rotation transformRotation) {
		if (this.getFacingDirection()
			.getAxis() != Direction.Axis.Y) {
			switch (transformRotation) {
			case CLOCKWISE_180:
				facingDirection = facingDirection.getOpposite();
				break;
			case COUNTERCLOCKWISE_90:
				facingDirection = facingDirection.rotateYCCW();
				break;
			case CLOCKWISE_90:
				facingDirection = facingDirection.rotateY();
			default:
				break;
			}
		}

		float f = MathHelper.wrapDegrees(this.rotationYaw);
		switch (transformRotation) {
		case CLOCKWISE_180:
			return f + 180.0F;
		case COUNTERCLOCKWISE_90:
			return f + 90.0F;
		case CLOCKWISE_90:
			return f + 270.0F;
		default:
			return f;
		}
	}

	public BlockPos getHangingPosition() {
		return this.hangingPosition;
	}

	@Override
	public float getMirroredYaw(Mirror transformMirror) {
		return this.getRotatedYaw(transformMirror.toRotation(this.getFacingDirection()));
	}

	public Direction getAttachedDirection(BlockPos pos) {
		return !pos.equals(hangingPosition) ? getFacingDirection() : getFacingDirection().getOpposite();
	}

	@Override
	public void onStruckByLightning(LightningBoltEntity lightningBolt) {}

	@Override
	public void recalculateSize() {}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<SuperGlueEntity> entityBuilder = (EntityType.Builder<SuperGlueEntity>) builder;
		return entityBuilder;
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

	public Direction getFacingDirection() {
		return facingDirection;
	}

	@Override
	public ItemRequirement getRequiredItems() {
		return new ItemRequirement(ItemUseType.DAMAGE, AllItems.SUPER_GLUE.get());
	}

	@Override
	public boolean doesEntityNotTriggerPressurePlate() {
		return true;
	}
}
