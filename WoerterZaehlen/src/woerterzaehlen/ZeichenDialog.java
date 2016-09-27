package woerterzaehlen;

import java.awt.*;
import java.awt.event.*;

public class ZeichenDialog extends Dialog implements ActionListener, KeyListener {
    private Checkbox zahlenAlsWoerter;
    
    public ZeichenDialog(Frame owner) {
        super(owner,"Zu Zälende Zeichen",true);
        
        setLayout(new GridLayout(0, 1,0,10));
        
        zahlenAlsWoerter = new Checkbox("Zahlen als Wörter zählen?",Einstellungen.instance().getZahlenSindWoerter());
        add(zahlenAlsWoerter);
        
        Button ok = new Button("OK");
        ok.addActionListener(this);
        add(ok);
        
        addWindowListener(new WindowAdapter() {
           public void windowClosing(WindowEvent e) {
               dispose();
           }
        });
        addKeyListener(this);
        
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }

    
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("OK")) {
            Einstellungen einst = Einstellungen.instance();
            einst.setZahlenSindWoerter(zahlenAlsWoerter.getState());
            dispose();
        }
    }

    public void keyTyped(KeyEvent e) {
    }


    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_ENTER) {
            actionPerformed(new ActionEvent(this,0,"ok"));
        }
    }

    public void keyReleased(KeyEvent e) {
    }
    
}
