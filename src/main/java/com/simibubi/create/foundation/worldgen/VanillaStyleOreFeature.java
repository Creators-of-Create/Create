package com.simibubi.create.foundation.worldgen;

import java.util.BitSet;
import java.util.Random;

import com.simibubi.create.Create;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class VanillaStyleOreFeature extends OreFeatureBase {

	public static final VanillaStyleOreFeature INSTANCE = new VanillaStyleOreFeature();

	public static final ResourceLocation ID = Create.asResource("config_driven_ore");

	public VanillaStyleOreFeature() {

	}

	// From OreFeature, slight adjustments

	public boolean place(FeaturePlaceContext<ConfigDrivenOreConfiguration> pContext) {

		Random random = pContext.random();
		BlockPos blockpos = pContext.origin();
		WorldGenLevel worldgenlevel = pContext.level();
		ConfigDrivenOreConfiguration oreconfiguration = pContext.config();

		float f = random.nextFloat() * (float) Math.PI;
		float diameter = (float) oreconfiguration.getSize() / 8.0F;
		int centeringOffset = Mth.ceil(((float) oreconfiguration.getSize() / 8.0F + 1.0F) / 2.0F);
		double d0 = blockpos.getX() + Math.sin(f) * diameter;
		double d1 = blockpos.getX() - Math.sin(f) * diameter;
		double d2 = blockpos.getZ() + Math.cos(f) * diameter;
		double d3 = blockpos.getZ() - Math.cos(f) * diameter;
		double d4 = (blockpos.getY() + random.nextInt(3) - 2);
		double d5 = (blockpos.getY() + random.nextInt(3) - 2);
		int k = blockpos.getX() - Mth.ceil(diameter) - centeringOffset;
		int l = blockpos.getY() - 2 - centeringOffset;
		int i1 = blockpos.getZ() - Mth.ceil(diameter) - centeringOffset;
		int j1 = 2 * (Mth.ceil(diameter) + centeringOffset);
		int k1 = 2 * (2 + centeringOffset);

		for (int x = k; x <= k + j1; ++x)
			for (int z = i1; z <= i1 + j1; ++z)
				if (l <= worldgenlevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z))
					return this.doPlace(worldgenlevel, random, oreconfiguration, d0, d1, d2, d3, d4, d5, k, l, i1, j1,
						k1);

		return false;
	}

	protected boolean doPlace(WorldGenLevel pLevel, Random pRandom, ConfigDrivenOreConfiguration pConfig, double pMinX,
		double pMaxX, double pMinZ, double pMaxZ, double pMinY, double pMaxY, int pX, int pY, int pZ, int pWidth,
		int pHeight) {
		int i = 0;
		BitSet bitset = new BitSet(pWidth * pHeight * pWidth);
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		int j = pConfig.getSize();
		double[] adouble = new double[j * 4];

		for (int k = 0; k < j; ++k) {
			float f = (float) k / (float) j;
			double d0 = Mth.lerp(f, pMinX, pMaxX);
			double d1 = Mth.lerp(f, pMinY, pMaxY);
			double d2 = Mth.lerp(f, pMinZ, pMaxZ);
			double d3 = pRandom.nextDouble() * j / 16.0D;
			double d4 = ((Mth.sin((float) Math.PI * f) + 1.0F) * d3 + 1.0D) / 2.0D;
			adouble[k * 4 + 0] = d0;
			adouble[k * 4 + 1] = d1;
			adouble[k * 4 + 2] = d2;
			adouble[k * 4 + 3] = d4;
		}

		for (int l3 = 0; l3 < j - 1; ++l3) {
			if (adouble[l3 * 4 + 3] <= 0.0D)
				continue;
			for (int i4 = l3 + 1; i4 < j; ++i4) {
				if (adouble[i4 * 4 + 3] <= 0.0D)
					continue;

				double d8 = adouble[l3 * 4 + 0] - adouble[i4 * 4 + 0];
				double d10 = adouble[l3 * 4 + 1] - adouble[i4 * 4 + 1];
				double d12 = adouble[l3 * 4 + 2] - adouble[i4 * 4 + 2];
				double d14 = adouble[l3 * 4 + 3] - adouble[i4 * 4 + 3];
				if (d14 * d14 <= d8 * d8 + d10 * d10 + d12 * d12)
					continue;

				if (d14 > 0.0D)
					adouble[i4 * 4 + 3] = -1.0D;
				else
					adouble[l3 * 4 + 3] = -1.0D;
			}
		}

		BulkSectionAccess bulksectionaccess = new BulkSectionAccess(pLevel);

		try {
			for (int j4 = 0; j4 < j; ++j4) {
				double d9 = adouble[j4 * 4 + 3];
				if (d9 < 0.0D)
					continue;

				double d11 = adouble[j4 * 4 + 0];
				double d13 = adouble[j4 * 4 + 1];
				double d15 = adouble[j4 * 4 + 2];
				int k4 = Math.max(Mth.floor(d11 - d9), pX);
				int l = Math.max(Mth.floor(d13 - d9), pY);
				int i1 = Math.max(Mth.floor(d15 - d9), pZ);
				int j1 = Math.max(Mth.floor(d11 + d9), k4);
				int k1 = Math.max(Mth.floor(d13 + d9), l);
				int l1 = Math.max(Mth.floor(d15 + d9), i1);

				for (int i2 = k4; i2 <= j1; ++i2) {
					double d5 = (i2 + 0.5D - d11) / d9;
					if (d5 * d5 >= 1.0D)
						continue;
					for (int j2 = l; j2 <= k1; ++j2) {
						double d6 = (j2 + 0.5D - d13) / d9;
						if (d5 * d5 + d6 * d6 >= 1.0D)
							continue;
						for (int k2 = i1; k2 <= l1; ++k2) {
							double d7 = (k2 + 0.5D - d15) / d9;
							if (d5 * d5 + d6 * d6 + d7 * d7 >= 1.0D || pLevel.isOutsideBuildHeight(j2))
								continue;

							int l2 = i2 - pX + (j2 - pY) * pWidth + (k2 - pZ) * pWidth * pHeight;
							if (bitset.get(l2))
								continue;
							bitset.set(l2);
							blockpos$mutableblockpos.set(i2, j2, k2);
							if (!pLevel.ensureCanWrite(blockpos$mutableblockpos))
								continue;
							LevelChunkSection levelchunksection =
								bulksectionaccess.getSection(blockpos$mutableblockpos);
							if (levelchunksection == null)
								continue;
							int i3 = SectionPos.sectionRelative(i2);
							int j3 = SectionPos.sectionRelative(j2);
							int k3 = SectionPos.sectionRelative(k2);
							BlockState blockstate = levelchunksection.getBlockState(i3, j3, k3);

							for (OreConfiguration.TargetBlockState oreconfiguration$targetblockstate : pConfig.targetStates) {
								if (!canPlaceOre(blockstate, bulksectionaccess::getBlockState, pRandom, pConfig,
									oreconfiguration$targetblockstate, blockpos$mutableblockpos))
									continue;
								levelchunksection.setBlockState(i3, j3, k3, oreconfiguration$targetblockstate.state,
									false);
								++i;
								break;
							}
						}
					}
				}
			}

		} catch (Throwable throwable1) {
			try {
				bulksectionaccess.close();
			} catch (Throwable throwable) {
				throwable1.addSuppressed(throwable);
			}

			throw throwable1;
		}

		bulksectionaccess.close();
		return i > 0;
	}

}
