package woerterzaehlen;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Observable;
import java.util.Observer;

public class Daten extends Panel implements KeyListener, ActionListener, Runnable, Observer {
    private boolean zahlenSindWoerter;
    
    private Label lZeichen;
    private int zeichen;
    private Label lWoerter;
    private int woerter;
    private Label lRate;
    private int aktualisierungsrate;
    private boolean aktuell;
    private Label lAktuell;
    private Button bAktualisieren;
    
    private TextComponent tc;
    private boolean laeuft=false;
    
    public Daten(TextComponent tc) {
        this.tc=tc;
        tc.addKeyListener(this);
        
        aktualisierungsrate=Einstellungen.instance().getAktualisierungsrate();
        zahlenSindWoerter=Einstellungen.instance().getZahlenSindWoerter();
        
        setLayout(new BorderLayout());
        Panel p = new Panel();
        add(BorderLayout.NORTH, p);
        p.setLayout(new GridLayout(0,2));
        
        p.add(new Label("Wörter:"));
        lWoerter = new Label("0");
        p.add(lWoerter);
        
        p.add(new Label("Zeichen:"));
        lZeichen = new Label("0");
        p.add(lZeichen);
        
        
        p = new Panel();
        add(BorderLayout.SOUTH, p);
        p.setLayout(new GridLayout(0,1));
        
        p.add(new Label("Aktualisierungsrate"));
        lRate = new Label(Integer.toString(aktualisierungsrate));
        p.add(lRate);
        
        lAktuell = new Label("");
        p.add(lAktuell);
        bAktualisieren = new Button("aktualisieren");
        bAktualisieren.addActionListener(this);
        p.add(bAktualisieren);
        
        
        Einstellungen.instance().addObserver(this);
        laeuft=true;
    }
    
    public void run() {
        while(laeuft) {
            try{
                Thread.sleep(aktualisierungsrate);
            }catch(InterruptedException e) {}
            aktualisieren();
        }
    }
    
    private void aktualisieren() {
        zeichen=tc.getText().length();
        lZeichen.setText(""+zeichen);
        woerter=woerter();
        lWoerter.setText(""+woerter);
        setAktuell(true);        
    }
    
    private int woerter() {
        String[] w=tc.getText().replace('\n', ' ').replace('\t', ' ').split(" ");
        int x=0;
        if(zahlenSindWoerter) {
            for(int i=0;i<w.length;i++) {
                for(int j=0;j<w[i].length();j++) {
                    if(Character.isLetter(w[i].charAt(j)) || Character.isDigit(w[i].charAt(j))) {
                        x++;
                        break;
                    }
                }
            }            
        } else {
            for(int i=0;i<w.length;i++) {
                for(int j=0;j<w[i].length();j++) {
                    if(Character.isLetter(w[i].charAt(j))) {
                        x++;
                        break;
                    }
                }
            }
        }
        return x;
    }

    
    public void keyTyped(KeyEvent e) {
    
    }

    public void keyPressed(KeyEvent e) {
        if(aktuell)
            setAktuell(false);
    }

    
    public void keyReleased(KeyEvent e) {
    
    }
    
    public void actionPerformed(ActionEvent e) {
        aktualisieren();
    }
    
    
    private void setAktuell(boolean akt) {
        if(akt) {
            aktuell=true;
            lAktuell.setText("aktuell");
        } else {
            aktuell=false;
            lAktuell.setText("");
        }
    }

    public void update(Observable o, Object arg) {
        zahlenSindWoerter=Einstellungen.instance().getZahlenSindWoerter();
        int rate=Einstellungen.instance().getAktualisierungsrate();
        if (rate>0) {
            aktualisierungsrate=rate;
            lRate.setText(Integer.toString(rate));
            if(!laeuft) {
                laeuft=true;
                Thread t = new Thread(this);
                t.start();
            }
            
        } else if(rate==0) {
            aktualisierungsrate=0;
            lRate.setText("-deaktiviert-");
            laeuft=false;
        }
    }
}
