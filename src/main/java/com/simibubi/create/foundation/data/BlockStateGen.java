
package com.simibubi.create.foundation.data;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.components.steam.whistle.WhistleBlock.WhistleSize;
import com.simibubi.create.content.contraptions.components.steam.whistle.WhistleExtenderBlock;
import com.simibubi.create.content.contraptions.components.steam.whistle.WhistleExtenderBlock.WhistleExtenderShape;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.LinearChassisBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.RadialChassisBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssembleRailType;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pointing;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile;
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

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> directionalBlockProviderIgnoresWaterlogged(
		boolean customItem) {
		return (c, p) -> directionalBlockIgnoresWaterlogged(c, p, getBlockModel(customItem, c, p));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalBlockProvider(
		boolean customItem) {
		return (c, p) -> p.horizontalBlock(c.get(), getBlockModel(customItem, c, p));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalAxisBlockProvider(
		boolean customItem) {
		return (c, p) -> horizontalAxisBlock(c, p, getBlockModel(customItem, c, p));
	}
	
	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> simpleCubeAll(
		String path) {
		return (c, p) -> p.simpleBlock(c.get(), p.models()
			.cubeAll(c.getName(), p.modLoc("block/" + path)));
	}

	public static <T extends DirectionalAxisKineticBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> directionalAxisBlockProvider() {
		return (c, p) -> directionalAxisBlock(c, p, ($, vertical) -> p.models()
			.getExistingFile(p.modLoc("block/" + c.getName() + "/" + (vertical ? "vertical" : "horizontal"))));
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalWheelProvider(
		boolean customItem) {
		return (c, p) -> horizontalWheel(c, p, getBlockModel(customItem, c, p));
	}

	// Utility

	private static <T extends Block> Function<BlockState, ModelFile> getBlockModel(boolean customItem,
		DataGenContext<Block, T> c, RegistrateBlockstateProvider p) {
		return $ -> customItem ? AssetLookup.partialBaseModel(c, p) : AssetLookup.standardModel(c, p);
	}

	// Generators

	public static <T extends Block> void directionalBlockIgnoresWaterlogged(DataGenContext<Block, T> ctx,
		RegistrateBlockstateProvider prov, Function<BlockState, ModelFile> modelFunc) {
		prov.getVariantBuilder(ctx.getEntry())
			.forAllStatesExcept(state -> {
				Direction dir = state.getValue(BlockStateProperties.FACING);
				return ConfiguredModel.builder()
					.modelFile(modelFunc.apply(state))
					.rotationX(dir == Direction.DOWN ? 180
						: dir.getAxis()
							.isHorizontal() ? 90 : 0)
					.rotationY(dir.getAxis()
						.isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
					.build();
			}, BlockStateProperties.WATERLOGGED);
	}

	public static <T extends Block> void axisBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		Function<BlockState, ModelFile> modelFunc) {
		axisBlock(ctx, prov, modelFunc, false);
	}

	public static <T extends Block> void axisBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		Function<BlockState, ModelFile> modelFunc, boolean uvLock) {
		prov.getVariantBuilder(ctx.getEntry())
			.forAllStatesExcept(state -> {
				Axis axis = state.getValue(BlockStateProperties.AXIS);
				return ConfiguredModel.builder()
					.modelFile(modelFunc.apply(state))
					.uvLock(uvLock)
					.rotationX(axis == Axis.Y ? 0 : 90)
					.rotationY(axis == Axis.X ? 90 : axis == Axis.Z ? 180 : 0)
					.build();
			}, BlockStateProperties.WATERLOGGED);
	}

	public static <T extends Block> void simpleBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		Function<BlockState, ModelFile> modelFunc) {
		prov.getVariantBuilder(ctx.getEntry())
			.forAllStatesExcept(state -> {
				return ConfiguredModel.builder()
					.modelFile(modelFunc.apply(state))
					.build();
			}, BlockStateProperties.WATERLOGGED);
	}

	public static <T extends Block> void horizontalAxisBlock(DataGenContext<Block, T> ctx,
		RegistrateBlockstateProvider prov, Function<BlockState, ModelFile> modelFunc) {
		prov.getVariantBuilder(ctx.getEntry())
			.forAllStates(state -> {
				Axis axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
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

				boolean alongFirst = state.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
				Direction direction = state.getValue(DirectionalAxisKineticBlock.FACING);
				boolean vertical = direction.getAxis()
					.isHorizontal() && (direction.getAxis() == Axis.X) == alongFirst;
				int xRot = direction == Direction.DOWN ? 270 : direction == Direction.UP ? 90 : 0;
				int yRot = direction.getAxis()
					.isVertical() ? alongFirst ? 0 : 90 : (int) direction.toYRot();

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
				.rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING)
					.toYRot() + 180) % 360)
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

	public static NonNullBiConsumer<DataGenContext<Block, CartAssemblerBlock>, RegistrateBlockstateProvider> cartAssembler() {
		return (c, p) -> p.getVariantBuilder(c.get())
			.forAllStates(state -> {
				CartAssembleRailType type = state.getValue(CartAssemblerBlock.RAIL_TYPE);
				Boolean powered = state.getValue(CartAssemblerBlock.POWERED);
				Boolean backwards = state.getValue(CartAssemblerBlock.BACKWARDS);
				RailShape shape = state.getValue(CartAssemblerBlock.RAIL_SHAPE);

				int yRotation = shape == RailShape.EAST_WEST ? 270 : 0;
				if (backwards)
					yRotation += 180;

				return ConfiguredModel.builder()
					.modelFile(p.models()
						.getExistingFile(p.modLoc("block/" + c.getName() + "/block_" + type.getSerializedName()
							+ (powered ? "_powered" : ""))))
					.rotationY(yRotation % 360)
					.build();
			});
	}

	public static NonNullBiConsumer<DataGenContext<Block, BlazeBurnerBlock>, RegistrateBlockstateProvider> blazeHeater() {
		return (c, p) -> ConfiguredModel.builder()
			.modelFile(p.models()
				.getExistingFile(p.modLoc("block/" + c.getName() + "/block")))
			.build();
	}

	public static <B extends LinearChassisBlock> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockstateProvider> linearChassis() {
		return (c, p) -> {
			ResourceLocation side = p.modLoc("block/" + c.getName() + "_side");
			ResourceLocation top = p.modLoc("block/linear_chassis_end");
			ResourceLocation top_sticky = p.modLoc("block/linear_chassis_end_sticky");

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

			axisBlock(c, p, state -> modelFunc.apply(state.getValue(LinearChassisBlock.STICKY_TOP),
				state.getValue(LinearChassisBlock.STICKY_BOTTOM)));
		};
	}

	public static <B extends RadialChassisBlock> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockstateProvider> radialChassis() {
		return (c, p) -> {
			String path = "block/" + c.getName();
			ResourceLocation side = p.modLoc(path + "_side");
			ResourceLocation side_sticky = p.modLoc(path + "_side_sticky");

			String templateModelPath = "block/radial_chassis";
			ModelFile base = p.models()
				.getExistingFile(p.modLoc(templateModelPath + "/base"));
			Vector<ModelFile> faces = new Vector<>(3);
			Vector<ModelFile> stickyFaces = new Vector<>(3);

			for (Axis axis : Iterate.axes) {
				String suffix = "side_" + axis.getSerializedName();
				faces.add(p.models()
					.withExistingParent("block/" + c.getName() + "_" + suffix,
						p.modLoc(templateModelPath + "/" + suffix))
					.texture("side", side));
			}
			for (Axis axis : Iterate.axes) {
				String suffix = "side_" + axis.getSerializedName();
				stickyFaces.add(p.models()
					.withExistingParent("block/" + c.getName() + "_" + suffix + "_sticky",
						p.modLoc(templateModelPath + "/" + suffix))
					.texture("side", side_sticky));
			}

			MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());
			BlockState propertyGetter = c.get()
				.defaultBlockState()
				.setValue(RadialChassisBlock.AXIS, Axis.Y);

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
						int horizontalAngle = (int) (face.toYRot());
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

	public static <P extends Block> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> naturalStoneTypeBlock(
		String type) {
		return (c, p) -> {
			ConfiguredModel[] variants = new ConfiguredModel[4];
			for (int i = 0; i < variants.length; i++)
				variants[i] = ConfiguredModel.builder()
					.modelFile(p.models()
						.cubeAll(type + "_natural_" + i, p.modLoc("block/palettes/stone_types/natural/" + type + "_" + i)))
					.buildLast();
			p.getVariantBuilder(c.get())
				.partialState()
				.setModels(variants);
		};
	}

	public static <P extends EncasedPipeBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> encasedPipe() {
		return (c, p) -> {
			ModelFile open = AssetLookup.partialBaseModel(c, p, "open");
			ModelFile flat = AssetLookup.partialBaseModel(c, p, "flat");
			MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());
			for (boolean flatPass : Iterate.trueAndFalse)
				for (Direction d : Iterate.directions) {
					int verticalAngle = d == Direction.UP ? 90 : d == Direction.DOWN ? -90 : 0;
					builder.part()
						.modelFile(flatPass ? flat : open)
						.rotationX(verticalAngle)
						.rotationY((int) (d.toYRot() + (d.getAxis()
							.isVertical() ? 90 : 0)) % 360)
						.addModel()
						.condition(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(d), !flatPass)
						.end();
				}
		};
	}

	public static <P extends TrapDoorBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> uvLockedTrapdoorBlock(
		P block, ModelFile bottom, ModelFile top, ModelFile open) {
		return (c, p) -> {
			p.getVariantBuilder(block)
				.forAllStatesExcept(state -> {
					int xRot = 0;
					int yRot = ((int) state.getValue(TrapDoorBlock.FACING)
						.toYRot()) + 180;
					boolean isOpen = state.getValue(TrapDoorBlock.OPEN);
					if (!isOpen)
						yRot = 0;
					yRot %= 360;
					return ConfiguredModel.builder()
						.modelFile(isOpen ? open : state.getValue(TrapDoorBlock.HALF) == Half.TOP ? top : bottom)
						.rotationX(xRot)
						.rotationY(yRot)
						.uvLock(!isOpen)
						.build();
				}, TrapDoorBlock.POWERED, TrapDoorBlock.WATERLOGGED);
		};
	}

	public static <P extends WhistleExtenderBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> whistleExtender() {
		return (c, p) -> {
			BlockModelProvider models = p.models();
			String basePath = "block/steam_whistle/extension/";
			MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());

			for (WhistleSize size : WhistleSize.values()) {
				String basePathSize = basePath + size.getSerializedName() + "_";
				ExistingModelFile topRim = models.getExistingFile(Create.asResource(basePathSize + "top_rim"));
				ExistingModelFile single = models.getExistingFile(Create.asResource(basePathSize + "single"));
				ExistingModelFile double_ = models.getExistingFile(Create.asResource(basePathSize + "double"));

				builder.part()
					.modelFile(topRim)
					.addModel()
					.condition(WhistleExtenderBlock.SIZE, size)
					.condition(WhistleExtenderBlock.SHAPE, WhistleExtenderShape.DOUBLE)
					.end()
					.part()
					.modelFile(single)
					.addModel()
					.condition(WhistleExtenderBlock.SIZE, size)
					.condition(WhistleExtenderBlock.SHAPE, WhistleExtenderShape.SINGLE)
					.end()
					.part()
					.modelFile(double_)
					.addModel()
					.condition(WhistleExtenderBlock.SIZE, size)
					.condition(WhistleExtenderBlock.SHAPE, WhistleExtenderShape.DOUBLE,
						WhistleExtenderShape.DOUBLE_CONNECTED)
					.end();
			}
		};
	}

	public static <P extends FluidPipeBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> pipe() {
		return (c, p) -> {
			String path = "block/" + c.getName();

			String LU = "lu";
			String RU = "ru";
			String LD = "ld";
			String RD = "rd";
			String LR = "lr";
			String UD = "ud";
			String U = "u";
			String D = "d";
			String L = "l";
			String R = "r";

			List<String> orientations = ImmutableList.of(LU, RU, LD, RD, LR, UD, U, D, L, R);
			Map<String, Pair<Integer, Integer>> uvs = ImmutableMap.<String, Pair<Integer, Integer>>builder()
				.put(LU, Pair.of(12, 4))
				.put(RU, Pair.of(8, 4))
				.put(LD, Pair.of(12, 0))
				.put(RD, Pair.of(8, 0))
				.put(LR, Pair.of(4, 8))
				.put(UD, Pair.of(0, 8))
				.put(U, Pair.of(4, 4))
				.put(D, Pair.of(0, 0))
				.put(L, Pair.of(4, 0))
				.put(R, Pair.of(0, 4))
				.build();

			Map<Axis, ResourceLocation> coreTemplates = new IdentityHashMap<>();
			Map<Pair<String, Axis>, ModelFile> coreModels = new HashMap<>();

			for (Axis axis : Iterate.axes)
				coreTemplates.put(axis, p.modLoc(path + "/core_" + axis.getSerializedName()));

			for (Axis axis : Iterate.axes) {
				ResourceLocation parent = coreTemplates.get(axis);
				for (String s : orientations) {
					Pair<String, Axis> key = Pair.of(s, axis);
					String modelName = path + "/" + s + "_" + axis.getSerializedName();
					coreModels.put(key, p.models()
						.withExistingParent(modelName, parent)
						.element()
						.from(4, 4, 4)
						.to(12, 12, 12)
						.face(Direction.get(AxisDirection.POSITIVE, axis))
						.end()
						.face(Direction.get(AxisDirection.NEGATIVE, axis))
						.end()
						.faces((d, builder) -> {
							Pair<Integer, Integer> pair = uvs.get(s);
							float u = pair.getKey();
							float v = pair.getValue();
							if (d == Direction.UP)
								builder.uvs(u + 4, v + 4, u, v);
							if (d == Direction.DOWN)
								builder.uvs(u + 4, v, u, v + 4);
							if (d == Direction.NORTH)
								builder.uvs(u, v, u + 4, v + 4);
							if (d == Direction.SOUTH)
								builder.uvs(u + 4, v, u, v + 4);
							if (d == Direction.EAST)
								builder.uvs(u, v, u + 4, v + 4);
							if (d == Direction.WEST)
								builder.uvs(u + 4, v, u, v + 4);
							builder.texture("#0");
						})
						.end());
				}
			}

			MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());
			for (Axis axis : Iterate.axes) {
				putPart(coreModels, builder, axis, LU, true, false, true, false);
				putPart(coreModels, builder, axis, RU, true, false, false, true);
				putPart(coreModels, builder, axis, LD, false, true, true, false);
				putPart(coreModels, builder, axis, RD, false, true, false, true);
				putPart(coreModels, builder, axis, UD, true, true, false, false);
				putPart(coreModels, builder, axis, U, true, false, false, false);
				putPart(coreModels, builder, axis, D, false, true, false, false);
				putPart(coreModels, builder, axis, LR, false, false, true, true);
				putPart(coreModels, builder, axis, L, false, false, true, false);
				putPart(coreModels, builder, axis, R, false, false, false, true);
			}
		};
	}

	private static void putPart(Map<Pair<String, Axis>, ModelFile> coreModels, MultiPartBlockStateBuilder builder,
		Axis axis, String s, boolean up, boolean down, boolean left, boolean right) {
		Direction positiveAxis = Direction.get(AxisDirection.POSITIVE, axis);
		Map<Direction, BooleanProperty> propertyMap = FluidPipeBlock.PROPERTY_BY_DIRECTION;

		Direction upD = Pointing.UP.getCombinedDirection(positiveAxis);
		Direction leftD = Pointing.LEFT.getCombinedDirection(positiveAxis);
		Direction rightD = Pointing.RIGHT.getCombinedDirection(positiveAxis);
		Direction downD = Pointing.DOWN.getCombinedDirection(positiveAxis);

		if (axis == Axis.Y || axis == Axis.X) {
			leftD = leftD.getOpposite();
			rightD = rightD.getOpposite();
		}

		builder.part()
			.modelFile(coreModels.get(Pair.of(s, axis)))
			.addModel()
			.condition(propertyMap.get(upD), up)
			.condition(propertyMap.get(leftD), left)
			.condition(propertyMap.get(rightD), right)
			.condition(propertyMap.get(downD), down)
			.end();
	}

	public static Function<BlockState, ConfiguredModel[]> mapToAir(@NonnullType RegistrateBlockstateProvider p) {
		return state -> ConfiguredModel.builder()
			.modelFile(p.models()
				.getExistingFile(p.mcLoc("block/air")))
			.build();
	}

}
