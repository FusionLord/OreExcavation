package oreexcavation.client;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import oreexcavation.core.ExcavationSettings;
import oreexcavation.core.OreExcavation;
import org.apache.logging.log4j.Level;

public class UpdateNotification
{
	boolean hasChecked = false;
	@SuppressWarnings("unused")
	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(!OreExcavation.proxy.isClient() || hasChecked)
		{
			return;
		}
		
		hasChecked = true;
		
		if(OreExcavation.HASH == "CI_MOD_" + "HASH")
		{
			event.player.sendMessage(new TextComponentString(TextFormatting.RED + "THIS COPY OF " + OreExcavation.NAME.toUpperCase() + " IS NOT FOR PUBLIC USE!"));
			return;
		}
		
		try
		{
			String[] data = getNotification("https://goo.gl/q9VC9j", true);
			
			if(ExcavationSettings.hideUpdates)
			{
				return;
			}
			
			ArrayList<String> changelog = new ArrayList<String>();
			boolean hasLog = false;
			
			for(String s : data)
			{
				if(s.equalsIgnoreCase("git_branch:" + OreExcavation.BRANCH))
				{
					if(!hasLog)
					{
						hasLog = true;
						changelog.add(s);
						continue;
					} else
					{
						break;
					}
				} else if(s.toLowerCase().startsWith("git_branch:"))
				{
					if(hasLog)
					{
						break;
					} else
					{
						continue;
					}
				} else if(hasLog)
				{
					changelog.add(s);
				}
			}
			
			if(!hasLog || data.length < 2)
			{
				event.player.sendMessage(new TextComponentString(TextFormatting.RED + "An error has occured while checking " + OreExcavation.NAME + " version!"));
				OreExcavation.logger.log(Level.ERROR, "An error has occured while checking " + OreExcavation.NAME + " version! (hasLog: " + hasLog + ", data: " + data.length + ")");
				return;
			} else
			{
				// Only the relevant portion of the changelog is preserved
				data = changelog.toArray(new String[0]);
			}
			
			String hash = data[1].trim();
			
			boolean hasUpdate = !OreExcavation.HASH.equalsIgnoreCase(hash);
			
			if(hasUpdate)
			{
				event.player.sendMessage(new TextComponentString(TextFormatting.RED + "Update for " + OreExcavation.NAME + " available!"));
				event.player.sendMessage(new TextComponentString("Download: http://minecraft.curseforge.com/projects/ore-excavation"));
				
				for(int i = 2; i < data.length; i++)
				{
					if(i > 5)
					{
						event.player.sendMessage(new TextComponentString("and " + (data.length - 5) + " more..."));
						break;
					} else
					{
						event.player.sendMessage(new TextComponentString("- " + data[i].trim()));
					}
				}
			}
			
		} catch(Exception e)
		{
			event.player.sendMessage(new TextComponentString(TextFormatting.RED + "An error has occured while checking " + OreExcavation.NAME + " version!"));
			OreExcavation.logger.log(Level.ERROR, "An error has occured while checking " + OreExcavation.NAME + " version!", e);
			return;
		}
	}
	
	public static String[] getNotification(String link, boolean doRedirect) throws Exception
	{
		URL url = new URL(link);
		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setDoOutput(false);
		con.setReadTimeout(20000);
		con.setRequestProperty("Connection", "keep-alive");
		
		con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0");
		((HttpURLConnection)con).setRequestMethod("GET");
		con.setConnectTimeout(5000);
		BufferedInputStream in = new BufferedInputStream(con.getInputStream());
		int responseCode = con.getResponseCode();
		HttpURLConnection.setFollowRedirects(true);
		if(responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_MOVED_PERM)
		{
			System.out.println("Update request returned response code: " + responseCode + " " + con.getResponseMessage());
		} else if(responseCode == HttpURLConnection.HTTP_MOVED_PERM)
		{
			if(doRedirect)
			{
				try
				{
					return getNotification(con.getHeaderField("location"), false);
				} catch(Exception e)
				{
					throw e;
				}
			} else
			{
				throw new Exception();
			}
		}
		StringBuffer buffer = new StringBuffer();
		int chars_read;
		//	int total = 0;
		while((chars_read = in.read()) != -1)
		{
			char g = (char)chars_read;
			buffer.append(g);
		}
		final String page = buffer.toString();
		
		String[] pageSplit = page.split("\\n");
		
		return pageSplit;
	}
}
