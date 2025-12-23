package Chant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.w3c.dom.Text;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
/*
Chant is a programing language which uses an ultraminimal instruction set virtual machine, called the CVM.
The CVM is 32 bit with support for 64 bit numbers, and has a 32 bit 4 byte alighned instruction in the following format
iiiccIdraaaaaaaaaaaaaaaaaaaaaaaa
i represents the 3 bit instruction which can be one of the following instuctions:
0 - NOP
1 - ADD
2 - AND
3 - NOT (Indirect adressing mode loads the register with the current Processor ID)
4 - STORE and CLEAR
5 - JMP to subroutine (There is a call stack)
6 - INC (Increments data at or pointed to by the address, not the register)
7 - DEC (Deincrements data at or pointed to by the address, not the register)
c represents the 2 bit conditional which can be one of the following:
00 - Unconditional
01 - Zero
10 - Greater than zero (Bit 31 or 63 is 0 and any other bit is 1)
11 - Less than zero (Bit 31 or 63 is 1)
a represents the 24 bit pointer
If I is 1 then the 24 bit points to a 32 bit pointer which points to the data else it points directly to the data.
If the d flag is 1 then data is 64 bit. If the r flag is 1 then return after this instruction.
The CVM uses MMIO and has the following Memory Map
0x0000 0000 - 0x0000 03FF: System Memory, ths following structure is duplicated every 32 bytes. 
                      +00: Processor Flags
                            * Bit 0 enables interupts.
                            * Bit 1 halts the processor(Autocleared on interupt)
                            * Bit 2 enables the Keyboard Interupt
                            * Bit 3 enables the Timer 1 Interupt
                            * Bit 4 enables the Timer 2 Interupt
                            * Bit 5 enables the Menu Item interupt
                            * Bit 6 enables the Shutdown Request Interupt
                            * Bit 7 enables receving  Interprocessor Interupt
                            * Bits 8 sends an IPI
                            * Bit 9-13 contains the target to recive an IPI
                            * Bit 14 sets the target of the next IPI to be all except target
                            * All other bits are reserved(0)
                      +04: Stack Pointer, pointer grows upwads from this value.
                      +08: Keyboard Interupt Handler Pointer.
                      +0C: Timer 1 Interupt Handler Pointer.
                      +10: Timer 2 Interupt Handler Pointer.
                      +14: Menu Item Interupt Handler Pointer. Inquire on the peripheral for information(MP 3)
                      +18: Shutdown Request Interupt Handler Pointer. Use this to clean up when the program is told to quit normaly.
                      +1C: Interprocessor Interupt Handler.
0x0000 0400 - 0x002F FFFF: Free Memory for any purpose
0x0030 0000 - 0x0034 AFFF: Video Out Frame 1 (8 bits per pixel or in 4 bits per pixel this becomes frames 1 and 2)
0x0034 B000 - 0x0039 5FFF: Video Out Frame 2 (8 bits per pixel, or in 4 bits per piexel this becomes frames 3 and 4)
0x0039 6000 - 0x0039 62FF: Color Pallet 1(Frame 1)
0x0039 6300 - 0x0039 65FF: Color Pallet 2(Frame 1 Alt or Frame 2 in 4bpp mode)
0x0039 6600 - 0x0039 68FF: Color Pallet 3(Frame 2 or Frame 3 in 4bpp mode)
0x0039 6900 - 0x0039 6BFF: Color Pallet 4(Frame 2 Alt or Frame 4 in 4bpp)
0x0039 6C00 - 0x0039 917F: Console Frame (Arragnged as Character Forground Background 0
0x0039 9180 - 0x0039 FFFF: Hole/Pointer Space(Must be set at start up with the EXTRA Pointer space flag set in the header)
0x003A 0000 - 0x003A FFFF: Character Rom
0x003B 0000 - 0x003E FFFF: BIOS/ROM
0x003F 0000 - 0x003F FFFF: MMIO
         +0x0000 - 0x0003: STDIO
                       +0: OUTPUT BYTE
                       +1: CMD BYTE Set to 255 to request input from STDIN, 1 clears the console
                       +2: INPUT BYTE
                       +3: Number of bytes remaining in STDIN
         +0x0004 - 0x0009: MICO PERIPHERALS
                       +0: PERIPHERAL
                       +1: CMD
                       +2: ARG1
                       +3: ARG2
                       +4: ARG3
                       +5: ARG4
         +0x000A - 0x000D: DISPLAY/INTERFACE
                       +0: CMD
                       +1: ARG1
                       +2: ARG2
                       +3: ARG3 
         +0x000E - 0x000F: TIMER 1 and 2
                       +0: CMD
                       +1: ARG
0x0040 0000 - 0x00FF FFFF: Program Code
0x0100 0000 - 0xFFEF FFFF: Free Memory for data or more program code, probobly has lots of holes and can't be used for pointers(Indirect addressing required).
                            (Must be set at start up with the EXTRA Memory length field in the header.)
0xFFF0 0000 - 0xFFFF FFFF: Stacks

BIOS must start with an unconditional jump to the Boot Processor BIOS, followed by the Extra Processor boot program, which should probobly just halt the processor.
*/
@SuppressWarnings("all")
public class CVM{
    public byte ID;
    private long register = 0;
    private int PC = 0x3B0000;
    public static RAM memory;
    final static String versionString = "1.0.0";
    public static String[] peripheralList;
    public static MPHandler MPhandler;
    public static Screen screen;
    public static CVM[] CORES;
    public static void printhelp(){
        System.out.println("cvm usage: cvm [options] file [options]");
        System.out.println("Options can be any of the following:");
        System.out.println("\t-V, --version\t\tPrints the version and exits");
        System.out.println("\t-v, --verbose\t\tPuts the CVM in verbose mode, revealing all actions");
        System.out.println("\t-h ,--help\t\tPrints this message then exits");
        System.out.println("\t-H, --header\t\tSets a specific file to be the header file\n\t\t\t\t\trather than read from the start of the main file");
        System.out.println("\t-i, --input\t\tSets the input file");
        System.out.println("\t-M, --memory-monitor\tDisplays a memory monitor");
    }
    public static int readHeader(byte[] data){
        BIOS = new String(Arrays.copyOfRange(data, 9, 9+data[5]));
        ExtraMemoryLength=data[1]+(data[2]<<8)+(data[3]<<16)+(data[4]<<24);
        if (ExtraMemoryLength>4277141504l) ExtraMemoryLength=0xFEF00000;
        if (verbose){
            System.out.print("Processing header: ");
            for (int j = 0; j < data.length; j++) {
                System.out.printf("%x, ", data[j]);
            }
            System.out.println("\nBIOS file: "+BIOS);
            System.out.printf("Extra memory amount: 0x%x\n", ExtraMemoryLength);
            System.out.printf("Number of Processors: %d\n", 1+((data[0]>>>1)&31));
        }
        peripherals = new Peripheral[4+data[6]];
        peripheralList = new String[data[6]];
        int currentAddress = data[7]+(data[8]<<8);
        //TODO implement micro peripheral loader?
        for (int i=0; i<data[6]; i++){
            int len = data[currentAddress];
            currentAddress++;
            String peripheralName = "";
            for (int j=0; j<len; j++){
                peripheralName=peripheralName+(char)data[currentAddress];
                currentAddress++;
            }
            if(verbose) System.out.println("Requesting Peripheral: "+peripheralName);
            
        }
        return (data[0]>>>1)&31;
    }
    public static String BIOS;
    public static int ExtraMemoryLength =0;
    public static byte readError(FileInputStream fileInputStream) throws IOException{
        byte a = (byte)fileInputStream.read();
        if(a==-1) {
            System.err.println("File ended abruptly");
            System.exit(5);
        }
        return a;
    }
    static Peripheral[] peripherals;
    static boolean verbose=false;
    static int pCount=0;
    public static boolean HALT=false;
    public static void addPeripheral(Peripheral peripheral) {
        peripherals[pCount]=peripheral;
        pCount++;
    }
    public static RamWatcher ramWatcher;
    public static void main(String[] args){
        /* 
         The CVM accepts a header to state information about the virtual machine which is structured as follows:
         0x00000, 3 - Length of header, byte after starts program data(Little Endian).
         0x00003, 1 - Properties field
                [0] - EXTRA Pointer space
              [1-5] - Number of extra processors(If 0 then 1 processor, if 1 then 2 processors and so on).
         0x00004, 4 - EXTRA memory length(Little endian)
         0x00008, 1 - Length of BIOS field
         0x00009, 1 - Number of IO fields
         0x00010, 2 - Start of IO fields
         0x00012, ? - BIOS field(A string containing the file path to the BIOS file)
         0x?????, ? - First IO field
              +0, 1 - Length of path field
              +1, ? - Path field
         0x?????, ? - Second IO field
         etc.
         */
        if (args.length == 0){
            System.out.println("CVM version "+versionString);
            printhelp();
            return;
        }
        byte c = 0;
        byte j=0;
        boolean memuse=false;
        String seperateHeader = "";
        int ExtraCores=0;
        for (String string : args) {
            switch (string) { 
                case "-V":
                case "--version":
                    System.out.println("CVM version "+versionString);
                    return;
                case "-v":
                case "--verbose":
                    verbose=true;
                    if (c==j) c++;
                    break;
                case "-h":
                case "--help":
                    printhelp();
                    return;
                case "-H":
                case "--header":
                    if (c==j) c+=2;
                    j++;
                    seperateHeader=args[j];
                    break;
                case "-i":
                case "--input":
                    j++;
                    c=j;
                    break;
                case "-m":
                case "--memory-usage":
                    memuse=true;
                    c++;
                    break;
                case "-M":
                case "--memory-monitor":
                    ramWatcher = new RamWatcher();
                    c++;
                    break;
                default:
                    if (string.charAt(0) == '-'){
                        System.out.println("Invalid command line option: "+string);
                        printhelp();
                        return;
                    }
                    break;
            }
            j++;
        }
        byte[] header;
        try(FileInputStream file = new FileInputStream(args[c])) {
            int headerLen = readError(file);
            headerLen+=readError(file)<<8;
            headerLen+=readError(file)<<16;
            if(seperateHeader.length()==0){
                header = new byte[headerLen];
                if (file.read(header)==-1){
                    System.err.println("Malformated file: header length mismatch");
                    System.exit(3);
                }
                ExtraCores=readHeader(header);
            } else {
                FileInputStream headerFile= new FileInputStream(seperateHeader);
                header=headerFile.readAllBytes();
                ExtraCores=readHeader(header);
                headerFile.close();
                file.skip(headerLen);
            }
            if (!(new File(BIOS).exists())){
                BIOS = System.getProperty("java.class.path")+"/Chant/"+BIOS;
            }
            try (FileInputStream BIOSfile = new FileInputStream(BIOS)){
                memory = new RAM(
                    new int[]{0, 0x400, 0x300000, 0x34B000, 0x396000, 0x396300, 0x396600, 0x396900, 0x396C00, 0x399180, 0x3A0000, 0x3B0000, 0x3F0000, 0x400000, 0x1000000, 0xFFF00000},
                    new int[]{0x400, 0x2FFC00, 0x4B000, 0x4B000, 0x300, 0x300, 0x300, 0x300, 9600, (header[0] & 1) == 1 ? 0x6E80 : 4, 65536, 0x40000, 65536, 0xC00000, ExtraMemoryLength, 0x100000}, new byte[][]{{}, BIOSfile.readAllBytes()}, new int[]{10, 11});    
            } catch (IllegalArgumentException iae){
                System.err.println("Error in memory creation.");
                System.exit(1);
                return;
            }catch (IOException ioe) {
                System.err.println("Error reading BIOS file");
                System.exit(4);
                return;
            }
            byte[] data = memory.getRegion(0x00400000);
            int a=0;
            byte b=0;
            while ((b=(byte)file.read())!=-1) {
                data[a]=b;
                a++;
            }
        } catch (IOException ioe) {
            System.err.println("Error reading file");
            System.exit(2);
            return;
        }
        if (verbose) System.out.println("Initilizing Peripherals");
        addPeripheral(new Peripheral(0, memory.getRegion(0x3F0000)) {
            public String data="";
            @Override
            public void refresh() {
                if (this.region[this.MMIOaddr]!=0) {
                    System.out.printf("%c", this.region[this.MMIOaddr]);
                    this.region[this.MMIOaddr]=0;
                }
                if (this.region[this.MMIOaddr+1] == 255){
                    Scanner sc = new Scanner(System.in);
                    data= data + sc.nextLine();
                    sc.close();
                    this.region[this.MMIOaddr+1]=0;
                } else if (this.region[this.MMIOaddr+1]==1){
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                }
                if (this.region[this.MMIOaddr+2]==0 && data.length()>0){
                    this.region[this.MMIOaddr+2] = (byte)data.charAt(0);
                    data=data.substring(1);
                }
                this.region[this.MMIOaddr+3] = (data.length()<256 ? (byte)data.length() : (byte)255);
            }
            @Override
            public int getIOports() {
                return 4;
            }
            
        });
        MPhandler = new MPHandler(4, memory.getRegion(0x3F0000));
        addPeripheral(MPhandler);
        MPhandler.addPeripheral(new MicroPeripheral(1) { // PROCESSOR MANAGMENT
            @Override
            public byte[] refresh(byte cmd, byte arg1, byte arg2, byte arg3, byte arg4) {
                if (cmd==1){
                    System.exit(arg1);
                } else if (cmd==2){
                    System.out.println("CVM: Full Stop");
                    CVM.HALT = true;
                } else if (cmd==3){
                    Runtime.getRuntime().halt(arg1);
                }
                return this.unchanged(cmd, arg1, arg2, arg3, arg4);
            }
            
        });
        MPhandler.addPeripheral(new MicroPeripheral(2) { //Keyboard
            public int KeyboardBufferPoint = 0;
            @Override
            public byte[] refresh(byte cmd, byte arg1, byte arg2, byte arg3, byte arg4) {
                if (arg1==0 && (screen.KeyboardBuffer[KeyboardBufferPoint] != 0) ) {
                    byte[] data = new byte[]{cmd, (byte)screen.KeyboardBuffer[KeyboardBufferPoint], 0, (byte)screen.KeyboardBufferPoint, (byte)KeyboardBufferPoint};
                    screen.KeyboardBuffer[KeyboardBufferPoint] = 0;
                    KeyboardBufferPoint++;
                    return data;
                }
                return unchanged(cmd, arg1, arg2, arg3, arg4);
            }
            
        });
        MPhandler.addPeripheral(new MicroPeripheral(3) {
            public JMenuBar CVMmenu;
            @Override
            public byte[] refresh(byte cmd, byte arg1, byte arg2, byte arg3, byte arg4) {
                if (CVMmenu!=null){
                    
                } else if(cmd == 255 && screen.Initilized){
                    CVMmenu = new JMenuBar();
                    screen.Frame1.setJMenuBar(CVMmenu);
                    screen.Frame2.setJMenuBar(CVMmenu);
                    screen.Frame3.setJMenuBar(CVMmenu);
                    screen.Frame4.setJMenuBar(CVMmenu);
                    screen.TextFrame.setJMenuBar(CVMmenu);
                }
                return unchanged(cmd, arg1, arg2, arg3, arg4);
            }
            
        });
	//TODO Add arbitrary micro peripheral loader?
        screen = new Screen(10, memory.getRegion(0x3F0000));
        addPeripheral(screen);
        addPeripheral(new Peripheral(14, memory.getRegion(0x3F0000)) {
            Timer t1;
            Timer t2; 
            TimerTask t1Task;
            TimerTask t2Task;
            boolean INIT=false;
            int time = 0;
            Calendar c;
            @Override
            public void refresh() {
                if (!INIT && this.region[MMIOaddr]==255){
                    t1=new Timer(true);
                    t2=new Timer(true);
                    t1Task = new VMTimerTask(1);
                    t1Task = new VMTimerTask(2);
                    c = Calendar.getInstance();
                } else switch (this.region[MMIOaddr]) {
                    // CMDS: 1) SET t1, 2) SET t2, 3) prepare for set with 1000/ARG miliseconds
                    // 4) prepare for set with ARG miliseconds 5) prepare for set with current setting and ARG*100 miliseconds
                    // 6) Clear setting 7) Get day of month 8) Get Month 9) Get year in centry
                    // 10) get centry 11) get hour 12) get minute 13) get second 14) set time zone
                    case 1:
                        if (time!=0) {
                            t1Task.cancel();
                            t1.purge();
                            t1Task = new VMTimerTask(1);
                            t1.scheduleAtFixedRate(t1Task, time, time);
                        }
                        time=0;
                        break;
                    case 2:
                        if (time!=0) {
                            t2Task.cancel();
                            t2.purge();
                            t2Task = new VMTimerTask(2);
                            t2.scheduleAtFixedRate(t1Task, time, time);
                        }
                        time=0;
                        break;
                    case 3:
                        time = 1000/this.region[MMIOaddr+1];
                        break;
                    case 4:
                        time = this.region[MMIOaddr+1];
                        break;
                    case 5:
                        time = time+=this.region[MMIOaddr+1]*100;
                        break;
                    case 6:
                        time = 0;
                        break;
                    case 7:
                        this.region[MMIOaddr+1]=(byte)(c.get(Calendar.DAY_OF_MONTH));
                        break;
                    case 8:
                        this.region[MMIOaddr+1]=(byte)(c.get(Calendar.MONTH)+1);
                        break;
                    case 9:
                        this.region[MMIOaddr+1]=(byte)(c.get(Calendar.YEAR)%100);
                        break;
                    case 10:
                        this.region[MMIOaddr+1]=(byte)((c.get(Calendar.YEAR)/100)+1);
                        break;
                    case 11:
                        this.region[MMIOaddr+1]=(byte)(c.get(Calendar.HOUR_OF_DAY));
                        break;
                    case 12:
                        this.region[MMIOaddr+1]=(byte)(c.get(Calendar.MINUTE));
                        break;
                    case 13:
                        this.region[MMIOaddr+1]=(byte)(c.get(Calendar.SECOND));
                        break;
                    case 14:
                        c.setTimeZone(TimeZone.getTimeZone(TimeZone.getAvailableIDs()[this.region[MMIOaddr+1]]));
                        break;
                }
                this.region[MMIOaddr] = 0;
            }

            @Override
            public int getIOports() {
                return 2;
            }
            
        });
        int baseAddr = 16;
        for (int i=0; i<peripheralList.length; i++){
            try {
                Class p = CVM.class.getClassLoader().loadClass(peripheralList[i]);
                peripherals[i] = (Peripheral)p.getDeclaredConstructor(int.class, byte[].class).newInstance(baseAddr, memory.getRegion(0x3F0000));
                if (peripherals[i] == null) {
                    peripherals[i] = Peripheral.defaultPeripheral;
                }
                baseAddr+=peripherals[i].getIOports();
            } catch (ClassNotFoundException npe){
                System.err.println("Can not find peripheral "+peripheralList[i]);
            } catch (Exception e) {
                System.err.println("Class is not a peripheral");
            }
        }
        Signal.handle(new Signal("INT"), new ShutdownHook());
        if (memuse) System.out.println(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        if (verbose) System.out.println("Creating cores");
        CORES = new CVM[ExtraCores+1];
        for (byte i=0; i<=ExtraCores; i++){
            CORES[i]=new CVM(i);
        }
        if (ramWatcher != null) {
            ramWatcher.ram=memory;
            ramWatcher.display();
        }
        if (verbose) System.out.println("Starting Execution");
        EXEC();
    }
    public CVM(byte ID){
        this.ID=ID;
        this.register=0;
        if (ID==0) this.PC = 0x3B0000;
        else this.PC=0x3B0004;
    }
    public void exec(){
        if ((memory.getByte(ID*32)&2)!=0) return;
        int instruction = memory.get(PC);
        int address = instruction & 0xFFFFFF;
        boolean conditional = false;
        switch ((instruction & 0x18000000) >> 27){
            case 0:
                conditional=true;
                break;
            case 1:
                if (register == 0) conditional=true;
                break;
            case 2:
                if (register != 0 && (register & (1<<31)) == 0) conditional=true;
                break;
            case 3:
                if ((register & (1<<31)) != 0) conditional=true;
                break;
        }
        if (conditional){
            switch (instruction>>>29) {
                case 1:
                    address=((instruction&(1<<26)) != 0 ? memory.get(address): address );
                    this.register+= ((instruction&(1<<25)) != 0 ? memory.getD(address) : memory.get(address));
                    if ((instruction&(1<<25)) == 0) this.register&=(1l<<32)-1;
                    break;
                case 2:
                    address=((instruction&(1<<26)) != 0 ? memory.get(address): address );
                    this.register&= ((instruction&(1<<25)) != 0 ? memory.getD(address) : memory.get(address));
                    break;
                case 3:
                    this.register= ((instruction&(1<<26)) != 0 ? this.ID : ~this.register) & ((instruction&(1<<25)) != 0 ? 0xFFFFFFFFFFFFFFFFl : 0xFFFFFFFF);
                    break;
                case 4:
                    address=((instruction&(1<<26)) != 0 ? memory.get(address): address );
                    if ((instruction&(1<<25)) != 0){
                        memory.writeD(address, register);
                    } else {
                        memory.write(address, (int)(register));
                    }
                    this.register=0;
                    break;
                case 5:
                    memory.write(4+32*this.ID, memory.get(4+32*this.ID)+4);
                    memory.write(memory.get(4+32*this.ID), PC);
                    if (verbose) System.out.println("Jumping to: "+String.format("%H", address));
                    this.PC = ((instruction&(1<<26)) != 0 ? memory.get(address): address)-4;
                    break;
                case 6:
                    address=((instruction&(1<<26)) != 0 ? memory.get(address): address );
                    if ((instruction&(1<<25)) != 0){
                        memory.writeD(address, memory.getD(address)+1);
                    } else {
                        memory.write(address, memory.get(address)+1);
                    }
                case 7:
                    address=((instruction&(1<<26)) != 0 ? memory.get(address): address );
                    if ((instruction&(1<<25)) != 0){
                        memory.writeD(address, memory.getD(address)-1);
                    } else {
                        memory.write(address, memory.get(address)-1);
                    }
            }
        }
        if ((instruction & 0x1000000) != 0){
            this.PC = memory.get(memory.get(4+32*this.ID)) - 4;
            memory.write(4+32*this.ID, memory.get(4+32*this.ID)-4);
        }
        this.PC+=4;
    }
    public static void EXEC(){
        while (true){
            if (ramWatcher != null) {
                ramWatcher.refresh();
                ramWatcher.PC.setText(String.format("%08X", CORES[0].PC));
                ramWatcher.REG.setText(String.format("%08X", CORES[0].register));
            }
            if (HALT) continue;
            for (CVM core : CORES) {
                core.exec();
            }
            for (Peripheral p : peripherals){
                p.refresh();
            }
        }
    }
    public boolean interupt(int I){
        byte flags = memory.getByte(this.ID*32);
        if ((flags & 1) == 1){
            if ((flags & (1<<(2+I))) != 0){
                if (verbose) System.out.printf("Interupt %d recived and handled\n", I);
                memory.write(4+32*this.ID, memory.get(4+32*this.ID)+4);
                memory.write(memory.get(4+32*this.ID), this.PC);
                this.PC = memory.get(32*this.ID+8+4*I);
                memory.writeByte(32*this.ID, (byte)(flags & 0b11111101));
            }
        }
        return false;
    }
}
@SuppressWarnings("all")
class ShutdownHook implements SignalHandler {
    public void handle(Signal signal) {
        if (CVM.HALT) System.exit(5);
        for (CVM core : CVM.CORES) {
            if (CVM.verbose) {
                System.out.printf("Sending Shutdown Interupt to processor %d\n", core.ID);
            }
            core.interupt(4); //Send a shutdown request to each processor
        }
    }
}
class Screen extends Peripheral{
        public Screen(int MMIOaddr, byte[] region) {
            super(MMIOaddr, region);
        }
        public int[] KeyboardBuffer;
        public int KeyboardBufferPoint;
        public JFrame Frame1;
        public Canvas Display1;
        public JFrame Frame2;
        public Canvas Display2;
        public JFrame Frame3; //Skip
        public Canvas Display3;
        public JFrame Frame4; //Skip
        public Canvas Display4;
        public JFrame TextFrame;
        public Canvas TextDisplay;
        public byte FLAGS; 
        public boolean Initilized = false;
        public static KeyListener Keyboard;
        /*
        Flag 0: Display 1, Flag 1: Display 2, Flag 2: Display 3(Only matteres if in 4bpp), Flag 3: Display 4(Only matteres if in 4bpp)
        Flag 4: Text Display, Flag 5: 4bpp mode
        */
        private boolean testFlag(int Flag){
            return (FLAGS&(1<<Flag))!=0;
        }
        public void reset(){
            if (testFlag(0) && Frame1 == null) {
                Frame1 = new JFrame("CVM Frame 1");
                Frame1.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                Frame1.setUndecorated(true);
                Display1 = new Canvas();
                Frame1.addKeyListener(Keyboard);
            } else if (!testFlag(0) && Frame1 != null){
                Frame1.setVisible(false);
                Frame1.setEnabled(false);
            } else if (testFlag(0) && Frame1 != null){
                Frame1.setVisible(true);
                Frame1.setEnabled(true);
            }
            if (testFlag(1) && Frame2 == null){
                Frame2 = new JFrame("CVM Frame 2");
                Frame2.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                Frame2.setUndecorated(true);
                Display2 = new Canvas();
                Frame2.addKeyListener(Keyboard);
            } else if (!testFlag(1) && Frame2 != null){
                Frame2.setVisible(false);
                Frame2.setEnabled(false);
            } else if (testFlag(1) && Frame2 != null){
                Frame2.setVisible(true);
                Frame2.setEnabled(true);
            }
            if (testFlag(4) && TextFrame == null){
                TextFrame = new JFrame("CVM Text Frame");
                TextFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                TextFrame.setUndecorated(true);
                TextDisplay = new Canvas();
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
                    Display3 = new Canvas();
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
                    Display4 = new Canvas();
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
class CanvasDrawer {
    public Canvas myCanvas;
    public RAM ram;
    public boolean bpp=true; // false: 4, true: 8
    public int Pallet;
    public int address;
    public CanvasDrawer(Canvas c, RAM ram, int Pallet, int address){
        this.myCanvas = c;
        this.ram = ram;
        this.Pallet=Pallet;
        this.address = address;
    }
    public CanvasDrawer(Canvas c, RAM ram, int Pallet, int address, boolean bpp){
        this.myCanvas = c;
        this.ram = ram;
        this.Pallet=Pallet;
        this.address = address;
        this.bpp=bpp;
    }
    public void Draw(){

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
class VMTimerTask extends TimerTask{
    int interupt;
    public VMTimerTask(int interupt){
        this.interupt=interupt;
    }
    @Override
    public void run() {
        for (CVM cvm : CVM.CORES) {
            cvm.interupt(interupt);
        }
    }

}