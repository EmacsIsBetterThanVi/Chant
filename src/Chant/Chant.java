package Chant;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Chant {
    public static final String versionString = "0.2.0";
    private static void printheaderhelp(){
        System.out.println("Run only the header generation module.\nNOTE: Usage of the header module should only be done by advanced users");
        System.out.println("usage: chant --header [options] <header info>");
        System.out.println("options:\n\t-f <file>, --file <file>\tWrites the data to a file.\n\t-h, --help\t\t\tDisplays this message and exits");
        System.out.println("\t-a, --amend\t\t\tAmends the provided file to have new options\n\t-c <file>, --copy <file>\tCopies the header of a file(Ignore header info)");
        System.out.println("\t-d, --dump\t\t\tPrints the header info of the target file");
        System.out.println("Header Info is structured as follows:\n<Include extra pointer space[y/N]> <Number of extra cores to include> <Extra Memory space in bytes> <BIOS file> [Peripheral files]");
    }
    private static void printhelp(){
        System.out.println("Chant compiler version "+versionString+"\nusage: chant [options] <file>");
        System.out.println("options:\n\t--header\t\t\tInvoke the header generation module without running the compiler(see --header -h)");
    }
    public static void main(String[] args) {
        if (args.length==0){
            printhelp();
            return;
        }
        if (args[0].equals("--header")){
            int base=1;
            String fileString="";
            String source="";
            boolean amend=false;
            boolean dump=false;
            for (int i = 1; i<args.length; i++){
                switch (args[i]) {
                    case "-f":
                    case "--file":
                        base+=2;
                        i++;
                        fileString=args[i];
                        break;
                    case "-a":
                    case "--amend":
                        dump=false;
                        amend=true;
                        base++;
                        break;
                    case "-c":
                    case "--copy":
                        dump=false;
                        base+=2;
                        i++;
                        source=args[i];
                        break;
                    case "-h":
                    case "--help":
                        printheaderhelp();
                        return;
                    case "-d":
                    case "--dump":
                        dump=true;
                        break;
                    default:
                        if (args[i].charAt(0)=='-'){
                            System.err.println("Unknown option: "+args[i]);
                            printheaderhelp();
                            System.exit(3);
                        }
                }
            }
            CVMHeader cvmHeader;
            try {
                if (source.length()==0) 
                    if (dump) {
                        if (fileString.length()==0) {
                            System.err.println("Dump requires a file to output");
                            System.exit(3);
                        }
                        FileInputStream fileInputStream = new FileInputStream(fileString);
                        int start = fileInputStream.read();
                        start+=fileInputStream.read()<<8;
                        start+=fileInputStream.read()<<16;
                        cvmHeader=CVMHeader.readHeader(fileInputStream.readNBytes(start));
                        fileInputStream.close();
                        System.out.println(cvmHeader);
                        return;
                    } else 
                        cvmHeader = new CVMHeader((args[base].toUpperCase().charAt(0)=='Y'), Integer.parseInt(args[base+1]), Integer.parseInt(args[base+2]), args[base+3], Arrays.copyOfRange(args, base+4, args.length));
                else {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(source);
                        int start = fileInputStream.read();
                        start+=fileInputStream.read()<<8;
			            start+=fileInputStream.read()<<16;
                        cvmHeader=CVMHeader.readHeader(fileInputStream.readNBytes(start));
                        fileInputStream.close();
                    } catch (IOException e){
                        System.err.println("Error reading file, it either does not exist, or it is malformated");
                        System.exit(2);
                        return;
                    }
                }
            } catch (Exception e){
                System.err.println("Improper formating for data");
                printheaderhelp();
                System.exit(3);
                return; //Never reached, but it provides a safety mechanism for the compiler
            }
            if (!amend && fileString.length()==0){
                for (byte b : cvmHeader.header()) {
                    System.out.printf("%x ", b, b);
                }
            } else if (amend){
                if (fileString.length()==0){
                    System.err.println("Amend requires a file to amend");
                    System.exit(1);
                } else {
                    try {
                        FileInputStream Ifile = new FileInputStream(fileString);
                        int start = Ifile.read();
			            start+=Ifile.read()<<8;
			            start+=Ifile.read()<<16;
			            Ifile.readNBytes(start);
                        byte[] NotHeader = Ifile.readAllBytes();
                        Ifile.close();
                        FileOutputStream fileOutputStream = new FileOutputStream(fileString);
                        fileOutputStream.write(cvmHeader.header());
                        fileOutputStream.write(NotHeader);
                        fileOutputStream.close();
                    } catch (IOException e){
                        System.err.println("Error reading file, it either does not exist, or it is malformated");
                        System.exit(2);
                    }
                    
                }
            } else {
                try {
                FileOutputStream fileOutputStream = new FileOutputStream(fileString);
                fileOutputStream.write(cvmHeader.header());
                fileOutputStream.close();
                } catch (IOException e){
                    System.err.println("Error writing to file");
                    System.exit(2);
                }

            }
        } else {
            compilerMain();
        } 
    }
    public static void compilerMain(){
        printhelp();
	//TODO write compiler
    }
}
