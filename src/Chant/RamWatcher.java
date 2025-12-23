package Chant;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;

public class RamWatcher {
    int region = 0;
    RAM ram;
    JFrame frame;
    JTextField[] bytes;
    JTextField[] labels;
    JTextField input;
    JTextField PC;
    JButton EXIT;
    JTextField REG;
    public RamWatcher(RAM ram){
        this();
        this.ram = ram;
    }
    public RamWatcher(){
        this.frame=new JFrame("Memory Watcher: 0x00000000");
        this.frame.setLayout(new GridBagLayout());
        this.frame.setSize(600, 600);
        this.frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        labels=new JTextField[32];
        GridBagConstraints c = new GridBagConstraints();
        c.fill = 1;
        c.weightx=0.5;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = new JTextField(2);
            labels[i].setEditable(false);
            labels[i].setText(String.format("%01X", (i<16) ? i : (i%16)*16));
            c.gridx = i<16 ? i+1 : 0;
            c.gridy = i<16 ? 0 : (i-16)+1;
            this.frame.add(labels[i], c);
        }
        bytes = new JTextField[256];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = new JTextField(2);
            bytes[i].setEditable(false);
            bytes[i].setText(String.format("%01X", 0));
            c.gridx = (i%16)+1;
            c.gridy = (int)Math.floor(i/16)+1;
            this.frame.add(bytes[i], c);
        }
        c.gridwidth=4;
        c.gridx=0;
        c.gridy=18;
        this.input = new JTextField(8);
        this.input.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {}

            @Override
            public void focusLost(FocusEvent e) {
                region = Integer.parseUnsignedInt(input.getText(), 16);
                frame.setTitle(String.format("Memory Watcher: %#08X", region));
                refresh();
            }
            
        });
        this.frame.add(input, c);
        c.gridx=4;
        this.PC = new JTextField(8);
        PC.setEditable(false);
        this.frame.add(PC, c);
        this.REG = new JTextField(8);
        this.REG.setEditable(false);
        c.gridx=8;
        this.frame.add(REG, c);
        c.gridx=12;
        this.EXIT = new JButton("QUIT");
        EXIT.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
                Runtime.getRuntime().halt(0);
            }
            
        });
        this.frame.add(EXIT, c);
    }
    public void display(){
        this.refresh();
        this.frame.setVisible(true);
    }
    public void refresh(){
        if (ram != null) {
            for (int i = 0; i < 256; i++) {
                bytes[i].setText(String.format("%02X",ram.getByte(region+i)));
            }
        }
    }
}
