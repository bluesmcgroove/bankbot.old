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
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Channel {
	public JSONObject config;
	public JSONObject balconfig;

	private String channel;
	private String twitchname;

	boolean staticChannel;
	private HashMap<String, String> commands = new HashMap<String, String>();
	private HashMap<String, Integer> commandsRestrictions = new HashMap<String, Integer>();
	private HashMap<String, Integer> commandCounts = new HashMap<String, Integer>();
	private HashMap<String, String> commandAdders = new HashMap<String, String>();
	HashMap<String, RepeatCommand> commandsRepeat = new HashMap<String, RepeatCommand>();
	HashMap<String, ScheduledCommand> commandsSchedule = new HashMap<String, ScheduledCommand>();
	List<Pattern> autoReplyTrigger = new ArrayList<Pattern>();
	List<String> autoReplyResponse = new ArrayList<String>();
	private Set<String> regulars = new HashSet<String>();
	// private Set<String> subscribers = new HashSet<String>();
	private Set<String> moderators = new HashSet<String>();
	Set<String> tagModerators = new HashSet<String>();
	private Set<String> owners = new HashSet<String>();
	private Set<String> permittedUsers = new HashSet<String>();
	private ArrayList<String> permittedDomains = new ArrayList<String>();
	private boolean signKicks;
	private boolean announceJoinParts;
	private int mode; // 0: Admin/owner only; 1: Mod Only; 2: Everyone; -1
						// Special mode to admins to use for channel moderation

	public boolean logChat;
	public long messageCount;
	private boolean filterColors;
	private boolean filterMe;
	public static long defaultBalance = 1000;
	private Set<String> offensiveWords = new HashSet<String>();
	private List<Pattern> offensiveWordsRegex = new LinkedList<Pattern>();
	private int timeoutDuration;
	Map<String, Long> commandCooldown;
	
	String prefix;
	String currency;
	String emoteSet;
	boolean subscriberRegulars;
	private boolean wpOn;
	private long sinceWp = System.currentTimeMillis();
	private int wpCount = 0;
	private String bullet = "#!";

	private JSONObject defaults = new JSONObject();
	//private JSONObject balDefaults = new JSONObject();

	private int cooldown = 0;

	private int maxViewers = 0;
	private boolean streamUp = false;
	private int streamMax = 0;
	private int streamNumber = 0;
	private int runningMaxViewers = 0;

	private int punishCount = 0;
	private int updateDelay = 120;

	private long sincePunish = System.currentTimeMillis();
	private String maxviewerDate = new java.util.Date().toString();

	public boolean subsRegsMinusLinks;

	public boolean active;
	private boolean streamAlive = false;
	private boolean urbanEnabled = false;
	private boolean currencyEnabled = false;
	private ArrayList<String> ignoredUsers = new ArrayList<String>();
	private HashMap<String, Long> userBalances = new HashMap<String, Long>();
	
	public Channel(String name) {
		channel = name;
		twitchname = channel.substring(1);
		String balname = twitchname + "balances";
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(channel + ".json"));
			config = (JSONObject) obj;

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Generating new config for " + channel);
			config = new JSONObject();
		}

		
		try {
			Object balobj = parser.parse(new FileReader(twitchname + "balances.json"));
			//userBalances = (JSONObject) balobj;
			balconfig = (JSONObject) balobj;
			System.out.println(balobj.toString());


		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Generating new balance config for " + channel);
			balconfig = new JSONObject();
		}
		 
		
		
		loadProperties(name);
		loadBalances(balname);
		//System.out.println(balconfig.toJSONString());

		if ((!checkPermittedDomain("coebot.tv"))) {
			this.addPermittedDomain("coebot.tv");
		}
		
		commandCooldown = new HashMap<String, Long>();

	}
	


	public Channel(String name, int mode) {
		this(name);
		setMode(mode);
	}

	public String getChannel() {
		return channel;
	}

	public String getTwitchName() {
		return twitchname;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix.charAt(0) + "";

		config.put("commandPrefix", this.prefix);
		saveConfig(true);
	}
	public String getCurrencyName() {
		return currency;
	}
	public void setCurrencyName(String currencyName) {
		this.currency = currencyName;

		config.put("currencyName", this.currency);
		saveConfig(true);
	}

	public boolean getWp() {
		return wpOn;
	}

	public void setStreamCount(int newCount) {
		streamNumber = newCount;
		config.put("streamCount", streamNumber);
		saveConfig(true);
	}

	public void setWp(boolean state) {
		wpOn = state;
		config.put("wpTimer", wpOn);
		saveConfig(true);
	}

	public long timeSinceSaid() {
		long now = System.currentTimeMillis();
		long differenceInSeconds = (now - sinceWp) / 1000L;
		sinceWp = now;
		config.put("sinceWp", sinceWp);
		saveConfig(true);
		return (differenceInSeconds);
	}

	public long timeSinceNoUpdate() {
		long now = System.currentTimeMillis();
		long differenceInSeconds = (now - sinceWp) / 1000L;

		return (differenceInSeconds);
	}

	public long timeSincePunished() {
		long now = System.currentTimeMillis();
		long differenceInSeconds = (now - sincePunish) / 1000L;

		return (differenceInSeconds);
	}

	public void setBullet(String newBullet) {
		bullet = newBullet;
		config.put("bullet", newBullet);
		saveConfig(true);
	}

	public void setCurrency(String newCurrency){
		currency = newCurrency;
		config.put("currency", newCurrency);
		saveConfig(true);
	}
	
	public String getChannelBullet() {
		return bullet;
	}
	
	public String getCurrency() {
		return currency;
	}

	public void increaseWpCount() {
		wpCount++;
		config.put("wpCount", wpCount);
		saveConfig(false);
	}

	public int getWpCount() {
		return wpCount;
	}

	public String getEmoteSet() {
		return emoteSet;
	}

	public void setEmoteSet(String emoteSet) {
		this.emoteSet = emoteSet;

		config.put("emoteSet", emoteSet);
		saveConfig(true);
	}

	public boolean getSubsRegsMinusLinks() {
		return subsRegsMinusLinks;
	}

	public void setSubsRegsMinusLinks(boolean on) {

		// subscribers.clear();

		subsRegsMinusLinks = on;
		config.put("subsRegsMinusLinks", subsRegsMinusLinks);
		saveConfig(true);

	}

	public boolean getSubscriberRegulars() {
		return subscriberRegulars;
	}

	public void setSubscriberRegulars(boolean subscriberRegulars) {

		// subscribers.clear();

		this.subscriberRegulars = subscriberRegulars;
		config.put("subscriberRegulars", subscriberRegulars);
		saveConfig(true);
	}

	// ################################################################
	public String getCommand(String key) {
		key = key.toLowerCase();

		if (commands.containsKey(key)) {
			return commands.get(key);
		} else {
			return null;
		}
	}

	public void setCommand(String key, String command, String adder) {
		key = key.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
		System.out.println("Key: " + key);
		command = command.replaceAll(",,", "");

		if (key.length() < 1)
			return;

		if (commands.containsKey(key)) {

			commands.remove(key);
			commandAdders.remove(key);
			commands.put(key, command);
			commandAdders.put(key, adder);

		} else {
			commands.put(key, command);
			commandAdders.put(key, adder);
			commandCounts.put(key, 0);
		}

		saveCommands(true);

	}
	
	public void editCommand(String key, String command, String adder) {
		key = key.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
		System.out.println("Key: " + key);
		command = command.replaceAll(",,", "");

		if (key.length() < 1)
			return;

		if (commands.containsKey(key)) {

			commands.remove(key);
			commandAdders.remove(key);
			commands.put(key, command);
			commandAdders.put(key, adder);
		}
		saveCommands(true);
	}
	public void renameCommand(String key, String key2, String adder) {
		key = key.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
		System.out.println("Key: " + key);
		key2 = key2.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
		System.out.println("Key2: " + key2);
		String command = this.getCommand(key);

		if (key.length() < 1)
			return;

		if (commands.containsKey(key)) {

			commands.remove(key);
			commandAdders.remove(key);
			commands.put(key2, command);
			commandAdders.put(key2, adder);
		}
		saveCommands(true);
	}
	public boolean removeCommand(String key) {
		if (commands.containsKey(key)) {
			commands.remove(key);
			commandsRestrictions.remove(key);
			commandCounts.remove(key);
			commandAdders.remove(key);

			saveCommands(true);
			return true;
		}else
			return false;

	}
	
	
	

	public void saveCommands(Boolean shouldSendUpdate) {
		JSONArray commandsArr = new JSONArray();

		Iterator itr = commands.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry pairs = (Map.Entry) itr.next();
			JSONObject commandObj = new JSONObject();
			commandObj.put("key", pairs.getKey());
			commandObj.put("value", pairs.getValue());
			if (commandsRestrictions.containsKey(pairs.getKey())) {
				commandObj.put("restriction",
						commandsRestrictions.get(pairs.getKey()));
			} else {
				commandObj.put("restriction", 1);
			}
			if (commandAdders.containsKey(pairs.getKey())) {
				commandObj.put("editor", commandAdders.get(pairs.getKey()));
			} else
				commandObj.put("editor", null);
			commandObj.put("count", commandCounts.get(pairs.getKey()));
			commandsArr.add(commandObj);

		}

		config.put("commands", commandsArr);
		saveConfig(shouldSendUpdate);
	}

	public void increaseCommandCount(String commandName) {
		commandName = commandName.toLowerCase();
		if (commandCounts.containsKey(commandName)) {
			int currentCount = commandCounts.get(commandName);
			currentCount++;
			commandCounts.put(commandName, currentCount);
		}
		saveCommands(false);

	}

	public int getCurrentCount(String commandName) {
		commandName = commandName.toLowerCase();
		if (commandCounts.containsKey(commandName)) {
			int currentCount = commandCounts.get(commandName);
			return currentCount;
		} else
			return -1;
	}

	public boolean setCommandsRestriction(String command, int level) {
		command = command.toLowerCase();

		if (!commands.containsKey(command))
			return false;

		commandsRestrictions.put(command, level);

		saveCommands(true);

		return true;
	}

	public boolean checkCommandRestriction(String command, int level) {
		System.out.println("Checking command: " + command + " User level: "
				+ level);
		if (!commandsRestrictions.containsKey(command.toLowerCase()))
			return true;

		if (level >= commandsRestrictions.get(command.toLowerCase()))
			return true;

		return false;
	}

	public void setRepeatCommand(String key, int delay, int diff) {
		key = key.toLowerCase();
		if (commandsRepeat.containsKey(key)) {
			commandsRepeat.get(key).timer.cancel();
			commandsRepeat.remove(key);
			RepeatCommand rc = new RepeatCommand(channel, key, delay, diff,
					true);
			commandsRepeat.put(key, rc);
		} else {
			RepeatCommand rc = new RepeatCommand(channel, key, delay, diff,
					true);
			commandsRepeat.put(key, rc);
		}

		saveRepeatCommands();
	}

	public void removeRepeatCommand(String key) {
		key = key.toLowerCase();
		if (commandsRepeat.containsKey(key)) {
			commandsRepeat.get(key).timer.cancel();
			commandsRepeat.remove(key);

			saveRepeatCommands();
		}
	}

	public void setRepeatCommandStatus(String key, boolean status) {
		if (commandsRepeat.containsKey(key)) {
			commandsRepeat.get(key).setStatus(status);
			saveRepeatCommands();
		}
	}

	private void saveRepeatCommands() {
		JSONArray repeatedCommands = new JSONArray();
		Iterator itr = commandsRepeat.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry pairs = (Map.Entry) itr.next();
			JSONObject repeatObj = new JSONObject();
			repeatObj.put("name", pairs.getKey());
			repeatObj.put("delay", ((RepeatCommand) pairs.getValue()).delay);
			repeatObj.put("messageDifference",
					((RepeatCommand) pairs.getValue()).messageDifference);
			repeatObj.put("active", ((RepeatCommand) pairs.getValue()).active);
			repeatedCommands.add(repeatObj);

		}

		config.put("repeatedCommands", repeatedCommands);
		saveConfig(true);
	}

	
	// Save balances to JSON

	public Long getBalance(String key) {
		key = key.toLowerCase();

		if (userBalances.containsKey(key)) {
			return userBalances.get(key);
		} else {
			return null;
		}
	}
	
	/*
	 public String getCommand(String key) {
		key = key.toLowerCase();

		if (commands.containsKey(key)) {
			return commands.get(key);
		} else {
			return null;
		}
	}
	*/
	
	public void setBalance(String key, Long balance) {
		
		
		/*
		key = key.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
		System.out.println("Key: " + key);
		command = command.replaceAll(",,", "");

		if (key.length() < 1)
			return;

		if (commands.containsKey(key)) {

			commands.remove(key);
			commandAdders.remove(key);
			commands.put(key, command);
			commandAdders.put(key, adder);

		} else {
			commands.put(key, command);
			commandAdders.put(key, adder);
			commandCounts.put(key, 0);
		}
		*/
		
		key = key.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
		balance = balance.longValue();
		System.out.println("Setting " + key + "balance to " + balance + ".");

		if (key.length() < 1)
			return;

		if (userBalances.containsKey(key)) {
			
			userBalances.put(key, balance);

		} else {
			userBalances.put(key, balance);
		}
		
		saveBalance(true);
		
	}
	
	public boolean removeBalance(String key, Long balance){
		if(userBalances.containsKey(key)){
			userBalances.replace(key, getBalance(key), defaultBalance);
			
			saveBalance(true);
			return true;
		}
		return false;
	}


	// Save balance reference?
	
	void saveBalance(boolean shouldUpdate) {
		JSONArray balanceArr = new JSONArray();
		Iterator itr = userBalances.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry pairs = (Map.Entry) itr.next();
			JSONObject balanceObj = new JSONObject();
			balanceObj.put("key", pairs.getKey());
			balanceObj.put("balance", pairs.getValue());
			
			balanceArr.add(balanceObj);
			balconfig.put("userBalances", balanceArr);
			//saveConfig(shouldUpdate);
			saveCurrency(shouldUpdate);
			System.out.println("Saving from saveBalance() " + userBalances.toString());
		}
		
		
	}
	
	/*
		JSONArray commandsArr = new JSONArray();

		Iterator itr = commands.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry pairs = (Map.Entry) itr.next();
			JSONObject commandObj = new JSONObject();
			commandObj.put("key", pairs.getKey());
			commandObj.put("value", pairs.getValue());
			if (commandsRestrictions.containsKey(pairs.getKey())) {
				commandObj.put("restriction",
						commandsRestrictions.get(pairs.getKey()));
			} else {
				commandObj.put("restriction", 1);
			}
			if (commandAdders.containsKey(pairs.getKey())) {
				commandObj.put("editor", commandAdders.get(pairs.getKey()));
			} else
				commandObj.put("editor", null);
			commandObj.put("count", commandCounts.get(pairs.getKey()));
			commandsArr.add(commandObj);

		}

		config.put("commands", commandsArr);
		saveConfig(shouldSendUpdate);
	}
	*/
	
	//end reference
	
	//increase balance
	
	public void increaseBalance(String key, Long incBal) {
		key = key.toLowerCase();
		if (userBalances.containsKey(key)) {
			Long currentBalance = userBalances.get(key);
			long summedBalance = Math.addExact(currentBalance, incBal);
			userBalances.put(key, summedBalance);
			saveBalance(true);
		}
		saveBalance(false);

	}
	
	//decrease balance
	public void decreaseBalance(String key, Long decBal) {
		key = key.toLowerCase();
		if (userBalances.containsKey(key)) {
			long currentBalance = userBalances.get(key);
			long subtrBalance = Math.subtractExact(currentBalance, decBal);
			userBalances.put(key, subtrBalance);
			saveBalance(true);
		}
		saveBalance(false);

	}
	
	public void setScheduledCommand(String key, String pattern, int diff) {
		if (commandsSchedule.containsKey(key)) {
			commandsSchedule.get(key).s.stop();
			commandsSchedule.remove(key);
			ScheduledCommand rc = new ScheduledCommand(channel, key, pattern,
					diff, true);
			commandsSchedule.put(key, rc);
		} else {
			ScheduledCommand rc = new ScheduledCommand(channel, key, pattern,
					diff, true);
			commandsSchedule.put(key, rc);
		}

		saveScheduledCommands();

	}

	public void removeScheduledCommand(String key) {
		if (commandsSchedule.containsKey(key)) {
			commandsSchedule.get(key).s.stop();
			commandsSchedule.remove(key);

			saveScheduledCommands();
		}
	}

	public void setScheduledCommandStatus(String key, boolean status) {
		if (commandsSchedule.containsKey(key)) {
			commandsSchedule.get(key).setStatus(status);
			saveScheduledCommands();
		}
	}

	private void saveScheduledCommands() {

		JSONArray scheduledCommands = new JSONArray();

		Iterator itr = commandsSchedule.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry pairs = (Map.Entry) itr.next();
			JSONObject scheduleObj = new JSONObject();
			scheduleObj.put("name", pairs.getKey());
			scheduleObj.put("pattern",
					((ScheduledCommand) pairs.getValue()).pattern);
			scheduleObj.put("messageDifference",
					((ScheduledCommand) pairs.getValue()).messageDifference);
			scheduleObj.put("active",
					((ScheduledCommand) pairs.getValue()).active);
			scheduledCommands.add(scheduleObj);

		}

		config.put("scheduledCommands", scheduledCommands);
		saveConfig(true);
	}
	
	public ArrayList<String> getCommandList() {

		ArrayList<String> sorted = new ArrayList<String>(commands.keySet());

		java.util.Collections.sort(sorted);
		return sorted;

	}

	public void addAutoReply(String trigger, String response) {
		trigger = trigger.replaceAll(",,", "");
		response.replaceAll(",,", "");

		if (!trigger.startsWith("REGEX:")) {
			String[] parts = trigger.replaceFirst("^\\*", "")
					.replaceFirst("\\*$", "").split("\\*");

			// Only apply leading & trailing any if an one was requested
			boolean trailingAny = trigger.endsWith("*");
			if (trigger.startsWith("*"))
				trigger = ".*";
			else
				trigger = "";

			for (int i = 0; i < parts.length; i++) {
				if (parts[i].length() < 1)
					continue;

				trigger += Pattern.quote(parts[i]);
				if (i != parts.length - 1)
					trigger += ".*";
			}

			if (trailingAny)
				trigger += ".*";

		} else {
			trigger = trigger.replaceAll("REGEX:", "");
		}

		System.out.println("Final: " + trigger);
		autoReplyTrigger
				.add(Pattern.compile(trigger, Pattern.CASE_INSENSITIVE));
		autoReplyResponse.add(response);

		saveAutoReply();
	}

	public boolean removeAutoReply(int pos) {
		pos = pos - 1;

		if (pos > autoReplyTrigger.size() - 1)
			return false;

		autoReplyTrigger.remove(pos);
		autoReplyResponse.remove(pos);

		saveAutoReply();

		return true;
	}

	public boolean editAutoReplyResponse(int pos, String newResponse) {
		pos = pos - 1;
		if (pos > autoReplyTrigger.size() - 1)
			return false;

		autoReplyResponse.remove(pos);
		autoReplyResponse.add(pos, newResponse);

		saveAutoReply();

		return true;
	}

	private void saveAutoReply() {
		JSONArray triggerString = new JSONArray();
		JSONArray responseString = new JSONArray();
		JSONArray autoReplies = new JSONArray();

		for (int i = 0; i < autoReplyTrigger.size(); i++) {
			JSONObject autoreplyObj = new JSONObject();
			autoreplyObj.put("trigger", autoReplyTrigger.get(i).toString());
			autoreplyObj.put("response", autoReplyResponse.get(i).toString());
			autoReplies.add(autoreplyObj);
		}

		config.put("autoReplies", autoReplies);
		saveConfig(true);
	}
	
	// ###################################################

	public boolean isRegular(String name) {
		synchronized (regulars) {
			for (String s : regulars) {
				if (s.equalsIgnoreCase(name)) {
					return true;
				}
			}
		}
		return false;
	}

	public void addRegular(String name) {
		synchronized (regulars) {
			regulars.add(name.toLowerCase());

		}

		JSONArray regularsArray = new JSONArray();

		synchronized (regulars) {
			for (String s : regulars) {
				regularsArray.add(s);
			}
		}

		config.put("regulars", regularsArray);
		saveConfig(true);
	}

	public void removeRegular(String name) {
		synchronized (regulars) {
			if (regulars.contains(name.toLowerCase()))
				regulars.remove(name.toLowerCase());
		}
		JSONArray regularsArray = new JSONArray();

		synchronized (regulars) {
			for (String s : regulars) {
				regularsArray.add(s);
			}
		}

		config.put("regulars", regularsArray);
		saveConfig(true);
	}

	public Set<String> getRegulars() {
		return regulars;
	}

	public void permitUser(String name) {
		synchronized (permittedUsers) {
			if (permittedUsers.contains(name.toLowerCase()))
				return;
		}

		synchronized (permittedUsers) {
			permittedUsers.add(name.toLowerCase());
		}
	}

	public boolean linkPermissionCheck(String name) {

		if (this.isRegular(name)) {
			return true;
		}

		synchronized (permittedUsers) {
			if (permittedUsers.contains(name.toLowerCase())) {
				permittedUsers.remove(name.toLowerCase());
				return true;
			}
		}

		return false;
	}
	
	// ##################################################

	public boolean isModerator(String name) {
		synchronized (tagModerators) {
			if (tagModerators.contains(name))
				return true;
		}
		synchronized (moderators) {
			if (moderators.contains(name.toLowerCase()))
				return true;
		}

		return false;
	}

	public void addModerator(String name) {
		synchronized (moderators) {
			moderators.add(name.toLowerCase());
		}

		JSONArray moderatorsArray = new JSONArray();

		synchronized (moderators) {
			for (String s : moderators) {
				moderatorsArray.add(s);
			}
		}

		config.put("moderators", moderatorsArray);
		saveConfig(true);
	}

	public void removeModerator(String name) {
		synchronized (moderators) {
			if (moderators.contains(name.toLowerCase()))
				moderators.remove(name.toLowerCase());
		}

		JSONArray moderatorsArray = new JSONArray();

		synchronized (moderators) {
			for (String s : moderators) {
				moderatorsArray.add(s);
			}
		}

		config.put("moderators", moderatorsArray);
		saveConfig(true);
	}

	public Set<String> getModerators() {
		return moderators;
	}

	// ###################################################

	public boolean isOwner(String name) {
		synchronized (owners) {
			if (owners.contains(name.toLowerCase()))
				return true;
		}

		return false;
	}

	public void addOwner(String name) {
		synchronized (owners) {
			owners.add(name.toLowerCase());
		}

		JSONArray ownersString = new JSONArray();

		synchronized (owners) {
			for (String s : owners) {
				ownersString.add(s);
			}
		}

		config.put("owners", ownersString);
		saveConfig(true);
	}

	public void removeOwner(String name) {
		synchronized (owners) {
			if (owners.contains(name.toLowerCase()))
				owners.remove(name.toLowerCase());
		}

		JSONArray ownersString = new JSONArray();

		synchronized (owners) {
			for (String s : owners) {
				ownersString.add(s);
			}
		}

		config.put("owners", ownersString);
		saveConfig(true);
	}

	public Set<String> getOwners() {
		return owners;
	}

	// ###################################################

	public void addPermittedDomain(String name) {
		synchronized (permittedDomains) {
			permittedDomains.add(name.toLowerCase());
		}

		JSONArray permittedDomainsString = new JSONArray();

		synchronized (permittedDomains) {
			for (String s : permittedDomains) {
				permittedDomainsString.add(s);
			}
		}

		config.put("permittedDomains", permittedDomainsString);
		saveConfig(true);
	}

	public void removePermittedDomain(String name) {
		synchronized (permittedDomains) {
			for (int i = 0; i < permittedDomains.size(); i++) {
				if (permittedDomains.get(i).equalsIgnoreCase(name)) {
					permittedDomains.remove(i);
				}
			}
		}

		JSONArray permittedDomainsString = new JSONArray();

		synchronized (permittedDomains) {
			for (String s : permittedDomains) {
				permittedDomainsString.add(s);
			}
		}

		config.put("permittedDomains", permittedDomainsString);
		saveConfig(true);

	}

	public boolean isDomainPermitted(String domain) {
		for (String d : permittedDomains) {
			if (d.equalsIgnoreCase(domain)) {
				return true;
			}
		}

		return false;
	}

	public ArrayList<String> getpermittedDomains() {
		return permittedDomains;
	}

	// #################################################

	public void addOffensive(String word) {
		synchronized (offensiveWords) {
			offensiveWords.add(word);
		}

		synchronized (offensiveWordsRegex) {
			if (word.startsWith("REGEX:")) {
				String line = word.substring(6);
				
				Pattern tempP = Pattern.compile(line);
				offensiveWordsRegex.add(tempP);
			} else {
				String line = ".*" + Pattern.quote(word) + ".*";
				
				Pattern tempP = Pattern.compile(line, Pattern.CASE_INSENSITIVE);
				offensiveWordsRegex.add(tempP);
			}

		}

		JSONArray offensiveWordsArray = new JSONArray();

		synchronized (offensiveWords) {
			for (String s : offensiveWords) {
				offensiveWordsArray.add(s);
			}
		}

		config.put("offensiveWords", offensiveWordsArray);
		saveConfig(true);
	}

	public void removeOffensive(String word) {
		synchronized (offensiveWords) {
			if (offensiveWords.contains(word))
				offensiveWords.remove(word);
		}

		JSONArray offensiveWordsArray = new JSONArray();

		synchronized (offensiveWords) {
			for (String s : offensiveWords) {
				offensiveWordsArray.add(s);
			}
		}

		config.put("offensiveWords", offensiveWordsArray);
		saveConfig(true);

		synchronized (offensiveWordsRegex) {
			offensiveWordsRegex.clear();

			for (String w : offensiveWords) {
				if (w.startsWith("REGEX:")) {
					String line = w.substring(6);
					System.out.println("ReAdding: " + line);
					Pattern tempP = Pattern.compile(line);
					offensiveWordsRegex.add(tempP);
				} else {
					String line = ".*" + Pattern.quote(w) + ".*";
					System.out.println("ReAdding: " + line);
					Pattern tempP = Pattern.compile(line);
					offensiveWordsRegex.add(tempP);
				}
			}
		}
	}

	public void clearBannedPhrases() {
		offensiveWords.clear();
		offensiveWordsRegex.clear();
		config.put("offensiveWords", new JSONArray());
		saveConfig(true);
	}

	public boolean isBannedPhrase(String phrase) {
		return offensiveWords.contains(phrase);
	}

	public boolean isOffensive(String word) {
		for (Pattern reg : offensiveWordsRegex) {
			Matcher match = reg.matcher(word.toLowerCase());
			if (match.find()) {
				System.out.println("Matched: " + reg.toString());
				return true;
			}
		}

		int severity = ((Long) config.get("banPhraseSeverity")).intValue();
		if (BotManager.getInstance().banPhraseLists.containsKey(severity)) {
			for (Pattern reg : BotManager.getInstance().banPhraseLists
					.get(severity)) {
				Matcher match = reg.matcher(word.toLowerCase());
				if (match.find()) {
					System.out.println("Matched: " + reg.toString());
					return true;
				}
			}
		}

		return false;
	}

	public Set<String> getOffensive() {
		return offensiveWords;
	}
	
	// ##################################################

	public boolean checkPermittedDomain(String message) {
		// Allow base domain w/o a path
		if (message.matches(".*(twitch\\.tv|twitchtv\\.com|justin\\.tv)")) {
			System.out
					.println("INFO: Permitted domain match on jtv/ttv base domain.");
			return true;
		}

		for (String d : permittedDomains) {
			// d = d.replaceAll("\\.", "\\\\.");

			String test = ".*(\\.|^|//)" + Pattern.quote(d) + "(/|$).*";
			if (message.matches(test)) {
				// System.out.println("DEBUG: Matched permitted domain: " +
				// test);
				return true;
			}
		}
		return false;
	}
	public void checkViewerStats(String name) {
		long viewers = JSONUtil.krakenViewers(name);
		if (viewers > maxViewers) {
			maxViewers = (int) viewers;
			config.put("maxViewers", maxViewers);
			maxviewerDate = new java.util.Date().toString();
			config.put("maxviewerDate", maxviewerDate);

		}
		saveConfig(false);

	}

	public int getViewerStats() {
		return maxViewers;
	}

	public String getViewerStatsTime() {
		return maxviewerDate;
	}

	public void resetMaxViewers(int newMax) {
		maxViewers = newMax;
		config.put("maxViewers", maxViewers);
		saveConfig(false);
	}

	public void increasePunCount() {
		punishCount++;
		sincePunish = System.currentTimeMillis();
		config.put("sincePunish", sincePunish);
		config.put("punishCount", punishCount);
		saveConfig(false);
	}

	public int getPunCount() {
		return punishCount;
	}

	public void alive(String name) {

		long curViewers = JSONUtil.krakenViewers(name);
		if (curViewers > streamMax) {
			streamMax = (int) curViewers;
			config.put("maxViewersStream", curViewers);
		}
	}

	public void dead(String name) {

		streamNumber++;
		config.put("streamCount", streamNumber);
		runningMaxViewers += streamMax;
		config.put("runningMaxViewers", runningMaxViewers);
		streamMax = 0;
		config.put("maxViewersStream", 0);
		saveConfig(false);
	}

	public double getAverage() {
		return (double) runningMaxViewers / (streamNumber);
	}

	public void registerCommandUsage(String command) {
		synchronized (commandCooldown) {
			System.out.println("DEBUG: Adding command " + command
					+ " to cooldown list");
			commandCooldown.put(command.toLowerCase(), getTime());
		}
	}

	public boolean onCooldown(String command) {
		command = command.toLowerCase();
		if (commandCooldown.containsKey(command)) {
			long lastUse = commandCooldown.get(command);
			if ((getTime() - lastUse) > 30) {
				// Over
				System.out.println("DEBUG: Cooldown for " + command
						+ " is over");

				return false;
			} else {
				// Not Over
				System.out.println("DEBUG: Cooldown for " + command
						+ " is NOT over");
				return true;
			}
		} else {

			return false;
		}
	}

	public void setUpdateDelay(int newDelay) {
		updateDelay = newDelay;
		config.put("updateDelay", newDelay);
		saveConfig(false);
	}

	public void reload() {
		BotManager.getInstance().removeChannel(channel);
		BotManager.getInstance().addChannel(channel, mode);
	}

	private void setDefaults() {

		// defaults.put("channel", channel);
		defaults.put("ignoredUsers", new JSONArray());
		defaults.put("currencyEnabled", true);
		defaults.put("subsRegsMinusLinks", new Boolean(false));
		defaults.put("filterCaps", new Boolean(false));
		defaults.put("filterOffensive", new Boolean(true));
		defaults.put("filterCapsPercent", 50);
		defaults.put("filterCapsMinCharacters", 0);
		defaults.put("filterCapsMinCapitals", 6);
		defaults.put("filterLinks", new Boolean(false));
		defaults.put("filterEmotes", new Boolean(false));
		defaults.put("filterSymbols", new Boolean(false));
		defaults.put("filterEmotesMax", 4);

		defaults.put("punishCount", 0);
		defaults.put("sincePunish", sincePunish);
		defaults.put("sinceWp", System.currentTimeMillis());
		defaults.put("maxviewerDate", "");

		defaults.put("commands", new JSONArray());

		defaults.put("repeatedCommands", new JSONArray());
		defaults.put("scheduledCommands", new JSONArray());
		defaults.put("autoReplies", new JSONArray());
		defaults.put("regulars", new JSONArray());
		defaults.put("moderators", new JSONArray());
		defaults.put("owners", new JSONArray());
		defaults.put("permittedDomains", new JSONArray());
		defaults.put("signKicks", new Boolean(false));
		defaults.put("topicTime", 0);
		defaults.put("mode", 2);
		defaults.put("announceJoinParts", new Boolean(false));
		defaults.put("logChat", new Boolean(false));
		defaults.put("offensiveWords", new JSONArray());
		defaults.put("staticChannel", new Boolean(false));
		defaults.put("commandPrefix", "!");

		defaults.put("emoteSet", "");
		defaults.put("subscriberRegulars", new Boolean(false));
		defaults.put("banPhraseSeverity", 99);

		defaults.put("wpTimer", new Boolean(false));
		defaults.put("wpCount", 0);
		defaults.put("bullet", BotManager.getInstance().defaultBullet);
		defaults.put("currencyName", BotManager.getInstance().defaultCurrency);
		defaults.put("cooldown", 5);

		defaults.put("maxViewers", 0);
		defaults.put("runningMaxViewers", 0);
		defaults.put("streamCount", 0);
		defaults.put("streamAlive", new Boolean(false));
		defaults.put("maxViewersStream", 0);

		defaults.put("updateDelay", 120);
		
		// User Balance JSONArray
		defaults.put("userBalances", new JSONArray());

		Iterator it = defaults.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			String key = String.valueOf(pairs.getKey());
			Object value = pairs.getValue();
			if (value instanceof Integer) {
				value = Integer.parseInt(String.valueOf(value)) * 1L;
			}
			if (!config.containsKey(key))
				config.put(key, value);
		}
		saveConfig(false);
	}
	
	/*
	private void setBalanceDefaults() {
		balDefaults.put("userBalances", new JSONArray());

		Iterator it = defaults.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			String key = String.valueOf(pairs.getKey());
			Object value = pairs.getValue();
			if (value instanceof Integer) {
				value = Integer.parseInt(String.valueOf(value)) * 1L;
			}
			if (!balconfig.containsKey(key))
				balconfig.put(key, value);
		}
		
		
	}
	*/

	private void loadProperties(String name) {

		setDefaults();

		urbanEnabled = Boolean.valueOf((Boolean) config.get("urbanEnabled"));
		currencyEnabled = Boolean.valueOf((Boolean) config.get("currencyEnabled"));
		// channel = config.getString("channel");

		subsRegsMinusLinks = Boolean.valueOf((Boolean) config
				.get("subsRegsMinusLinks"));
		updateDelay = ((Long) config.get("updateDelay")).intValue();
		punishCount = ((Long) config.get("punishCount")).intValue();
		streamAlive = (Boolean) config.get("streamAlive");
		sinceWp = ((Long) config.get("sinceWp"));
		maxviewerDate = (String) config.get("maxviewerDate");
		runningMaxViewers = ((Long) config.get("runningMaxViewers")).intValue();
		streamNumber = ((Long) config.get("streamCount")).intValue();
		streamMax = ((Long) config.get("maxViewersStream")).intValue();
		maxViewers = ((Long) config.get("maxViewers")).intValue();

		wpOn = Boolean.valueOf((Boolean) config.get("wpTimer"));
		wpCount = ((Long) config.get("wpCount")).intValue();
		bullet = (String) config.get("bullet");
		currency = (String) config.get("curency");
		cooldown = ((Long) config.get("cooldown")).intValue();
		sincePunish = (Long) config.get("sincePunish");
		// announceJoinParts =
		// Boolean.parseBoolean(config.getString("announceJoinParts"));
		announceJoinParts = false;
		signKicks = Boolean.valueOf((Boolean) config.get("signKicks"));
		logChat = Boolean.valueOf((Boolean) config.get("logChat"));
		mode = ((Long) config.get("mode")).intValue();
		filterColors = Boolean.valueOf((Boolean) config.get("filterColors"));
		filterMe = Boolean.valueOf((Boolean) config.get("filterMe"));
		staticChannel = Boolean.valueOf((Boolean) config.get("staticChannel"));
		timeoutDuration = ((Long) config.get("timeoutDuration")).intValue();
		prefix = (String) config.get("commandPrefix");
		currency = (String) config.get("currencyName") ;
		emoteSet = (String) config.get("emoteSet");
		subscriberRegulars = Boolean.valueOf((Boolean) config
				.get("subscriberRegulars"));

		// timeAliveStart = (Long)config.get("timeAliveStart");

		JSONArray jsonignoredUsers = (JSONArray) config.get("ignoredUsers");
		for (int i = 0; i < jsonignoredUsers.size(); i++) {
			ignoredUsers.add((String) jsonignoredUsers.get(i));
		}
		
		JSONArray commandsArray = (JSONArray) config.get("commands");

		for (int i = 0; i < commandsArray.size(); i++) {
			JSONObject commandObject = (JSONObject) commandsArray.get(i);
			commands.put((String) commandObject.get("key"),
					(String) commandObject.get("value"));
			if (commandObject.containsKey("restriction")) {
				commandsRestrictions.put((String) commandObject.get("key"),
						((Long) commandObject.get("restriction")).intValue());
			}
			if (commandObject.containsKey("count")
					&& commandObject.get("count") != null) {
				commandCounts.put((String) commandObject.get("key"),
						((Long) commandObject.get("count")).intValue());
			} else {
				commandCounts.put((String) commandObject.get("key"), 0);
			}
			if (commandObject.containsKey("editor")
					&& commandObject.get("editor") != null) {
				commandAdders.put((String) commandObject.get("key"),
						(String) commandObject.get("editor"));
			} else {
				commandAdders.put((String) commandObject.get("key"), null);
			}

		}
		
		System.out.println(commands.toString());
		
		saveCommands(false);

		JSONArray repeatedCommandsArray = (JSONArray) config
				.get("repeatedCommands");

		for (int i = 0; i < repeatedCommandsArray.size(); i++) {
			JSONObject repeatedCommandObj = (JSONObject) repeatedCommandsArray
					.get(i);
			RepeatCommand rc = new RepeatCommand(channel,
					((String) repeatedCommandObj.get("name")).replaceAll(
							"[^a-zA-Z0-9]", ""),
					((Long) repeatedCommandObj.get("delay")).intValue(),
					((Long) repeatedCommandObj.get("messageDifference"))
							.intValue(),
					Boolean.valueOf((Boolean) repeatedCommandObj.get("active")));
			commandsRepeat.put(((String) repeatedCommandObj.get("name"))
					.replaceAll("[^a-zA-Z0-9]", ""), rc);

		}

		JSONArray scheduledCommandsArray = (JSONArray) config
				.get("scheduledCommands");

		for (int i = 0; i < scheduledCommandsArray.size(); i++) {
			JSONObject scheduledCommandsObj = (JSONObject) scheduledCommandsArray
					.get(i);
			ScheduledCommand rc = new ScheduledCommand(channel,
					((String) scheduledCommandsObj.get("name")).replaceAll(
							"[^a-zA-Z0-9]", ""),
					(String) scheduledCommandsObj.get("pattern"),
					((Long) scheduledCommandsObj.get("messageDifference"))
							.intValue(),
					Boolean.valueOf((Boolean) scheduledCommandsObj
							.get("active")));
			commandsSchedule.put(((String) scheduledCommandsObj.get("name"))
					.replaceAll("[^a-zA-Z0-9]", ""), rc);

		}

		JSONArray autoReplyArray = (JSONArray) config.get("autoReplies");
		for (int i = 0; i < autoReplyArray.size(); i++) {
			JSONObject autoReplyObj = (JSONObject) autoReplyArray.get(i);
			autoReplyTrigger.add(Pattern.compile(
					(String) autoReplyObj.get("trigger"),
					Pattern.CASE_INSENSITIVE));
			autoReplyResponse.add((String) autoReplyObj.get("response"));

		}

		JSONArray regularsJSONArray = (JSONArray) config.get("regulars");
		synchronized (regulars) {
			for (int i = 0; i < regularsJSONArray.size(); i++) {
				String reg = ((String) regularsJSONArray.get(i)).toLowerCase();
				if (reg != "" && reg != null)
					regulars.add(reg);

			}
		}

		JSONArray modsArray = (JSONArray) config.get("moderators");
		synchronized (moderators) {
			for (int i = 0; i < modsArray.size(); i++) {

				moderators.add(((String) modsArray.get(i)).toLowerCase());

			}
		}

		JSONArray ownersArray = (JSONArray) config.get("owners");
		synchronized (owners) {
			for (int i = 0; i < ownersArray.size(); i++) {

				owners.add(((String) ownersArray.get(i)).toLowerCase());

			}
		}

		JSONArray domainsArray = (JSONArray) config.get("permittedDomains");

		synchronized (permittedDomains) {
			for (int i = 0; i < domainsArray.size(); i++) {

				permittedDomains.add(((String) domainsArray.get(i))
						.toLowerCase());

			}
		}

		JSONArray offensiveArray = (JSONArray) config.get("offensiveWords");

		synchronized (offensiveWords) {
			synchronized (offensiveWordsRegex) {
				for (int i = 0; i < offensiveArray.size(); i++) {

					String w = (String) offensiveArray.get(i);
					offensiveWords.add(w);
					if (w.startsWith("REGEX:")) {
						String line = w.substring(6);
						System.out.println("Adding: " + line);
						Pattern tempP = Pattern.compile(line);
						offensiveWordsRegex.add(tempP);
					} else {
						String line = "(?i).*" + Pattern.quote(w) + ".*";
						System.out.println("Adding: " + line);
						Pattern tempP = Pattern.compile(line);
						offensiveWordsRegex.add(tempP);
					}

				}
			}
			

		}
		
		/*
		System.out.println("Balances loaded from " + name);
		JSONArray balanceArray = (JSONArray) config.get("userBalances");
		
		System.out.println("Printing balconfig " + userBalances.toString());

		for (int i = 0; i < userBalances.size(); i++) {
			JSONObject balanceObject = (JSONObject) balanceArray.get(i);
			userBalances.put((String) balanceObject.get("key"),
					(Long) balanceObject.get("balance"));
					
					//saveCurrency(false);
					saveBalance(false);
		}
		*/
		
		//JSONArray balanceArray = (JSONArray) config.get("userBalances");

		//for (int i = 0; i < balanceArray.size(); i++) {
		//	JSONObject balanceObject = (JSONObject) balanceArray.get(i);
		//	userBalances.put((String) balanceObject.get("key"),
		//			(Long) balanceObject.get("balance"));

		//}
		
		System.out.println(userBalances.toString());
		
		saveConfig(true);

	}
	
	private void loadBalances(String name){
		
		JSONArray balanceArray = (JSONArray) balconfig.get("userBalances");

				for (int i = 0; i < balanceArray.size(); i++) {
					JSONObject balanceObject = (JSONObject) balanceArray.get(i);
					userBalances.put((String) balanceObject.get("key"),
							(Long) balanceObject.get("balance"));

					
					saveBalance(false);
					
		}
		saveCurrency(true);
		System.out.println("Printing userBalances " + userBalances.toString());
	}
	

	public void setMode(int mode) {
		this.mode = mode;
		config.put("mode", this.mode);
		
		saveConfig(true);
	}

	public int getMode() {
		return mode;
	}

	private long getTime() {
		return (System.currentTimeMillis() / 1000L);
	}
	public void setCooldown(int newCooldown) {
		cooldown = newCooldown;
		config.put("cooldown", newCooldown);
		saveConfig(true);
	}

	public long getCooldown() {

		return cooldown;
	}

	public static String getDurationBreakdown(long millis) {
		if (millis < 0) {
			throw new IllegalArgumentException(
					"Duration must be greater than zero!");
		}

		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

		StringBuilder sb = new StringBuilder(64);
		if (days > 0) {
			sb.append(days);
			sb.append(" days, ");
		}

		if (hours < 10)
			sb.append(0);
		sb.append(hours);

		sb.append(" hours, ");
		if (minutes < 10)
			sb.append(0);
		sb.append(minutes);
		sb.append(" minutes, and ");
		if (seconds < 10)
			sb.append(0);
		sb.append(seconds);
		sb.append(" seconds.");

		return (sb.toString());
	}

	public void saveConfig(Boolean shouldSendUpdate) {
		try {

			FileWriter file = new FileWriter(channel + ".json");

			StringWriter out = new StringWriter();
			JSONValue.writeJSONString(config, out);
			String jsonText = out.toString();
			file.write(jsonText);
			// file.write(config.toJSONString());
			file.flush();
			file.close();
			if (shouldSendUpdate) {
				BotManager.getInstance().postCoebotConfig(
						config.toJSONString(), twitchname);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Save currency balances?
	
	//Should be like saveConfig
	  public void saveCurrency(Boolean shouldUpdate) {
		try {

			FileWriter file = new FileWriter(twitchname + "balances.json");

			StringWriter out = new StringWriter();
			JSONValue.writeJSONString(balconfig, out);
			String jsonText = out.toString();
			file.write(jsonText);
			// file.write(config.toJSONString());
			file.flush();
			file.close();
			if (shouldUpdate) {
				BotManager.getInstance().postCoebotConfig(config.toJSONString(), jsonText);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("Saving" + balconfig.toJSONString());
	}
	  
	
	 

	public void setUrban(boolean enabled) {
		urbanEnabled = enabled;
		config.put("urbanEnabled", enabled);
		saveConfig(true);

	}

	public boolean getUrban() {
		return urbanEnabled;
	}

	public ArrayList<String> getIgnoredUsers() {
		return ignoredUsers;
	}

	public boolean addIgnoredUser(String user) {
		if (ignoredUsers.contains(user)) {
			return false;
		} else {
			ignoredUsers.add(user);
			config.put("ignoredUsers", ignoredUsers);
			saveConfig(true);
			return true;
		}

	}

	public boolean removeIgnoredUser(String user) {
		if (ignoredUsers.contains(user)) {
			ignoredUsers.remove(user);
			config.put("ignoredUsers", ignoredUsers);
			saveConfig(true);
			return true;
		} else
			return false;
	}
}