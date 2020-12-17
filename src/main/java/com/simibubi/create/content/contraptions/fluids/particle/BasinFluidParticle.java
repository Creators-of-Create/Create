package com.simibubi.create.content.contraptions.fluids.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class BasinFluidParticle extends FluidStackParticle {

	BlockPos basinPos;
	Vec3d targetPos;
	Vec3d centerOfBasin;
	float yOffset;

	public BasinFluidParticle(World world, FluidStack fluid, double x, double y, double z, double vx, double vy,
		double vz) {
		super(world, fluid, x, y, z, vx, vy, vz);
		particleGravity = 0;
		motionX = 0;
		motionY = 0;
		motionZ = 0;
		yOffset = world.rand.nextFloat() * 1 / 32f;
		posY += yOffset;
		particleScale = 0;
		maxAge = 60;
		Vec3d currentPos = new Vec3d(posX, posY, posZ);
		basinPos = new BlockPos(currentPos);
		centerOfBasin = VecHelper.getCenterOf(basinPos);

		if (vx != 0) {
			maxAge = 20;
			Vec3d centerOf = VecHelper.getCenterOf(basinPos);
			Vec3d diff = currentPos.subtract(centerOf)
				.mul(1, 0, 1)
				.normalize()
				.scale(.375);
			targetPos = centerOf.add(diff);
			prevPosX = posX = centerOfBasin.x;
			prevPosZ = posZ = centerOfBasin.z;
		}
	}

	@Override
	public void tick() {
		super.tick();
		particleScale = targetPos != null ? Math.max(1 / 32f, ((1f * age) / maxAge) / 8)
			: 1 / 8f * (1 - ((Math.abs(age - (maxAge / 2)) / (1f * maxAge))));

		if (age % 2 == 0) {
			if (!AllBlocks.BASIN.has(world.getBlockState(basinPos))) {
				setExpired();
				return;
			}

			TileEntity tileEntity = world.getTileEntity(basinPos);
			if (tileEntity instanceof BasinTileEntity) {
				float totalUnits = ((BasinTileEntity) tileEntity).getTotalFluidUnits(0);
				if (totalUnits < 1)
					totalUnits = 0;
				float fluidLevel = MathHelper.clamp(totalUnits / 2000, 0, 1);
				posY = 2 / 16f + basinPos.getY() + 12 / 16f * fluidLevel + yOffset;
			}

		}

		if (targetPos != null) {
			float progess = (1f * age) / maxAge;
			Vec3d currentPos = centerOfBasin.add(targetPos.subtract(centerOfBasin)
				.scale(progess));
			posX = currentPos.x;
			posZ = currentPos.z;
		}
	}

	@Override
	public void buildGeometry(IVertexBuilder vb, ActiveRenderInfo info, float pt) {
		Quaternion rotation = info.getRotation();
		Quaternion prevRotation = new Quaternion(rotation);
		rotation.set(1, 0, 0, 1);
		rotation.normalize();
		super.buildGeometry(vb, info, pt);
		rotation.set(0, 0, 0, 1);
		rotation.multiply(prevRotation);
	}

	@Override
	protected boolean canEvaporate() {
		return false;
	}

}
