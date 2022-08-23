/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cifra;

import core.ByteArray;
import java.util.ArrayList;

/**
 *
 * @author ´Gabriel
 */
public abstract class Cipher {
    public static final int NUM_BYTES_IN_256_BITS = 256 / Byte.SIZE;
    public static final int NUM_BYTES_IN_128_BITS = 128 / Byte.SIZE;
    public static int BLOCK_SIZE_IN_BYTES = 16;
    public static int KEY_SIZE_IN_BYTES = 32;
    public static int ROUND_KEY_SIZE_IN_BYTES = 16;
    public static int NUM_ROUNDS = 32;
    public static int NUM_KEYS = 33;
    public static int EXPANDED_KEY_SIZE = (NUM_ROUNDS + 1) * NUM_BYTES_IN_128_BITS;
    
    private ByteArray secretKey;
    private ByteArray secretKeyNoSbox;
    private int roundOfFirstKeyToBeApplied;
    
    public Cipher(ByteArray key,int round){
        setKey(key,round);
    }
    
    public Cipher(){
    }
    
    public Cipher Reset(ByteArray key,int round){
        setKey(key,round);
        return this;
    }
    
    public Cipher Reset(ByteArray key){
        setKey(key);
        return this;
    }
         
    public ByteArray getExpandedKey(){
        return secretKey;
    }
        
    public ByteArray getExpandedKeyNoSbox(){
        return secretKeyNoSbox;
    }
	
    public void setRoundOfFirstKeyToBeApplied(int round){
        roundOfFirstKeyToBeApplied = round;
    }
    
    public abstract int getBLOCK_SIZE_IN_BYTES();
    public abstract int getKEY_SIZE_IN_BYTES();
    public abstract int getROUND_KEY_SIZE_IN_BYTES();
    public abstract int getNUM_ROUNDS();
    public abstract int getNUM_KEYS();
        
    /**
    * This setKey expands a pair of round keys of 128 bits each, of rounds
    * 0 and 1.
    * 
    * @param key is the pair of keys that will be used to expand alll others
    */
    public void setKey(ByteArray key){
        setKey(key, 0);
    }
        
    /**
    * This setKey expands a pair of round keys of 128 bits each, of rounds
    * 'round' and 'round'+1.
    * 
    * @param key is the pair of keys that will be used to expand alll others
    * @param round is the first round of the pair of keys
    */
    public abstract void setKey(ByteArray key, int round);
    
    public abstract ByteArray getExpandedKey(ByteArray key, int round1);
    
    public abstract ArrayList<ByteArray> encryptFullSavingStates(ByteArray block,int fromState,int toState);
    
    public abstract ArrayList<ByteArray> encryptRoundsFromStatesSavingStates(ByteArray block, int fromState, int toState);
    
    public abstract ArrayList<ByteArray> encryptRoundsBackwardsFromStatesSavingStates(ByteArray block, int fromState, int toState);
    
    public abstract ByteArray encryptRounds(ByteArray block, int fromRound, int toRound);
    
    public abstract ArrayList<ByteArray> encryptFullSavingStates(ByteArray block);
    
}
