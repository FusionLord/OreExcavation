package oreexcavation.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import oreexcavation.core.ExcavationSettings;

public class ToolEffectiveCheck
{
	public static boolean canHarvestBlock(World world, IBlockState state, BlockPos pos, EntityPlayer player)
	{
		if(world == null || state == null || pos == null || player == null)
		{
			return false;
		}
		
		if(!ExcavationSettings.openHand && player.getHeldItem(EnumHand.MAIN_HAND) == null)
		{
			return false;
		} else if(ExcavationSettings.toolClass)
		{
			ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
			
			if(held == null)
			{
				return false;
			}
			
			if(held.getItem() instanceof ItemShears && state.getBlock() instanceof IShearable)
			{
				return true;
			}
			
			for(String type : held.getItem().getToolClasses(held))
			{
				if(type.equalsIgnoreCase(state.getBlock().getHarvestTool(state)) && held.getItem().getHarvestLevel(held, type, player, state) >= state.getBlock().getHarvestLevel(state))
				{
					return true;
				} else if(state.getBlock().isToolEffective(type, state))
				{
					return true;
				}
			}
			
			return false;
		} else if(ExcavationSettings.altTools)
		{
			ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
			
			if(held != null && held.getStrVsBlock(state) > 1F)
			{
				return true;
			}
		}
		
		return state.getBlock().canHarvestBlock(world, pos, player);
	}
}
