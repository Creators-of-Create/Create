package com.simibubi.create.modules.contraptions.relays.belt;

import com.simibubi.create.foundation.utility.data.SpecialBlockStateGen;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Part;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelFile;

public class BeltGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return state.get(BeltBlock.SLOPE) == Slope.VERTICAL ? 90 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		Boolean casing = state.get(BeltBlock.CASING);
		Slope slope = state.get(BeltBlock.SLOPE);

		boolean flip = casing && slope == Slope.UPWARD;
		int horizontalAngle = (int) state.get(BeltBlock.HORIZONTAL_FACING)
			.getHorizontalAngle();
		return (360 + horizontalAngle + (flip ? 180 : 0)) % 360;
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		Boolean casing = state.get(BeltBlock.CASING);
		BeltBlock.Part part = state.get(BeltBlock.PART);
		Direction direction = state.get(BeltBlock.HORIZONTAL_FACING);
		Slope slope = state.get(BeltBlock.SLOPE);
		boolean diagonal = slope == Slope.UPWARD || slope == Slope.DOWNWARD;
		boolean vertical = slope == Slope.VERTICAL;
		boolean pulley = part == Part.PULLEY;
		boolean negative = direction.getAxisDirection() == AxisDirection.NEGATIVE;

		if (!casing && pulley)
			part = Part.MIDDLE;

		if ((!casing && vertical && negative || casing && diagonal && negative != (direction.getAxis() == Axis.X))
			&& part != Part.MIDDLE && !pulley)
			part = part == Part.END ? Part.START : Part.END;

		if (!casing && vertical)
			slope = Slope.HORIZONTAL;

		String path = "block/" + (casing ? "belt_casing/" : "belt/");
		String slopeName = slope.getName();
		String partName = part.getName();

		if (casing && diagonal)
			slopeName = "diagonal";

		ResourceLocation location = prov.modLoc(path + slopeName + "_" + partName);
		return prov.models()
			.getExistingFile(location);
	}

}
