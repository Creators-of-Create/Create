package com.simibubi.create.content.contraptions.processing.fan.custom;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * use a single string to represent block state
 * format: "[block id]&[property name]=[value name]&..."
 * use a single string because property values are hard to encode back to string
 */
public class BlockStatePredicate {

	public static class PropertyPair<T extends Comparable<T>> {

		public final Property<T> property;
		public final T value;

		@SuppressWarnings({"unchecked", "unsafe"})
		public PropertyPair(BlockState state, String[] equation) {
			for (Property<?> property : state.getProperties()) {
				if (property.getName().equals(equation[0])) {
					this.property = (Property<T>) property;
					this.value = (T) property.getValue(equation[1]).orElse(null);
					if (value == null) {
						throw new IllegalArgumentException("invalid property value " + equation[1]);
					}
					return;
				}
			}
			throw new IllegalArgumentException("invalid property " + equation[0]);
		}

		public BlockState mutate(BlockState state) {
			return state.setValue(property, value);
		}
	}

	public final Block block;
	public final List<PropertyPair<?>> properties = new ArrayList<>();
	private final String str;

	public BlockStatePredicate(String str) {
		this.str = str;
		String[] parts = str.split("\\&");
		ResourceLocation id = new ResourceLocation(parts[0]);
		if (!ForgeRegistries.BLOCKS.containsKey(id))
			throw new IllegalArgumentException("invalid block id for block state " + str);
		block = ForgeRegistries.BLOCKS.getValue(id);
		BlockState state = block.defaultBlockState();
		for (int i = 1; i < parts.length; i++) {
			String[] equation = parts[i].split("=");
			if (equation.length != 2)
				throw new IllegalArgumentException("invalid property " + parts[i] + " for block state " + str);
			properties.add(new PropertyPair<>(state, equation));
		}
	}

	public String toString() {
		return str;
	}

	public BlockState getDisplay() {
		BlockState state = block.defaultBlockState();
		for (PropertyPair<?> pair : properties) {
			state = pair.mutate(state);
		}
		return state;
	}

	public boolean testBlockState(BlockState blockState, ResourceLocation name) {
		if (!blockState.is(block)) return false;
		for (PropertyPair<?> pair : properties) {
			if (!blockState.hasProperty(pair.property)) return false;
			if (blockState.getValue(pair.property) != pair.value) return false;
		}
		return true;
	}

}
