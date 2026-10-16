package com.friendlist.mixin;

import com.friendlist.FriendManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    // Default leather armor color
    @Unique
    private static final int FRIENDLIST_COLOR = 0xA06540;

    @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
    private void friendlist$glow(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && friendlist$isFriend()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTeamColorValue", at = @At("RETURN"), cancellable = true)
    private void friendlist$color(CallbackInfoReturnable<Integer> cir) {
        if (friendlist$isFriend()) {
            cir.setReturnValue(FRIENDLIST_COLOR);
        }
    }

    @Unique
    private boolean friendlist$isFriend() {
        Entity self = (Entity) (Object) this;
        return self instanceof PlayerEntity p
                && p.getWorld().isClient
                && p != MinecraftClient.getInstance().player
                && FriendManager.isFriend(p.getGameProfile().getName());
    }
}
