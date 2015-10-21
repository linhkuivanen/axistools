/**
 * 
 */
package at.telekom.util.axis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Security;
import java.util.Hashtable;

import com.sun.net.ssl.SSLContext;
import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.components.net.SunJSSESocketFactory;


/**
 * <p>
 * Secure Socket Factory
 * </p>
 * <p>
 * Capable of initializing secure socket with its own trust and key-store.
 * </p>
 * @author Richard Unger
 */
public class CustomSecureSocketFactory extends SunJSSESocketFactory {
	
	/**
	 * @param attributes
	 */
	public CustomSecureSocketFactory(Hashtable attributes) {
		super(attributes);
	}


    /**
     * Read the keystore, init the SSL socket factory
     *
     * @throws IOException
     */
    protected void initFactory() throws IOException {

        try {

            Security.addProvider(java.security.Security.getProvider("SUN"));
//            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

            //Configuration specified in wsdd.
            SSLContext context = getContext();
            sslFactory = context.getSocketFactory();
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e.getMessage());
        }
    }
	
	
	
	/**
	 * @see org.apache.axis.components.net.JSSESocketFactory#create(java.lang.String, int, java.lang.StringBuffer, org.apache.axis.components.net.BooleanHolder)
	 */
	@Override
	public Socket create(String arg0, int arg1, StringBuffer arg2, BooleanHolder arg3) throws Exception {
		//initFactory();
		Socket sock = super.create(arg0, arg1, arg2, arg3);
		String httpLogDir = (String) attributes.get("httplogdirectory");
		if (httpLogDir!=null){
			LoggingSocket logSock = new LoggingSocket(sock,httpLogDir);
			return logSock;
		}
		return sock;
	}






	/**
	 * @see org.apache.axis.components.net.SunJSSESocketFactory#getContext()
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected com.sun.net.ssl.SSLContext getContext() throws Exception {
        if(attributes == null) {
        	com.sun.net.ssl.SSLContext context = com.sun.net.ssl.SSLContext.getInstance("SSL");    // SSL
            // init context with the key managers
            context.init(null, null, null);
            return context;
        }
        // Please don't change the name of the attribute - other
        // software may depend on it ( j2ee for sure )
        String keystoreFile = (String) attributes.get("keystore");
        if (keystoreFile == null)
            keystoreFile = System.getProperty("user.home") + "/.keystore";
        String keystoreType = (String) attributes.get("keystoreType");
        if (keystoreType == null)
            keystoreType = "JKS";
        String keyPass = (String) attributes.get("keypass");
        if (keyPass == null)
            keyPass = "changeit";
        String keystorePass = (String) attributes.get("keystorePass");
        if (keystorePass == null)
            keystorePass = keyPass;
        String truststorePass = (String) attributes.get("truststorePass");
        if (truststorePass==null)
        	truststorePass = keyPass;
        String truststoreFile = (String) attributes.get("truststore");
        if (truststoreFile == null)
        	truststoreFile = System.getProperty("user.home") + "/.truststore";
        String truststoreType = (String) attributes.get("truststoreType");
        if (truststoreType==null)
        	truststoreType = "JKS";
        
        // protocol for the SSL ie - TLS, SSL v3 etc.
        String protocol = (String) attributes.get("protocol");
        if (protocol == null)
            protocol = "TLS";
        // Algorithm used to encode the certificate ie - SunX509
        String algorithm = (String) attributes.get("algorithm");
        if (algorithm == null)
            algorithm = "SunX509";
        
        // Create a KeyStore
        KeyStore kstore = initKeyStore(keystoreFile, keystorePass, keystoreType);
        com.sun.net.ssl.KeyManagerFactory kmf = com.sun.net.ssl.KeyManagerFactory.getInstance(algorithm);
        kmf.init(kstore, keyPass.toCharArray());
        
        // Set up TrustManager
        KeyStore tStore = initKeyStore(truststoreFile, truststorePass, truststoreType);
        com.sun.net.ssl.TrustManagerFactory tmf = com.sun.net.ssl.TrustManagerFactory.getInstance("SunX509");
        tmf.init(tStore);

        // Create a SSLContext ( to create the ssl factory )
        com.sun.net.ssl.SSLContext context = com.sun.net.ssl.SSLContext.getInstance(protocol);    // SSL

        // init context with the key managers
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new java.security.SecureRandom());
        return context;
	}

	
	


    /**
     * intializes a keystore.
     *
     * @param keystoreFile
     * @param keyPass
     *
     * @return keystore
     * @throws IOException
     */
    protected KeyStore initKeyStore(String keystoreFile, String keyPass,String type) throws IOException {
        try {
            KeyStore kstore = KeyStore.getInstance(type);
            InputStream istream = new FileInputStream(keystoreFile);
            kstore.load(istream, keyPass.toCharArray());
            return kstore;
        } catch (FileNotFoundException fnfe) {
            throw fnfe;
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException("Exception trying to load keystore " + keystoreFile + ": " + ex.getMessage());
        }
    }
	
	
}
