/**
 * 
 */
package at.telekom.util.axis;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;

/**
 * <p>
 * This class implements an Axis Handler which intercepts the SOAP messages sent/recieved
 * using Axis. The SOAP messages are logged into a configurable directory, using a timestamp as
 * the filename.
 * </p>
 * @author Richard Unger
 */
public class AxisDebugLogHandler extends org.apache.axis.handlers.BasicHandler implements Handler {

	/**
	 * request message
	 */
	String reqMessage = null;
	/**
	 * response message
	 */
	String respMessage = null;
	/**
	 * the basedir
	 */
	private File baseDir = null;
	/**
	 * Dateformate for filename
	 */
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");  
	
	
	/**
	 * Serial Version ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public AxisDebugLogHandler() {
		super();
	}

	
	
	/**
	 * @see org.apache.axis.handlers.LogHandler#invoke(org.apache.axis.MessageContext)
	 */
	public void invoke(MessageContext arg0) throws AxisFault {		
		Date now = new Date();
		Message m = arg0.getResponseMessage();
		if (m!=null){
			respMessage = m.getSOAPPartAsString();
			if (baseDir!=null)
				dumpToFile(sdf.format(now)+"_response",m.getSOAPPartAsBytes());
			return;
		}
		else
			respMessage = null;
		m = arg0.getRequestMessage();
		if (m!=null){			
			reqMessage = m.getSOAPPartAsString();
			if (baseDir!=null)
				dumpToFile(sdf.format(now)+"_request",m.getSOAPPartAsBytes());
		}
		else
			reqMessage = null;
	}

	
	
	
	
	
	/**
	 * @see org.apache.axis.handlers.BasicHandler#init()
	 */
	@Override
	public void init() {
		super.init();
	}



	/**
	 * @see org.apache.axis.handlers.BasicHandler#onFault(org.apache.axis.MessageContext)
	 */
	@Override
	public void onFault(MessageContext msgContext) {
		try {
			invoke(msgContext);
		} catch (AxisFault e) {
			System.err.println("Error while trying to log: "+e);
			e.printStackTrace();
		}
	}



	/**
	 * <p>
	 * write the file
	 * </p>
	 * @param fName
	 * @param partAsBytes
	 * @throws AxisFault
	 */
	private void dumpToFile(String fName, byte[] partAsBytes) throws AxisFault {
		if (baseDir == null)
			throw new IllegalArgumentException("Basedir not set in AxisDebugLogHandler!");
		File f = new File(baseDir,fName);
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
			bos.write(partAsBytes);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			throw new AxisFault("Problem writing debug file.",e);
		}
	}
	
	
	
	
	

	/**
	 * <p>
	 * Get a copy of the request message
	 * </p>
	 * @return the message
	 */
	public String getReqMessage() {
		return reqMessage;
	}

	/**
	 * <p>
	 * Get a copy of the response message
	 * </p>
	 * @return the message
	 */
	public String getRespMessage() {
		return respMessage;
	}

	/**
	 * <p>
	 * Get the directory where log files are written
	 * </p>
	 * @return the directory
	 */
	public File getBaseDir() {
		return baseDir;
	}


	/**
	 * <p>
	 * Set the directory for logfiles
	 * </p>
	 * @param baseDir tbe directory
	 */
	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}


	
}
