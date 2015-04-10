package net.bashtech.geobot;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import net.bashtech.geobot.*;


public class Balances {
	
	public static JSONObject balconfig;
	
	private String channel;
	private static String twitchname;
	private ArrayList<String> permittedDomains = new ArrayList<String>();
	Map<String, EnumMap<FilterType, Integer>> warningCount;
	Map<String, Long> warningTime;
	private int timeoutDuration;
	private boolean enableWarnings;
	Map<String, Long> commandCooldown;
	private static HashMap<String, Long> userBalances = new HashMap<String, Long>();
	public static long defaultBalance = 0L;
	
	public  Balances(String name) {
		channel = name;
		twitchname = channel.substring(1);
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(channel + "balances.json"));
			balconfig = (JSONObject) obj;

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Generating new config for " + channel);
			balconfig = new JSONObject();
		}

		loadProperties(name);
		/*
		if ((!checkPermittedDomain("coebot.tv"))) {
			this.addPermittedDomain("coebot.tv");
		}
		*/
		
		warningCount = new HashMap<String, EnumMap<FilterType, Integer>>();
		warningTime = new HashMap<String, Long>();
		commandCooldown = new HashMap<String, Long>();

	}
	
	private void loadProperties(String name) {

		
			// TODO Create JSONArray for user balances. Learn from Line 1766?
			
			JSONArray balanceArray = (JSONArray) balconfig.get("userBalances");

			for (int i = 0; i < userBalances.size(); i++) {
				JSONObject balanceObject = (JSONObject) balanceArray.get(i);
				userBalances.put((String) balanceObject.get("name"),
						(Long) balanceObject.get("balance"));
				//Not necessary for balance array?
/*
				if (balanceObject.containsKey("count")
						&& balanceObject.get("count") != null) {
					balanceCounts.put((String) balanceObject.get("key"),
							((Long) balanceObject.get("count")).intValue());
				} else {
					balanceCounts.put((String) balanceObject.get("name"), 0);
				}
				if (balanceObject.containsKey("editor")
						&& balanceObject.get("editor") != null) {
					commandAdders.put((String) balanceObject.get("name"),
							(String) balanceObject.get("editor"));
				} else {
					commandAdders.put((String) balanceObject.get("name"), null);
				}
*/
			}
			

		
		saveConfig(true);

	}
	
	public void saveConfig(Boolean shouldSendUpdate) {
		try {

			FileWriter file = new FileWriter(channel + ".json");

			StringWriter out = new StringWriter();
			JSONValue.writeJSONString(balconfig, out);
			String jsonText = out.toString();
			file.write(jsonText);
			// file.write(config.toJSONString());
			file.flush();
			file.close();
			if (shouldSendUpdate) {
				BotManager.getInstance();
				BotManager.postCoebotConfig(
						balconfig.toJSONString(), twitchname);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void setBalance(String key, Long balance) {
		/*
		JSONArray balanceArr = new JSONArray();
		Iterator itr = userBalances.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry pairs = (Map.Entry) itr.next();
			JSONObject balanceObj = new JSONObject();
			balanceObj.put("key", pairs.getKey());
			balanceObj.put("balance", pairs.getValue());

		config.put("balance", balanceArr);
		 */
		key = key.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
		System.out.println("User: " + key);
		balance = Long.valueOf(balance);

		if (key.length() < 1)
			return;

		if (userBalances.containsKey(key)) {

			userBalances.remove(key);
			userBalances.put(key, balance);

		} else {
			userBalances.put(key, balance);
		}
		
		saveBalance(true);
		}

	

	private void saveUserBalance(boolean shouldUpdate) {
		JSONArray balanceArr = new JSONArray();
		Iterator itr = userBalances.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry pairs = (Map.Entry) itr.next();
			JSONObject balanceObj = new JSONObject();
			balanceObj.put("key", pairs.getKey());
			balanceObj.put("balance", pairs.getValue());

			balconfig.put("balance", balanceArr);
			saveBalance(shouldUpdate);
		}
	}

//increase balance

	public void increaseBalance(String key, Long incBal) {
		key = key.toLowerCase();
		if (userBalances.containsKey(key)) {
			Long currentBalance = userBalances.get(key);
			long summedBalance = Math.addExact(currentBalance, incBal);
			userBalances.put(key, summedBalance);
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
		}
		saveBalance(false);
		
	}
	public static Long getBalance(String key, Long balance) {
		key = key.toLowerCase();
		
		if (userBalances.containsKey(key)) {
			return userBalances.get(key);
		} else {
			return userBalances.put(key, defaultBalance);
		}
	}
	
	public static void saveBalance(Boolean shouldUpdate) {
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
				BotManager.getInstance();
						balconfig.toJSONString();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean removeBalance(String key, Long balance){
		if(userBalances.containsKey(key)){
			userBalances.replace(key, getBalance(key, balance), defaultBalance);
			
			saveBalance(true);
			return true;
		}
		return false;
	}
	
	
/*
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
	*/
	
	
	
//end of code
}
