package com.simibubi.create.modules.logistics.block;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.logistics.item.CardboardBoxItem;
import com.simibubi.create.modules.logistics.transport.CardboardBoxEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

// Its like delegation but better!
public interface IExtractor extends ITickableTileEntity, IInventoryManipulator {

	public enum State {
		WAITING_FOR_INVENTORY, WAITING_FOR_ENTITY, RUNNING, ON_COOLDOWN, LOCKED;
	}

	public State getState();

	public void setState(State state);

	public int tickCooldown();

	@Override
	default void tick() {
		if (isFrozen())
			return;

		State state = getState();

		if (state == State.LOCKED)
			return;

		if (state == State.ON_COOLDOWN || state == State.WAITING_FOR_INVENTORY) {
			int cooldown = tickCooldown();
			if (cooldown <= 0) {
				setState(State.RUNNING);
				if (!getInventory().isPresent())
					findNewInventory();
			}
			return;
		}

		boolean hasSpace = hasSpaceForExtracting();
		boolean hasInventory = getInventory().isPresent();
		ItemStack toExtract = ItemStack.EMPTY;

		if (hasSpace && hasInventory) {
			toExtract = extract(true);

			if (!matchesFilter(toExtract))
				toExtract = ItemStack.EMPTY;
		}

		if (state == State.WAITING_FOR_ENTITY) {
			if (hasSpace)
				setState(State.RUNNING);
		}

		if (state == State.RUNNING) {
			if (!hasSpace) {
				setState(State.WAITING_FOR_ENTITY);
				return;
			}
			if (!hasInventory || toExtract.isEmpty()) {
				setState(State.WAITING_FOR_INVENTORY);
				return;
			}

			extract(false);
			setState(State.ON_COOLDOWN);
			return;
		}

	}

	default boolean matchesFilter(ItemStack stack) {
		FilteringBehaviour filteringBehaviour = TileEntityBehaviour.get((TileEntity) this, FilteringBehaviour.TYPE);
		return filteringBehaviour == null || filteringBehaviour.test(stack);
	}

	public default void setLocked(boolean locked) {
		setState(locked ? State.LOCKED : State.ON_COOLDOWN);
	}

	public default void neighborChanged() {
		if (isFrozen())
			return;

		boolean hasSpace = hasSpaceForExtracting();
		boolean hasInventory = getInventory().isPresent();
		ItemStack toExtract = ItemStack.EMPTY;

		if (hasSpace && hasInventory) {
			toExtract = extract(true);
			if (!matchesFilter(toExtract))
				toExtract = ItemStack.EMPTY;
		}

		if (getState() == State.WAITING_FOR_INVENTORY) {
			if (!hasInventory) {
				if (findNewInventory()) {
					setState(State.RUNNING);
				}
			}
			if (!toExtract.isEmpty())
				setState(State.RUNNING);
			return;
		}
	}

	default boolean hasSpaceForExtracting() {
		BlockPos pos = getPos();
		World world = getWorld();

		if (AllBlocks.BELT.typeOf(world.getBlockState(pos.down()))) {
			TileEntity te = world.getTileEntity(pos.down());
			if (te != null && te instanceof BeltTileEntity) {
				BeltTileEntity belt = (BeltTileEntity) te;
				BeltTileEntity controller = belt.getControllerTE();
				if (controller != null) {
					if (!controller.getInventory().canInsertFrom(belt.index, Direction.UP))
						return false;
				}
			}
		}

		return world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(getPos())).isEmpty();
	}

	default ItemStack extract(boolean simulate) {
		IItemHandler inv = getInventory().orElse(null);
		ItemStack extracting = ItemStack.EMPTY;
		FilteringBehaviour filteringBehaviour = TileEntityBehaviour.get((TileEntity) this, FilteringBehaviour.TYPE);
		ItemStack filterItem = filteringBehaviour == null ? ItemStack.EMPTY : filteringBehaviour.getFilter();
		World world = getWorld();
		int extractionCount = filterItem.isEmpty() ? CreateConfig.parameters.extractorAmount.get()
				: filterItem.getCount();
		boolean checkHasEnoughItems = !filterItem.isEmpty();
		boolean hasEnoughItems = !checkHasEnoughItems;

		Extraction: do {
			extracting = ItemStack.EMPTY;

			for (int slot = 0; slot < inv.getSlots(); slot++) {
				ItemStack stack = inv.extractItem(slot, extractionCount - extracting.getCount(), true);
				ItemStack compare = stack.copy();

				compare.setCount(filterItem.getCount());
				if (!filterItem.isEmpty() && !filterItem.equals(compare, false))
					continue;

				compare.setCount(extracting.getCount());
				if (!extracting.isEmpty() && !extracting.equals(compare, false))
					continue;

				if (extracting.isEmpty())
					extracting = stack.copy();
				else
					extracting.grow(stack.getCount());

				if (!simulate && hasEnoughItems)
					inv.extractItem(slot, stack.getCount(), false);

				if (extracting.getCount() >= extractionCount) {
					if (checkHasEnoughItems) {
						hasEnoughItems = true;
						checkHasEnoughItems = false;
						continue Extraction;
					} else {
						break Extraction;
					}
				}
			}

			if (checkHasEnoughItems)
				checkHasEnoughItems = false;
			else
				break Extraction;
		} while (true);

		if (!simulate && hasEnoughItems) {
			Vec3d entityPos = VecHelper.getCenterOf(getPos()).add(0, -0.5f, 0);
			Entity entityIn = null;

			if (extracting.getItem() instanceof CardboardBoxItem) {
				Direction face = getWorld().getBlockState(getPos()).get(HORIZONTAL_FACING).getOpposite();
				entityIn = new CardboardBoxEntity(world, entityPos, extracting, face);
				world.playSound(null, getPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, .25f, .05f);

			} else {
				entityIn = new ItemEntity(world, entityPos.x, entityPos.y, entityPos.z, extracting);
				entityIn.setMotion(Vec3d.ZERO);
				world.playSound(null, getPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, .125f, .1f);
			}

			world.addEntity(entityIn);
		}

		return extracting;
	}

	public static boolean isFrozen() {
		return CreateConfig.parameters.freezeExtractors.get();
	}

}
