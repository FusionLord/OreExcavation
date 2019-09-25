package oreexcavation.core;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import oreexcavation.handlers.MiningScheduler;
import oreexcavation.undo.RestoreResult;

import javax.annotation.Nonnull;

public class CommandUndo extends CommandBase
{
	@Nonnull
	@Override
	public String getName()
	{
		return "undo_excavation";
	}
	
	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender)
	{
		return "/undo_excavation";
	}
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}
	
	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException
	{
		if(args.length != 0 || !(sender instanceof EntityPlayer))
		{
			throw new CommandException(getUsage(sender));
		}
		
		if(ExcavationSettings.maxUndos <= 0)
		{
			throw new CommandException("oreexcavation.undo.failed.disabled");
		}
		
		EntityPlayer player = (EntityPlayer)sender;
		RestoreResult result = MiningScheduler.INSTANCE.attemptUndo(player, false);
		
		switch(result)
		{
			case INVALID_XP:
				sender.sendMessage(new TextComponentTranslation("oreexcavation.undo.failed.xp"));
				break;
			case INVALID_ITEMS:
				sender.sendMessage(new TextComponentTranslation("oreexcavation.undo.failed.items"));
				break;
			case NO_UNDO_HISTORY:
				sender.sendMessage(new TextComponentTranslation("oreexcavation.undo.failed.no_history"));
				break;
			case OBSTRUCTED:
				sender.sendMessage(new TextComponentTranslation("oreexcavation.undo.failed.obstructed"));
				break;
			case SUCCESS:
				sender.sendMessage(new TextComponentTranslation("oreexcavation.undo.success"));
				break;
		}
	}
}
