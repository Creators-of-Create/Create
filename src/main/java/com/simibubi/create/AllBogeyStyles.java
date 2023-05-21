package com.simibubi.create;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;
import com.simibubi.create.content.trains.bogey.BogeyRenderer;
import com.simibubi.create.content.trains.bogey.BogeyRenderer.CommonRenderer;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.bogey.BogeyStyle;
import com.simibubi.create.content.trains.bogey.StandardBogeyRenderer.CommonStandardBogeyRenderer;
import com.simibubi.create.content.trains.bogey.StandardBogeyRenderer.LargeStandardBogeyRenderer;
import com.simibubi.create.content.trains.bogey.StandardBogeyRenderer.SmallStandardBogeyRenderer;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class AllBogeyStyles {
	public static final Map<ResourceLocation, BogeyStyle> BOGEY_STYLES = new HashMap<>();
	public static final Map<ResourceLocation, Map<ResourceLocation, BogeyStyle>> CYCLE_GROUPS = new HashMap<>();
	private static final Map<ResourceLocation, BogeyStyle> EMPTY_GROUP = ImmutableMap.of();

	public static Map<ResourceLocation, BogeyStyle> getCycleGroup(ResourceLocation cycleGroup) {
		return CYCLE_GROUPS.getOrDefault(cycleGroup, EMPTY_GROUP);
	}

	public static final String STANDARD_CYCLE_GROUP = "standard";

	public static final BogeyStyle STANDARD =
		create("standard", STANDARD_CYCLE_GROUP).commonRenderer(() -> CommonStandardBogeyRenderer::new)
			.displayName(Components.translatable("create.bogey.style.standard"))
			.size(BogeySizes.SMALL, () -> SmallStandardBogeyRenderer::new, AllBlocks.SMALL_BOGEY)
			.size(BogeySizes.LARGE, () -> LargeStandardBogeyRenderer::new, AllBlocks.LARGE_BOGEY)
			.build();

	private static BogeyStyleBuilder create(String name, String cycleGroup) {
		return create(Create.asResource(name), Create.asResource(cycleGroup));
	}

	public static BogeyStyleBuilder create(ResourceLocation name, ResourceLocation cycleGroup) {
		return new BogeyStyleBuilder(name, cycleGroup);
	}

	public static void register() {}

	public static class BogeyStyleBuilder {
		protected final Map<BogeySizes.BogeySize, Supplier<BogeyStyle.SizeData>> sizes = new HashMap<>();
		protected final ResourceLocation name;
		protected final ResourceLocation cycleGroup;

		protected Component displayName = Lang.translateDirect("bogey.style.invalid");
		protected ResourceLocation soundType = AllSoundEvents.TRAIN2.getId();
		protected CompoundTag defaultData = new CompoundTag();
		protected ParticleOptions contactParticle = ParticleTypes.CRIT;
		protected ParticleOptions smokeParticle = ParticleTypes.POOF;
		protected Optional<Supplier<? extends CommonRenderer>> commonRenderer = Optional.empty();

		public BogeyStyleBuilder(ResourceLocation name, ResourceLocation cycleGroup) {
			this.name = name;
			this.cycleGroup = cycleGroup;
		}

		public BogeyStyleBuilder displayName(Component displayName) {
			this.displayName = displayName;
			return this;
		}

		public BogeyStyleBuilder soundType(ResourceLocation soundType) {
			this.soundType = soundType;
			return this;
		}

		public BogeyStyleBuilder defaultData(CompoundTag defaultData) {
			this.defaultData = defaultData;
			return this;
		}

		public BogeyStyleBuilder size(BogeySizes.BogeySize size, Supplier<Supplier<? extends BogeyRenderer>> renderer,
			BlockEntry<? extends AbstractBogeyBlock<?>> blockEntry) {
			this.size(size, renderer, blockEntry.getId());
			return this;
		}

		public BogeyStyleBuilder size(BogeySizes.BogeySize size, Supplier<Supplier<? extends BogeyRenderer>> renderer,
			ResourceLocation location) {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
				this.sizes.put(size, () -> new BogeyStyle.SizeData(location, renderer.get(), renderer.get()
					.get()));
			});
			return this;
		}

		public BogeyStyleBuilder contactParticle(ParticleOptions contactParticle) {
			this.contactParticle = contactParticle;
			return this;
		}

		public BogeyStyleBuilder smokeParticle(ParticleOptions smokeParticle) {
			this.smokeParticle = smokeParticle;
			return this;
		}

		public BogeyStyleBuilder commonRenderer(Supplier<Supplier<? extends CommonRenderer>> commonRenderer) {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
				this.commonRenderer = Optional.of(commonRenderer.get());
			});
			return this;
		}

		public BogeyStyle build() {
			BogeyStyle entry = new BogeyStyle(name, cycleGroup, displayName, soundType, contactParticle, smokeParticle,
				defaultData, sizes, commonRenderer);
			BOGEY_STYLES.put(name, entry);
			CYCLE_GROUPS.computeIfAbsent(cycleGroup, l -> new HashMap<>())
				.put(name, entry);
			return entry;
		}
	}
}
