package com.simibubi.create.foundation.tileEntity.behaviour.filtering;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SidedFilteringBehaviour extends FilteringBehaviour {

	Map<Direction, FilteringBehaviour> sidedFilters;
	private BiFunction<Direction, FilteringBehaviour, FilteringBehaviour> filterFactory;
	private Predicate<Direction> validDirections;

	public SidedFilteringBehaviour(SmartTileEntity te, ValueBoxTransform.Sided sidedSlot,
		BiFunction<Direction, FilteringBehaviour, FilteringBehaviour> filterFactory,
		Predicate<Direction> validDirections) {
		super(te, sidedSlot);
		this.filterFactory = filterFactory;
		this.validDirections = validDirections;
		sidedFilters = new IdentityHashMap<>();
		updateFilterPresence();
	}

	@Override
	public void initialize() {
		super.initialize();
	}

	public FilteringBehaviour get(Direction side) {
		return sidedFilters.get(side);
	}

	public void updateFilterPresence() {
		Set<Direction> valid = new HashSet<>();
		for (Direction d : Iterate.directions)
			if (validDirections.test(d))
				valid.add(d);
		for (Direction d : Iterate.directions)
			if (valid.contains(d)) {
				if (!sidedFilters.containsKey(d))
					sidedFilters.put(d, filterFactory.apply(d, new FilteringBehaviour(tileEntity, slotPositioning)));
			} else if (sidedFilters.containsKey(d))
				removeFilter(d);
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		nbt.put("Filters", NBTHelper.writeCompoundList(sidedFilters.entrySet(), entry -> {
			CompoundTag compound = new CompoundTag();
			compound.putInt("Side", entry.getKey()
				.get3DDataValue());
			entry.getValue()
				.write(compound, clientPacket);
			return compound;
		}));
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		NBTHelper.iterateCompoundList(nbt.getList("Filters", Tag.TAG_COMPOUND), compound -> {
			Direction face = Direction.from3DDataValue(compound.getInt("Side"));
			if (sidedFilters.containsKey(face))
				sidedFilters.get(face)
					.read(compound, clientPacket);
		});
		super.read(nbt, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();
		sidedFilters.values()
			.forEach(FilteringBehaviour::tick);
	}

	@Override
	public void setFilter(Direction side, ItemStack stack) {
		if (!sidedFilters.containsKey(side))
			return;
		sidedFilters.get(side)
			.setFilter(stack);
	}

	@Override
	public ItemStack getFilter(Direction side) {
		if (!sidedFilters.containsKey(side))
			return ItemStack.EMPTY;
		return sidedFilters.get(side)
			.getFilter();
	}

	public boolean test(Direction side, ItemStack stack) {
		if (!sidedFilters.containsKey(side))
			return true;
		return sidedFilters.get(side)
				.test(stack);
	}

	@Override
	public void destroy() {
		sidedFilters.values()
				.forEach(FilteringBehaviour::destroy);
		super.destroy();
	}

	@Override
	public ItemRequirement getRequiredItems() {
		return sidedFilters.values().stream().reduce(
				ItemRequirement.NONE,
				(a, b) -> a.union(b.getRequiredItems()),
				(a, b) -> a.union(b)
		);
	}

	public void removeFilter(Direction side) {
		if (!sidedFilters.containsKey(side))
			return;
		sidedFilters.remove(side)
				.destroy();
	}

	public boolean testHit(Direction direction, Vec3 hit) {
		ValueBoxTransform.Sided sidedPositioning = (Sided) slotPositioning;
		BlockState state = tileEntity.getBlockState();
		Vec3 localHit = hit.subtract(Vec3.atLowerCornerOf(tileEntity.getBlockPos()));
		return sidedPositioning.fromSide(direction)
			.testHit(state, localHit);
	}

}
