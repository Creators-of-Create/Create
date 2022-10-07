package com.simibubi.create.foundation.mixin;

import com.simibubi.create.content.logistics.trains.entity.CarriageContraptionEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractPiglin.class)
public abstract class PiglinZombification extends Entity {

    public PiglinZombification(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(at = @At("HEAD"), method = "isConverting", cancellable = true)
    private void dontDriveAndZombify(CallbackInfoReturnable<Boolean> cir) {
        if(this.getVehicle() instanceof CarriageContraptionEntity) {
            cir.setReturnValue(false);
        }
    }
}
