package web;

import java.math.*;
import java.sql.Timestamp;

import com.ibm.as400.access.*;

public class StatusWorker {

	SequentialFile pfnGE;
	AS400 as400;
	Record pfnger;
	int errors;
	
	
	
	public StatusWorker() {
		as400 = new AS400();
		QSYSObjectPathName fileName = new QSYSObjectPathName("*LIBL", "PFNGE", "FILE");
		pfnGE = new SequentialFile(as400, fileName.getPath());
		try {
			pfnGE.setRecordFormat();
			pfnGE.open(AS400File.READ_WRITE, 1, AS400File.COMMIT_LOCK_LEVEL_NONE);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pfnger = new Record(pfnGE.getRecordFormat());

	}


	public synchronized void writePFNGE(String program, String body, int httpStatus,
			String httpStatusText, Timestamp httpStart, Timestamp httpEnd, byte[] wwPsjNam) {

		pfnger.setField("GEOPGM", program);
		pfnger.setField("GEARCL", new BigDecimal(body.length()));
		
		pfnger.setField("GETIMH", new BigDecimal(httpEnd.getTime() - httpStart.getTime()));
		pfnger.setField("GERTNC", new BigDecimal(httpStatus));
		if (httpStatusText.length()>50) {
			httpStatusText = httpStatusText.substring(0, 50);
		}
		pfnger.setField("GERTNT", new String(httpStatusText));
		pfnger.setField("GETSRE", httpStart.toString().replaceAll(" ", "-").replaceAll(":", "."));
		pfnger.setField("GETSPR", "0001-01-01-00.00.00.000000");
		pfnger.setField("GEDATA", body);
		try {
			pfnGE.write(pfnger);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String messageData = String.format("%250s%3s%100s%-10s", 
											"",
											new String(wwPsjNam),
											String.format("%-100s", httpStatusText).substring(0, 100),
											httpStatus);
		if (httpStatus != 0) {
			errors += 1;
			if (errors == 1) {
				MessageQueue queue = new MessageQueue(as400, "/qsys.lib/qsysopr.msgq");
				try {
					queue.sendInformational("ERR7056", new QSYSObjectPathName("*LIBL", "NMSGF", "MSGF").getPath(),
							messageData.getBytes("Cp037"));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	public synchronized void updatePFNGE(int rrn, String program, String body, int httpStatus,
			String httpStatusText, Timestamp httpStart, Timestamp httpEnd, byte[] wwPsjNam) {

		pfnger.setField("GEOPGM", program);
		pfnger.setField("GEARCL", new BigDecimal(body.length()));
		
		pfnger.setField("GETIMH", new BigDecimal(httpEnd.getTime() - httpStart.getTime()));
		pfnger.setField("GERTNC", new BigDecimal(httpStatus));
		if (httpStatusText.length()>50) {
			httpStatusText = httpStatusText.substring(0, 50);
		}
		pfnger.setField("GERTNT", new String(httpStatusText));
		pfnger.setField("GETSRE", httpStart.toString().replaceAll(" ", "-").replaceAll(":", "."));
		pfnger.setField("GETSPR", httpEnd.toString().replaceAll(" ", "-").replaceAll(":", "."));
		pfnger.setField("GEDATA", body);
		try {
			pfnGE.update(rrn, pfnger);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}