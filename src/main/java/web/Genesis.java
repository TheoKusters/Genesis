package web;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.sql.Timestamp;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Genesis {
	
	private int maxConnections;
	volatile private int connectionsActive = 0;
	
	StatusWorker rf;
	public Genesis() {
		rf = new StatusWorker();
		maxConnections = Integer.parseInt(System.getProperty("http.maxConnections", "5"));
	}


	@Async
	public <T> void post(
			byte[] url, 
			byte[] body, 
			byte[] auth,
			byte[] program, 
			byte[] proxyUrl, 
			byte[] proxyPort,
			byte[] message,
			int rrn)  {

		
		String urlStr = new String(url).trim();
		String bodyStr = new String(body).trim();
		String authStr = new String(auth).trim();
		String programStr = new String(program);
		String proxyUrlStr = new String(proxyUrl).trim();
		String proxyPortStr = new String(proxyPort).trim();
		
		
		String httpStatusText = "";
		int httpStatus = 0;
		Date date= new Date();
		Timestamp httpStart = new Timestamp(date.getTime());
		
	    RestTemplate restTemplate;
		if (proxyUrlStr.equals("") ) {
		    restTemplate = new RestTemplate();
		} else { 
		    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		    Proxy proxy= new Proxy(Type.HTTP, new InetSocketAddress(proxyUrlStr, Integer.parseInt(proxyPortStr)));
		    requestFactory.setProxy(proxy);
		    restTemplate = new RestTemplate(requestFactory);
		}
		
	//	restTemplate.getInterceptors().add(new GZipInterceptor());
		restTemplate.getInterceptors().add(new ContentTypeInterceptor());
		restTemplate.getInterceptors().add(new AuthorizationInterceptor(authStr));
	
		// Make the HTTP POST request, marshaling the request to JSON, and the response to a String
		try {
			ResponseEntity<String> entity = restTemplate.postForEntity(urlStr, bodyStr.getBytes("UTF-8"), String.class);	

			httpStatusText = entity.getStatusCode().getReasonPhrase();
			httpStatus = entity.getStatusCode().value();
			
			if (entity.getStatusCode()==HttpStatus.NO_CONTENT) {
				httpStatusText = "";
				httpStatus = 0;
			}
				
		} catch (Exception e){
			httpStatus = -1;
			httpStatusText = e.getMessage(); //.substring(e.getMessage().length()-50);
			
		} finally {

//			System.out.println(httpStatusText + " (" + httpStatus + ")");
			date= new Date();
			Timestamp httpEnd = new Timestamp(date.getTime());

			if (httpStatus != 0 && rrn == 0) {
				rf.writePFNGE(programStr, bodyStr, httpStatus, httpStatusText, httpStart, httpEnd, message);
			} 
			if (httpStatus == 0 && rrn !=0 ) {
				rf.updatePFNGE(rrn, programStr, bodyStr, httpStatus, httpStatusText, httpStart, httpEnd, message);
			}
			
		}
		
		rmvConnection();
	}
	
	
	
	
	public void setTrustManager() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Activate the new trust manager
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
            	e.printStackTrace();
            }
	}

	public boolean availableThreads() {
		if (connectionsActive < maxConnections) 
			return true;
		else 
			return false;
	}
	
	public void waitAvailable() {
		while (!availableThreads()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	
	public int getNrThreads() {
		return connectionsActive;
	}
	
	synchronized public void addConnection() {
		connectionsActive += 1;
	}
	synchronized private void rmvConnection() {
		connectionsActive -= 1;
	}
}
