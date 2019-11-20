package com.simibubi.create.modules.contraptions.receivers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.logistics.InWorldProcessing;
import com.simibubi.create.modules.logistics.InWorldProcessing.Type;

import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EncasedFanBeltHandler {

	// Fans need to be aware of belt TEs within their range
	// all belts are handled equally
	// requires ref to controller and index

	static List<BeltTileEntity> findBelts(EncasedFanTileEntity fan) {
		if (fan.getSpeed() == 0)
			return Collections.emptyList();
		List<BeltTileEntity> belts = new ArrayList<>();
		AxisAlignedBB searchBB = fan.frontBB.shrink(.25).contract(0, 0, 0).expand(0, -.25f, 0);

		BlockPos.getAllInBox((int) searchBB.minX, (int) searchBB.minY, (int) searchBB.minZ, (int) searchBB.maxX - 1,
				(int) searchBB.maxY - 1, (int) searchBB.maxZ - 1)
				.filter(p -> AllBlocks.BELT.typeOf(fan.getWorld().getBlockState(p))).forEach(p -> {
					TileEntity te = fan.getWorld().getTileEntity(p);
					if (te == null || !(te instanceof BeltTileEntity))
						return;
					belts.add((BeltTileEntity) te);
				});

		return belts;
	}

	static void tickBelts(EncasedFanTileEntity fan, List<BeltTileEntity> belts) {
		Type processingType = fan.getProcessingType();
		if (processingType == null)
			return;
		for (BeltTileEntity belt : belts) {
			BeltTileEntity controller = belt.getControllerTE();
			if (controller == null)
				continue;
			World world = belt.getWorld();

			controller.getInventory().forEachWithin(belt.index + .5f, .5f, (transported) -> {
				if (world.rand.nextInt(4) == 0 && world.isRemote) {
					Vec3d vec = controller.getInventory().getVectorForOffset(transported.beltPosition);
					if (processingType == Type.BLASTING)
						world.addParticle(ParticleTypes.LARGE_SMOKE, vec.x, vec.y + .25f, vec.z, 0, 1 / 16f, 0);
					if (processingType == Type.SMOKING)
						world.addParticle(ParticleTypes.CLOUD, vec.x, vec.y + .25f, vec.z, 0, 1 / 16f, 0);
					if (processingType == Type.SPLASHING)
						world.addParticle(ParticleTypes.BUBBLE_POP, vec.x + (world.rand.nextFloat() - .5f) * .5f,
								vec.y + .25f, vec.z + (world.rand.nextFloat() - .5f) * .5f, 0, 1 / 16f, 0);
				}
				if (world.isRemote)
					return null;
				return InWorldProcessing.applyProcessing(transported, belt, processingType);
			});
		}

	}
}
