package com.simibubi.create.content.logistics.trains.entity;

import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.trains.BogeyRenderer;
import com.simibubi.create.content.logistics.trains.AbstractBogeyBlock;

import com.simibubi.create.content.logistics.trains.BogeySizes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;


public final class BogeyStyle extends ForgeRegistryEntry<BogeyStyle> implements IForgeRegistryEntry<BogeyStyle> {
	public Map<BogeySizes.BogeySize, ResourceLocation> blocks = new HashMap<>();
	public Component displayName;
	public ResourceLocation soundType;
	public CompoundTag defaultData;
	public BogeyRenderer renderer;

	public <T extends AbstractBogeyBlock> void addBlockForSize(BogeySizes.BogeySize size, T block) {
		this.addBlockForSize(size, block.getRegistryName());
	}

	public void addBlockForSize(BogeySizes.BogeySize size, ResourceLocation location) {
		blocks.put(size, location);
	}

	public Block getNextBlock(BogeySizes.BogeySize currentSize) {
		return Stream.iterate(currentSize.increment(), BogeySizes.BogeySize::increment)
				.filter(size -> blocks.containsKey(size))
				.findFirst()
				.map(size -> ForgeRegistries.BLOCKS.getValue(blocks.get(size)))
				.orElse(ForgeRegistries.BLOCKS.getValue(blocks.get(currentSize)));
	}

	public Block getBlockOfSize(BogeySizes.BogeySize size) {
		return ForgeRegistries.BLOCKS.getValue(blocks.get(size));
	}

	public Set<BogeySizes.BogeySize> validSizes() {
		return blocks.keySet();
	}

	@NotNull
	public SoundEvent getSoundType() {
		AllSoundEvents.SoundEntry entry = AllSoundEvents.ALL.get(this.soundType);
		if (entry == null || entry.getMainEvent() == null) entry = AllSoundEvents.TRAIN2;
		return entry.getMainEvent();
	}

	public BogeyInstance createInstance(CarriageBogey bogey, BogeySizes.BogeySize size, MaterialManager materialManager) {
		return new BogeyInstance(bogey, this.renderer.newInstance(), size, materialManager);
	}
}
