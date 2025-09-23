package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlock;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.DyeColor;

public class DebugHatsCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("debugHats")
			.requires(cs -> cs.hasPermission(4))
			.then(Commands.argument("pos", BlockPosArgument.blockPos())
				.executes((ctx) -> {
					BlockPos origin = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
					BlockPos.MutableBlockPos pos = origin.mutable();
					for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
						ServerLevel level = ctx.getSource().getLevel();

						Entity entity = entityType.create(level);
						if (entity instanceof LivingEntity) {
							level.setBlockAndUpdate(pos, AllBlocks.SEATS.get(DyeColor.RED).getDefaultState());
							level.setBlockAndUpdate(pos.east(), AllBlocks.STOCK_TICKER.getDefaultState().setValue(StockTickerBlock.FACING, Direction.EAST));

							entity.moveTo(pos.getCenter());

							if (entity instanceof Mob mob)
								mob.setNoAi(true);

							entity.setInvulnerable(true);
							entity.setSilent(true);

							level.tryAddFreshEntityWithPassengers(entity);

							SeatBlock.sitDown(level, pos, entity);

							pos.move(0, 0, 2);
						}
					}

					ctx.getSource().sendSuccess(() -> Component.literal("Placed entities"), true);
					return 1;
				}));

	}
}
