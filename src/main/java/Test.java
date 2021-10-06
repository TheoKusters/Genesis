
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


import web.Genesis;
import web.StatusWorker;

public class Test {

	public static void main(String[] args)  {
		StatusWorker rf = new StatusWorker();
		rf.writePFNGE( 
				"TEST", 
				"HIHIHIHI", 
				200,
				"This is a test",  
				new Timestamp((new Date()).getTime()),  
				new Timestamp((new Date()).getTime()+1234), 
				"PDS".getBytes());
		rf.updatePFNGE(5, 
				"TEST", 
				"HIHIHIHI", 
				200,
				"This is a test",  
				new Timestamp((new Date()).getTime()),  
				new Timestamp((new Date()).getTime()+1234), 
				"PDS".getBytes());
	}

	public static void main1(String[] args) throws InterruptedException {

		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext("web");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		Genesis genesis = context.getBean(Genesis.class);
		genesis.setTrustManager();

		String proxyUrl, proxyPort;
		// System.out.println("about to run");
		if (args[0].equals("proxy")) {
			proxyUrl = "b2b-http.dhl.com           ";
			proxyPort = "8080          ";
		} else {
			proxyUrl = "";
			proxyPort = "";
		}

		
		
		for (int i = 1; i <= 19; i++) {

			genesis.addConnection();
			genesis.post("https://pds-accept.dhlparcel.nl/genesis".getBytes(),
					"{\"batchId\":\"EI160113-1-1\",\"timestamp\":\"2016-01-13T17:56:22.772Z\",\"records\":[{\"record\":\"HJ\",\"process\":\"C\",\"version\":9,\"data\":{\"HJNZND\":19458211,\"HJEVRZ\":20,\"HJJVRZ\":16,\"HJMVRZ\":1,\"HJKREF\":\"АБВГДЕЖЗAAO\",\"HJOREF\":\"839999963805143\"}}]}"
							.getBytes(),
					"Authorization: 88aad782327d478fa8710c5d84ea342a  ".getBytes(), "NEX102".getBytes(),
					proxyUrl.getBytes(), proxyPort.getBytes(), 
					"PDS".getBytes(),
					0);
			System.out.println(i + "  "  + genesis.getNrThreads() + "  " + genesis.availableThreads() +  "    "  + dateFormat.format(new Date()));
			genesis.waitAvailable();

		}
		
		
		System.out.println(dateFormat.format(new Date()));
		context.stop();
		context.close();
		System.out.println(dateFormat.format(new Date()));
		}

}
