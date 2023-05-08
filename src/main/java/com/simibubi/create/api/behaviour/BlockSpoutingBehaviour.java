package com.simibubi.create.api.behaviour;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.simibubi.create.Create;
import com.simibubi.create.compat.tconstruct.SpoutCasting;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

public abstract class BlockSpoutingBehaviour {

	private static final Map<ResourceLocation, BlockSpoutingBehaviour> BLOCK_SPOUTING_BEHAVIOURS = new HashMap<>();

	public static void addCustomSpoutInteraction(ResourceLocation resourceLocation,
		BlockSpoutingBehaviour movementBehaviour) {
		BLOCK_SPOUTING_BEHAVIOURS.put(resourceLocation, movementBehaviour);
	}

	public static void forEach(Consumer<? super BlockSpoutingBehaviour> accept) {
		BLOCK_SPOUTING_BEHAVIOURS.values()
			.forEach(accept);
	}

	/**
	 * While idle, Spouts will call this every tick with simulate == true <br>
	 * When fillBlock returns &gt; 0, the Spout will start its animation cycle <br>
	 * <br>
	 * During this animation cycle, fillBlock is called once again with simulate == false but only on the relevant SpoutingBehaviour <br>
	 * When fillBlock returns &gt; 0 once again, the Spout will drain its content by the returned amount of units <br>
	 * Perform any other side-effects in this method <br>
	 * This method is called server-side only (except in ponder) <br>
	 * 
	 * @param world
	 * @param pos            of the affected block
	 * @param spout
	 * @param availableFluid do not modify, return the amount to be subtracted instead
	 * @param simulate       whether the spout is testing or actually performing this behaviour
	 * @return amount filled into the block, 0 to idle/cancel
	 */
	public abstract int fillBlock(Level world, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid,
		boolean simulate);

	public static void registerDefaults() {
		addCustomSpoutInteraction(Create.asResource("ticon_casting"), new SpoutCasting());
	}

}
