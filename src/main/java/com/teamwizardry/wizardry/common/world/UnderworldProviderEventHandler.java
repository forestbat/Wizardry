package com.teamwizardry.wizardry.common.world;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by TheCodeWarrior
 */
public class UnderworldProviderEventHandler {
	public static final UnderworldProviderEventHandler INSTANCE = new UnderworldProviderEventHandler();
	private static Set<WorldProviderUnderWorld> handlers = Collections.newSetFromMap(new WeakHashMap<>());
	private static Set<WorldProviderUnderWorld> tmpSet = new HashSet<>();
	
	public static void add(WorldProviderUnderWorld e) {
		handlers.add(e);
	}
	
	private UnderworldProviderEventHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void load(ChunkEvent.Load event) {
		Set<WorldProviderUnderWorld> tmpLocal = handlers;
		handlers = tmpSet;
		for(WorldProviderUnderWorld e : tmpLocal) {
			e.wrapChunk(event.getChunk());
		}
		handlers = tmpLocal;
		handlers.addAll(tmpSet);
		tmpSet.clear();
	}
}
