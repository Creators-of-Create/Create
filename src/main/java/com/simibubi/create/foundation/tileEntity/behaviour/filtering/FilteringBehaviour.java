package com.simibubi.create.foundation.tileEntity.behaviour.filtering;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FilteringBehaviour extends TileEntityBehaviour {

	public static BehaviourType<FilteringBehaviour> TYPE = new BehaviourType<>();

	ValueBoxTransform slotPositioning;
	boolean showCount;
	Vec3d textShift;

	private ItemStack filter;
	public int count;
	private Consumer<ItemStack> callback;
	private Supplier<Boolean> isActive;
	private Supplier<Boolean> showCountPredicate;

	int scrollableValue;
	int ticksUntilScrollPacket;
	boolean forceClientState;
	boolean recipeFilter;

	public FilteringBehaviour(SmartTileEntity te, ValueBoxTransform slot) {
		super(te);
		filter = ItemStack.EMPTY;
		slotPositioning = slot;
		showCount = false;
		callback = stack -> {
		};
		isActive = () -> true;
		textShift = Vec3d.ZERO;
		count = 0;
		ticksUntilScrollPacket = -1;
		showCountPredicate = () -> showCount;
		recipeFilter = false;
	}

	@Override
	public void write(CompoundNBT nbt, boolean clientPacket) {
		nbt.put("Filter", getFilter().serializeNBT());
		nbt.putInt("FilterAmount", count);

		if (clientPacket && forceClientState) {
			nbt.putBoolean("ForceScrollable", true);
			forceClientState = false;
		}
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundNBT nbt, boolean clientPacket) {
		filter = ItemStack.read(nbt.getCompound("Filter"));
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

		if (!getWorld().isRemote)
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

	public FilteringBehaviour moveText(Vec3d shift) {
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
		filter = stack.copy();
		callback.accept(filter);
		count = (filter.getItem() instanceof FilterItem) ? 0 : Math.min(stack.getCount(), stack.getMaxStackSize());
		forceClientState = true;

		tileEntity.markDirty();
		tileEntity.sendData();
	}

	@Override
	public void destroy() {
		if (filter.getItem() instanceof FilterItem) {
			Vec3d pos = VecHelper.getCenterOf(getPos());
			World world = getWorld();
			world.addEntity(new ItemEntity(world, pos.x, pos.y, pos.z, filter.copy()));
		}

		super.destroy();
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
		return !isActive() || filter.isEmpty() || FilterItem.test(tileEntity.getWorld(), stack, filter);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public boolean testHit(Vec3d hit) {
		BlockState state = tileEntity.getBlockState();
		Vec3d localHit = hit.subtract(new Vec3d(tileEntity.getPos()));
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
