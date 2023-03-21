package org.dialog.dops;
import java.sql.*;

class AmountDueDelegate implements Runnable {

	private static Connection con;
  	private String mcFlag;
  	private String mcValue;
  	private String mobile;
  	private String cntID;
	private String mobileToSend;
  	private String isPre;
  	private String tranID="";
  	


public AmountDueDelegate(String _mcFlag,String _mcValue,String _tranID,String _mobileToSend) {

	mcValue = _mcValue;
	mcFlag  = _mcFlag;
	mobileToSend = _mobileToSend;
	tranID = _tranID;
	OracleConnection oc = new OracleConnection();
	con = oc.getConnection();
}

public void run(){
	CallableStatement cPackage1,cPackage2;
	int acctStat=0;
	double acctBal=0;
	int fail =0;

  	
  try {	
  			
        	if(mcFlag.equals("M")) {
            	mobile = mcValue;
        	} else if(mcFlag.equals("C")) {
        		try {
        			cntID = mcValue;
        			cPackage1 = con.prepareCall("{? = call CAM_NODE_CREATION.GET_PHONE_NO_OF_CT(?)}");
        			cPackage1.registerOutParameter(1,java.sql.Types.VARCHAR);
        			cPackage1.setString(2,cntID);
        			cPackage1.execute();
        			mobile=cPackage1.getString(1);
        			if((mobile.equals("")) || (!mobile.substring(0,2).equals("77")) ) {
        				SmsSend ss = new SmsSend(mobileToSend,"INVALID ACCOUNT "+"#ABANS");
            			ss.run();
            			fail =1;
        			}	
        		} catch(NullPointerException ne) {
					SmsSend ss = new SmsSend(mobileToSend,"INVALID ACCOUNT "+"#ABANS");
            		ss.run();
            		fail = 1;
				}
        	}
        	if ((fail == 0) && ((mobile.substring(0,2).equals("77")) && (mobile.length()==9))) {
        		cPackage2 = con.prepareCall("call SMS_ONLINE_BILL_PAY.CHK_PREPAID(?,?)");
            	cPackage2.registerOutParameter(2,java.sql.Types.VARCHAR);
            	cPackage2.setString(1,mobile);    
            	cPackage2.execute();
            	isPre = cPackage2.getString(2);
            
        		String res = URLRequest.amtDue("ABANSPAY","dfgweg",mobile,tranID);
            	int transRes = Integer.parseInt(res.substring(0,res.indexOf("|")));
            
            
            	System.out.println("Transaction Result transRes:" +transRes);
            	
            	if (transRes ==0) {
            		int first = res.indexOf("|");
            		String tempStr = res.substring(first+1,res.length());
            		acctBal = Double.parseDouble(tempStr.substring(0,tempStr.indexOf("|")));
            		System.out.println("acctBal: "+acctBal);
            	
            		acctStat = Integer.parseInt(tempStr.substring(tempStr.indexOf("|")+1,tempStr.length()));
            		System.out.println("acctStat: "+acctStat);
            		if(mcFlag.equals("M")) {
            			SmsSend ss = new SmsSend(mobileToSend,"AMTDUE "+mobile+" "+isPre+" "+acctBal+" "+acctStat+"#ABANS");
            			ss.run();
        			} else if(mcFlag.equals("C")) {
        				SmsSend ss = new SmsSend(mobileToSend,"AMTDUE "+mobile+" "+cntID+" "+isPre+" "+acctBal+" "+acctStat+"#ABANS");
            			ss.run();
        			}
            	}
            	else if (transRes ==2) {
            		System.out.println("Transaction Result: " + transRes);
            		SmsSend ss = new SmsSend(mobileToSend,"INVALID ACCOUNT "+"#ABANS");
            		ss.run();
            	}
            	else {
            		System.out.println("Transaction Result: " + transRes);
            		SmsSend ss = new SmsSend(mobileToSend,"GATEWAY FAILURE "+"#ABANS");
            		ss.run();
            	}
            }
            
            		
		} 
		catch (SQLException sqlex) {
			SmsSend ss = new SmsSend(mobileToSend,"DBERR "+"#ABANS");
    	    ss.run();
			System.err.println(sqlex);
		}
		catch (Exception ex) {
			SmsSend ss = new SmsSend(mobileToSend,"FAILED REQUEST "+"#ABANS");
    	    ss.run();
			System.err.println(ex);
		}finally {
			try {
				con.close();
			} catch (Exception e) {
				System.err.println("Inside Finally"+e);
				logError(mobileToSend.substring(3),mobile,tranID,e.toString());
			}
		}

}

public static void logError(String counter,String mob,String txNum,String err) {
		CallableStatement cLogError;
		try {
			cLogError = con.prepareCall("call SMS_ONLINE_BILL_PAY.LOG_ERROR(?,?,?,?)");
			cLogError.setString(1,counter);
        	cLogError.setString(2,mob);
        	cLogError.setString(3,txNum);
        	cLogError.setString(4,err);
        	cLogError.execute();
        	con.commit();
        	cLogError.close();
        } catch (SQLException ex) {
				System.err.println("Error not logged: "+ex);
		} catch (Exception e) {
				System.err.println("Error not logged: "+e);
		}
}

}

