/*
 * Copyright 2009-2010, 2020-2023 Sven Strickroth <email@cs-ware.de>
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

import java.lang.invoke.MethodHandles;
import java.util.Set;

import jakarta.persistence.Entity;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate Session Helper Singleton+Facade
 * @author Sven Strickroth
 *
 */
public class HibernateSessionHelper {
	final private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final SessionFactory sessionFactory;

	static {
		StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder().configure().build();
		MetadataSources metadataSources = new MetadataSources(standardRegistry);
		Set<Class<?>> entities = new Reflections("de.tuclausthal.submissioninterface.persistence.datamodel").getTypesAnnotatedWith(Entity.class);
		for (Class<?> entity : entities) {
			metadataSources.addAnnotatedClass(entity);
		}
		Metadata metadata = metadataSources.getMetadataBuilder().build();
		SessionFactory sf = null;
		try {
			sf = metadata.getSessionFactoryBuilder().build();
		} catch (HibernateException e) {
			LOG.error("Hibernate SessionFactory Builder failed:", e);
			try {
				Thread.sleep(5000); // HACK to allow some cleanup in background
			} catch (InterruptedException e1) {
			}
		}
		sessionFactory = sf;
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
