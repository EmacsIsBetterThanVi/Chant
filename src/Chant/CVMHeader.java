package Chant;

import java.util.Arrays;

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
         0x00012, 1 - Number of SysOps fields
         0x00013, 2 - Start of SysOps fields
         0x00015, 1 - Number of Micro fields
         0x00016, 2 - Start of Micro fields
         0x00018, ? - BIOS field(A string containing the file path to the BIOS file)
         0x?????, ? - First IO field
              +0, 1 - Length of path field
              +1, ? - Path field
         0x?????, ? - Second IO field
         0x?????, ? - First SysOp field
              +0, 1 - Length of path field
              +1, ? - Path field
         0x?????, ? - Second SysOp field
         0x?????, ? - First MICRO field
              +0, 1 - Length of path field
              +1, ? - Path field
         0x?????, ? - Second MICRO field
         etc.
         */
public record CVMHeader(boolean ExtraPointerSpace, int ExtraCores, long ExtraMemoryLength, String BIOS, String[] peripherals, String[] SysOps, String[] MicroPeripherals) {
    public static CVMHeader readHeader(byte[] data){
        String BIOS = new String(Arrays.copyOfRange(data, 9, 9+data[5]));
        long ExtraMemoryLength=data[1]+(data[2]<<8)+(data[3]<<16)+(data[4]<<24);
        if (ExtraMemoryLength>4277141504L) ExtraMemoryLength= 0xFEF00000L;
        String[] peripheralList = new String[data[6]];
        int currentAddress = data[7]+(data[8]<<8);
        for (int i=0; i<data[6]; i++){
            int len = data[currentAddress];
            currentAddress++;
            String peripheralName = "";
            for (int j=0; j<len; j++){
                peripheralName=peripheralName+(char)data[currentAddress];
                currentAddress++;
            }
        }
        int ExtraCores= (data[0]>>>1)&31;
        return new CVMHeader((data[0]&1)==1, ExtraCores, ExtraMemoryLength, BIOS, peripheralList, new String[]{}, new String[]{});
    }
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder(String.format("Extra Pointer Space: %s\nNumber of Cores: %d\nLength of Extended Memory: %d\nBIOS FILE: %s\nPeripherals to attach: ", this.ExtraPointerSpace ? "Yes" : "No", this.ExtraCores + 1, this.ExtraMemoryLength, this.BIOS));
        for (String peripheral : this.peripherals) {
            tmp.append("\n").append(peripheral);
        }
        tmp.append("\nExtra Ops: ");
        for (String sysOp : this.SysOps) {
            tmp.append("\n").append(sysOp);
        }
        tmp.append("\nMicro Peripherals: ");
        for (String microPeripheral : this.MicroPeripherals){
            tmp.append("\n").append(microPeripheral);
        }
        return tmp.toString();
    }
    public byte[] header(){
        int IOstart = this.BIOS.length()+9;
        int length = IOstart;
        for (String string : this.peripherals) {
            length+=1+string.length();
        }
        byte[] tmp = new byte[3+length];
        tmp[0] = (byte)(length%256);
        tmp[1] = (byte)((length>>>8)%256);
        tmp[2] = (byte)((length>>>16)%256);
        tmp[3] = (byte)((this.ExtraCores<<1)+(ExtraPointerSpace ? 1: 0));
        tmp[4] = (byte)(this.ExtraMemoryLength%256);
        tmp[5] = (byte)((this.ExtraMemoryLength>>>8)%256);
        tmp[6] = (byte)((this.ExtraMemoryLength>>>16)%256);
        tmp[7] = (byte)((this.ExtraMemoryLength>>>24)%256);
        tmp[8] = (byte)(this.BIOS.length());
        tmp[9] = (byte)(this.peripherals.length);
        tmp[10] = (byte)(IOstart%256);
        tmp[11] = (byte)(IOstart>>>8);
        int addr=12;
        for (int i=0; i<this.BIOS.length(); i++){
            tmp[addr] = (byte)this.BIOS.codePointAt(i);
            addr++;
        }
        for (String string: this.peripherals){
            tmp[addr]=(byte)string.length();
            addr++;
            for (int i=0; i<string.length(); i++){
                tmp[addr]=(byte)string.codePointAt(i);
                addr++;
            }
        }
        return tmp;
    }
}
