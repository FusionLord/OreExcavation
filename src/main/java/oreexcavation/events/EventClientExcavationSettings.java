package oreexcavation.events;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.eventbus.api.Event;

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

    private final CompoundNBT tags;

    public EventClientExcavationSettings(final CompoundNBT tags)
    {
        this.tags = tags;
    }

    public int getTag(TAG tag)
    {
        return this.tags.getInt(tag.getKey());
    }

    public void setTag(TAG tag, int value)
    {
        this.tags.putInt(tag.getKey(), value);
    }

    public void removeShape() {
        this.tags.remove("shape");
    }
}
