package com.teamwizardry.wizardry.asm;

import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.events.EntityMoveWithHeadingEvent;
import com.teamwizardry.wizardry.api.events.EntityPostMoveEvent;
import com.teamwizardry.wizardry.api.events.EntityRenderShadowAndFireEvent;
import com.teamwizardry.wizardry.api.events.PlayerClipEvent;
import com.teamwizardry.wizardry.common.world.underworld.UnderworldChunkWrapper;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by LordSaad.
 */
public class WizardryASMHooks {

	public static boolean playerClipEventHook(boolean isSpectator, EntityPlayer player) {
		if (isSpectator) return true;

		PlayerClipEvent event = new PlayerClipEvent(player);
		MinecraftForge.EVENT_BUS.post(event);
		return event.noClip;
	}

	public static boolean entityPreMoveHook(Entity entity, MoverType type, double x, double y, double z) {
		EntityPostMoveEvent event = new EntityPostMoveEvent(entity, type, x, y, z);
		MinecraftForge.EVENT_BUS.post(event);
		return !event.override;
	}

	public static boolean travel(EntityLivingBase entity, float strafe, float vertical, float forward) {
		EntityMoveWithHeadingEvent event = new EntityMoveWithHeadingEvent(entity, strafe, vertical, forward);
		MinecraftForge.EVENT_BUS.post(event);
		return !event.override;
	}

	public static boolean entityRenderShadowAndFire(Entity entity) {
		EntityRenderShadowAndFireEvent event = new EntityRenderShadowAndFireEvent(entity);
		MinecraftForge.EVENT_BUS.post(event);
		return !event.override;
	}

	@SideOnly(Side.CLIENT)
	public static Chunk wrapChunk(Chunk chunk, ChunkProviderClient chunkProvider) {
		if (chunk == null) return null;
		if (chunkProvider.world.provider.getDimensionType() == Wizardry.underWorld)
			return new UnderworldChunkWrapper(chunk);
		return chunk;
	}

	public static Chunk wrapChunk(Chunk chunk, ChunkProviderServer chunkProvider) {
		if (chunk == null) return null;
		if (chunkProvider.world.provider.getDimensionType() == Wizardry.underWorld)
			return new UnderworldChunkWrapper(chunk);
		return chunk;
	}
}
