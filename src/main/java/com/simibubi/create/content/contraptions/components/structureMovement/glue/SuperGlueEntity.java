package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.jozufozu.flywheel.backend.instancing.IInstanceRendered;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.AbstractChassisBlock;
import com.simibubi.create.content.schematics.ISpecialEntityItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.BooleanProperty;
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
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

public class SuperGlueEntity extends Entity implements IEntityAdditionalSpawnData, ISpecialEntityItemRequirement, IInstanceRendered {

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
	protected void defineSynchedData() {}

	public int getWidthPixels() {
		return 12;
	}

	public int getHeightPixels() {
		return 12;
	}

	public void onBroken(@Nullable Entity breaker) {
		playSound(SoundEvents.SLIME_SQUISH_SMALL, 1.0F, 1.0F);
		if (onValidSurface()) {
			AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
				new GlueEffectPacket(getHangingPosition(), getFacingDirection().getOpposite(), false));
			AllSoundEvents.SLIME_ADDED.playFrom(this, 0.5F, 0.5F);
		}
	}

	public void playPlaceSound() {
		AllSoundEvents.SLIME_ADDED.playFrom(this, 0.5F, 0.75F);
	}

	protected void updateFacingWithBoundingBox() {
		Validate.notNull(getFacingDirection());
		if (getFacingDirection().getAxis()
			.isHorizontal()) {
			this.xRot = 0.0F;
			this.yRot = getFacingDirection().get2DDataValue() * 90;
		} else {
			this.xRot = -90 * getFacingDirection().getAxisDirection()
				.getStep();
			this.yRot = 0.0F;
		}

		this.xRotO = this.xRot;
		this.yRotO = this.yRot;
		this.updateBoundingBox();
	}

	protected void updateBoundingBox() {
		if (this.getFacingDirection() != null) {
			double offset = 0.5 - 1 / 256d;
			double x = hangingPosition.getX() + 0.5 - facingDirection.getStepX() * offset;
			double y = hangingPosition.getY() + 0.5 - facingDirection.getStepY() * offset;
			double z = hangingPosition.getZ() + 0.5 - facingDirection.getStepZ() * offset;
			this.setPosRaw(x, y, z);
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
		if (this.validationTimer++ == 10 && !this.level.isClientSide) {
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
		if (level instanceof WrappedWorld)
			return true;

		BlockPos pos = hangingPosition;
		BlockPos pos2 = pos.relative(getFacingDirection().getOpposite());
		return isValidFace(level, pos2, getFacingDirection()) != isValidFace(level, pos,
			getFacingDirection().getOpposite());
	}

	public boolean onValidSurface() {
		BlockPos pos = hangingPosition;
		BlockPos pos2 = hangingPosition.relative(getFacingDirection().getOpposite());
		if (pos2.getY() >= 256)
			return false;
		if (!level.isAreaLoaded(pos, 0) || !level.isAreaLoaded(pos2, 0))
			return true;
		if (!isValidFace(level, pos2, getFacingDirection())
				&& !isValidFace(level, pos, getFacingDirection().getOpposite()))
			return false;
		if (isSideSticky(level, pos2, getFacingDirection())
				|| isSideSticky(level, pos, getFacingDirection().getOpposite()))
			return false;
		return level.getEntities(this, getBoundingBox(), e -> e instanceof SuperGlueEntity)
				.isEmpty();
	}

	public static boolean isValidFace(World world, BlockPos pos, Direction direction) {
		BlockState state = world.getBlockState(pos);
		if (BlockMovementChecks.isBlockAttachedTowards(state, world, pos, direction))
			return true;
		if (!BlockMovementChecks.isMovementNecessary(state, world, pos))
			return false;
		if (BlockMovementChecks.isNotSupportive(state, direction))
			return false;
		return true;
	}

	public static boolean isSideSticky(World world, BlockPos pos, Direction direction) {
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
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean skipAttackInteraction(Entity entity) {
		return entity instanceof PlayerEntity
			? hurt(DamageSource.playerAttack((PlayerEntity) entity), 0)
			: false;
	}

	@Override
	public Direction getDirection() {
		return this.getFacingDirection();
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source))
			return false;

		boolean mobGriefing = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
		Entity trueSource = source.getEntity();
		if (!mobGriefing && trueSource instanceof MobEntity)
			return false;

		Entity immediateSource = source.getDirectEntity();
		if (!isVisible() && immediateSource instanceof PlayerEntity) {
			if (!AllItems.SUPER_GLUE.isIn(((PlayerEntity) immediateSource).getMainHandItem()))
				return true;
		}

		if (isAlive() && !level.isClientSide) {
			remove();
			markHurt();
			onBroken(source.getEntity());
		}

		return true;
	}

	@Override
	public void move(MoverType typeIn, Vector3d pos) {
		if (!level.isClientSide && isAlive() && pos.lengthSqr() > 0.0D) {
			remove();
			onBroken(null);
		}
	}

	@Override
	public void push(double x, double y, double z) {
		if (!level.isClientSide && isAlive() && x * x + y * y + z * z > 0.0D) {
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
	public void push(Entity entityIn) {
		super.push(entityIn);
	}

	@Override
	public ActionResultType interact(PlayerEntity player, Hand hand) {
		if (player instanceof FakePlayer)
			return ActionResultType.PASS;
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			triggerPlaceBlock(player, hand);
		});
		return ActionResultType.CONSUME;
	}

	@OnlyIn(Dist.CLIENT)
	private void triggerPlaceBlock(PlayerEntity player, Hand hand) {
		if (!(player instanceof ClientPlayerEntity))
			return;
		if (!(player.level instanceof ClientWorld))
			return;

		ClientPlayerEntity cPlayer = (ClientPlayerEntity) player;
		Minecraft mc = Minecraft.getInstance();
		RayTraceResult ray =
			cPlayer.pick(mc.gameMode.getPickRange(), AnimationTickHolder.getPartialTicks(), false);

		if (!(ray instanceof BlockRayTraceResult))
			return;
		if (ray.getType() == Type.MISS)
			return;
		BlockRayTraceResult blockRay = (BlockRayTraceResult) ray;
		BlockFace rayFace = new BlockFace(blockRay.getBlockPos(), blockRay.getDirection());
		BlockFace hangingFace = new BlockFace(getHangingPosition(), getFacingDirection().getOpposite());
		if (!rayFace.isEquivalent(hangingFace))
			return;

		for (Hand handIn : Hand.values()) {
			ItemStack itemstack = cPlayer.getItemInHand(handIn);
			int countBefore = itemstack.getCount();
			ActionResultType actionResultType =
				mc.gameMode.useItemOn(cPlayer, (ClientWorld) cPlayer.level, handIn, blockRay);
			if (actionResultType != ActionResultType.SUCCESS)
				return;

			cPlayer.swing(handIn);
			if (!itemstack.isEmpty() && (itemstack.getCount() != countBefore || mc.gameMode.hasInfiniteItems()))
				mc.gameRenderer.itemInHandRenderer.itemUsed(handIn);
			return;
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundNBT compound) {
		compound.putByte("Facing", (byte) this.getFacingDirection()
			.get3DDataValue());
		BlockPos blockpos = this.getHangingPosition();
		compound.putInt("TileX", blockpos.getX());
		compound.putInt("TileY", blockpos.getY());
		compound.putInt("TileZ", blockpos.getZ());
	}

	@Override
	public void readAdditionalSaveData(CompoundNBT compound) {
		this.hangingPosition =
			new BlockPos(compound.getInt("TileX"), compound.getInt("TileY"), compound.getInt("TileZ"));
		this.facingDirection = Direction.from3DDataValue(compound.getByte("Facing"));
		updateFacingWithBoundingBox();
	}

	@Override
	public ItemEntity spawnAtLocation(ItemStack stack, float yOffset) {
		float xOffset = (float) this.getFacingDirection()
			.getStepX() * 0.15F;
		float zOffset = (float) this.getFacingDirection()
			.getStepZ() * 0.15F;
		ItemEntity itementity =
			new ItemEntity(this.level, this.getX() + xOffset, this.getY() + yOffset, this.getZ() + zOffset, stack);
		itementity.setDefaultPickUpDelay();
		this.level.addFreshEntity(itementity);
		return itementity;
	}

	@Override
	protected boolean repositionEntityAfterLoad() {
		return false;
	}

	@Override
	public void setPos(double x, double y, double z) {
		hangingPosition = new BlockPos(x, y, z);
		updateBoundingBox();
		hasImpulse = true;
	}

	@Override
	public float rotate(Rotation transformRotation) {
		if (this.getFacingDirection()
			.getAxis() != Direction.Axis.Y) {
			switch (transformRotation) {
			case CLOCKWISE_180:
				facingDirection = facingDirection.getOpposite();
				break;
			case COUNTERCLOCKWISE_90:
				facingDirection = facingDirection.getCounterClockWise();
				break;
			case CLOCKWISE_90:
				facingDirection = facingDirection.getClockWise();
			default:
				break;
			}
		}

		float f = MathHelper.wrapDegrees(this.yRot);
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
	public float mirror(Mirror transformMirror) {
		return this.rotate(transformMirror.getRotation(this.getFacingDirection()));
	}

	public Direction getAttachedDirection(BlockPos pos) {
		return !pos.equals(hangingPosition) ? getFacingDirection() : getFacingDirection().getOpposite();
	}

	@Override
	public void thunderHit(ServerWorld world, LightningBoltEntity lightningBolt) {}

	@Override
	public void refreshDimensions() {}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<SuperGlueEntity> entityBuilder = (EntityType.Builder<SuperGlueEntity>) builder;
		return entityBuilder;
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		CompoundNBT compound = new CompoundNBT();
		addAdditionalSaveData(compound);
		buffer.writeNbt(compound);
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		readAdditionalSaveData(additionalData.readNbt());
	}

	public Direction getFacingDirection() {
		return facingDirection;
	}

	@Override
	public ItemRequirement getRequiredItems() {
		return new ItemRequirement(ItemUseType.DAMAGE, AllItems.SUPER_GLUE.get());
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	public World getWorld() {
		return level;
	}
}
