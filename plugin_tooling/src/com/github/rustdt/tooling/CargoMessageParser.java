/*******************************************************************************
 * Copyright (c) 2016 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pieter Penninckx - initial implementation
 *******************************************************************************/
package com.github.rustdt.tooling;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;

import java.io.IOException;
import java.io.Reader;

import com.github.rustdt.tooling.CargoMessage.CargoMessageTarget;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import melnorme.lang.utils.gson.GsonHelper;
import melnorme.lang.utils.gson.JsonParserX;
import melnorme.lang.utils.gson.JsonParserX.JsonSyntaxExceptionX;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.core.CommonException;

public class CargoMessageParser {
	
	protected final GsonHelper helper = new GsonHelper();
	
	protected final Reader reader;
	protected final JsonReader jsonReader;
	
	public CargoMessageParser(Reader reader) {
		this.reader = assertNotNull(reader);
		this.jsonReader = new JsonReader(reader);
		this.jsonReader.setLenient(true);
	}
	
	public ArrayList2<CargoMessage> parseCargoMessages() throws CommonException {
		ArrayList2<CargoMessage> cargoMessages = new ArrayList2<>();
		cargoMessages.collectUntilNull(this::parseCargoMessage);
		return cargoMessages;
	}
	
	public CargoMessage parseCargoMessage() throws CommonException {
		try {
			return doParseCargoMessage();
		} catch(JsonSyntaxExceptionX e) {
			throw new CommonException("JSON syntax error in Cargo message: ",  e);
		} catch (IOException ioe) {
			throw new CommonException("Unexpected IO Exception: ", ioe);
		}
	}
	
	public CargoMessage doParseCargoMessage() throws IOException, JsonSyntaxExceptionX, CommonException {
		if(JsonParserX.isEndOfInput(jsonReader)) {
			return null;
		}
		
		JsonElement element = new JsonParserX().parse(jsonReader);
		JsonObject cargoMsg = helper.asObject(element);
		
		String reason = helper.getStringOr(cargoMsg, "reason", "");
		String packageId = helper.getStringOr(cargoMsg, "package_id", "");
		
		CargoMessageTarget msgTarget = parseCargoMessageTarget(helper.getObject(cargoMsg, "target"));
		RustMainMessage message = parseRustMessage(helper.getObject(cargoMsg, "message"));
		
		return new CargoMessage(reason, packageId, msgTarget, message);
	}
	
	protected RustMainMessage parseRustMessage(JsonObject object) throws CommonException {
		RustJsonMessageParser rustMessageParser = new RustJsonMessageParser();
		return rustMessageParser.parseTopLevelRustMessage(object);
	}
	
	protected CargoMessageTarget parseCargoMessageTarget(JsonObject messageTarget) throws CommonException {
		JsonArray jsonArray = helper.getArray(messageTarget, "kind");
		ArrayList2<String> kind = new ArrayList2<>(jsonArray).mapx((jsonElement) -> {
			return helper.asString(jsonElement);
		});
		
		String name = helper.getStringOr(messageTarget, "name", "");
		String path = helper.getStringOr(messageTarget, "src_path", "");
		
		return new CargoMessageTarget(kind, name, path);
	}
	
}