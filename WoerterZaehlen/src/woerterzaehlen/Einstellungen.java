package woerterzaehlen;

import java.util.Observable;

public class Einstellungen extends Observable {
    private int aktualisierungsrate=1000;
    private boolean zahlenSindWoerter=true;
    private static Einstellungen unique=null;
    
    private Einstellungen() {
        super();
    }    
    
    public static Einstellungen instance() {
        if(unique==null) 
            unique = new Einstellungen();
        return unique;
    }
    
    public void setAktualisierungsrate(int rate) {
        if(rate>=0) {
            aktualisierungsrate=rate;
            aktualisiert();
        }
    }
    
    public void setZahlenSindWoerter(boolean zsw) {
        zahlenSindWoerter=zsw;
        aktualisiert();
    }
    
    public int getAktualisierungsrate() {
        return aktualisierungsrate;
    }
    
    public boolean getZahlenSindWoerter() {
        return zahlenSindWoerter;
    }
    
    
    
    private void aktualisiert() {
        setChanged();
        notifyObservers();
    }
    
}
