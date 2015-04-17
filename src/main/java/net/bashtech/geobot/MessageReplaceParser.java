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

public class MessageReplaceParser {

	public static String parseMessage(String channel, String sender,
			String message, String[] args) {
		Channel ci = BotManager.getInstance().getChannel(channel);
		
		
//		if (message.contains("(_RICHEST_USER_)"))
//			message = message.replace("(_RICHEST_USER_)",
//					somethingHere);
//		if (message.contains("(_HIGHEST_TIPPER_)"))
//			message = message.replace("(_HIGHEST_RIPPER_)", somethingHere);
		
		if (sender != null && message.contains("(_USER_)"))
			message = message.replace("(_USER_)", sender);
		if (message.contains("(_GAME_)"))
			message = message.replace("(_GAME_)",
					JSONUtil.krakenGame(channel.substring(1)));
		if (message.contains("(_STATUS_)"))
			message = message.replace("(_STATUS_)",
					JSONUtil.krakenStatus(channel.substring(1)));
		if (message.contains("(_VIEWERS_)"))
			message = message.replace("(_VIEWERS_)",
					"" + JSONUtil.krakenViewers(channel.substring(1)));
		if (message.contains("(_BOT_HELP_)"))
			message = message.replace("(_BOT_HELP_)",
					BotManager.getInstance().bothelpMessage);
		if (message.contains("(_CHANNEL_URL_)"))
			message = message.replace("(_CHANNEL_URL_)",
					"twitch.tv/" + channel.substring(1));
		if (message.contains("(_ONLINE_CHECK_)")) {
			if (!JSONUtil.krakenIsLive(channel.substring(1))) {
				message = "";
			} else {
				message = message.replace("(_ONLINE_CHECK_)", "");
			}
		}
		if (message.contains("(_NUMCHANNELS_)")) {
			message = message.replace("(_NUMCHANNELS_)",
					BotManager.getInstance().channelList.size() + "");
			System.out.println(message);
		}

		if (args != null) {
			int argCounter = 1;
			for (String argument : args) {
				if (message.contains("(_" + argCounter + "_)"))
					message = message.replace("(_" + argCounter + "_)",
							argument);
				argCounter++;
			}
		}
		if (message.contains("(_") && message.contains("_COUNT_)")) {
			int commandStart = message.indexOf("(_");
			int commandEnd = message.indexOf("_COUNT_)");
			String commandName = message
					.substring(commandStart + 2, commandEnd).toLowerCase();
			String value = ci.getCommand(commandName);
			String replaced = message.substring(commandStart, commandEnd + 8);
			if (value != null) {

				int count = ci.getCurrentCount(commandName);
				if (count > -1) {
					message = message.replace(replaced, count + "");
				} else {
					message = message.replace(replaced,
							"No count for that command...");
				}

			} else {
				message = message.replace(replaced,
						"No count for that command...");
			}
		}

		return message;
	}
}