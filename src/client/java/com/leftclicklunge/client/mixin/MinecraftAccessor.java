package com.leftclicklunge.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Lets us call the game's own (private) left-click routine, so the lunge jab goes through
 * exactly the same code path as a real left click.
 */
@Mixin(Minecraft.class)
public interface MinecraftAccessor {
	@Invoker("startAttack")
	boolean leftclicklunge$startAttack();
}
