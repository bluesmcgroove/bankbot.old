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

import java.util.ArrayList;

public class Tester {

	static ArrayList<String>responses = new ArrayList<String>();
	static ArrayList<String>patterns = new ArrayList<String>();
	public static void main(String[] args) {
		for(int i = 0; i<5;i++){
			responses.add("test"+i);
			patterns.add("pattern"+i);
		}
		System.out.println(responses.toString());
		System.out.println(patterns.toString());
		responses.remove(2);
		responses.add(2,"test2");
		System.out.println(responses.toString());
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
