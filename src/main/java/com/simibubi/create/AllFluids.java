package com.simibubi.create;

import javax.annotation.Nullable;

import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid;
import com.simibubi.create.content.palettes.AllPaletteStoneTypes;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.builders.FluidBuilder.RenderHandlerFactory;
import com.tterrag.registrate.fabric.EnvExecutor;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.util.entry.FluidEntry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.List;

import static net.minecraft.world.item.Items.BUCKET;
import static net.minecraft.world.item.Items.GLASS_BOTTLE;
import static net.minecraft.world.item.Items.HONEY_BOTTLE;

@SuppressWarnings("UnstableApiUsage")
public class AllFluids {

	private static final CreateRegistrate REGISTRATE = Create.registrate();

	public static final FluidEntry<PotionFluid> POTION =
			REGISTRATE.virtualFluid("potion", /*PotionFluidAttributes::new, */PotionFluid::new)
					.lang(f -> "fluid.create.potion", "Potion")
					.onRegister(potion -> {
						Fluid still = potion.getSource();
						Fluid flowing = potion.getFlowing();
						PotionFluidVariantAttributeHandler handler = new PotionFluidVariantAttributeHandler();
						FluidVariantAttributes.register(still, handler);
						FluidVariantAttributes.register(flowing, handler);
						// evil. why do we need this like this only here.
						EnvExecutor.runWhenOn(EnvType.CLIENT, () -> new Runnable() {
							@Override
							@Environment(EnvType.CLIENT)
							public void run() {
								PotionFluidVariantRenderHandler handler = new PotionFluidVariantRenderHandler();
								FluidVariantRendering.register(still, handler);
								FluidVariantRendering.register(flowing, handler);
							}
						});
					})
					.register();

	public static final FluidEntry<VirtualFluid> TEA = REGISTRATE.virtualFluid("tea")
			.lang(f -> "fluid.create.tea", "Builder's Tea")
			.tag(AllTags.forgeFluidTag("tea"))
			.onRegisterAfter(Item.class, tea -> {
				Fluid still = tea.getSource();
				Fluid flowing = tea.getFlowing();
				FluidStorage.combinedItemApiProvider(AllItems.BUILDERS_TEA.get()).register(context ->
						new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(still), FluidConstants.BOTTLE));
				FluidStorage.combinedItemApiProvider(GLASS_BOTTLE).register(context ->
						new EmptyItemFluidStorage(context, bottle -> ItemVariant.of(AllItems.BUILDERS_TEA.get()), still, FluidConstants.BOTTLE));

				FluidVariantAttributes.register(still, new TeaFluidVariantAttributeHandler());
				FluidVariantAttributes.register(flowing, new TeaFluidVariantAttributeHandler());
			})
			.register();

	public static final FluidEntry<SimpleFlowableFluid.Flowing> HONEY =
			REGISTRATE.standardFluid("honey"/*, NoColorFluidAttributes::new*/)
					.lang(f -> "fluid.create.honey", "Honey")
//					.attributes(b -> b.viscosity(2000)
//							.density(1400))
					.properties(p -> p.levelDecreasePerBlock(2)
							.tickRate(25)
							.flowSpeed(3)
							.blastResistance(100f))
					.tag(AllFluidTags.HONEY.tag)
					.source(SimpleFlowableFluid.Still::new) // TODO: remove when Registrate fixes FluidBuilder
					.bucket()
					.tag(AllTags.forgeItemTag("buckets/honey"))
					.build()
					.renderHandler(() -> SimpleFluidRenderHandler::new)
					.onRegisterAfter(Item.class, honey -> {
						Fluid source = honey.getSource();
						FluidStorage.combinedItemApiProvider(HONEY_BOTTLE).register(context ->
								new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(source), FluidConstants.BOTTLE));
						FluidStorage.combinedItemApiProvider(GLASS_BOTTLE).register(context ->
								new EmptyItemFluidStorage(context, bottle -> ItemVariant.of(HONEY_BOTTLE), source, FluidConstants.BOTTLE));
						FluidStorage.combinedItemApiProvider(source.getBucket()).register(context ->
								new FullItemFluidStorage(context, bucket -> ItemVariant.of(BUCKET), FluidVariant.of(source), FluidConstants.BUCKET));
						FluidStorage.combinedItemApiProvider(BUCKET).register(context ->
								new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(source.getBucket()), source, FluidConstants.BUCKET));
					})
					.register();

	public static final FluidEntry<SimpleFlowableFluid.Flowing> CHOCOLATE =
			REGISTRATE.standardFluid("chocolate"/*, NoColorFluidAttributes::new*/)
					.lang(f -> "fluid.create.chocolate", "Chocolate")
					.tag(AllTags.forgeFluidTag("chocolate"))
//					.attributes(b -> b.viscosity(1500)
//							.density(1400))
					.properties(p -> p.levelDecreasePerBlock(2)
							.tickRate(25)
							.flowSpeed(3)
							.blastResistance(100f))
					.renderHandler(() -> SimpleFluidRenderHandler::new)
					.onRegisterAfter(Item.class, chocolate -> {
						Fluid source = chocolate.getSource();
						// transfer values
						FluidStorage.combinedItemApiProvider(source.getBucket()).register(context ->
								new FullItemFluidStorage(context, bucket -> ItemVariant.of(BUCKET), FluidVariant.of(source), FluidConstants.BUCKET));
						FluidStorage.combinedItemApiProvider(BUCKET).register(context ->
								new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(source.getBucket()), source, FluidConstants.BUCKET));
					})
					.register();

	// Load this class

	public static void register() {}

	@Environment(EnvType.CLIENT)
	public static void assignRenderLayers() {}

	@Environment(EnvType.CLIENT)
	private static void makeTranslucent(FluidEntry<?> entry) {
//		SimpleFlowableFluid fluid = entry.get();
//		ItemBlockRenderTypes.setRenderLayer(fluid, RenderType.translucent());
//		ItemBlockRenderTypes.setRenderLayer(fluid.getSource(), RenderType.translucent());

		// fabric
		BlockRenderLayerMap.INSTANCE.putFluid(entry.get(), RenderType.translucent());
		BlockRenderLayerMap.INSTANCE.putFluid(entry.get().getSource(), RenderType.translucent());
	}

	@Nullable
	public static BlockState getLavaInteraction(FluidState fluidState) {
		Fluid fluid = fluidState.getType();
		if (fluid.isSame(HONEY.get()))
			return AllPaletteStoneTypes.LIMESTONE.getBaseBlock()
				.get()
				.defaultBlockState();
		if (fluid.isSame(CHOCOLATE.get()))
			return AllPaletteStoneTypes.SCORIA.getBaseBlock()
				.get()
				.defaultBlockState();
		return null;
	}

//	/**
//	 * Removing alpha from tint prevents optifine from forcibly applying biome
//	 * colors to modded fluids (Makes translucent fluids disappear)
//	 */
//	private static class NoColorFluidAttributes extends FluidAttributes {
//
//		protected NoColorFluidAttributes(Builder builder, Fluid fluid) {
//			super(builder, fluid);
//		}
//
//		@Override
//		public int getColor(BlockAndTintGetter world, BlockPos pos) {
//			return 0x00ffffff;
//		}
//
//	}

	@Environment(EnvType.CLIENT)
	private static class PotionFluidVariantRenderHandler implements FluidVariantRenderHandler {
		@Override
		public int getColor(FluidVariant fluidVariant, @Nullable BlockAndTintGetter view, @Nullable BlockPos pos) {
			return PotionUtils.getColor(PotionUtils.getAllEffects(fluidVariant.getNbt())) | 0xff000000;
		}
	}

	private static class PotionFluidVariantAttributeHandler implements FluidVariantAttributeHandler {
		@Override
		public Component getName(FluidVariant fluidVariant) {
			List<MobEffectInstance> list = PotionUtils.getAllEffects(fluidVariant.getNbt());
			for (MobEffectInstance effect : list) {
				return new TranslatableComponent(effect.getDescriptionId());
			}
			return FluidVariantAttributeHandler.super.getName(fluidVariant);
		}
	}

	private static class TeaFluidVariantAttributeHandler implements FluidVariantAttributeHandler {
		@Override
		public Component getName(FluidVariant fluidVariant) {
			return new TranslatableComponent("fluid.create.tea");
		}
	}
}
