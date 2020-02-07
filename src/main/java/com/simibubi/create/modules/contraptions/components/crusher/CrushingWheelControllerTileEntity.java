package com.simibubi.create.modules.contraptions.components.crusher;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.block.SyncedTileEntity;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.processing.ProcessingInventory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CrushingWheelControllerTileEntity extends SyncedTileEntity implements ITickableTileEntity {

	public Entity processingEntity;
	private UUID entityUUID;
	protected boolean searchForEntity;

	public ProcessingInventory inventory;
	public float crushingspeed;

	public CrushingWheelControllerTileEntity() {
		super(AllTileEntities.CRUSHING_WHEEL_CONTROLLER.type);
		inventory = new ProcessingInventory();
	}

	@Override
	public void tick() {
		if (isFrozen())
			return;
		if (searchForEntity) {
			searchForEntity = false;
			List<Entity> search = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(getPos()),
					e -> entityUUID.equals(e.getUniqueID()));
			if (search.isEmpty())
				clear();
			else
				processingEntity = search.get(0);
		}

		if (!isOccupied())
			return;
		if (crushingspeed == 0)
			return;

		float speed = crushingspeed * 4;
		Vec3d outPos = VecHelper.getCenterOf(pos);

		if (!hasEntity()) {

			float processingSpeed = MathHelper.clamp(
					(speed) / (!inventory.appliedRecipe ? MathHelper.log2(inventory.getStackInSlot(0).getCount()) : 1),
					.25f, 20);
			inventory.remainingTime -= processingSpeed;
			spawnParticles(inventory.getStackInSlot(0));

			if (world.isRemote)
				return;

			if (inventory.remainingTime < 20 && !inventory.appliedRecipe) {
				applyRecipe();
				inventory.appliedRecipe = true;
				world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2 | 16);
				return;
			}

			if (inventory.remainingTime <= 0) {
				for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
					ItemStack stack = inventory.getStackInSlot(slot);
					if (stack.isEmpty())
						continue;
					ItemEntity entityIn = new ItemEntity(world, outPos.x, outPos.y, outPos.z, stack);
					entityIn.setMotion(Vec3d.ZERO);
					entityIn.getPersistentData().put("BypassCrushingWheel", NBTUtil.writeBlockPos(pos));
					world.addEntity(entityIn);
				}
				inventory.clear();
				world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2 | 16);
				return;
			}

			return;
		}

		if (!processingEntity.isAlive()
				|| !processingEntity.getBoundingBox().intersects(new AxisAlignedBB(pos).grow(.5f))) {
			clear();
			return;
		}

		double xMotion = ((pos.getX() + .5f) - processingEntity.posX) / 2f;
		double zMotion = ((pos.getZ() + .5f) - processingEntity.posZ) / 2f;
		if (processingEntity.isSneaking())
			xMotion = zMotion = 0;

		processingEntity.setMotion(new Vec3d(xMotion, Math.max(-speed / 4f, -.5f), zMotion));

		if (world.isRemote)
			return;

		if (!(processingEntity instanceof ItemEntity)) {
			processingEntity.attackEntityFrom(CrushingWheelTileEntity.damageSource,
					AllConfigs.SERVER.kinetics.crushingDamage.get());
			if (!processingEntity.isAlive()) {
				processingEntity.setPosition(outPos.x, outPos.y - .75f, outPos.z);
			}
			return;
		}

		ItemEntity itemEntity = (ItemEntity) processingEntity;
		itemEntity.setPickupDelay(20);
		if (processingEntity.posY < pos.getY() + .25f) {
			insertItem(itemEntity);
			itemEntity.remove();
			world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2 | 16);
		}

	}

	protected void spawnParticles(ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return;

		IParticleData particleData = null;
		if (stack.getItem() instanceof BlockItem)
			particleData = new BlockParticleData(ParticleTypes.BLOCK,
					((BlockItem) stack.getItem()).getBlock().getDefaultState());
		else
			particleData = new ItemParticleData(ParticleTypes.ITEM, stack);

		Random r = world.rand;
		for (int i = 0; i < 4; i++)
			world.addParticle(particleData, pos.getX() + r.nextFloat(), pos.getY() + r.nextFloat(),
					pos.getZ() + r.nextFloat(), 0, 0, 0);
	}

	private void applyRecipe() {
		Optional<CrushingRecipe> recipe = world.getRecipeManager().getRecipe(AllRecipes.CRUSHING.getType(), inventory,
				world);

		if (recipe.isPresent()) {
			int rolls = inventory.getStackInSlot(0).getCount();
			inventory.clear();

			for (int roll = 0; roll < rolls; roll++) {
				List<ItemStack> rolledResults = recipe.get().rollResults();

				for (int i = 0; i < rolledResults.size(); i++) {
					ItemStack stack = rolledResults.get(i);

					for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
						stack = inventory.getItems().insertItem(slot, stack, false);

						if (stack.isEmpty())
							break;
					}
				}
			}

		} else {
			inventory.clear();
		}

	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		if (hasEntity() && !isFrozen())
			compound.put("Entity", NBTUtil.writeUniqueId(entityUUID));
		inventory.write(compound);
		compound.putFloat("Speed", crushingspeed);

		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);

		if (compound.contains("Entity") && !isFrozen() && !isOccupied()) {
			entityUUID = NBTUtil.readUniqueId(compound.getCompound("Entity"));
			this.searchForEntity = true;
		}
		crushingspeed = compound.getFloat("Speed");
		inventory = ProcessingInventory.read(compound);

	}

	public void startCrushing(Entity entity) {
		processingEntity = entity;
		entityUUID = entity.getUniqueID();
	}

	private void insertItem(ItemEntity entity) {
		inventory.clear();
		inventory.setInventorySlotContents(0, entity.getItem());
		Optional<CrushingRecipe> recipe = world.getRecipeManager().getRecipe(AllRecipes.CRUSHING.getType(), inventory,
				world);

		inventory.remainingTime = recipe.isPresent() ? recipe.get().getProcessingDuration() : 100;
		inventory.appliedRecipe = false;
	}

	public void clear() {
		processingEntity = null;
		entityUUID = null;
	}

	public boolean isOccupied() {
		return hasEntity() || !inventory.isEmpty();
	}

	public boolean hasEntity() {
		return processingEntity != null;
	}

	public static boolean isFrozen() {
		return AllConfigs.SERVER.control.freezeCrushing.get();
	}

}
