package org.dialog.dops;

import java.sql.*;
import java.io.*;


class SmsPaymentDelegate implements Runnable {

	private static Connection con;
	private String mcFlag;
  	private String mcValue;
  	private static String mobile;
  	private static String cntID;
	private static double amnt;
	private static String mobileToSend;
	private static int accNo;
	private static String receipt;
	private static String _path;
	private static String _txNumber;
  	private static String _pmode;
  	private static String isPre;
  	private static String preFlag;
  	private static String counter_id;
  	java.util.Date now = new java.util.Date();
  	

public SmsPaymentDelegate() {
	
	/*System.out.println("Payment Started ");
	System.out.println("Check1 Connecting to oracle");
	OracleConnection oc = new OracleConnection();
	con = oc.getConnection();
	_path = filePath;
	_txNumber = txNumber;
  	_pmode = pmode;
  	mcFlag = _mcFlag;
	mcValue = _mcValue;
	mobileToSend = _mobileToSend;
  	amnt = (new Double(_amnt).doubleValue());*/
}

public SmsPaymentDelegate(Object object, String phoneNo, String mcFlag2, String mcValue2, String amount,
        String txNumberPay, String pMode, String pRef, String logPath) {
}

public void getRecs(Connection _con,String _mobileToSend,String _mcFlag,String _mcValue,String _amnt,String txNumber,String pmode,String pref,String filePath)
{
	System.out.println("Payment Started ");
	System.out.println("Check1 Connecting to oracle");
	OracleConnection oc = new OracleConnection();
	con = oc.getConnection();
	_path = filePath;
	_txNumber = txNumber;
  	_pmode = pmode;
  	mcFlag = _mcFlag;
	mcValue = _mcValue;
	mobileToSend = _mobileToSend;
  	amnt = (new Double(_amnt).doubleValue());
}

private static void appendToLog(String lineTobeAppended) {
	
	File f = new File(_path);
	BufferedWriter bWriter;
	
	try {
	
		if (!f.exists()) {
			f.createNewFile();	
		}
	
		bWriter = new BufferedWriter(new FileWriter(_path,true));
		
		bWriter.write(lineTobeAppended,0,lineTobeAppended.length());
		bWriter.newLine();
		
		bWriter.flush();
		bWriter.close();
		
	
	} catch (Exception e) {
  	System.err.println(e);
	}
	
}

public void run(){
	
	CallableStatement cPackage1, cPackage2;

	try {
		
			if(mcFlag.equals("M")) {
            	mobile = mcValue;
        	} else if(mcFlag.equals("C")) {
        		cntID = mcValue;
        		cPackage1 = con.prepareCall("{? = call CAM_NODE_CREATION.GET_PHONE_NO_OF_CT(?)}");
        		cPackage1.registerOutParameter(1,java.sql.Types.VARCHAR);
        		cPackage1.setString(2,cntID);
        		cPackage1.execute();
        		mobile=cPackage1.getString(1);	
        	}
            cPackage1 = con.prepareCall("call SMS_ONLINE_BILL_PAY.CHK_PREPAID(?,?)");
            cPackage1.registerOutParameter(2,java.sql.Types.VARCHAR);
            cPackage1.setString(1,mobile);    
            cPackage1.execute();
            isPre = cPackage1.getString(2);
            System.out.println("isPre "+isPre);
            
           	cPackage2 = con.prepareCall("call SMS_ONLINE_BILL_PAY.CHK_SMS_TRANSACTION(?,?,?,?,?,?,?,?)");
            cPackage2.registerOutParameter(4,java.sql.Types.VARCHAR);
            cPackage2.registerOutParameter(5,java.sql.Types.VARCHAR);
            cPackage2.registerOutParameter(6,java.sql.Types.NUMERIC);
            cPackage2.registerOutParameter(7,java.sql.Types.VARCHAR);
            cPackage2.registerOutParameter(8,java.sql.Types.VARCHAR);
            cPackage2.setString(1,mobileToSend.substring(3));
            cPackage2.setString(2,mobile);
            cPackage2.setString(3,_txNumber);
            cPackage2.execute();
                
            
            String error_code = cPackage2.getString(5); 
            accNo   = cPackage2.getInt(6);
            counter_id = cPackage2.getString(7); 
            preFlag = cPackage2.getString(8);
            //receipt = cPackage2.getString(4).substring(0,cPackage2.getString(4).indexOf(counter_id))+counter_id; 
            receipt = cPackage2.getString(4).replaceFirst(counter_id,"");


			if((isPre.equals("T")  && preFlag.equals("N"))) {
				SmsSend ss = new SmsSend(mobileToSend,"PPBLOCK"+"#ABANS");
            	ss.run();
			}
            else if (error_code == null) {   // trx already entered
            	if (isPre.equalsIgnoreCase("F")) {
            		if(mcFlag.equals("M")) {
            			SmsSend ss = new SmsSend(mobileToSend,"PAID "+mobile+" "+amnt+" "+receipt+ " "+accNo+"#ABANS");
            			ss.run();
        			} else if(mcFlag.equals("C")) {
        				SmsSend ss = new SmsSend(mobileToSend,"PAID "+mobile+" "+cntID+" "+amnt+" "+receipt+ " "+accNo+"#ABANS");
            			ss.run();
        			}
            	}
            	else if (isPre.equalsIgnoreCase("T")) {
            		if(mcFlag.equals("M")) {
            			SmsSend ss = new SmsSend(mobileToSend,"TOPUP "+mobile+" "+amnt+" "+receipt+ " "+accNo+"#ABANS");
            			ss.run();
        			} else if(mcFlag.equals("C")) {
        				SmsSend ss = new SmsSend(mobileToSend,"TOPUP "+mobile+" "+cntID+" "+amnt+" "+receipt+ " "+accNo+"#ABANS");
            			ss.run();
        			}
                }
                else if (isPre.equalsIgnoreCase("N")) {
            		SmsSend ss = new SmsSend(mobileToSend,"INVALID ACCOUNT "+"#ABANS");
            		ss.run();
                }
                //appendToLog(mobile+" "+mobileToSend+" "+amnt+" "+receipt+" "+accNo+" "+0+" "+" Existing Transaction Request"+" "+now);
                appendToLog(mobile+" "+counter_id+" "+amnt+" "+receipt+" "+accNo+" "+0+" "+" Existing Transaction Request"+" "+now);
            } else if (error_code.equalsIgnoreCase("COUNTER NOT ALLOWED") ){   // unauthorized counter
                    SmsSend ss = new SmsSend(mobileToSend,"INVCNT "+error_code+"#ABANS");
                    ss.run();
            } else if (error_code.equalsIgnoreCase("NEW") ){  // new trx                
            	//doProcess("ABANSPAY","dfgweg");
            	/*Modified due to the request to use counter_id as the username and application id 
            	  in the CG - Dinesh - 20/06/2006*/
            	doProcess(counter_id,"dfgweg");
				            
         	} else {
         	  SmsSend ss = new SmsSend(mobileToSend,"DBERR");
         	  ss.run();
          	}	
         	
		cPackage1.close();
		cPackage2.close();	
	
          
		} catch (SQLException se) {
				// Database Error
				SmsSend ss = new SmsSend(mobileToSend,"DBERR in updation"+"#ABANS");
				ss.run();
				System.err.println(se);
				System.err.println(se.getErrorCode());
		System.err.println(se.getSQLState());
				logError(mobileToSend.substring(3),mobile,_txNumber,se.toString());
		} catch (Exception e) {
				// Other Errors
				SmsSend ss = new SmsSend(mobileToSend,"TRXFLD "+"#ABANS");
				ss.run();
				System.err.println(e);
				logError(mobileToSend.substring(3),mobile,_txNumber,e.toString());
		} 
	}
	
	public void doProcess(String ame,String ord) {
		int transRes=0;
		String res = null;
		CallableStatement cLog;

		
		try {
			
        	if(_pmode.equals("CASH")) {                 	
                     res = URLRequest.paymentCash(ame,ord,mobile,amnt,_txNumber); 
                     transRes = Integer.parseInt(res.substring(0,res.indexOf("|")));
                     receipt = res.substring(res.indexOf("|")+1,res.length());
            }   
                else if (_pmode.equals("CARD")) {
                     res = URLRequest.paymentCard(ame,ord,mobile,amnt,_txNumber);
                     transRes = Integer.parseInt(res.substring(0,res.indexOf("|")));        
                     receipt = res.substring(res.indexOf("|")+1,res.length());
            }
        	
        	//appendToLog(mobile+" "+mobileToSend+" "+amnt+" "+receipt+" "+accNo+" "+transRes+" New Transaction Request"+" "+now);
        	appendToLog(mobile+" "+counter_id+" "+amnt+" "+receipt+" "+accNo+" "+transRes+" New Transaction Request"+" "+now);
  
        	if (transRes ==0) {
         		System.out.println("Before Log SMS Tran");
         		cLog = con.prepareCall("call SMS_ONLINE_BILL_PAY.LOG_SMS_TRANSACTION(?,?,?,?,?,?,?)");
				cLog.setString(1,counter_id);
                cLog.setString(2,mobile);
                cLog.setDouble(3,(new Double(amnt)).doubleValue());
                cLog.setString(4,_txNumber);
                cLog.setString(5,receipt);
                cLog.setInt(6,accNo);
                cLog.setString(7,_pmode);
                    
				cLog.execute();
		        
				con.commit();
				cLog.close();
										
               
         	
        		if (isPre.equalsIgnoreCase("F")) {
        			if(mcFlag.equals("M")) {
            			SmsSend ss = new SmsSend(mobileToSend,"PAID "+mobile+" "+amnt+" "+receipt+ " "+accNo+"#ABANS");
        				ss.run();
        			} else if(mcFlag.equals("C")) {
        				SmsSend ss = new SmsSend(mobileToSend,"PAID "+mobile+" "+cntID+" "+amnt+" "+receipt+ " "+accNo+"#ABANS");
        				ss.run();
        			}
        		}
        		else if (isPre.equalsIgnoreCase("T")) {
        			if(mcFlag.equals("M")) {
            			SmsSend ss = new SmsSend(mobileToSend,"TOPUP "+mobile+" "+amnt+" "+receipt+ " "+accNo+"#ABANS");
        				ss.run();
        			} else if(mcFlag.equals("C")) {
        				SmsSend ss = new SmsSend(mobileToSend,"TOPUP "+mobile+" "+cntID+" "+amnt+" "+receipt+ " "+accNo+"#ABANS");
        				ss.run();
        			}
        		}
        	}
        	else if (transRes ==2) {
        		System.out.println("Transaction Result: " + transRes);
            	SmsSend ss = new SmsSend(mobileToSend,"INVALID ACCOUNT "+"#ABANS");
            	ss.run();
        	}
        	else if (transRes ==8) {
        		System.out.println("Transaction Result: " + transRes);
            	SmsSend ss = new SmsSend(mobileToSend,"INSUFFICIENT BALANCE "+"#ABANS");
            	ss.run();
        	}
        	else if (transRes ==10) {
        		System.out.println("Transaction Result: " + transRes);
            	SmsSend ss = new SmsSend(mobileToSend,"THRESHOLD PASSED "+"#ABANS");
            	ss.run();
        	}
        	else {
            	System.out.println("Transaction Result: " + transRes);
            	SmsSend ss = new SmsSend(mobileToSend,"GATEWAY FAILURE "+"#ABANS");
            	ss.run();
            }
        } catch (SQLException ex) {
			// Inform the Error
			SmsSend ss = new SmsSend(mobileToSend,"DBERR in updation "+"#ABANS");
			ss.run();
			System.err.println(ex);
			System.err.println("Error Code: "+ex.getErrorCode());
			System.err.println("SQL State: " +ex.getSQLState());
			logError(mobileToSend.substring(3),mobile,_txNumber,ex.toString());
			
		} catch (Exception e) {
			System.err.println(e);
			logError(mobileToSend.substring(3),mobile,_txNumber,e.toString());
			// Inform the Error
			SmsSend ss = new SmsSend(mobileToSend,"TRXFLD "+"#ABANS");
			ss.run();
			
		} finally {
			try {
				con.close();
			} catch (Exception e) {
				System.err.println("Inside Finally "+e);
				logError(mobileToSend.substring(3),mobile,_txNumber,e.toString());
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


