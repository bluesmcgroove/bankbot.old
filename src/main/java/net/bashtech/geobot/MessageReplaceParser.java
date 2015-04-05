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

import java.net.URLEncoder;

public class MessageReplaceParser {

	public static String parseMessage(String channel, String sender,
			String message, String[] args) {
		Channel ci = BotManager.getInstance().getChannel(channel);

		if (sender != null && message.contains("(_USER_)"))
			message = message.replace("(_USER_)", sender);
		if (message.contains("(_GAME_)"))
			message = message.replace("(_GAME_)",
					JSONUtil.krakenGame(channel.substring(1)));
		if (message.contains("(_STATUS_)"))
			message = message.replace("(_STATUS_)",
					JSONUtil.krakenStatus(channel.substring(1)));
		// if (message.contains("(_JTV_STATUS_)"))
		// message = message.replace("(_JTV_STATUS_)",
		// JSONUtil.jtvStatus(channel.substring(1)));
		if (message.contains("(_VIEWERS_)"))
			message = message.replace("(_VIEWERS_)",
					"" + JSONUtil.krakenViewers(channel.substring(1)));
		// if (message.contains("(_JTV_VIEWERS_)"))
		// message = message.replace("(_JTV_VIEWERS_)",
		// "" + JSONUtil.jtvViewers(channel.substring(1)));
		// if (message.contains("(_CHATTERS_)"))
		// message = message.replace("(_CHATTERS_)", "" +
		// ReceiverBot.getInstance().getUsers(channel).length);
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
		if (message.contains("(_QUOTE_)")) {
			int randQuotes = (int) (Math.random() * ci.getQuoteSize());
			String quote = ci.getQuote(randQuotes);
			message = message.replace("(_QUOTE_)", quote);

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
		if (message.contains("(_VARS_")) {

			int begName = message.indexOf("(_VARS_") + 7;
			int endName = message.indexOf("_", begName);
			String varName = message.substring(begName, endName);
			System.out.println("varName = " + varName);
			int endMethod = message.indexOf("_", endName + 1);
			String method = message.substring(endName + 1, endMethod);
			System.out.println("method = " + method);

			if (method.equals("INCREMENT")) {
				int endInc = message.indexOf("_)", endMethod);
				System.out.println("index endInc =" + endInc);
				String inc = message.substring(endMethod + 1, endInc);
				System.out.println("inc = " + inc);
				int incValue = Integer.valueOf(inc);

				String response = JSONUtil.incVar(channel.substring(1),
						varName, incValue);
				if (response != null) {
					message = message.replace("(_VARS_" + varName
							+ "_INCREMENT_" + inc + "_)", response);
				} else {
					message = message.replace("(_VARS_" + varName + "_INCREMENT_" + inc
							+ "_)", "(error)");
				}
			}

			else if (method.equals("DECREMENT")) {
				int endDec = message.indexOf("_)", endMethod);
				String dec = message.substring(endMethod + 1, endDec);
				System.out.println("dec = " + dec);
				int decValue = Integer.valueOf(dec);
				String response = JSONUtil.decVar(channel.substring(1),
						varName, decValue);
				if (response != null) {
					message = message.replace("(_VARS_" + varName
							+ "_DECREMENT_" + dec + "_)", response);
				} else {
					message = message.replace("(_VARS_" + varName + "_DECREMENT_" + dec
							+ "_)", "(error)");
				}
			}

			else if (method.equals("GET")) {
				int endChannel = message.indexOf("_)", endMethod);

				String otherChannel = message.substring(endMethod + 1,
						endChannel);
				System.out.println(otherChannel);
				String response = JSONUtil.getVar(otherChannel, varName);
				if (response != null) {
					message = message.replace("(_VARS_" + varName + "_GET_"
							+ otherChannel + "_)", response);
				} else {
					message = message.replace("(_VARS_" + varName + "_GET_"
							+ otherChannel + "_)", "(error)");
				}

			}

		}

		return message;
	}
}
