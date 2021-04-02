/*
 * Copyright 2009-2010, 2020-2021 Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of the SubmissionInterface.
 * 
 * SubmissionInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * 
 * SubmissionInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SubmissionInterface. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuclausthal.submissioninterface.util;

import java.util.Set;

import javax.persistence.Entity;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.reflections.Reflections;

/**
 * Hibernate Session Helper Singleton+Facade
 * @author Sven Strickroth
 *
 */
public class HibernateSessionHelper {
	private static final SessionFactory sessionFactory;

	static {
		StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder().configure().build();
		MetadataSources metadataSources = new MetadataSources(standardRegistry);
		Set<Class<?>> entities = new Reflections("de.tuclausthal.submissioninterface.persistence.datamodel").getTypesAnnotatedWith(Entity.class);
		for (Class<?> entity : entities) {
			metadataSources.addAnnotatedClass(entity);
		}
		Metadata metadata = metadataSources.getMetadataBuilder().build();
		sessionFactory = metadata.getSessionFactoryBuilder().build();
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
