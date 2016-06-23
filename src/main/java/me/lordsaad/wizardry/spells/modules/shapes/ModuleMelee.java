package me.lordsaad.wizardry.spells.modules.shapes;

import me.lordsaad.wizardry.api.modules.Module;
import me.lordsaad.wizardry.spells.modules.ModuleType;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleMelee extends Module
{
	private Module[] modules;
	
	public ModuleMelee(Module... modules)
	{
		this.modules = modules;
	}
	
    @Override
    public ModuleType getType() {
        return ModuleType.SHAPE;
    }
    
	@Override
	public NBTTagCompound getModuleData()
	{
		return null;
	}
}