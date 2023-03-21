package org.dialog.dops;

/**
 * @author Ashan Malinda Perera (JKCS)
 * @Date Thursday, December 22, 2005 4:00:00 PM
 * @Modified on Friday, February 24, 2006
 * @Purpose To create a SMS Credit Transfer Application 
 */

import javax.management.MBeanServer;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.callback.InvokerCallbackHandler;
import org.jboss.remoting.transport.Connector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class MainClass {
	static SmsPaymentDelegate smsDel;

	static OracleConnection oc;

	private static Connection con;

	private static String transport = "";

	private static String host = "";

	private static int port = 0;

	private static final String responseValue = "This is the return to SampleInvocationHandler invocation";

	static int count = 0;

	public void setupServer(String locatorURI) throws Exception {
		InvokerLocator locator = new InvokerLocator(locatorURI);
		Connector connector = new Connector(locator);
		connector.create();
		SampleInvocationHandler invocationHandler = new SampleInvocationHandler();
		connector.addInvocationHandler("sample", invocationHandler);
		connector.start();
	}

	public static void main(String[] args) {
		try {
			RequestProcessor reqProcessor = new RequestProcessor();
			reqProcessor.RequestProcessor();
			reqProcessor.start1();
			Properties prop = new Properties();
			prop.load(ClassLoader.getSystemResourceAsStream("./remotingConfig/remotingConfig.properties"));
			host = prop.getProperty("dialog.org.sharecredit.host");
			port = Integer.parseInt(prop.getProperty("dialog.org.sharecredit.port"));
			transport = prop.getProperty("dialog.org.sharecredit.transport");
		} catch (Exception e) {
		}
		smsDel = new SmsPaymentDelegate();
		oc = new OracleConnection();
		con = oc.getConnection();
		String locatorURI = transport + "://" + host + ":" + port;
		MainClass server = new MainClass();
		try {
			server.setupServer(locatorURI);
		} catch (Exception e) {
			System.out.println("Error:" + e);
		}
	}

	public static class SampleInvocationHandler implements
			ServerInvocationHandler {
		public Object invoke(InvocationRequest invocation) throws Throwable {
			String message = (String) invocation.getParameter();
			System.out.println(message);
			String mobile_no = message.substring(2, 11);
			String requestData = message.substring(12, message.length());
			System.out.println(mobile_no);
			System.out.println(requestData);
			add_data(mobile_no, requestData);
			return responseValue;
		}
 
		public void add_data(String mobile_no, String data) throws Exception {
			Statement stmt;
			stmt = con.createStatement();
			try {
				stmt.executeUpdate("INSERT INTO ABANS_SMS_CACHE VALUES('"
						+ mobile_no.trim() + "','" + data.trim() + "')");
				con.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		public void addListener(InvokerCallbackHandler callbackHandler) {
		}

		public void removeListener(InvokerCallbackHandler callbackHandler) {
		}

		public void setMBeanServer(MBeanServer server) {
		}

		public void setInvoker(ServerInvoker invoker) {
		}
	}

}
/**
 * @author Ashan Malinda Perera (JKCS)
 * @Date Thursday, December 22, 2005 4:00:00 PM
 * @Modified on Friday, February 24, 2006
 * @Purpose To create a SMS Credit Transfer Application
 */
