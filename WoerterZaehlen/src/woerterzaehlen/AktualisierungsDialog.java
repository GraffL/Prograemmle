package woerterzaehlen;

import java.awt.*;
import java.awt.event.* ;

public class AktualisierungsDialog extends Dialog implements ActionListener,KeyListener {
    
    private TextField rate;
    private Fenster owner;
    
    public AktualisierungsDialog(Frame owner) {
        super(owner,"Aktualisierungsrate einstellen",true);
        this.owner=(Fenster)owner;
        
        setLayout(new BorderLayout());
        this.rate= new TextField(Integer.toString(Einstellungen.instance().getAktualisierungsrate()));
        this.rate.addKeyListener(this);
        add(BorderLayout.CENTER,this.rate);
        Label l=new Label("Aktualisierungsrate:");
        add(BorderLayout.WEST,l);
        l=new Label("ms");
        add(BorderLayout.EAST,l);
        
        Panel p = new Panel();
        p.setLayout(new GridLayout(2,1));
        add(BorderLayout.SOUTH,p);
        l=new Label(" (0 deaktiviert automatische Aktualisierung)");
        p.add(l);
        Button ok = new Button("OK");
        ok.addActionListener(this);
        p.add(ok);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }

    
    public void actionPerformed(ActionEvent e) {
        try {
            Einstellungen.instance().setAktualisierungsrate(Integer.parseInt(rate.getText()));
        } catch(Exception ex) {
        } finally {
            dispose();
        }        
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_ENTER) {
            actionPerformed(new ActionEvent(this,0,"ok"));
        } else if(!(Character.isDigit(e.getKeyChar()) || e.getKeyCode()==KeyEvent.VK_DELETE || 
                e.getKeyCode()==KeyEvent.VK_BACK_SPACE || e.getKeyCode()==KeyEvent.VK_LEFT || e.getKeyCode()==KeyEvent.VK_RIGHT)) {
            e.consume();
        }
    }

    public void keyReleased(KeyEvent e) {
    }
    
}
