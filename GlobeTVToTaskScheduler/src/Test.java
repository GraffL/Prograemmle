import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;



public class Test {

	
	public static int AufnahmenZahl = 0;
	
	public static void main(String[] args) throws JDOMException, IOException {
		
		final Document Settings = new SAXBuilder().build("Settings.xml");

	    final JFrame Fenster = new JFrame( "Fenstername" );
	    Fenster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    Fenster.setSize( 300, 150 );
	    Fenster.setLocation(100,150);
	    
	    Fenster.setLayout(new BorderLayout());

	    ScrollPane scrollPanel = new ScrollPane(1);
	    Fenster.add(scrollPanel, BorderLayout.CENTER);
	    Panel body = new Panel();
	    body.setLayout(new GridLayout(0,10,2,2));
	    scrollPanel.add(body);
    	    
	    
	    JLabel Titel = new JLabel("Titel");
	    Titel.setPreferredSize(new Dimension(200,20));
	    JLabel Datum = new JLabel("Datum");
	    Datum.setPreferredSize(new Dimension(70,20));
	    JLabel Start = new JLabel("Start");
	    Start.setPreferredSize(new Dimension(50,20));
	    JLabel Ende = new JLabel("Ende");
	    Ende.setPreferredSize(new Dimension(50,20));
	    JLabel Aufnehmen = new JLabel("Aufnehmen?");
	    Aufnehmen.setPreferredSize(new Dimension(50,20));
	    JLabel Hochfahren = new JLabel("Hochfahren");
	    Hochfahren.setPreferredSize(new Dimension(50,20));
	    JLabel GTVstarten = new JLabel("GTV starten");
	    GTVstarten.setPreferredSize(new Dimension(50,20));
	    JLabel GTVbeenden = new JLabel("GTV beenden");
	    GTVbeenden.setPreferredSize(new Dimension(50,20));
	    JLabel Ruhezustand = new JLabel("Ruhezustand");
	    Ruhezustand.setPreferredSize(new Dimension(50,20));
	    JLabel Herunterfahren = new JLabel("Herunterfahren");
	    Herunterfahren.setPreferredSize(new Dimension(50,20));
	    
	    
	    
	    body.add(Titel);
	    body.add(Datum);
	    body.add(Start);
	    body.add(Ende);
	    body.add(Aufnehmen);
	    body.add(Hochfahren);
	    body.add(GTVstarten);
	    body.add(GTVbeenden);
	    body.add(Ruhezustand);
	    body.add(Herunterfahren);
        
	    Fenster.setVisible(true);
	
	    
	    String path = Settings.getRootElement().getChild("paths").getChild("GlobeTV").getValue();
	    GlobeTVDaten Daten = new GlobeTVDaten(path);
	    

		if(Daten.DokumentGesetzt()) {
			
			AufnahmenZahl = Daten.AufnahmenZahl;
			final CAufnahme[] Aufnahme = new CAufnahme[AufnahmenZahl];

			for(int i=0;i<AufnahmenZahl;i++) {
				Aufnahme[i] = new CAufnahme(body, Daten, i);
			}
					
			JPanel Schlusszeile = new JPanel(new FlowLayout(FlowLayout.CENTER));
			Fenster.add(Schlusszeile, BorderLayout.SOUTH);
			final JButton OK = new JButton("OK");
			Schlusszeile.add(OK);
			
			ActionListener OKbestaetigt = new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					JDialog Fortschritt = new JDialog();
					Fortschritt.setLayout(new GridLayout(3, 1));
					Fortschritt.setAlwaysOnTop(true);
					Fortschritt.setVisible(true);
					
					JProgressBar FortschrittAnzeige = new JProgressBar();
					FortschrittAnzeige.setPreferredSize(new Dimension(200, 30));
					Fortschritt.add(FortschrittAnzeige);
					FortschrittAnzeige.setStringPainted(true);
					
					Fortschritt.pack();
					
					Fortschritt.setLocation(
						Fenster.getLocation().x+Fenster.getSize().width/2-Fortschritt.getSize().width/2, 
						Fenster.getLocation().y+Fenster.getSize().height/2-Fortschritt.getSize().height/2
					);	
					
					Document PCaufwecken = null;
					Document GlobeTVstarten = null;
					Document GlobeTVbeenden = null;
					Document PCRuhezustand = null;
					Document PCherunterfahren = null;
					Namespace ns = null;
					
					try {
						PCaufwecken = new SAXBuilder().build("(Vorlage) PC aufwecken");
						ns = PCaufwecken.getRootElement().getNamespace();						

						GlobeTVstarten = new SAXBuilder().build("(Vorlage) Globe TV starten");
						GlobeTVbeenden = new SAXBuilder().build("(Vorlage) Globe TV beenden");
						PCRuhezustand = new SAXBuilder().build("(Vorlage) PC in Ruhezustand versetzen");
						PCherunterfahren = new SAXBuilder().build("(Vorlage) PC herunterfahren");
					} catch (JDOMException e1) {} 
					catch (IOException e1) {} 
					
					PCaufwecken.getRootElement().getChild("Triggers", ns).removeContent();
					GlobeTVstarten.getRootElement().getChild("Triggers", ns).removeContent();
					GlobeTVbeenden.getRootElement().getChild("Triggers", ns).removeContent();
					PCRuhezustand.getRootElement().getChild("Triggers", ns).removeContent();
					PCherunterfahren.getRootElement().getChild("Triggers", ns).removeContent();

					int FortschrittProzent = 10;
					FortschrittAnzeige.setValue(FortschrittProzent);
																
					for(int i=0;i<AufnahmenZahl;i++) {
						if(Aufnahme[i].Aufnehmen.isSelected()) {
							boolean Erfolg = Aufnahme[i].DatenSchreiben(PCaufwecken, GlobeTVstarten, GlobeTVbeenden, PCRuhezustand, PCherunterfahren, ns);
						}
						FortschrittProzent += (i+1) * (80 / AufnahmenZahl);
						FortschrittAnzeige.setValue(FortschrittProzent);
					}
					
					try {
						String taskPath = Settings.getRootElement().getChild("paths").getChild("Tasks").getValue();
						
						XMLDateiSchreiben(PCaufwecken, taskPath + "PC aufwecken.xml");
						XMLDateiSchreiben(GlobeTVstarten, taskPath + "Globe TV starten.xml");
						XMLDateiSchreiben(GlobeTVbeenden, taskPath + "Globe TV beenden.xml");
						XMLDateiSchreiben(PCherunterfahren, taskPath + "PC herunterfahren.xml");
						XMLDateiSchreiben(PCRuhezustand, taskPath + "PC in Ruhezustand versetzen.xml");
			
						File Datei = new File("Aufgaben importieren.bat");
						Desktop.getDesktop().open(Datei);
					} catch (JDOMException e1) {} catch (IOException e1) {} 
					
					
					FortschrittProzent = 100;
					FortschrittAnzeige.setValue(FortschrittProzent);	
					
					JLabel Fertig = new JLabel("Programmierung erfolgreich");
					JButton Beenden = new JButton("Beenden");
					Fortschritt.add(Fertig);
					Fortschritt.add(Beenden);
					
					Fortschritt.pack();
					
					ActionListener ButtonBeenden = new ActionListener() {
						@Override public void actionPerformed(ActionEvent e) {
							System.exit(0);							
						}
					};
					Beenden.addActionListener(ButtonBeenden);
					

				}
			};
			
			OK.addActionListener(OKbestaetigt);		
			
		};
		
		
		Fenster.pack();
		
	}


	static class GlobeTVDaten {
		String DokumentPfad;
		Document Dokument;
		Element scheduler_list;
		boolean DokumentGesetzt;
		List<?> AufnahmeInformationen;
		Element AufnahmeItem;
		public int AufnahmenZahl;
		public CAufnahmen[] Aufnahmen;
		
		GlobeTVDaten (String AufzeichnungsDaten) throws JDOMException, IOException {
			DokumentGesetzt = false;
			DokumentGesetzt = DokumentSetzen(AufzeichnungsDaten); 
		}
		
		boolean DokumentSetzen (String AufzeichnungsDaten) throws JDOMException, IOException {
			DokumentPfad = AufzeichnungsDaten;
			Dokument = new SAXBuilder().build(AufzeichnungsDaten);	
			scheduler_list = Dokument.getRootElement();
			if(scheduler_list.getChild("items") != null) {
				AufnahmeInformationen = scheduler_list.getChild("items").getChildren();
				AufnahmenZahl = AufnahmeInformationen.size();
				Aufnahmen = new CAufnahmen[AufnahmenZahl];
				
				for(int i=0;i<AufnahmenZahl;i++) {
					AufnahmeItem = scheduler_list.getChild("items").getChild("item");
					
					Aufnahmen[i] = new CAufnahmen();
					Aufnahmen[i].Titel = AufnahmeItem.getChild("epg").getAttributeValue("text");
					
					String StartZeit = AufnahmeItem.getChild("time").getAttributeValue("start-time");
					int Stunde = Integer.parseInt(StartZeit.substring(0, 2));
					int Minute = Integer.parseInt(StartZeit.substring(3, 5));
					int Sekunde = Integer.parseInt(StartZeit.substring(6, 8));
					String StartDatum = AufnahmeItem.getChild("time").getAttributeValue("start-date");
					int Jahr = Integer.parseInt(StartDatum.substring(6, 10));
					int Monat = Integer.parseInt(StartDatum.substring(3, 5));
					int Tag = Integer.parseInt(StartDatum.substring(0, 2));
					Aufnahmen[i].Start = new GregorianCalendar(Jahr, Monat-1, Tag , Stunde, Minute, Sekunde);
					
					String EndZeit = AufnahmeItem.getChild("time").getAttributeValue("end-time");
					Stunde = Integer.parseInt(EndZeit.substring(0, 2));
					Minute = Integer.parseInt(EndZeit.substring(3, 5));
					Sekunde = Integer.parseInt(EndZeit.substring(6, 8));
					String EndDatum = AufnahmeItem.getChild("time").getAttributeValue("end-date");
					Jahr = Integer.parseInt(EndDatum.substring(6, 10));
					Monat = Integer.parseInt(EndDatum.substring(3, 5));
					Tag = Integer.parseInt(EndDatum.substring(0, 2));				
					Aufnahmen[i].Ende = new GregorianCalendar(Jahr, Monat-1, Tag , Stunde, Minute, Sekunde);
									
					scheduler_list.getChild("items").removeChild("item");
				}
				
				scheduler_list = Dokument.getRootElement();
				return true;
			} else  {
				return false;
			}
			
		}
		
		boolean DokumentGesetzt () {
			return DokumentGesetzt;
		}
		
		class CAufnahmen {
			public String Titel;
			Calendar Start;
			Calendar Ende;
			
			String StartZeitAlsString (String Trenner) {
				return ZeitalsString(Start, Trenner);
			}
			
			String StartDatumAlsString (String Trenner) {
				return DatumalsString(Start, Trenner);
			}
			
			String EndZeitAlsString (String Trenner) {
				return ZeitalsString(Ende, Trenner);
			}
			
			String EndDatumAlsString (String Trenner) {
				return DatumalsString(Ende, Trenner);
			}
			
			String ZeitPunktAlsString (int StartEnde, String TrennerDatum, String TrennerMitte, String TrennerZeit, int Veraenderung) {
				String ZeitPunktString = "";
				Calendar ZeitPunkt = new GregorianCalendar();
				if(StartEnde==1) {
					ZeitPunkt.setTime(Start.getTime());
				} else if(StartEnde==2) {
					ZeitPunkt.setTime(Ende.getTime());
				} else {
					return null;
				}
				
				ZeitPunkt.add(ZeitPunkt.SECOND, Veraenderung);
				ZeitPunktString += DatumalsString(ZeitPunkt, TrennerDatum)
								+ TrennerMitte
								+ ZeitalsString(ZeitPunkt, TrennerZeit);
				
				return ZeitPunktString;
			}
			
			String ZeitalsString (Calendar Zeit, String Trenner) {
				String ZeitString = "";

				if(Zeit.get(Zeit.HOUR_OF_DAY)<10) {
					ZeitString += "0" + Zeit.get(Zeit.HOUR_OF_DAY);
				} else {
					ZeitString += "" + Zeit.get(Zeit.HOUR_OF_DAY);
				}
				ZeitString += Trenner;
				if(Zeit.get(Zeit.MINUTE)<10) {
					ZeitString += "0" + Zeit.get(Zeit.MINUTE);
				} else {
					ZeitString += "" + Zeit.get(Zeit.MINUTE);
				}
				ZeitString += Trenner;
				if(Zeit.get(Zeit.SECOND)<10) {
					ZeitString += "0" + Zeit.get(Zeit.SECOND);
				} else {
					ZeitString += "" + Zeit.get(Zeit.SECOND);
				}
				
				return ZeitString;
			}
			
			String DatumalsString (Calendar Zeit, String Trenner) {
				String Datum = "";
				
				Datum += Zeit.get(Zeit.YEAR) + Trenner;
				if(Zeit.get(Zeit.MONTH)<9) {
					Datum += "0" + (Zeit.get(Zeit.MONTH) + 1) + Trenner;
				} else {
					Datum += (Zeit.get(Zeit.MONTH) + 1) + Trenner;
				}
				if(Zeit.get(Zeit.DAY_OF_MONTH)<10) {
					Datum += "0" + Zeit.get(Zeit.DAY_OF_MONTH);
				} else {
					Datum += Zeit.get(Zeit.DAY_OF_MONTH);
				}
								
				return Datum;				
			}
		}
	} 
	
	
	static class CAufnahme {
		Panel Body;
		JLabel Titel;
		JLabel Datum;
		JLabel Start;
		JLabel Ende;
		JCheckBox Aufnehmen;
		JCheckBox[] AufgabenBoxen;
		ItemListener AufnehmenListener;
		GlobeTVDaten Daten;
		int Nr;
			
		public CAufnahme(Panel Fenster, GlobeTVDaten DatenKlasse, int Nummer) {
			Daten = DatenKlasse;
			Nr = Nummer;
			
			Body = Fenster;
			
			Titel = new JLabel(Daten.Aufnahmen[Nr].Titel);
			Datum = new JLabel(Daten.Aufnahmen[Nr].StartDatumAlsString("."));
			Start = new JLabel(Daten.Aufnahmen[Nr].StartZeitAlsString(":"));
			Ende = new JLabel(Daten.Aufnahmen[Nr].EndZeitAlsString(":"));
			
			Titel.setPreferredSize(new Dimension(200, 20));
			Datum.setPreferredSize(new Dimension(70, 20));
			Start.setPreferredSize(new Dimension(50, 20));
			Ende.setPreferredSize(new Dimension(50, 20));
			
			
			Body.add(Titel);	
			Body.add(Datum);
			Body.add(Start);
			Body.add(Ende);
			
			Aufnehmen = new JCheckBox("", true);
			Aufnehmen.setPreferredSize(new Dimension(50, 20));
			Body.add(Aufnehmen);
			AufgabenBoxen = new JCheckBox[5];
			for(int i=0;i<5;i++) {
				AufgabenBoxen[i] = new JCheckBox("", true);
				AufgabenBoxen[i].setPreferredSize(new Dimension(50, 20));
				Body.add(AufgabenBoxen[i]);
			}
			AufgabenBoxen[4].setSelected(false);
		
			
			ItemListener AufnehmenListener = new ItemListener() {
				@Override public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==e.DESELECTED) {
						for(int i=0;i<5;i++) {
							AufgabenBoxen[i].setEnabled(false);
						}					
					} else {
						for(int i=0;i<4;i++) {
							AufgabenBoxen[i].setEnabled(true);
							AufgabenBoxen[i].setSelected(true);
						}	
						AufgabenBoxen[4].setEnabled(true);
					}

				}
			};
			Aufnehmen.addItemListener(AufnehmenListener);
			
			
			ItemListener RuhezustandListener = new ItemListener() {
				@Override public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==e.SELECTED) {
						AufgabenBoxen[4].setSelected(false);	
					} 
				}
			};
			AufgabenBoxen[3].addItemListener(RuhezustandListener);
			
			ItemListener HerunterfahrenListener = new ItemListener() {
				@Override public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==e.SELECTED) {
						AufgabenBoxen[3].setSelected(false);	
					} 
				}
			};
			AufgabenBoxen[4].addItemListener(HerunterfahrenListener);
			
			
			
		}
		
		boolean DatenSchreiben (Document PCaufwecken, Document GlobeTVstarten, Document GlobeTVbeenden, Document PCRuhezustand, Document PCherunterfahren, Namespace ns) {
			String EingabeDaten = "";
			
			if(AufgabenBoxen[0].isSelected()) {
				EingabeDaten = Daten.Aufnahmen[Nr].ZeitPunktAlsString(1, "-", "T", ":", -600);
				PCaufwecken.getRootElement().getChild("Triggers", ns).addContent(TimeTriggerEinfuegen(EingabeDaten, ns));
			}
			
			if(AufgabenBoxen[1].isSelected()) {
				EingabeDaten = Daten.Aufnahmen[Nr].ZeitPunktAlsString(1, "-", "T", ":", -300);
				GlobeTVstarten.getRootElement().getChild("Triggers", ns).addContent(TimeTriggerEinfuegen(EingabeDaten, ns));
			}
			
			if(AufgabenBoxen[2].isSelected()) {	
				EingabeDaten = Daten.Aufnahmen[Nr].ZeitPunktAlsString(2, "-", "T", ":", 150);
				GlobeTVbeenden.getRootElement().getChild("Triggers", ns).addContent(TimeTriggerEinfuegen(EingabeDaten, ns));
			}
			
			if(AufgabenBoxen[3].isSelected()) {
				EingabeDaten = Daten.Aufnahmen[Nr].ZeitPunktAlsString(2, "-", "T", ":", 300);
				PCRuhezustand.getRootElement().getChild("Triggers", ns).addContent(TimeTriggerEinfuegen(EingabeDaten, ns));
			}
			
			if(AufgabenBoxen[4].isSelected()) {
				EingabeDaten = Daten.Aufnahmen[Nr].ZeitPunktAlsString(2, "-", "T", ":", 300);
				
				Element TimeTrigger = new Element("TimeTrigger").setNamespace(ns);
				
				Element Repetition = new Element("Repetition").setNamespace(ns);
					Element Interval = new Element("Interval").setNamespace(ns);
					Interval.addContent("PT5M");
					Element Duration = new Element("Duration").setNamespace(ns);
					Duration.addContent("PT30M");
					Element StopAtDurationEnd = new Element("StopAtDurationEnd").setNamespace(ns);
					StopAtDurationEnd.addContent("false");
				Repetition.addContent(Interval);
				Repetition.addContent(Duration);
				Repetition.addContent(StopAtDurationEnd);
				TimeTrigger.addContent(Repetition);
				
				Element StartBoundary = new Element("StartBoundary").setNamespace(ns);
				StartBoundary.addContent(EingabeDaten);
				TimeTrigger.addContent(StartBoundary);
				
				Element Enabled = new Element("Enabled").setNamespace(ns);
				Enabled.addContent("true");
				TimeTrigger.addContent(Enabled);
				
				PCherunterfahren.getRootElement().getChild("Triggers", ns).addContent(TimeTrigger);

			}
			
			return true;
		}
		
		
		static Element TimeTriggerEinfuegen (String EingabeDaten, Namespace ns) {
			Element TimeTrigger = new Element("TimeTrigger").setNamespace(ns);
			Element StartBoundary = new Element("StartBoundary").setNamespace(ns);
			StartBoundary.addContent(EingabeDaten);
			TimeTrigger.addContent(StartBoundary);
			
			Element Enabled = new Element("Enabled").setNamespace(ns);
			Enabled.addContent("true");
			TimeTrigger.addContent(Enabled);
			
			return TimeTrigger;
		}
		
	}
	
	static boolean XMLDateiSchreiben (Document Dokument, String Dateiname) throws JDOMException, IOException {
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat().setEncoding("UTF-16"));
        java.io.FileWriter writer = new java.io.FileWriter(Dateiname);
        out.output(Dokument, writer);
        writer.flush();
        writer.close(); 
        return true;
	}
	
}










