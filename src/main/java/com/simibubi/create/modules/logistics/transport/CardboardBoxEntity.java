package com.simibubi.create.modules.logistics.transport;

import java.util.Collections;

import javax.annotation.Nullable;

import com.simibubi.create.AllEntities;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.actors.DrillTileEntity;
import com.simibubi.create.modules.logistics.item.CardboardBoxItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages.SpawnEntity;
import net.minecraftforge.fml.network.NetworkHooks;

public class CardboardBoxEntity extends LivingEntity implements IEntityAdditionalSpawnData {

	public ItemStack box;

	public int extractorAnimationProgress;
	public Direction extractorSide;

	@SuppressWarnings("unchecked")
	public CardboardBoxEntity(EntityType<? extends Entity> entityTypeIn, World worldIn) {
		super((EntityType<? extends LivingEntity>) entityTypeIn, worldIn);
	}

	protected CardboardBoxEntity(World worldIn, double x, double y, double z) {
		this(AllEntities.CARDBOARD_BOX.type, worldIn);
		this.setPosition(x, y, z);
		this.recalculateSize();
		this.rotationYaw = this.rand.nextFloat() * 360.0F;
	}

	public CardboardBoxEntity(World worldIn, Vec3d pos, ItemStack stack, Direction extractionDirection) {
		this(worldIn, pos.x, pos.y, pos.z);
		this.setBox(stack);
		this.extractedFrom(extractionDirection);
	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<CardboardBoxEntity> boxBuilder = (EntityType.Builder<CardboardBoxEntity>) builder;
		return boxBuilder.setCustomClientFactory(CardboardBoxEntity::spawn).size(1, 1);
	}

	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(5.0D);
	}

	private void extractedFrom(Direction side) {
		extractorSide = side;
		extractorAnimationProgress = 20;
	}

	public String getAddress() {
		return box.getTag().getString("Address");
	}
	
	@Override
	public void tick() {
		if (extractorAnimationProgress == 0) {
			setMotion(new Vec3d(extractorSide.getDirectionVec()).scale(1 / 16f).add(0, 1 / 32f, 0));
		}
		if (extractorAnimationProgress > -1) {
			extractorAnimationProgress--;
			return;
		}
		super.tick();
	}

	@Override
	public EntitySize getSize(Pose poseIn) {
		if (box == null)
			return super.getSize(poseIn);
		if (AllItems.CARDBOARD_BOX_1410.typeOf(box))
			return new EntitySize(14 / 16f, 10 / 16f, true);
		if (AllItems.CARDBOARD_BOX_1416.typeOf(box))
			return new EntitySize(14 / 16f, 1f, true);
		if (AllItems.CARDBOARD_BOX_1612.typeOf(box))
			return new EntitySize(1f, 12 / 16f, true);
		if (AllItems.CARDBOARD_BOX_1616.typeOf(box))
			return new EntitySize(1f, 1f, true);

		return super.getSize(poseIn);
	}

	public static CardboardBoxEntity spawn(SpawnEntity spawnEntity, World world) {
		return new CardboardBoxEntity(world, 0, 0, 0);
	}

	public ItemStack getBox() {
		return box;
	}

	public void setBox(ItemStack box) {
		this.box = box.copy();
		recalculateSize();
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox() {
		return getBoundingBox(getPose()).grow(-.1f, 0, -.1f);
	}

	@Override
	public boolean canBePushed() {
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entityIn) {
		if (entityIn instanceof CardboardBoxEntity)
			return getBoundingBox();
		if (entityIn instanceof MinecartEntity)
			return null;
		return super.getCollisionBox(entityIn);
	}

	@Override
	public boolean canBeCollidedWith() {
		return isAlive();
	}

	@Override
	public void applyEntityCollision(Entity entityIn) {
		if (entityIn instanceof CardboardBoxEntity) {
			if (entityIn.getBoundingBox().minY < this.getBoundingBox().maxY) {
				super.applyEntityCollision(entityIn);
			}
		} else if (entityIn.getBoundingBox().minY <= this.getBoundingBox().minY) {
			super.applyEntityCollision(entityIn);
		}
	}

	@Override
	public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d vec, Hand hand) {
		return super.applyPlayerInteraction(player, vec, hand);
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		if (player.getPassengers().isEmpty()) {
			startRiding(player);
			return true;
		} else {
			for (Entity e : player.getPassengers()) {
				while (e instanceof CardboardBoxEntity) {
					if (e == this)
						return false;
					if (e.getPassengers().isEmpty()) {
						startRiding(e);
						return false;
					}
					e = e.getPassengers().get(0);
				}
			}
		}

		return super.processInitialInteract(player, hand);
	}

	@Override
	public void updateRidden() {
		super.updateRidden();
		Entity ridingEntity = getRidingEntity();
		if (ridingEntity instanceof LivingEntity) {

			if (!(ridingEntity instanceof CardboardBoxEntity)) {
				Vec3d front = VecHelper.rotate(new Vec3d(1, 0, 0), -90 - ridingEntity.getRotationYawHead(), Axis.Y);
				double x = ridingEntity.posX + front.x;
				double y = ridingEntity.posY + ridingEntity.getMountedYOffset() / 2 + this.getYOffset();
				double z = ridingEntity.posZ + front.z;

				prevRotationYaw = rotationYaw;
				setRotation(-ridingEntity.rotationYaw, 0);

				if (world.isRemote)
					setPosition(x, y, z);
				setPositionAndUpdate(x, y, z);

				if (ridingEntity.isSneaking()) {
					stopRiding();
					return;
				}

			} else {
				prevRotationYaw = rotationYaw;
				setRotation(rotationYaw + ridingEntity.rotationYaw - ridingEntity.prevRotationYaw, 0);
			}
		}
	}

	@Override
	public double getMountedYOffset() {
		return this.getSize(getPose()).height;
	}

	@Override
	public void dismountEntity(Entity ridingEntity) {
		boolean ridingBox = ridingEntity instanceof CardboardBoxEntity;

		if (ridingBox) {
			super.dismountEntity(ridingEntity);
		}

		if (ridingEntity instanceof LivingEntity && !ridingBox) {
			Vec3d front = VecHelper.rotate(new Vec3d(1, 0, 0), -90 - ridingEntity.rotationYaw, Axis.Y);
			double x = ridingEntity.posX + front.x;
			double y = ridingEntity.posY + ridingEntity.getMountedYOffset() / 2 + this.getYOffset();
			double z = ridingEntity.posZ + front.z;
			setRotation(-ridingEntity.rotationYaw, 0);
			if (world.isRemote)
				setPosition(x, y, z);
			setPositionAndUpdate(x, y, z);
		}

		getPassengers().forEach(x -> x.stopRiding());

	}

	@Override
	protected void onInsideBlock(BlockState state) {
		if (state.getBlock() == Blocks.WATER) {
			destroy(DamageSource.DROWN);
			remove();
		}
		super.onInsideBlock(state);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (world.isRemote || !this.isAlive())
			return false;

		if (DamageSource.OUT_OF_WORLD.equals(source)) {
			this.remove();
			return false;
		}

		if (DamageSource.IN_WALL.equals(source) && isPassenger())
			return false;

		if (DamageSource.FALL.equals(source))
			return false;

		if (this.isInvulnerableTo(source))
			return false;

		if (source.isExplosion()) {
			this.destroy(source);
			this.remove();
			return false;
		}

		if (DamageSource.IN_FIRE.equals(source)) {
			if (this.isBurning()) {
				this.takeDamage(source, 0.15F);
			} else {
				this.setFire(5);
			}
			return false;
		}

		if (DamageSource.ON_FIRE.equals(source) && this.getHealth() > 0.5F) {
			this.takeDamage(source, 4.0F);
			return false;
		}

		boolean wasShot = source.getImmediateSource() instanceof AbstractArrowEntity;
		boolean shotCanPierce = wasShot && ((AbstractArrowEntity) source.getImmediateSource()).getPierceLevel() > 0;

		if (source.getTrueSource() instanceof PlayerEntity
				&& !((PlayerEntity) source.getTrueSource()).abilities.allowEdit)
			return false;

		this.destroy(source);
		this.remove();
		return shotCanPierce;
	}

	private void takeDamage(DamageSource source, float amount) {
		float hp = this.getHealth();
		hp = hp - amount;
		if (hp <= 0.5F) {
			this.destroy(source);
			this.remove();
		} else {
			this.setHealth(hp);
		}
	}

	private void destroy(DamageSource source) {
		this.world.playSound((PlayerEntity) null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARMOR_STAND_BREAK,
				this.getSoundCategory(), 1.0F, 1.0F);
		this.spawnDrops(source);
	}

	@Override
	protected void spawnDrops(DamageSource source) {
		super.spawnDrops(source);
		for (ItemStack stack : CardboardBoxItem.getContents(box)) {
			ItemEntity entityIn = new ItemEntity(world, posX, posY, posZ, stack);
			world.addEntity(entityIn);
			if (DrillTileEntity.damageSourceDrill.equals(source))
				entityIn.setMotion(Vec3d.ZERO);
		}
	}

	@Override
	public void remove(boolean keepData) {
		if (world.isRemote) {
			for (int i = 0; i < 20; i++) {
				Vec3d pos = VecHelper.offsetRandomly(this.getPositionVector(), world.rand, .5f);
				Vec3d motion = Vec3d.ZERO;
				world.addParticle(new ItemParticleData(ParticleTypes.ITEM, box), pos.x, pos.y, pos.z, motion.x,
						motion.y, motion.z);
			}
		}
		super.remove(keepData);
	}

	@Override
	protected void registerData() {
		super.registerData();
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		box = ItemStack.read(compound.getCompound("Box"));
		if (compound.contains("Direction"))
			extractedFrom(Direction.byIndex(compound.getInt("Direction")));
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.put("Box", box.serializeNBT());
		if (extractorSide != null)
			compound.putInt("Direction", extractorSide.getIndex());
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public Iterable<ItemStack> getArmorInventoryList() {
		return Collections.emptyList();
	}

	@Override
	public ItemStack getItemStackFromSlot(EquipmentSlotType slotIn) {
		if (slotIn == EquipmentSlotType.MAINHAND)
			return getBox();
		return ItemStack.EMPTY;
	}

	@Override
	public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack) {
		if (slotIn == EquipmentSlotType.MAINHAND)
			setBox(stack);
	}

	@Override
	public HandSide getPrimaryHand() {
		return HandSide.LEFT;
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		buffer.writeItemStack(getBox());
		boolean sidePresent = extractorSide != null;
		buffer.writeBoolean(sidePresent);
		if (sidePresent)
			buffer.writeInt(extractorSide.getIndex());
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		setBox(additionalData.readItemStack());
		if (additionalData.readBoolean())
			extractedFrom(Direction.byIndex(additionalData.readInt()));
	}

	protected SoundEvent getFallSound(int heightIn) {
		return SoundEvents.ENTITY_ARMOR_STAND_FALL;
	}

	@Nullable
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_ARMOR_STAND_HIT;
	}

	@Nullable
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_ARMOR_STAND_BREAK;
	}

}
