package com.simibubi.create.content.contraptions.fluids.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fluids.FluidStack;

public class BasinFluidParticle extends FluidStackParticle {

	BlockPos basinPos;
	Vector3d targetPos;
	Vector3d centerOfBasin;
	float yOffset;

	public BasinFluidParticle(ClientWorld world, FluidStack fluid, double x, double y, double z, double vx, double vy,
		double vz) {
		super(world, fluid, x, y, z, vx, vy, vz);
		gravity = 0;
		xd = 0;
		yd = 0;
		zd = 0;
		yOffset = world.random.nextFloat() * 1 / 32f;
		y += yOffset;
		quadSize = 0;
		lifetime = 60;
		Vector3d currentPos = new Vector3d(x, y, z);
		basinPos = new BlockPos(currentPos);
		centerOfBasin = VecHelper.getCenterOf(basinPos);

		if (vx != 0) {
			lifetime = 20;
			Vector3d centerOf = VecHelper.getCenterOf(basinPos);
			Vector3d diff = currentPos.subtract(centerOf)
				.multiply(1, 0, 1)
				.normalize()
				.scale(.375);
			targetPos = centerOf.add(diff);
			xo = x = centerOfBasin.x;
			zo = z = centerOfBasin.z;
		}
	}

	@Override
	public void tick() {
		super.tick();
		quadSize = targetPos != null ? Math.max(1 / 32f, ((1f * age) / lifetime) / 8)
			: 1 / 8f * (1 - ((Math.abs(age - (lifetime / 2)) / (1f * lifetime))));

		if (age % 2 == 0) {
			if (!AllBlocks.BASIN.has(level.getBlockState(basinPos))) {
				remove();
				return;
			}

			TileEntity tileEntity = level.getBlockEntity(basinPos);
			if (tileEntity instanceof BasinTileEntity) {
				float totalUnits = ((BasinTileEntity) tileEntity).getTotalFluidUnits(0);
				if (totalUnits < 1)
					totalUnits = 0;
				float fluidLevel = MathHelper.clamp(totalUnits / 2000, 0, 1);
				y = 2 / 16f + basinPos.getY() + 12 / 16f * fluidLevel + yOffset;
			}

		}

		if (targetPos != null) {
			float progess = (1f * age) / lifetime;
			Vector3d currentPos = centerOfBasin.add(targetPos.subtract(centerOfBasin)
				.scale(progess));
			x = currentPos.x;
			z = currentPos.z;
		}
	}

	@Override
	public void render(IVertexBuilder vb, ActiveRenderInfo info, float pt) {
		Quaternion rotation = info.rotation();
		Quaternion prevRotation = new Quaternion(rotation);
		rotation.set(1, 0, 0, 1);
		rotation.normalize();
		super.render(vb, info, pt);
		rotation.set(0, 0, 0, 1);
		rotation.mul(prevRotation);
	}

	@Override
	protected boolean canEvaporate() {
		return false;
	}

}
