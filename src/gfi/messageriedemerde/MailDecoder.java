package gfi.messageriedemerde;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.CommandMap;
import javax.activation.DataContentHandler;
import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.util.SharedByteArrayInputStream;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.util.CompatibilityHints;

import com.sun.mail.handlers.multipart_mixed;

/**
 * @author Administrateur
 * 
 */
public class MailDecoder extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8970202602256698685L;
	private JButton boutonConvert;
	private static final String bountonConvertString = "Décoder email";
	private JEditorPane outputPane;
	private static JTextArea inPutTextArea;
	private static JPanel attachmentPane;
	private static JFrame frame;

	private static GridBagLayout gbl = new GridBagLayout();
	private static GridBagConstraints c = new GridBagConstraints();
	private static ImageIcon calendarIcon = createImageIcon("/images/cal.gif", "Invit Outlook");
	private ImageIcon fileIcon = createImageIcon("/images/file.gif", "Fichier joint");

	private static final String CALSUFIX = ".ics";
	private static final String CALPREFIX = "Calendar";
	private static final String ACTION_SEP = "open@@@tototutu@@@";
	private int nbCalendars = 0;

	/**
	 * 
	 */
	public MailDecoder() {
		setLayout(new BorderLayout());

		// Create a text area.
		inPutTextArea = new JTextArea("Coller le texte de l'email encodé ici");
		inPutTextArea.selectAll();
		inPutTextArea.setSelectedTextColor(Color.GREEN);
		inPutTextArea.setFont(new Font("Courier", Font.PLAIN, 12));
		inPutTextArea.setLineWrap(true);
		inPutTextArea.setWrapStyleWord(true);
		JScrollPane inputPane = new JScrollPane(inPutTextArea);
		inputPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		inputPane.setPreferredSize(new Dimension(250, 250));
		// inputPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Message initial encodé"),
		// BorderFactory.createEmptyBorder(5, 5, 5, 5)),
		// inputPane.getBorder()));

		// Create an editor pane.

		// GridBagLayout gbl = new GridBagLayout();

		JButton fileBut = new JButton(fileIcon);
		fileBut.setMargin(new Insets(0, 0, 0, 0));

		fileBut.setVisible(false);

		outputPane = new JEditorPane();

		outputPane.setPreferredSize(new Dimension(500, 250));
		JScrollPane editorScrollPane = new JScrollPane(outputPane);
		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(500, 145));
		editorScrollPane.setMinimumSize(new Dimension(250, 205));

		// Create a button
		boutonConvert = new JButton(bountonConvertString);
		boutonConvert.setActionCommand(bountonConvertString);
		boutonConvert.addActionListener(this);

		// Put the editor pane and the text pane in a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPane, editorScrollPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.5);
		JPanel rightPane = new JPanel(new GridLayout(1, 0));
		rightPane.add(splitPane);

		JPanel convertButtonPane = new JPanel();
		convertButtonPane.add(boutonConvert);

		// Put everything together.

		c.gridwidth = GridBagConstraints.REMAINDER; // last
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;

		attachmentPane = new JPanel(gbl);

		attachmentPane.add(boutonConvert, c);

		attachmentPane.add(fileBut, c);

		// leftPane.add(inputPane, BorderLayout.CENTER);
		// rightPane.add(boutonConvert, BorderLayout.SOUTH);
		rightPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Décodage de mail"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		// add(leftPane, BorderLayout.LINE_START);
		add(rightPane, BorderLayout.WEST);
		// add(boutonConvert, BorderLayout.PAGE_END);
		// add(convertButtonPane,BorderLayout.SOUTH);
		add(attachmentPane, BorderLayout.SOUTH);

	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = MailDecoder.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	public void actionPerformed(ActionEvent e) {

		if (bountonConvertString.equals(e.getActionCommand())) {
			// action conversion message mail

			InputStream is = new ByteArrayInputStream(inPutTextArea.getText().getBytes());

			MimeMessage mm;
			try {
				mm = new MimeMessage(null, is);

				String prefix = "";

				MailcapCommandMap mc = new MailcapCommandMap();

				mc.addMailcap("text/plain; ; x-java-content-handler=com.sun.mail.handlers.text_plain");
				mc.addMailcap("text/html; ; x-java-content-handler=com.sun.mail.handlers.text_html");
				mc.addMailcap("text/xml; ; x-java-content-handler=com.sun.mail.handlers.text_xml");
				//mc.addMailcap("text/*; ; x-java-content-handler=com.sun.mail.handlers.text_html");
				mc.addMailcap("multipart/*; ; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
				// mc.addMailcap("multipart/alternative; ; x-java-content-handler=gfi.messageriedemerde.handlers.MimeMultipartAlternativeHandler");
				mc.addMailcap("message/rfc822; ; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
				mc.addMailcap("image/gif; ; x-java-content-handler=com.sun.mail.handlers.image_gif");
				mc.addMailcap("image/jpeg; ; x-java-content-handler=com.sun.mail.handlers.image_jpeg");

				CommandMap.setDefaultCommandMap(mc);

				System.out.println(CommandMap.getDefaultCommandMap().getCommand("multipart/alternative", "content-handler").getCommandClass());

				for (String mimeType : mc.getMimeTypes()) {
					System.out.println(mimeType);
					DataContentHandler dch = mc.createDataContentHandler(mimeType);
					try {
						System.out.println("content-handler : " + dch.getClass().getName());
					} catch (Exception e4) {
						System.out.println(e4.getMessage());
						// e.printStackTrace();
					}
				}

				dumpPart(prefix, mm);

			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			if (outputPane.getText().length() == 0) {
				outputPane.setText("Données incorrectes : rien n'a été décodé");
			} else {
				hideConvertButton();
			}

		} else if (e.getActionCommand().matches("^" + ACTION_SEP + ".*")) {

			String fileName = e.getActionCommand().replaceAll(ACTION_SEP, "");
			try {
				System.out.println("Ouverture demandée du fichier : " + fileName);

				Desktop.getDesktop().open(new File(fileName));
			} catch (Exception e2) {
				System.err.println("Woooo ! J'arrive pas à trouver ton fichier");
			}

		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event dispatch thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		frame = new JFrame("MailDecoder");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add content to the window.
		frame.add(new MailDecoder());

		// Display the window.
		frame.pack();
		frame.setVisible(true);

		inPutTextArea.grabFocus();

	}

	public static void main(String[] args) {

		// ClassLoader.getSystemClassLoader().getResource("");

		// Schedule a job for the event dispatching thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);

				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				createAndShowGUI();
			}
		});
	}

	protected void dumpPart(String prefix, Part p) throws ParserException, ValidationException {
		try {

			System.out.println(prefix + "----------------");
			System.out.println(prefix + "Content-Type: " + p.getContentType());
			System.out.println(prefix + "Class: " + p.getClass().toString());

			String thisPartsFileName = p.getFileName();

			Object o = p.getContent();

			// System.out.println(p.getContentType());

			if (p.isMimeType("text/plain")||p.isMimeType("text/html")) {
				// System.out.println(p.getContent());
				String text=outputPane.getText();
				
				text=text+"\n__________________________________________________\n"+(String) p.getContent();
				outputPane.setText(text);
			}

			if (p.isMimeType("text/calendar")) {

				CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
				CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);

				//TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();

				CalendarBuilder calBuilder = new CalendarBuilder();

				Calendar calendar = calBuilder.build(p.getInputStream());

				nbCalendars++;
				String calName = getNewCalFileName();
				FileOutputStream fout = new FileOutputStream(calName);

				CalendarOutputter outputter = new CalendarOutputter(false);
				outputter.output(calendar, fout);

				addCalendarButton(calName);
				frame.pack();

			} else if (thisPartsFileName != null && thisPartsFileName.length() > 0) {
				System.out.println(prefix + "Attachment found : " + thisPartsFileName);

				MimeBodyPart mp = (MimeBodyPart) p;
				mp.saveFile(thisPartsFileName);
				addFileButton(thisPartsFileName);
				frame.pack();
			}

			if (o == null) {
				System.out.println(prefix + "Content:  is null");
			} else {
				System.out.println(prefix + "Content: " + o.getClass().toString());
			}

			if (o instanceof Multipart) {
				String newpref = prefix + "\t";
				Multipart mp = (Multipart) o;
				int count = mp.getCount();
				for (int i = 0; i < count; i++) {
					dumpPart(newpref, mp.getBodyPart(i));
				}
			}

			if (p.isMimeType("multipart/alternative") && o instanceof SharedByteArrayInputStream) {

				DataHandler d = p.getDataHandler();
				System.out.println(ClassLoader.getSystemClassLoader().getClass().getName());
				multipart_mixed mm = new multipart_mixed();

				System.out.println(d.getClass().getName());
				System.out.println(d.getName());
				String newpref = prefix + "\t";
				SharedByteArrayInputStream s = (SharedByteArrayInputStream) o;

				// MimeMessage mm = new MimeMessage(null, s);

				// com.sun.mail.handlers.multipart_mixed m_m=(multipart_mixed)
				// mm.getDataHandler();
				/*
				 * int count = mp.getCount(); for (int i = 0; i < count; i++) {
				 * dumpPart(newpref, mp.getBodyPart(i)); }
				 */
			}

		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException ioex) {
			System.out.println("Cannot get content" + ioex.getMessage());
		}
	}

	protected void addCalendarButton(String calName) {

		/**
		 * boutton pour une invit outlook => obligé de l'appeler calbut ^^
		 */
		JButton calBut = new JButton();

		if (calendarIcon != null) {
			calBut.setIcon(calendarIcon);
		} else {
			calBut.setText("Invit. réunion");
		}
		calBut.setMargin(new Insets(0, 0, 0, 0));
		calBut.setVisible(true);
		calBut.setActionCommand(ACTION_SEP + calName);
		calBut.addActionListener(this);
		addLabelButton(calName, calBut, attachmentPane);

	}

	protected void addFileButton(String fileName) {

		// boutton pour une pj

		JButton fileBut = new JButton();

		if (fileIcon != null) {
			fileBut.setIcon(fileIcon);
		} else {
			fileBut.setText("PJ");
		}
		fileBut.setMargin(new Insets(0, 0, 0, 0));
		fileBut.setVisible(true);
		fileBut.setActionCommand(ACTION_SEP + fileName);
		fileBut.addActionListener(this);
		addLabelButton(fileName, fileBut, attachmentPane);
		// attachmentPane.add(fileBut, c);

	}

	private String getNewCalFileName() {
		String ret = CALPREFIX;

		if (nbCalendars > 1) {
			ret += "_" + (nbCalendars - 1);
		}

		ret += CALSUFIX;
		return ret;
	}

	private void addLabelButton(String intitule, JButton bouton, Container container) {
		JLabel label = new JLabel(intitule);
		label.setLabelFor(bouton);
		container.add(bouton, c);
		container.add(label, c);

	}

	private void hideConvertButton() {
		attachmentPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("PJ trouvées dans le mail"), BorderFactory.createEmptyBorder(5, 5, 5, 5)), attachmentPane.getBorder()));
		boutonConvert.setVisible(false);
	}
}
