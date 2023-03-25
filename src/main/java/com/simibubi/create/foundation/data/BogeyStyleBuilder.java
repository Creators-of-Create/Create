package com.simibubi.create.foundation.data;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRegistries;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.trains.BogeyRenderer;
import com.simibubi.create.content.logistics.trains.IBogeyBlock;
import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;

import com.tterrag.registrate.AbstractRegistrate;

import com.tterrag.registrate.builders.AbstractBuilder;

import com.tterrag.registrate.builders.BuilderCallback;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.core.particles.ParticleType;

import net.minecraft.nbt.CompoundTag;

import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class BogeyStyleBuilder<T extends BogeyStyle, P> extends AbstractBuilder<BogeyStyle, T, P, BogeyStyleBuilder<T, P>> {
	private final T style;

	private NonNullSupplier<Map<BogeyRenderer.BogeySize, BlockEntry<? extends IBogeyBlock>>> bogeyBlocks
			= () -> new EnumMap<>(BogeyRenderer.BogeySize.class);
	private Supplier<AllSoundEvents.SoundEntry> sounds;
	private Supplier<CompoundTag> data;
	private Supplier<ParticleType<?>> particles;

	public static <T extends BogeyStyle, P> BogeyStyleBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, T style) {
		return new BogeyStyleBuilder<>(owner, parent, name, callback, style);
	}

	protected BogeyStyleBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, T style) {
		super(owner, parent, name, callback, AllRegistries.Keys.BOGEYS);
		this.style = style;

//		bogeyBlocks.get().put(BogeyRenderer.BogeySize.SMALL, AllBlocks.SMALL_BOGEY.get());
//		bogeyBlocks.get().put(BogeyRenderer.BogeySize.LARGE, AllBlocks.LARGE_BOGEY.get());
	}

	public BogeyStyleBuilder<T, P> defaultData(CompoundTag data) {
		this.data = () -> data;
		return this;
	}

	public BogeyStyleBuilder<T, P> particles(ParticleType<?> particleType) {
		this.particles = () -> particleType;
		return this;
	}

	public BogeyStyleBuilder<T, P> soundType(AllSoundEvents.SoundEntry soundEntry) {
		this.sounds = () -> soundEntry;
		return this;
	}

	public BogeyStyleBuilder<T, P> block(BogeyRenderer.BogeySize size, BlockEntry<? extends IBogeyBlock> block) {
		this.bogeyBlocks.get().put(size, block);
		return this;
	}

	@Override
	protected @NotNull T createEntry() {
		return style;
	}
}
