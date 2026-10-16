package com.friendlist.mixin;

import com.friendlist.FriendManager;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin {
    // For friends, the armor renderer sees dyed leather armor instead of the real equipment.
    @Redirect(
            method = "renderArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private ItemStack friendlist$leather(LivingEntity entity, EquipmentSlot slot) {
        if (entity instanceof PlayerEntity p && FriendManager.isFriend(p.getGameProfile().getName())) {
            ItemStack leather = FriendManager.leatherFor(slot);
            if (!leather.isEmpty()) {
                return leather;
            }
        }
        return entity.getEquippedStack(slot);
    }
}
