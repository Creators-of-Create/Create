package com.simibubi.create.content.trains.bogey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBogeyStyles;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.trains.bogey.BogeySizes.BogeySize;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.foundation.utility.Lang;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class BogeyStyle {
	public final ResourceLocation id;
	public final ResourceLocation cycleGroup;
	public final Component displayName;
	public final Supplier<SoundEvent> soundEvent;
	public final ParticleOptions contactParticle;
	public final ParticleOptions smokeParticle;
	public final CompoundTag defaultData;
	private final Map<BogeySizes.BogeySize, Supplier<? extends AbstractBogeyBlock<?>>> sizes;

	@OnlyIn(Dist.CLIENT)
	private Map<BogeySizes.BogeySize, SizeRenderer> sizeRenderers;

	public BogeyStyle(ResourceLocation id, ResourceLocation cycleGroup, Component displayName,
		Supplier<SoundEvent> soundEvent, ParticleOptions contactParticle, ParticleOptions smokeParticle,
		CompoundTag defaultData, Map<BogeySizes.BogeySize, Supplier<? extends AbstractBogeyBlock<?>>> sizes,
		Map<BogeySizes.BogeySize, Supplier<? extends SizeRenderer>> sizeRenderers) {

		this.id = id;
		this.cycleGroup = cycleGroup;
		this.displayName = displayName;
		this.soundEvent = soundEvent;
		this.contactParticle = contactParticle;
		this.smokeParticle = smokeParticle;
		this.defaultData = defaultData;
		this.sizes = sizes;

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			this.sizeRenderers = new HashMap<>();
			sizeRenderers.forEach((k, v) -> this.sizeRenderers.put(k, v.get()));
		});
	}

	public Map<ResourceLocation, BogeyStyle> getCycleGroup() {
		return AllBogeyStyles.getCycleGroup(cycleGroup);
	}

	public Set<BogeySizes.BogeySize> validSizes() {
		return sizes.keySet();
	}

	public AbstractBogeyBlock<?> getBlockForSize(BogeySizes.BogeySize size) {
		return sizes.get(size).get();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AbstractBogeyBlock<?> getNextBlock(BogeySizes.BogeySize currentSize) {
		return Stream.iterate(currentSize.nextBySize(), BogeySizes.BogeySize::nextBySize)
				.filter(sizes::containsKey)
				.findFirst()
				.map(this::getBlockForSize)
				.orElse((AbstractBogeyBlock) getBlockForSize(currentSize));
	}

	@OnlyIn(Dist.CLIENT)
	public void render(BogeySize size, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay, float wheelAngle, @Nullable CompoundTag bogeyData, boolean inContraption) {
		if (bogeyData == null)
			bogeyData = new CompoundTag();

		poseStack.translate(0, -1.5 - 1 / 128f, 0);

		SizeRenderer renderer = sizeRenderers.get(size);
		if (renderer != null) {
			renderer.renderer.render(bogeyData, wheelAngle, partialTick, poseStack, buffers, light, overlay, inContraption);
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Nullable
	public BogeyVisual createVisual(VisualizationContext ctx, CarriageBogey bogey, float partialTick) {
		SizeRenderer renderer = sizeRenderers.get(bogey.getSize());
		if (renderer != null) {
			return renderer.visualizer.createVisual(ctx, bogey, partialTick);
		}
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	public record SizeRenderer(BogeyRenderer renderer, BogeyVisualizer visualizer) {
	}

	public static class Builder {
		protected final ResourceLocation id;
		protected final ResourceLocation cycleGroup;
		protected final Map<BogeySizes.BogeySize, Supplier<? extends AbstractBogeyBlock<?>>> sizes = new HashMap<>();

		protected Component displayName = Lang.translateDirect("bogey.style.invalid");
		protected Supplier<SoundEvent> soundEvent = AllSoundEvents.TRAIN2::getMainEvent;
		protected ParticleOptions contactParticle = ParticleTypes.CRIT;
		protected ParticleOptions smokeParticle = ParticleTypes.POOF;
		protected CompoundTag defaultData = new CompoundTag();

		protected final Map<BogeySizes.BogeySize, Supplier<? extends SizeRenderer>> sizeRenderers = new HashMap<>();

		public Builder(ResourceLocation id, ResourceLocation cycleGroup) {
			this.id = id;
			this.cycleGroup = cycleGroup;
		}

		public Builder displayName(Component displayName) {
			this.displayName = displayName;
			return this;
		}

		public Builder soundEvent(Supplier<SoundEvent> soundEvent) {
			this.soundEvent = soundEvent;
			return this;
		}

		public Builder contactParticle(ParticleOptions contactParticle) {
			this.contactParticle = contactParticle;
			return this;
		}

		public Builder smokeParticle(ParticleOptions smokeParticle) {
			this.smokeParticle = smokeParticle;
			return this;
		}

		public Builder defaultData(CompoundTag defaultData) {
			this.defaultData = defaultData;
			return this;
		}

		public Builder size(BogeySizes.BogeySize size, Supplier<? extends AbstractBogeyBlock<?>> block,
			Supplier<? extends SizeRenderer> renderer) {
			this.sizes.put(size, block);
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
				this.sizeRenderers.put(size, renderer);
			});
			return this;
		}

		public BogeyStyle build() {
			BogeyStyle entry = new BogeyStyle(id, cycleGroup, displayName, soundEvent, contactParticle, smokeParticle,
				defaultData, sizes, sizeRenderers);
			AllBogeyStyles.BOGEY_STYLES.put(id, entry);
			AllBogeyStyles.CYCLE_GROUPS.computeIfAbsent(cycleGroup, l -> new HashMap<>())
				.put(id, entry);
			return entry;
		}
	}
}
