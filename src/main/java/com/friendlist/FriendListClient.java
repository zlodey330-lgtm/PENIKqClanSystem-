package com.friendlist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class FriendListClient implements ClientModInitializer {
    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        FriendManager.load();

        // Registered in vanilla Controls settings, so it can be rebound. Default: M.
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.friendlist.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.friendlist"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (client.currentScreen == null && client.player != null) {
                    client.setScreen(new FriendScreen());
                }
            }
        });

        // Friends can't be attacked.
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient
                    && entity instanceof PlayerEntity target
                    && FriendManager.isFriend(target.getGameProfile().getName())) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }
}
