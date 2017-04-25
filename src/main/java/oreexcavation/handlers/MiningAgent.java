package oreexcavation.handlers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.BlockSnapshot;
import oreexcavation.core.ExcavationSettings;
import oreexcavation.core.OreExcavation;
import oreexcavation.overrides.ToolOverride;
import oreexcavation.overrides.ToolOverrideHandler;
import oreexcavation.shapes.ExcavateShape;
import oreexcavation.undo.BlockHistory;
import oreexcavation.undo.ExcavateHistory;
import oreexcavation.utils.BigItemStack;
import oreexcavation.utils.ToolEffectiveCheck;
import oreexcavation.utils.XPHelper;
import org.apache.logging.log4j.Level;
import com.google.common.base.Stopwatch;

public class MiningAgent
{
	private ItemStack blockStack = null;
	private Item origTool = null;
	private List<BlockPos> mined = new ArrayList<BlockPos>();
	private List<BlockPos> scheduled = new ArrayList<BlockPos>();
	private final EntityPlayerMP player;
	private final BlockPos origin;
	private EnumFacing facing = EnumFacing.SOUTH;
	private ExcavateShape shape = null;
	private final ExcavateHistory history;
	
	private IBlockState state;
	private Block block;
	private int meta;
	
	private ToolOverride toolProps;
	
	private boolean subtypes = true; // Ignore metadata
	
	private List<BigItemStack> drops = new ArrayList<BigItemStack>();
	private int experience = 0;
	
	private Stopwatch timer;
	
	public MiningAgent(EntityPlayerMP player, BlockPos origin, IBlockState state)
	{
		this.timer = Stopwatch.createUnstarted();
		this.player = player;
		this.origin = origin;
		
		this.state = state;
		this.block = state.getBlock();
		this.meta = block.getMetaFromState(state);
		
		this.history = new ExcavateHistory(player.world.provider.getDimension());
	}
	
	public void init()
	{
		if(m_createStack != null)
		{
			try
			{
				blockStack = (ItemStack)m_createStack.invoke(block, state);
				
				if(blockStack == null || blockStack.isEmpty())
				{
					blockStack = null;
				}
			} catch(Exception e){}
		}
		
		this.subtypes = blockStack == null? true : !blockStack.getHasSubtypes();
		
		ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
		origTool = held == null || held.isEmpty()? null : held.getItem();
		
		if(held == null)
		{
			toolProps = new ToolOverride("", -1);
		} else
		{
			toolProps = ToolOverrideHandler.INSTANCE.getOverride(held);
			
			if(toolProps == null)
			{
				toolProps = new ToolOverride("", -1);
			}
		}
		
		for(int i = -1; i <= 1; i++)
		{
			for(int j = -1; j <= 1; j++)
			{
				for(int k = -1; k <= 1; k++)
				{
					appendBlock(origin.add(i, j, k));
				}
			}
		}
	}
	
	public MiningAgent setShape(ExcavateShape shape, EnumFacing facing)
	{
		this.shape = shape;
		this.facing = facing;
		return this;
	}
	
	/**
	 * Returns true if the miner is no longer valid or has completed
	 */
	public boolean tickMiner()
	{
		if(origin == null || player == null || !player.isEntityAlive() || mined.size() >= toolProps.getLimit())
		{
			return true;
		}
		
		timer.reset();
		timer.start();
		
		for(int n = 0; scheduled.size() > 0; n++)
		{
			if(n >= toolProps.getSpeed() || mined.size() >= toolProps.getLimit())
			{
				break;
			}
			
			if(ExcavationSettings.tpsGuard && timer.elapsed(TimeUnit.MILLISECONDS) > 40)
			{
				break;
			}
			
			ItemStack heldStack = player.getHeldItem(EnumHand.MAIN_HAND);
			Item heldItem = heldStack == null || heldStack.isEmpty()? null : heldStack.getItem();
			
			if(heldItem != origTool)
			{
				// Original tool has been swapped or broken
				timer.stop();
				return true;
			} else if(!hasEnergy(player))
			{
				timer.stop();
				return true;
			}
			
			BlockPos pos = scheduled.remove(0);
			
			if(pos == null)
			{
				continue;
			} else if(player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > toolProps.getRange())
			{
				mined.add(pos);
				continue;
			}
			
			IBlockState s = player.world.getBlockState(pos);
			Block b = s.getBlock();
			int m = b.getMetaFromState(s);
			
			boolean flag = b == block && (subtypes || m == meta);
			
			if(!flag && blockStack != null)
			{
				ItemStack stack = null;
				
				try
				{
					stack = (ItemStack)m_createStack.invoke(b, s);
				} catch(Exception e){}
				
				if(stack != null && !stack.isEmpty() && stack.getItem() == blockStack.getItem() && stack.getItemDamage() == blockStack.getItemDamage())
				{
					flag = true;
				}
			}
			
			if(flag)
			{
				player.world.captureBlockSnapshots = true;
				player.world.capturedBlockSnapshots.clear();
				
				if(!(ExcavationSettings.ignoreTools || ToolEffectiveCheck.canHarvestBlock(player.world, s, pos, player)))
				{
					mined.add(pos);
					continue;
				} else if(player.interactionManager.tryHarvestBlock(pos))
				{
					player.world.captureBlockSnapshots = false;
					
					EventHandler.captureAgent = null;
					while(player.world.capturedBlockSnapshots.size() > 0)
					{
						BlockSnapshot snap = player.world.capturedBlockSnapshots.get(0);
						if(pos.equals(snap.getPos()))
						{
							history.addRecordedBlock(new BlockHistory(snap));
						}
						player.world.capturedBlockSnapshots.remove(0);
						
						player.world.markAndNotifyBlock(snap.getPos(), player.world.getChunkFromChunkCoords(snap.getPos().getX() >> 4, snap.getPos().getZ() >> 4), snap.getReplacedBlock(), snap.getCurrentBlock(), snap.getFlag());
					}
					EventHandler.captureAgent = this;
					
					if(!player.isCreative())
					{
						player.getFoodStats().addExhaustion(toolProps.getExaustion());
						
						if(toolProps.getExperience() > 0)
						{
							XPHelper.addXP(player, -toolProps.getExperience(), false);
						}
					}
					
					for(int i = -1; i <= 1; i++)
					{
						for(int j = -1; j <= 1; j++)
						{
							for(int k = -1; k <= 1; k++)
							{
								appendBlock(pos.add(i, j, k));
							}
						}
					}
				}
				
				mined.add(pos);
			}
		}
		
		timer.stop();
		
		if(!player.isCreative())
		{
			XPHelper.syncXP(player);
		}
		
		return scheduled.size() <= 0 || mined.size() >= toolProps.getLimit();
	}
	
	/**
	 * Appends a block position to the miners current pass
	 */
	public void appendBlock(BlockPos pos)
	{
		if(pos == null || mined.contains(pos) || scheduled.contains(pos))
		{
			return;
		} else if(player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > toolProps.getRange() || !player.world.getWorldBorder().contains(pos) || !player.canPlayerEdit(pos, facing, player.getHeldItemMainhand()))
		{
			return;
		} else if(shape != null && !shape.isValid(origin, pos, facing))
		{
			return;
		}
		
		scheduled.add(pos);
	}
	
	private boolean hasEnergy(EntityPlayerMP player)
	{
		return (toolProps.getExaustion() <= 0 || player.getFoodStats().getFoodLevel() > 0) && (toolProps.getExperience() <= 0 || XPHelper.getPlayerXP(player) >= toolProps.getExperience());
	}
	
	public void dropEverything()
	{
		// Temporarily halt any ongoing captures
		MiningAgent ca = EventHandler.captureAgent;
		EventHandler.captureAgent = null;
		
		history.setRecievedStacks(drops);
		history.setRecievedXP(experience);
		
		for(BigItemStack bigStack : drops)
		{
			for(ItemStack stack : bigStack.getCombinedStacks())
			{
				if(!ExcavationSettings.autoPickup)
				{
					EntityItem eItem = new EntityItem(this.player.world, origin.getX() + 0.5D, origin.getY() + 0.5D, origin.getZ() + 0.5D, stack);
					this.player.world.spawnEntity(eItem);
				} else
				{
					EntityItem eItem = new EntityItem(this.player.world, player.posX, player.posY, player.posZ, stack);
					this.player.world.spawnEntity(eItem);
				}
			}
		}
		
		if(this.experience > 0)
		{
			EntityXPOrb orb = null;
			
			if(ExcavationSettings.autoPickup)
			{
				orb = new EntityXPOrb(this.player.world, player.posX, player.posY, player.posZ, experience);
			} else
			{
				orb = new EntityXPOrb(this.player.world, origin.getX(), origin.getY(), origin.getZ(), experience);
			}
			
			this.player.world.spawnEntity(orb);
		}
		
		drops.clear();
		this.experience = 0;
		
		EventHandler.captureAgent = ca;
	}
	
	public void addItemDrop(ItemStack stack)
	{
		for(BigItemStack bigStack : drops)
		{
			if(bigStack.equals(stack))
			{
				bigStack.stackSize += stack.getCount();
				return;
			}
		}
		
		this.drops.add(new BigItemStack(stack));
	}
	
	public void addExperience(int value)
	{
		this.experience += value;
	}
	
	public boolean hasMinedPosition(BlockPos pos)
	{
		return mined.contains(pos);
	}
	
	public ExcavateHistory getHistory()
	{
		return history;
	}
	
	private static Method m_createStack = null;
	
	static
	{
		try
		{
			m_createStack = Block.class.getDeclaredMethod("func_180643_i", IBlockState.class);
			m_createStack.setAccessible(true);
		} catch(Exception e1)
		{
			try
			{
				m_createStack = Block.class.getDeclaredMethod("createStackedBlock", IBlockState.class);
				m_createStack.setAccessible(true);
			} catch(Exception e2)
			{
				OreExcavation.logger.log(Level.INFO, "Unable to use block hooks for excavation", e2);
			}
		}
	}
}
