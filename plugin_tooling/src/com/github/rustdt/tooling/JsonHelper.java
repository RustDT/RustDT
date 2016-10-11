/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pieter Penninckx - initial API and implementation
 *******************************************************************************/
package com.github.rustdt.tooling;

import com.google.gson.JsonObject;

public class JsonHelper {
	/**
	 * @param jsonObject The JsonObject from which we want to get a member.
	 * @param memberName The name of the member to be returned.
	 * @return Null if the field is not present or not a String, 
	 * 		   the value of the member with the given name in the given JsonObject otherwise.       
	 */
	static String getStringMemberFromJsonObject(JsonObject jsonObject, String memberName) {
		if (jsonObject.has(memberName) && 
				jsonObject.get(memberName).isJsonPrimitive() && 
				jsonObject.get(memberName).getAsJsonPrimitive().isString()) {
			return jsonObject.get(memberName).getAsJsonPrimitive().getAsString();
		} else {
			return null;
		}
	}
	
	/**
	 * @param jsonObject The JsonObject for which we want to get a member.
	 * @param memberName The name of the member to be returned.
	 * @param defaultValue The value to return if the member is not present or not a boolean.
	 * @return The value of the member with the given name in the given JsonObject, or
	 *         the defaultValue if the member is not present or not a boolean. 
	 */
	static boolean getBooleanMemberFromJsonObject(JsonObject jsonObject, String memberName, boolean defaultValue) {
		if (jsonObject.has(memberName) && 
				jsonObject.get(memberName).isJsonPrimitive() && 
				jsonObject.get(memberName).getAsJsonPrimitive().isBoolean()) {
			return jsonObject.getAsJsonPrimitive(memberName).getAsBoolean();
		} else {
			return defaultValue;
		}
	}
	
	/**
	 * @param jsonObject The JsonObject for which we want to get a member.
	 * @param memberName The name of the member to be returned.
	 * @param defaultValue The value to return if the member is not present or not an integer.
	 * @return The value of the member with the given name in the given JsonObject, or
	 *         the defaultValue if the member is not present or not an integer. 
	 */
	static int getIntMemberFromJsonObject(JsonObject jsonObject, String memberName, int defaultValue) {
		if (jsonObject.has(memberName) && 
				jsonObject.get(memberName).isJsonPrimitive() && 
				jsonObject.get(memberName).getAsJsonPrimitive().isNumber()) {
			return jsonObject.getAsJsonPrimitive(memberName).getAsInt();
		} else {
			return defaultValue;
		}
	}
	
	/**
	 * @param jsonObject The JsonObject for which we want to get a member.
	 * @param memberName The name of the member to be returned.
	 * @return The value of the member with the given name in the given JsonObject, or
	 *         null if the member is not present or not a JsonObject; 
	 */
	static JsonObject getObjectMemberFromJsonObject(JsonObject jsonObject, String memberName) {
		if (jsonObject.has(memberName) && 
				jsonObject.get(memberName).isJsonObject()) {
			return jsonObject.getAsJsonObject(memberName);
		} else {
			return null;
		}
	}
}
