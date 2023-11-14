package com.simibubi.create.foundation.blockEntity.behaviour.filtering;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

public class FilteringBehaviour extends BlockEntityBehaviour implements ValueSettingsBehaviour {

	public static final BehaviourType<FilteringBehaviour> TYPE = new BehaviourType<>();

	public MutableComponent customLabel;
	ValueBoxTransform slotPositioning;
	boolean showCount;

	private FilterItemStack filter;
	
	public int count;
	public boolean upTo;
	private Predicate<ItemStack> predicate;
	private Consumer<ItemStack> callback;
	private Supplier<Boolean> isActive;
	private Supplier<Boolean> showCountPredicate;

	boolean recipeFilter;
	boolean fluidFilter;

	public FilteringBehaviour(SmartBlockEntity be, ValueBoxTransform slot) {
		super(be);
		filter = FilterItemStack.empty();
		slotPositioning = slot;
		showCount = false;
		callback = stack -> {
		};
		predicate = stack -> true;
		isActive = () -> true;
		count = AllConfigs.server().kinetics.filterMaxItemsToTransfer.get();
		showCountPredicate = () -> showCount;
		recipeFilter = false;
		fluidFilter = false;
		upTo = true;
	}

	@Override
	public boolean isSafeNBT() {
		return true;
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		nbt.put("Filter", getFilter().serializeNBT());
		nbt.putInt("FilterAmount", count);
		nbt.putBoolean("UpTo", upTo);
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		filter = FilterItemStack.of(nbt.getCompound("Filter"));
		count = nbt.getInt("FilterAmount");
		upTo = nbt.getBoolean("UpTo");

		// Migrate from previous behaviour
		if (count == 0) {
			upTo = true;
			count = filter.item()
				.getMaxStackSize();
		}

		super.read(nbt, clientPacket);
	}

	public FilteringBehaviour withCallback(Consumer<ItemStack> filterCallback) {
		callback = filterCallback;
		return this;
	}

	public FilteringBehaviour withPredicate(Predicate<ItemStack> filterPredicate) {
		predicate = filterPredicate;
		return this;
	}

	public FilteringBehaviour forRecipes() {
		recipeFilter = true;
		return this;
	}

	public FilteringBehaviour forFluids() {
		fluidFilter = true;
		return this;
	}

	public FilteringBehaviour onlyActiveWhen(Supplier<Boolean> condition) {
		isActive = condition;
		return this;
	}

	public FilteringBehaviour showCountWhen(Supplier<Boolean> condition) {
		showCountPredicate = condition;
		return this;
	}

	public FilteringBehaviour showCount() {
		showCount = true;
		return this;
	}

	public boolean setFilter(Direction face, ItemStack stack) {
		return setFilter(stack);
	}

	public void setLabel(MutableComponent label) {
		this.customLabel = label;
	}

	public boolean setFilter(ItemStack stack) {
		ItemStack filter = stack.copy();
		if (!filter.isEmpty() && !predicate.test(filter))
			return false;
		this.filter = FilterItemStack.of(filter);
		if (!upTo)
			count = Math.min(count, stack.getMaxStackSize());
		callback.accept(filter);
		blockEntity.setChanged();
		blockEntity.sendData();
		return true;
	}

	@Override
	public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
		if (getValueSettings().equals(settings))
			return;
		count = Mth.clamp(settings.value(), 1, filter.item()
			.getMaxStackSize());
		upTo = settings.row() == 0;
		blockEntity.setChanged();
		blockEntity.sendData();
		playFeedbackSound(this);
	}

	@Override
	public ValueSettings getValueSettings() {
		return new ValueSettings(upTo ? 0 : 1, count == 0 ? filter.item()
			.getMaxStackSize() : count);
	}

	@Override
	public void destroy() {
		if (filter.isFilterItem()) {
			Vec3 pos = VecHelper.getCenterOf(getPos());
			Level world = getWorld();
			world.addFreshEntity(new ItemEntity(world, pos.x, pos.y, pos.z, filter.item()
				.copy()));
		}
		super.destroy();
	}

	@Override
	public ItemRequirement getRequiredItems() {
		if (filter.isFilterItem())
			return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, filter.item());

		return ItemRequirement.NONE;
	}

	public ItemStack getFilter(Direction side) {
		return getFilter();
	}

	public ItemStack getFilter() {
		return filter.item();
	}

	public boolean isCountVisible() {
		return showCountPredicate.get() && filter.item()
			.getMaxStackSize() > 1;
	}

	public boolean test(ItemStack stack) {
		return !isActive() || filter.test(blockEntity.getLevel(), stack);
	}

	public boolean test(FluidStack stack) {
		return !isActive() || filter.test(blockEntity.getLevel(), stack);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean testHit(Vec3 hit) {
		BlockState state = blockEntity.getBlockState();
		Vec3 localHit = hit.subtract(Vec3.atLowerCornerOf(blockEntity.getBlockPos()));
		return slotPositioning.testHit(state, localHit);
	}

	public int getAmount() {
		return count;
	}

	public boolean anyAmount() {
		return count == 0;
	}

	@Override
	public boolean acceptsValueSettings() {
		return isCountVisible();
	}

	@Override
	public boolean isActive() {
		return isActive.get();
	}

	@Override
	public ValueBoxTransform getSlotPositioning() {
		return slotPositioning;
	}

	@Override
	public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
		ItemStack filter = getFilter(hitResult.getDirection());
		int maxAmount = (filter.getItem() instanceof FilterItem) ?
				AllConfigs.server().kinetics.filterMaxItemsToTransfer.get()
				: filter.getMaxStackSize();
		return new ValueSettingsBoard(Lang.translateDirect("logistics.filter.extracted_amount"), maxAmount, 16,
			Lang.translatedOptions("logistics.filter", "up_to", "exactly"),
			new ValueSettingsFormatter(this::formatValue));
	}

	public MutableComponent formatValue(ValueSettings value) {
		if (value.row() == 0 && value.value() == filter.item()
			.getMaxStackSize())
			return Lang.translateDirect("logistics.filter.any_amount_short");
		return Components.literal(((value.row() == 0) ? "\u2264" : "=") + Math.max(1, value.value()));
	}

	@Override
	public void onShortInteract(Player player, InteractionHand hand, Direction side) {
		Level level = getWorld();
		BlockPos pos = getPos();
		ItemStack itemInHand = player.getItemInHand(hand);
		ItemStack toApply = itemInHand.copy();

		if (AllItems.WRENCH.isIn(toApply))
			return;
		if (AllBlocks.MECHANICAL_ARM.isIn(toApply))
			return;
		if (level.isClientSide())
			return;

		if (getFilter(side).getItem() instanceof FilterItem) {
			if (!player.isCreative() || ItemHelper
				.extract(new InvWrapper(player.getInventory()),
					stack -> ItemHandlerHelper.canItemStacksStack(stack, getFilter(side)), true)
				.isEmpty())
				player.getInventory()
					.placeItemBackInInventory(getFilter(side).copy());
		}

		if (toApply.getItem() instanceof FilterItem)
			toApply.setCount(1);

		if (!setFilter(side, toApply)) {
			player.displayClientMessage(Lang.translateDirect("logistics.filter.invalid_item"), true);
			AllSoundEvents.DENY.playOnServer(player.level, player.blockPosition(), 1, 1);
			return;
		}
		
		if (!player.isCreative()) {
			if (toApply.getItem() instanceof FilterItem) {
				if (itemInHand.getCount() == 1)
					player.setItemInHand(hand, ItemStack.EMPTY);
				else
					itemInHand.shrink(1);
			}
		}

		level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
	}

	public MutableComponent getLabel() {
		if (customLabel != null)
			return customLabel;
		return Lang.translateDirect(
			recipeFilter ? "logistics.recipe_filter" : fluidFilter ? "logistics.fluid_filter" : "logistics.filter");
	}

	@Override
	public String getClipboardKey() {
		return "Filtering";
	}

	@Override
	public boolean writeToClipboard(CompoundTag tag, Direction side) {
		ValueSettingsBehaviour.super.writeToClipboard(tag, side);
		ItemStack filter = getFilter(side);
		tag.put("Filter", filter.serializeNBT());
		return true;
	}

	@Override
	public boolean readFromClipboard(CompoundTag tag, Player player, Direction side, boolean simulate) {
		boolean upstreamResult = ValueSettingsBehaviour.super.readFromClipboard(tag, player, side, simulate);
		if (!tag.contains("Filter"))
			return upstreamResult;
		if (simulate)
			return true;
		if (getWorld().isClientSide)
			return true;

		ItemStack refund = ItemStack.EMPTY;
		if (getFilter(side).getItem() instanceof FilterItem && !player.isCreative())
			refund = getFilter(side).copy();

		ItemStack copied = ItemStack.of(tag.getCompound("Filter"));

		if (copied.getItem() instanceof FilterItem filterType && !player.isCreative()) {
			InvWrapper inv = new InvWrapper(player.getInventory());

			for (boolean preferStacksWithoutData : Iterate.trueAndFalse) {
				if (refund.getItem() != filterType && ItemHelper
					.extract(inv, stack -> stack.getItem() == filterType && preferStacksWithoutData != stack.hasTag(),
						1, false)
					.isEmpty())
					continue;

				if (!refund.isEmpty() && refund.getItem() != filterType)
					player.getInventory()
						.placeItemBackInInventory(refund);

				setFilter(side, copied);
				return true;
			}

			player.displayClientMessage(Lang
				.translate("logistics.filter.requires_item_in_inventory", copied.getHoverName()
					.copy()
					.withStyle(ChatFormatting.WHITE))
				.style(ChatFormatting.RED)
				.component(), true);
			AllSoundEvents.DENY.playOnServer(player.level, player.blockPosition(), 1, 1);
			return false;
		}

		if (!refund.isEmpty())
			player.getInventory()
				.placeItemBackInInventory(refund);

		return setFilter(side, copied);
	}

}
