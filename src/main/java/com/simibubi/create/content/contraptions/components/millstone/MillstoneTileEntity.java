package com.simibubi.create.content.contraptions.components.millstone;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.ViewOnlyWrappedStorageView;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.sound.SoundScapes.AmbienceGroup;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemTransferable;
import io.github.fabricators_of_create.porting_lib.transfer.item.RecipeWrapper;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MillstoneTileEntity extends KineticTileEntity implements ItemTransferable {

	public ItemStackHandler inputInv;
	public ItemStackHandler outputInv;
	public MillstoneInventoryHandler capability;
	public int timer;
	private MillingRecipe lastRecipe;

	public MillstoneTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		inputInv = new ItemStackHandler(1);
		outputInv = new ItemStackHandler(9);
		capability = new MillstoneInventoryHandler();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
		super.addBehaviours(behaviours);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void tickAudio() {
		super.tickAudio();

		if (getSpeed() == 0)
			return;
		if (inputInv.getStackInSlot(0)
			.isEmpty())
			return;

		float pitch = Mth.clamp((Math.abs(getSpeed()) / 256f) + .45f, .85f, 1f);
		SoundScapes.play(AmbienceGroup.MILLING, worldPosition, pitch);
	}

	@Override
	public void tick() {
		super.tick();

		if (getSpeed() == 0)
			return;
		for (int i = 0; i < outputInv.getSlots(); i++)
			if (outputInv.getStackInSlot(i)
				.getCount() == outputInv.getSlotLimit(i))
				return;

		if (timer > 0) {
			timer -= getProcessingSpeed();

			if (level.isClientSide) {
				spawnParticles();
				return;
			}
			if (timer <= 0)
				process();
			return;
		}

		if (inputInv.getStackInSlot(0)
			.isEmpty())
			return;

		RecipeWrapper inventoryIn = new RecipeWrapper(inputInv);
		if (lastRecipe == null || !lastRecipe.matches(inventoryIn, level)) {
			Optional<MillingRecipe> recipe = AllRecipeTypes.MILLING.find(inventoryIn, level);
			if (!recipe.isPresent()) {
				timer = 100;
				sendData();
			} else {
				lastRecipe = recipe.get();
				timer = lastRecipe.getProcessingDuration();
				sendData();
			}
			return;
		}

		timer = lastRecipe.getProcessingDuration();
		sendData();
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
	}

	private void process() {
		RecipeWrapper inventoryIn = new RecipeWrapper(inputInv);

		if (lastRecipe == null || !lastRecipe.matches(inventoryIn, level)) {
			Optional<MillingRecipe> recipe = AllRecipeTypes.MILLING.find(inventoryIn, level);
			if (!recipe.isPresent())
				return;
			lastRecipe = recipe.get();
		}

		ItemStack stackInSlot = inputInv.getStackInSlot(0);
		stackInSlot.shrink(1);
		inputInv.setStackInSlot(0, stackInSlot);
		try (Transaction t = TransferUtil.getTransaction()) {
			lastRecipe.rollResults().forEach(stack -> outputInv.insert(ItemVariant.of(stack), stack.getCount(), t));
			t.commit();
		}
		sendData();
		setChanged();
	}

	public void spawnParticles() {
		ItemStack stackInSlot = inputInv.getStackInSlot(0);
		if (stackInSlot.isEmpty())
			return;

		ItemParticleOption data = new ItemParticleOption(ParticleTypes.ITEM, stackInSlot);
		float angle = level.random.nextFloat() * 360;
		Vec3 offset = new Vec3(0, 0, 0.5f);
		offset = VecHelper.rotate(offset, angle, Axis.Y);
		Vec3 target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Axis.Y);

		Vec3 center = offset.add(VecHelper.getCenterOf(worldPosition));
		target = VecHelper.offsetRandomly(target.subtract(offset), level.random, 1 / 128f);
		level.addParticle(data, center.x, center.y, center.z, target.x, target.y, target.z);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("Timer", timer);
		compound.put("InputInventory", inputInv.serializeNBT());
		compound.put("OutputInventory", outputInv.serializeNBT());
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		timer = compound.getInt("Timer");
		inputInv.deserializeNBT(compound.getCompound("InputInventory"));
		outputInv.deserializeNBT(compound.getCompound("OutputInventory"));
		super.read(compound, clientPacket);
	}

	public int getProcessingSpeed() {
		return Mth.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
	}

	@Nullable
	@Override
	public Storage<ItemVariant> getItemStorage(@Nullable Direction direction) {
		return capability;
	}

	private boolean canProcess(ItemStack stack) {
		ItemStackHandler tester = new ItemStackHandler(1);
		tester.setStackInSlot(0, stack);
		RecipeWrapper inventoryIn = new RecipeWrapper(tester);

		if (lastRecipe != null && lastRecipe.matches(inventoryIn, level))
			return true;
		return AllRecipeTypes.MILLING.find(inventoryIn, level)
			.isPresent();
	}

	private class MillstoneInventoryHandler extends CombinedStorage<ItemVariant, ItemStackHandler> {

		public MillstoneInventoryHandler() {
			super(List.of(inputInv, outputInv));
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (canProcess(resource.toStack()))
				return inputInv.insert(resource, maxAmount, transaction);
			return 0;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			return outputInv.extract(resource, maxAmount, transaction);
		}

		@Override
		public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
			return new MillstoneInventoryHandlerIterator(transaction);
		}

		private class MillstoneInventoryHandlerIterator implements Iterator<StorageView<ItemVariant>> {
			private final TransactionContext ctx;
			private boolean open = true;
			private boolean output = true;
			private Iterator<StorageView<ItemVariant>> wrapped;

			public MillstoneInventoryHandlerIterator(TransactionContext ctx) {
				this.ctx = ctx;
				ctx.addCloseCallback((t, r) -> open = false);
				wrapped = outputInv.iterator(ctx);
			}

			@Override
			public boolean hasNext() {
				return open && wrapped.hasNext();
			}

			@Override
			public StorageView<ItemVariant> next() {
				StorageView<ItemVariant> view = wrapped.next();
				if (!output) view = new ViewOnlyWrappedStorageView<>(view);
				if (output && !hasNext()) {
					wrapped = inputInv.iterator(ctx);
					output = false;
				}
				return view;
			}
		}
	}

}
