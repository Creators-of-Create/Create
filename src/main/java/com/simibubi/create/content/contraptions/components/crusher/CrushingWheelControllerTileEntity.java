package com.simibubi.create.content.contraptions.components.crusher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.ProcessingInventory;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;

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
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class CrushingWheelControllerTileEntity extends SmartTileEntity {

	public Entity processingEntity;
	private UUID entityUUID;
	protected boolean searchForEntity;

	public ProcessingInventory inventory;
	protected LazyOptional<IItemHandlerModifiable> handler = LazyOptional.of(() -> inventory);
	private RecipeWrapper wrapper;
	public float crushingspeed;

	public CrushingWheelControllerTileEntity(TileEntityType<? extends CrushingWheelControllerTileEntity> type) {
		super(type);
		inventory = new ProcessingInventory(this::itemInserted) {

			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return super.isItemValid(slot, stack) && processingEntity == null;
			}

		};
		wrapper = new RecipeWrapper(inventory);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
	}

	@Override
	public void tick() {
		super.tick();
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

			float processingSpeed =
				MathHelper.clamp((speed) / (!inventory.appliedRecipe ? MathHelper.log2(inventory.getStackInSlot(0)
					.getCount()) : 1), .25f, 20);
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
				for (int slot = 0; slot < inventory.getSlots(); slot++) {
					ItemStack stack = inventory.getStackInSlot(slot);
					if (stack.isEmpty())
						continue;
					ItemEntity entityIn = new ItemEntity(world, outPos.x, outPos.y, outPos.z, stack);
					entityIn.setMotion(Vec3d.ZERO);
					entityIn.getPersistentData()
						.put("BypassCrushingWheel", NBTUtil.writeBlockPos(pos));
					world.addEntity(entityIn);
				}
				inventory.clear();
				world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2 | 16);
				return;
			}

			return;
		}

		if (!processingEntity.isAlive() || !processingEntity.getBoundingBox()
			.intersects(new AxisAlignedBB(pos).grow(.5f))) {
			clear();
			return;
		}

		double xMotion = ((pos.getX() + .5f) - processingEntity.getX()) / 2f;
		double zMotion = ((pos.getZ() + .5f) - processingEntity.getZ()) / 2f;
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
		if (processingEntity.getY() < pos.getY() + .25f) {
			inventory.clear();
			inventory.setStackInSlot(0, itemEntity.getItem()
				.copy());
			itemInserted(inventory.getStackInSlot(0));
			itemEntity.remove();
			world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2 | 16);
		}

	}

	protected void spawnParticles(ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return;

		IParticleData particleData = null;
		if (stack.getItem() instanceof BlockItem)
			particleData = new BlockParticleData(ParticleTypes.BLOCK, ((BlockItem) stack.getItem()).getBlock()
				.getDefaultState());
		else
			particleData = new ItemParticleData(ParticleTypes.ITEM, stack);

		Random r = world.rand;
		for (int i = 0; i < 4; i++)
			world.addParticle(particleData, pos.getX() + r.nextFloat(), pos.getY() + r.nextFloat(),
				pos.getZ() + r.nextFloat(), 0, 0, 0);
	}

	private void applyRecipe() {
		Optional<ProcessingRecipe<RecipeWrapper>> recipe = findRecipe();

		List<ItemStack> list = new ArrayList<>();
		if (recipe.isPresent()) {
			int rolls = inventory.getStackInSlot(0)
				.getCount();
			inventory.clear();
			for (int roll = 0; roll < rolls; roll++) {
				List<ItemStack> rolledResults = recipe.get()
					.rollResults();
				for (int i = 0; i < rolledResults.size(); i++) {
					ItemStack stack = rolledResults.get(i);
					ItemHelper.addToList(stack, list);
				}
			}
			for (int slot = 0; slot < list.size() && slot + 1 < inventory.getSlots(); slot++)
				inventory.setStackInSlot(slot + 1, list.get(slot));
		} else {
			inventory.clear();
		}

	}

	public Optional<ProcessingRecipe<RecipeWrapper>> findRecipe() {
		Optional<ProcessingRecipe<RecipeWrapper>> crushingRecipe = world.getRecipeManager()
			.getRecipe(AllRecipeTypes.CRUSHING.getType(), wrapper, world);
		if (!crushingRecipe.isPresent())
			crushingRecipe = world.getRecipeManager()
				.getRecipe(AllRecipeTypes.MILLING.getType(), wrapper, world);
		return crushingRecipe;
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		if (hasEntity())
			compound.put("Entity", NBTUtil.writeUniqueId(entityUUID));
		compound.put("Inventory", inventory.serializeNBT());
		compound.putFloat("Speed", crushingspeed);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		if (compound.contains("Entity") && !isFrozen() && !isOccupied()) {
			entityUUID = NBTUtil.readUniqueId(compound.getCompound("Entity"));
			this.searchForEntity = true;
		}
		crushingspeed = compound.getFloat("Speed");
		inventory.deserializeNBT(compound.getCompound("Inventory"));
	}

	public void startCrushing(Entity entity) {
		processingEntity = entity;
		entityUUID = entity.getUniqueID();
	}

	private void itemInserted(ItemStack stack) {
		Optional<ProcessingRecipe<RecipeWrapper>> recipe = findRecipe();
		inventory.remainingTime = recipe.isPresent() ? recipe.get()
			.getProcessingDuration() : 100;
		inventory.appliedRecipe = false;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return handler.cast();
		return super.getCapability(cap, side);
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
