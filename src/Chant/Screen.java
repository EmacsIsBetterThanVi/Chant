package Chant;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

public class Screen extends Peripheral{
    public Screen(int MMIOaddr, byte[] region) {
        super(MMIOaddr, region);
    }
    public int[] KeyboardBuffer;
    public int KeyboardBufferPoint;
    public JFrame Frame1;
    public PixelCanvas Display1;
    public JFrame Frame2;
    public PixelCanvas Display2;
    public JFrame Frame3; //Skip
    public PixelCanvas Display3;
    public JFrame Frame4; //Skip
    public PixelCanvas Display4;
    public JFrame TextFrame;
    public TextCanvas TextDisplay;
    public byte FLAGS; 
    public boolean Initilized = false;
    public static KeyListener Keyboard;
    /*
    Flag 0: Display 1, Flag 1: Display 2, Flag 2: Display 3(Only matteres if in 4bpp), Flag 3: Display 4(Only matteres if in 4bpp)
    Flag 4: Text Display, Flag 5: 4bpp mode, Flag 6: Frame 1 ALT(Only matteres if in 8bpp), Flag 7: Frame 2 ALT(Only matteres if in 8bpp)
    */
    private boolean testFlag(int Flag){
        return (FLAGS&(1<<Flag))!=0;
    }
    public static int getPallet(int n){
        return 0x396000+(768*n);
    }
    public static int getPallet(int n, boolean bpp4){
        return getPallet(bpp4 ? (n/2) : n);
    }
    public void reset(){
        if (testFlag(0) && Frame1 == null) {
            Frame1 = new JFrame("CVM Frame 1");
            Frame1.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            Frame1.setUndecorated(true);
            Display1 = new PixelCanvas(CVM.memory, getPallet(testFlag(6) ? 0 : 1, testFlag(5)), 0x300000, !testFlag(5));
            Frame1.addKeyListener(Keyboard);
        } else if (!testFlag(0) && Frame1 != null){
            Frame1.setVisible(false);
            Frame1.setEnabled(false);
        } else if (testFlag(0) && Frame1 != null){
            Frame1.setVisible(true);
            Frame1.setEnabled(true);
            Display1.bpp = !testFlag(5);
            Display1.Pallet = getPallet(testFlag(6) ? 0 : 1, testFlag(5));
        }
        if (testFlag(1) && Frame2 == null){
            Frame2 = new JFrame("CVM Frame 2");
            Frame2.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            Frame2.setUndecorated(true);
            Display2 = new PixelCanvas(CVM.memory, getPallet(testFlag(7) ? 2 : 3, testFlag(5)), testFlag(5) ? 0x325800 : 0x34B000, !testFlag(5));
            Frame2.addKeyListener(Keyboard);
        } else if (!testFlag(1) && Frame2 != null){
            Frame2.setVisible(false);
            Frame2.setEnabled(false);
        } else if (testFlag(1) && Frame2 != null){
            Frame2.setVisible(true);
            Frame2.setEnabled(true);
            Display2.address = testFlag(5) ? 0x325800 : 0x34B000;
            Display2.bpp = !testFlag(5);
            Display2.Pallet = getPallet(testFlag(7) ? 0 : 1, testFlag(5));
        }
        if (testFlag(4) && TextFrame == null){
            TextFrame = new JFrame("CVM Text Frame");
            TextFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            TextFrame.setUndecorated(true);
            TextDisplay = new TextCanvas(CVM.memory, getPallet(3), 0x396C00, 0x3A0000);
            TextDisplay.setSize(640, 400);
            TextFrame.setSize(640, 400);
            TextFrame.add(TextDisplay);
            TextFrame.setVisible(true);
            TextFrame.addKeyListener(Keyboard);
        } else if (!testFlag(4) && TextFrame != null){
            TextFrame.setVisible(false);
            TextFrame.setEnabled(false);
        } else if (testFlag(4) && TextFrame != null){
            TextFrame.setVisible(true);
            TextFrame.setEnabled(true);
        }
        if (testFlag(5)){
            if (testFlag(2) && Frame3 == null){
                Frame3 = new JFrame("CVM Frame 3");
                Frame3.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                Frame3.setUndecorated(true);
                Display3 = new PixelCanvas(CVM.memory, getPallet(2), 0x34B000, false);
                Frame3.addKeyListener(Keyboard);
            } else if (!testFlag(2) && Frame3 != null){
                Frame3.setVisible(false);
                Frame3.setEnabled(false);
            } else if (testFlag(2) && Frame3 != null){
                Frame3.setVisible(true);
                Frame3.setEnabled(true);
            }
            if (testFlag(3) && Frame4 == null){
                Frame4 = new JFrame("CVM Frame 4");
                Frame4.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                Frame4.setUndecorated(true);
                Display4 = new PixelCanvas(CVM.memory, getPallet(3), 0x370800, false);
                Frame4.addKeyListener(Keyboard);
            } else if (!testFlag(3) && Frame4 != null){
                Frame4.setVisible(false);
                Frame4.setEnabled(false);
            } else if (testFlag(3) && Frame4 != null){
                Frame4.setVisible(true);
                Frame4.setEnabled(true);
            }
        } else {
            if (Frame3!=null){
                Frame3.setVisible(false);
                Frame3.setEnabled(false);
            }
            if (Frame4!=null){
                Frame4.setVisible(false);
                Frame4.setEnabled(false);
            }
        }
    }
    public void INIT(){
        Keyboard=new KeyListener() {
            private void key(int e){
                KeyboardBuffer[KeyboardBufferPoint]=e;
                KeyboardBufferPoint++;
                if(KeyboardBufferPoint==64){
                    KeyboardBufferPoint=0;
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                key(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                key(255);
                key(e.getKeyCode());
            }
            
        };
        KeyboardBuffer = new int[64];
        reset();
        Initilized=true;
    }
    @Override
    public void refresh() {
        //TODO finish Display Class(Will be part of version 1.2)
        switch(this.region[this.MMIOaddr]){
            case 1:
                FLAGS = this.region[this.MMIOaddr+1];
                if (Initilized) {
                    reset();
                }
                break;
            case 2:
                if (!Initilized) INIT();
                else reset();
                break;
        }
        this.region[this.MMIOaddr]=0;
    }

    @Override
    public int getIOports() {
        return 4;
    }
        
}
class PixelCanvas extends Canvas {
public RAM ram;
public boolean bpp=true; // false: 4, true: 8
public int Pallet;
public int address;
public PixelCanvas(RAM ram, int Pallet, int address){
    this.ram = ram;
    this.Pallet=Pallet;
    this.address = address;
}
public PixelCanvas(RAM ram, int Pallet, int address, boolean bpp){
    this.ram = ram;
    this.Pallet=Pallet;
    this.address = address;
    this.bpp=bpp;
}
@Override
public void paint(Graphics g){
    super.paint(g);

}
}
class TextCanvas extends Canvas {
public RAM ram;
public int Pallet;
public int address;
public int CharacterRom;
public TextCanvas(RAM ram, int Pallet, int address, int CharacterRom){
    this.ram = ram;
    this.Pallet=Pallet;
    this.address = address;
    this.CharacterRom = CharacterRom;
}
@Override
public void paint(Graphics g) {
    super.paint(g);
    int c;
    byte ch;
    int chLine;
    for (int i = 0; i < 2000; i++) {
        c=this.ram.getByte(this.address+(i*4)+2)*3;
        g.setColor(new Color(this.ram.getByte(c), this.ram.getByte(c+1), this.ram.getByte(c+2)));
        g.fillRect(8*(i%80), (int)(16*Math.floor(i/80)), 8, 16);
        c=this.ram.getByte(this.address+(i*4)+1)*3;
        g.setColor(new Color(this.ram.getByte(c), this.ram.getByte(c+1), this.ram.getByte(c+2)));
        ch = this.ram.getByte(this.address+(i*4));
        for (int j=0; j<16; j++){
            chLine = this.ram.getByte(this.CharacterRom+(16*ch)+j);
            for (int k=0; k<8; k++){
                if ((chLine&(1<<(7-k)))!=0) g.fillRect(8*(i%80)+k, (int)(16*Math.floor(i/80))+j, 1, 1);
            }
        }
    }
}
}
