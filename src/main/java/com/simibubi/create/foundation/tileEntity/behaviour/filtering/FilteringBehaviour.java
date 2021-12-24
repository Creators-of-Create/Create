package com.simibubi.create.foundation.tileEntity.behaviour.filtering;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.lib.transfer.fluid.FluidStack;
import com.simibubi.create.lib.transfer.item.ItemHandlerHelper;
import com.simibubi.create.lib.util.NBTSerializer;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FilteringBehaviour extends TileEntityBehaviour {

	public static BehaviourType<FilteringBehaviour> TYPE = new BehaviourType<>();

	ValueBoxTransform slotPositioning;
	boolean showCount;
	Vec3 textShift;

	private ItemStack filter;
	public int count;
	private Consumer<ItemStack> callback;
	private Supplier<Boolean> isActive;
	private Supplier<Boolean> showCountPredicate;

	int scrollableValue;
	int ticksUntilScrollPacket;
	boolean forceClientState;
	boolean recipeFilter;
	boolean fluidFilter;

	public FilteringBehaviour(SmartTileEntity te, ValueBoxTransform slot) {
		super(te);
		filter = ItemStack.EMPTY;
		slotPositioning = slot;
		showCount = false;
		callback = stack -> {
		};
		isActive = () -> true;
		textShift = Vec3.ZERO;
		count = 0;
		ticksUntilScrollPacket = -1;
		showCountPredicate = () -> showCount;
		recipeFilter = false;
		fluidFilter = false;
	}

	@Override
	public boolean isSafeNBT() { return true; }

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		nbt.put("Filter", NBTSerializer.serializeNBT(getFilter()));
		nbt.putInt("FilterAmount", count);

		if (clientPacket && forceClientState) {
			nbt.putBoolean("ForceScrollable", true);
			forceClientState = false;
		}
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		filter = ItemStack.of(nbt.getCompound("Filter"));
		count = nbt.getInt("FilterAmount");
		if (nbt.contains("ForceScrollable")) {
			scrollableValue = count;
			ticksUntilScrollPacket = -1;
		}
		super.read(nbt, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();

		if (!getWorld().isClientSide)
			return;
		if (ticksUntilScrollPacket == -1)
			return;
		if (ticksUntilScrollPacket > 0) {
			ticksUntilScrollPacket--;
			return;
		}

		AllPackets.channel.sendToServer(new FilteringCountUpdatePacket(getPos(), scrollableValue));
		ticksUntilScrollPacket = -1;
	}

	public FilteringBehaviour withCallback(Consumer<ItemStack> filterCallback) {
		callback = filterCallback;
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

	public FilteringBehaviour moveText(Vec3 shift) {
		textShift = shift;
		return this;
	}

	@Override
	public void initialize() {
		super.initialize();
		scrollableValue = count;
	}

	public void setFilter(Direction face, ItemStack stack) {
		setFilter(stack);
	}

	public void setFilter(ItemStack stack) {
		boolean confirm = ItemHandlerHelper.canItemStacksStack(stack, filter);
		filter = stack.copy();
		callback.accept(filter);
		count = !confirm ? 0
			: (filter.getItem() instanceof FilterItem) ? 0 : Math.min(stack.getCount(), stack.getMaxStackSize());
		forceClientState = true;

		tileEntity.setChanged();
		tileEntity.sendData();
	}

	@Override
	public void destroy() {
		if (filter.getItem() instanceof FilterItem) {
			Vec3 pos = VecHelper.getCenterOf(getPos());
			Level world = getWorld();
			world.addFreshEntity(new ItemEntity(world, pos.x, pos.y, pos.z, filter.copy()));
		}

		super.destroy();
	}

	@Override
	public ItemRequirement getRequiredItems() {
		Item filterItem = filter.getItem();
		if (filterItem instanceof FilterItem)
			return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, filterItem);

		return ItemRequirement.NONE;
	}

	public ItemStack getFilter(Direction side) {
		return getFilter();
	}

	public ItemStack getFilter() {
		return filter.copy();
	}

	public boolean isCountVisible() {
		return showCountPredicate.get();
	}

	public boolean test(ItemStack stack) {
		return !isActive() || filter.isEmpty() || FilterItem.test(tileEntity.getLevel(), stack, filter);
	}

	public boolean test(FluidStack stack) {
		return !isActive() || filter.isEmpty() || FilterItem.test(tileEntity.getLevel(), stack, filter);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public boolean testHit(Vec3 hit) {
		BlockState state = tileEntity.getBlockState();
		Vec3 localHit = hit.subtract(Vec3.atLowerCornerOf(tileEntity.getBlockPos()));
		return slotPositioning.testHit(state, localHit);
	}

	public int getAmount() {
		return count;
	}

	public boolean anyAmount() {
		return count == 0;
	}

	public boolean isActive() {
		return isActive.get();
	}

}
