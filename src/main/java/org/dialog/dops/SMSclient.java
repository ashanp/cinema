package org.dialog.dops;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.*;
import java.util.Properties;


public class SMSclient implements MessageListener 
{
	private QueueConnection queueConnection;
	private QueueSender queueSender;
	private Vector temp_vec;
	private int def_sms_val=50;
	private Properties properties;
	private Context jndiContext;
	
	public SMSclient() 
	{
		temp_vec  = new Vector();
		properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
    	properties.put(Context.URL_PKG_PREFIXES,"jboss.naming:org.jnp.interfaces");	
    	try
    	{
    	jndiContext = new InitialContext(properties);
		}
		catch(Exception e){}
	}

//	public static void main(String[] args)
//	{
//		SMSclient t = new SMSclient();
//		t.send_sms("0777303770|hi how are you");
//		Vector vv = new Vector();
//		for(int a=0;a<=100;a++)
//		{
//			vv.addElement("0777303770|hi how are you");
//		}
//		t.send_bulk_sms(vv);
//		System.out.println(t.send());
//		System.gc();
//	}
	
	public void send_sms(String message)
	{
		temp_vec.addElement(message);
	}

	public void send_bulk_sms(Vector message_list)
	{
		temp_vec = message_list;
	}
	
	public void bulk_sms_delay(int delay)
	{
		def_sms_val = delay;
	}

	public boolean send()
	{
		if(temp_vec.size()>0)
		{
			try 
			{
				QueueConnectionFactory ref = (QueueConnectionFactory)jndiContext.lookup("ConnectionFactory");
				Queue queue = (Queue)jndiContext.lookup("queue/testQueue");
				queueConnection = ref.createQueueConnection();
				QueueSession queueSession =	queueConnection.createQueueSession(false,Session.AUTO_ACKNOWLEDGE);
				TemporaryQueue tq = queueSession.createTemporaryQueue();
				queueSender = queueSession.createSender(queue);
				//QueueReceiver qr = queueSession.createReceiver(tq);
				//qr.setMessageListener(this);
				TextMessage msg = queueSession.createTextMessage();
				msg.setJMSReplyTo(tq);
				queueConnection.start();
				for(int a=0; a<temp_vec.size();a++)
				{
					System.out.println("Sending message: "+(String)temp_vec.elementAt(a));
					msg.setText((String)temp_vec.elementAt(a));
					queueSender.send(msg);
					Thread.sleep(def_sms_val);
				}
				System.out.println("Message Sent to EJB Server");
			}
			catch (Exception e) 
			{
				System.out.println(e);
				return false;
			}
			return true;
		}
		else
		{
			System.out.println("Warning!!. No messages to send. please invoke send_sms()"); 
			System.out.print("or send_bulk_sms() to submit messages prior of calling the send() method");	
			return false;
		}
	}

	
	public void onMessage(Message message) 
	{
		try 
		{
			TextMessage msg1 = (TextMessage)message;
			System.out.println(msg1.getText());
		}
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
			//e.printStackTrace();
		}
	}
}
/*
 * Author: Ashan Malinda Perera 
 * Created: Thursday, December 22, 2005 4:00:00 PM
 * Last Modified on January 18, 2006
 * as a Globle SMS Gateway Client
 */