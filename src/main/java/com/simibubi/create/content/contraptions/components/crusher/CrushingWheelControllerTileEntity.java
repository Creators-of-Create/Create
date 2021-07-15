package com.simibubi.create.content.contraptions.components.crusher;

import static net.minecraft.block.DirectionalBlock.FACING;

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
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.sound.SoundScapes.AmbienceGroup;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
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
		behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen(this::supportsDirectBeltInput));
	}

	private boolean supportsDirectBeltInput(Direction side) {
		BlockState blockState = getBlockState();
		if (blockState == null)
			return false;
		Direction direction = blockState.getValue(CrushingWheelControllerBlock.FACING);
		return direction == Direction.DOWN || direction == side;
	}

	@Override
	public void tick() {
		super.tick();
		if (searchForEntity) {
			searchForEntity = false;
			List<Entity> search = level.getEntities((Entity) null, new AxisAlignedBB(getBlockPos()),
				e -> entityUUID.equals(e.getUUID()));
			if (search.isEmpty())
				clear();
			else
				processingEntity = search.get(0);
		}

		if (!isOccupied())
			return;
		if (crushingspeed == 0)
			return;

		if (level.isClientSide)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> this.tickAudio());

		float speed = crushingspeed * 4;

		Vector3d centerPos = VecHelper.getCenterOf(worldPosition);
		Direction facing = getBlockState().getValue(FACING);
		int offset = facing.getAxisDirection()
			.getStep();
		Vector3d outSpeed = new Vector3d((facing.getAxis() == Axis.X ? 0.25D : 0.0D) * offset,
			offset == 1 ? (facing.getAxis() == Axis.Y ? 0.5D : 0.0D) : 0.0D // Increased upwards speed so upwards
																			// crushing wheels shoot out the item
																			// properly.
			, (facing.getAxis() == Axis.Z ? 0.25D : 0.0D) * offset); // No downwards speed, so downwards crushing wheels
																		// drop the items as before.
		Vector3d outPos = centerPos.add((facing.getAxis() == Axis.X ? .55f * offset : 0f),
			(facing.getAxis() == Axis.Y ? .55f * offset : 0f), (facing.getAxis() == Axis.Z ? .55f * offset : 0f));

		if (!hasEntity()) {

			float processingSpeed =
				MathHelper.clamp((speed) / (!inventory.appliedRecipe ? MathHelper.log2(inventory.getStackInSlot(0)
					.getCount()) : 1), .25f, 20);
			inventory.remainingTime -= processingSpeed;
			spawnParticles(inventory.getStackInSlot(0));

			if (level.isClientSide)
				return;

			if (inventory.remainingTime < 20 && !inventory.appliedRecipe) {
				applyRecipe();
				inventory.appliedRecipe = true;
				level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
				return;
			}

			if (inventory.remainingTime > 0) {
				return;
			}
			inventory.remainingTime = 0;

			// Output Items
			if (facing.getAxis()
				.isHorizontal() || facing == Direction.DOWN) {
				BlockPos nextPos = worldPosition.offset(facing.getAxis() == Axis.X ? 1f * offset : 0f, (-1f),
					facing.getAxis() == Axis.Z ? 1f * offset : 0f);
				DirectBeltInputBehaviour behaviour =
					TileEntityBehaviour.get(level, nextPos, DirectBeltInputBehaviour.TYPE);
				if (behaviour != null) {
					boolean changed = false;
					if (!behaviour.canInsertFromSide(facing))
						return;
					for (int slot = 0; slot < inventory.getSlots(); slot++) {
						ItemStack stack = inventory.getStackInSlot(slot);
						if (stack.isEmpty())
							continue;
						ItemStack remainder = behaviour.handleInsertion(stack, facing, false);
						if (remainder.equals(stack, false))
							continue;
						inventory.setStackInSlot(slot, remainder);
						changed = true;
					}
					if (changed) {
						setChanged();
						sendData();
					}
					return;
				}
			}

			// Eject Items
			for (int slot = 0; slot < inventory.getSlots(); slot++) {
				ItemStack stack = inventory.getStackInSlot(slot);
				if (stack.isEmpty())
					continue;
				ItemEntity entityIn = new ItemEntity(level, outPos.x, outPos.y, outPos.z, stack);
				entityIn.setDeltaMovement(outSpeed);
				entityIn.getPersistentData()
					.put("BypassCrushingWheel", NBTUtil.writeBlockPos(worldPosition));
				level.addFreshEntity(entityIn);
			}
			inventory.clear();
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);

			return;
		}

		if (!processingEntity.isAlive() || !processingEntity.getBoundingBox()
			.intersects(new AxisAlignedBB(worldPosition).inflate(.5f))) {
			clear();
			return;
		}

		double xMotion = ((worldPosition.getX() + .5f) - processingEntity.getX()) / 2f;
		double zMotion = ((worldPosition.getZ() + .5f) - processingEntity.getZ()) / 2f;
		if (processingEntity.isShiftKeyDown())
			xMotion = zMotion = 0;
		double movement = Math.max(-speed / 4f, -.5f) * -offset;
		processingEntity.setDeltaMovement(
			new Vector3d(facing.getAxis() == Axis.X ? movement : xMotion, facing.getAxis() == Axis.Y ? movement : 0f // Do
																														// not
																														// move
																														// entities
																														// upwards
																														// or
																														// downwards
																														// for
																														// horizontal
																														// crushers,
				, facing.getAxis() == Axis.Z ? movement : zMotion)); // Or they'll only get their feet crushed.

		if (level.isClientSide)
			return;

		if (!(processingEntity instanceof ItemEntity)) {
			Vector3d entityOutPos = outPos.add(facing.getAxis() == Axis.X ? .5f * offset : 0f,
				facing.getAxis() == Axis.Y ? .5f * offset : 0f, facing.getAxis() == Axis.Z ? .5f * offset : 0f);
			int crusherDamage = AllConfigs.SERVER.kinetics.crushingDamage.get();

			if (processingEntity instanceof LivingEntity) {
				if ((((LivingEntity) processingEntity).getHealth() - crusherDamage <= 0) // Takes LivingEntity instances
																							// as exception, so it can
																							// move them before it would
																							// kill them.
					&& (((LivingEntity) processingEntity).hurtTime <= 0)) { // This way it can actually output the items
																			// to the right spot.
					processingEntity.setPos(entityOutPos.x, entityOutPos.y, entityOutPos.z);
				}
			}
			processingEntity.hurt(CrushingWheelTileEntity.damageSource, crusherDamage);
			if (!processingEntity.isAlive()) {
				processingEntity.setPos(entityOutPos.x, entityOutPos.y, entityOutPos.z);
			}
			return;
		}

		ItemEntity itemEntity = (ItemEntity) processingEntity;
		itemEntity.setPickUpDelay(20);
		if (facing.getAxis() == Axis.Y) {
			if (processingEntity.getY() * -offset < (centerPos.y - .25f) * -offset) {
				intakeItem(itemEntity);
			}
		} else if (facing.getAxis() == Axis.Z) {
			if (processingEntity.getZ() * -offset < (centerPos.z - .25f) * -offset) {
				intakeItem(itemEntity);
			}
		} else {
			if (processingEntity.getX() * -offset < (centerPos.x - .25f) * -offset) {
				intakeItem(itemEntity);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void tickAudio() {
		float pitch = MathHelper.clamp((crushingspeed / 256f) + .45f, .85f, 1f);
		if (entityUUID == null && inventory.getStackInSlot(0)
			.isEmpty())
			return;
		SoundScapes.play(AmbienceGroup.CRUSHING, worldPosition, pitch);
	}

	private void intakeItem(ItemEntity itemEntity) {
		inventory.clear();
		inventory.setStackInSlot(0, itemEntity.getItem()
			.copy());
		itemInserted(inventory.getStackInSlot(0));
		itemEntity.remove();
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
	}

	protected void spawnParticles(ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return;

		IParticleData particleData = null;
		if (stack.getItem() instanceof BlockItem)
			particleData = new BlockParticleData(ParticleTypes.BLOCK, ((BlockItem) stack.getItem()).getBlock()
				.defaultBlockState());
		else
			particleData = new ItemParticleData(ParticleTypes.ITEM, stack);

		Random r = level.random;
		for (int i = 0; i < 4; i++)
			level.addParticle(particleData, worldPosition.getX() + r.nextFloat(), worldPosition.getY() + r.nextFloat(),
				worldPosition.getZ() + r.nextFloat(), 0, 0, 0);
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
		Optional<ProcessingRecipe<RecipeWrapper>> crushingRecipe = AllRecipeTypes.CRUSHING.find(wrapper, level);
		if (!crushingRecipe.isPresent())
			crushingRecipe = AllRecipeTypes.MILLING.find(wrapper, level);
		return crushingRecipe;
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		if (hasEntity())
			compound.put("Entity", NBTUtil.createUUID(entityUUID));
		compound.put("Inventory", inventory.serializeNBT());
		compound.putFloat("Speed", crushingspeed);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		if (compound.contains("Entity") && !isOccupied()) {
			entityUUID = NBTUtil.loadUUID(NBTHelper.getINBT(compound, "Entity"));
			this.searchForEntity = true;
		}
		crushingspeed = compound.getFloat("Speed");
		inventory.deserializeNBT(compound.getCompound("Inventory"));
	}

	public void startCrushing(Entity entity) {
		processingEntity = entity;
		entityUUID = entity.getUUID();
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

}
