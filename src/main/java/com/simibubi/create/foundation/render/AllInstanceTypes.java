package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.api.instance.InstanceType;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.actors.flwdata.ActorInstance;
import com.simibubi.create.content.contraptions.actors.flwdata.ActorType;
import com.simibubi.create.content.kinetics.base.flwdata.BeltInstance;
import com.simibubi.create.content.kinetics.base.flwdata.BeltType;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingType;
import com.simibubi.create.content.logistics.flwdata.FlapData;
import com.simibubi.create.content.logistics.flwdata.FlapType;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AllInstanceTypes {

	public static final InstanceType<RotatingInstance> ROTATING = new RotatingType();
	public static final InstanceType<BeltInstance> BELTS = new BeltType();
	public static final InstanceType<ActorInstance> ACTORS = new ActorType();
	public static final InstanceType<FlapData> FLAPS = new FlapType();

	public static class Locations {
		public static final ResourceLocation ROTATING = Create.asResource("rotating");
		public static final ResourceLocation BELTS = Create.asResource("belts");
		public static final ResourceLocation ACTORS = Create.asResource("actors");
		public static final ResourceLocation FLAPS = Create.asResource("flaps");
	}
}
