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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class FilteringBehaviour extends TileEntityBehaviour {

	public static BehaviourType<FilteringBehaviour> TYPE = new BehaviourType<>();

	ValueBoxTransform slotPositioning;
	boolean showCount;
	Vector3d textShift;

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
		textShift = Vector3d.ZERO;
		count = 0;
		ticksUntilScrollPacket = -1;
		showCountPredicate = () -> showCount;
		recipeFilter = false;
		fluidFilter = false;
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

	public FilteringBehaviour moveText(Vector3d shift) {
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
			Vector3d pos = VecHelper.getCenterOf(getPos());
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
	
	public boolean test(FluidStack stack) {
		return !isActive() || filter.isEmpty() || FilterItem.test(tileEntity.getWorld(), stack, filter);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public boolean testHit(Vector3d hit) {
		BlockState state = tileEntity.getBlockState();
		Vector3d localHit = hit.subtract(Vector3d.of(tileEntity.getPos()));
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
