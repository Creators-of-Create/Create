package com.simibubi.create.compat.ae2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.StackRequirement;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AppliedEnergistics2InSchematics {

	private static final ResourceLocation CABLE_BUS = Mods.AE2.rl("cable_bus");
	private static final List<String> PART_KEYS =
		List.of("cable", "down", "up", "north", "south", "west", "east");
	private static final List<String> FACADE_KEYS = List.of(
		"facadeDown", "facadeUp", "facadeNorth", "facadeSouth", "facadeWest", "facadeEast"
	);
	private static final List<String> KEYS_TO_RETAIN = new ArrayList<>();

	static {
		KEYS_TO_RETAIN.add("id");
		KEYS_TO_RETAIN.add("hasRedstone");
		KEYS_TO_RETAIN.addAll(PART_KEYS);
		KEYS_TO_RETAIN.addAll(FACADE_KEYS);
	}

	public static boolean isCableBus(BlockState blockState) {
		return Mods.AE2.isLoaded() && BuiltInRegistries.BLOCK.getKey(blockState.getBlock())
			.equals(CABLE_BUS);
	}

	public static CompoundTag prepareBlockEntityData(Level level, BlockState blockState, BlockEntity blockEntity) {
		if (blockEntity == null || !isCableBus(blockState))
			return null;

		CompoundTag data = blockEntity.saveWithFullMetadata(level.registryAccess());
		List<String> keysToRemove = new ArrayList<>();
		for (String key : data.getAllKeys())
			if (!KEYS_TO_RETAIN.contains(key))
				keysToRemove.add(key);
		for (String key : keysToRemove)
			data.remove(key);

		return data;
	}

	public static ItemRequirement getRequiredItems(BlockState blockState, BlockEntity blockEntity) {
		if (blockEntity == null || !isCableBus(blockState))
			return ItemRequirement.INVALID;

		List<StackRequirement> list = new ArrayList<>();
		addPartStack(blockEntity, null, list);
		for (Direction direction : Direction.values())
			addPartStack(blockEntity, direction, list);
		addFacadeStacks(blockEntity, list);

		return list.isEmpty() ? ItemRequirement.INVALID : new ItemRequirement(list);
	}

	private static void addPartStack(BlockEntity blockEntity, Direction direction, List<StackRequirement> list) {
		Object part = invoke(blockEntity, "getPart", new Class<?>[] { Direction.class }, new Object[] { direction });
		if (part == null)
			return;

		Object partItem = invoke(part, "getPartItem", new Class<?>[0], new Object[0]);
		if (partItem instanceof ItemLike itemLike)
			addStack(itemLike.asItem(), list);
	}

	private static void addFacadeStacks(BlockEntity blockEntity, List<StackRequirement> list) {
		Object facadeContainer = invoke(blockEntity, "getFacadeContainer", new Class<?>[0], new Object[0]);
		if (facadeContainer == null)
			return;

		for (Direction direction : Direction.values()) {
			Object facade =
				invoke(facadeContainer, "getFacade", new Class<?>[] { Direction.class }, new Object[] { direction });
			if (facade == null)
				continue;

			Object itemStack = invoke(facade, "getItemStack", new Class<?>[0], new Object[0]);
			if (itemStack instanceof ItemStack stack)
				list.add(new StackRequirement(stack, ItemUseType.CONSUME));
		}
	}

	private static void addStack(ItemLike item, List<StackRequirement> list) {
		ItemStack itemStack = new ItemStack(item);
		if (!itemStack.isEmpty())
			list.add(new StackRequirement(itemStack, ItemUseType.CONSUME));
	}

	private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object[] args) {
		try {
			Method method = target.getClass()
				.getMethod(methodName, parameterTypes);
			method.setAccessible(true);
			return method.invoke(target, args);
		} catch (ReflectiveOperationException | RuntimeException e) {
			return null;
		}
	}

}
