package Chant;

public abstract class MicroPeripheral{
    public byte ID;
    public byte[] unchanged(byte cmd, byte arg1, byte arg2, byte arg3, byte arg4){return new byte[]{this.ID, cmd, arg1, arg2, arg3, arg4};}
    public MicroPeripheral(int ID){this.ID=(byte)ID;}
    public abstract byte[] refresh(byte cmd, byte arg1, byte arg2, byte arg3, byte arg4);
}