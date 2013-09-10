/*
 * Copyright 2011 Sven Strickroth <email@cs-ware.de>
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

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

public class MailSender {
	public final static String mailServer = "127.0.0.1";
	public final static String from = "\"GATE\" <noreply@si.in.tu-clausthal.de>";
	public final static String subjectPrefix = "[GATE] ";

	public static void sendMail(String to, String subject, String messageText) {
		MimeMessage msg;
		Properties props = System.getProperties();
		props.put("mail.smtp.host", mailServer);

		Session session = Session.getDefaultInstance(props, null);

		msg = new MimeMessage(session);

		try {
			// Im folgenden werden die Absenderadresse, der direkte
			// Empfänger, das Absendedatum, der Betreff kodiert in
			// US-ASCII Zeichen und der Headereintrag "X-Mailer" gesetzt
			msg.setFrom(new InternetAddress(from));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
			msg.setSentDate(new Date());
			subject = subject.replace("\r", " ");
			subject = subject.replace("\n", " ");
			msg.setSubject(MimeUtility.encodeText(subjectPrefix + subject, "iso-8859-1", "Q"));
			msg.setHeader("X-Mailer", "GATE");

			// kein Anhang, Mailtext wird direkt der Mail hinzugefügt.
			msg.setText(messageText, "iso-8859-1");

			Transport.send(msg);
		} catch (java.io.UnsupportedEncodingException e) {
			System.out.println("Fehler UnsupportedEncodingException in MailSender: " + e.getMessage());
			e.printStackTrace();
		} catch (MessagingException e) {
			System.out.println("Fehler MessagingException in MailSender: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
