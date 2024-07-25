package com.simibubi.create.infrastructure.worldgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.simibubi.create.infrastructure.worldgen.LayerPattern.Layer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class LayeredOreFeature extends Feature<LayeredOreConfiguration> {
	public LayeredOreFeature() {
		super(LayeredOreConfiguration.CODEC);
	}

	private static final float MAX_LAYER_DISPLACEMENT = 1.75f;
	private static final float LAYER_NOISE_FREQUENCY = 0.125f;

	private static final float MAX_RADIAL_THRESHOLD_REDUCTION = 0.25f;
	private static final float RADIAL_NOISE_FREQUENCY = 0.125f;

	@Override
	public boolean place(FeaturePlaceContext<LayeredOreConfiguration> pContext) {
		RandomSource random = pContext.random();
		BlockPos origin = pContext.origin();
		WorldGenLevel worldGenLevel = pContext.level();
		LayeredOreConfiguration config = pContext.config();
		List<LayerPattern> patternPool = config.layerPatterns;

		if (patternPool.isEmpty())
			return false;

		LayerPattern layerPattern = patternPool.get(random.nextInt(patternPool.size()));

		int placedAmount = 0;
		int size = config.size + 1;
		float radius = config.size * 0.5f;
		int radiusBound = Mth.ceil(radius) - 1;
		int x0 = origin.getX();
		int y0 = origin.getY();
		int z0 = origin.getZ();

		if (origin.getY() >= worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ()))
			return false;

		List<TemporaryLayerEntry> tempLayers = new ArrayList<>();
		float layerSizeTotal = 0.0f;
		LayerPattern.Layer current = null;
		while (layerSizeTotal < size) {
			Layer next = layerPattern.rollNext(current, random);
			float layerSize = Mth.randomBetween(random, next.minSize, next.maxSize);
			tempLayers.add(new TemporaryLayerEntry(next, layerSize));
			layerSizeTotal += layerSize;
			current = next;
		}

		List<ResolvedLayerEntry> resolvedLayers = new ArrayList<>(tempLayers.size());
		float cumulativeLayerSize = -(layerSizeTotal - size) * random.nextFloat();
		for (TemporaryLayerEntry tempLayerEntry : tempLayers) {
			float rampStartValue = resolvedLayers.size() == 0 ?
					Float.NEGATIVE_INFINITY :
					cumulativeLayerSize * (2.0f / size) - 1.0f;
			cumulativeLayerSize += tempLayerEntry.size();
			if (cumulativeLayerSize < 0)
				continue;
			float radialThresholdMultiplier = Mth.randomBetween(random, 0.5f, 1.0f);
			resolvedLayers.add(new ResolvedLayerEntry(tempLayerEntry.layer, radialThresholdMultiplier, rampStartValue));
		}

		// Choose stacking direction
		float gy = Mth.randomBetween(random, -1.0f, 1.0f);
		gy = (float) Math.cbrt(gy); // Make layer alignment tend towards horizontal more than vertical
		float xzRescale = Mth.sqrt(1.0f - gy * gy);
		float theta = random.nextFloat() * Mth.TWO_PI;
		float gx = Mth.cos(theta) * xzRescale;
		float gz = Mth.sin(theta) * xzRescale;

		SimplexNoise layerDisplacementNoise = new SimplexNoise(random);
		SimplexNoise radiusNoise = new SimplexNoise(random);

		MutableBlockPos mutablePos = new MutableBlockPos();
		BulkSectionAccess bulkSectionAccess = new BulkSectionAccess(worldGenLevel);

		try {

			for (int dzBlock = -radiusBound; dzBlock <= radiusBound; dzBlock++) {
				float dz = dzBlock * (1.0f / radius);
				if (dz * dz > 1)
					continue;

				for (int dxBlock = -radiusBound; dxBlock <= radiusBound; dxBlock++) {
					float dx = dxBlock * (1.0f / radius);
					if (dz * dz + dx * dx > 1)
						continue;

					for (int dyBlock = -radiusBound; dyBlock <= radiusBound; dyBlock++) {
						float dy = dyBlock * (1.0f / radius);
						float distanceSquared = dz * dz + dx * dx + dy * dy;
						if (distanceSquared > 1)
							continue;
						if (worldGenLevel.isOutsideBuildHeight(y0 + dyBlock))
							continue;

						int currentX = x0 + dxBlock;
						int currentY = y0 + dyBlock;
						int currentZ = z0 + dzBlock;

						float rampValue = gx * dx + gy * dy + gz * dz;
						rampValue += layerDisplacementNoise.getValue(
								currentX * LAYER_NOISE_FREQUENCY, currentY * LAYER_NOISE_FREQUENCY, currentZ * LAYER_NOISE_FREQUENCY
						) * (MAX_LAYER_DISPLACEMENT / size);

						int layerIndex = Collections.binarySearch(resolvedLayers, new ResolvedLayerEntry(null, 0, rampValue));
						if (layerIndex < 0) layerIndex = -2 - layerIndex; // Counter (-insertionIndex - 1) return result, where insertionIndex = layerIndex + 1
						ResolvedLayerEntry layerEntry = resolvedLayers.get(layerIndex);

						if (distanceSquared > layerEntry.radialThresholdMultiplier)
							continue;

						float thresholdNoiseValue = Mth.map(
								(float) radiusNoise.getValue(currentX * RADIAL_NOISE_FREQUENCY, currentY * RADIAL_NOISE_FREQUENCY, currentZ * RADIAL_NOISE_FREQUENCY),
								-1.0f, 1.0f, 1.0f - MAX_RADIAL_THRESHOLD_REDUCTION, 1.0f
						);

						if (distanceSquared > layerEntry.radialThresholdMultiplier * thresholdNoiseValue)
							continue;

						LayerPattern.Layer layer = layerEntry.layer;
						List<TargetBlockState> targetBlockStates = layer.rollBlock(random);

						mutablePos.set(currentX, currentY, currentZ);
						if (!worldGenLevel.ensureCanWrite(mutablePos))
							continue;
						LevelChunkSection levelChunkSection = bulkSectionAccess.getSection(mutablePos);
						if (levelChunkSection == null)
							continue;

						int localX = SectionPos.sectionRelative(currentX);
						int localY = SectionPos.sectionRelative(currentY);
						int localZ = SectionPos.sectionRelative(currentZ);
						BlockState blockState = levelChunkSection.getBlockState(localX, localY, localZ);

						for (OreConfiguration.TargetBlockState targetBlockState : targetBlockStates) {
							if (!canPlaceOre(blockState, bulkSectionAccess::getBlockState, random, config,
								targetBlockState, mutablePos))
								continue;
							if (targetBlockState.state.isAir())
								continue;
							levelChunkSection.setBlockState(localX, localY, localZ, targetBlockState.state, false);
							++placedAmount;
							break;
						}

					}
				}
			}

		} catch (Throwable throwable1) {
			try {
				bulkSectionAccess.close();
			} catch (Throwable throwable) {
				throwable1.addSuppressed(throwable);
			}

			throw throwable1;
		}

		bulkSectionAccess.close();
		return placedAmount > 0;
	}

	public boolean canPlaceOre(BlockState pState, Function<BlockPos, BlockState> pAdjacentStateAccessor,
		RandomSource pRandom, LayeredOreConfiguration pConfig, OreConfiguration.TargetBlockState pTargetState,
		BlockPos.MutableBlockPos pMatablePos) {
		if (!pTargetState.target.test(pState, pRandom))
			return false;
		if (shouldSkipAirCheck(pRandom, pConfig.discardChanceOnAirExposure))
			return true;

		return !isAdjacentToAir(pAdjacentStateAccessor, pMatablePos);
	}

	protected boolean shouldSkipAirCheck(RandomSource pRandom, float pChance) {
		return pChance <= 0 ? true : pChance >= 1 ? false : pRandom.nextFloat() >= pChance;
	}

	private record TemporaryLayerEntry(Layer layer, float size) { }

	private record ResolvedLayerEntry(Layer layer, float radialThresholdMultiplier, float rampStartValue) implements Comparable<ResolvedLayerEntry> {
		@Override
		public int compareTo(LayeredOreFeature.ResolvedLayerEntry b) {
			return Float.compare(rampStartValue, b.rampStartValue);
		}
	}
}
