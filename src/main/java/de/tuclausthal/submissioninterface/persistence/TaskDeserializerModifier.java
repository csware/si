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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

import de.tuclausthal.submissioninterface.persistence.datamodel.MCOption;
import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

public class TaskDeserializerModifier extends BeanDeserializerModifier {
	private static final long serialVersionUID = 1L;

	private final Map<Task, Map<String, byte[]>> files;
	private final List<MCOption> mcOptions;

	public TaskDeserializerModifier(final Map<Task, Map<String, byte[]>> files, final List<MCOption> mcOptions) {
		this.files = files;
		this.mcOptions = mcOptions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JsonDeserializer<?> modifyDeserializer(final DeserializationConfig config, final BeanDescription beanDesc, final JsonDeserializer<?> deserializer) {
		if (beanDesc.getBeanClass().equals(Task.class)) {
			return new TaskDeserializer((JsonDeserializer<Object>) deserializer, files, mcOptions);
		}
		return deserializer;
	}
}
