package me.lordsaad.wizardry.spells.modules.modifiers;

import me.lordsaad.wizardry.api.modules.Module;
import me.lordsaad.wizardry.spells.modules.ModuleType;
import net.minecraft.nbt.NBTTagCompound;

public class ModulePierce extends Module
{
	public ModulePierce()
	{
		
	}

	@Override
	public ModuleType getType()
	{
		return ModuleType.MODIFIER;
	}

	@Override
	public NBTTagCompound getModuleData()
	{
		return null;
	}
}