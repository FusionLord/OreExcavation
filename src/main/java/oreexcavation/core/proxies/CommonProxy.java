package oreexcavation.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import oreexcavation.core.OreExcavation;
import oreexcavation.handlers.EventHandler;
import oreexcavation.network.PacketExcavation;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		EventHandler handler = new EventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		FMLCommonHandler.instance().bus().register(handler);
		
		OreExcavation.instance.network.registerMessage(PacketExcavation.ServerHandler.class, PacketExcavation.class, 0, Side.SERVER);
	}
}
