package javatlsserver;

import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class JavaTLSServer {

    public JavaTLSServer(int portnum) throws IOException{
            InetSocketAddress bindAddress = new InetSocketAddress(portnum);
            server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(bindAddress);
            System.out.println("Server Started!");
    }


    public void serve() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException{
            while (true){
		Socket client = server.accept();


                SSLSocketFactory sf = ((SSLSocketFactory) SSLSocketFactory.getDefault());

                //SSLSocketFactory sf = sslContext.getSocketFactory();

                InetSocketAddress remoteAddress = (InetSocketAddress) client.getRemoteSocketAddress();
                SSLSocket s = (SSLSocket) (sf.createSocket(client, remoteAddress.getHostName(), client.getPort(), true));

                s.setUseClientMode(false);

                s.setEnabledProtocols(s.getSupportedProtocols());
                s.setEnabledCipherSuites(s.getSupportedCipherSuites());

                //s.setNeedClientAuth(true);

                s.startHandshake();

                client = s;

                BufferedReader r = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter w = new PrintWriter(client.getOutputStream(), true);
                w.println("Hai. Type 'Sayonara' to close.");
                String line;

                do {
                    line = r.readLine();
                    if ( line != null )
                        w.println("Server Echo User Input: "+ line);
                }while ( !line.trim().equals("Sayonara") );

                client.close();

            }

    }

    public static void main(String[] args) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        // TODO code application logic here
        JavaTLSServer s = new JavaTLSServer(9999);
	s.serve();
    }
    private ServerSocket server;
}
