package rl.sage.rangerlevels.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class LevelStorage implements Capability.IStorage<ILevel> {

    @Override
    public INBT writeNBT(Capability<ILevel> capability, ILevel instance, Direction side) {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("level", instance.getLevel());
        tag.putInt("exp", instance.getExp());
        tag.putFloat("playerMultiplier", instance.getPlayerMultiplier()); // guarda el multiplicador
        return tag;
    }

    @Override
    public void readNBT(Capability<ILevel> capability, ILevel instance, Direction side, INBT nbt) {
        if (!(nbt instanceof CompoundNBT)) return;
        CompoundNBT tag = (CompoundNBT) nbt;
        instance.setLevel(tag.getInt("level"));
        instance.setExp(tag.getInt("exp"));
        instance.setPlayerMultiplier(tag.getFloat("playerMultiplier"));    // lee el multiplicador
    }
}
