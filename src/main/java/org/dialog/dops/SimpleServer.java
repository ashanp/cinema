package org.dialog.dops;

import javax.management.MBeanServer;

import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.callback.InvokerCallbackHandler;
import org.jboss.remoting.transport.Connector;

import java.util.*;
import java.io.*;

public class SimpleServer
{
   private static String transport = "socket";
   private static String host = "192.168.99.72";
   private static int port = 5408;
   private static final String RESPONSE_VALUE = "This is the return to SampleInvocationHandler invocation";
   private static String logPath;
   private static ArrayList msgList = new ArrayList(100);
   private static String message;
   static SmsPaymentDelegate smsDel;

   public void setupServer(String locatorURI) throws Exception
   {
      InvokerLocator locator = new InvokerLocator(locatorURI);
      System.out.println("Starting remoting server with locator uri of: " + locatorURI);
      Connector connector = new Connector(locator);
      connector.create();
      SampleInvocationHandler invocationHandler = new SampleInvocationHandler();
      connector.addInvocationHandler("sample", invocationHandler);
      connector.start();
   }
   
   public void readConfigFile() {
      String line;
      int i=0;
      try {
        BufferedReader in = new BufferedReader(new FileReader("./Config/config.txt"));
		line=in.readLine();
        while(line != null) {
			StringTokenizer st = new StringTokenizer(line,"=");
        	String cmp = st.nextToken();
			if(cmp.equals("LogPath")) {	
				logPath = st.nextToken();
				i=1;
			}
			line=in.readLine();
		}
		if(i==0) {
			System.out.println("LogPath not mentioned in config.txt");
			System.exit(0);
		}	
      } catch (Exception ex) {
        	System.out.println("Config file not available"+ex); 
        	System.exit(0);
      }      	
   }

   public static void main(String[] args)
   {
	   
      String locatorURI = transport + "://" + host + ":" + port;
      SimpleServer server = new SimpleServer();
      server.readConfigFile();
      try
      {
         server.setupServer(locatorURI);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   public static class SampleInvocationHandler implements ServerInvocationHandler
   {
   	
   	Vector v = new Vector();
   	
      public Object invoke(InvocationRequest invocation) throws Throwable
      {
         message = (String)invocation.getParameter();
         System.out.println("Invocation request is: " + invocation.getParameter());
         System.out.println("Returning response of: " + RESPONSE_VALUE);
         v.addElement(message);
         Thread tr = new Thread(new Runnable() {
      		public void run() {
         		processMsgList();
         	}
         });
         tr.start();
         return RESPONSE_VALUE;
      }
      
      public static void delay(int msec)
      {
     	try
     	{
       	 	Thread.sleep(msec);
        } 
        catch(InterruptedException e2) 
        {
	       	System.out.println(e2);
     	}
	  }
	  
	  public void addMsgList(String msg) 
	  {
      	 		msgList.add(message); 	 
      }
      
      public synchronized void processMsgList() {
           try {      	
      			for(int j=0;j<v.size();j++) 
      			{
      				String phoneNo = "0"+v.elementAt(j).toString().substring(2,v.elementAt(j).toString().indexOf("|"));		
        			String req = v.elementAt(j).toString().substring(v.elementAt(j).toString().indexOf("|")+1, v.elementAt(j).toString().length());
         			System.out.println("phone to reply :"+phoneNo);					
         			System.out.println("msg to reply :"+req);       			
         			StringTokenizer st = new StringTokenizer(req);   		
         			String firstToken = st.nextToken();
         			
         			if (firstToken.equalsIgnoreCase("ABANS")) 
         			{
         				if (st.countTokens() >= 2) {		
							String secondToken = st.nextToken();
							if (secondToken.equalsIgnoreCase("CGAMTDUE")) {
								String mcFlag = st.nextToken();
								String mcValue = st.nextToken(); 
								String txNumberDue = st.nextToken();
								System.out.println("Request For: "+mcValue);
								if ((mcFlag.equals("M"))&&(!mcValue.substring(0,2).equals("77")) && (mcValue.length()!=9)) {
									SmsSend ss = new SmsSend(phoneNo,"Request is invalid");
									ss.run();
								} else if((mcFlag.equals("C"))&&(Integer.parseInt(mcValue) < 0)) {
									SmsSend ss = new SmsSend(phoneNo,"Request is invalid");
									ss.run();		
								} else {
									AmountDueDelegate amtDel = new AmountDueDelegate(mcFlag,mcValue,txNumberDue,phoneNo);
									amtDel.run();
								}
							}
						
							if (secondToken.equalsIgnoreCase("CGPAY")) {
								String mcFlag = st.nextToken();
								String mcValue = st.nextToken(); 
								String Amount = st.nextToken();
								String txNumberPay = st.nextToken();
                				String pMode = st.nextToken();
                				String pRef = st.nextToken();
                				if ((mcFlag.equals("M"))&&(!mcValue.substring(0,2).equals("77")) && (mcValue.length()!=9)) {
									SmsSend ss = new SmsSend(phoneNo,"Request is invalid");
									ss.run();
								} else if((mcFlag.equals("C"))&&(Integer.parseInt(mcValue) < 0)) {
									SmsSend ss = new SmsSend(phoneNo,"Request is invalid");
									ss.run();		
								} else {
									SmsPaymentDelegate smsDel = new SmsPaymentDelegate(null,phoneNo,mcFlag,mcValue,Amount,txNumberPay,pMode,pRef,logPath);
									smsDel.run();
								}
							}	
		 				}
		 			}
         			v.removeElementAt(j);
      			}
      		} catch(Exception e) {
      			System.out.println("Inside processMsgList "+e);
      		}
      	
      }

      public void addListener(InvokerCallbackHandler callbackHandler)
      {
      }

      public void removeListener(InvokerCallbackHandler callbackHandler)
      {
      }

      public void setMBeanServer(MBeanServer server)
      {
      }

      public void setInvoker(ServerInvoker invoker)
      {
      }  
   }
   
}