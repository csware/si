/*
 * Copyright 2009, 2020-2023 Sven Strickroth <email@cs-ware.de>
 *
 * This file is part of the GATE.
 *
 * GATE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * GATE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GATE. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import jakarta.persistence.Entity;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaExport.Action;
import org.hibernate.tool.schema.TargetType;
import org.reflections.Reflections;

/**
 * Hibernate DDL-definition to SQL-exporter
 * @author Sven Strickroth
 */
public class HibernateSQLExporter {
	public static final String FILENAME = "db-schema.sql";

	public static void main(String[] fdf) throws FileNotFoundException, IOException {
		try (FileOutputStream fos = new FileOutputStream(new File(FILENAME), false)) {
			// clear file
		}

		StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder().configure().build();
		MetadataSources metadataSources = new MetadataSources(standardRegistry);
		Set<Class<?>> entities = new Reflections("de.tuclausthal.submissioninterface.persistence.datamodel").getTypesAnnotatedWith(Entity.class);
		for (Class<?> entity : entities) {
			metadataSources.addAnnotatedClass(entity);
		}
		Metadata metadata = metadataSources.getMetadataBuilder().build();

		EnumSet<TargetType> enumSet = EnumSet.of(TargetType.SCRIPT);
		SchemaExport schemaExport = new SchemaExport();
		schemaExport.setOutputFile(FILENAME);
		schemaExport.execute(enumSet, Action.CREATE, metadata);

		standardRegistry.close();
	}
}
