/*
 * Copyright 2022-2024 Sven Strickroth <email@cs-ware.de>
 *
 * This file is part of the GATE.
 *
 * This program free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.persistence;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

public class TaskDeserializer extends StdDeserializer<Task> implements ContextualDeserializer, ResolvableDeserializer {
	private static final long serialVersionUID = 1L;
	private final JsonDeserializer<Object> defaultDeserializer;
	private final List<MCOption> mcOptions;
	private final Map<Task, Map<String, byte[]>> files;

	public TaskDeserializer(final JsonDeserializer<Object> defaultDeserializer, final Map<Task, Map<String, byte[]>> files, final List<MCOption> mcOptions) {
		super(Task.class);
		this.defaultDeserializer = defaultDeserializer;
		this.files = files;
		this.mcOptions = mcOptions;
	}

	@Override
	public Task deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
		if (jp.currentToken() != JsonToken.START_OBJECT || jp.nextToken() != JsonToken.FIELD_NAME || !"taskConfiguration".equals(jp.currentName())) { // consume START_OBJECT of taskConfiguration
			throw new RuntimeException("taskConfiguration missing");
		}
		jp.nextToken(); // consume FIELD_NAME of taskConfiguration
		final Task task = (Task) defaultDeserializer.deserialize(jp, ctxt);
		jp.nextToken(); // consume END_OBJECT of taskConfiguration
		if (jp.getCurrentToken() == JsonToken.FIELD_NAME && "files".equals(jp.currentName())) {
			jp.nextToken(); // consume FIELD_NAME of files
			jp.nextToken(); // consume START_OBJECT
			while (jp.getCurrentToken() == JsonToken.FIELD_NAME && "file".equals(jp.currentName())) {
				jp.nextToken(); // consume FIELD_NAME of file
				jp.nextToken(); // consume START_OBJECT of filename
				if (!"filename".equals(jp.currentName())) {
					throw new RuntimeException("filename missing");
				}
				if (jp.nextToken() != JsonToken.VALUE_STRING) { // consume FIELD_NAME of filename
					throw new SecurityException("empty filename detected");
				}
				final String filename = jp.getText(); // checks are in TaskSerializer::storeFilesToDisk
				if (filename == null || filename.isBlank()) {
					throw new SecurityException("invalid filename detected");
				}
				jp.nextToken(); // consume VALUE_STRING of filename
				if (!"encoding".equals(jp.currentName())) {
					throw new RuntimeException("encoding missing");
				}
				jp.nextToken(); // consume FIELD_NAME of encoding
				if (!"base64".equals(jp.getText())) {
					throw new RuntimeException("unsupported encoding found, only supported is base64 at the moment");
				}
				jp.nextToken(); // consume VALUE_STRING of encoding
				if (!"content".equals(jp.currentName())) {
					throw new RuntimeException("content missing");
				}
				if (jp.nextToken() != JsonToken.VALUE_STRING) { // consume FIELD_NAME of content
					throw new SecurityException("empty content element detected");
				}
				final byte[] content = jp.getBinaryValue();
				jp.nextToken(); // consume VALUE_STRING of content
				if (jp.getCurrentToken() == JsonToken.END_OBJECT && "file".equals(jp.currentName())) {
					jp.nextToken();
				}
				files.computeIfAbsent(task, k -> new HashMap<>()).put(filename, content);
			}
			if (jp.getCurrentToken() == JsonToken.END_OBJECT && "files".equals(jp.currentName())) {
				jp.nextToken();
			}
		}
		if (jp.getCurrentToken() == JsonToken.FIELD_NAME && "scmcoptions".equals(jp.currentName())) {
			jp.nextToken(); // consume FIELD_NAME of scmcoptions
			jp.nextToken(); // consume START_OBJECT
			while (jp.getCurrentToken() == JsonToken.FIELD_NAME && "scmcoption".equals(jp.currentName())) {
				jp.nextToken(); // consume FIELD_NAME of scmcoption
				final MCOption mcoption = jp.readValueAs(MCOption.class);
				mcoption.setTask(task);
				mcOptions.add(mcoption);
				jp.nextToken();
				if (jp.getCurrentToken() == JsonToken.END_OBJECT && "scmcoption".equals(jp.currentName())) {
					jp.nextToken();
				}
			}
			if (jp.getCurrentToken() == JsonToken.END_OBJECT && "scmcoptions".equals(jp.currentName())) {
				jp.nextToken();
			}
			// need to match references here, because id is not yet set
			if (!task.isSCMCTask() && mcOptions.stream().anyMatch(mc -> mc.getTask() == task)) {
				throw new RuntimeException("MCOptions found but not a choice task");
			}
			// need to match references here, because id is not yet set
			if (task.isSCTask() && mcOptions.stream().filter(mc -> mc.getTask() == task).filter(mc -> mc.isCorrect()).count() != 1) {
				throw new RuntimeException("MCOption for single choice has not exactly one correct answer for task \"" + task.getTitle() + "\"");
			}
		}
		return task;
	}

	@Override
	public SettableBeanProperty findBackReference(final String logicalName) {
		return defaultDeserializer.findBackReference(logicalName);
	}

	@Override
	public void resolve(final DeserializationContext ctxt) throws JsonMappingException {
		((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
	}

	@Override
	public JsonDeserializer<?> createContextual(final DeserializationContext ctxt, final BeanProperty property) throws JsonMappingException {
		return this;
	}
}
