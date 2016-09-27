package woerterzaehlen;

import java.awt.*;
import java.awt.event.*;

public class Fenster extends Frame implements ActionListener {
    
    private TextArea text;
    private Daten daten;
    
    public Fenster() {
        super("Wörter Zählen");
        
        setLayout(new BorderLayout());
        
        text=new TextArea("",0,0,TextArea.SCROLLBARS_VERTICAL_ONLY);
        add(BorderLayout.CENTER,text);
        
        Einstellungen.instance().setAktualisierungsrate(1000);
        Einstellungen.instance().setZahlenSindWoerter(true);
        Daten d = new Daten(text);
        daten = d;
        add(BorderLayout.EAST,d);
        
        MenuBar mb = new MenuBar();
        setMenuBar(mb);
        Menu m = new Menu("Einstellungen");
        mb.add(m);
        MenuItem aktRate = new MenuItem("Aktualisierungsrate");
        aktRate.addActionListener(this);
        m.add(aktRate);
        MenuItem zeichenEinst = new MenuItem("Zeichen...");
        zeichenEinst.addActionListener(this);
        m.add(zeichenEinst);        
        
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                dispose();
                Einstellungen.instance().setAktualisierungsrate(0);
                System.exit(0);
            }
        });
        
        Thread t = new Thread(d);
        t.start();
        
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("Aktualisierungsrate")) {
            new AktualisierungsDialog(this);
        } else if(e.getActionCommand().equals("Zeichen...")) {
            new ZeichenDialog(this);
        }
        
    }
    
}
