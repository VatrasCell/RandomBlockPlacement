package de.guntram.mcmod.randomblockplacement.mixins;

import de.guntram.mcmod.randomblockplacement.RandomBlockPlacement;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class PlayerInteractBlockMixin {

    @Inject(method="useItemOn", at=@At("RETURN"))
    public void onPlayerInteractBlockSuccessfully(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable cir) {
        RandomBlockPlacement.getInstance().onPlayerInteract();
    }

}
