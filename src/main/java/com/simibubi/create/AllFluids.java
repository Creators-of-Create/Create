package com.simibubi.create;

import javax.annotation.Nullable;

import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid;
import com.simibubi.create.content.palettes.AllPaletteStoneTypes;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.fabric.EnvExecutor;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.util.entry.FluidEntry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import static net.minecraft.world.item.Items.BUCKET;
import static net.minecraft.world.item.Items.GLASS_BOTTLE;
import static net.minecraft.world.item.Items.HONEY_BOTTLE;

@SuppressWarnings("UnstableApiUsage")
public class AllFluids {

	private static final CreateRegistrate REGISTRATE = Create.registrate();

	public static final FluidEntry<PotionFluid> POTION =
			REGISTRATE.virtualFluid("potion", /*PotionFluidAttributes::new, */PotionFluid::new)
					.lang(f -> "fluid.create.potion", "Potion")
					.register();

	public static final FluidEntry<VirtualFluid> TEA = REGISTRATE.virtualFluid("tea")
			.lang(f -> "fluid.create.tea", "Builder's Tea")
			.tag(AllTags.forgeFluidTag("tea"))
			.onRegister(tea -> {
				FluidStorage.combinedItemApiProvider(AllItems.BUILDERS_TEA.get()).register(context ->
						new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(tea), FluidConstants.BUCKET / 10));
				FluidStorage.combinedItemApiProvider(GLASS_BOTTLE).register(context ->
						new EmptyItemFluidStorage(context, bottle -> ItemVariant.of(AllItems.BUILDERS_TEA.get()), tea, FluidConstants.BUCKET / 10));

				EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () ->
						FluidVariantRendering.register(tea, new FluidVariantRenderHandler() {
							@Override
							public Component getName(FluidVariant fluidVariant) {
								return new TranslatableComponent("fluid.create.tea");
							}
						}));
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
					.onRegister(honey -> {
						FluidStorage.combinedItemApiProvider(HONEY_BOTTLE).register(context ->
								new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(honey), FluidConstants.BOTTLE));
						FluidStorage.combinedItemApiProvider(GLASS_BOTTLE).register(context ->
								new EmptyItemFluidStorage(context, bottle -> ItemVariant.of(HONEY_BOTTLE), honey, FluidConstants.BOTTLE));
						FluidStorage.combinedItemApiProvider(honey.getBucket()).register(context ->
								new FullItemFluidStorage(context, bucket -> ItemVariant.of(BUCKET), FluidVariant.of(honey), FluidConstants.BUCKET));
						FluidStorage.combinedItemApiProvider(BUCKET).register(context ->
								new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(honey.getBucket()), honey, FluidConstants.BUCKET));
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
					.onRegister(chocolate -> {
						FluidStorage.combinedItemApiProvider(chocolate.getBucket()).register(context ->
								new FullItemFluidStorage(context, bucket -> ItemVariant.of(BUCKET), FluidVariant.of(chocolate), FluidConstants.BUCKET));
						FluidStorage.combinedItemApiProvider(BUCKET).register(context ->
								new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(chocolate.getBucket()), chocolate, FluidConstants.BUCKET));
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

}
