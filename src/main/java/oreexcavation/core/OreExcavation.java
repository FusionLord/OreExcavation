package oreexcavation.core;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import oreexcavation.core.proxies.CommonProxy;
import oreexcavation.handlers.ConfigHandler;
import org.apache.logging.log4j.Logger;

@Mod(modid = OreExcavation.MODID, name = OreExcavation.NAME, guiFactory = OreExcavation.MODID + ".handlers.ConfigGuiFactory")
public class OreExcavation
{
    public static final String MODID = "oreexcavation";
    public static final String NAME = "OreExcavation";
    public static final String PROXY = MODID + ".core.proxies";
    public static final String CHANNEL = "OE_CHAN";
	
	@Instance(MODID)
	public static OreExcavation instance;
	
	@SidedProxy(clientSide = PROXY + ".ClientProxy", serverSide = PROXY + ".CommonProxy")
	public static CommonProxy proxy;
	public SimpleNetworkWrapper network;
	public static Logger logger;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	logger = event.getModLog();
    	network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
    	
    	ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile(), true);
    	ConfigHandler.initConfigs();
    	
    	proxy.registerHandlers();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }
    
    @EventHandler
    public void onServerStart(FMLServerStartingEvent event)
    {
    	MinecraftServer server = event.getServer();
    	((ServerCommandManager)server.getCommandManager()).registerCommand(new CommandUndo());
    }
}
