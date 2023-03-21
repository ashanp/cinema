package org.dialog.dops;


public class SmsSend implements Runnable
{
	private String mobileNo;
	private String message;
    
    public SmsSend(String mNo,String msg) {
    	this.mobileNo=mNo;
    	this.message=msg;
    }
    
    public void run() {
    	//ss = new javax.dialog.smppgateway.SmsListener();
		//org.jboss.remoting.samples.simple.MessageBeanClient t = new org.jboss.remoting.samples.simple.MessageBeanClient();
    	org.dialog.dops.MessageBeanClient t = new org.dialog.dops.MessageBeanClient();
		System.out.println("Ashan--------- "+mobileNo+"|"+message);
    	t.send(mobileNo+"|"+message);
		System.gc();
		
    }

}
