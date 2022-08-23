/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis;

import java.util.InputMismatchException;

/**
 *
 * @author ´Gabriel
 */
public class Bitstring {
    private Bit[] block;
    private String id;

    public Bitstring(String id, int size) {
        this.block = new Bit[size];
        this.id = id;
        for (int i = 0; i < size; i++)
            this.block[i] = new Bit(0, id+"["+i+"]");
        
    }
    public Bitstring(int value, String id) {
        this.block = new Bit[32];
        this.id = id;
        
        for (int i = 0; i < 32; i++) 
            block[i] = new Bit((value>>i)&1);
    }
    public Bitstring(int[] value, String id) {
        this.block = new Bit[32*value.length];
        this.id = id;
        
        for (int i = 0; i < 32*value.length; i++)
            block[i] = new Bit((value[i/32]>>i)&1);
    }
    public Bitstring(Bit[] block) {
        this.block = block;
        this.id = "";
    }
    public Bitstring(Bit[] block, String id) {
        this.block = block;
        this.id = id;
    }
    
    public Bitstring sum(Bitstring b){
        if(b.block.length != this.block.length)
            throw new InputMismatchException(this.id + 
                    " has length " + this.block.length +
                    " while " + b.id + " has length " +
                    b.block.length + ". They must be equal.");
        
        Bit[] result = new Bit[block.length];
        
        for (int i = 0; i < block.length; i++) 
            result[i] = block[i].sum(b.block[i]);
        
        return new Bitstring(result);
    }
    
    public Bitstring and(Bitstring b){
        if(b.block.length != this.block.length)
            throw new InputMismatchException(this.id + 
                    " has length " + this.block.length +
                    " while " + b.id + " has length " +
                    b.block.length + ". They must be equal.");
        
        Bit[] result = new Bit[block.length];
        
        for (int i = 0; i < block.length; i++) 
            result[i] = block[i].and(b.block[i]);
        
        return new Bitstring(result);
    }
    
    public Bitstring or(Bitstring b){
        if(b.block.length != this.block.length)
            throw new InputMismatchException(this.id + 
                    " has length " + this.block.length +
                    " while " + b.id + " has length " +
                    b.block.length + ". They must be equal.");
        
        Bit[] result = new Bit[block.length];
        
        for (int i = 0; i < block.length; i++) 
            result[i] = block[i].or(b.block[i]);
        
        return new Bitstring(result);        
    }
    
    public Bitstring xor(Bitstring b){
        if(b.block.length != this.block.length)
            throw new InputMismatchException(this.id + 
                    " has length " + this.block.length +
                    " while " + b.id + " has length " +
                    b.block.length + ". They must be equal.");
        
        Bit[] result = new Bit[block.length];
        
        for (int i = 0; i < block.length; i++) 
            result[i] = block[i].xor(b.block[i]);
        
        return new Bitstring(result);        
    }
    
    public Bitstring not(){        
        Bit[] result = new Bit[block.length];
        
        for (int i = 0; i < block.length; i++) 
            result[i] = block[i].not();
        
        return new Bitstring(result);        
    }
    
    public Bitstring leftShift(int amount){
        if(amount > block.length) amount = block.length;
        if(amount < -block.length) amount = -block.length;
        
        Bit[] result = new Bit[block.length];
        
        if(amount < 0){
            for (int i = amount+block.length-1; i >= 0; i--)
                result[i] = block[i-amount];
            for (int i = block.length-1; i >= amount+block.length; i--)
                result[i] = new Bit(0);
        }else{
            for (int i = amount; i < block.length; i++) 
                result[i] = block[(i-amount)%block.length];
            for (int i = 0; i < amount; i++) 
                result[i] = new Bit(0);
        }
        return new Bitstring(result);
        
    }
    public Bitstring leftRotation(int amount){
        if(amount > block.length) amount = block.length;
        if(amount < -block.length) amount = -block.length;
        if(amount > 0) amount -= block.length;
        
        Bit[] result = new Bit[block.length];
        
        for (int i = 0; i < block.length; i++){
            result[i] = block[(i-amount)%block.length];
        } 
        
        return new Bitstring(result);
    }

    @Override
    public String toString() {
        String result = "||";
        
        for (int i = block.length-1; i >= 0; i--) {
            result += block[i].getGenerator() + "||";
        }
        
        return result;
    }
    
    
    
}
