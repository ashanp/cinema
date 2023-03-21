package org.dialog.dops;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class URLRequest
{
	HttpURLConnection httpCon;
	String respondBody;
	static URLRequest u;
	
	public URLRequest()
	{
	}
     
	public URLRequest(String host) throws MalformedURLException,IOException{
	
		URL url = new URL(host);
		httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
	
	}
	
	public URLRequest(String host,String sessionID) throws MalformedURLException,IOException{
		URL url = new URL(host);
		httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
		httpCon.setRequestProperty("Cookie", "JSESSIONID="+sessionID);
	}
	
	public void connect() throws IOException
	{
	
	   	httpCon.connect();
	   	BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
	   	String line = null;
	   	String inputLine=null; 
		while ((line = in.readLine()) != null) 
		{
		  	inputLine = line;
	   	}
	   	in.close();
	   	respondBody =inputLine;	
	
	}
	
	public String getBody()
	{
		return respondBody;    
	} 
	
	public void disconnect()
	{
		httpCon.disconnect();
		
	}
	
	public static void main(String argv[])
	{
	    try
	    {
	    	
		}
        catch(Exception e)
        {
			e.printStackTrace();
		}
	}
//	public static synchronized String amtDue(String uName,String pWord,String mobile,String tranID)
//	{
//		String ret=null;
//		try {
//			
//			String session = sessionInit(uName,pWord);
//			String strRes = URLRequest.creditBalance(mobile,tranID,session);
//			String tranRes=URLRequest.getTransResult(strRes);
//			String acctBal = URLRequest.getAcctBal(strRes);
//			String acctStat = URLRequest.getAcctStatus(strRes);
//			ret= tranRes+"|"+acctBal+"|"+acctStat;
//			
//			return ret;
//		} catch(Exception e) {
//			System.out.println(e);
//		}
//		return ret;
//	}
    
    public static String amtDue(String uName,String pWord,String mobile,String tranID)
    {
        String ret=null;
        try {
            
            String session = sessionInit(uName,pWord);
            String strRes = URLRequest.creditBalance(mobile,tranID,session);
            String tranRes=URLRequest.getTransResult(strRes);
            String acctBal = URLRequest.getAcctBal(strRes);
            String acctStat = URLRequest.getAcctStatus(strRes);
            ret= tranRes+"|"+acctBal+"|"+acctStat;
            
            return ret;
        } catch(Exception e) {
            System.out.println(e);
        }
        return ret;
    }
    
	
//	public static synchronized String paymentCash(String uName,String pWord,String mobile,double amnt,String tranID)
//	{
//		String ret=null;
//		try {
//			
//			String session = sessionInit(uName,pWord);
//			String strRes = URLRequest.creditAccountCash(uName,mobile,session,amnt,tranID);
//			String tranRes=URLRequest.getTransResult(strRes);
//			String tranRecpt=URLRequest.getReceipt(strRes);
//			ret= tranRes+"|"+tranRecpt;
//			return ret;
//			/*		
//			ret= Integer.parseInt(tranRes);
//			return ret;
//			*/
//		} catch(Exception e) {
//			System.out.println(e);
//		}
//		return ret;
//	}
    
    public static String paymentCash(String uName,String pWord,String mobile,double amnt,String tranID)
    {
        String ret=null;
        try {
            
            String session = sessionInit(uName,pWord);
            String strRes = URLRequest.creditAccountCash(uName,mobile,session,amnt,tranID);
            String tranRes=URLRequest.getTransResult(strRes);
            String tranRecpt=URLRequest.getReceipt(strRes);
            ret= tranRes+"|"+tranRecpt;
            return ret;
            /*      
            ret= Integer.parseInt(tranRes);
            return ret;
            */
        } catch(Exception e) {
            System.out.println(e);
        }
        return ret;
    }
	
//	public static synchronized String paymentCard(String uName,String pWord,String mobile,double amnt,String tranID)
//	{
//		String ret=null;
//		try {
//			
//			String session = sessionInit(uName,pWord);
//			String strRes = URLRequest.creditAccountCard(uName,mobile,session,amnt,tranID);
//			String tranRes=URLRequest.getTransResult(strRes);
//			String tranRecpt=URLRequest.getReceipt(strRes);
//			ret= tranRes+"|"+tranRecpt;
//			return ret;		
//			/*
//			ret= Integer.parseInt(tranRes);
//			return ret;
//			*/
//		} catch(Exception e) {
//			System.out.println(e);
//		}
//		return ret;
//	}
    
    public static String paymentCard(String uName,String pWord,String mobile,double amnt,String tranID)
    {
        String ret=null;
        try {
            
            String session = sessionInit(uName,pWord);
            String strRes = URLRequest.creditAccountCard(uName,mobile,session,amnt,tranID);
            String tranRes=URLRequest.getTransResult(strRes);
            String tranRecpt=URLRequest.getReceipt(strRes);
            ret= tranRes+"|"+tranRecpt;
            return ret;     
            /*
            ret= Integer.parseInt(tranRes);
            return ret;
            */
        } catch(Exception e) {
            System.out.println(e);
        }
        return ret;
    }
    
    
	
	public static String sessionInit(String uName,String pWord) throws MalformedURLException,IOException
	{
		String session=null;
		try {
		u=new URLRequest("http://172.26.1.40:8989/CGApps/jsp/cgAction.jsp?user_name="+uName+"&password="+pWord);
		u.connect();
		session=u.getBody();
		System.out.println("Body :"+session);
		//u.disconnect();
		return session;
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return session;
	}
	

	
	public static String creditAccountCash(String uName,String Mobile_number,String session,double Amount,String transID) throws MalformedURLException,IOException
	{
		String res=null;
		try {
		u=new URLRequest("http://172.26.1.40:8989/CGApps/jsp/cgAction.jsp?transType=ADJ&reqString=" +uName+ "|" + transID+"|"+Mobile_number+"|CREDIT|1|"+Amount+"|244|1|N",session);
		System.out.println("http://172.26.1.40:8989/CGApps/jsp/cgAction.jsp?transType=ADJ&reqString=" +uName+ "|" + transID+"|"+Mobile_number+"|CREDIT|1|"+Amount+"|244|1|N");
		u.connect();
		res=u.getBody();
		System.out.println("Body :"+res);
		//u.disconnect();
		return res;
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return res;
	}
	
	public static String creditAccountCard(String uName,String Mobile_number,String session,double Amount,String transID) throws MalformedURLException,IOException
	{
		String res=null;
		try {
		u=new URLRequest("http://172.26.1.40:8989/CGApps/jsp/cgAction.jsp?transType=ADJ&reqString=" +uName+ "|" + transID+"|"+Mobile_number+"|CARD|"+Amount+"|244|ABANS|ABANS|ABANS123",session);
		System.out.println("http://172.26.1.40:8989/CGApps/jsp/cgAction.jsp?transType=ADJ&reqString=" +uName+ "|" + transID+"|"+Mobile_number+"|CARD|"+Amount+"|244|ABANS|ABANS|ABANS123");
		u.connect();
		res=u.getBody();
		System.out.println("Body :"+res);
		//u.disconnect();
		return res;
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return res;
	}
	
	
	public static String creditBalance(String Mobile_number,String tranId,String session) throws MalformedURLException,IOException,InterruptedException
	{
		String res=null;	
		try {
		Mobile_number = Mobile_number.trim();
		u=new URLRequest("http://172.26.1.40:8989/CGApps/jsp/cgAction.jsp?transType=CHK&reqString=ABANSPAY|"+tranId+"|"+Mobile_number,session);
		u.connect();
		res=u.getBody();
		System.out.println("Body :"+res);
		
		//u.disconnect();
		return res;
		} catch(Exception e) {
			System.out.println(e);
		}
		return res;
	} 
	
	public static String getTransResult(String res)
    {
    	String tempString3="";
    	int first = res.indexOf("|");
		String tempString = res.substring(first+1,res.length());
		int nd_index = tempString.indexOf("|");
		String tempString1 = tempString.substring(nd_index+1,tempString.length());	
		int rd_index1 = tempString1.indexOf("|");		
		String tempString2 = tempString1.substring(rd_index1+1,tempString1.length());			
		int final1 = tempString2.indexOf("|");
		if (final1 == -1) {
			tempString3 = tempString2.substring(0);
		}
		else {
			tempString3 = tempString2.substring(0,final1);
		}		
		//int a=Integer.parseInt(tempString3);
   		return tempString3; 	
    }
    
    public static String getAcctBal(String val)
    {
    	int first = val.indexOf("|");
		String tempString = val.substring(first+1,val.length());
		int nd_index = tempString.indexOf("|");
		String tempString1 = tempString.substring(nd_index+1,tempString.length());	
		int rd_index1 = tempString1.indexOf("|");		
		String tempString2 = tempString1.substring(rd_index1+1,tempString1.length());			
		int rd_index2 = tempString2.indexOf("|");		
		String tempString3 = tempString2.substring(rd_index2+1,tempString2.length());
		int rd_index3 = tempString3.indexOf("|");		
		String tempString4 = tempString3.substring(rd_index3+1,tempString3.length());
		int rd_index4 = tempString4.indexOf("|");		
		String tempString5 = tempString4.substring(rd_index4+1,tempString4.length());
		int final1 = tempString5.indexOf("|");
		String tempString6 = tempString5.substring(0,final1);
   		//double d = Double.parseDouble(tempString6);
   		return tempString6;	
    }
    
    public static String getAcctStatus(String val)
    {
    	int first = val.indexOf("|");
		String tempString = val.substring(first+1,val.length());
		int nd_index = tempString.indexOf("|");
		String tempString1 = tempString.substring(nd_index+1,tempString.length());	
		int rd_index1 = tempString1.indexOf("|");		
		String tempString2 = tempString1.substring(rd_index1+1,tempString1.length());			
		int rd_index2 = tempString2.indexOf("|");		
		String tempString3 = tempString2.substring(rd_index2+1,tempString2.length());
		int rd_index3 = tempString3.indexOf("|");
		String tempString4 = tempString3.substring(rd_index3+1,tempString3.length());
		int final1 = tempString4.indexOf("|");
		String tempString5 = tempString4.substring(0,final1);
   		//int a=Integer.parseInt(tempString5);
   		return tempString5; 
    }
    
    public static String getReceipt(String val) {
    	int first = val.indexOf("|");
		String tempString = val.substring(first+1,val.length());
		int nd_index = tempString.indexOf("|");
		String tempString1 = tempString.substring(nd_index+1,tempString.length());	
		int rd_index1 = tempString1.indexOf("|");		
		String tempString2 = tempString1.substring(rd_index1+1,tempString1.length());			
		int rd_index2 = tempString2.indexOf("|");		
		String tempString3 = tempString2.substring(rd_index2+1,tempString2.length());
		int final1 = tempString3.indexOf("|");
		String tempString4 = tempString3.substring(0,final1);
		return tempString4;
    }
}