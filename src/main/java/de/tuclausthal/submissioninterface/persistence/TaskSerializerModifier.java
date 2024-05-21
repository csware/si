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

import java.nio.file.Path;

import org.hibernate.Session;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

public class TaskSerializerModifier extends BeanSerializerModifier {
	private static final long serialVersionUID = 1L;

	final private Session session;
	final private Path lecturePath;

	public TaskSerializerModifier(final Session session, final Path lecturePath) {
		this.session = session;
		this.lecturePath = lecturePath;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JsonSerializer<?> modifySerializer(final SerializationConfig config, final BeanDescription beanDesc, final JsonSerializer<?> serializer) {
		if (beanDesc.getBeanClass().equals(Task.class)) {
			return new TaskSerializer((JsonSerializer<Object>) serializer, session, lecturePath);
		}
		return serializer;
	}
}
