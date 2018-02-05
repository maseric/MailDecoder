package gfi.messageriedemerde.handlers;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.OutputStream;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

public class MimeMultipartAlternativeHandler implements DataContentHandler {

	ActivationDataFlavor mimeAltDataFlavor = new ActivationDataFlavor(javax.mail.internet.MimeMultipart.class, "multipart/alternative", "MIME aternative");

	@Override
	public Object getContent(DataSource ds) throws IOException {
		try {
			return new MimeMultipart(ds);
		} catch (MessagingException e) {
			IOException ioex = new IOException("Exception while constructing MimeMultipart");
			ioex.initCause(e);
			throw ioex;
		}
	}

	@Override
	public Object getTransferData(DataFlavor df, DataSource ds) throws UnsupportedFlavorException, IOException {
		// make sure the datasource is of the type edifactDataFlavor
		if (mimeAltDataFlavor.equals(df)) {
			return getContent(ds);
		} else {
			return null;
		}
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { mimeAltDataFlavor };
	}

	@Override
	public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
		if (obj instanceof MimeMultipart) {
			try {
				((MimeMultipart) obj).writeTo(os);
			} catch (MessagingException e) {
				throw new IOException(e.toString());
			}
		}
	}

}
