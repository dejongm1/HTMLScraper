package com.mcd.scraper.util;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

/**
 * 
 * @author U569220
 *
 */

//implement or create as a test
//public class SendMailSSL{    
//public static void main(String[] args) {    
//  //from,password,to,subject,message  
//  EmailUtil.send("from@gmail.com","xxxxx","to@gmail.com","hello javatpoint","How r u?");  
//  //change from, password and to  
//}    
//}  

//Might need to do this - https://myaccount.google.com/lesssecureapps?pli=1

public class EmailUtil {  

	private static final Logger logger = Logger.getLogger(EmailUtil.class);

	private EmailUtil(){}

	public static void send(String from,String password,String to,String sub,String msg){  
		Util util = new Util();
		if (!util.offline()) {
			//Create new properties object to only pass email props??
		
			//		props.setProperty("mail.smtp.host");  
			//		props.setProperty("mail.smtp.socketFactory.port");  
			//		props.setProperty("mail.smtp.socketFactory.class");
			//		props.setProperty("mail.smtp.auth");    
			//		props.setProperty("mail.smtp.port");    
			//get Session   
			Session session = Session.getDefaultInstance(util.getProperties(),    
					new javax.mail.Authenticator() {    
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {    
					return new PasswordAuthentication(from,password);  
				}    
			});    
			//compose message    
			try {    
				MimeMessage message = new MimeMessage(session);    
				message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));    
				message.setSubject(sub);    
				message.setText(msg);    
				//send message  
				Transport.send(message);    
				logger.info("Email sent successfully");    
			} catch (MessagingException e) {
				logger.info("Error while sending email \n"
					+ "From: " + from
					+ "To: " + to
					+ "Subject: " + sub
					+ "Message: " + msg, e);
				throw new RuntimeException(e);
			}
		} else {
			logger.info("Offline, here are the email details \n"
					+ "Error while sending email \n"
					+ "From: " + from + " \n"
					+ "To: " + to+ " \n"
					+ "Subject: " + sub+ " \n"
					+ "Message: " + msg);
		}
	}  
}  
