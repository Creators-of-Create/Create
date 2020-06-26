package com.simibubi.create.foundation.tileEntity.behaviour.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class InventoryManagementBehaviour extends TileEntityBehaviour {

	Map<Pair<BlockPos, Direction>, LazyOptional<IItemHandler>> inventories;
	private Supplier<List<Pair<BlockPos, Direction>>> attachments;
	private List<IItemHandler> activeHandlers;

	public static BehaviourType<InventoryManagementBehaviour> TYPE = new BehaviourType<>();

	public InventoryManagementBehaviour(SmartTileEntity te, Supplier<List<Pair<BlockPos, Direction>>> attachments) {
		super(te);
		this.attachments = attachments;
		setLazyTickRate(20);
		activeHandlers = new ArrayList<>();
		inventories = new HashMap<>();
	}

	@Override
	public void initialize() {
		super.initialize();
		attachments.get().forEach(offset -> inventories.put(offset, findInventory(offset)));
		lazyTick();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		activeHandlers.clear();
		for (Pair<BlockPos, Direction> pair : inventories.keySet()) {
			LazyOptional<IItemHandler> lazyOptional = inventories.get(pair);
			if (lazyOptional.isPresent()) {
				activeHandlers.add(lazyOptional.orElse(null));
				continue;
			}

			lazyOptional = findInventory(pair);
			if (lazyOptional.isPresent())
				activeHandlers.add(lazyOptional.orElse(null));
			inventories.put(pair, lazyOptional);
		}
	}

	public List<IItemHandler> getInventories() {
		return activeHandlers;
	}

	public IItemHandler getInventory() {
		if (activeHandlers.isEmpty())
			return null;
		return activeHandlers.get(0);
	}

	protected LazyOptional<IItemHandler> findInventory(Pair<BlockPos, Direction> offset) {
		BlockPos invPos = tileEntity.getPos().add(offset.getKey());
		World world = getWorld();

		if (!world.isBlockPresent(invPos))
			return LazyOptional.empty();
		BlockState invState = world.getBlockState(invPos);

		if (!invState.hasTileEntity())
			return LazyOptional.empty();
		TileEntity invTE = world.getTileEntity(invPos);
		if (invTE == null)
			return LazyOptional.empty();

		LazyOptional<IItemHandler> inventory = invTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
				offset.getValue());
		if (inventory == null) {
			return LazyOptional.empty();
		}

		return inventory;
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public static class Attachments {
		public static final Supplier<List<Pair<BlockPos, Direction>>> toward(Supplier<Direction> facing) {
			return () -> ImmutableList
					.of(Pair.of(new BlockPos(facing.get().getDirectionVec()), facing.get().getOpposite()));
		};
	}

}
