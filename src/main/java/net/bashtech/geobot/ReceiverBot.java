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

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiverBot extends PircBot {
	static ReceiverBot instance;
	Timer joinCheck;
	Random random = new Random();
	private Pattern[] linkPatterns = new Pattern[4];
	private Pattern[] symbolsPatterns = new Pattern[2];
	private int lastPing = -1;
	private String bullet[] = { "#!" };

	private Pattern banNoticePattern = Pattern.compile(
			"^You are permanently banned from talking in ([a-z_]+).$",
			Pattern.CASE_INSENSITIVE);
	private Pattern toNoticePattern = Pattern
			.compile(
					"^You are banned from talking in ([a-z_]+) for (?:[0-9]+) more seconds.$",
					Pattern.CASE_INSENSITIVE);

	long lastCommand = System.currentTimeMillis();
	private boolean privMsgSub = false;
	private ArrayList<Long> msgTimer = new ArrayList<Long>();
	private ArrayList<QueuedMessage> queuedMessages = new ArrayList<QueuedMessage>();
	private boolean tried;
	private boolean delete;
	private boolean permitted;
	private long lastConch = System.currentTimeMillis();
	String botName;

	public ReceiverBot(String server, int port) {
		ReceiverBot.setInstance(this);

		linkPatterns[0] = Pattern.compile(".*http://.*",
				Pattern.CASE_INSENSITIVE);
		linkPatterns[1] = Pattern.compile(".*https://.*",
				Pattern.CASE_INSENSITIVE);
		linkPatterns[2] = Pattern
				.compile(
						".*[-A-Za-z0-9]+\\s?(\\.|\\(dot\\))\\s?(ac|ad|ae|aero|af|ag|ai|al|am|an|ao|aq|ar|as|asia|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|biz|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cat|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|com|coop|cr|cu|cv|cw|cx|cy|cz|de|dj|dk|dm|do|dz|ec|edu|ee|eg|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gov|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|info|int|io|iq|ir|is|it|je|jm|jo|jobs|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mil|mk|ml|mm|mn|mo|mobi|mp|mq|mr|ms|mt|mu|museum|mv|mw|mx|my|mz|na|name|nc|ne|net|nf|ng|ni|nl|no|np|nr|nu|nz|om|org|pa|pe|pf|pg|ph|pk|pl|pm|pn|post|pr|pro|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|sl|sm|sn|so|sr|st|su|sv|sx|sy|sz|tc|td|tel|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|travel|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|xxx|ye|yt|za|zm|zw)(\\W|$).*",
						Pattern.CASE_INSENSITIVE);
		linkPatterns[3] = Pattern
				.compile(".*(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\\s+|:|/|$).*");

		symbolsPatterns[0] = Pattern
				.compile("(\\p{InPhonetic_Extensions}|\\p{InLetterlikeSymbols}|\\p{InDingbats}|\\p{InBoxDrawing}|\\p{InBlockElements}|\\p{InGeometricShapes}|\\p{InHalfwidth_and_Fullwidth_Forms}|Ã£ï¿½Â¤|Ã‚Â°|Ã ÂºË†|Ã¢â€“â€˜|Ã¢â€“â‚¬|Ã¢â€“â€ž|ÃŒÂ°ÃŒÂ¦ÃŒÂ®ÃŒÂ eÃ�Â¦ÃŒÅ¡Ã�Â¯Ã�Â¯ÃŒï¿½Ã�Â®ÃŒÅ ÃŒï¿½Ã�Å’ÃŒâ€°Ã�â€˜Ã�Â¨ÃŒÅ Ã�ï¿½Ã�ï¿½ÃŒÂ¨ÃŒÅ¸ÃŒÂ¹|UÃŒÂ¶ÃŒÂ§Ã�Â©Ã�Â­Ã�Â§Ã�Å ÃŒâ€¦ÃŒÅ Ã�Â¥Ã�Â©ÃŒÂ¿ÃŒâ€�ÃŒâ€�Ã�Â¥Ã�Å’Ã�Â¬Ã�Å Ã�â€¹Ã�Â¬Ã’â€°|Ã¡Â»Å’ÃŒÂµÃ�â€¡ÃŒâ€“ÃŒâ€“|AÃŒÂ´Ã�ï¿½ÃŒÂ¥ÃŒÂ³ÃŒÂ ÃŒÅ¾ÃŒÂ¹Ã�Â©ÃŒâ€¹ÃŒâ€ Ã�Â¤Ã�â€¦|EÃŒÂ¡ÃŒâ€ºÃ�Å¡ÃŒÂºÃŒâ€“ÃŒÂªÃ�Ë†ÃŒÂ²ÃŒÂ»ÃŒÂ ÃŒÂ°ÃŒÂ³ÃŒï¿½ÃŒÂ¿)");
		symbolsPatterns[1] = Pattern.compile("[!-/:-@\\[-`{-~]");

		this.setName(BotManager.getInstance().nick);
		this.setLogin(this.getName());
		this.setMessageDelay(0);
		botName = this.getNick();
		this.setVerbose(BotManager.getInstance().verboseLogging);

		try {
			this.connect(server, port, BotManager.getInstance().password);
		} catch (NickAlreadyInUseException e) {
			logMain("RB: [ERROR] Nickname already in use - " + this.getNick()
					+ " " + this.getServer());
		} catch (IOException e) {
			logMain("RB: [ERROR] Unable to connect to server - "
					+ this.getNick() + " " + this.getServer());
		} catch (IrcException e) {
			logMain("RB: [ERROR] Error connecting to server - "
					+ this.getNick() + " " + this.getServer());
		}

		startJoinCheck();
	}

	public void parseEvent(String data) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(data);

			JSONObject jsonObject = (JSONObject) obj;

			String channel = (String) jsonObject.get("channel");
			String editor = (String) jsonObject.get("editor");
			Channel channelInfo = getChannelObject("#"
					+ (String) jsonObject.get("channel"));
			JSONArray actionArray = (JSONArray) jsonObject.get("actions");
			for (int i = 0; i < actionArray.size(); i++) {
				JSONObject actionObject = (JSONObject) actionArray.get(i);
				String action = (String) actionObject.get("action");
				if (channelInfo == null && !action.equals("join")) {
					return;
				}

				switch (action) {
				case "join": {

					if (!BotManager.getInstance().publicJoin) {

						return;
					}

					if (JSONUtil.krakenChannelExist(channel)) {

						BotManager.getInstance().addChannel("#" + channel, 2);

						BotManager.getInstance().coebotJoinChannel(channel,
								getNick());

					}
					break;
				}
				case "part": {

					BotManager.getInstance().removeChannel("#" + channel);
					BotManager.getInstance().coebotPartChannel(channel,
							getNick());
					break;
				}
				case "add command": {

					String key = ((String) actionObject.get("key")).replaceAll(
							"[^a-zA-Z0-9]", "");
					key = key.toLowerCase();
					String value = (String) actionObject.get("value");

					channelInfo.setCommand(key, value, editor);
					if (value.contains("(_PURGE_)")
							|| value.contains("(_TIMEOUT_)")
							|| value.contains("(_BAN_)")
							|| value.contains("(_COMMERCIAL_)")) {
						channelInfo.setCommandsRestriction(key, 2);
					} else
						channelInfo.setCommandsRestriction(key, 0);
					break;
				}
				case "edit command": {
					String key = ((String) actionObject.get("key")).replaceAll(
							"[^a-zA-Z0-9]", "");
					key = key.toLowerCase();
					String value = (String) actionObject.get("value");

					channelInfo.editCommand(key, value, editor);
					if (value.contains("(_PURGE_)")
							|| value.contains("(_TIMEOUT_)")
							|| value.contains("(_BAN_)")
							|| value.contains("(_COMMERCIAL_)")) {
						channelInfo.setCommandsRestriction(key, 2);
					} else
						channelInfo.setCommandsRestriction(key, 0);
					break;
				}
				case "delete command": {
					String key = ((String) actionObject.get("key")).replaceAll(
							"[^a-zA-Z0-9]", "");
					key = key.toLowerCase();
					channelInfo.removeCommand(key);

					channelInfo.removeRepeatCommand(key);
					channelInfo.removeScheduledCommand(key);
					break;
				}
				case "restrict command": {
					int level = 3;
					String levelStr = (String) actionObject.get("restriction");
					if (channelInfo
							.getCommand((String) actionObject.get("key")) != null) {
						if (levelStr.equalsIgnoreCase("owner")
								|| levelStr.equalsIgnoreCase("owners"))
							level = 3;
						if (levelStr.equalsIgnoreCase("mod")
								|| levelStr.equalsIgnoreCase("mods"))
							level = 2;
						if (levelStr.equalsIgnoreCase("regular")
								|| levelStr.equalsIgnoreCase("regulars"))
							level = 1;
						if (levelStr.equalsIgnoreCase("everyone"))
							level = 0;

						channelInfo.setCommandsRestriction(
								(String) actionObject.get("key"), level);

					}
					break;
				}
				case "add autoreply": {

					String pattern = ((String) actionObject.get("pattern"))
							.replaceAll("_", " ");
					String response = (String) actionObject.get("response");

					channelInfo.addAutoReply(pattern, response);
					break;
				}
				case "delete autoreply": {
					Long index = (Long) actionObject.get("index");
					int intIndex = index.intValue();

					channelInfo.removeAutoReply(intIndex);
					break;
				}
				case "edit autoreply": {
					Long index = (Long) actionObject.get("index");
					int intIndex = index.intValue();
					String newResponse = (String) actionObject.get("response");
					if (channelInfo
							.editAutoReplyResponse(intIndex, newResponse))
						break;
				}
				case "add repeat": {
					int delay = ((Long) actionObject.get("delay")).intValue();
					int difference = ((Long) actionObject.get("difference"))
							.intValue();
					String key = (String) actionObject.get("key");

					if (channelInfo.getCommand(key).equalsIgnoreCase("invalid")
							|| delay < 30) {

					} else {
						channelInfo.setRepeatCommand(key, delay, difference);
					}
					break;
				}
				case "delete repeat": {
					String key = (String) actionObject.get("key");
					channelInfo.removeRepeatCommand(key);

					break;
				}
				case "add scheduled": {
					String key = (String) actionObject.get("key");

					String pattern = (String) actionObject.get("pattern");
					int difference = ((Long) actionObject.get("difference"))
							.intValue();
					if (pattern.equalsIgnoreCase("hourly"))
						pattern = "0 * * * *";
					else if (pattern.equalsIgnoreCase("semihourly"))
						pattern = "0,30 * * * *";
					else
						pattern = pattern.replace("_", " ");

					if (channelInfo.getCommand(key).equalsIgnoreCase("invalid")
							|| pattern.contains(",,")) {
					} else {
						channelInfo.setScheduledCommand(key, pattern,
								difference);

					}

					break;
				}
				case "delete scheduled": {
					String key = (String) actionObject.get("key");
					channelInfo.removeScheduledCommand(key);

					break;
				}
				case "set urban": {
					boolean enabled = false;
					String value = (String) actionObject.get("value");
					if (value.equalsIgnoreCase("on")
							|| value.equalsIgnoreCase("enabled")) {
						enabled = true;
					}
					channelInfo.setUrban(enabled);

					break;
				}
				case "set bullet": {
					channelInfo.setBullet((String) actionObject.get("value"));
					break;
				}
				case "set subsRegsMinusLinks": {
					boolean status = false;
					String value = (String) actionObject.get("value");
					if (value.equalsIgnoreCase("on")) {
						status = true;
					}
					channelInfo.setSubsRegsMinusLinks(status);
					break;
				}
				case "set cooldown": {
					int value = ((Long) actionObject.get("value")).intValue();
					channelInfo.setCooldown(value);
					break;
				}
				case "set mode": {
					String value = (String) actionObject.get("value");
					if ((value.equalsIgnoreCase("0") || value
							.equalsIgnoreCase("owner"))) {
						channelInfo.setMode(0);

					} else if (value.equalsIgnoreCase("1")
							|| value.equalsIgnoreCase("mod")) {
						channelInfo.setMode(1);

					} else if (value.equalsIgnoreCase("2")
							|| value.equalsIgnoreCase("everyone")) {
						channelInfo.setMode(2);

					} else if (value.equalsIgnoreCase("-1")
							|| value.equalsIgnoreCase("admin")) {
						channelInfo.setMode(-1);

					}

					break;
				}
				case "set prefix": {
					channelInfo.setPrefix((String) actionObject.get("value"));
					break;
				}
				case "set currency": {
					channelInfo.setCurrencyName((String) actionObject.get("value"));
					break;
				}
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public static ReceiverBot getInstance() {
		return instance;
	}

	public static void setInstance(ReceiverBot rb) {
		if (instance == null) {
			instance = rb;
		}
	}

	// private static String getTagValue(String sTag, Element eElement) {
	// NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
	// .getChildNodes();
	//
	// Node nValue = (Node) nlList.item(0);
	//
	// return nValue.getNodeValue();
	// }

	private Channel getChannelObject(String channel) {
		Channel channelInfo = null;
		channelInfo = BotManager.getInstance().getChannel(channel);
		return channelInfo;
	}

	@Override
	protected void onDeop(String channel, String sourceNick,
			String sourceLogin, String sourceHostname, String recipient) {
		recipient = recipient.replace(":", "");
		System.out.println("DEBUG: Got DEOP for " + recipient + " in channel: "
				+ channel);
		this.getChannelObject(channel).tagModerators.remove(recipient);
	}

	@Override
	protected void onOp(String channel, String sourceNick, String sourceLogin,
			String sourceHostname, String recipient) {
		recipient = recipient.replace(":", "");
		System.out.println("DEBUG: Got OP for " + recipient + " in channel: "
				+ channel);
		this.getChannelObject(channel).tagModerators.add(recipient);
	}

	@Override
	protected void onConnect() {
		// Force TMI to send USERCOLOR AND SPECIALUSER messages.
		this.sendRawLine("TWITCHCLIENT 3");
	}

	@Override
	protected void onPrivateMessage(String sender, String login,
			String hostname, String message) {
		if (!message.startsWith("USERCOLOR") && !message.startsWith("EMOTESET")
				&& !message.startsWith("SPECIALUSER")
				&& !message.startsWith("HISTORYEND")
				&& !message.startsWith("CLEARCHAT")
				&& !message.startsWith("Your color"))
			BotManager.getInstance().log("RB PM: " + sender + " " + message);

		Matcher m = banNoticePattern.matcher(message);
		if (m.matches()) {
			String channel = "#" + m.group(1);
			BotManager.getInstance().log(
					"SB: Detected ban in " + channel + ". Parting..");
			BotManager.getInstance().removeChannel(channel);
			BotManager.getInstance().coebotPartChannel(channel.substring(1),
					getNick());
		}

		m = toNoticePattern.matcher(message);
		if (m.matches()) {
			String channel = "#" + m.group(1);
			BotManager.getInstance().log(
					"SB: Detected timeout in " + channel + ". Parting..");
			BotManager.getInstance().removeChannel(channel);
			BotManager.getInstance().coebotPartChannel(channel.substring(1),
					getNick());
		}

		if (sender.equals("jtv"))
			onAdministrativeMessage(message, null);
	}

	@Override
	protected void onAction(String sender, String login, String hostname,
			String target, String action) {
		this.onMessage(target, sender, login, hostname, "/me " + action);
	}

	@Override
	protected void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		if (!BotManager.getInstance().useEventFeed)
			onChannelMessage(channel, channel, sender, message);
	}

	@SuppressWarnings("rawtypes")
	protected void onChannelMessage(String channel, String targetChannel,
			String sender, String message) {
		if (!BotManager.getInstance().verboseLogging
				&& !message.startsWith("USERCOLOR")
				&& !message.startsWith("EMOTESET")
				&& !message.startsWith("SPECIALUSER")
				&& !message.startsWith("HISTORYEND")
				&& !message.startsWith("CLEARCHAT")
				&& !message.startsWith("Your color"))
			logMain("MSG: " + targetChannel + " " + sender + " : " + message);

		Channel channelInfo = getChannelObject(targetChannel);
		String twitchName = channelInfo.getTwitchName();
		String prefix = channelInfo.getPrefix();
		String currency = channelInfo.getCurrencyName();
		bullet[0] = channelInfo.getChannelBullet();

		if (!sender.equalsIgnoreCase(this.getNick()))
			channelInfo.messageCount++; // Inc message count

		// Ignore messages from self.
		if (sender.equalsIgnoreCase(this.getNick())) {
			// System.out.println("Message from bot");
			return;
		}

		// Handle future administrative messages from JTV
		if (sender.equalsIgnoreCase("jtv")) {
			onAdministrativeMessage(message, channelInfo);
			return;
		}

		// Split message on spaces.
		String[] msg = message.trim().split(" ");

		// ********************************************************************************
		// ****************************** User Ranks
		// **************************************
		// ********************************************************************************

		boolean isAdmin = false;
		boolean isOwner = false;
		boolean isOp = false;
		boolean isRegular = false;
		boolean isSub = false;
		int accessLevel = 0;

		// Check for user level based on other factors.
		if (BotManager.getInstance().isAdmin(sender))
			isAdmin = true;
		if (BotManager.getInstance().isTagAdmin(sender)
				|| BotManager.getInstance().isTagStaff(sender))
			isAdmin = true;
		if (channel.equalsIgnoreCase("#" + sender))
			isOwner = true;
		if (channelInfo.isModerator(sender))
			isOp = true;
		if (channelInfo.isOwner(sender))
			isOwner = true;
		if (channelInfo.isRegular(sender)
				|| (channelInfo.subscriberRegulars && privMsgSub)) {
			isRegular = true;
			privMsgSub = false;
		}
		if (privMsgSub && channelInfo.subsRegsMinusLinks) {
			accessLevel = 1;
			isSub = true;
			privMsgSub = false;
		}
		if (channelInfo.getIgnoredUsers().contains(sender.toLowerCase())) {
			isAdmin = false;
			isOwner = false;
			isRegular = false;
			isSub = false;
		}
		// Give users all the ranks below them
		if (isAdmin) {
			log("RB: " + sender + " is admin.");
			isOwner = true;
			isOp = true;
			isRegular = true;
			isSub = true;
			accessLevel = 99;
		} else if (isOwner) {
			log("RB: " + sender + " is owner.");
			isOp = true;
			isRegular = true;
			isSub = true;
			accessLevel = 3;
		} else if (isOp) {
			log("RB: " + sender + " is op.");
			isRegular = true;
			isSub = true;
			accessLevel = 2;
		} else if (isRegular) {
			log("RB: " + sender + " is regular.");
			isSub = true;
			accessLevel = 1;
		} else if (isSub) {
			log("RB: " + sender + " is a subscriber");
		}

		checkQueued();
		// !{botname} command
		if (msg[0].equalsIgnoreCase(prefix + this.getName())) {
			if (msg.length >= 2) {

				String[] newMsg = new String[msg.length - 1];
				for (int i = 1; i < msg.length; i++) {
					newMsg[i - 1] = msg[i];
				}
				msg = newMsg;
				msg[0] = prefix + msg[0];

				message = fuseArray(msg, 0);
				System.out.println("DEBUG: Command rewritten as " + message);
			}

		}

		// !leave - Owner
		if ((msg[0].equalsIgnoreCase(prefix + "leave")
				|| msg[0].equalsIgnoreCase(prefix + "remove") || msg[0]
					.equalsIgnoreCase(prefix + "part")) && isOwner) {
			send(channel, "Leaving channel " + channelInfo.getChannel() + ".");
			BotManager.getInstance().coebotPartChannel(channel.substring(1),
					getNick());
			BotManager.getInstance().removeChannel(channelInfo.getChannel());
			return;
		}

		// ********************************************************************************
		// ********************************** Filters
		// *************************************
		// ********************************************************************************
		

		// ignore messages from blacklisted users
		if (!BotManager.getInstance().isAdmin(sender)
				&& channelInfo.getIgnoredUsers().contains(sender.toLowerCase())) {

			return;
		}
		// verbose logging on/off
		if (isAdmin && msg[0].equalsIgnoreCase(prefix + "verboseLogging")) {
			if (msg.length > 1) {
				if (msg[1].equalsIgnoreCase("true")
						|| msg[1].equalsIgnoreCase("on")) {
					this.setVerbose(true);
					send(channel, "Verbose Logging turned on.");
				} else if (msg[1].equalsIgnoreCase("false")
						|| msg[1].equalsIgnoreCase("off")) {
					this.setVerbose(false);
					send(channel, "Verbose Logging turned off.");

				}
			}
		}
		// Impersonation command
		if (isAdmin && msg[0].equalsIgnoreCase(prefix + "imp")) {
			if (msg.length >= 3) {
				channelInfo = getChannelObject("#" + msg[1]);
				twitchName = channelInfo.getTwitchName();

				String[] newMsg = new String[msg.length - 2];
				for (int i = 2; i < msg.length; i++) {
					newMsg[i - 2] = msg[i];
				}
				msg = newMsg;

				message = fuseArray(msg, 0);
				send(channel,
						"Impersonating channel " + channelInfo.getChannel()
								+ " with command: " + message);
				System.out.println("IMP: Impersonating channel "
						+ channelInfo.getChannel() + " with command: "
						+ message);
			}

		}

		// ********************************************************************************
		// ******************************* Mode Checks
		// ************************************
		// ********************************************************************************

		// Check channel mode.
		if ((channelInfo.getMode() == 0 || channelInfo.getMode() == -1)
				&& !isOwner)
			return;
		if (channelInfo.getMode() == 1 && !isOp)
			return;

		// ********************************************************************************
		// ********************************* Commands
		// *************************************
		// ********************************************************************************

		// Command cooldown check
		if (msg[0].substring(0, 1).equalsIgnoreCase(prefix)
				&& channelInfo.onCooldown(msg[0])) {
			if (!isOp)
				return;
		}

		// !bothelp - All
		if (msg[0].equalsIgnoreCase(prefix + "bothelp")) {
			log("RB: Matched command !bothelp");
			send(channel, BotManager.getInstance().bothelpMessage);
			return;
		}

		// !viewers - All
		if ((msg[0].equalsIgnoreCase(prefix + "viewers") || msg[0]
				.equalsIgnoreCase(prefix + "lurkers"))) {
			log("RB: Matched command !viewers");

			try {
				send(channel, JSONUtil.krakenViewers(twitchName) + " viewers.");
			} catch (Exception e) {
				send(channel, "Stream is not live.");
			}

			return;
		}
		// !chatters
		if ((msg[0].equalsIgnoreCase(prefix + "chatters"))) {
			log("RB: Matched command !viewers");

			try {
				send(channel, JSONUtil.tmiChattersCount(twitchName)
						+ " people currently connected to chat.");
			} catch (Exception e) {
				send(channel, "Stream is not live.");
			}

			return;
		}
		long newConch = System.currentTimeMillis();

		if ((newConch >= (lastConch + 15 * 1000L)) || isOp) {
			if ((msg[0].equalsIgnoreCase(prefix + "conch") || msg[0]
					.equalsIgnoreCase(prefix + "helix")) && isSub) {
				log("RB: Matched command !conch");
				int rand = (int) Math.round(Math.random() * 14);
				if (msg[1].equalsIgnoreCase("#admin") && isAdmin) {
					rand = Integer.parseInt(msg[2]);
				}
				switch (rand) {
				case 0:
					send(channel, "It is certain.");
					break;
				case 1:
					send(channel, "It is decidedly so.");
					break;
				case 2:
					send(channel, "Better not to tell.");
					break;
				case 3:
					send(channel, "You may rely on it.");
					break;
				case 4:
					send(channel, "Don't count on it.");
					break;
				case 5:
					send(channel, "My reply is no.");
					break;
				case 6:
					send(channel, "Very doubtful.");
					break;
				case 7:
					send(channel, "My sources say no.");
					break;
				case 8:
					send(channel, "Most likely.");
					break;
				case 9:
					send(channel, "Signs point to yes.");
					break;
				case 10:
					send(channel, "Outlook doesn't look good.");
					break;
				case 11:
					send(channel, "The future seems hazy on this.");
					break;
				default:
					send(channel, "Unable to discern.");
					break;

				}
				lastConch = newConch;
			}
		}
		if (msg[0].equalsIgnoreCase(prefix + "punishstats") && isOp) {
			log("RB: Matched command !punishstats");

			long timeSince = channelInfo.timeSincePunished();
			int days = (int) (timeSince / 86400);
			int hours = (int) ((timeSince / 3600) % 24);
			int mins = (int) ((timeSince / 60) % 60);
			int seconds = (int) (timeSince % 60);
			String parsedSince = "";
			if (days > 0) {
				parsedSince = "It has been " + days + " days, " + hours
						+ " hours, " + mins + " minutes, and " + seconds
						+ " seconds since a punishment has been issued.";
			} else if (hours > 0) {
				parsedSince = "It has been " + hours + " hours, " + mins
						+ " minutes, and " + seconds
						+ " seconds since a punishment has been issued.";
			} else if (mins > 0) {
				parsedSince = "It has been " + mins + " minutes, and "
						+ seconds
						+ " seconds since a punishment has been issued.";

			} else {
				parsedSince = "It has been " + seconds
						+ " seconds since a punishment has been issued.";
			}
			send(channel, "The number of punishments doled out is "
					+ channelInfo.getPunCount() + ". " + parsedSince);
		}
		// +whatprefix
		if (msg[0].equalsIgnoreCase("+whatprefix") && isAdmin) {
			send(channel, "The prefix for this channel is: " + prefix);
		}
		// !altsend
		if (msg[0].equalsIgnoreCase(prefix + "altsend") && isAdmin) {
			if (msg.length > 2 && msg[1].startsWith("#")) {
				send(msg[1], fuseArray(msg, 2));
				send(channel,
						"Message sent to " + msg[1] + ": " + fuseArray(msg, 2));
			}
		}
		// !coetime
		if (msg[0].equalsIgnoreCase(prefix + "coetime")) {
			String time = Calendar.getInstance().getTime().toString();
			System.out.println(time);
			int indexColon = time.indexOf(":") - 2;
			int end = time.indexOf(" ", indexColon + 1);
			end = time.indexOf(" ", end + 1);
			time = time.substring(indexColon, end);
			send(channel, "CoeTime is currently: " + time);
		}
		// !uptime - All

		if (msg[0].equalsIgnoreCase(prefix + "uptime")) {
			log("RB: Matched command !uptime");
			try {
				String uptime = JSONUtil.krakenCreated_at(channelInfo
						.getTwitchName());
				send(channel, this.getTimeStreaming(uptime));
			} catch (Exception e) {
				send(channel, "Error accessing Twitch API.");
			}
			return;
		}

		String senderTriggered = "";
		if (msg[0].equalsIgnoreCase(prefix + "whalepenis")) {
			if (msg.length > 1 && isOp) {
				senderTriggered = sender;
				if (msg[1].equalsIgnoreCase("on")) {
					channelInfo.setWp(true);
					send(channel, "Whale penis timer has been turned on");

				} else if (msg[1].equalsIgnoreCase("off")) {
					channelInfo.setWp(false);
					send(channel, "Whale penis timer has been turned off");

				} else if (msg[1].equalsIgnoreCase("stats")) {

					long timeSince = channelInfo.timeSinceNoUpdate();
					int days = (int) (timeSince / 86400);
					int hours = (int) ((timeSince / 3600) % 24);
					int mins = (int) ((timeSince / 60) % 60);
					int seconds = (int) (timeSince % 60);

					if (days > 0) {
						send(channel,
								"It has been "
										+ days
										+ " days, "
										+ hours
										+ " hours, "
										+ mins
										+ " minutes, and "
										+ seconds
										+ " seconds since whale penis has last been mentioned. It has been mentioned "
										+ channelInfo.getWpCount() + " times.");
					} else if (hours > 0) {
						send(channel,
								"It has been "
										+ hours
										+ " hours, "
										+ mins
										+ " minutes, and "
										+ seconds
										+ " seconds since whale penis has last been mentioned. It has been mentioned "
										+ channelInfo.getWpCount() + " times.");
					} else if (mins > 0) {
						send(channel,
								"It has been "
										+ mins
										+ " minutes, and "
										+ seconds
										+ " seconds since whale penis has last been mentioned. It has been mentioned "
										+ channelInfo.getWpCount() + " times.");
					} else {
						send(channel,
								"It has been "
										+ seconds
										+ " seconds since whale penis has last been mentioned. It has been mentioned "
										+ channelInfo.getWpCount() + " times.");
					}
				} else
					send(channel, "Command syntax: " + prefix
							+ "whalepenis <on/off/stats>");
			}
		}

		// whale penis timer

		String combined = this.fuseArray(msg, 0);
		combined = combined.toLowerCase();

		if (((combined.indexOf("whalepenis") > -1) || (combined
				.indexOf("whale penis") > -1))
				&& channelInfo.getWp()
				&& !sender.equalsIgnoreCase(getNick())
				&& !sender.equalsIgnoreCase(senderTriggered)) {

			channelInfo.increaseWpCount();
			channelInfo.timeSinceSaid();

		}
		// !me
		if (msg[0].equalsIgnoreCase(prefix + "me") && isOp) {
			if (msg.length > 1) {
				String rest = fuseArray(msg, 1);
				sendCommand(channel, ".me " + rest);
			} else
				send(channel, "Useage is " + prefix + "me <string>");
		}
		// isLive
		if (msg[0].equalsIgnoreCase(prefix + "islive") && isOp) {
			if (msg.length > 1) {
				if (JSONUtil.krakenIsLive(msg[1].toLowerCase())) {
					send(channel, "Yes, " + msg[1].toLowerCase()
							+ " is streaming " + JSONUtil.krakenGame(msg[1])
							+ " to " + JSONUtil.krakenViewers(msg[1])
							+ " viewers right now.");
				} else
					send(channel, "No, " + msg[1].toLowerCase()
							+ " isn't streaming right now.");
			} else {
				if (JSONUtil.krakenIsLive(channel.substring(1))) {
					send(channel, "Yes, " + channel.substring(1)
							+ " is streaming right now.");
				} else
					send(channel, "No, " + channel.substring(1)
							+ " isn't streaming right now.");
			}
		}
		// !ishere
		// Possibly useful for balance?
		if (msg[0].equalsIgnoreCase(prefix + "ishere") && isOp) {
			if (msg.length > 1) {
				if (JSONUtil.tmiChatters(channel.substring(1)).contains(
						msg[1].toLowerCase())) {
					send(channel, "Yes, " + msg[1] + " is connected to chat.");
				} else
					send(channel, "No, " + msg[1]
							+ " is not connected to chat.");

			}
		}
		
		// !followme - Owner
		if (msg[0].equalsIgnoreCase(prefix + "followme") && isOwner
				&& BotManager.getInstance().twitchChannels) {
			log("RB: Matched command !followme");
			BotManager.getInstance().followChannel(twitchName);
			send(channel, "Follow update sent.");
			return;
		}
		
		// !properties - Owner
		if (msg[0].equalsIgnoreCase(prefix + "properties") && isOp
				&& BotManager.getInstance().twitchChannels) {
			log("RB: Matched command !properties");
			send(channel,
					JSONUtil.getChatProperties(channelInfo.getTwitchName()));
			return;
		}

		// !commands - Op
		if ((msg[0].equalsIgnoreCase(prefix + "commands") || msg[0]
				.equalsIgnoreCase(prefix + "coemands")) && isSub) {
			log("RB: Matched command !commands");

			 ArrayList<String> sorted = channelInfo.getCommandList();
			 String sortedList = "";
			 for (int i = 0; i < sorted.size(); i++) {
			 if (i == sorted.size() - 1) {
			 sortedList += sorted.get(i);
			 } else
			 sortedList += sorted.get(i) + ", ";
			 }
			 send(channel, "Commands: " + sortedList);

			//send(channel, "You can find the list of commands at coebot.tv/c/"
			//		+ twitchName + "/#commands");
			return;
		}
		// !throw - All
		// if (msg[0].equalsIgnoreCase(prefix + "throw") && (isSub)) {
		// log("RB: Matched command !throw");
		// if (msg.length > 1) {
		//
		// send(channel, " (Ã¢â€¢Â¯Ã‚Â°Ã¢â€“Â¡Ã‚Â°Ã¯Â¼â€°Ã¢â€¢Â¯Ã¥Â½Â¡ " +
		// fuseArray(msg, 1));
		// }
		//
		// }

		// !command - Ops
		if ((msg[0].equalsIgnoreCase(prefix + "command") || (msg[0]
				.equalsIgnoreCase(prefix + "coemand"))) && isOp) {
			log("RB: Matched command !command");
			if (msg.length < 3) {
				send(channel,
						"Syntax: \"!command add/delete [name] [message]\" - Name is the command trigger without \"!\" and message is the response.");
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add") && msg.length > 3) {
					String key = msg[2].replaceAll("[^a-zA-Z0-9]", "");
					key = key.toLowerCase();
					String value = fuseArray(msg, 3);

					channelInfo.setCommand(key, value, sender);
					if (value.contains("(_PURGE_)")
							|| value.contains("(_TIMEOUT_)")
							|| value.contains("(_BAN_)")
							|| value.contains("(_COMMERCIAL_)")
							|| (value.contains("(_VARS_") && (value
									.contains("_INCREMENT_") || value
									.contains("_DECREMENT_")))) {
						channelInfo.setCommandsRestriction(key, 2);
					} else
						channelInfo.setCommandsRestriction(key, 1);

					send(channel, "Command added/updated.");

				} else if (msg[1].equalsIgnoreCase("delete")
						|| msg[1].equalsIgnoreCase("remove")) {
					String key = msg[2].replaceAll("[^a-zA-Z0-9]", "");
					key = key.toLowerCase();
					boolean removed = channelInfo.removeCommand(key);

					channelInfo.removeRepeatCommand(key);
					channelInfo.removeScheduledCommand(key);
					if (removed) {
						send(channel, "Command " + key + " removed.");
					} else
						send(channel, "Command " + key + " doesn't exist.");

				} else if (msg[1].equalsIgnoreCase("restrict")
						&& msg.length >= 4) {
					String command = msg[2].toLowerCase();
					String levelStr = msg[3].toLowerCase();
					int level = 0;
					if (channelInfo.getCommand(command) != null) {
						if (levelStr.equalsIgnoreCase("owner")
								|| levelStr.equalsIgnoreCase("owners"))
							level = 3;
						if (levelStr.equalsIgnoreCase("mod")
								|| levelStr.equalsIgnoreCase("mods"))
							level = 2;
						if (levelStr.equalsIgnoreCase("regular")
								|| levelStr.equalsIgnoreCase("regulars"))
							level = 1;
						if (levelStr.equalsIgnoreCase("everyone"))
							level = 0;

						if (channelInfo.setCommandsRestriction(command, level))
							send(channel, prefix + command + " restricted to "
									+ levelStr + " only.");
						else
							send(channel, "Error setting restriction.");
					} else {
						send(channel, "Command does not exist.");
					}
				}
			}
			return;
		}	
		
		// ********************************************************************************
		// ********************************* Currency and Balance Commands
		// *************************************
		// ********************************************************************************
		
		// !tip
		if (msg[0].equalsIgnoreCase(prefix + "tip")) {
			if (msg.length < 3) {
				send(channel, "Syntax: \"tip [username] [number] - Username is the person and number is the ammount you wish to tip.\"");
			} else {
				String key = msg[1].replaceAll("[^a-zA-z0-9]", "");
				key = key.toLowerCase();
				Long balance = Long.valueOf(msg[2]);
				
				long bal = channelInfo.decreaseBalance(sender, balance);
				channelInfo.increaseBalance(key, bal);
				send(channel, sender + " tipped " + bal + " " + currency + " to " + key + ".");
				
			}
		}
		// Balance
		if ((msg[0].equalsIgnoreCase(prefix + "balance")) || (msg[0].equalsIgnoreCase(prefix + "bal"))) {
			log("RB: Matched command !balance");
			
			send(channel, "You have " + channelInfo.getBalance(sender) + " " + currency);
			
			return;
		}
		// !bet
		// !bet set [options]
		// !bet [options] [amount]
		// !bet winner [options]
		// !bet clear
		if ((msg[0].equalsIgnoreCase(prefix + "bet"))) {
			log("RB: Matched command !bet");
			
			if (msg.length < 2 && isOp) {
				send(channel,
						"Syntax: \"!bet set/winner/clear [options]\" - Options is something like 'yes/no/maybe' for set and 'yes'/'no'/'maybe' for winner.");
			} else if (msg.length < 2) {
				send(channel,
						"Syntax: \"!bet [option] [number]\" - Option is the option you want to choose and Number is the amount you want to bet.");
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("set") && isOp) {
					// !bet set [options]
					String key = msg[2].replaceAll("[^a-zA-z0-9]", "");
					
					send(channel,"This command does nothing yet, try again later.");
					
//					channelInfo.setBet(key);
//					send(channel,"Bet created with the options " + key + " use \"!bet [option] [amount]\" to enter your bet.");
				} else if (msg[1].equalsIgnoreCase("winner") || msg[1].equalsIgnoreCase("win") && isOp) { 
					// !bet winner [options]
					String key = msg[2].replaceAll("[^a-zA-z0-9]", "");
					
					send(channel,"This command does nothing yet, try again later.");
					
//					String winners = channelInfo.getBetWinners(key);
//					Long betWin = channelInfo.getBetWinAmount(key);
//					send(channel, "The following people have been given " + betWin + " for winning the bet: " + winners);
				} else if (msg[1].equalsIgnoreCase("clear") && isOp) {
					send(channel,"This command does nothing yet, try again later.");
					
//					channelInfo.clearBet();
//					send(channel, "The most recent bet has been cleared."
				} else { 
					// !bet [options] [amount]
					String key = msg[1].replaceAll("[^a-zA-z0-9]", "");
					Long balance = Long.valueOf(msg[2]);
					
					send(channel,"This command does nothing yet, try again later.");
					
//					channelInfo.enterBet(sender, key, balance);
//					channelInfo.increaseBalance("citizensbankbet", balance);
//					channelInfo.decreaseBalance(sender, balance);
				}
			}
		}
		
		// !currency - All
		if ((msg[0].equalsIgnoreCase(prefix + "currency")) || 
			(msg[0].equalsIgnoreCase(prefix + "curr")) || 
			(msg[0].equalsIgnoreCase(prefix + "cur"))) {
			log("RB: Matched command !currency");
			
			if (msg.length < 3) {
				send(channel,
						"Syntax: \"!currency set/clear/get/remove [username] [number]\" - Name is the username and number is the amount you wish to adjust.");
				
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("set") && msg.length > 3 && isOp) {
					String key = msg[2].replaceAll("[^a-zA-Z0-9]", "");
					key = key.toLowerCase();
					Long balance = Long.valueOf(msg[3]);//.replaceAll("[0-9]", "")).longValue();

					channelInfo.setBalance(key, balance);
					
//					channelInfo.saveBalance(true);
//					channelInfo.saveConfig(true);

					send(channel, key + " balance updated to " + balance + " " + currency + ".");

				} else if (msg[1].equalsIgnoreCase("clear") && isOp) {
					String key = msg[2].replaceAll("[^a-zA-Z0-9]", "");
					key = key.toLowerCase();
					//Long balance = balance;
					boolean removed = channelInfo.removeBalance(key, null);
					if (removed) {
						send(channel, "The balance of "+ key + " was cleared.");
					} else
						send(channel, key + " doesn't exist.");

				} else if (msg[1].equalsIgnoreCase("get")) {
					String key = msg[2].replaceAll("[^a-zA-Z0-9]", "");
					key=key.toLowerCase();
					
						send(channel, "The balance of " + key + " is " + channelInfo.getBalance(key) + " " + currency);
				} else if (msg[1].equalsIgnoreCase("remove") && isOp) {
					String key = msg[2].replaceAll("[^a-zA-Z0-9]", "");
					key = key.toLowerCase();
					Long balance = Long.valueOf(msg[3]);
					
					channelInfo.decreaseBalance(key, balance);
					
					send(channel, "The balance of " + key + " was decreased by " + balance + " " + currency + ".");
					
				} else if (msg[1].equalsIgnoreCase("add") && isOp) {
					String key = msg[2].replaceAll("[^a-zA-Z0-9]", "");
					key = key.toLowerCase();
					Long balance = Long.valueOf(msg[3]);
					
					channelInfo.increaseBalance(key, balance);
					
					send(channel, "The balance of " + key + " was increased by " + balance + " " + currency + ".");
					
				}
			}
			return;
		}
		/*
		// For debug purposes - Blues
		if ((msg[0].equalsIgnoreCase(prefix + "loadbal")) && isOp) {
			if (msg.length < 2) {
				send(channel, "Specify which balance file to load !loadbal [channel]");
			} else {
				String key = msg[1].replaceAll("[^a-zA-z0-9]", "");
				key = key.toLowerCase();
				
				channelInfo.loadBalances(key);
				send(channel, sender + " loaded " + key + " balance file.");
				
			}
			//send(channel, "Channel " + channel + "balances loaded.");
			
			return;
		}
		*/
		/*
		if ((msg[0].equalsIgnoreCase(prefix + "savebal")) && isOp) {
			log("RB: Matched command !balance");
			channelInfo.saveBalance(true);
			send(channel, "Channel " + channel + "balances saved.");
			
			return;
		}
		*/
		
		
		// !repeat - Ops
		if (msg[0].equalsIgnoreCase(prefix + "repeat") && isOp) {
			log("RB: Matched command !repeat");
			if (msg.length < 3) {
				if (msg.length > 1 && msg[1].equalsIgnoreCase("list")) {
					String commandsRepeatKey = "";

					Iterator itr = channelInfo.commandsRepeat.entrySet()
							.iterator();

					while (itr.hasNext()) {
						Map.Entry pairs = (Map.Entry) itr.next();
						RepeatCommand rc = (RepeatCommand) pairs.getValue();
						commandsRepeatKey += pairs.getKey() + " ["
								+ (rc.active == true ? "ON" : "OFF") + "] ("
								+ rc.delay + ")" + ", ";
					}
					send(channel, "Repeating commands: " + commandsRepeatKey);
				} else {
					send(channel,
							"Syntax: \"!repeat add/delete [commandname] [delay in seconds] [message difference - optional]\"");
				}
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add") && msg.length > 3) {
					String key = msg[2];
					try {
						int delay = Integer.parseInt(msg[3]);
						int difference = 1;
						if (msg.length == 5)
							difference = Integer.parseInt(msg[4]);

						if (channelInfo.getCommand(key).equalsIgnoreCase(
								"invalid")
								|| delay < 30) {
							// Key not found or delay to short
							send(channel,
									"Command not found or delay is less than 30 seconds.");
						} else {
							channelInfo
									.setRepeatCommand(key, delay, difference);
							send(channel, "Command " + key
									+ " will repeat every " + delay
									+ " seconds if " + difference
									+ " messages have passed.");
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}

				} else if (msg[1].equalsIgnoreCase("delete")
						|| msg[1].equalsIgnoreCase("remove")) {
					String key = msg[2];

					channelInfo.removeRepeatCommand(key);
					send(channel, "Command " + key + " will no longer repeat.");

				} else if (msg[1].equalsIgnoreCase("on")
						|| msg[1].equalsIgnoreCase("off")) {
					String key = msg[2];
					if (msg[1].equalsIgnoreCase("on")) {
						channelInfo.setRepeatCommandStatus(key, true);
						send(channel, "Repeat command " + key
								+ " has been enabled.");
					} else if (msg[1].equalsIgnoreCase("off")) {
						channelInfo.setRepeatCommandStatus(key, false);
						send(channel, "Repeat command " + key
								+ " has been disabled.");
					}

				}
			}
			return;
		}

		// !schedule - Ops
		if (msg[0].equalsIgnoreCase(prefix + "schedule") && isOp) {
			log("RB: Matched command !schedule");
			if (msg.length < 3) {
				if (msg.length > 1 && msg[1].equalsIgnoreCase("list")) {
					String commandsScheduleKey = "";

					Iterator itr = channelInfo.commandsSchedule.entrySet()
							.iterator();

					while (itr.hasNext()) {
						Map.Entry pairs = (Map.Entry) itr.next();
						ScheduledCommand sc = (ScheduledCommand) pairs
								.getValue();
						commandsScheduleKey += pairs.getKey() + " ["
								+ (sc.active == true ? "ON" : "OFF") + "]"
								+ ", ";
					}
					send(channel, "Scheduled commands: " + commandsScheduleKey);
				} else {
					send(channel,
							"Syntax: \"!schedule add/delete/on/off [commandname] [pattern] [message difference - optional]\"");
				}
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add") && msg.length > 3) {
					String key = msg[2];
					try {
						String pattern = msg[3];
						if (pattern.equals("hourly"))
							pattern = "0 * * * *";
						else if (pattern.equals("semihourly"))
							pattern = "0,30 * * * *";
						else
							pattern = pattern.replace("_", " ");

						int difference = 1;
						if (msg.length == 5)
							difference = Integer.parseInt(msg[4]);

						if (channelInfo.getCommand(key).equalsIgnoreCase(
								"invalid")
								|| pattern.contains(",,")) {
							// Key not found or delay to short
							send(channel,
									"Command not found or invalid pattern.");
						} else {
							channelInfo.setScheduledCommand(key, pattern,
									difference);
							send(channel, "Command " + key
									+ " will repeat every " + pattern + " if "
									+ difference + " messages have passed.");
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}

				} else if (msg[1].equalsIgnoreCase("delete")
						|| msg[1].equalsIgnoreCase("remove")) {
					String key = msg[2];
					channelInfo.removeScheduledCommand(key);
					send(channel, "Command " + key + " will no longer repeat.");

				} else if (msg[1].equalsIgnoreCase("on")
						|| msg[1].equalsIgnoreCase("off")) {
					String key = msg[2];
					if (msg[1].equalsIgnoreCase("on")) {
						channelInfo.setScheduledCommandStatus(key, true);
						send(channel, "Scheduled command " + key
								+ " has been enabled.");
					} else if (msg[1].equalsIgnoreCase("off")) {
						channelInfo.setScheduledCommandStatus(key, false);
						send(channel, "Scheduled command " + key
								+ " has been disabled.");
					}

				}
			}
			return;
		}

		// !autoreply - Ops
		if (msg[0].equalsIgnoreCase(prefix + "autoreply") && isOp) {
			log("RB: Matched command !autoreply");
			if (msg.length < 3) {
				if (msg.length > 1 && msg[1].equalsIgnoreCase("list")) {
					// for (int i = 0; i < channelInfo.autoReplyTrigger.size();
					// i++) {
					// String cleanedTrigger = channelInfo.autoReplyTrigger
					// .get(i).toString().replaceAll("\\.\\*", "*")
					// .replaceAll("\\\\Q", "")
					// .replaceAll("\\\\E", "");
					// send(channel,
					// "[" + (i + 1) + "] " + cleanedTrigger
					// + " ---> "
					// + channelInfo.autoReplyResponse.get(i));
					// }
					send(channel,
							"You can find this channel's autoreplies at coebot.tv/c/"
									+ twitchName + "/#autoreplies");
				} else {
					send(channel,
							"Syntax: \"!autoreply add/delete/list [pattern] [response]\"");
				}
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add") && msg.length > 3) {
					String pattern = msg[2].replaceAll("_", " ");
					String response = fuseArray(msg, 3);

					channelInfo.addAutoReply(pattern, response);
					send(channel, "Autoreply added.");
				} else if (msg[1].equalsIgnoreCase("editresponse")
						&& msg.length > 3) {
					if (Main.isInteger(msg[2])) {
						int pos = Integer.parseInt(msg[2]);
						String newResponse = fuseArray(msg, 3);
						if (channelInfo.editAutoReplyResponse(pos, newResponse))
							send(channel, "Autoreply response edited.");
						else
							send(channel,
									"Autoreply not found. Are you sure you have the correct number?");
					}
				} else if ((msg[1].equalsIgnoreCase("delete") || msg[1]
						.equalsIgnoreCase("remove")) && msg.length > 2) {
					if (Main.isInteger(msg[2])) {
						int pos = Integer.parseInt(msg[2]);

						if (channelInfo.removeAutoReply(pos))
							send(channel, "Autoreply removed.");
						else
							send(channel,
									"Autoreply not found. Are you sure you have the correct number?");
					}
				}
			}
			return;
		}
//		// !random - Ops
//		if ((msg[0].equalsIgnoreCase(prefix + "random") || msg[0]
//				.equalsIgnoreCase(prefix + "roll")) && isSub) {
//			log("RB: Matched command !random");
//
//			if (msg.length > 1) {
//				if (msg[1].equalsIgnoreCase("regular") && isOp) {
//					logMain("Matched command random regular");
//					ArrayList<String> onlineRegs = new ArrayList<String>();
//					ArrayList<String> chatters = JSONUtil
//							.tmiChatters(twitchName);
//					Set<String> regs = channelInfo.getRegulars();
//					for (String s : chatters) {
//						if (regs.contains(s.toLowerCase())) {
//							onlineRegs.add(s);
//						}
//					}
//					if (onlineRegs.size() > 0) {
//						String selected = onlineRegs
//								.get((int) (Math.random() * onlineRegs.size()));
//
//						send(channel, selected
//								+ " is the lucky random regular!");
//					} else
//						send(channel,
//								"No regulars are connected to chat right now.");
//				}
//				if (msg[1].equalsIgnoreCase("coin")) {
//					Random rand = new Random();
//					boolean coin = rand.nextBoolean();
//					if (coin == true)
//						send(channel, "Heads!");
//					else
//						send(channel, "Tails!");
//				} else if (isInteger(msg[1])) {
//					int randMax = Integer.parseInt(msg[1]);
//					if (randMax <= 0)
//						return;
//					long randReturn = Math
//							.round((Math.random() * (randMax - 1)) + 1);
//					send(channel, "You rolled: " + randReturn);
//				}
//			}
//			return;
//		}
		// ********************************************************************************
		// ***************************** Moderation Commands
		// ******************************
		// ********************************************************************************

		// Moderation commands - Ops
		if (isOp) {
			if (msg[0].equalsIgnoreCase("+m") && msg.length > 1) {
				int time = Integer.parseInt(msg[1]);
				sendCommand(channel, ".slow " + time);
			} else if (msg[0].equalsIgnoreCase("+m")) {
				sendCommand(channel, ".slow ");
			}
			if (msg[0].equalsIgnoreCase("-m")) {
				sendCommand(channel, ".slowoff");
			}
			if (msg[0].equalsIgnoreCase("+s")) {
				sendCommand(channel, ".subscribers");
			}
			if (msg[0].equalsIgnoreCase("-s")) {
				sendCommand(channel, ".subscribersoff");
			}
			if (msg.length > 0) {
				if (msg[0].equalsIgnoreCase("+b")
						|| msg[0].equalsIgnoreCase(prefix + "ban")) {
					sendCommand(channel, ".ban " + msg[1].toLowerCase());
					send(channel, msg[1].toLowerCase() + " was banned.");
					channelInfo.increasePunCount();
				}
				if (msg[0].equalsIgnoreCase("-b")) {
					sendCommand(channel, ".unban " + msg[1].toLowerCase());
					sendCommand(channel, ".timeout " + msg[1].toLowerCase()
							+ " 1");
					send(channel, msg[1].toLowerCase() + " was unbanned.");
				}
				if (msg[0].equalsIgnoreCase("+t")) {
					if (msg.length > 2) {
						sendCommand(channel, ".timeout " + msg[1].toLowerCase()
								+ " " + msg[2]);
						send(channel, msg[1].toLowerCase()
								+ " was timed out for " + msg[2] + " seconds.");
						channelInfo.increasePunCount();
					} else {
						sendCommand(channel, ".timeout " + msg[1].toLowerCase());
						send(channel, msg[1].toLowerCase() + " was timed out.");
						channelInfo.increasePunCount();
					}
				}
				if (msg[0].equalsIgnoreCase("-t")) {
					sendCommand(channel, ".timeout " + msg[1].toLowerCase()
							+ " 1");
					send(channel, msg[1].toLowerCase()
							+ " is no longer timed out.");
				}
				if (msg[0].equalsIgnoreCase("+p")) {
					sendCommand(channel, ".timeout " + msg[1].toLowerCase()
							+ " 1");
					send(channel, msg[1].toLowerCase()
							+ "'s chat history was purged.");
					channelInfo.increasePunCount();
				}
			}

		}

		// !clear - Ops
		if (msg[0].equalsIgnoreCase(prefix + "clear") && isOp) {
			log("RB: Matched command !clear");
			sendCommand(channel, ".clear");
			return;
		}
		
		// coebot ignores

		if (msg[0].equalsIgnoreCase(prefix + "ignore") && isOp) {
			if (msg.length == 2) {
				if (msg[1].equalsIgnoreCase("list")) {
					String tempList = "Ignored users: ";
					ArrayList<String> ignored = channelInfo.getIgnoredUsers();
					java.util.Collections.sort(ignored);
					for (int i = 0; i < ignored.size(); i++) {
						if (i == ignored.size() - 1) {
							tempList += (ignored.get(i));
						} else
							tempList += (ignored.get(i) + ", ");
					}
					send(channel, tempList);
				}
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add")) {
					if (msg[2].equalsIgnoreCase(channel.substring(1))) {
						send(channel,
								"You can't add the channel owner to the ignore list.");
						return;
					}
					if (channelInfo.addIgnoredUser(msg[2].toLowerCase())) {
						send(channel,
								msg[2].toLowerCase()
										+ " has been added to the ignore list for this channel.");

					} else
						send(channel,
								msg[2].toLowerCase()
										+ " is already on the ignore list for this channel.");

				} else if (msg[1].equalsIgnoreCase("remove")
						|| msg[1].equalsIgnoreCase("delete")) {
					if (channelInfo.removeIgnoredUser(msg[2].toLowerCase())) {
						send(channel,
								msg[2].toLowerCase()
										+ " was successfully removed from the ignore list for this channel.");
					} else
						send(channel, msg[2].toLowerCase()
								+ " was not on this channel's ignore list.");
				}
			}
		}

		// !regular - Owner
		if ((msg[0].equalsIgnoreCase(prefix + "regular") || msg[0]
				.equalsIgnoreCase(prefix + "regulars")) && isOp) {
			log("RB: Matched command !regular");
			if (msg.length < 2) {
				send(channel,
						"Syntax: \"!regular add/delete [name]\", \"!regular list\"");
			} else if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add")) {
					if (channelInfo.isRegular(msg[2])) {
						send(channel, "User already exists." + "(" + msg[2]
								+ ")");
					} else {
						channelInfo.addRegular(msg[2]);
						send(channel, "User added. " + "(" + msg[2] + ")");
					}
				} else if (msg[1].equalsIgnoreCase("delete")
						|| msg[1].equalsIgnoreCase("remove")) {
					if (channelInfo.isRegular(msg[2])) {
						channelInfo.removeRegular(msg[2]);
						send(channel, "User removed." + "(" + msg[2] + ")");
					} else {
						send(channel, "User does not exist. " + "(" + msg[2]
								+ ")");
					}
				}
			} else if (msg.length > 1 && msg[1].equalsIgnoreCase("list")
					&& isOp) {
				String tempList = "Regulars: ";
				ArrayList<String> arrRegs = new ArrayList<String>();
				for (String s : channelInfo.getRegulars()) {
					arrRegs.add(s);
				}
				java.util.Collections.sort(arrRegs);
				for (int i = 0; i < arrRegs.size(); i++) {
					if (i == arrRegs.size() - 1) {
						tempList += (arrRegs.get(i));
					} else
						tempList += (arrRegs.get(i) + ", ");
				}
				send(channel, tempList);
			}
			return;
		}

		// !mod - Owner
		if (msg[0].equalsIgnoreCase(prefix + "mod") && isOwner) {
			log("RB: Matched command !mod");
			if (msg.length < 2) {
				send(channel,
						"Syntax: \"!mod add/delete [name]\", \"!mod list\"");
			}
			if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add")) {
					if (channelInfo.isModerator(msg[2])) {
						send(channel, "User already exists. " + "(" + msg[2]
								+ ")");
					} else {
						channelInfo.addModerator(msg[2]);
						send(channel, "User added. " + "(" + msg[2] + ")");
					}
				} else if (msg[1].equalsIgnoreCase("delete")
						|| msg[1].equalsIgnoreCase("remove")) {
					if (channelInfo.isModerator(msg[2])) {
						channelInfo.removeModerator(msg[2]);
						send(channel, "User removed. " + "(" + msg[2] + ")");
					} else {
						send(channel, "User does not exist. " + "(" + msg[2]
								+ ")");
					}
				}
			} else if (msg.length > 1 && msg[1].equalsIgnoreCase("list")
					&& isOwner) {
				String tempList = "Moderators: ";
				ArrayList<String> arrRegs = new ArrayList<String>();
				for (String s : channelInfo.getModerators()) {
					arrRegs.add(s);
				}
				java.util.Collections.sort(arrRegs);
				for (int i = 0; i < arrRegs.size(); i++) {
					if (i == arrRegs.size() - 1) {
						tempList += (arrRegs.get(i));
					} else
						tempList += (arrRegs.get(i) + ", ");
				}
				send(channel, tempList);
			}
			return;
		}

		// !owner - Owner
		if (msg[0].equalsIgnoreCase(prefix + "owner") && isOwner) {
			log("RB: Matched command !owner");
			if (msg.length < 2) {
				send(channel,
						"Syntax: \"!owner add/delete [name]\", \"!owner list\"");
			}
			if (msg.length > 2) {
				if (msg[1].equalsIgnoreCase("add")) {
					if (channelInfo.isOwner(msg[2])) {
						send(channel, "User already exists. " + "(" + msg[2]
								+ ")");
					} else {
						channelInfo.addOwner(msg[2]);
						send(channel, "User added. " + "(" + msg[2] + ")");
					}
				} else if (msg[1].equalsIgnoreCase("delete")
						|| msg[1].equalsIgnoreCase("remove")) {
					if (channelInfo.isOwner(msg[2])) {
						channelInfo.removeOwner(msg[2]);
						send(channel, "User removed. " + "(" + msg[2] + ")");
					} else {
						send(channel, "User does not exist. " + "(" + msg[2]
								+ ")");
					}
				}
			} else if (msg.length > 1 && msg[1].equalsIgnoreCase("list")
					&& isOwner) {
				String tempList = "Owners: ";
				ArrayList<String> arrRegs = new ArrayList<String>();
				for (String s : channelInfo.getOwners()) {
					arrRegs.add(s);
				}
				java.util.Collections.sort(arrRegs);
				for (int i = 0; i < arrRegs.size(); i++) {
					if (i == arrRegs.size() - 1) {
						tempList += (arrRegs.get(i));
					} else
						tempList += (arrRegs.get(i) + ", ");
				}
				send(channel, tempList);
			}
			return;
		}
		// custom commands from another channel
		if (msg[0].startsWith(prefix + "#") && isOwner && msg[0].contains("/")) {

			String otherChannel = msg[0].substring(1, msg[0].indexOf("/"));
			Channel newChannelInfo = getChannelObject(otherChannel);

			logMain(otherChannel);
			String command = msg[0].substring(msg[0].indexOf("/") + 1);
			logMain(command);
			String value = newChannelInfo.getCommand(command);
			logMain(value);
			if (value != null) {
				log("RB: Matched command " + command);

				if (value.contains("(_PURGE_)")) {
					value = value.replace("(_PURGE_)", msg[1].toLowerCase());
					sendCommand(channel, ".timeout " + msg[1].toLowerCase()
							+ " 1");
				} else if (value.contains("(_TIMEOUT_)")) {
					value = value.replace("(_TIMEOUT_)", msg[1].toLowerCase());
					sendCommand(channel, ".timeout " + msg[1].toLowerCase());

				} else if (value.contains("(_BAN_)")) {
					value = value.replace("(_BAN_)", msg[1].toLowerCase());
					sendCommand(channel, ".ban " + msg[1].toLowerCase());
				}
				if (value.contains("(_PARAMETER_)")) {

					String[] parts = fuseArray(msg, 1).split(";");
					if (parts.length > 1) {
						for (String s : parts) {
							value = value.replaceFirst("\\(_PARAMETER_\\)",
									s.trim());
						}
					} else
						value = value.replace("(_PARAMETER_)", parts[0]);

				}
				if (value.contains("(_PARAMETER_CAPS_)")) {

					String[] parts = fuseArray(msg, 1).split(";");
					if (parts.length > 1) {
						for (String s : parts) {
							value = value.replaceFirst(
									"\\(_PARAMETER_CAPS_\\)", s.trim());
						}
					} else
						value = value.replace("(_PARAMETER_CAPS_)",
								parts[0].toUpperCase());

				}

				send(channel, sender, value);

			}
		}

		// !set - Owner
		if (msg[0].equalsIgnoreCase(prefix + "set") && isOp) {
			log("RB: Matched command !set");
			if (msg.length == 1) {
				send(channel,
						"Syntax: \"!set [option] [value]\". Options: joinsparts, mode, chatlogging");
			} // setbullet
			else if (msg[1].equalsIgnoreCase("bullet")) {
				if (msg.length > 2) {
					if (!msg[2].startsWith("/") && !msg[2].startsWith(".")) {
						bullet[0] = msg[2];
						channelInfo.setBullet(msg[2]);
						send(channel, "Bullet is now set to \"" + bullet[0]
								+ "\"");
					} else
						send(channel, "Bullet cannot start with \"/\" or \".\"");

				} else
					send(channel, "Usage is " + prefix
							+ "set bullet <new bullet>");
			}// setSubsRegsMinusLinks
			else if (msg[1].equalsIgnoreCase("subsRegsMinusLinks")) {
				if (msg.length > 2) {
					if (msg[2].equalsIgnoreCase("on")) {
						channelInfo.setSubsRegsMinusLinks(true);
						send(channel,
								"Subscribers are now considered regulars except for the ability to post links.");
					} else if (msg[2].equalsIgnoreCase("off")) {
						channelInfo.setSubsRegsMinusLinks(false);
						send(channel,
								"Subscribers are now considered non-regulars.");
					} else
						send(channel, "Syntax is " + prefix
								+ "set subsRegsMinusLinks <on/off>");
				} else
					send(channel, "Syntax is " + prefix
							+ "set subsRegsMinusLinks <on/off>");
			}

			// set cooldown for custom commands
			else if (msg[1].equalsIgnoreCase("cooldown")) {
				if (msg.length > 2) {
					int newCooldown = Integer.parseInt(msg[2]);
					channelInfo.setCooldown(newCooldown);
					send(channel, "Cooldown for custom commands is now "
							+ newCooldown + " seconds.");
				} else
					send(channel, "Usage is " + prefix
							+ "setbullet <new bullet>");
			}   else if (msg[1].equalsIgnoreCase("mode")) {
				if (msg.length < 3) {
					send(channel, "Mode set to " + channelInfo.getMode() + "");
				} else if ((msg[2].equalsIgnoreCase("0") || msg[2]
						.equalsIgnoreCase("owner")) && isOwner) {
					channelInfo.setMode(0);
					send(channel, "Mode set to admin/owner only.");
				} else if (msg[2].equalsIgnoreCase("1")
						|| msg[2].equalsIgnoreCase("mod")) {
					channelInfo.setMode(1);
					send(channel, "Mode set to admin/owner/mod only.");
				} else if (msg[2].equalsIgnoreCase("2")
						|| msg[2].equalsIgnoreCase("everyone")) {
					channelInfo.setMode(2);
					send(channel, "Mode set to everyone.");
				} else if (msg[2].equalsIgnoreCase("-1")
						|| msg[2].equalsIgnoreCase("admin")) {
					channelInfo.setMode(-1);
					send(channel, "Special moderation mode activated.");
				}
			// Prefix
			} else if (msg[1].equalsIgnoreCase("prefix")) {
				if (msg.length > 2) {
					if (msg[2].length() > 1) {
						send(channel, "Prefix may only be 1 character.");
					} else if (msg[2].equals("/") || msg[2].equals(".")) {
						send(channel, "Command prefix cannot be / or .");
					} else {
						channelInfo.setPrefix(msg[2]);
						send(channel,
								"Command prefix is " + channelInfo.getPrefix());
					}
				} else {
					send(channel,
							"Command prefix is " + channelInfo.getPrefix());
				}
			// Currency Name
			} else if (msg[1].equalsIgnoreCase("currency")) {
					channelInfo.setCurrencyName(msg[2]);
					send(channel, "Currency name is "+ channelInfo.getCurrencyName());				
			} else if (msg[1].equalsIgnoreCase("emoteset") && msg.length > 2) {
				channelInfo.setEmoteSet(msg[2]);
				send(channel,
						"Emote set ID set to " + channelInfo.getEmoteSet());
			} else if (msg[1].equalsIgnoreCase("subscriberregulars")) {
				if (msg[2].equalsIgnoreCase("on")) {
					channelInfo.setSubscriberRegulars(true);
					send(channel,
							"Subscribers will now be treated as regulars.");
				} else if (msg[2].equalsIgnoreCase("off")) {
					channelInfo.setSubscriberRegulars(false);
					send(channel,
							"Subscribers will no longer be treated as regulars.");
				}
			}
			return;
		}
		// !modchan - Mod
		if (msg[0].equalsIgnoreCase(prefix + "modchan") && isOwner) {
			log("RB: Matched command !modchan");
			if (channelInfo.getMode() == 2) {
				channelInfo.setMode(1);
				send(channel, "Mode set to admin/owner/mod only.");
			} else if (channelInfo.getMode() == 1) {
				channelInfo.setMode(2);
				send(channel, "Mode set to everyone.");
			} else {
				send(channel, "Mode can only be changed by bot admin.");
			}
			return;
		}

		// !join
		if (msg[0].equalsIgnoreCase(prefix + "join")
				&& channel.equalsIgnoreCase("#" + getNick())) {
			log("RB: Matched command !join");

			if (!BotManager.getInstance().publicJoin) {
				send(channel, "Public joining is disabled at this time.");
				return;
			}

			if (JSONUtil.krakenChannelExist(sender)) {
				send(channel, "Joining channel #" + sender + ".");
				boolean joinStatus = BotManager.getInstance().addChannel(
						"#" + sender, 2);
				boolean createStatus = false;
				String created = BotManager.getInstance().coebotJoinChannel(
						sender, getNick());
				if (created.equalsIgnoreCase("ok"))
					createStatus = true;
				if (joinStatus && createStatus) {
					send(channel, "Channel #" + sender + " joined.");

				} else {
					send(channel, "Already in channel #" + sender
							+ " or could not join.");
				}
			} else {
				send(channel,
						"Unable to join "
								+ sender
								+ ". This could be because your channel is on Justin.tv and not Twitch. If you are sure your channel is on Twitch, try again later.");
			}
			return;
		}

		if (msg[0].equalsIgnoreCase(prefix + "rejoin")) {
			log("RB: Matched command !rejoin");
			if (msg.length > 1 && isAdmin) {
				if (msg[1].contains("#")) {
					send(channel, "Rejoining channel " + msg[1] + ".");
					boolean joinStatus = BotManager.getInstance()
							.rejoinChannel(msg[1]);
					if (joinStatus) {
						send(channel, "Channel " + msg[1] + " rejoined.");
					} else {
						send(channel, "Bot is not assigned to channel "
								+ msg[1] + ".");
					}

				} else {
					send(channel,
							"Invalid channel format. Must be in format #channelname.");
				}
			} else {
				send(channel, "Rejoining channel #" + sender + ".");
				boolean joinStatus = BotManager.getInstance().rejoinChannel(
						"#" + sender);
				if (joinStatus) {
					send(channel, "Channel #" + sender + " rejoined.");
				} else {
					send(channel, "Bot is not assigned to channel #" + sender
							+ ".");
				}
			}
			return;
		}

		// ********************************************************************************
		// **************************** Administration Commands
		// ***************************
		// ********************************************************************************

		if (msg[0].equalsIgnoreCase(prefix + "admin") && isAdmin
				&& msg.length > 1) {
			if (msg[1].equalsIgnoreCase("channels")) {
				send(channel, "Currently in "
						+ BotManager.getInstance().channelList.size()
						+ " channels.");
				String channelString = "";
				for (Map.Entry<String, Channel> entry : BotManager
						.getInstance().channelList.entrySet()) {
					channelString += entry.getValue().getChannel() + ", ";
				}
				send(channel, "Channels: " + channelString);
				return;
			} else if (msg[1].equalsIgnoreCase("join") && msg.length > 2) {
				if (msg[2].contains("#")) {
					String toJoin = msg[2];
					int mode = 2;
					if (msg.length > 3 && Main.isInteger(msg[3]))
						mode = Integer.parseInt(msg[3]);
					send(channel, "Joining channel " + toJoin + " with mode ("
							+ mode + ").");
					boolean joinStatus = BotManager.getInstance().addChannel(
							toJoin, 2);

					boolean createStatus = false;
					String created = BotManager.getInstance()
							.coebotJoinChannel(toJoin.substring(1), getNick());
					if (created.equalsIgnoreCase("ok"))
						createStatus = true;
					if (joinStatus && createStatus) {
						send(channel, "Channel #" + toJoin + " joined.");

					} else {
						send(channel, "Already in channel #" + toJoin
								+ " or could not join.");
					}

				} else {
					send(channel,
							"Invalid channel format. Must be in format #channelname.");
				}
				return;
			} else if (msg[1].equalsIgnoreCase("part") && msg.length > 2) {
				if (msg[2].contains("#")) {
					String toPart = msg[2];
					send(channel, "Channel " + toPart + " parting...");
					BotManager.getInstance().removeChannel(toPart);
					BotManager.getInstance().coebotPartChannel(
							toPart.substring(1), getNick());
					send(channel, "Channel " + toPart + " parted.");
				} else {
					send(channel,
							"Invalid channel format. Must be in format #channelname.");
				}
				return;
			} else if (msg[1].equalsIgnoreCase("reconnect")) {
				send(channel, "Reconnecting all servers.");
				BotManager.getInstance().reconnectAllBotsSoft();
				return;
			} else if (msg[1].equalsIgnoreCase("reload") && msg.length > 2) {
				if (msg[2].contains("#")) {
					String toReload = msg[2];
					send(channel, "Reloading channel " + toReload);
					BotManager.getInstance().reloadChannel(toReload);
					send(channel, "Channel " + toReload + " reloaded.");
				} else {
					send(channel,
							"Invalid channel format. Must be in format #channelname.");
				}
				return;
			} else if (msg[1].equalsIgnoreCase("color") && msg.length > 2) {
				sendCommand(channel, ".color " + msg[2]);
				send(channel, "Color set to " + msg[2]);
				return;
			} else if (msg[1].equalsIgnoreCase("loadfilter")) {
				BotManager.getInstance().loadGlobalBannedWords();
				BotManager.getInstance().loadBanPhraseList();
				send(channel, "Global banned filter reloaded.");
				return;
			} else if (msg[1].equalsIgnoreCase("spam")) {
				if (msg.length > 3 && Main.isInteger(msg[2])) {
					String toSpam = fuseArray(msg, 3);
					for (int i = 0; i < Integer.parseInt(msg[2]); i++)
						send(channel, toSpam + " " + (i + 1));
					return;
				}
			} else if (msg[1].startsWith("#")) {
				if (msg.length > 2) {

					onChannelMessage(channel, msg[1], sender, fuseArray(msg, 2));
				}
			}
		}
		// ********************************************************************************
		// ***************************** Info/Catch-all Command
		// ***************************
		// ********************************************************************************

		long cooldown = channelInfo.getCooldown() * 1L;

		if (msg[0].substring(0, 1).equalsIgnoreCase(prefix)) {
			String command = msg[0].substring(1).toLowerCase();
			String value = channelInfo.getCommand(command);
			if (value != null) {
				log("RB: Matched command " + msg[0]);

				if (channelInfo.checkCommandRestriction(command, accessLevel)) {
					long currentTime = System.currentTimeMillis();
					if (currentTime > (lastCommand + cooldown * 1000L) || isOp) {
						lastCommand = currentTime;

						if (value.contains("(_PURGE_)")) {
							value = value.replace("(_PURGE_)",
									msg[1].toLowerCase());
							sendCommand(channel,
									".timeout " + msg[1].toLowerCase() + " 1");
						} else if (value.contains("(_TIMEOUT_)")) {
							value = value.replace("(_TIMEOUT_)",
									msg[1].toLowerCase());
							sendCommand(channel,
									".timeout " + msg[1].toLowerCase());

						} else if (value.contains("(_BAN_)")) {
							value = value.replace("(_BAN_)",
									msg[1].toLowerCase());
							sendCommand(channel, ".ban " + msg[1].toLowerCase());
						}
						if (value.contains("(_PARAMETER_)")) {

							String[] parts = fuseArray(msg, 1).split(";");
							if (parts.length > 1) {
								for (String s : parts) {
									value = value.replaceFirst(
											"\\(_PARAMETER_\\)", s.trim());
								}
							} else
								value = value
										.replace("(_PARAMETER_)", parts[0]);

						}
						if (value.contains("(_PARAMETER_CAPS_)")) {

							String[] parts = fuseArray(msg, 1).split(";");
							if (parts.length > 1) {
								for (String s : parts) {
									value = value.replaceFirst(
											"\\(_PARAMETER_CAPS_\\)", s.trim());
								}
							} else
								value = value.replace("(_PARAMETER_CAPS_)",
										parts[0].toUpperCase());

						}
						channelInfo.increaseCommandCount(command);
						send(channel, sender, value);

					}
				}

			}

		}

		// ********************************************************************************
		// *********************************** Auto Reply
		// *********************************
		// ********************************************************************************
		boolean matched = false;
		for (int i = 0; i < channelInfo.autoReplyTrigger.size(); i++) {
			Matcher m = channelInfo.autoReplyTrigger.get(i).matcher(message);
			if (m.matches()) {
				if (matched) {
					matched = false;
					break;
				}
				matched = true;
				if (!channelInfo.onCooldown(channelInfo.autoReplyTrigger.get(i)
						.toString())) {
					String value = channelInfo.autoReplyResponse.get(i);
					if (value.contains("(_REGULARS_ONLY_)")) {
						if (isSub) {
							value = value.replace("(_REGULARS_ONLY_)", "");
							if (value.contains("(_PURGE_)")) {
								value = value.replace("(_PURGE_)", sender);
								sendCommand(channel, ".timeout " + sender
										+ " 1");
							} else if (value.contains("(_TIMEOUT_)")) {
								value = value.replace("(_TIMEOUT_)", sender);
								sendCommand(channel, ".timeout " + sender);

							} else if (value.contains("(_BAN_)")) {
								value = value.replace("(_BAN_)", sender);
								sendCommand(channel, ".ban " + sender);
							}
							send(channel, sender, value);
							channelInfo
									.registerCommandUsage(channelInfo.autoReplyTrigger
											.get(i).toString());
						}
					} else {

						if (value.contains("(_PURGE_)")) {
							value = value.replace("(_PURGE_)", sender);
							sendCommand(channel, ".timeout " + sender + " 1");
						} else if (value.contains("(_TIMEOUT_)")) {
							value = value.replace("(_TIMEOUT_)", sender);
							sendCommand(channel, ".timeout " + sender);

						} else if (value.contains("(_BAN_)")) {
							value = value.replace("(_BAN_)", sender);
							sendCommand(channel, ".ban " + sender);
						}

						send(channel, sender, value);
						channelInfo
								.registerCommandUsage(channelInfo.autoReplyTrigger
										.get(i).toString());
					}
				}
			}
		}
	}

	protected void onAdministrativeMessage(String message, Channel channelInfo) {
		String[] msg = message.trim().split(" ");

		if (msg.length > 0) {
			if (msg[0].equalsIgnoreCase("SPECIALUSER")) {
				String user = msg[1];
				String tag = msg[2];

				if (tag.equalsIgnoreCase("admin")
						|| tag.equalsIgnoreCase("staff"))
					BotManager.getInstance().addTagAdmin(user);
				if (tag.equalsIgnoreCase("staff"))
					BotManager.getInstance().addTagStaff(user);
				if (tag.equalsIgnoreCase("subscriber") && channelInfo != null) {
					if (!user.equalsIgnoreCase("Coebot")) {
						privMsgSub = true;
					}

				}
				if (tag.equalsIgnoreCase("subscriber") && channelInfo == null) {
					if (!user.equalsIgnoreCase("Coebot")) {
						privMsgSub = true;
					}
				}

			} else if (msg[0].equalsIgnoreCase("CLEARCHAT")) {
				if (msg.length > 1) {
					String user = msg[1];
					if (!BotManager.getInstance().verboseLogging)
						System.out.println("RAW: CLEARCHAT " + user);
				} else {
					if (!BotManager.getInstance().verboseLogging)
						System.out.println("RAW: CLEARCHAT");
				}
			} else if (msg[0].equalsIgnoreCase("HISTORYEND")) {
				String channel = msg[1];
				Channel ci = BotManager.getInstance().getChannel("#" + channel);
				ci.active = true;

			} else if (msg[0].equalsIgnoreCase("EMOTESET")) {
				String user = msg[1];
				String setsList = msg[2].replaceAll("(\\[|\\])", "");
				String[] sets = setsList.split(",");
				for (String s : sets)
					BotManager.getInstance().addSubBySet(user, s);
			}
		}
	}


	@Override
	public void onDisconnect() {
		lastPing = -1;
		try {
			System.out.println("INFO: Internal reconnection: "
					+ this.getServer());
			String[] channels = this.getChannels();
			try {
				System.out
						.println("Sleeping for 20 seconds to allow for more JOINs");
				Thread.sleep(20000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			this.reconnect();
			for (int i = 0; i < channels.length; i++) {
				this.joinChannel(channels[i]);
				try {
					Thread.sleep(350);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
		} catch (NickAlreadyInUseException e) {
			logMain("RB: [ERROR] Nickname already in use - " + this.getNick()
					+ " " + this.getServer());
		} catch (IOException e) {
			logMain("RB: [ERROR] Unable to connect to server - "
					+ this.getNick() + " " + this.getServer());
		} catch (IrcException e) {
			logMain("RB: [ERROR] Error connecting to server - "
					+ this.getNick() + " " + this.getServer());
		}

	}

	public void onJoin(String channel, String sender, String login,
			String hostname) {

		Channel channelInfo = getChannelObject(channel);

		if (channelInfo == null)
			return;

		if (this.getNick().equalsIgnoreCase(sender)) {
			log("RB: Got self join for " + channel);
			if (BotManager.getInstance().ignoreHistory) {
				System.out.println("DEBUG: Marking " + channel
						+ " as inactive.");
				channelInfo.active = false;
			}
		}
	}

	public void onPart(String channel, String sender, String login,
			String hostname) {

		Channel channelInfo = getChannelObject(channel);

		if (channelInfo == null)
			return;
	}

	public void send(String target, String sender, String message) {
		send(target, sender, message, null);
	}

	public void send(String target, String message) {
		send(target, null, message, null);
	}

	public void send(String target, String sender, String message, String[] args) {
		if (msgTimer.size() > 19) {

			msgTimer.add(System.currentTimeMillis());
			System.out.println(msgTimer.size());

			long diff = 0;
			try {
				diff = msgTimer.get(20) - msgTimer.get(0);
			} catch (Exception e) {
				logMain("RESETTING THE MSGTIMER QUEUE");
				e.printStackTrace();
				msgTimer = new ArrayList<Long>();
			}
			log("RB: There are " + msgTimer.size()
					+ " times in msgTimer. Diff = " + diff);
			if (diff > 30 * 1000L) {

				msgTimer.remove(0);
				Channel channelInfo = getChannelObject(target);

				if (!BotManager.getInstance().verboseLogging)
					logMain("SEND: " + target + " " + getNick() + " : "
							+ message);

				message = MessageReplaceParser.parseMessage(target, sender,
						message, args);
				boolean useBullet = true;

				if (message.startsWith("/me "))
					useBullet = false;

				// Split if message > X characters
				if (message.length() > 0) {
					List<String> chunks = Main.splitEqually(message, 500);
					int c = 1;
					if (target == null) {
						sendMessage(target, "The bullet is null.");
					}
					for (String chunk : chunks) {
						sendMessage(target,
								(useBullet ? channelInfo.getChannelBullet()
										+ " " : "")
										+ (chunks.size() > 1 ? "[" + c + "] "
												: "") + chunk);
						c++;
						useBullet = true;
					}
				}

				if (tried) {
					delete = true;
					tried = false;
				}
				checkQueued();
			} else {
				msgTimer.remove(20);
				log("RB: Prevented overflow of messages that would result in a ban.");
				QueuedMessage qm = new QueuedMessage(target, sender, message,
						args);
				boolean matchesAny = false;
				for (int i = 0; i < queuedMessages.size(); i++) {
					if (queuedMessages.get(i).getMessage().equals(message)
							&& queuedMessages.get(i).getTarget().equals(target))
						matchesAny = true;
				}
				if (!matchesAny) {
					queuedMessages.add(qm);
				}

			}
		} else {
			log("RB: seeding msgTimer list");
			for (int i = 0; i < (20); i++) {
				msgTimer.add(System.currentTimeMillis() - 31000L);
			}
			QueuedMessage qm = new QueuedMessage(target, sender, message, args);
			queuedMessages.add(qm);
		}

	}

	public void checkQueued() {
		log("There are " + queuedMessages.size() + " queued messages");
		if (delete) {
			queuedMessages.remove(0);
			delete = false;
		}
		if (queuedMessages.size() > 0) {
			tried = true;
			QueuedMessage qm = queuedMessages.get(0);
			if (qm.isCommand()) {

				sendCommand(qm.getTarget(), qm.getMessage());
			} else {
				send(qm.getTarget(), qm.getSender(), qm.getMessage(),
						qm.getArgs());

			}
		}

	}

	public void sendCommand(String target, String message) {
		if (msgTimer.size() > 19) {
			System.out.println(msgTimer.size());
			msgTimer.add(System.currentTimeMillis());

			long diff = 0;
			try {
				diff = msgTimer.get(20) - msgTimer.get(0);
			} catch (Exception e) {
				logMain("RESETTING THE MSGTIMER QUEUE");
				e.printStackTrace();
				msgTimer = new ArrayList<Long>();
			}

			log("RB: There are " + msgTimer.size()
					+ " times in msgTimer. Diff = " + diff);

			if (diff > 30 * 1000L) {
				msgTimer.remove(0);

				sendMessage(target, message);
				if (tried) {
					delete = true;
					tried = false;
				}
				checkQueued();

			} else {
				msgTimer.remove(20);
				log("RB: Prevented overflow of messages that would result in a ban.");
				QueuedMessage qm = new QueuedMessage(target, message, true);
				boolean matchesAny = false;
				for (int i = 0; i < queuedMessages.size(); i++) {
					if (queuedMessages.get(i).getMessage().equals(message)
							&& queuedMessages.get(i).getTarget().equals(target))
						matchesAny = true;
				}
				if (!matchesAny)
					queuedMessages.add(qm);

			}
		} else {
			log("RB: seeding msgTimer list");
			for (int i = 0; i < (20); i++) {
				msgTimer.add(System.currentTimeMillis() - 31000L);
			}

		}
	}

	@Override
	public void onServerPing(String response) {
		super.onServerPing(response);
		lastPing = (int) (System.currentTimeMillis() / 1000);
	}

	public void log(String line) {
		if (this.getVerbose()) {
			logMain(System.currentTimeMillis() + " " + line);
		}
	}

	public void logMain(String line) {
		BotManager.getInstance().log(line);
	}

	private void startJoinCheck() {

		joinCheck = new Timer();

		int delay = 60000;

		joinCheck.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				String[] currentChanList = ReceiverBot.this.getChannels();
				for (Map.Entry<String, Channel> entry : BotManager
						.getInstance().channelList.entrySet()) {
					boolean inList = false;
					for (String c : currentChanList) {
						if (entry.getValue().getChannel().equals(c))
							inList = true;
					}

					if (!inList) {
						log("RB: " + entry.getValue().getChannel()
								+ " is not in the joined list.");
						ReceiverBot.this.joinChannel(entry.getValue()
								.getChannel());
						try {
							Thread.sleep(350);
						} catch (InterruptedException e) {

							e.printStackTrace();
						}
					}

				}
			}
		}, delay, delay);

	}
	
	public boolean isGlobalBannedWord(String message) {
		for (Pattern reg : BotManager.getInstance().globalBannedWords) {
			Matcher match = reg.matcher(message.toLowerCase());
			if (match.matches()) {
				log("RB: Global banned word matched: " + reg.toString());
				return true;
			}
		}
		return false;
	}

	public String getTimeStreaming(String uptime) {
		uptime = uptime.replace("Z", "UTC");
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
		try {
			Date then = format.parse(uptime);
			return "Streaming for " + this.getTimeTilNow(then) + ".";
		} catch (ParseException e) {
			e.printStackTrace();

		}

		return "An error occurred or stream is offline.";
	}

	public boolean checkStalePing() {
		if (lastPing == -1)
			return false;

		int difference = ((int) (System.currentTimeMillis() / 1000)) - lastPing;

		if (difference > BotManager.getInstance().pingInterval) {
			log("RB: Ping is stale. Last ping= " + lastPing + " Difference= "
					+ difference);
			lastPing = -1;
			return true;
		}

		return false;
	}

	private String fuseArray(String[] array, int start) {
		String fused = "";
		for (int c = start; c < array.length; c++)
			fused += array[c] + " ";

		return fused.trim();

	}

	public String getTimeTilNow(Date date) {
		long difference = (long) (System.currentTimeMillis() / 1000)
				- (date.getTime() / 1000);
		String returnString = "";

		if (difference >= 86400) {
			int days = (int) (difference / 86400);
			returnString += days + "d ";
			difference -= days * 86400;
		}
		if (difference >= 3600) {
			int hours = (int) (difference / 3600);
			returnString += hours + "h ";
			difference -= hours * 3600;
		}

		int seconds = (int) (difference / 60);
		returnString += seconds + "m";
		difference -= seconds * 60;

		return returnString;
	}

	public void logGlobalBan(String channel, String sender, String message) {
		String line = sender + "," + channel + ",\"" + message + "\"\n";

		// System.out.print(line);
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("globalbans.csv", true), "UTF-8"));
			out.write(line);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}
}