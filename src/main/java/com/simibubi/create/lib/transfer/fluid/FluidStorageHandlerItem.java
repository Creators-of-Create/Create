package com.simibubi.create.lib.transfer.fluid;

import com.simibubi.create.lib.mixin.compat.fapi.accessor.FullItemFluidStorageAccessor;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnstableApiUsage"})
public class FluidStorageHandlerItem extends FluidStorageHandler implements IFluidHandlerItem {
	public static final Map<Fluid, BucketItem> BUCKETS = new HashMap<>();

	protected ContainerItemContext ctx;
	protected boolean emptied;

	public FluidStorageHandlerItem(ContainerItemContext ctx, Storage<FluidVariant> storage) {
		super(storage);
		this.ctx = ctx;
		emptied = empty();
	}

	@Override
	public ItemStack getContainer() {
		ItemStack stack = ctx.getItemVariant().toStack();
		if (stack.isEmpty()) return stack;
		stack.setCount((int) ctx.getAmount());
		return stack;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		if (emptied) return FluidStack.empty();
		return super.getFluidInTank(tank);
	}

	@Override
	public long fill(FluidStack stack, boolean sim) {
		if (emptied) {
			try (Transaction t = Transaction.openOuter()) {
				if (ctx.exchange(getFilled(stack), 1, t) == 1) {
					emptied = !sim;
				}
			}
		}
		if (emptied) return 0;
		return super.fill(stack, sim);
	}

	@Override
	public FluidStack drain(long amount, boolean sim) {
		if (emptied) return FluidStack.empty();
		FluidStack drained = super.drain(amount, sim);
		if (empty()) {
			try (Transaction t = Transaction.openOuter()) {
				if (ctx.exchange(getEmptied(), 1, t) == 1) {
					emptied = !sim;
				}
			}
		}
		return drained;
	}

	@Override
	public FluidStack drain(FluidStack stack, boolean sim) {
		if (emptied) return FluidStack.empty();
		FluidStack drained = super.drain(stack, sim);
		if (!sim && empty()) {
			emptied = true;
		}
		return drained;
	}

	public ItemVariant getEmptied() {
		if (emptied) return ItemVariant.blank();
		ItemStack stack = getContainer();
		if (stack.getItem() instanceof BucketItem) {
			return ItemVariant.of(Items.BUCKET);
		} else if (stack.getItem() instanceof BottleItem) {
			return ItemVariant.of(Items.GLASS_BOTTLE);
		} else if (storage instanceof FullItemFluidStorageAccessor access) {
			return access.create$fullToEmptyMapping().apply(ItemVariant.of(stack));
		} else if (storage instanceof CombinedStorage combined) {
			if (combined.parts.size() == 1) {
				if (combined.parts.get(0) instanceof FullItemFluidStorageAccessor access) {
					return access.create$fullToEmptyMapping().apply(ItemVariant.of(stack));
				}
			}
		}
		return ItemVariant.of(stack);
	}

	public ItemVariant getFilled(FluidStack fluid) {
		ItemStack stack = getContainer();
		if (stack.getItem() instanceof BucketItem) {
			BucketItem bucket = BUCKETS.get(fluid.getFluid());
			if (bucket != null) return ItemVariant.of(bucket);
		} else if (stack.getItem() instanceof BottleItem) {
			return ItemVariant.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
		} else if (storage instanceof FullItemFluidStorageAccessor access) {
			return ItemVariant.of(access.create$fullItem());
		} else if (storage instanceof CombinedStorage combined) {
			if (combined.parts.size() == 1) {
				if (combined.parts.get(0) instanceof FullItemFluidStorageAccessor access) {
					return ItemVariant.of(access.create$fullItem());
				}
			}
		}
		return ItemVariant.of(stack);
	}

	public boolean empty() {
		try (Transaction t = Transaction.openOuter()) {
			for (StorageView<FluidVariant> view : storage.iterable(t)) {
				if (view.getAmount() > 0) {
					return false;
				}
			}
		}
		return true;
	}
}
