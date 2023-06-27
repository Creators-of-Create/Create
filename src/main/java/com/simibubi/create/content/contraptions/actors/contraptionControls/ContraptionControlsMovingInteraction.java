package com.simibubi.create.content.contraptions.actors.contraptionControls;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;

import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement.ElevatorFloorSelection;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.elevator.ElevatorContraption;
import com.simibubi.create.content.contraptions.elevator.ElevatorTargetFloorPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class ContraptionControlsMovingInteraction extends MovingInteractionBehaviour {

	@Override
	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		Contraption contraption = contraptionEntity.getContraption();

		MutablePair<StructureBlockInfo, MovementContext> actor = contraption.getActorAt(localPos);
		if (actor == null)
			return false;
		MovementContext ctx = actor.right;
		if (ctx == null)
			return false;
		if (contraption instanceof ElevatorContraption ec)
			return elevatorInteraction(localPos, contraptionEntity, ec, ctx);
		if (contraptionEntity.level().isClientSide()) {
			if (contraption.presentBlockEntities.get(ctx.localPos) instanceof ContraptionControlsBlockEntity cbe)
				cbe.pressButton();
			return true;
		}

		ItemStack filter = ContraptionControlsMovement.getFilter(ctx);
		boolean disable = true;
		boolean invert = false;

		List<ItemStack> disabledActors = contraption.getDisabledActors();
		for (Iterator<ItemStack> iterator = disabledActors.iterator(); iterator.hasNext();) {
			ItemStack presentFilter = iterator.next();
			boolean sameFilter = ContraptionControlsMovement.isSameFilter(presentFilter, filter);
			if (presentFilter.isEmpty()) {
				iterator.remove();
				disable = false;
				if (!sameFilter)
					invert = true;
				continue;
			}
			if (!sameFilter)
				continue;
			iterator.remove();
			disable = false;
			break;
		}

		if (invert) {
			for (MutablePair<StructureBlockInfo, MovementContext> pair : contraption.getActors()) {
				MovementBehaviour behaviour = AllMovementBehaviours.getBehaviour(pair.left.state());
				if (behaviour == null)
					continue;
				ItemStack behaviourStack = behaviour.canBeDisabledVia(pair.right);
				if (behaviourStack == null)
					continue;
				if (ContraptionControlsMovement.isSameFilter(behaviourStack, filter))
					continue;
				if (contraption.isActorTypeDisabled(behaviourStack))
					continue;
				disabledActors.add(behaviourStack);
				send(contraptionEntity, behaviourStack, true);
			}
		}

		if (filter.isEmpty())
			disabledActors.clear();
		if (disable)
			disabledActors.add(filter);

		contraption.setActorsActive(filter, !disable);
		ContraptionControlsBlockEntity.sendStatus(player, filter, !disable);
		send(contraptionEntity, filter, disable);

		AllSoundEvents.CONTROLLER_CLICK.play(player.level(), null,
			BlockPos.containing(contraptionEntity.toGlobalVector(Vec3.atCenterOf(localPos), 1)), 1, disable ? 0.8f : 1.5f);

		return true;
	}

	private void send(AbstractContraptionEntity contraptionEntity, ItemStack filter, boolean disable) {
		AllPackets.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> contraptionEntity),
			new ContraptionDisableActorPacket(contraptionEntity.getId(), filter, !disable));
	}

	private boolean elevatorInteraction(BlockPos localPos, AbstractContraptionEntity contraptionEntity,
		ElevatorContraption contraption, MovementContext ctx) {
		Level level = contraptionEntity.level();
		if (!level.isClientSide()) {
			BlockPos pos = BlockPos.containing(contraptionEntity.toGlobalVector(Vec3.atCenterOf(localPos), 1));
			AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, 1.5f);
			AllSoundEvents.CONTRAPTION_ASSEMBLE.play(level, null, pos, 0.75f, 0.8f);
			return true;
		}
		if (!(ctx.temporaryData instanceof ElevatorFloorSelection efs))
			return false;
		if (efs.currentTargetY == contraption.clientYTarget)
			return true;

		AllPackets.getChannel().sendToServer(new ElevatorTargetFloorPacket(contraptionEntity, efs.currentTargetY));
		if (contraption.presentBlockEntities.get(ctx.localPos) instanceof ContraptionControlsBlockEntity cbe)
			cbe.pressButton();
		return true;
	}

}
