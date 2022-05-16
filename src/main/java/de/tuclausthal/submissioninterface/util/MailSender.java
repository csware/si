/*
 * Copyright 2011, 2020-2022 Sven Strickroth <email@cs-ware.de>
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
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailSender {
	final private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static boolean sendMail(String to, String subject, String messageText, List<File> attachments) {
		MimeMessage msg;
		Properties props = new Properties();
		props.put("mail.smtp.host", Configuration.getInstance().getMailServer());

		Session session = Session.getDefaultInstance(props, null);

		msg = new MimeMessage(session);

		try {
			// Im folgenden werden die Absenderadresse, der direkte
			// Empfänger, das Absendedatum, der Betreff kodiert in
			// US-ASCII Zeichen und der Headereintrag "X-Mailer" gesetzt
			msg.setFrom(new InternetAddress(Configuration.getInstance().getMailFrom()));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
			msg.setSentDate(new Date());
			subject = subject.replace("\r", " ");
			subject = subject.replace("\n", " ");
			msg.setSubject(Configuration.getInstance().getMailSubjectPrefix() + subject, "UTF-8");
			msg.setHeader("X-Mailer", "GATE");

			if (attachments == null || attachments.isEmpty()) {
				// kein Anhang, Mailtext wird direkt der Mail hinzugefügt.
				msg.setText(messageText, "UTF-8");
			} else {
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(messageText, "UTF-8");

				MimeMultipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);

				for (File file : attachments) {
					MimeBodyPart messagePart = new MimeBodyPart();
					DataSource source = new FileDataSource(file);
					messagePart.setDataHandler(new DataHandler(source));
					messagePart.setFileName(file.getName());
					multipart.addBodyPart(messagePart);
				}

				msg.setContent(multipart);
			}

			Transport.send(msg);
		} catch (MessagingException e) {
			LOG.error("Fehler MessagingException in MailSender: ", e);
			return false;
		}
		return true;
	}

	public static boolean sendMail(String to, String subject, String messageText) {
		return sendMail(to, subject, messageText, null);
	}
}
