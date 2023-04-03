package com.simibubi.create.content.logistics.trains.entity;

import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.logistics.trains.BogeyRenderer;
import com.simibubi.create.content.logistics.trains.BogeyRenderer.BogeySize;
import com.simibubi.create.content.logistics.trains.AbstractBogeyBlock;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;


public final class BogeyStyle extends ForgeRegistryEntry<BogeyStyle> implements IForgeRegistryEntry<BogeyStyle> {
	public Map<BogeySize, ResourceLocation> blocks = new EnumMap<>(BogeySize.class);
	public Component displayName;
	public SoundEvent soundType;
	public CompoundTag defaultData;
	public BogeyRenderer renderer;

	public <T extends AbstractBogeyBlock> void addBlockForSize(BogeySize size, T block) {
		this.addBlockForSize(size, block.getRegistryName());
	}

	public void addBlockForSize(BogeySize size, ResourceLocation location) {
		blocks.put(size, location);
	}

	public Block getNextBlock(BogeySize currentSize) {
		return Stream.iterate(getNextSize(currentSize), this::getNextSize)
				.filter(size -> blocks.containsKey(size))
				.findFirst()
				.map(size -> ForgeRegistries.BLOCKS.getValue(blocks.get(size)))
				.orElse(ForgeRegistries.BLOCKS.getValue(blocks.get(currentSize)));
	}

	public Block getBlockOfSize(BogeySize size) {
		return ForgeRegistries.BLOCKS.getValue(blocks.get(size));
	}

	public Set<BogeySize> validSizes() {
		return blocks.keySet();
	}

	private BogeySize getNextSize(BogeySize size) {
		BogeySize[] sizes = BogeySize.values();
		int nextOrdinal = (size.ordinal() + 1) % sizes.length;
		return sizes[nextOrdinal];
	}

	public BogeyInstance createInstance(CarriageBogey bogey, BogeySize size, MaterialManager materialManager) {
		return new BogeyInstance(bogey, this.renderer.newInstance(), size, materialManager);
	}
}
