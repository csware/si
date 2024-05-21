/*
 * Copyright 2024 Sven Strickroth <email@cs-ware.de>
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.tuclausthal.submissioninterface.persistence.datamodel.Task;

public class TaskDescriptionSerializer extends StdSerializer<String> {
	private static final long serialVersionUID = 1L;
	public final static String TASKID_PLACEHOLDER = "taskid=--GATETASKID--";

	public TaskDescriptionSerializer() {
		super(String.class);
	}

	@Override
	public void serialize(final String value, final JsonGenerator generator, final SerializerProvider prov) throws IOException {
		final Task task = (Task) generator.currentValue();
		generator.writeString(value.replaceAll("taskid=" + String.valueOf(task.getTaskid()), TASKID_PLACEHOLDER));
	}
}
