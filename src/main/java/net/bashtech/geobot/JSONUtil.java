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

	public static String BOICharacter(String channel) {

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://coebot.tv/configs/boir/"
							+ channel.substring(1) + ".json"));

			JSONObject jsonObject = (JSONObject) obj;

			String character = (String) jsonObject.get("character");

			return character;
		} catch (Exception ex) {
			return null;
		}
	}

	public static String BOISeed(String channel) {

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://coebot.tv/configs/boir/"
							+ channel.substring(1) + ".json"));

			JSONObject jsonObject = (JSONObject) obj;

			String seed = (String) jsonObject.get("seed");

			return seed;
		} catch (Exception ex) {
			return null;
		}
	}

	public static String BOIFloor(String channel) {

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://coebot.tv/configs/boir/"
							+ channel.substring(1) + ".json"));

			JSONObject jsonObject = (JSONObject) obj;

			String floor = (String) jsonObject.get("floor");

			return floor;
		} catch (Exception ex) {
			return null;
		}
	}

	public static ArrayList<String> BOIItems(String channel) {

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://coebot.tv/configs/boir/"
							+ channel.substring(1) + ".json"));

			JSONObject jsonObject = (JSONObject) obj;

			JSONArray items = (JSONArray) jsonObject.get("items");
			ArrayList<String> itemList = new ArrayList<String>();
			for (int i = 0; i < items.size(); i++) {
				itemList.add((String) items.get(i));
			}

			return itemList;
		} catch (Exception ex) {
			return null;
		}
	}

	public static ArrayList<String> BOIGuppyItems(String channel) {

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://coebot.tv/configs/boir/"
							+ channel.substring(1) + ".json"));

			JSONObject jsonObject = (JSONObject) obj;

			JSONArray items = (JSONArray) jsonObject.get("guppyItems");
			ArrayList<String> itemList = new ArrayList<String>();
			for (int i = 0; i < items.size(); i++) {
				itemList.add((String) items.get(i));
			}

			return itemList;
		} catch (Exception ex) {
			return null;
		}
	}

	public static ArrayList<String> BOIFlyItems(String channel) {

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://coebot.tv/configs/boir/"
							+ channel.substring(1) + ".json"));

			JSONObject jsonObject = (JSONObject) obj;

			JSONArray items = (JSONArray) jsonObject.get("flyItems");
			ArrayList<String> itemList = new ArrayList<String>();
			for (int i = 0; i < items.size(); i++) {
				itemList.add((String) items.get(i));
			}

			return itemList;
		} catch (Exception ex) {
			return null;
		}
	}

	public static Long BOIGuppyProgress(String channel) {

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://coebot.tv/configs/boir/"
							+ channel.substring(1) + ".json"));

			JSONObject jsonObject = (JSONObject) obj;

			Long progress = (Long) jsonObject.get("guppyProgress");

			return progress;
		} catch (Exception ex) {
			return null;
		}
	}

	public static Long BOIFlyProgress(String channel) {

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://coebot.tv/configs/boir/"
							+ channel.substring(1) + ".json"));

			JSONObject jsonObject = (JSONObject) obj;

			Long progress = (Long) jsonObject.get("flyProgress");

			return progress;
		} catch (Exception ex) {
			return null;
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
	// public static String getWiki(String query, int tries) {
	// try {
	// query = query.replaceAll(" ", "_");
	// JSONParser parser = new JSONParser();
	// Object obj = parser
	// .parse(BotManager
	// .getRemoteContent("http://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&rvsection=0&titles="
	// + query + "&format=json"));
	//
	// JSONObject jsonObject = (JSONObject) obj;
	//
	// JSONObject jsonquery = (JSONObject) jsonObject.get("query");
	// JSONObject pages = (JSONObject) jsonquery.get("pages");
	// Set<String> keys = (Set<String>) pages.keySet();
	// ArrayList<String> keyList = new ArrayList<String>();
	// for (String s : keys)
	// keyList.add(s);
	//
	// JSONObject id = (JSONObject) pages.get(keyList.get(0));
	// JSONArray revisions = (JSONArray) id.get("revisions");
	// JSONObject index0 = (JSONObject) revisions.get(0);
	//
	// String content = (String) index0.get("*");
	// if (content.contains("#REDIRECT") && tries < 3) {
	// String newSearch = content.substring(content.indexOf("[") + 2,
	// content.indexOf("]"));
	//
	// return getWiki(newSearch, tries++);
	// }
	//
	// content = content.replaceAll("\\\\", "");
	// content = content.replaceAll("\\{", "");
	// content = content.replaceAll("\\}", "");
	// int start = content.indexOf("'''");
	// content = content.substring(start + 3);
	// content = content.replaceAll("'''", "");
	// int refIndex = content.indexOf("<ref");
	// int refEndIndex = content.indexOf("</ref>");
	// int commentIndex = content.indexOf("<!--");
	// int commentEndIndex = content.indexOf("-->");
	// String firstPart;
	// String lastPart;
	//
	// while (refIndex > 0) {
	//
	// firstPart = content.substring(0, refIndex);
	// lastPart = content.substring(refEndIndex + 6);
	// content = firstPart + lastPart;
	// refIndex = content.indexOf("<ref");
	// refEndIndex = content.indexOf("</ref>", refEndIndex + 1);
	// }
	// while (commentIndex > 0) {
	//
	// firstPart = content.substring(0, commentIndex);
	// lastPart = content.substring(commentEndIndex + 3);
	// content = firstPart + lastPart;
	// commentIndex = content.indexOf("<!--");
	// commentEndIndex = content.indexOf("-->");
	// }
	// Pattern r = Pattern
	// .compile("\\[\\[(?:[^\\|\\]]*\\|)?([^\\]]+)\\]\\]");
	// Matcher m = r.matcher(content);
	// while (m.find()) {
	// String match = m.group(1);
	// String match0 = m.group();
	// // System.out.println(match0);
	// // System.out.println(match);
	// content = content.replace(match0, match);
	//
	// }
	// if (content.equals("")) {
	// content = "Malformed article abstract on Wikipedia's end.";
	// }
	// return content;
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// return "Unable to find that article.";
	// }
	// }

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

	public static String getXKCDTitle(int number) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://xkcd.com/" + number
							+ "/info.0.json"));

			JSONObject jsonObject = (JSONObject) obj;
			String safe_title = (String) jsonObject.get("safe_title");

			return safe_title;

		} catch (Exception ex) {
			System.out.println("Failed to get chatters");
			return null;
		}
	}

	public static String getXKCDImage(int number) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://xkcd.com/" + number
							+ "/info.0.json"));

			JSONObject jsonObject = (JSONObject) obj;
			String imagelink = (String) jsonObject.get("img");

			return imagelink;

		} catch (Exception ex) {
			System.out.println("Failed to get chatters");
			return null;
		}
	}

	public static String getXKCDAltText(int number) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://xkcd.com/" + number
							+ "/info.0.json"));

			JSONObject jsonObject = (JSONObject) obj;
			String alt = (String) jsonObject.get("alt");

			return alt;

		} catch (Exception ex) {
			System.out.println("Failed to get chatters");
			return null;
		}
	}

	public static String getRace(String channel) {
		String raceID = "";

		Set<String> entrantSet = new HashSet<String>();
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://api.speedrunslive.com:81/races"));

			JSONObject jsonObject = (JSONObject) obj;
			int count = Integer.parseInt((String) jsonObject.get("count"));
			JSONArray raceList = (JSONArray) jsonObject.get("races");

			for (int i = 0; i < count; i++) {
				JSONObject races = (JSONObject) raceList.get(i);
				String id = (String) races.get("id");

				JSONObject entrants = (JSONObject) races.get("entrants");
				Set<String> entrantNames = entrants.keySet();
				String entrantsString = entrants.toJSONString();
				if (entrantsString.contains(channel)) {
					raceID = id;
					entrantSet.add(channel);
					for (String s : entrantNames) {

						JSONObject entrant = (JSONObject) entrants.get(s);
						String entranttwitch = (String) entrant.get("twitch");
						if (entrantSet.size() < 5) {
							entrantSet.add(entranttwitch);
						}

					}
				}

			}
			if (entrantSet.size() > 0) {
				String raceLink = "http://speedrun.tv/race:" + raceID;
				for (String s : entrantSet) {
					raceLink = raceLink + "/" + s;
				}
				return raceLink;
			} else {
				return null;
			}

		} catch (Exception ex) {
			System.out.println("Failed to get races.");
			return null;
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

	public static String defineUrban(String word) {

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser
					.parse(BotManager
							.getRemoteContent("http://api.urbandictionary.com/v0/define?term="
									+ word));

			JSONObject jsonObject = (JSONObject) obj;
			JSONArray items = (JSONArray) jsonObject.get("list");
			JSONObject items0 = (JSONObject) items.get(0);
			String title = (String) items0.get("definition");

			return title;

		} catch (Exception ex) {

			return "Couldn't find any results, sorry";
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

	public static String BOIItemInfo(String searchTerm) {
		String modified = searchTerm.toLowerCase().replace(" a ", "")
				.replace(" ", "").replace("/", "").replace("'", "")
				.replace("the", "").replace("&lt;", "<").replace("1", "one")
				.replace("2", "two").replace("3", "three")
				.replace("20", "twenty").replace("-", "").replace(".", "")
				.replace("!", "").replace("=", "").replace("equals", "");
		int found = -1;
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://coebot.tv/boiitemsarray.json"));
			JSONArray topArray = (JSONArray) obj;
			for (int i = 0; i < topArray.size(); i++) {
				JSONObject tempobj = (JSONObject) topArray.get(i);
				String orig = (String) tempobj.get("title");
				if (modified.equalsIgnoreCase(orig.toLowerCase()
						.replace(" a ", "").replace(" ", "").replace("/", "")
						.replace("'", "").replace("the", "")
						.replace("&lt;", "<").replace("1", "one")
						.replace("2", "two").replace("3", "three")
						.replace("20", "twenty").replace("-", "")
						.replace(".", "").replace("!", "").replace("=", "")
						.replace("equals", ""))) {
					found = i;
					break;
				}
			}
			if (found > -1) {
				JSONObject item = (JSONObject) topArray.get(found);
				JSONArray info = (JSONArray) item.get("info");
				String retString = "";
				for (int i = 0; i < info.size(); i++) {
					if (retString.length() + ((String) info.get(i)).length() < 245)
						retString += info.get(i) + "; ";
					else
						break;
				}
				retString = retString.trim();
				retString = retString.substring(0, retString.length() - 1);
				return retString;
			} else {

				return "Item Not Found";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
	}

	public static String shortenUrlTinyURL(String longUrl) {
		String shortUrl = BotManager
				.getRemoteContent("http://tinyurl.com/api-create.php?url="
						+ longUrl);
		return shortUrl;
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
	public static boolean deleteVar(String channel, String varName) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://coebot.tv/api/v1/vars/unset/"
							+ varName + "/" + channel+ "$"
							+ BotManager.getInstance().CoeBotTVAPIKey + "$"
							+ BotManager.getInstance().nick));

			JSONObject jsonObject = (JSONObject) obj;

			String status = (String) (jsonObject.get("status"));
			return(status.equalsIgnoreCase("ok"));

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

	}
	public static String getVar(String channel, String varName) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(BotManager
					.getRemoteContent("http://coebot.tv/api/v1/vars/get/"
							+ varName + "/" + channel));

			JSONObject jsonObject = (JSONObject) obj;

			String status = (String) (jsonObject.get("status"));
			if (status.equals("ok")) {
				String value = (String) jsonObject.get("value");
				return value;
			} else {
				return null;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	public static boolean setVar(String channel, String varName, String newValue) {
		JSONObject postData = new JSONObject();
		postData.put("value", newValue);
		try {

			JSONParser parser = new JSONParser();
			String resp = BotManager.postCoebotVars(postData.toJSONString(),
					"http://coebot.tv/api/v1/vars/set/" + varName + "/"
							+ channel);
			if (!resp.equals("")) {
				Object obj = parser.parse(resp);

				JSONObject jsonObject = (JSONObject) obj;

				String status = (String) (jsonObject.get("status"));
				return status.equals("ok");

			} else
				return false;

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

	}

	public static String incVar(String channel, String varName, int incValue) {
		JSONObject postData = new JSONObject();
		postData.put("incAmount", incValue);
		try {

			JSONParser parser = new JSONParser();
			String resp = BotManager.postCoebotVars(postData.toJSONString(),
					"http://coebot.tv/api/v1/vars/increment/" + varName + "/"
							+ channel);
			if (!resp.equals("")) {
				Object obj = parser.parse(resp);

				JSONObject jsonObject = (JSONObject) obj;

				String status = (String) (jsonObject.get("status"));
				if (status.equals("ok")) {
					String newValue = (String) jsonObject.get("newValue");
					return newValue;
				} else {
					return null;
				}
			} else {
				return null;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	public static String decVar(String channel, String varName, int incValue) {
		incValue *= -1;
		JSONObject postData = new JSONObject();
		postData.put("incAmount", incValue);
		try {

			JSONParser parser = new JSONParser();
			String resp = BotManager.postCoebotVars(postData.toJSONString(),
					"http://coebot.tv/api/v1/vars/increment/" + varName + "/"
							+ channel);
			if (!resp.equals("")) {
				Object obj = parser.parse(resp);

				JSONObject jsonObject = (JSONObject) obj;

				String status = (String) (jsonObject.get("status"));
				if (status.equals("ok")) {
					String newValue = (String) jsonObject.get("newValue");
					return newValue;
				} else {
					return null;
				}
			} else {
				return null;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

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
