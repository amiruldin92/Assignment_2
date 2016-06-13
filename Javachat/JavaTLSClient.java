package javatlsclient;

import java.io.*;
import java.net.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class JavaTLSClient {


    public static void main(String[] args) {
        // TODO code application logic here
        try{
            Socket client = new Socket("127.0.0.1", 9999);

            SSLSocketFactory sf = ((SSLSocketFactory) SSLSocketFactory.getDefault());
            InetSocketAddress remoteAddress = (InetSocketAddress) client.getRemoteSocketAddress();
            SSLSocket s = (SSLSocket) (sf.createSocket(client, remoteAddress.getHostName(), client.getPort(), true));

            s.setUseClientMode(true);

            s.setEnabledProtocols(s.getSupportedProtocols());
            s.setEnabledCipherSuites(s.getSupportedCipherSuites());

            s.startHandshake();

            client = s;

            BufferedReader r = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter w = new PrintWriter(client.getOutputStream(), true);
            BufferedReader con = new BufferedReader(new InputStreamReader(System.in));
            String line;
            do{
		line = r.readLine();
		if ( line != null )
                    System.out.println(line);
		line = con.readLine();
		w.println(line);
            }while ( !line.trim().equals("bye") );
	}catch (Exception err){
            System.err.println(err);
	}
    }

}
