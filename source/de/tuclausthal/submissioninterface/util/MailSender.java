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
	public static String mailServer = "127.0.0.1";
	public static String from = "\"Submission Interface\" <noreply@si.in.tu-clausthal.de>";
	public static String subjectPrefix = "[SubmissionInterface] ";

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
			msg.setSubject(MimeUtility.encodeText(subjectPrefix + subject, "iso-8859-1", "Q"));
			msg.setHeader("X-Mailer", "SubmissionInterface");

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
