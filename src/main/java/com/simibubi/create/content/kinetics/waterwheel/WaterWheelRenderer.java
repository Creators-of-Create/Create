package com.simibubi.create.content.kinetics.waterwheel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.core.StitchedSprite;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.model.BakedModelHelper;
import com.simibubi.create.foundation.render.BakedModelRenderHelper;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.SuperByteBufferCache.Compartment;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;

public class WaterWheelRenderer<T extends WaterWheelBlockEntity> extends KineticBlockEntityRenderer<T> {
	public static final Compartment<WaterWheelModelKey> WATER_WHEEL = new Compartment<>();

	public static final StitchedSprite OAK_PLANKS_TEMPLATE = new StitchedSprite(new ResourceLocation("block/oak_planks"));
	public static final StitchedSprite OAK_LOG_TEMPLATE = new StitchedSprite(new ResourceLocation("block/oak_log"));
	public static final StitchedSprite OAK_LOG_TOP_TEMPLATE = new StitchedSprite(new ResourceLocation("block/oak_log_top"));

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
		WaterWheelModelKey key = new WaterWheelModelKey(large, state, be.material);
		return CreateClient.BUFFER_CACHE.get(WATER_WHEEL, key, () -> {
			BakedModel model = generateModel(key);
			BlockState state1 = key.state();
			Direction dir;
			if (key.large()) {
				dir = Direction.fromAxisAndDirection(state1.getValue(LargeWaterWheelBlock.AXIS), AxisDirection.POSITIVE);
			} else {
				dir = state1.getValue(WaterWheelBlock.FACING);
			}
			PoseStack transform = CachedBufferer.rotateToFaceVertical(dir).get();
			return BakedModelRenderHelper.standardModelRender(model, Blocks.AIR.defaultBlockState(), transform);
		});
	}

	public static BakedModel generateModel(WaterWheelModelKey key) {
		BakedModel template;
		if (key.large()) {
			boolean extension = key.state()
				.getValue(LargeWaterWheelBlock.EXTENSION);
			if (extension) {
				template = AllPartialModels.LARGE_WATER_WHEEL_EXTENSION.get();
			} else {
				template = AllPartialModels.LARGE_WATER_WHEEL.get();
			}
		} else {
			template = AllPartialModels.WATER_WHEEL.get();
		}

		return generateModel(template, key.material());
	}

	public static BakedModel generateModel(BakedModel template, BlockState planksBlockState) {
		Block planksBlock = planksBlockState.getBlock();
		ResourceLocation id = RegisteredObjects.getKeyOrThrow(planksBlock);
		String wood = plankStateToWoodName(planksBlockState);

		if (wood == null)
			return BakedModelHelper.generateModel(template, sprite -> null);

		String namespace = id.getNamespace();
		BlockState logBlockState = getLogBlockState(namespace, wood);

		Map<TextureAtlasSprite, TextureAtlasSprite> map = new Reference2ReferenceOpenHashMap<>();
		map.put(OAK_PLANKS_TEMPLATE.get(), getSpriteOnSide(planksBlockState, Direction.UP));
		map.put(OAK_LOG_TEMPLATE.get(), getSpriteOnSide(logBlockState, Direction.SOUTH));
		map.put(OAK_LOG_TOP_TEMPLATE.get(), getSpriteOnSide(logBlockState, Direction.UP));

		return BakedModelHelper.generateModel(template, map::get);
	}
	
	@Nullable
	private static String plankStateToWoodName(BlockState planksBlockState) {
		Block planksBlock = planksBlockState.getBlock();
		ResourceLocation id = RegisteredObjects.getKeyOrThrow(planksBlock);
		String path = id.getPath();
		
		if (path.endsWith("_planks")) // Covers most wood types
			return path.substring(0, path.length() - 7);
		
		if (path.contains("wood/planks/")) // TerraFirmaCraft
			return path.substring(12);
		
		return null;
	}

	private static final String[] LOG_LOCATIONS = new String[] {

		"x_log", "x_stem", // Covers most wood types
		"wood/log/x" // TerraFirmaCraft

	};

	private static BlockState getLogBlockState(String namespace, String wood) {
		for (String location : LOG_LOCATIONS) {
			Optional<BlockState> state =
				ForgeRegistries.BLOCKS.getHolder(new ResourceLocation(namespace, location.replace("x", wood)))
					.map(Holder::value)
					.map(Block::defaultBlockState);
			if (state.isPresent())
				return state.get();
		}
		return Blocks.OAK_LOG.defaultBlockState();
	}

	private static TextureAtlasSprite getSpriteOnSide(BlockState state, Direction side) {
		BakedModel model = Minecraft.getInstance()
			.getBlockRenderer()
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

}
