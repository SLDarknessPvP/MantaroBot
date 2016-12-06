package net.kodehawa.mantarobot.cmd;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantarobot.cmd.management.Command;
import net.kodehawa.mantarobot.util.Utils;

public class Yandere extends Command {
	
	public Yandere()
	{
		setName("yandere");
		setDescription("Fetches images from yande.re. For detailed information use ~>help yandere.");
		setExtendedHelp(
				"This command fetches images from the image board **yande.re**. Normally used to store *NSFW* images, "
				+ "but tags can be set to safe if you so desire.\n"
				+ "~>yandere: Gets you a completely random image.\n"
				+ "~>yandere get page <imagenumber> <rating>: Gets you an image with the specified parameters.\n"
				+ "~>yandere tags page <tag> <rating> <imagenumber>: Gets you an image with the respective tag and specified parameters.\n"
				+ "This command can be only used in NSFW channels! (Unless rating has been specified as safe)\n"
				+ "> Parameter explanation:\n"
				+ "*page*: Can be any value from 1 to the yande.re maximum page. Probably around 4000.\n"
				+ "*imagenumber*: Any number from 1 to the maximum possible images to get, specified by the first instance of the command.\n"
				+ "*tag*: Any valid image tag. For example animal_ears or yuri."
				+ "*rating*: Can be either safe, questionable or explicit, depends on the type of image you want to get.\n"
				+ "**Note: Image number and rating is optional.**"
				);
		setCommandType("user");
	}

	String yandereUrlParsed;
    int limit = 0;
    int number;
	int page = 0;
	String tagsToEncode = "no";
	String rating = "e";
	String tagsEncoded = "";
	
	@Override
	public void onCommand(String[] message, String beheadedMessage, MessageReceivedEvent evt) {
		guild = evt.getGuild();
        author = evt.getAuthor();
        channel = evt.getChannel();
        receivedMessage = evt.getMessage();
        
		try{
			page = Integer.parseInt(message[1]);
			tagsToEncode = message[2];
			rating = message[3];
			number = Integer.parseInt(message[4]); 
			
			if(rating.equals("safe")){ rating = "s"; }
			if(rating.equals("questionable")){ rating = "q"; }
			if(rating.equals("explicit")){ rating = "e"; }
		}
		catch(Exception e){}
		
		try {
			tagsEncoded = URLEncoder.encode(tagsToEncode, "UTF-8");
		} catch (UnsupportedEncodingException e1){
			e1.printStackTrace();
		}
				
        String noArgs = beheadedMessage.split(" ")[0];
		switch(noArgs){
		case "get":
			channel.sendMessage("Fetching data...").queue(
					sentMessage ->
					{
						String url = String.format("https://yande.re/post.json?limit=60&page=%2s", String.valueOf(page)).replace(" ", "");
						sentMessage.editMessage(getImage("get", url, limit, rating, message, evt)).queue();
					});
			break;
		case "tags":
			channel.sendMessage("Fetching data...").queue(
					sentMessage ->
					{
						String url = String.format("https://yande.re/post.json?limit=%1s&page=%2s&tags=%3s", String.valueOf(limit),
								String.valueOf(page), tagsEncoded).replace(" ", "");
						sentMessage.editMessage(getImage("tags", url, limit, rating, message, evt)).queue();
					});			
			break;
		case "":
			Random r = new Random();
			int randomPage = r.nextInt(4);

			channel.sendMessage("Fetching data...").queue(
					sentMessage ->
					{
						String url = String.format("https://yande.re/post.json?limit=%1s&page=%2s", "60", 
								String.valueOf(randomPage)).replace(" ", "");
						sentMessage.editMessage(getImage("random", url, limit, rating, message, evt)).queue();
					});
			break;
		default:
			channel.sendMessage(":heavy_multiplication_x: Incorrect usage. For info on how to use the command do ~>help yandere");
			break;
		}
	}
	
	public String getImage(String requestType, String url, int limit, String rating, String[] messageArray, MessageReceivedEvent evt){
		CopyOnWriteArrayList<String> urls = new CopyOnWriteArrayList<String>();
		if(limit > 60 ) limit = 60;
		JSONArray fetchedData = Utils.instance().getJSONArrayFromUrl(url, evt);
		 
		for(int i = 0; i < fetchedData.length(); i++)  {
			JSONObject entry = fetchedData.getJSONObject(i);
			if(entry.getString("rating").equals(rating)){
		        urls.add(entry.getString("file_url"));
			}
		}
		
		int get = 1;
		try{ 
			if(requestType.equals("tags")) 	get = number;
			if(requestType.equals("get")) get = Integer.parseInt(messageArray[2]);
		} catch(Exception e) { }
		
		List<TextChannel> array = channel.getJDA().getTextChannels();
		boolean trigger = false;
		
		for(MessageChannel ch : array) {
			if(ch.getName().contains("lewd") | ch.getName().contains("nsfw") | ch.getName().contains("nether") && ch.getId() == channel.getId()){
				trigger = true;
				break;
			} else if(rating.equals("s")){
				trigger = true;
			}
		} 
		if(trigger) {
			return String.format(":thumbsup: " + "I found an image! You can get a total of %1s images.\n %2s" , urls.size(), urls.get(get - 1));
		} else{
			return ":heavy_multiplication_x: " + "You only can use this command in nsfw channels!";
		}
	}
}