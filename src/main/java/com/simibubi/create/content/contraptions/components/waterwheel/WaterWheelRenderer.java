package com.simibubi.create.content.contraptions.components.waterwheel;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.StitchedSprite;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticBlockEntityRenderer;
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
			BakedModel model = WaterWheelRenderer.generateModel(key);
			BlockState state1 = key.state();
			// TODO waterwheels
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

	public static PartialModel getTemplateModel(boolean large, boolean extension) {
		if (large) {
			if (extension) {
				return AllPartialModels.LARGE_WATER_WHEEL_EXTENSION;
			} else {
				return AllPartialModels.LARGE_WATER_WHEEL;
			}
		} else {
			return AllPartialModels.WATER_WHEEL;
		}
	}

	public static BakedModel generateModel(WaterWheelModelKey key) {
		boolean extension = key.state()
			.getOptionalValue(LargeWaterWheelBlock.EXTENSION)
			.orElse(false);
		BakedModel template = getTemplateModel(key.large(), extension).get();

		BlockState planksBlockState = key.material();
		Block planksBlock = planksBlockState.getBlock();
		ResourceLocation id = RegisteredObjects.getKeyOrThrow(planksBlock);
		String path = id.getPath();

		if (path.endsWith("_planks")) {
			String namespace = id.getNamespace();
			String wood = path.substring(0, path.length() - 7);
			BlockState logBlockState = getLogBlockState(namespace, wood);
			
			Map<TextureAtlasSprite, TextureAtlasSprite> map = new Reference2ReferenceOpenHashMap<>();
			map.put(OAK_PLANKS_TEMPLATE.get(), getSpriteOnSide(planksBlockState, Direction.UP));
			map.put(OAK_LOG_TEMPLATE.get(), getSpriteOnSide(logBlockState, Direction.NORTH));
			map.put(OAK_LOG_TOP_TEMPLATE.get(), getSpriteOnSide(logBlockState, Direction.UP));

			return BakedModelHelper.generateModel(template, map::get);
		}

		return BakedModelHelper.generateModel(template, sprite -> null);
	}

	private static BlockState getLogBlockState(String namespace, String wood) {
		for (String suffix : new String[] { "_log", "_stem" }) {
			Optional<BlockState> state =
				ForgeRegistries.BLOCKS.getHolder(new ResourceLocation(namespace, wood + suffix))
					.map(Holder::value)
					.map(Block::defaultBlockState);
			if (state.isPresent())
				return state.get();
		}
		return Blocks.OAK_LOG.defaultBlockState();
	}

	private static TextureAtlasSprite getSpriteOnSide(BlockState blockstate, Direction side) {
		BakedModel blockModel = Minecraft.getInstance()
			.getBlockRenderer()
			.getBlockModel(blockstate);
		if (blockModel == null)
			return null;
		@SuppressWarnings("deprecation")
		List<BakedQuad> quads = blockModel.getQuads(blockstate, side, RandomSource.create());
		if (quads.isEmpty())
			return null;
		return quads.get(0)
			.getSprite();
	}

}
