package com.simibubi.create;

import com.simibubi.create.content.logistics.trains.AbstractBogeyBlock;
import com.simibubi.create.content.logistics.trains.BogeyRenderer;
import com.simibubi.create.content.logistics.trains.BogeyRenderer.CommonRenderer;
import com.simibubi.create.content.logistics.trains.BogeySizes;
import com.simibubi.create.content.logistics.trains.StandardBogeyRenderer.*;
import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;

import com.simibubi.create.foundation.utility.Lang;

import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.simibubi.create.Create.LOGGER;

public class AllBogeyStyles {
	public static final Map<ResourceLocation, BogeyStyle> BOGEY_STYLES = new HashMap<>();

	public static BogeyStyle STANDARD = create("standard")
			.commonRenderer(CommonStandardBogeyRenderer::new)
			.size(BogeySizes.SMALL, SmallStandardBogeyRenderer::new, AllBlocks.SMALL_BOGEY)
			.size(BogeySizes.LARGE, LargeStandardBogeyRenderer::new, AllBlocks.LARGE_BOGEY)
			.build();

	public static BogeyStyleBuilder create(String name) {
		return create(Create.asResource(name));
	}

	public static BogeyStyleBuilder create(ResourceLocation name) {
		return new BogeyStyleBuilder(name);
	}

	public static void register() {
		LOGGER.info("Registered bogey styles from " + Create.ID);
	}

	public static class BogeyStyleBuilder {
		protected final Map<BogeySizes.BogeySize, BogeyStyle.SizeData> sizes = new HashMap<>();
		protected final ResourceLocation name;

		protected Component displayName = Lang.translateDirect("create.bogeys.invalid");
		protected ResourceLocation soundType = AllSoundEvents.TRAIN2.getId();
		protected CompoundTag defaultData = new CompoundTag();
		protected Optional<CommonRenderer> commonRenderer = Optional.empty();

		public BogeyStyleBuilder(ResourceLocation name) {
			this.name = name;
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

		public BogeyStyleBuilder size(BogeySizes.BogeySize size, Supplier<? extends BogeyRenderer> renderer,
									   BlockEntry<? extends AbstractBogeyBlock> blockEntry) {
			this.size(size, renderer, blockEntry.getId());
			return this;
		}

		public BogeyStyleBuilder size(BogeySizes.BogeySize size, Supplier<? extends BogeyRenderer> renderer,
									   ResourceLocation location) {
			this.sizes.put(size, new BogeyStyle.SizeData(location, renderer.get()));
			return this;
		}

		public BogeyStyleBuilder commonRenderer(Supplier<? extends CommonRenderer> commonRenderer) {
			this.commonRenderer = Optional.of(commonRenderer.get());
			return this;
		}

		public BogeyStyle build() {
			BogeyStyle entry =
					new BogeyStyle(name, displayName, soundType, defaultData, sizes, commonRenderer);
			BOGEY_STYLES.put(name, entry);
			return entry;
		}
	}
}
