package com.simibubi.create.content.curiosities.frames;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class SpecialCopycatPanelBlockState extends SpecialBlockStateGen {

	private String name;

	public SpecialCopycatPanelBlockState(String name) {
		this.name = name;
	}
	
	@Override
	protected int getXRotation(BlockState state) {
		return facing(state) == Direction.UP ? 0 : facing(state) == Direction.DOWN ? 180 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return horizontalAngle(facing(state));
	}

	private Direction facing(BlockState state) {
		return state.getValue(DirectionalBlock.FACING);
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		BlockModelProvider models = prov.models();
		return facing(state).getAxis() == Axis.Y
			? models.getExistingFile(prov.modLoc("block/copycat_panel/" + name + "_vertical"))
			: models.getExistingFile(prov.modLoc("block/copycat_panel/" + name));
	}

}