/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cifra;
import core.ByteArray;
import core.Difference;
import core.Differential;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author ´Gabriel
 */
public class Main {
    public static void main1(String[] args){    
        Object[] attack = createAttack();
        
        ArrayList<ByteArray> activeNibblesDelta = (ArrayList<ByteArray>)attack[0];
        ArrayList<ByteArray> activeNibblesNabla = (ArrayList<ByteArray>)attack[1];
        
        Differential delta = (Differential)attack[3];
        Differential nabla = (Differential)attack[4];
        
        ArrayList<ByteArray> keyActiveNibblesDelta = (ArrayList<ByteArray>)attack[5];
        ArrayList<ByteArray> keyActiveNibblesNabla = (ArrayList<ByteArray>)attack[6];
        
        System.out.println("Active Nibbles of the Delta key");
        for (int i = 0; i < keyActiveNibblesDelta.size(); i++) {
            System.out.println("K"+i+" = "+keyActiveNibblesDelta.get(i));
        }
        
        System.out.println("Active Nibbles of the Nabla key");
        for (int i = 0; i < keyActiveNibblesNabla.size(); i++) {
            System.out.println("K"+i+" = "+keyActiveNibblesNabla.get(i));
        }
        
        Serpent cifra1 = new Serpent(delta.firstSecretKey, 31);
        Serpent cifra2 = new Serpent(delta.firstSecretKey, 31);
        
        ByteArray state1 = new ByteArray(16);
        ByteArray state2 = new ByteArray(16);
        
        ArrayList<ByteArray> activeNibbles = new ArrayList<>();
        ArrayList<ByteArray> currNibbles;
        
        for (int i = 0; i < 91; i++) {
            activeNibbles.add(new ByteArray(16));
        }
            
        int fromState = 75;
        int toState = 90;
        
        for (int i = 1; i < 16; i++) {
            state2.setNibble(31, i);
        
            ArrayList<ByteArray> ida1 = cifra1.encryptRoundsFromStatesSavingStates(state1,fromState,toState);
            ArrayList<ByteArray> ida2 = cifra2.encryptRoundsFromStatesSavingStates(state2,fromState,toState);
            ArrayList<ByteArray> volta1 = cifra1.encryptRoundsBackwardsFromStatesSavingStates(state1,fromState,0);
            ArrayList<ByteArray> volta2 = cifra2.encryptRoundsBackwardsFromStatesSavingStates(state2,fromState,0);
            
            ByteArray aux;
            //System.out.println("");
            for (int j = 0; j < volta1.size(); j++) {
                aux = volta1.get(j).clone().xor(volta2.get(j));
                for (int k = 0; k < 32; k++) {
                    if(activeNibbles.get(j).getNibble(k) == 0 && aux.getNibble(k) !=0)
                        activeNibbles.get(j).setNibble(k, 0xA);                
                }
                //System.out.println("#"+(j)+" = "+aux);
            }
            for (int j = 0; j < ida1.size(); j++) {
                aux = ida1.get(j).clone().xor(ida2.get(j));
                for (int k = 0; k < 32; k++) {
                    if(activeNibbles.get((fromState+j)).getNibble(k) == 0 && aux.getNibble(k) !=0)
                        activeNibbles.get((fromState+j)).setNibble(k, 0xA);                
                }
                //System.out.println("#"+(fromState+j)+" = "+aux);
            }
        }
        
//---------------Manually altering nibbles influenced by the Delta and Nabla Differentials-------------------------
        
        for (int i = 84; i <= toState; i++) //Influenced by Delta Differentials in states 84 to 'toState'
            activeNibbles.set(i,activeNibblesDelta.get(i));
        
        cifra1 = new Serpent(nabla.firstSecretKey, 15);
        
        ByteArray baseKey = nabla.firstSecretKey;
        ArrayList<Integer> activeNibblesFromKeyDifference = nabla.keyDifference.getActiveNibbles();
        int randomState = 10;
        
        for (int k = 1; k < 16; k++) {
            for (int l = 1; l < 16; l++) {
                ByteArray P1 = new ByteArray(16);
                P1.randomize();
                ByteArray P2 = P1.clone();
                
                ByteArray diff = nabla.keyDifference;
                diff.setNibble(activeNibblesFromKeyDifference.get(0), k^baseKey.getNibble(activeNibblesFromKeyDifference.get(0)));
                diff.setNibble(activeNibblesFromKeyDifference.get(1), l^baseKey.getNibble(activeNibblesFromKeyDifference.get(1)));
                cifra2 = new Serpent(nabla.firstSecretKey.clone().xor(diff), 15);
                
                ArrayList<ByteArray> aux1 = cifra1.encryptFullSavingStates(P1, 0, randomState);
                ArrayList<ByteArray> aux2 = cifra2.encryptFullSavingStates(P2, 0, randomState);

                for (int i = 0; i < aux1.size(); i++) {
                    ByteArray aux;
                    for (int j = 0; j < Serpent.BLOCK_SIZE_IN_BYTES*2; j++) {
                        aux = aux1.get(i).clone().getDifference(aux2.get(i));
                        if(activeNibbles.get(i).getNibble(j) != 0xb){
                            if(aux.getNibble(j) == 0){
                                activeNibbles.get(i).setNibble(j, 0);
                            }else
                                activeNibbles.get(i).setNibble(j, 0xb);
                        }
                    }
                }
            }
        }
        
        for (int i = 0; i <= randomState; i++) 
            for (int j = 0; j < Serpent.BLOCK_SIZE_IN_BYTES*2; j++) 
                if(activeNibbles.get(i).getNibble(j) == 0xb) activeNibbles.get(i).setNibble(j, 0xA);
//-----------------------------------------------------------------------------------------------------------------
                
        int numActiveSboxes = 0;
        int numActiveSboxesForward = 0;
        int numActiveSboxesBackward = 0;
        int numActiveSboxesForwardKey = 0;
        int numActiveSboxesBackwardkey = 0;
        System.out.println("Nibbles that influence variable v = ");
        for (int i = 0; i < activeNibbles.size(); i++) {
            System.out.println("#"+i+" = "+activeNibbles.get(i));
            if(i <= fromState && i%3 == 2){// Ida e apenas os estados que foram afetados por S-boxes
                for (int j = 0; j < activeNibbles.get(i).length()*2; j++){
                    if(activeNibbles.get(i).getNibble(j) != 0){
                        numActiveSboxes++;
                        numActiveSboxesForward++;
                    }
                }
                //System.out.println("Ida, "+numActiveSboxes);
            }
            if(i <= fromState && i%3 == 1){// Ida e apenas as subchaves que foram afetadas por S-boxes
                for (int j = 0; j < keyActiveNibblesNabla.get(i/3).length()*2; j++){
                    if(keyActiveNibblesNabla.get(i/3).getNibble(j) != 0 && activeNibbles.get(i).getNibble(j) != 0){
                        numActiveSboxes++;
                        numActiveSboxesForwardKey++;
                    }
                }
                //System.out.println("Ida, "+numActiveSboxes);
            }
            if(i >= fromState && i%3 == 1){// Volta e apenas os estados que foram afetados por S-boxes
                for (int j = 0; j < activeNibbles.get(i).length()*2; j++){
                    if(activeNibbles.get(i).getNibble(j) != 0){
                        numActiveSboxes++;
                        numActiveSboxesBackward++;
                    }
                }
                //System.out.println("Volta, "+numActiveSboxes);
            }
            if(i >= fromState && i%3 == 0){// Volta e apenas as subchaves que foram afetadas por S-boxes
                //System.out.print("state: " + i + " subkey: " + i/3);
                //int aux = 0;
                for (int j = 0; j < keyActiveNibblesDelta.get(i/3).length()*2; j++){
                    if(keyActiveNibblesDelta.get(i/3).getNibble(j) != 0 && activeNibbles.get(i).getNibble(j) != 0){ 
                        numActiveSboxes++;
                        numActiveSboxesBackwardkey++;
                        //aux++;
                    }
                }
                //System.out.println(" total active nibbles of key: " + aux);
                //System.out.println("Volta, "+numActiveSboxes);
            }
        }
        System.out.println("#Active Sboxes Forward = "+numActiveSboxesForward);
        System.out.println("#Active Sboxes Forward Key = "+numActiveSboxesForwardKey);
        System.out.println("#Active Sboxes Backward = "+numActiveSboxesBackward);
        System.out.println("#Active Sboxes Backward Key = "+numActiveSboxesBackwardkey);
        System.out.println("#Active Sboxes = "+numActiveSboxes+"/2080 = "+((double)numActiveSboxes/2080*100)+"% of the whole cipher, which is");
        System.out.println("(2^{12}*"
                +((double)numActiveSboxes/2080)+" = "+(((double)numActiveSboxes/2080)*4098)+" = 2^{"+Math.log(((double)numActiveSboxes/2080)*4098)/Math.log(2)+"})");
        
        System.out.println("C_attack = 2^{k-3d}*(C_biclique + C_precomp + C_recomp + C_falpos)");
        System.out.println("C_attack = 2^{244}*(((2^{4}+2^{8})*(5/96)) + (2^{4}*(75/96) + 2^{8}*(16/96)) + 2^{"+Math.log(((double)numActiveSboxes/2080)*4098)/Math.log(2)+"} + 2^{12-4})");
        System.out.println("C_attack = 2^{244}*(2^{3.824428435416545} + 2^{5.785724906086061} + 2^{"+Math.log(((double)numActiveSboxes/2080)*4098)/Math.log(2)+"} + 2^8)");
        System.out.println("C_attack = 2^{"+(244+Math.log(Math.pow(2, 3.824428435416545)+Math.pow(2, 5.785724906086061)+Math.pow(2, Math.log(((double)numActiveSboxes/2080)*4098)/Math.log(2))+Math.pow(2, 8))/Math.log(2))+"}");
        System.out.printf("C_attack ~ 2^{%.2f}\n",(244+Math.log(Math.pow(2, 3.824428435416545)+Math.pow(2, 5.785724906086061)+Math.pow(2, Math.log(((double)numActiveSboxes/2080)*4098)/Math.log(2))+Math.pow(2, 8))/Math.log(2)));
        
    }
    
    public static Object[] createAttack(){
        boolean debug = false;
        int roundDelta = 31;
        int roundNabla = 30;
        int debugDiffs = 0;
        ArrayList<ByteArray> activeNibblesDelta = new ArrayList<>();
        ArrayList<ByteArray> activeNibblesNabla = new ArrayList<>();
        ArrayList<ByteArray> activeNibblesDifference = new ArrayList<>();
        
        Differential deltaDiff = null;
        Differential nablaDiff = null;
        
        for (int i = 0; i < 97; i++) {
            activeNibblesDelta.add(new ByteArray(16));
            activeNibblesNabla.add(new ByteArray(16));
        }
        
        Object[] lowDiff = getLowestDiffBasic(false,roundDelta,28);
        
        ArrayList<Integer> activeNibbles = (ArrayList<Integer>)lowDiff[3];
        Difference keyDiff = (Difference)lowDiff[1];
            
        for (int i :activeNibbles) {
            for (int j = 1; j < 16; j++) {
                keyDiff.setNibble(i, j);
                deltaDiff = getDeltaDifferential(debugDiffs, roundDelta, keyDiff);
                if(debug) System.out.println(deltaDiff.keyDifference);
                for (int k = 0; k < deltaDiff.stateDifferences.size(); k++) {
                    for (int l = 0; l < deltaDiff.stateDifferences.get(k).length()*2; l++) {
                        if(deltaDiff.stateDifferences.get(k).getNibble(l) != 0 && activeNibblesDelta.get(k).getNibble(l)==0){
                            activeNibblesDelta.get(k).setNibble(l, 0xA);
                        }  
                    }
                }

                ByteArray aux = new ByteArray(32);
                for (int k = 1; k < 16; k++) {
                    for (int l = 1; l < 16; l++) {
                        aux.setNibble(5, k);
                        aux.setNibble(26, l);
                        if(debug) System.out.println(aux);
                        nablaDiff = getNablaDifferential(debugDiffs, roundNabla, new Difference(aux));
                        if(debug) System.out.println(nablaDiff.keyDifference);
                        
                        for (int m = 0; m < nablaDiff.stateDifferences.size(); m++) {
                            for (int n = 0; n < nablaDiff.stateDifferences.get(m).length()*2; n++) {
                                if(nablaDiff.stateDifferences.get(m).getNibble(n) != 0 && activeNibblesNabla.get(m).getNibble(n)==0){
                                    activeNibblesNabla.get(m).setNibble(n, 0xB);
                                }                                
                            }
                        }

                    }
                }
            }
        }
        System.out.println("\nDelta Differentials =");
        for (int i = 0; i < activeNibblesDelta.size(); i++) {
            System.out.println(activeNibblesDelta.get(i));
        }
        System.out.println("\nNabla Differentials =");
        for (int i = 0; i < activeNibblesNabla.size(); i++) {
            System.out.println(activeNibblesNabla.get(i));
        }
        System.out.println("\nSee independence (a is for Delta, b is for Nabla, 1 is for both and 0 is none) =");
        for (int i = 0; i < activeNibblesNabla.size(); i++) {
            activeNibblesDifference.add(activeNibblesNabla.get(i).clone().xor(activeNibblesDelta.get(i)));
            if (i == 0) System.out.println("#"+i+" P\t="+activeNibblesDifference.get(i));
            else if (i%3 == 1) System.out.println("#"+i+" AK"+((i+2)/3)+"\t="+activeNibblesDifference.get(i));
            else if (i%3 == 2) System.out.println("#"+i+" S"+((i+1)/3)+"\t\t="+activeNibblesDifference.get(i));
            else if (i%3 == 0 && i/3 !=32) System.out.println("#"+i+" L"+(i/3)+"\t\t="+activeNibblesDifference.get(i));
            else System.out.println("#"+i+" AK33 = C\t="+activeNibblesDifference.get(i));
        }
        
        //-----------------Getting the active nibbles of Nabla key----------------------------
        ArrayList<Integer> indexActiveNibbles = nablaDiff.keyDifference.getActiveNibbles();
        ArrayList<ByteArray> activeNibblesNablaKey = new ArrayList<>();
        for (int i = 0; i < 33; i++) 
            activeNibblesNablaKey.add(new ByteArray(16));
        for (int i = 1; i < 16; i++) {
            for (int j = 1; j < 16; j++) {
                ByteArray key1 = new ByteArray(32);
                ByteArray key2 = key1.clone();
                key2.setNibble(indexActiveNibbles.get(0), i);
                key2.setNibble(indexActiveNibbles.get(1), j);
                
                Serpent cifra1 = new Serpent(key1, roundNabla);
                Serpent cifra2 = new Serpent(key2, roundNabla);
                
                ByteArray keyDifference = cifra1.getExpandedKey(key1, roundNabla).clone().getDifference(cifra2.getExpandedKey(key2, roundNabla));
                for (int k = 0; k < keyDifference.length()*2; k++) {
                    int key = k/32;
                    int nibble = k%32;
                    if (activeNibblesNablaKey.get(key).getNibble(nibble)==0 && keyDifference.getNibble(k)!=0) 
                        activeNibblesNablaKey.get(key).setNibble(nibble,0xA);
                }   
            }
        }
        
        /*System.out.println("Active Nibbles of the Nabla key");
        for (int i = 0; i < activeNibblesNablaKey.size(); i++) {
            System.out.println("#"+i+" = "+activeNibblesNablaKey.get(i));
        }*/
        //------------------------------------------------------------------------------------
        
        Object[] result = {activeNibblesDelta,activeNibblesNabla,activeNibblesDifference,deltaDiff,nablaDiff,lowDiff[4],activeNibblesNablaKey};
        return result;
        
    }
        
    public static Differential getDeltaDifferential(int debug, int round, Difference keyDiff){
        if(debug>0) System.out.println("Diferencial de Chave = \n" + keyDiff);
        
        ByteArray key1 = new ByteArray(Serpent.KEY_SIZE_IN_BYTES);      //Chave Base
        ByteArray key2 = keyDiff.xorDifference(key1);                   //Chave Base 2
        if(debug>1){ 
            System.out.println("Chave 1= \n" + key1);
            System.out.println("Chave 2= \n" + key2);
            System.out.println("Chave 1\\oplus Chave 2= \n" + key1.clone().xor(key2));
        }
        Serpent cifra1 = new Serpent(key1,round);       //Usada para expandir a chave
        Serpent cifra2 = new Serpent(key2, round);      //Usada para expandir a chave 2
        if(debug>0){
            if(debug>1) System.out.println("Chave expandida 1= \n" + cifra1.getExpandedKey());
            if(debug>1) System.out.println("Chave expandida 2= \n" + cifra2.getExpandedKey());
            System.out.println("Chave expandida 1\\oplus Chave expandida 2= \n" + cifra2.getExpandedKey().clone().xor(cifra1.getExpandedKey()));
        }     
        Difference stateDiff = new Difference(Serpent.BLOCK_SIZE_IN_BYTES); //Diferencial dos estados
        
        ByteArray state1 = new ByteArray(Serpent.BLOCK_SIZE_IN_BYTES);      //Estado Base
        state1.randomize();
        ByteArray state2 = stateDiff.xorDifference(state1);                 //Estado Base 2
        if(debug>1){
            System.out.println("Diferencial inicial do estado = \n" + stateDiff);
            System.out.println("Estado 1= \n" + state1);
            System.out.println("Estado 2= \n" + state2);
            System.out.println("Estado 1\\oplus Estado 2= \n" + state1.clone().xor(state2));
        }
        
        if(debug>0){
            ByteArray cypherText1;
            ByteArray cypherText2;
            cypherText1 = cifra1.encryptRounds(state1, 31, 31);
            cypherText2 = cifra2.encryptRounds(state2, 31, 31);
            if(debug>1) System.out.println("Estado 1(31)= \n" + cypherText1);
            if(debug>1) System.out.println("Estado 2(31)= \n" + cypherText2);
            System.out.println("Estado 1(31)\\oplus Estado 2(31)= \n" + cypherText1.clone().xor(cypherText2));
            cypherText1 = cifra1.encryptRounds(state1, 31, 32);
            cypherText2 = cifra2.encryptRounds(state2, 31, 32);
            if(debug>1) System.out.println("Estado 1(32)= \n" + cypherText1);
            if(debug>1) System.out.println("Estado 2(32)= \n" + cypherText2);
            System.out.println("Estado 1(32)\\oplus Estado 2(32)= \n" + cypherText1.clone().xor(cypherText2));
        }
        ArrayList<ByteArray> allStates1 = cifra1.encryptFullSavingStates(state1);
        ArrayList<ByteArray> allStates2 = cifra2.encryptFullSavingStates(state2);
        
        ArrayList<Difference> allStatesDiff = new ArrayList<>();
        ArrayList<Difference> allKeysDiff = new ArrayList<>();
        
        
        for (int i = 0; i < allStates1.size(); i++) 
            allStatesDiff.add(new Difference(allStates1.get(i).clone().xor(allStates2.get(i))));        
        
        if(debug>0){
            System.out.println("Diferencial de estados = ");
            for (int i = 0; i < allStates1.size(); i++) {
                System.out.println(allStates1.get(i).getDifference(allStates2.get(i)));
            }
        }
        Differential result = new Differential(1, 32);
        result.stateDifferences = allStatesDiff;
        result.keyDifferences = Difference.toDifferenceArrayList(cifra1.getExpandedKey().getDifference(cifra2.getExpandedKey()).split(Serpent.ROUND_KEY_SIZE_IN_BYTES));
        result.fromRound = 1;
        result.toRound = Serpent.NUM_ROUNDS;
        result.firstSecretKey = key1;
        result.secondSecretKey = key2;
        result.keyDifference = keyDiff.getDelta();
        result.intermediateStateDifferences = allStatesDiff;
                
        return result;
    } 
        
    public static Differential getNablaDifferential(int debug, int round, Difference keyDiff){
        if(debug>0) System.out.println("Diferencial de Chave = \n" + keyDiff);
        
        ByteArray key1 = new ByteArray(Serpent.KEY_SIZE_IN_BYTES);      //Chave Base
        key1.randomize();
        ByteArray key2 = keyDiff.xorDifference(key1);                   //Chave Base 2
        if(debug>1){
            System.out.println("Chave 1= \n" + key1);
            System.out.println("Chave 2= \n" + key2);
            System.out.println("Chave 1\\oplus Chave 2= \n" + key1.clone().xor(key2));
        }
        
        Serpent cifra1 = new Serpent(key1,round);                       //Usada para expandir a chave
        Serpent cifra2 = new Serpent(key2, round);                      //Usada para expandir a chave 2
        if(debug>0){
            if(debug>1) System.out.println("Chave expandida 1= \n" + cifra1.getExpandedKey());
            if(debug>1) System.out.println("Chave expandida 2= \n" + cifra2.getExpandedKey());
            System.out.println("Chave expandida 1\\oplus Chave expandida 2= \n" + cifra2.getExpandedKey().clone().xor(cifra1.getExpandedKey()));
        }    
        Difference stateDiff = new Difference(Serpent.BLOCK_SIZE_IN_BYTES); //Diferencial dos estados
        
        ByteArray state1 = new ByteArray(Serpent.BLOCK_SIZE_IN_BYTES);      //Estado Base
        state1.randomize();
        ByteArray state2 = stateDiff.xorDifference(state1);                 //Estado Base 2
        if(debug>1){
            System.out.println("Diferencial inicial do estado = \n" + stateDiff);
            System.out.println("Estado 1= \n" + state1);
            System.out.println("Estado 2= \n" + state2);
            System.out.println("Estado 1\\oplus Estado 2= \n" + state1.clone().xor(state2));
        }
        ByteArray cypherText1;
        ByteArray cypherText2;
        cypherText1 = cifra1.encryptRoundsBackwards(state1, 32, 32);
        cypherText2 = cifra2.encryptRoundsBackwards(state2, 32, 32);
        if(debug>0){
            if(debug>1) System.out.println("Estado 1(32)= \n" + cypherText1);
            if(debug>1) System.out.println("Estado 2(32)= \n" + cypherText2);
            System.out.println("Estado 1(32)\\oplus Estado 2(32)= \n" + cypherText1.clone().xor(cypherText2));
        }
        cypherText1 = cifra1.encryptRoundsBackwards(state1, 32, 31);
        cypherText2 = cifra2.encryptRoundsBackwards(state2, 32, 31);
        if(debug>0){
            if(debug>1) System.out.println("Estado 1(31)= \n" + cypherText1);
            if(debug>1) System.out.println("Estado 2(31)= \n" + cypherText2);
            System.out.println("Estado 1(31)\\oplus Estado 2(31)= \n" + cypherText1.clone().xor(cypherText2));
        }
        cifra1.setRoundOfFirstKeyToBeApplied(33);
        cifra2.setRoundOfFirstKeyToBeApplied(33);
        
        ArrayList<ByteArray> allStates1 = cifra1.encryptFullSavingStates(state1);
        ArrayList<ByteArray> allStates2 = cifra2.encryptFullSavingStates(state2);
        
        ArrayList<Difference> allStatesDiff = new ArrayList<>();
        ArrayList<Difference> allKeysDiff = new ArrayList<>();
        
        for (int i = 0; i < allStates1.size(); i++) 
            allStatesDiff.add(new Difference(allStates1.get(i).clone().xor(allStates2.get(i))));        
        
        if(debug>0){
            System.out.println("Diferencial de estados = ");
            for (int i = 0; i < allStates1.size(); i++) {
                System.out.println(allStates1.get(i).getDifference(allStates2.get(i)));
            }
        }
        
        Differential result = new Differential(1, 32);
        result.stateDifferences = allStatesDiff;
        result.keyDifferences = Difference.toDifferenceArrayList(cifra1.getExpandedKey().getDifference(cifra2.getExpandedKey()).split(Serpent.ROUND_KEY_SIZE_IN_BYTES));
        result.fromRound = 1;
        result.toRound = Serpent.NUM_ROUNDS;
        result.firstSecretKey = key1;
        result.secondSecretKey = key2;
        result.keyDifference = keyDiff.getDelta();
        result.intermediateStateDifferences = allStatesDiff;
                
        return result;
    }
    
     /**
     * Retorna, entre outras informações, a diferencial de chave nas rodadas 
     * 'round1' e 'round1' + 1 que ativam a menor quantidade de bits nas rodadas
     * 'comparableRound' e ('comparableRound'+1).
     * 
     * @param debug seta se os prints de teste serão feitos ou não.
     * @param round1 é o primeiro índice das duas chaves consecutivas que serão
     * testadas. O máximo é 31 (Chaves variam de 0 a 32).
     * @param comparableRound é a rodada utilizada para checar a quantidade de 
     * nibbles ativos na chave. Não pode ser maior que 31.
     * 
     * @return 
     * (int)Object[0] é a quantidade de nibbles afetados, 
     * (Difference)Object[1] é a diferencial que afeta menos bits na chave anterior,
     * (ByteArray)Object[2] é a chave expandida da diferencial de Object[1],
     * (ArrayList>Integer>)Object[3] contém as posições dos nibbles afetados da chave base e
     * (ArrayList>ByteArray>)Object[4] contém os nibbles ativos da chave expandida.
     */
    public static Object[] getLowestDiffBasic(boolean debug, int round1, int comparableRound){
        
        Scanner scanner = new Scanner(System.in);
        Serpent cifra = new Serpent();
        cifra.setKey(new ByteArray(32));
        ByteArray expandedKey = cifra.getExpandedKey();
        
        ByteArray expandedKeyNoSbox = cifra.getExpandedKeyNoSbox();
        
        ByteArray leastActive = null;
        ByteArray leastActiveExpandedKey = null;
        int leastNumActive = 1000;
        int numMaxActNibbles;
        ArrayList<ByteArray> activeNibblesExpandedKey = null;
        
        for (int i = 0; i < 64; i++) {
            ArrayList<ByteArray> activeNibblesExpandedKeyAux = new ArrayList<>();
            for (int j = 0; j < Serpent.NUM_KEYS; j++) {
                activeNibblesExpandedKeyAux.add(new ByteArray(Serpent.BLOCK_SIZE_IN_BYTES));
            }
            numMaxActNibbles = 0;
            ByteArray k30k31 = null;
            ByteArray k30k31_ = null;
            ByteArray k0k1 = null;
            ByteArray k0k1_ = null;
            ByteArray expandedKeyk0k1 = null;
            ByteArray expandedKeyk0k1_ = null;
            ByteArray expandedKeyDifference = null;
            for (int j = 1; j < 16; j++) {
                k0k1 = new ByteArray(32);
                k0k1_ = new ByteArray(32);
                k0k1.copyBytes(expandedKeyNoSbox, 16*round1, 0, 32);
                k0k1_.copyBytes(expandedKeyNoSbox, 16*round1, 0, 32);

                k0k1_.setNibble(i, j^k0k1_.getNibble(i));
                if(debug) System.out.println("K \\oplus K' =\n"+k0k1.getDifference(k0k1_));
                if(debug) System.out.println("");
                
                expandedKeyk0k1 = cifra.getExpandedKey(k0k1,round1);
                expandedKeyk0k1_ = cifra.getExpandedKey(k0k1_,round1);
                expandedKeyDifference = expandedKeyk0k1.getDifference(expandedKeyk0k1_);
                if(debug) System.out.println("expandedKey k31\\oplus expandedKey k`31 =\n"+expandedKeyDifference);
                if(debug) System.out.println("");
                
                for (int k = 0; k < expandedKeyDifference.length()*2; k++) {
                    int nibble_ = k%32;
                    int key = k/32;
                    //Se o nibble atual ainda não tiver sido ativado e ele seja ativo, ative-o
                    if(activeNibblesExpandedKeyAux.get(key).getNibble(nibble_) == 0 && expandedKeyDifference.getNibble(k)!=0)
                        activeNibblesExpandedKeyAux.get(key).setNibble(nibble_,0xA);
                }

                k30k31 = new ByteArray(32);
                k30k31_ = new ByteArray(32);
                k30k31.copyBytes(expandedKeyk0k1, 16*comparableRound, 0, 32);
                k30k31_.copyBytes(expandedKeyk0k1_, 16*comparableRound, 0, 32);
                int numActNibbles = k30k31.getDifference(k30k31_).countNumActiveNibbles();
                if(numActNibbles >= numMaxActNibbles){
                    numMaxActNibbles = numActNibbles;
                    //System.out.println("#Nibble = "+i+", j = "+j);
                }
            }
            if(numMaxActNibbles < leastNumActive){
                leastNumActive = numMaxActNibbles;
                leastActive = k0k1.getDifference(k0k1_);
                leastActiveExpandedKey = expandedKeyk0k1.getDifference(expandedKeyk0k1_);
                activeNibblesExpandedKey = activeNibblesExpandedKeyAux;
            }
            
        }
        
        if(debug){
            System.out.println("least active difference =\n"+leastActive);
            System.out.println("");
            System.out.println("Minimum #active nibbles in K"+(round1+1)+"||K"+(round1+2)+"\\oplus K`"+(round1+1)+"||K`"+(round1+2)+"=\n"+leastNumActive);
            System.out.println("");
        }
        
        ArrayList<Integer> activeNibbles = new ArrayList<>();
        for (int i = 0; i < leastActive.length()*2; i++) {
            if(leastActive.getNibble(i) != 0) activeNibbles.add(i);
        }
        
        Object[] result = {leastNumActive,new Difference(leastActive),leastActiveExpandedKey,activeNibbles,activeNibblesExpandedKey};
        if(debug){
            System.out.println((int)result[0]);
            System.out.println(result[1]);
            System.out.println((ByteArray)result[2]);
            for (int i = 0; i < ((ArrayList<Integer>)result[3]).size(); i++) 
                System.out.println(((ArrayList<Integer>)result[3]).get(i));
            System.out.println("Active Nibbles in the Expanded Key");
            for (int i = 0; i < ((ArrayList<ByteArray>)result[4]).size(); i++) {
                System.out.println(((ArrayList<ByteArray>)result[4]).get(i));
            }
        }
        return result;
    }
    
    public static Differential getDeltaDifferentialORIGINAL(int debug, int round, Difference keyDiff){
        if(debug>0) System.out.println("Diferencial de Chave = \n" + keyDiff);
        
        ByteArray key1 = new ByteArray(Serpent.KEY_SIZE_IN_BYTES);      //Chave Base
        ByteArray key2 = keyDiff.xorDifference(key1);                   //Chave Base 2
        if(debug>1){ 
            System.out.println("Chave 1= \n" + key1);
            System.out.println("Chave 2= \n" + key2);
            System.out.println("Chave 1\\oplus Chave 2= \n" + key1.clone().xor(key2));
        }
        Serpent cifra1 = new Serpent(key1,round);       //Usada para expandir a chave
        Serpent cifra2 = new Serpent(key2, round);      //Usada para expandir a chave 2
        if(debug>0){
            if(debug>1) System.out.println("Chave expandida 1= \n" + cifra1.getExpandedKey());
            if(debug>1) System.out.println("Chave expandida 2= \n" + cifra2.getExpandedKey());
            System.out.println("Chave expandida 1\\oplus Chave expandida 2= \n" + cifra2.getExpandedKey().clone().xor(cifra1.getExpandedKey()));
        }     
        Difference stateDiff = new Difference(Serpent.BLOCK_SIZE_IN_BYTES); //Diferencial dos estados
        
        ByteArray state1 = new ByteArray(Serpent.BLOCK_SIZE_IN_BYTES);      //Estado Base
        state1.randomize();
        ByteArray state2 = stateDiff.xorDifference(state1);                 //Estado Base 2
        if(debug>1){
            System.out.println("Diferencial inicial do estado = \n" + stateDiff);
            System.out.println("Estado 1= \n" + state1);
            System.out.println("Estado 2= \n" + state2);
            System.out.println("Estado 1\\oplus Estado 2= \n" + state1.clone().xor(state2));
        }
        
        if(debug>0){
            ByteArray cypherText1;
            ByteArray cypherText2;
            cypherText1 = cifra1.encryptRounds(state1, 31, 31);
            cypherText2 = cifra2.encryptRounds(state2, 31, 31);
            if(debug>1) System.out.println("Estado 1(31)= \n" + cypherText1);
            if(debug>1) System.out.println("Estado 2(31)= \n" + cypherText2);
            System.out.println("Estado 1(31)\\oplus Estado 2(31)= \n" + cypherText1.clone().xor(cypherText2));
            cypherText1 = cifra1.encryptRounds(state1, 31, 32);
            cypherText2 = cifra2.encryptRounds(state2, 31, 32);
            if(debug>1) System.out.println("Estado 1(32)= \n" + cypherText1);
            if(debug>1) System.out.println("Estado 2(32)= \n" + cypherText2);
            System.out.println("Estado 1(32)\\oplus Estado 2(32)= \n" + cypherText1.clone().xor(cypherText2));
        }
        ArrayList<ByteArray> allStates1 = cifra1.encryptFull(state1);
        ArrayList<ByteArray> allStates2 = cifra2.encryptFull(state2);
        
        ArrayList<Difference> allStatesDiff = new ArrayList<>();
        ArrayList<Difference> allKeysDiff = new ArrayList<>();
        
        for (int i = 0; i < allStates1.size(); i++) 
            allStatesDiff.add(new Difference(allStates1.get(i).clone().xor(allStates2.get(i))));        
        
        if(debug>0){
            System.out.println("Diferencial de estados = ");
            for (int i = 0; i < allStates1.size(); i++) {
                System.out.println(allStates1.get(i).getDifference(allStates2.get(i)));
            }
        }
        Differential result = new Differential(1, 32);
        result.stateDifferences = allStatesDiff;
        result.keyDifferences = Difference.toDifferenceArrayList(cifra1.getExpandedKey().getDifference(cifra2.getExpandedKey()).split(Serpent.ROUND_KEY_SIZE_IN_BYTES));
        result.fromRound = 1;
        result.toRound = Serpent.NUM_ROUNDS;
        result.firstSecretKey = key1;
        result.secondSecretKey = key2;
        result.keyDifference = keyDiff.getDelta();
        result.intermediateStateDifferences = allStatesDiff;
        result.intermediateStateDifferences.add(result.stateDifferences.get(result.stateDifferences.size()-1));
                
        return result;
    }    
    
    public static Differential getNablaDifferentialORIGINAL(int debug, int round, Difference keyDiff){
        if(debug>0) System.out.println("Diferencial de Chave = \n" + keyDiff);
        
        ByteArray key1 = new ByteArray(Serpent.KEY_SIZE_IN_BYTES);      //Chave Base
        key1.randomize();
        ByteArray key2 = keyDiff.xorDifference(key1);                   //Chave Base 2
        if(debug>1){
            System.out.println("Chave 1= \n" + key1);
            System.out.println("Chave 2= \n" + key2);
            System.out.println("Chave 1\\oplus Chave 2= \n" + key1.clone().xor(key2));
        }
        
        Serpent cifra1 = new Serpent(key1,round);                       //Usada para expandir a chave
        Serpent cifra2 = new Serpent(key2, round);                      //Usada para expandir a chave 2
        if(debug>0){
            if(debug>1) System.out.println("Chave expandida 1= \n" + cifra1.getExpandedKey());
            if(debug>1) System.out.println("Chave expandida 2= \n" + cifra2.getExpandedKey());
            System.out.println("Chave expandida 1\\oplus Chave expandida 2= \n" + cifra2.getExpandedKey().clone().xor(cifra1.getExpandedKey()));
        }    
        Difference stateDiff = new Difference(Serpent.BLOCK_SIZE_IN_BYTES); //Diferencial dos estados
        
        ByteArray state1 = new ByteArray(Serpent.BLOCK_SIZE_IN_BYTES);      //Estado Base
        state1.randomize();
        ByteArray state2 = stateDiff.xorDifference(state1);                 //Estado Base 2
        if(debug>1){
            System.out.println("Diferencial inicial do estado = \n" + stateDiff);
            System.out.println("Estado 1= \n" + state1);
            System.out.println("Estado 2= \n" + state2);
            System.out.println("Estado 1\\oplus Estado 2= \n" + state1.clone().xor(state2));
        }
        ByteArray cypherText1;
        ByteArray cypherText2;
        cypherText1 = cifra1.encryptRoundsBackwards(state1, 32, 32);
        cypherText2 = cifra2.encryptRoundsBackwards(state2, 32, 32);
        if(debug>0){
            if(debug>1) System.out.println("Estado 1(32)= \n" + cypherText1);
            if(debug>1) System.out.println("Estado 2(32)= \n" + cypherText2);
            System.out.println("Estado 1(32)\\oplus Estado 2(32)= \n" + cypherText1.clone().xor(cypherText2));
        }
        cypherText1 = cifra1.encryptRoundsBackwards(state1, 32, 31);
        cypherText2 = cifra2.encryptRoundsBackwards(state2, 32, 31);
        if(debug>0){
            if(debug>1) System.out.println("Estado 1(31)= \n" + cypherText1);
            if(debug>1) System.out.println("Estado 2(31)= \n" + cypherText2);
            System.out.println("Estado 1(31)\\oplus Estado 2(31)= \n" + cypherText1.clone().xor(cypherText2));
        }
        cifra1.setRoundOfFirstKeyToBeApplied(33);
        cifra2.setRoundOfFirstKeyToBeApplied(33);
        
        ArrayList<ByteArray> allStates1 = cifra1.encryptFull(state1);
        ArrayList<ByteArray> allStates2 = cifra2.encryptFull(state2);
        
        ArrayList<Difference> allStatesDiff = new ArrayList<>();
        ArrayList<Difference> allKeysDiff = new ArrayList<>();
        
        for (int i = 0; i < allStates1.size(); i++) 
            allStatesDiff.add(new Difference(allStates1.get(i).clone().xor(allStates2.get(i))));        
        
        if(debug>0){
            System.out.println("Diferencial de estados = ");
            for (int i = 0; i < allStates1.size(); i++) {
                System.out.println(allStates1.get(i).getDifference(allStates2.get(i)));
            }
        }
        Differential result = new Differential(1, 32);
        result.stateDifferences = allStatesDiff;
        result.keyDifferences = Difference.toDifferenceArrayList(cifra1.getExpandedKey().getDifference(cifra2.getExpandedKey()).split(Serpent.ROUND_KEY_SIZE_IN_BYTES));
        result.fromRound = 1;
        result.toRound = Serpent.NUM_ROUNDS;
        result.firstSecretKey = key1;
        result.secondSecretKey = key2;
        result.keyDifference = keyDiff.getDelta();
        result.intermediateStateDifferences = allStatesDiff;
        result.intermediateStateDifferences.add(result.stateDifferences.get(result.stateDifferences.size()-1));
                
        return result;
    }
///w[0]~w[7] = K
    /// w[i] = (w[i-8]^w[i-^5]^w[i-3]^w[i-1]^i^PHI)<<<11
}
