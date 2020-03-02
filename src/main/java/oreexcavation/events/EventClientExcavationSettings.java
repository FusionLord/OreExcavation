package oreexcavation.events;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;

public class EventClientExcavationSettings extends Event
{
    public enum TAG
    {
        SHAPE("shape"),
        ORIGIN("origin"),
        DEPTH("depth"),
        SIDE("side"),
        STATEID("stateId"),
        X("x"),
        Y("y"),
        Z("z");

        String key;

        TAG(String key)
        {
            this.key = key;
        }

        public String getKey()
        {
            return this.key;
        }
    }

    private final NBTTagCompound tags;

    public EventClientExcavationSettings(final NBTTagCompound tags)
    {
        this.tags = tags;
    }

    public int getTag(TAG tag)
    {
        return this.tags.getInteger(tag.getKey());
    }

    public void setTag(TAG tag, int value)
    {
        this.tags.setInteger(tag.getKey(), value);
    }

    public void removeShape() {
        this.tags.removeTag("shape");
    }
}
