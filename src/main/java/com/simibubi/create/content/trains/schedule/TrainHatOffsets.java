package com.simibubi.create.content.trains.schedule;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.world.phys.Vec3;

public class TrainHatOffsets {

	// sorry
	public static Vec3 getOffset(EntityModel<?> model) {

		float x = 0;
		float y = 0;
		float z = 0;
		
		if (model instanceof AgeableListModel) {
			if (model instanceof WolfModel) {
				x += .5f;
				y += 1.5f;
				z += .25f;
			} else if (model instanceof OcelotModel) {
				y += 1f;
				z -= .25f;
			} else if (model instanceof ChickenModel) {
				z -= .25f;
			} else if (model instanceof FoxModel) {
				x += .5f;
				y += 2f;
				z -= 1f;
			} else if (model instanceof QuadrupedModel) {
				y += 2f;

				if (model instanceof CowModel)
					z -= 1.25f;
				else if (model instanceof PandaModel)
					z += .5f;
				else if (model instanceof PigModel)
					z -= 2f;
				else if (model instanceof SheepModel) {
					z -= .75f;
					y -= 1.5f;

				}
			} else if (model instanceof HoglinModel)
				z -= 4.5f;
			else if (model instanceof BeeModel) {
				z -= .75f;
				y -= 4f;
			} else if (model instanceof AxolotlModel) {
				z -= 5f;
				y += .5f;
			}
		}
		
		if (model instanceof HierarchicalModel) {
			if (model instanceof BlazeModel)
				y += 4;
			else if (model instanceof GuardianModel)
				y += 20;
			else if (model instanceof IronGolemModel) {
				z -= 1.5f;
				y -= 2f;
			} else if (model instanceof SnowGolemModel) {
				z -= .75f;
				y -= 3f;
			} else if (model instanceof SlimeModel || model instanceof LavaSlimeModel) {
				y += 22;
			} else if (model instanceof SpiderModel) {
				z -= 3.5f;
				y += 2f;
			} else if (model instanceof ParrotModel) {
				z -= 1.5f;
			} else if (model instanceof WardenModel) {
				y += 3.5f;
				z += .5f;
			} else if (model instanceof FrogModel) {
				y += 16.75f;
				z -= .25f;
			}
		}

		return new Vec3(x, y, z);

	}

}
