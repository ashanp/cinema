package org.dialog.dops;

import java.util.Properties;
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


public class MessageBeanClient implements MessageListener {
	private QueueConnection queueConnection;

	static int a1 = 0;

	static int limit = 0;

	private QueueSender queueSender;
	
	private Properties properties;
	
	private Context jndiContext;

	public void send(String messag_1) {
		try {
			properties = new Properties();
			properties.put(Context.INITIAL_CONTEXT_FACTORY,"org.jnp.interfaces.NamingContextFactory");
			properties.put(Context.URL_PKG_PREFIXES,"jboss.naming:org.jnp.interfaces");
			properties.put(Context.PROVIDER_URL, "172.26.1.5:1099"); // HA-JNDI
																		// port.
			try {
				jndiContext = new InitialContext(properties);
			} catch (Exception e) {
				System.out.println(e);
			}
			
			
			QueueConnectionFactory ref = (QueueConnectionFactory) jndiContext
					.lookup("ConnectionFactory");
			Queue queue = (Queue) jndiContext.lookup("queue/testQueue");
			queueConnection = ref.createQueueConnection();
			QueueSession queueSession = queueConnection.createQueueSession(
					false, Session.AUTO_ACKNOWLEDGE);
			TemporaryQueue tq = queueSession.createTemporaryQueue();
			queueSender = queueSession.createSender(queue);
			TextMessage msg = queueSession.createTextMessage();
			msg.setJMSReplyTo(tq);
			queueConnection.start();
			System.out.println("Server Response is " + messag_1);
			msg.setText(messag_1+"#Dialog");
			queueSender.send(msg);
			System.out
					.println("=============================================================================");

			queueConnection.stop();
			queueConnection.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error found!!!");
		}
	}

	public Context getInitialContext() throws javax.naming.NamingException {
		return new InitialContext();
	}

	public void onMessage(Message message) {
		try {

			TextMessage msg1 = (TextMessage) message;
			System.out.println(msg1.getText());

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}