package com.simibubi.create.content.fluids.pipes;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.pipes.StraightPipeBlockEntity.StraightPipeFluidTransportBehaviour;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.fluids.FluidStack;

public class SmartFluidPipeBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, Clearable {
	private FilteringBehaviour filter;

	public SmartFluidPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(new SmartPipeBehaviour(this));
		behaviours.add(filter = new FilteringBehaviour(this, new SmartPipeFilterSlot()).forFluids()
			.withCallback(this::onFilterChanged));
		registerAwardables(behaviours, FluidPropagator.getSharedTriggers());
	}

	@Override
	public void clearContent() {
		filter.setFilter(ItemStack.EMPTY);
	}

	private void onFilterChanged(ItemStack newFilter) {
		if (!level.isClientSide)
			FluidPropagator.propagateChangedPipe(level, worldPosition, getBlockState());
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		CreateLang.translate("tooltip.smart_fluid_pipe.header")
			.forGoggles(tooltip);
		
		boolean hasFilter = addFilterTooltip(tooltip);
		if (!hasFilter) {
			tooltip.remove(0);
			return false;
		}
		return true;
	}

	private boolean addFilterTooltip(List<Component> tooltip) {
		// Get filter blocks and items
		ItemStack filterStack = filter == null ? ItemStack.EMPTY : filter.getFilter();
		// Verify if the smart fluid pipe has a filter
		if (filterStack.isEmpty())
			return false;

		tooltip.add(CommonComponents.EMPTY);
		List<Component> filterSummary;
		// If the filter is an item filter, use its summary, otherwise just show the item
		if (filterStack.getItem() instanceof FilterItem filterItem) {
			filterSummary = filterItem.makeSummary(filterStack);
		} else {
			CreateLang.translate("gui.filter.allow_item")
				.style(ChatFormatting.GOLD)
				.forGoggles(tooltip);
			filterSummary = List.of(Component.literal("- ").append(filterStack.getHoverName())
				.withStyle(ChatFormatting.GRAY));
		}
		// If the filter summary is not empty, add it to the tooltip
		if (!filterSummary.isEmpty()) {
			// Add the filter type (allow or deny) in the goggles tooltip format
			CreateLang.builder()
				.add(filterSummary.get(0))
				.forGoggles(tooltip);
			// Add the filter blocks and items in the goggles tooltip format
			for (int i = 1; i < filterSummary.size(); i++)
				CreateLang.builder()
					.add(filterSummary.get(i))
					.forGoggles(tooltip, 1);
		} else {
			CreateLang.translate("gui.filter.empty")
				.style(ChatFormatting.DARK_GRAY)
				.forGoggles(tooltip);
		}
		return true;
	}

	class SmartPipeBehaviour extends StraightPipeFluidTransportBehaviour {
		public SmartPipeBehaviour(SmartBlockEntity be) {
			super(be);
		}

		@Override
		public boolean canPullFluidFrom(FluidStack fluid, BlockState state, Direction direction) {
			if (fluid.isEmpty() || filter != null && filter.test(fluid))
				return super.canPullFluidFrom(fluid, state, direction);
			return false;
		}

		@Override
		public boolean canHaveFlowToward(BlockState state, Direction direction) {
			return state.getBlock() instanceof SmartFluidPipeBlock
				&& SmartFluidPipeBlock.getPipeAxis(state) == direction.getAxis();
		}
	}

	static class SmartPipeFilterSlot extends ValueBoxTransform {
		@Override
		public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
			AttachFace face = state.getValue(SmartFluidPipeBlock.FACE);
			float y = face == AttachFace.CEILING ? 0.55f : face == AttachFace.WALL ? 11.4f : 15.45f;
			float z = face == AttachFace.CEILING ? 4.6f : face == AttachFace.WALL ? 0.55f : 4.625f;
			return VecHelper.rotateCentered(VecHelper.voxelSpace(8, y, z), angleY(state), Axis.Y);
		}

		@Override
		public float getScale() {
			return super.getScale() * 1.02f;
		}

		@Override
		public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
			AttachFace face = state.getValue(SmartFluidPipeBlock.FACE);
			TransformStack.of(ms)
				.rotateYDegrees(angleY(state))
				.rotateXDegrees(face == AttachFace.CEILING ? -45 : 45);
		}

		protected float angleY(BlockState state) {
			AttachFace face = state.getValue(SmartFluidPipeBlock.FACE);
			float horizontalAngle = AngleHelper.horizontalAngle(state.getValue(SmartFluidPipeBlock.FACING));
			if (face == AttachFace.WALL)
				horizontalAngle += 180;
			return horizontalAngle;
		}
	}
}
