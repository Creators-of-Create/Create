package com.simibubi.create.lib.transfer.fluid;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid;
import com.simibubi.create.lib.mixin.compat.fapi.accessor.EmptyItemFluidStorageAccessor;
import com.simibubi.create.lib.mixin.compat.fapi.accessor.FullItemFluidStorageAccessor;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import net.minecraft.world.level.material.Fluid;

import java.util.Objects;

@SuppressWarnings({"UnstableApiUsage"})
public class FluidStorageHandlerItem extends FluidStorageHandler implements IFluidHandlerItem {
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
				if (!sim) t.commit();
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
		if (empty()) {
			try (Transaction t = Transaction.openOuter()) {
				if (ctx.exchange(getEmptied(), 1, t) == 1) {
					emptied = !sim;
				}
			}
		}
		return drained;
	}

	public ItemVariant getEmptied() {
		if (emptied) return ItemVariant.blank();
		ItemStack stack = getContainer();
		Item item = stack.getItem();
		if (item instanceof BucketItem) {
			return ItemVariant.of(Items.BUCKET);
		} else if (item instanceof BottleItem || item instanceof HoneyBottleItem) {
			return ItemVariant.of(Items.GLASS_BOTTLE);
		} else if (storage instanceof FullItemFluidStorageAccessor access) {
			return access.create$fullToEmptyMapping().apply(ItemVariant.of(stack));
		} else if (storage instanceof EmptyItemFluidStorageAccessor access) {
			return ItemVariant.of(access.create$emptyItem());
		} else if (storage instanceof CombinedStorage combined) {
			if (combined.parts.size() == 1) {
				Object storage = combined.parts.get(0);
				if (storage instanceof FullItemFluidStorageAccessor access) {
					return access.create$fullToEmptyMapping().apply(ItemVariant.of(stack));
				} else if (storage instanceof EmptyItemFluidStorageAccessor access) {
					return ItemVariant.of(access.create$emptyItem());
				}
			}
		}
		return ItemVariant.of(stack);
	}

	public ItemVariant getFilled(FluidStack fluid) {
		ItemStack stack = getContainer();
		Item item = stack.getItem();
		ItemVariant var = ItemVariant.of(stack);
		Fluid contained = fluid.getFluid();
		if (item instanceof BucketItem) {
			Item bucket = fluid.getFluid().getBucket();
			if (bucket != Items.AIR) return ItemVariant.of(bucket);
		}

		if (item instanceof BottleItem) {
			if (AllFluids.HONEY.is(contained)) {
				return ItemVariant.of(Items.HONEY_BOTTLE);
			} else if (AllFluids.POTION.is(contained)) {
				String potion = fluid.getOrCreateTag().getString("Potion");
				if (!potion.isEmpty()) {
					ItemStack potionStack = new ItemStack(Items.POTION);
					PotionUtils.setPotion(potionStack, Registry.POTION.get(new ResourceLocation(potion)));
					return ItemVariant.of(potionStack);
				}
			} else if (FluidTags.WATER.contains(contained)) {
				return ItemVariant.of(PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.WATER));
			}
		} else if (storage instanceof FullItemFluidStorageAccessor access) {
			return ItemVariant.of(access.create$fullItem());
		} else if (storage instanceof EmptyItemFluidStorageAccessor access) {
			return access.create$emptyToFullMapping().apply(var);
		} else if (storage instanceof CombinedStorage combined) {
			if (combined.parts.size() == 1) {
				Object storage = combined.parts.get(0);
				if (storage instanceof FullItemFluidStorageAccessor access) {
					return ItemVariant.of(access.create$fullItem());
				} else if (storage instanceof EmptyItemFluidStorageAccessor access) {
					return access.create$emptyToFullMapping().apply(var);
				}
			}
		}
		return var;
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
