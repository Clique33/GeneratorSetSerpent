/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis;

/**
 * Classe para análise de propagação de operações bit-a-bit.
 *
 * @author ´Gabriel
 */
public class Bit {
    
    private final boolean value;
    private final String generator;   // Operação que gerou esse bit (se houver)
    private final Bit op1;            // Operando 1 que gerou este bit (se houver)
    private final Bit op2;            // Operando 2 que gerou este bit (se houver)
    private String id;                // Símbolo identificador.Só terá se não
                                      // tiver sido gerado a partir de uma operação
    
    public static void main(String[] args){
        
        Bitstring w120,w121,w122,w123,w124,w125,w126,w127,w128,w129,w130,w131;
        Bitstring phi = new Bitstring(0x9e3779b9,"phi");
        
        System.out.println("------------------- Begin Backward Key -------------------");
        w131 = new Bitstring("w131",32);
        w130 = new Bitstring("w130",32);
        w129 = new Bitstring("w129",32);
        w128 = new Bitstring("w128",32);
        w127 = new Bitstring("w127",32);
        w126 = new Bitstring("w126",32);
        w125 = new Bitstring("w125",32);
        w124 = new Bitstring("w124",32);
        
        w123 = w131.leftRotation(-11);
        w123 = w123.xor(w128).xor(w126).xor(w130).xor(phi).xor(new Bitstring(131,"i"));
        
        w122 = w130.leftRotation(-11);
        w122 = w122.xor(w127).xor(w125).xor(w129).xor(phi).xor(new Bitstring(130,"i"));
        
        w121 = w129.leftRotation(-11);
        w121 = w121.xor(w126).xor(w124).xor(w128).xor(phi).xor(new Bitstring(129,"i"));
        
        w120 = w128.leftRotation(-11);
        w120 = w120.xor(w125).xor(w123).xor(w127).xor(phi).xor(new Bitstring(128,"i"));
        
        System.out.println(w123);
        System.out.println(w122);
        System.out.println(w121);
        System.out.println(w120);
        
        System.out.println("-------------------- End Backward Key --------------------");
        
        
        System.out.println("------------------- Begin Forward Key -------------------");
        w120 = new Bitstring("w120",32);
        w121 = new Bitstring("w121",32);
        w122 = new Bitstring("w122",32);
        w123 = new Bitstring("w123",32);
        w124 = new Bitstring("w124",32);
        w125 = new Bitstring("w125",32);
        w126 = new Bitstring("w126",32);
        w127 = new Bitstring("w127",32);
        
        w128 = w120.xor(w123).xor(w125).xor(w127).xor(phi).xor(new Bitstring(128,"i"));
        w128 = w128.leftRotation(11);
        
        w129 = w121.xor(w124).xor(w126).xor(w128).xor(phi).xor(new Bitstring(129,"i"));
        w129 = w129.leftRotation(11);
        
        w130 = w122.xor(w125).xor(w127).xor(w129).xor(phi).xor(new Bitstring(130,"i"));
        w130 = w130.leftRotation(11);
        
        w131 = w123.xor(w126).xor(w128).xor(w130).xor(phi).xor(new Bitstring(131,"i"));
        w131 = w131.leftRotation(11);
        
        System.out.println(w120);
        System.out.println(w128);
        System.out.println(w129);
        System.out.println(w130);
        System.out.println(w131);
        
        System.out.println("-------------------- End Forward Key --------------------");
        
    }
    
    public Bit(int value) {
        this.value = value != 0;
        this.generator = "";
        this.op1 = null;
        this.op2 = null;
        this.id = this.value ? "1":"0";
        
    }
    public Bit(boolean value) {
        this.value = value;
        this.generator = "";
        this.op1 = null;
        this.op2 = null;
        this.id = this.value ? "1":"0";
    }
    public Bit(int value, String id) {
        this.value = value != 0;
        this.generator = "";
        this.op1 = null;
        this.op2 = null;
        this.id = id;
    }
    public Bit(boolean value, String id) {
        this.value = value;
        this.generator = "";
        this.op1 = null;
        this.op2 = null;
        this.id = id;
    }
    public Bit(Bit b) {
        this.value = b.value;
        this.generator = b.generator;
        this.op1 = b.op1;
        this.op2 = b.op2;
        this.id = b.id;
    }

    private Bit(boolean value, String generator, Bit op1, Bit op2, String id) {
        this.value = value;
        this.generator = generator;
        this.op1 = op1;
        this.op2 = op2;
        this.id = id;
    }
    private Bit(boolean value, String generator, Bit op1, String id) {
        this.value = value;
        this.generator = generator;
        this.op1 = op1;
        this.op2 = null;
        this.id = id;
    }

    public String getGenerator(){
        if(!"".equals(this.id)){
            return this.id;
        }
        if(this.op2!=null)
            return "("+this.op1.getGenerator() + this.generator + this.op2.getGenerator()+")";
        return this.generator + this.op1.getGenerator();
    }
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    
    public Bit sum(Bit b){
        return this.value && b.value ? 
                new Bit(false, "+", this, b, ""): 
                new Bit(this.value ^ b.value, "+", this, b, "");
    }

    public boolean isValue() {
        return value;
    }
    public Bit and(Bit b){
        return new Bit(this.value && b.value, "&", this, b, "");
    }
    public Bit or(Bit b){
        return new Bit(this.value || b.value, "|", this, b, "");
    }
    public Bit xor(Bit b){
        return new Bit(this.value ^ b.value, "^", this, b, "");
    }
    public Bit not(){
        return new Bit(!this.value, "~", this, "");
    }

    @Override
    public String toString() {
        return this.value ? "1":"0";
    }
    
    
    
}
