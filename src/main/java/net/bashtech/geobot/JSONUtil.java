/*
 * Copyright 2012 Andrew Bashore
 * This file is part of GeoBot.
 *
 * GeoBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GeoBot is distributed in the hope that it will be useful
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoBot.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.bashtech.geobot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONUtil {

	public static Long krakenViewers(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("https://api.twitch.tv/kraken/streams/"
							+ channel));

			JSONObject jsonObject = (JSONObject) obj;

			JSONObject stream = (JSONObject) (jsonObject.get("stream"));
			if (stream == null)
				return (long) 0;

			Long viewers = (Long) stream.get("viewers");
			return viewers;
		} catch (Exception ex) {
			System.out.println("Kraken Viewers isn't working");
			return (long) 0;
		}

	}
	
	public static String getGameChannel(String gameName) {
		gameName = gameName.replaceAll(" ", "+");
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("https://api.twitch.tv/kraken/search/streams?q="
									+ gameName));

			JSONObject jsonObject = (JSONObject) obj;
			Long total = (Long) jsonObject.get("_total");
			if (total > 0) {
				JSONArray streams = (JSONArray) jsonObject.get("streams");
				int numStreams = streams.size();
				int randomChannel = (int) (Math.random() * (numStreams - 1));
				JSONObject stream = (JSONObject) streams.get(randomChannel);
				JSONObject channel = (JSONObject) stream.get("channel");
				String url = (String) channel.get("display_name");

				return url;
			} else
				return "No other channels playing this game";

		} catch (Exception ex) {
			ex.printStackTrace();
			return "Error Querying API";
		}
	}
	
	public static String krakenStatus(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContentTwitch(
					"https://api.twitch.tv/kraken/channels/" + channel, 2));

			JSONObject jsonObject = (JSONObject) obj;

			String status = (String) jsonObject.get("status");

			if (status == null)
				status = "(Not set)";

			return status;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "(Error querying API)";
		}

	}

	public static String krakenGame(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContentTwitch(
					"https://api.twitch.tv/kraken/channels/" + channel, 2));

			JSONObject jsonObject = (JSONObject) obj;

			String game = (String) jsonObject.get("game");

			if (game == null)
				game = "(Not set)";

			return game;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "(Error querying API)";
		}

	}

	public static ArrayList<String> tmiChatters(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("https://tmi.twitch.tv/group/user/"
							+ channel + "/chatters"));

			JSONObject jsonObject = (JSONObject) obj;
			JSONObject chatters = (JSONObject) jsonObject.get("chatters");
			JSONArray viewers = (JSONArray) chatters.get("viewers");
			JSONArray moderators = (JSONArray) chatters.get("moderators");
			for (int i = 0; i < moderators.size(); i++) {
				viewers.add(moderators.get(i));
			}

			return viewers;

		} catch (Exception ex) {
			System.out.println("Failed to get chatters");
			return null;
		}
	}

	public static Long tmiChattersCount(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("https://tmi.twitch.tv/group/user/"
							+ channel + "/chatters"));

			JSONObject jsonObject = (JSONObject) obj;
			Long chatterCount = (Long) jsonObject.get("chatter_count");

			return chatterCount;

		} catch (Exception ex) {
			System.out.println("Failed to get chatters");
			return (long) 0;
		}
	}

	public static String krakenCreated_at(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContentTwitch(
					"https://api.twitch.tv/kraken/streams/" + channel, 2));

			JSONObject jsonObject = (JSONObject) obj;

			JSONObject stream = (JSONObject) (jsonObject.get("stream"));
			if (stream == null)
				return "(offline)";

			String viewers = (String) stream.get("created_at");
			return viewers;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "(error)";
		}

	}
	// public static String googURL(String url) {
	// try {
	//
	// JSONParser parser = new JSONParser();
	// Object obj = parser.parse(BotManager.postDataLinkShortener(url));
	//
	// JSONObject jsonObject = (JSONObject) obj;
	// String response = (String) jsonObject.get("id");
	// return response;
	//
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// return url;
	// }
	// }

	public static String urlEncode(String data) {
		try {
			data = URLEncoder.encode(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static boolean krakenIsLive(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContentTwitch(
					"https://api.twitch.tv/kraken/streams/" + channel, 2));

			JSONObject jsonObject = (JSONObject) obj;

			JSONObject stream = (JSONObject) (jsonObject.get("stream"));

			if (stream != null)
				return true;
			else
				return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

	}
	
	public static boolean krakenChannelExist(String channel) {
		if (BotManager.getInstance().twitchChannels == false)
			return true;

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContentTwitch(
					"https://api.twitch.tv/kraken/channels/" + channel, 2));

			JSONObject jsonObject = (JSONObject) obj;

			Long _id = (Long) jsonObject.get("_id");

			return (_id != null);
		} catch (Exception ex) {
			// ex.printStackTrace();
			return false;
		}

	}

	public static boolean krakenOutdatedChannel(String channel) {
		if (BotManager.getInstance().twitchChannels == false)
			return false;

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager.getRemoteContentTwitch(
					"https://api.twitch.tv/kraken/channels/" + channel, 2));

			JSONObject jsonObject = (JSONObject) obj;

			Object statusO = jsonObject.get("status");
			Long status;
			if (statusO != null) {
				status = (Long) statusO;
				if (status == 422 || status == 404) {
					System.out.println("Channel " + channel
							+ " returned status: " + status
							+ ". Parting channel.");
					return true;
				}
			}

			String updatedAtString = (String) jsonObject.get("updated_at");

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			format.setTimeZone(java.util.TimeZone.getTimeZone("US/Pacific"));
			long differenceDay = 0;

			try {
				Date then = format.parse(updatedAtString);
				long differenceSec = (long) (System.currentTimeMillis() / 1000)
						- (then.getTime() / 1000);
				differenceDay = (long) (differenceSec / 86400);
			} catch (Exception exi) {
				exi.printStackTrace();
			}

			if (differenceDay > 30) {
				System.out.println("Channel " + channel + " not updated in "
						+ differenceDay + " days. Parting channel.");
				return true;
			}

		} catch (Exception ex) {
			return false;
		}

		return false;

	}
	
	public static Long updateTMIUserList(String channel, Set<String> staff,
			Set<String> admins, Set<String> mods) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://tmi.twitch.tv/group/user/"
							+ channel + "/chatters"));

			JSONObject jsonObject = (JSONObject) obj;

			Long chatter_count = (Long) jsonObject.get("chatter_count");

			JSONObject chatters = (JSONObject) jsonObject.get("chatters");

			JSONArray staffJO = (JSONArray) chatters.get("staff");
			for (Object user : staffJO) {
				staff.add((String) user);
			}

			JSONArray adminsJO = (JSONArray) chatters.get("admins");
			for (Object user : adminsJO) {
				admins.add((String) user);
			}

			JSONArray modsJO = (JSONArray) chatters.get("moderators");
			for (Object user : modsJO) {
				mods.add((String) user);
			}

			return chatter_count;
		} catch (Exception ex) {
			ex.printStackTrace();
			return new Long(-1);
		}

	}

	public static String getChatProperties(String channel) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://api.twitch.tv/api/channels/"
							+ channel + "/chat_properties"));

			JSONObject jsonObject = (JSONObject) obj;

			Boolean hide_chat_links = (Boolean) jsonObject
					.get("hide_chat_links");
			Boolean devchat = (Boolean) jsonObject.get("devchat");
			Boolean eventchat = (Boolean) jsonObject.get("eventchat");
			Boolean require_verified_account = (Boolean) jsonObject
					.get("require_verified_account");

			String response = "Hide links: " + hide_chat_links
					+ ", Require verified account: " + require_verified_account;

			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
			return "(Error querying API)";
		}

	}

	public static List<String> getEmotes() {
		List<String> emotes = new LinkedList<String>();
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("http://direct.twitchemotes.com/global.json"));

			JSONObject jsonObject = (JSONObject) obj;

			for (Object o : jsonObject.keySet()) {
				String name = (String) o;
				if (name.length() > 0)
					emotes.add(name);
			}
			emotes.add("<3");
			emotes.add(":)");
			emotes.add(":o");
			emotes.add(":(");
			emotes.add(";)");
			emotes.add(":/");
			emotes.add(";p");
			emotes.add(">(");
			emotes.add("B)");
			emotes.add("O_o");
			emotes.add("O_O");
			emotes.add("R)");
			emotes.add(":D");
			emotes.add(":z");
			emotes.add("D:");
			emotes.add(":p");
			emotes.add(":P");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			return emotes;
		}

	}

	public static String toTitleCase(String givenString) {
		String[] arr = givenString.split(" ");
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < arr.length; i++) {
			sb.append(Character.toUpperCase(arr[i].charAt(0)))
					.append(arr[i].substring(1)).append(" ");
		}
		return sb.toString().trim();
	}

}
