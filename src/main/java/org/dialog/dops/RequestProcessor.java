package org.dialog.dops;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;






public class RequestProcessor {
	static OracleConnection oc;
	private static Connection con;
	Statement stmt;
	SmsPaymentDelegate smsDel;
	Thread thread1;
	
	
	
	public void RequestProcessor()
	{
		try
		{
			oc = new OracleConnection(); 
			con = oc.getConnection();
		smsDel= new SmsPaymentDelegate();
		
		stmt = con.createStatement();
		System.out.println("sdsdsds");
		}catch(Exception e){e.printStackTrace();}
	}
	
	public void start1() {
	
		thread1 = new Thread(new Runnable() {

			public void run() {
				
				while (true)
				{
					try{
					Thread.sleep(1000);
					gettype();
					}catch(Exception e){}
				}
			}
		});
		thread1.start();
	}
	
	public void gettype(){
		try
		{
		System.out.println("Checking");
		ResultSet rset_SMS_MESSAGE_LIST1 = stmt.executeQuery("select * from ABANS_SMS_CACHE");
		String mob="";
		String dat="";
		while (rset_SMS_MESSAGE_LIST1.next()) {
			
			mob = rset_SMS_MESSAGE_LIST1.getString(1);
			dat = rset_SMS_MESSAGE_LIST1.getString(2);
			stmt.executeUpdate("DELETE FROM ABANS_SMS_CACHE WHERE MOBILE_NO = '"+mob+"' and DATA = '"+dat+"'");
			con.commit();
	
			StringTokenizer st = new StringTokenizer(dat);   		
 			String firstToken = st.nextToken();
 			System.out.println("firstToken"+firstToken);
 			
			if (firstToken.equalsIgnoreCase("ABANS1")) 
 			{
 				if (st.countTokens() >= 2) {		
					String secondToken = st.nextToken();
					if (secondToken.equalsIgnoreCase("CGAMTDUE")) {
						String mcFlag = st.nextToken();
						String mcValue = st.nextToken(); 
						String txNumberDue = st.nextToken();
						System.out.println("Request For: "+mcValue);
						if ((mcFlag.equals("M"))&&(!mcValue.substring(0,2).equals("77")) && (mcValue.length()!=9)) {
							SmsSend ss = new SmsSend(mob,"Request is invalid");
							ss.run();
						} else if((mcFlag.equals("C"))&&(Integer.parseInt(mcValue) < 0)) {
							SmsSend ss = new SmsSend(mob,"Request is invalid");
							ss.run();		
						} else {
							AmountDueDelegate amtDel = new AmountDueDelegate(mcFlag,mcValue,txNumberDue,mob);
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
							SmsSend ss = new SmsSend(mob,"Request is invalid");
							ss.run();
						} else if((mcFlag.equals("C"))&&(Integer.parseInt(mcValue) < 0)) {
							SmsSend ss = new SmsSend(mob,"Request is invalid");
							ss.run();		
						} else {
							smsDel.getRecs(null,mob,mcFlag,mcValue,Amount,txNumberPay,pMode,pRef,"logPath");
							smsDel.run();
						}
					}	
 				}
 			}
		}
		}catch(Exception e){e.printStackTrace();}
	}	
}
