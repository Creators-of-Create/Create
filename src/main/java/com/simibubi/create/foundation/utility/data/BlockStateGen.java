
package com.simibubi.create.foundation.utility.data;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.world.OxidizingBlock;
import com.simibubi.create.modules.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.LinearChassisBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.RadialChassisBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.mounted.CartAssemblerBlock;
import com.simibubi.create.modules.logistics.block.belts.observer.BeltObserverBlock;
import com.simibubi.create.modules.palettes.PavedBlock;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;

public class BlockStateGen {

	// Functions

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> axisBlockProvider(
		boolean customItem) {
		return (c, p) -> axisBlock(c, p, getBlockModel(customItem, c, p));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> directionalBlockProvider(
		boolean customItem) {
		return (c, p) -> p.directionalBlock(c.get(), getBlockModel(customItem, c, p));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalBlockProvider(
		boolean customItem) {
		return (c, p) -> p.horizontalBlock(c.get(), getBlockModel(customItem, c, p));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalAxisBlockProvider(
		boolean customItem) {
		return (c, p) -> horizontalAxisBlock(c, p, getBlockModel(customItem, c, p));
	}

	public static <T extends DirectionalAxisKineticBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> directionalAxisBlockProvider() {
		return (c, p) -> directionalAxisBlock(c, p, ($, vertical) -> p.models()
			.getExistingFile(p.modLoc("block/" + c.getName() + "/" + (vertical ? "vertical" : "horizontal"))));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalWheelProvider(
		boolean customItem) {
		return (c, p) -> horizontalWheel(c, p, getBlockModel(customItem, c, p));
	}

	public static <P> NonNullUnaryOperator<BlockBuilder<OxidizingBlock, P>> oxidizedBlockstate() {
		return b -> b.blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry())
			.forAllStates(state -> {
				String name = AssetLookup.getOxidizedModel(ctx.getName(), state.get(OxidizingBlock.OXIDIZATION));
				return ConfiguredModel.builder()
					.modelFile(prov.models()
						.cubeAll(name, prov.modLoc(name)))
					.build();
			}));
	}

	// Utility

	private static <T extends Block> Function<BlockState, ModelFile> getBlockModel(boolean customItem,
		DataGenContext<Block, T> c, RegistrateBlockstateProvider p) {
		return $ -> customItem ? AssetLookup.partialBaseModel(c, p) : AssetLookup.standardModel(c, p);
	}

	// Generators

	public static <T extends Block> void axisBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		Function<BlockState, ModelFile> modelFunc) {
		prov.getVariantBuilder(ctx.getEntry())
			.forAllStates(state -> {
				Axis axis = state.get(BlockStateProperties.AXIS);
				return ConfiguredModel.builder()
					.modelFile(modelFunc.apply(state))
					.rotationX(axis == Axis.Y ? 0 : 90)
					.rotationY(axis == Axis.X ? 90 : 0)
					.build();
			});
	}

	public static <T extends Block> void horizontalAxisBlock(DataGenContext<Block, T> ctx,
		RegistrateBlockstateProvider prov, Function<BlockState, ModelFile> modelFunc) {
		prov.getVariantBuilder(ctx.getEntry())
			.forAllStates(state -> {
				Axis axis = state.get(BlockStateProperties.HORIZONTAL_AXIS);
				return ConfiguredModel.builder()
					.modelFile(modelFunc.apply(state))
					.rotationY(axis == Axis.X ? 90 : 0)
					.build();
			});
	}

	public static <T extends DirectionalAxisKineticBlock> void directionalAxisBlock(DataGenContext<Block, T> ctx,
		RegistrateBlockstateProvider prov, BiFunction<BlockState, Boolean, ModelFile> modelFunc) {
		prov.getVariantBuilder(ctx.getEntry())
			.forAllStates(state -> {

				boolean alongFirst = state.get(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
				Direction direction = state.get(DirectionalAxisKineticBlock.FACING);
				boolean vertical = direction.getAxis()
					.isHorizontal() && (direction.getAxis() == Axis.X) == alongFirst;
				int xRot = direction == Direction.DOWN ? 270 : direction == Direction.UP ? 90 : 0;
				int yRot = direction.getAxis()
					.isVertical() ? alongFirst ? 0 : 90 : (int) direction.getHorizontalAngle();

				return ConfiguredModel.builder()
					.modelFile(modelFunc.apply(state, vertical))
					.rotationX(xRot)
					.rotationY(yRot)
					.build();
			});
	}

	public static <T extends Block> void horizontalWheel(DataGenContext<Block, T> ctx,
		RegistrateBlockstateProvider prov, Function<BlockState, ModelFile> modelFunc) {
		prov.getVariantBuilder(ctx.get())
			.forAllStates(state -> ConfiguredModel.builder()
				.modelFile(modelFunc.apply(state))
				.rotationX(90)
				.rotationY(((int) state.get(BlockStateProperties.HORIZONTAL_FACING)
					.getHorizontalAngle() + 180) % 360)
				.build());
	}

	public static <T extends Block> void cubeAll(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		String textureSubDir) {
		cubeAll(ctx, prov, textureSubDir, ctx.getName());
	}

	public static <T extends Block> void cubeAll(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		String textureSubDir, String name) {
		String texturePath = "block/" + textureSubDir + name;
		prov.simpleBlock(ctx.get(), prov.models()
			.cubeAll(ctx.getName(), prov.modLoc(texturePath)));
	}

	public static <T extends Block> void pavedBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		ModelFile top, ModelFile covered) {
		prov.getVariantBuilder(ctx.getEntry())
			.forAllStates(state -> ConfiguredModel.builder()
				.modelFile(state.get(PavedBlock.COVERED) ? covered : top)
				.build());
	}

	public static NonNullBiConsumer<DataGenContext<Block, CartAssemblerBlock>, RegistrateBlockstateProvider> cartAssembler() {
		return (c, p) -> p.getVariantBuilder(c.get())
			.forAllStates(state -> {
				return ConfiguredModel.builder()
					.modelFile(p.models()
						.getExistingFile(p.modLoc("block/" + c.getName() + "/block"
							+ (state.get(CartAssemblerBlock.POWERED) ? "_powered" : ""))))
					.rotationY(state.get(CartAssemblerBlock.RAIL_SHAPE) == RailShape.EAST_WEST ? 90 : 0)
					.build();
			});
	}

	public static NonNullBiConsumer<DataGenContext<Block, BeltObserverBlock>, RegistrateBlockstateProvider> beltObserver() {
		return (c, p) -> {

			Map<BeltObserverBlock.Mode, Map<String, ModelFile>> models = new IdentityHashMap<>();
			Map<String, ResourceLocation> baseModels = new HashMap<>();

			for (boolean powered : Iterate.trueAndFalse) {
				for (boolean belt : Iterate.trueAndFalse) {
					String suffix = (belt ? "_belt" : "") + (powered ? "_powered" : "");
					baseModels.put(suffix, p.modLoc("block/belt_observer/base" + suffix));
				}
			}

			for (BeltObserverBlock.Mode mode : BeltObserverBlock.Mode.values()) {
				String modeName = mode.getName();
				HashMap<String, ModelFile> map = new HashMap<>();
				for (boolean powered : Iterate.trueAndFalse) {
					for (boolean belt : Iterate.trueAndFalse) {
						String suffix = (belt ? "_belt" : "") + (powered ? "_powered" : "");
						map.put(suffix, p.models()
							.withExistingParent("block/belt_observer/" + modeName + suffix, baseModels.get(suffix))
							.texture("texture",
								p.modLoc("block/belt_observer_" + modeName + (powered ? "_powered" : ""))));
					}
				}
				models.put(mode, map);
			}

			p.getVariantBuilder(c.get())
				.forAllStates(state -> {
					String suffix = (state.get(BeltObserverBlock.BELT) ? "_belt" : "")
						+ (state.get(BeltObserverBlock.POWERED) ? "_powered" : "");
					return ConfiguredModel.builder()
						.modelFile(models.get(state.get(BeltObserverBlock.MODE))
							.get(suffix))
						.rotationY((int) state.get(BeltObserverBlock.HORIZONTAL_FACING)
							.getHorizontalAngle())
						.build();
				});
		};
	}

	public static <B extends LinearChassisBlock> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockstateProvider> linearChassis() {
		return (c, p) -> {
			ResourceLocation side = p.modLoc("block/" + c.getName() + "_side");
			ResourceLocation top = p.modLoc("block/translation_chassis_end");
			ResourceLocation top_sticky = p.modLoc("block/translation_chassis_end_sticky");

			Vector<ModelFile> models = new Vector<>(4);
			for (boolean isTopSticky : Iterate.trueAndFalse)
				for (boolean isBottomSticky : Iterate.trueAndFalse)
					models.add(p.models()
						.withExistingParent(
							c.getName() + (isTopSticky ? "_top" : "") + (isBottomSticky ? "_bottom" : ""),
							"block/cube_bottom_top")
						.texture("side", side)
						.texture("bottom", isBottomSticky ? top_sticky : top)
						.texture("top", isTopSticky ? top_sticky : top));
			BiFunction<Boolean, Boolean, ModelFile> modelFunc = (t, b) -> models.get((t ? 0 : 2) + (b ? 0 : 1));

			axisBlock(c, p, state -> modelFunc.apply(state.get(LinearChassisBlock.STICKY_TOP),
				state.get(LinearChassisBlock.STICKY_BOTTOM)));
		};
	}

	public static <B extends RadialChassisBlock> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockstateProvider> radialChassis() {
		return (c, p) -> {
			String path = "block/" + c.getName();
			ResourceLocation side = p.modLoc(path + "_side");
			ResourceLocation side_sticky = p.modLoc(path + "_side_sticky");

			String templateModelPath = "block/rotation_chassis";
			ModelFile base = p.models()
				.getExistingFile(p.modLoc(templateModelPath + "/base"));
			Vector<ModelFile> faces = new Vector<>(3);
			Vector<ModelFile> stickyFaces = new Vector<>(3);

			for (Axis axis : Iterate.axes) {
				String suffix = "side_" + axis.getName();
				faces.add(p.models()
					.withExistingParent("block/" + c.getName() + "_" + suffix,
						p.modLoc(templateModelPath + "/" + suffix))
					.texture("side", side));
			}
			for (Axis axis : Iterate.axes) {
				String suffix = "side_" + axis.getName();
				stickyFaces.add(p.models()
					.withExistingParent("block/" + c.getName() + "_" + suffix + "_sticky",
						p.modLoc(templateModelPath + "/" + suffix))
					.texture("side", side_sticky));
			}

			MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());
			BlockState propertyGetter = c.get()
				.getDefaultState()
				.with(RadialChassisBlock.AXIS, Axis.Y);

			for (Axis axis : Iterate.axes)
				builder.part()
					.modelFile(base)
					.rotationX(axis != Axis.Y ? 90 : 0)
					.rotationY(axis != Axis.X ? 0 : 90)
					.addModel()
					.condition(RadialChassisBlock.AXIS, axis)
					.end();

			for (Direction face : Iterate.horizontalDirections) {
				for (boolean sticky : Iterate.trueAndFalse) {
					for (Axis axis : Iterate.axes) {
						int horizontalAngle = (int) (face.getHorizontalAngle());
						int index = axis.ordinal();
						int xRot = 0;
						int yRot = 0;

						if (axis == Axis.X)
							xRot = -horizontalAngle + 180;
						if (axis == Axis.Y)
							yRot = horizontalAngle;
						if (axis == Axis.Z) {
							yRot = -horizontalAngle + 270;

							// blockstates can't have zRot, so here we are
							if (face.getAxis() == Axis.Z) {
								index = 0;
								xRot = horizontalAngle + 180;
								yRot = 90;
							}
						}

						builder.part()
							.modelFile((sticky ? stickyFaces : faces).get(index))
							.rotationX((xRot + 360) % 360)
							.rotationY((yRot + 360) % 360)
							.addModel()
							.condition(RadialChassisBlock.AXIS, axis)
							.condition(c.get()
								.getGlueableSide(propertyGetter, face), sticky)
							.end();
					}
				}
			}
		};
	}

}
