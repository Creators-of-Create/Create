package com.simibubi.create.content.kinetics.waterwheel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.StitchedSprite;
import net.createmod.catnip.api.client.render.SuperBufferFactory;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.SuperByteBufferCache;
import net.minecraft.client.Minecraft;
import com.simibubi.create.foundation.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.simibubi.create.foundation.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.model.data.ModelData;

public class WaterWheelRenderer<T extends WaterWheelBlockEntity> extends KineticBlockEntityRenderer<T> {
	public static final SuperByteBufferCache.Compartment<ModelKey> WATER_WHEEL = new SuperByteBufferCache.Compartment<>();

	public static final StitchedSprite OAK_PLANKS_TEMPLATE = new StitchedSprite(Identifier.withDefaultNamespace("block/oak_planks"));
	public static final StitchedSprite OAK_LOG_TEMPLATE = new StitchedSprite(Identifier.withDefaultNamespace("block/oak_log"));
	public static final StitchedSprite OAK_LOG_TOP_TEMPLATE = new StitchedSprite(Identifier.withDefaultNamespace("block/oak_log_top"));

	protected final boolean large;

	public WaterWheelRenderer(Context context, boolean large) {
		super(context);
		this.large = large;
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelRenderer<T> standard(Context context) {
		return new WaterWheelRenderer<>(context, false);
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelRenderer<T> large(Context context) {
		return new WaterWheelRenderer<>(context, true);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(T be, BlockState state) {
		ModelKey key = new ModelKey(large, state, be.material);
		return SuperByteBufferCache.getInstance().get(WATER_WHEEL, key, () -> {
			BlockState state1 = key.state();
			Direction dir;
			if (key.large()) {
				dir = Direction.fromAxisAndDirection(state1.getValue(LargeWaterWheelBlock.AXIS), AxisDirection.POSITIVE);
			} else {
				dir = state1.getValue(WaterWheelBlock.FACING);
			}
			PoseStack transform = CachedBuffers.rotateToFaceVertical(dir).get();
			return CachedBuffers.partial(Variant.of(key.large(), state1).partial(), state1)
				.transform(transform);
		});
	}

	@Nullable
	private static String plankStateToWoodName(BlockState planksBlockState) {
		Block planksBlock = planksBlockState.getBlock();
		Identifier id = RegisteredObjectsHelper.getKeyOrThrow(planksBlock);
		String path = id.getPath();

		if (path.endsWith("_planks")) // Covers most wood types
			return (path.startsWith("archwood") ? "blue_" : "") + path.substring(0, path.length() - 7);

		if (path.contains("wood/planks/")) // TerraFirmaCraft
			return path.substring(12);

		return null;
	}

	private static final String[] LOG_LOCATIONS = new String[] {

		"x_log", "x_stem", "x_block", // Covers most wood types
		"wood/log/x" // TerraFirmaCraft

	};

	private static BlockState getLogBlockState(String namespace, String wood) {
		for (String location : LOG_LOCATIONS) {
			Optional<BlockState> state =
				BuiltInRegistries.BLOCK.getOptional(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(namespace, location.replace("x", wood))))
					.map(Block::defaultBlockState);
			if (state.isPresent())
				return state.get();
		}
		return Blocks.OAK_LOG.defaultBlockState();
	}

	private static TextureAtlasSprite getSpriteOnSide(BlockState state, Direction side) {
		BakedModel model = com.simibubi.create.foundation.render.LegacyBlockRendererBridge.getBlockRenderer()
			.getBlockModel(state);
		if (model == null)
			return null;
		RandomSource random = RandomSource.create();
		random.setSeed(42L);
		List<BakedQuad> quads = model.getQuads(state, side, random, ModelData.EMPTY, null);
		if (!quads.isEmpty()) {
			return quads.get(0)
				.getSprite();
		}
		random.setSeed(42L);
		quads = model.getQuads(state, null, random, ModelData.EMPTY, null);
		if (!quads.isEmpty()) {
			for (BakedQuad quad : quads) {
				if (quad.getDirection() == side) {
					return quad.getSprite();
				}
			}
		}
		return model.getParticleIcon(ModelData.EMPTY);
	}

	public enum Variant {
		SMALL(AllPartialModels.WATER_WHEEL),
		LARGE(AllPartialModels.LARGE_WATER_WHEEL),
		LARGE_EXTENSION(AllPartialModels.LARGE_WATER_WHEEL_EXTENSION),
		;

		private final PartialModel partial;

		Variant(PartialModel partial) {
			this.partial = partial;
		}

		public PartialModel partial() {
			return partial;
		}

		public static Variant of(boolean large, BlockState blockState) {
			if (large) {
				boolean extension = blockState.getValue(LargeWaterWheelBlock.EXTENSION);
				if (extension) {
					return LARGE_EXTENSION;
				} else {
					return LARGE;
				}
			} else {
				return SMALL;
			}
		}
	}

	public record ModelKey(boolean large, BlockState state, BlockState material) {
	}
}
