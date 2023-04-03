package com.simibubi.create.foundation.data;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.AllRegistries;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.trains.BogeyRenderer;
import com.simibubi.create.content.logistics.trains.AbstractBogeyBlock;
import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;

import com.tterrag.registrate.AbstractRegistrate;

import com.tterrag.registrate.builders.AbstractBuilder;

import com.tterrag.registrate.builders.BuilderCallback;

import com.tterrag.registrate.util.entry.BlockEntry;

import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.core.particles.ParticleType;

import net.minecraft.nbt.CompoundTag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class BogeyStyleBuilder<T extends BogeyStyle, P> extends AbstractBuilder<BogeyStyle, T, P, BogeyStyleBuilder<T, P>> {
	private final T style;
	private NonNullSupplier<BogeyRenderer> renderer;
	private Supplier<ResourceLocation> soundType;
	private Supplier<CompoundTag> data;
	private Supplier<ParticleType<?>> particles;

	public static <T extends BogeyStyle, P> BogeyStyleBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, T style) {
		return new BogeyStyleBuilder<>(owner, parent, name, callback, style);
	}

	protected BogeyStyleBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, T style) {
		super(owner, parent, name, callback, AllRegistries.Keys.BOGEYS);
		this.style = style;
		this.soundType = AllSoundEvents.TRAIN2::getId;
		this.particles = AllParticleTypes.AIR_FLOW::get;
		this.data = CompoundTag::new;
	}

	public BogeyStyleBuilder<T, P> defaultData(CompoundTag data) {
		this.data = () -> data;
		return this;
	}

	public BogeyStyleBuilder<T, P> particles(ParticleType<?> particleType) {
		this.particles = () -> particleType;
		return this;
	}

	public BogeyStyleBuilder<T, P> soundType(ResourceLocation soundEntry) {
		this.soundType = () -> soundEntry;
		return this;
	}

	public BogeyStyleBuilder<T, P> block(BogeyRenderer.BogeySize size, BlockEntry<? extends AbstractBogeyBlock> block) {
		return this.block(size, block.getId());
	}

	public BogeyStyleBuilder<T, P> block(BogeyRenderer.BogeySize size, ResourceLocation location) {
		this.style.addBlockForSize(size, location);
		return this;
	}

	public BogeyStyleBuilder<T, P> renderer(BogeyRenderer renderer) {
		this.renderer = () -> renderer;
		return this;
	}

	@Override
	protected @NotNull T createEntry() {
		style.defaultData = data.get();
		style.renderer = renderer.get();
		style.soundType = soundType.get();
		return style;
	}
}
