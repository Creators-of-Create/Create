package com.simibubi.create.content.logistics.trains.entity;

import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.trains.BogeyRenderer;

import com.simibubi.create.content.logistics.trains.BogeyRenderer.CommonRenderer;
import com.simibubi.create.content.logistics.trains.BogeySizes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;


public class BogeyStyle {
	public final ResourceLocation name;
	private final Optional<CommonRenderer> commonRenderer;
	private final Map<BogeySizes.BogeySize, SizeData> sizes;
	public final Component displayName;
	public final ResourceLocation soundType;
	public final CompoundTag defaultData;

	public BogeyStyle(ResourceLocation name, Component displayName, ResourceLocation soundType, CompoundTag defaultData,
					  Map<BogeySizes.BogeySize, SizeData> sizes, Optional<CommonRenderer> commonRenderer) {
		this.name = name;
		this.displayName = displayName;
		this.soundType = soundType;
		this.defaultData = defaultData;

		this.sizes = sizes;
		this.commonRenderer = commonRenderer;
	}

	public Block getNextBlock(BogeySizes.BogeySize currentSize) {
		return Stream.iterate(currentSize.increment(), BogeySizes.BogeySize::increment)
				.filter(sizes::containsKey)
				.findFirst()
				.map(size -> ForgeRegistries.BLOCKS.getValue(sizes.get(size).block()))
				.orElse(ForgeRegistries.BLOCKS.getValue(sizes.get(currentSize).block()));
	}

	public Block getBlockOfSize(BogeySizes.BogeySize size) {
		return ForgeRegistries.BLOCKS.getValue(sizes.get(size).block());
	}

	public Set<BogeySizes.BogeySize> validSizes() {
		return sizes.keySet();
	}

	@NotNull
	public SoundEvent getSoundType() {
		AllSoundEvents.SoundEntry entry = AllSoundEvents.ALL.get(this.soundType);
		if (entry == null || entry.getMainEvent() == null) entry = AllSoundEvents.TRAIN2;
		return entry.getMainEvent();
	}

	public BogeyRenderer createRendererInstance(BogeySizes.BogeySize size) {
		return this.getInWorldRenderInstance(size).createNewInstance();
	}

	public BogeyRenderer getInWorldRenderInstance(BogeySizes.BogeySize size) {
		return this.sizes.get(size).renderer();
	}

	public Optional<CommonRenderer> getInWorldCommonRenderInstance() {
		return this.commonRenderer;
	}

	public Optional<CommonRenderer> getNewCommonRenderInstance() {
		return this.getInWorldCommonRenderInstance().map(CommonRenderer::createNewInstance);
	}

	public BogeyInstance createInstance(CarriageBogey bogey, BogeySizes.BogeySize size, MaterialManager materialManager) {
		return new BogeyInstance(bogey, this, size, materialManager);
	}

	public record SizeData(ResourceLocation block, BogeyRenderer renderer) {
	}
}
