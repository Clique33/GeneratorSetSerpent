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
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ´Gabriel
 */
public class Attack<T extends Cipher> {
    
    T cipher1;
    T cipher2;
    
    //Informações gerais
    private int amountAffectedNibbles;                      //é a quantidade de nibbles afetados                                            lowDiff[0]
    private Difference lowestDifferential;                  //é a diferencial que afeta menos bits em determinada rodada.                   lowDiff[1]
    private ByteArray fullLowestDifferential;               //é a chave expandida de lowestDifferential.                                    lowDiff[2]
    private ArrayList<Integer> posBaseKeyAffectedNibbles;   //contém as posições dos nibbles afetados da chave base.                        lowDiff[3]
    private ArrayList<ByteArray> activeNibblesDifference;   //contém os nibbles ativos por ambas diferenciais nos estados internos da cifra. attack[2]
    private ArrayList<ByteArray> activeNibblesDifferenceKey;//contém os nibbles ativos por ambas diferenciais nas subchaves da cifra.
    ArrayList<ByteArray> activeNibblesThatAffectV;          //contém os nibbles ativos que influenciam na computação da variável 'v'na fase de recomputação.
    private int roundDelta;
    private int roundNabla;
    private int stateOfV;
    private int statePreBiclique;
    private boolean independent;
    
    //Informações de Delta
    private ArrayList<ByteArray> activeNibblesDeltaKey;     //contém os nibbles ativos de chave da diferença Delta.                         attack[5] /lowDiff[4]
    private Differential delta;                             //contém a diferencial delta completa                                           attack[3] / getDeltaDifferential()
    private ArrayList<ByteArray> activeNibblesDelta;        //contém os nibbles ativos pela diferencial delta nos estados internos da cifra attack[0]
    private int initialNibblesDelta[];
    
    //Informações Nabla
    private ArrayList<ByteArray> activeNibblesNablaKey;     //contém os nibbles ativos de chave da diferença Nabla.                         attack[6]
    private Differential nabla;                             //contém a diferencial nabla completa                                           attack[4] / getNablaDifferential()
    private ArrayList<ByteArray> activeNibblesNabla;        //contém os nibbles ativos pela diferencial nabla nos estados internos da cifra attack[1]
    private int initialNibblesNabla[];
        
    //Informações para as chaves Base
    ArrayList<IndependentNibble> independentNibbles;        //contém os nibbles das subchaves que são independentes da outra differencial.
    ArrayList<IndependentNibble> candidateNibbles;          //contém os nibbles das subchaves que são candidatos à nibbles de chave base.
    ArrayList<BaseKey> baseKeyCandidates;                   //contém os candidatos à chave base.
    
    //Informações de complexidade de tempo
    int numActiveSboxes;                                    //é o número total de sboxes que são ativadas na fase de recomputação.
    int numActiveSboxesForward;                             //é o número total de sboxes que são ativadas na fase de recomputação adiante nos estados internos da cifra.
    int numActiveSboxesBackward;                            //é o número total de sboxes que são ativadas na fase de recomputação para trás nos estados internos da cifra.
    int numActiveSboxesForwardKey;                          //é o número total de sboxes que são ativadas na fase de recomputação adiante nas subchaves da cifra.
    int numActiveSboxesBackwardkey;                         //é o número total de sboxes que são ativadas na fase de recomputação para trás nas subchaves da cifra.
    double expTimeComplexity;                               //é o expoente de 2 que representa a complexidade de tempo total do ataque.
    
    public static void main(String[] args){
        /*LinkedList<Attack> ataques = new LinkedList<>();
        double percent = 0.001;
        Attack a;
        int ini[] = new int[1];
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 64; j++) {
                ini[0] = j;
                a = new Attack(31, i, ini);
                a.applyAttack(false);
                if(a.isIndependent()) ataques.add(a);
            }
            if((i+1)/32.0 > percent){
                System.out.printf("\rProgress : %.2f%%",((i+1)/32.0*100));
                percent += 0.001;
            }
        }
        System.out.println("\rProgress : 100%");
        ataques.sort((Attack o1, Attack o2) -> {
            if(o1.getExpTimeComplexity() > o2.getExpTimeComplexity()) return 1;
            if(o1.getExpTimeComplexity() < o2.getExpTimeComplexity()) return -1;
            return 0;
        });
        System.out.println("");
        System.out.println(ataques);
        int n[] = {21};
        
        Attack a = new Attack(31, 18, n);
        a.applyAttack(true);*/
        while(true){
            try{
                System.out.println("Choose a test option:");
                System.out.println("1 - Finds all possible bicliques where there is only\n"
                        + "one active nibble for \\Delta^K and one for \\nabla^K, and the\n"
                        + "active nibble of \\Delta^K must be form subkey K^{31}.\n"
                        + "(Can take a couple of hours to finish)");
                System.out.println("2 - Shows all the information about the best biclique found.");
                System.out.println("0 - Exit the program.");

                int input = new Scanner(System.in).nextInt();

                if (input == 0) {
                    System.exit(0);
                }else if (input == 1) {
                    LinkedList<Attack<Serpent>> ataques = new LinkedList<>();
                    double percent = 0.001;
                    Attack<Serpent> a;
                    int iniDelta[] = new int[1];
                    int iniNabla[] = new int[1];
                    for (int i = 0; i < 32; i++) {
                        for (int j = 0; j < 64; j++) {
                            iniNabla[0] = j;
                            for (int k = 0; k < 32; k++) {
                                iniDelta[0] = k;
                                a = new Attack<>(new Serpent(), new Serpent(), 31, i, iniDelta, iniNabla);
                                a.applyAttack(false);
                                if(a.isIndependent()) ataques.add(a);
                            }
                        }
                        if((i+1)/32.0 > percent){
                            System.out.printf("\rProgress : %.2f%%",((i+1)/32.0*100));
                            percent += 0.001;
                        }
                    }
                    System.out.println("\rProgress : 100%");
                    ataques.sort((Attack<Serpent> o1, Attack<Serpent> o2) -> {
                        if(o1.getExpTimeComplexity() > o2.getExpTimeComplexity()) return 1;
                        if(o1.getExpTimeComplexity() < o2.getExpTimeComplexity()) return -1;
                        return 0;
                    });
                    System.out.println("");
                    System.out.println(ataques);
                    System.exit(0);
                } else if (input == 2) {
                    int m[] = {6};
                    int n[] = {11};

                    Attack<Serpent> a = new Attack<>(new Serpent(), new Serpent(), 31, 18, m, n);
                    a.applyAttack(true);
                    System.out.println(a.isIndependent());
                    System.exit(0);
                }else{
                    throw new Exception();
                }
            }catch(Exception e){
                System.out.println("Select either 1, 2 or 0.");
            }
        }
        /*LinkedList<Attack> ataques = new LinkedList<>();
        double percent = 0.001;
        Attack a;
        int iniDelta[] = new int[1];
        int iniNabla[] = new int[1];
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 64; j++) {
                iniNabla[0] = j;
                for (int k = 0; k < 32; k++) {
                    iniDelta[0] = k;
                    a = new Attack(31, i, iniDelta, iniNabla);
                    a.applyAttack(false);
                    if(a.isIndependent()) ataques.add(a);
                }
            }
            if((i+1)/32.0 > percent){
                System.out.printf("\rProgress : %.2f%%",((i+1)/32.0*100));
                percent += 0.001;
            }
        }
        System.out.println("\rProgress : 100%");
        ataques.sort((Attack o1, Attack o2) -> {
            if(o1.getExpTimeComplexity() > o2.getExpTimeComplexity()) return 1;
            if(o1.getExpTimeComplexity() < o2.getExpTimeComplexity()) return -1;
            return 0;
        });
        System.out.println("");
        System.out.println(ataques);
        int m[] = {6};
        int n[] = {11};
        
        Attack a = new Attack(31, 18, m, n);
        a.applyAttack(true);
        System.out.println(a.isIndependent());*/
    }
    
    //---------------------------------Métodos---------------------------------
    
    /**
     * Inicializa a maior parte dos atributos de Attack.
     */
    public Attack() {
        activeNibblesThatAffectV = new ArrayList<>();
        for (int i = 0; i < 91; i++) {
            activeNibblesThatAffectV.add(new ByteArray(16));
        }
        activeNibblesDelta = new ArrayList<>();
        activeNibblesNabla = new ArrayList<>();
        activeNibblesDifference = new ArrayList<>();
        activeNibblesDeltaKey = new ArrayList<>();
        activeNibblesNablaKey = new ArrayList<>();
        activeNibblesDifferenceKey = new ArrayList<>();
        independentNibbles = new ArrayList<>();
        candidateNibbles = new ArrayList<>();
        baseKeyCandidates = new ArrayList<>();
        initialNibblesNabla = new int[2];
        initialNibblesNabla[0] = 5;
        initialNibblesNabla[1] = 26;
        initialNibblesDelta = null;
        
        delta = new Differential();
        nabla = new Differential();
        for (int i = 0; i < 97; i++) {
            activeNibblesDelta.add(new ByteArray(16));
            activeNibblesNabla.add(new ByteArray(16));
        }
        
        numActiveSboxes = 0;
        numActiveSboxesForward = 0;
        numActiveSboxesBackward = 0;
        numActiveSboxesForwardKey = 0;
        numActiveSboxesBackwardkey = 0;
        
        stateOfV = 75;
        statePreBiclique = 90;
        roundDelta = 31;
        roundNabla = 30;
        independent = false;
    }
    
    /**
     * Inicializa a maior parte dos atributos de Attack.
     */
    public Attack(int roundDelta, int roundNabla) {
        activeNibblesThatAffectV = new ArrayList<>();
        for (int i = 0; i < 91; i++) {
            activeNibblesThatAffectV.add(new ByteArray(16));
        }
        activeNibblesDelta = new ArrayList<>();
        activeNibblesNabla = new ArrayList<>();
        activeNibblesDifference = new ArrayList<>();
        activeNibblesDeltaKey = new ArrayList<>();
        activeNibblesNablaKey = new ArrayList<>();
        activeNibblesDifferenceKey = new ArrayList<>();
        independentNibbles = new ArrayList<>();
        candidateNibbles = new ArrayList<>();
        baseKeyCandidates = new ArrayList<>();
        initialNibblesNabla = new int[2];
        initialNibblesNabla[0] = 5;
        initialNibblesNabla[1] = 26;
        initialNibblesDelta = null;
        
        delta = new Differential();
        nabla = new Differential();
        for (int i = 0; i < 97; i++) {
            activeNibblesDelta.add(new ByteArray(16));
            activeNibblesNabla.add(new ByteArray(16));
        }
        
        numActiveSboxes = 0;
        numActiveSboxesForward = 0;
        numActiveSboxesBackward = 0;
        numActiveSboxesForwardKey = 0;
        numActiveSboxesBackwardkey = 0;
        
        stateOfV = 75;
        statePreBiclique = 90;
        this.roundDelta = roundDelta;
        this.roundNabla = roundNabla;
        independent = false;
    }
    
    /**
     * Inicializa a maior parte dos atributos de Attack.
     */
    public Attack(int roundDelta, int roundNabla, int initialNibblesNabla[]) {
        activeNibblesThatAffectV = new ArrayList<>();
        for (int i = 0; i < 91; i++) {
            activeNibblesThatAffectV.add(new ByteArray(16));
        }
        activeNibblesDelta = new ArrayList<>();
        activeNibblesNabla = new ArrayList<>();
        activeNibblesDifference = new ArrayList<>();
        activeNibblesDeltaKey = new ArrayList<>();
        activeNibblesNablaKey = new ArrayList<>();
        activeNibblesDifferenceKey = new ArrayList<>();
        independentNibbles = new ArrayList<>();
        candidateNibbles = new ArrayList<>();
        baseKeyCandidates = new ArrayList<>();
        this.initialNibblesNabla = Arrays.copyOf(initialNibblesNabla, initialNibblesNabla.length);
        initialNibblesDelta = null;
        
        delta = new Differential();
        nabla = new Differential();
        for (int i = 0; i < 97; i++) {
            activeNibblesDelta.add(new ByteArray(16));
            activeNibblesNabla.add(new ByteArray(16));
        }
        
        numActiveSboxes = 0;
        numActiveSboxesForward = 0;
        numActiveSboxesBackward = 0;
        numActiveSboxesForwardKey = 0;
        numActiveSboxesBackwardkey = 0;
        
        stateOfV = 75;
        statePreBiclique = 90;
        this.roundDelta = roundDelta;
        this.roundNabla = roundNabla;
        independent = false;
    }
        
    /**
     * Inicializa a maior parte dos atributos de Attack.
     */
    public Attack(T cifra1, T cifra2, int roundDelta, int roundNabla, int initialNibblesDelta[], int initialNibblesNabla[]) {
        this.cipher1 = cifra1;
        this.cipher2 = cifra2;
        activeNibblesThatAffectV = new ArrayList<>();
        for (int i = 0; i < 91; i++) {
            activeNibblesThatAffectV.add(new ByteArray(16));
        }
        activeNibblesDelta = new ArrayList<>();
        activeNibblesNabla = new ArrayList<>();
        activeNibblesDifference = new ArrayList<>();
        activeNibblesNablaKey = new ArrayList<>();
        activeNibblesDeltaKey = new ArrayList<>();
        activeNibblesDifferenceKey = new ArrayList<>();
        independentNibbles = new ArrayList<>();
        candidateNibbles = new ArrayList<>();
        baseKeyCandidates = new ArrayList<>();
        this.initialNibblesDelta = Arrays.copyOf(initialNibblesDelta, initialNibblesDelta.length);
        this.initialNibblesNabla = Arrays.copyOf(initialNibblesNabla, initialNibblesNabla.length);
        
        this.lowestDifferential = new Difference();
        this.amountAffectedNibbles = initialNibblesDelta.length;
        this.posBaseKeyAffectedNibbles = new ArrayList<>();
        for (int i = 0; i < initialNibblesDelta.length; i++) posBaseKeyAffectedNibbles.add(initialNibblesDelta[i]);
        
        delta = new Differential();
        nabla = new Differential();
        for (int i = 0; i < 97; i++) {
            activeNibblesDelta.add(new ByteArray(16));
            activeNibblesNabla.add(new ByteArray(16));
        }
        
        numActiveSboxes = 0;
        numActiveSboxesForward = 0;
        numActiveSboxesBackward = 0;
        numActiveSboxesForwardKey = 0;
        numActiveSboxesBackwardkey = 0;
        
        stateOfV = 75;
        statePreBiclique = 90;
        this.roundDelta = roundDelta;
        this.roundNabla = roundNabla;
        independent = false;
    }
    
    @Override
    public String toString(){
        String result = "Attack ( delta : {key = " + roundDelta + " nibbles : " + posBaseKeyAffectedNibbles +
                "} nabla : {key = " + roundNabla + " nibbles : " + Arrays.toString(initialNibblesNabla) + "})";
        return result+"\ntime complexity : C_attack ~ 2^{" + expTimeComplexity + "}";
    }
    
    /**
     * Aplica o ataque, chamando createAttack() e depois computando os nibbles
     * que afetam a variável intermediária 'v' do meet-in-the-middle na fase de
     * recomputação. Além disso, calcula quantas Sboxes são necessárias em cada
     * estado e chave para tal, e finaliza calculado a complexidadede tempo do 
     * ataque.
     * @param debug seta se serão feitos os prints de teste ou não.
     */
    public void applyAttack(boolean debug) throws Exception{
        createAttack(debug);
                
        computeActiveNibblesThatAffectV();
        findIndependentNibbles();
        computeCandidates();
        computeBaseKeyCandidates();
        
        countSboxes();
        
        computeTimeComplexity();

        if(debug) printActiveNibblesDeltaKey();
        if(debug) printActiveNibblesNablaKey();
        if(debug) printActiveNibblesThatAffectV();
        if(debug) printActiveNibblesDifferenceKey();
        //if(debug) printIndependentNibbles();
        //if(debug) printCandidates();
        if(debug) printBaseKeyCandidates();
        if(debug) printTimeComplexity();
    }

    public double getExpTimeComplexity() {
        return expTimeComplexity;
    }
    
    /**
     * Calcula todas as informações das diferenciais nabla e delta, setando os
     * atributos 'activeNibblesDeltaKey', 'activeNibblesDelta', 'delta',
     * 'activeNibblesNablaKey', 'activeNibblesNabla', 'nabla' e
     * 'activeNibblesDifference'.
     *
     * @param debug seta se serão feitos os prints de teste ou não.
     * @param roundDelta seta quais chaves de rodada serão usadas no cálculo das
     *              diferenciais delta. 'roundDelta' e 'roundDelta' + 1.
     * @param roundNabla seta quais chaves de rodada serão usadas no cálculo das
     *              diferenciais delta. 'roundNabla' e 'roundNabla' + 1.
     * 
     */
    public void createAttack(boolean debug){
        
        if(initialNibblesDelta == null) getLowestDiffBasic(false,28);
        
        computeActiveNibblesDelta();
                    
        computeActiveNibblesNabla();
        
        computeActiveNibblesDifference();
        
        computeActiveNibblesDeltaKey();
        
        computeActiveNibblesNablaKey();
        
        computeActiveNibblesDifferenceKey();
        
        checkIndependance();
        
        
        if(debug) printActiveNibblesDelta();
        if(debug) printActiveNibblesNabla();
        if(debug) printActiveNibblesDifference();
    }
    
    /**
     * Conta a quantidade de sbox lookups necessárias  na fase de recomputação
     * do Matching with precomputations (MwP).
     * 
     * @param stateOfV é o estado interno da cifra onde a variável 'v' do MwP
     *              está setada.
     */
    public void countSboxes(){
        for (int i = 0; i < activeNibblesThatAffectV.size(); i++) {
            if(i <= stateOfV && i%3 == 2){// Ida e apenas os estados que foram afetados por S-boxes
                for (int j = 0; j < activeNibblesThatAffectV.get(i).length()*2; j++){
                    if(activeNibblesThatAffectV.get(i).getNibble(j) != 0){
                        numActiveSboxes++;
                        numActiveSboxesForward++;
                    }
                }
                //System.out.println("Ida, "+numActiveSboxes);
            }
            if(i <= stateOfV && i%3 == 1){// Ida e apenas as subchaves que foram afetadas por S-boxes
                for (int j = 0; j < activeNibblesNablaKey.get(i/3).length()*2; j++){
                    if(activeNibblesNablaKey.get(i/3).getNibble(j) != 0 && activeNibblesThatAffectV.get(i).getNibble(j) != 0){
                        numActiveSboxes++;
                        numActiveSboxesForwardKey++;
                    }
                }
                //System.out.println("Ida, "+numActiveSboxes);
            }
            if(i >= stateOfV && i%3 == 1){// Volta e apenas os estados que foram afetados por S-boxes
                for (int j = 0; j < activeNibblesThatAffectV.get(i).length()*2; j++){
                    if(activeNibblesThatAffectV.get(i).getNibble(j) != 0){
                        numActiveSboxes++;
                        numActiveSboxesBackward++;
                    }
                }
                //System.out.println("Volta, "+numActiveSboxes);
            }
            if(i >= stateOfV && i%3 == 0){// Volta e apenas as subchaves que foram afetadas por S-boxes
                //System.out.print("state: " + i + " subkey: " + i/3);
                //int aux = 0;
                for (int j = 0; j < activeNibblesDeltaKey.get(i/3).length()*2; j++){
                    if(activeNibblesDeltaKey.get(i/3).getNibble(j) != 0 && activeNibblesThatAffectV.get(i).getNibble(j) != 0){ 
                        numActiveSboxes++;
                        numActiveSboxesBackwardkey++;
                        //aux++;
                    }
                }
                //System.out.println(" total active nibbles of key: " + aux);
                //System.out.println("Volta, "+numActiveSboxes);
            }
        }
    }
    
    /**
     * Computa todos os nibbles de todos os estados internos que afetam a
     * variável 'v' na recomputação do MwP.
     * 
     * @param stateOfV é o estado interno da cifra onde a variável 'v' do MwP
     *              está setada.
     * @param statePreBiclique é o último estado interno da cifra antes de
     *              entrar nos estados internos da biclique.
     */
    public void computeActiveNibblesThatAffectV() throws Exception{
        
        //Cipher cifra1 = T.newInstance(delta.firstSecretKey, 31);
        //Cipher cifra2 = T.newInstance(delta.firstSecretKey, 31);
        Cipher cifra1 = cipher1.Reset(delta.firstSecretKey, 31);
        Cipher cifra2 = cipher1.Reset(delta.firstSecretKey, 31);
        
        ByteArray state1 = new ByteArray(16);
        ByteArray state2 = new ByteArray(16);
        
        //int fromState = 75;
        //int toState = 90;
        
        for (int i = 1; i < 16; i++) {
            state2.setNibble(31, i);
        
            ArrayList<ByteArray> ida1 = cifra1.encryptRoundsFromStatesSavingStates(state1,stateOfV,statePreBiclique);
            ArrayList<ByteArray> ida2 = cifra2.encryptRoundsFromStatesSavingStates(state2,stateOfV,statePreBiclique);
            ArrayList<ByteArray> volta1 = cifra1.encryptRoundsBackwardsFromStatesSavingStates(state1,stateOfV,0);
            ArrayList<ByteArray> volta2 = cifra2.encryptRoundsBackwardsFromStatesSavingStates(state2,stateOfV,0);
            
            ByteArray aux;
            //System.out.println("");
            for (int j = 0; j < volta1.size(); j++) {
                aux = volta1.get(j).clone().xor(volta2.get(j));
                for (int k = 0; k < 32; k++) {
                    if(activeNibblesThatAffectV.get(j).getNibble(k) == 0 && aux.getNibble(k) !=0)
                        activeNibblesThatAffectV.get(j).setNibble(k, 0xA);                
                }
                //System.out.println("#"+(j)+" = "+aux);
            }
            for (int j = 0; j < ida1.size(); j++) {
                aux = ida1.get(j).clone().xor(ida2.get(j));
                for (int k = 0; k < 32; k++) {
                    if(activeNibblesThatAffectV.get((stateOfV+j)).getNibble(k) == 0 && aux.getNibble(k) !=0)
                        activeNibblesThatAffectV.get((stateOfV+j)).setNibble(k, 0xA);                
                }
                //System.out.println("#"+(fromState+j)+" = "+aux);
            }
        }
        
//---------------Manually altering nibbles influenced by the Delta and Nabla Differentials-------------------------
        
        for (int i = 84; i <= statePreBiclique; i++) //Influenced by Delta Differentials in states 84 to 'toState'
            activeNibblesThatAffectV.set(i,activeNibblesDelta.get(i));
        
        cifra1 = cipher1.Reset(nabla.firstSecretKey, roundNabla);
        
        ByteArray baseKey = nabla.firstSecretKey;
        ArrayList<Integer> activeNibblesFromKeyDifference = nabla.keyDifference.getActiveNibbles();
        int randomState = 10;
        
        for (int k = 1; k < 16; k++) {
            //for (int l = 1; l < 16; l++) {
                ByteArray P1 = new ByteArray(16);
                P1.randomize();
                ByteArray P2 = P1.clone();
                
                ByteArray diff = nabla.keyDifference;
                diff.setNibble(activeNibblesFromKeyDifference.get(0), k^baseKey.getNibble(activeNibblesFromKeyDifference.get(0)));
                //diff.setNibble(activeNibblesFromKeyDifference.get(1), l^baseKey.getNibble(activeNibblesFromKeyDifference.get(1)));
                cifra2 = cipher2.Reset(nabla.firstSecretKey.clone().xor(diff), roundNabla);
                //System.out.println(cifra1.getExpandedKey().clone().xor(cifra2.getExpandedKey()));
                //new Scanner(System.in).next();
                ArrayList<ByteArray> aux1 = cifra1.encryptFullSavingStates(P1, 0, randomState);
                ArrayList<ByteArray> aux2 = cifra2.encryptFullSavingStates(P2, 0, randomState);

                for (int i = 0; i < aux1.size(); i++) {
                    ByteArray aux;
                    for (int j = 0; j < cipher1.getBLOCK_SIZE_IN_BYTES()*2; j++) {
                        aux = aux1.get(i).clone().getDifference(aux2.get(i));
                        if(activeNibblesThatAffectV.get(i).getNibble(j) != 0xb){
                            if(aux.getNibble(j) == 0){
                                activeNibblesThatAffectV.get(i).setNibble(j, 0);
                            }else
                                activeNibblesThatAffectV.get(i).setNibble(j, 0xb);
                        }
                    }
                }
            //}
        }
        
        for (int i = 0; i <= randomState; i++) 
            for (int j = 0; j < cipher1.getBLOCK_SIZE_IN_BYTES()*2; j++) 
                if(activeNibblesThatAffectV.get(i).getNibble(j) == 0xb) activeNibblesThatAffectV.get(i).setNibble(j, 0xA);
//-----------------------------------------------------------------------------------------------------------------
    }
    
    /**
     * Computa a complexidade de tempo do ataque completo (Precisa que a
     * contagem das sboxes já tenha sido feita).
     */
    public void computeTimeComplexity(){
        double C_biclique = Math.pow(2, 0.736965594166206);
        double C_precomp = Math.pow(2, 3.922832139477540);
        double C_recomp = Math.pow(2, Math.log(((double)numActiveSboxes/2080)*256)/Math.log(2));
        double C_falpos = Math.pow(2, 4);
        expTimeComplexity = (248+Math.log(Math.pow(2, 0.736965594166206)+
                Math.pow(2, 3.922832139477540)+
                Math.pow(2, Math.log(((double)numActiveSboxes/2080)*256)/Math.log(2))+
                Math.pow(2, 4))/Math.log(2));        
    }
    
    /**
     * Computa os nibbles ativos tanto pelos nibbles ativos das diferenciais
     * nabla quanto das diferenciais delta. 'a' são os nibbles ativos somente
     * por delta, 'b' somente os ativos por nabla e '1' os ativos por ambos.
     */
    public void computeActiveNibblesDifference(){
        for (int i = 0; i < activeNibblesNabla.size(); i++) 
            activeNibblesDifference.add(activeNibblesNabla.get(i).clone().xor(activeNibblesDelta.get(i)));
    }    
    
    /**
     * Computa os nibbles ativos tanto pelos nibbles ativos das diferenciais
     * nabla quanto das diferenciais delta nas subchaves de cifra. 'a' são os
     * nibbles ativos somente por delta, 'b' somente os ativos por nabla e '1'
     * os ativos por ambos.
     */
    public void computeActiveNibblesDifferenceKey(){
        ArrayList<ByteArray> aux = new ArrayList<>();
        for (int i = 0; i < activeNibblesNablaKey.size(); i++) {
            aux.add(activeNibblesNablaKey.get(i).clone());
            for (int j = 0; j < aux.get(i).length()*2; j++) 
                if(aux.get(i).getNibble(j) == 0xa) aux.get(i).setNibble(j,0xb);
        }    
        for (int i = 0; i < activeNibblesNablaKey.size(); i++) 
            activeNibblesDifferenceKey.add(aux.get(i).clone().xor(activeNibblesDeltaKey.get(i)));
    }
    
    /**
     * Computa todos os nibbles que são ativados pelas diferenciais delta nos 
     * estados internos da cifra.
     * 
     * @param round e round + 1 são as rodadas onde a diferença de chave 
     *              'keyDiff' será definida.
     */
    public void computeActiveNibblesDelta(){
        if(!lowestDifferential.equals(new Difference())){
            for (int i : posBaseKeyAffectedNibbles) {
                for (int j = 1; j < 16; j++) {
                    lowestDifferential.setNibble(i, j);
                    computeDeltaDifferential(0, lowestDifferential);
                    //if(debug) System.out.println(delta.keyDifference);
                    for (int k = 0; k < delta.stateDifferences.size(); k++) {
                        for (int l = 0; l < delta.stateDifferences.get(k).length()*2; l++) {
                            if(delta.stateDifferences.get(k).getNibble(l) != 0 && activeNibblesDelta.get(k).getNibble(l)==0){
                                activeNibblesDelta.get(k).setNibble(l, 0xA);
                            }  
                        }
                    }
                }
            }
        }else{
            for (int j = 1; j < 16; j++) {
                ByteArray aux = new ByteArray(32);
                for (int k = 1; k < 0x1<<(initialNibblesDelta.length*4); k++) {
                    for (int l = 0; l < initialNibblesDelta.length; l++) {
                        aux.setNibble(initialNibblesDelta[l], (k>>(0x1*(l*4))));
                    }
                    //if(debug) System.out.println(aux);
                    computeDeltaDifferential(0, new Difference(aux));
                    //if(debug) System.out.println(nabla.keyDifference);

                    for (int m = 0; m < delta.stateDifferences.size(); m++) {
                        for (int n = 0; n < delta.stateDifferences.get(m).length()*2; n++) {
                            if(delta.stateDifferences.get(m).getNibble(n) != 0 && activeNibblesDelta.get(m).getNibble(n)==0){
                                activeNibblesDelta.get(m).setNibble(n, 0xA);
                            }                                
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Computa todos os nibbles que são ativados pelas diferenciais nabla nos 
     * estados internos da cifra.
     * 
     * @param round e round + 1 são as rodadas onde a diferença de chave 'nabla'
     *              será definida.
     * @param nibble1 posição do primeiro nibble a ser ativado
     * @param nibble2 posição do segundo nibble a ser ativado
     */
    public void computeActiveNibblesNabla(){
        
        for (int j = 1; j < 16; j++) {
            ByteArray aux = new ByteArray(32);
            for (int k = 1; k < 0x1<<(initialNibblesNabla.length*4); k++) {
                for (int l = 0; l < initialNibblesNabla.length; l++) {
                    aux.setNibble(initialNibblesNabla[l], (k>>(0x1*(l*4))));
                }
                //if(debug) System.out.println(aux);
                computeNablaDifferential(0, new Difference(aux));
                //if(debug) System.out.println(nabla.keyDifference);

                for (int m = 0; m < nabla.stateDifferences.size(); m++) {
                    for (int n = 0; n < nabla.stateDifferences.get(m).length()*2; n++) {
                        if(nabla.stateDifferences.get(m).getNibble(n) != 0 && activeNibblesNabla.get(m).getNibble(n)==0){
                            activeNibblesNabla.get(m).setNibble(n, 0xB);
                        }                                
                    }
                }
            }
        }
    }
    
    /**
     * Computa todos os nibbles que são ativados pelas diferenciais nabla nos 
     * estados internos da cifra.
     * 
     * @param nibble1 posição do primeiro nibble a ser ativado
     * @param nibble2 posição do segundo nibble a ser ativado
     */
    public void computeActiveNibblesNablaOriginal( int nibble1, int nibble2){
        for (int i : posBaseKeyAffectedNibbles) {
            for (int j = 1; j < 16; j++) {
                ByteArray aux = new ByteArray(32);
                for (int k = 1; k < 16; k++) {
                    for (int l = 1; l < 16; l++) {
                        aux.setNibble(nibble1, k);
                        aux.setNibble(nibble2, l);
                        //if(debug) System.out.println(aux);
                        computeNablaDifferential(0, new Difference(aux));
                        //if(debug) System.out.println(nabla.keyDifference);
                        
                        for (int m = 0; m < nabla.stateDifferences.size(); m++) {
                            for (int n = 0; n < nabla.stateDifferences.get(m).length()*2; n++) {
                                if(nabla.stateDifferences.get(m).getNibble(n) != 0 && activeNibblesNabla.get(m).getNibble(n)==0){
                                    activeNibblesNabla.get(m).setNibble(n, 0xB);
                                }                                
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Computa todos os nibbles que são ativados pelas diferenciais delta nas 
     * subchaves da cifra (na chave expandida).
     * 
     * @param round e round + 1 são as rodadas onde a diferença de chave 'delta'
     *              está definida.
     */
    public void computeActiveNibblesDeltaKey(){      
        
        //-----------------Getting the active nibbles of Delta key----------------------------
        
        ArrayList<Integer> indexActiveNibbles = delta.keyDifference.getActiveNibbles();
        for (int i = 0; i < 33; i++) 
            activeNibblesDeltaKey.add(new ByteArray(16));
        for (int i = 1; i < 16; i++) {
            for (int j = 1; j < 16; j++) {
                ByteArray key1 = new ByteArray(32);
                ByteArray key2 = key1.clone();
                
                for (int k = 0; k < indexActiveNibbles.size(); k++)
                    key2.setNibble(indexActiveNibbles.get(k), i);
                
                
                Cipher cifra1 = cipher1.Reset(key1, roundDelta);
                Cipher cifra2 = cipher2.Reset(key2, roundDelta);
                
                ByteArray keyDifference = cifra1.getExpandedKey(key1, roundDelta).clone().getDifference(cifra2.getExpandedKey(key2, roundDelta));
                for (int k = 0; k < keyDifference.length()*2; k++) {
                    int key = k/32;
                    int nibble = k%32;
                    if (activeNibblesDeltaKey.get(key).getNibble(nibble)==0 && keyDifference.getNibble(k)!=0) 
                        activeNibblesDeltaKey.get(key).setNibble(nibble,0xA);
                }   
            }
        }
        
        /*System.out.println("Active Nibbles of the Nabla key");
        for (int i = 0; i < activeNibblesNablaKey.size(); i++) {
            System.out.println("#"+i+" = "+activeNibblesNablaKey.get(i));
        }*/
        //------------------------------------------------------------------------------------     
        
    }
        
    /**
     * Computa todos os nibbles que são ativados pelas diferenciais nabla nas 
     * subchaves da cifra (na chave expandida).
     * 
     * @param round e round + 1 são as rodadas onde a diferença de chave 'nabla'
     *              está definida.
     */
    public void computeActiveNibblesNablaKey(){
        //-----------------Getting the active nibbles of Nabla key----------------------------
        ArrayList<Integer> indexActiveNibbles = nabla.keyDifference.getActiveNibbles();
        
        for (int i = 0; i < 33; i++) 
            activeNibblesNablaKey.add(new ByteArray(16));
        for (int i = 1; i < 16; i++) {
            for (int j = 1; j < 16; j++) {
                ByteArray key1 = new ByteArray(32);
                ByteArray key2 = key1.clone();
                
                for (int k = 0; k < indexActiveNibbles.size(); k++)
                    key2.setNibble(indexActiveNibbles.get(k), i);
                
                
                Cipher cifra1 = cipher1.Reset(key1, roundNabla);
                Cipher cifra2 = cipher2.Reset(key2, roundNabla);
                
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
        
    }
    
    public void printActiveNibblesThatAffectV(){
        System.out.println("Nibbles that influence variable v = ");
        for (int i = 0; i < activeNibblesThatAffectV.size(); i++) 
            System.out.println("#"+i+" = "+activeNibblesThatAffectV.get(i));
    }
    
    public void printTimeComplexity(){
        System.out.println("#Active Sboxes Forward = "+numActiveSboxesForward);
        System.out.println("#Active Sboxes Forward Key = "+numActiveSboxesForwardKey);
        System.out.println("#Active Sboxes Backward = "+numActiveSboxesBackward);
        System.out.println("#Active Sboxes Backward Key = "+numActiveSboxesBackwardkey);
        System.out.println("#Active Sboxes = "+numActiveSboxes+"/2080 = "+((double)numActiveSboxes/2080*100)+"% of the whole cipher, which is");
        System.out.println("(2^{8}*"
                +((double)numActiveSboxes/2080)+" = "+(((double)numActiveSboxes/2080)*256)+" = 2^{"+Math.log(((double)numActiveSboxes/2080)*256)/Math.log(2)+"})");
        
        System.out.println("C_attack = 2^{k-2d}*(C_biclique + C_precomp + C_recomp + C_falpos)");
        System.out.println("C_attack = 2^{248}*(((2^{4}+2^{4})*(5/96)) + (2^{4}*(75/96) + 2^{4}*(16/96)) + 2^{"+Math.log(((double)numActiveSboxes/2080)*256)/Math.log(2)+"} + 2^{8-4})");
        System.out.println("C_attack = 2^{248}*(2^{0.736965594166206} + 2^{3.922832139477540} + 2^{"+Math.log(((double)numActiveSboxes/2080)*256)/Math.log(2)+"} + 2^4)");
        System.out.println("C_attack = 2^{"+(248+Math.log(Math.pow(2, 0.736965594166206)+Math.pow(2, 3.922832139477540)+Math.pow(2, Math.log(((double)numActiveSboxes/2080)*256)/Math.log(2))+Math.pow(2, 4))/Math.log(2))+"}");
        System.out.printf("C_attack ~ 2^{%.2f}\n",expTimeComplexity);
        
    }
    
    public void printActiveNibblesDeltaKey(){
        System.out.println("Active Nibbles of the Delta key");
        for (int i = 0; i < activeNibblesDeltaKey.size(); i++) {
            System.out.println("K"+i+" = "+activeNibblesDeltaKey.get(i));
        }
    }
    
    public void printActiveNibblesNablaKey(){
        System.out.println("Active Nibbles of the Nabla key");
        for (int i = 0; i < activeNibblesNablaKey.size(); i++) {
            System.out.println("K"+i+" = "+activeNibblesNablaKey.get(i));
        }
    }
    
    public void printActiveNibblesDelta(){
        System.out.println("\nDelta Differentials =");
        for (int i = 0; i < activeNibblesDelta.size(); i++) {
            System.out.println(activeNibblesDelta.get(i));
        }
    }
    
    public void printActiveNibblesNabla(){
        System.out.println("\nNabla Differentials =");
        for (int i = 0; i < activeNibblesNabla.size(); i++) {
            System.out.println(activeNibblesNabla.get(i));
        }
    }
    
    public void printActiveNibblesDifference(){
        System.out.println("\nSee independence of internal states (a is for Delta, b is for Nabla, 1 is for both and 0 is none) =");
        for (int i = 0; i < activeNibblesDifference.size(); i++) {
            if (i == 0) System.out.println("#"+i+" P\t="+activeNibblesDifference.get(i));
            else if (i%3 == 1) System.out.println("#"+i+" AK"+((i+2)/3)+"\t="+activeNibblesDifference.get(i));
            else if (i%3 == 2) System.out.println("#"+i+" S"+((i+1)/3)+"\t\t="+activeNibblesDifference.get(i));
            else if (i%3 == 0 && i/3 !=32) System.out.println("#"+i+" L"+(i/3)+"\t\t="+activeNibblesDifference.get(i));
            else System.out.println("#"+i+" AK33 = C\t="+activeNibblesDifference.get(i));
        }
    }
        
    public void printActiveNibblesDifferenceKey(){
        System.out.println("\nSee independence of subkeys (a is for Delta, b is for Nabla, 1 is for both and 0 is none) =");
        for (int i = 0; i < activeNibblesDifferenceKey.size(); i++) {
            if (i < 10) System.out.println("K"+i+"  ="+activeNibblesDifferenceKey.get(i));
            else System.out.println("K"+i+" ="+activeNibblesDifferenceKey.get(i));
        }
    }
    
    /**
     * Calcula a diferencial delta a partir da diferença de chave 'keyDiff'
     * aplicada nas rodadas 'round' e 'round'+1.
     *
     * @param debug seta quais prints de teste serão feitos. 0 é nenhum, 1 é
     *              alguns e 2 são todos.
     * @param keyDiff é a diferença de chave usada para recuperar a diferencial
     *              nabla.
     * @param round e round + 1 são as rodadas onde a diferença de chave 
     *              'keyDiff' está definida.
     */
    public void computeDeltaDifferential(int debug, Difference keyDiff){
        if(debug>0) System.out.println("Diferencial de Chave = \n" + keyDiff);
        
        ByteArray key1 = new ByteArray(cipher1.getKEY_SIZE_IN_BYTES());      //Chave Base
        ByteArray key2 = keyDiff.xorDifference(key1);                   //Chave Base 2
        if(debug>1){ 
            System.out.println("Chave 1= \n" + key1);
            System.out.println("Chave 2= \n" + key2);
            System.out.println("Chave 1\\oplus Chave 2= \n" + key1.clone().xor(key2));
        }
        Cipher cifra1 = cipher1.Reset(key1,roundDelta);       //Usada para expandir a chave
        Cipher cifra2 = cipher2.Reset(key2, roundDelta);      //Usada para expandir a chave 2
        if(debug>0){
            if(debug>1) System.out.println("Chave expandida 1= \n" + cifra1.getExpandedKey());
            if(debug>1) System.out.println("Chave expandida 2= \n" + cifra2.getExpandedKey());
            System.out.println("Chave expandida 1\\oplus Chave expandida 2= \n" + cifra2.getExpandedKey().clone().xor(cifra1.getExpandedKey()));
        }     
        Difference stateDiff = new Difference(cipher1.getBLOCK_SIZE_IN_BYTES()); //Diferencial dos estados
        
        ByteArray state1 = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());      //Estado Base
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
        delta = new Differential(1, 32);
        delta.stateDifferences = allStatesDiff;
        delta.keyDifferences = Difference.toDifferenceArrayList(cifra1.getExpandedKey().getDifference(cifra2.getExpandedKey()).split(cipher1.getROUND_KEY_SIZE_IN_BYTES()));
        delta.fromRound = 1;
        delta.toRound = cipher1.getNUM_ROUNDS();
        delta.firstSecretKey = key1;
        delta.secondSecretKey = key2;
        delta.keyDifference = keyDiff.getDelta();
        delta.intermediateStateDifferences = allStatesDiff;
    } 
    
    /**
     * Calcula a diferencial nabla a partir da diferença de chave 'keyDiff'
     * aplicada nas rodadas 'round' e 'round'+1.
     *
     * @param debug seta quais prints de teste serão feitos. 0 é nenhum, 1 é
     *              alguns e 2 são todos.
     * @param keyDiff é a diferença de chave usada para recuperar a diferencial
     *              nabla.
     * @param round e round + 1 são as rodadas onde a diferença de chave 
     *              'keyDiff' está definida.
     */
    public void computeNablaDifferential(int debug, Difference keyDiff){
        if(debug>0) System.out.println("Diferencial de Chave = \n" + keyDiff);
        
        ByteArray key1 = new ByteArray(cipher1.getKEY_SIZE_IN_BYTES());      //Chave Base
        key1.randomize();
        ByteArray key2 = keyDiff.xorDifference(key1);                   //Chave Base 2
        if(debug>1){
            System.out.println("Chave 1= \n" + key1);
            System.out.println("Chave 2= \n" + key2);
            System.out.println("Chave 1\\oplus Chave 2= \n" + key1.clone().xor(key2));
        }
        
        Cipher cifra1 = cipher1.Reset(key1,roundNabla);                       //Usada para expandir a chave
        Cipher cifra2 = cipher2.Reset(key2, roundNabla);                      //Usada para expandir a chave 2
        if(debug>0){
            if(debug>1) System.out.println("Chave expandida 1= \n" + cifra1.getExpandedKey());
            if(debug>1) System.out.println("Chave expandida 2= \n" + cifra2.getExpandedKey());
            System.out.println("Chave expandida 1\\oplus Chave expandida 2= \n" + cifra2.getExpandedKey().clone().xor(cifra1.getExpandedKey()));
        }    
        Difference stateDiff = new Difference(cipher1.getBLOCK_SIZE_IN_BYTES()); //Diferencial dos estados
        
        ByteArray state1 = new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES());      //Estado Base
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
        /*cypherText1 = cifra1.encryptRoundsBackwards(state1, 32, 32);
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
        }*/
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
        
        nabla = new Differential(1, 32);
        nabla.stateDifferences = allStatesDiff;
        nabla.keyDifferences = Difference.toDifferenceArrayList(cifra1.getExpandedKey().getDifference(cifra2.getExpandedKey()).split(cipher1.getROUND_KEY_SIZE_IN_BYTES()));
        nabla.fromRound = 1;
        nabla.toRound = cipher1.getNUM_ROUNDS();
        nabla.firstSecretKey = key1;
        nabla.secondSecretKey = key2;
        nabla.keyDifference = keyDiff.getDelta();
        nabla.intermediateStateDifferences = allStatesDiff;
    }
    
    /**
     * Calcula, entre outras informações, a diferencial de chave nas rodadas 
     * 'round1' e 'round1' + 1 que ativam a menor quantidade de bits nas rodadas
     * 'comparableRound' e ('comparableRound'+1).
     * 
     * @param debug seta se os prints de teste serão feitos ou não.
     * @param round1 é o primeiro índice das duas chaves consecutivas que serão
     * testadas. O máximo é 31 (Chaves variam de 0 a 32).
     * @param comparableRound é a rodada utilizada para checar a quantidade de 
     * nibbles ativos na chave. Não pode ser maior que 31.
     * 
     */
    public void getLowestDiffBasic(boolean debug, int comparableRound){
        
        Scanner scanner = new Scanner(System.in);
        Cipher cifra = cipher1.Reset(new ByteArray(32));
        //Cipher cifra = new Serpent();
        //cifra.setKey(new ByteArray(32));
        ByteArray expandedKey = cifra.getExpandedKey();
        
        ByteArray expandedKeyNoSbox = cifra.getExpandedKeyNoSbox();
        
        ByteArray leastActive = null;
        ByteArray leastActiveExpandedKey = null;
        int leastNumActive = 1000;
        int numMaxActNibbles;
        ArrayList<ByteArray> activeNibblesExpandedKey = null;
        
        for (int i = 0; i < 64; i++) {
            ArrayList<ByteArray> activeNibblesExpandedKeyAux = new ArrayList<>();
            for (int j = 0; j < cipher1.getNUM_KEYS(); j++) {
                activeNibblesExpandedKeyAux.add(new ByteArray(cipher1.getBLOCK_SIZE_IN_BYTES()));
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
                k0k1.copyBytes(expandedKeyNoSbox, 16*roundDelta, 0, 32);
                k0k1_.copyBytes(expandedKeyNoSbox, 16*roundDelta, 0, 32);

                k0k1_.setNibble(i, j^k0k1_.getNibble(i));
                if(debug) System.out.println("K \\oplus K' =\n"+k0k1.getDifference(k0k1_));
                if(debug) System.out.println("");
                
                expandedKeyk0k1 = cifra.getExpandedKey(k0k1,roundDelta);
                expandedKeyk0k1_ = cifra.getExpandedKey(k0k1_,roundDelta);
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
            System.out.println("Minimum #active nibbles in K"+(roundDelta+1)+"||K"+(roundDelta+2)+"\\oplus K`"+(roundDelta+1)+"||K`"+(roundDelta+2)+"=\n"+leastNumActive);
            System.out.println("");
        }
        
        ArrayList<Integer> activeNibbles = new ArrayList<>();
        for (int i = 0; i < leastActive.length()*2; i++) {
            if(leastActive.getNibble(i) != 0) activeNibbles.add(i);
        }
        this.amountAffectedNibbles = leastNumActive;
        this.lowestDifferential = new Difference(leastActive);
        this.fullLowestDifferential = leastActiveExpandedKey;
        this.posBaseKeyAffectedNibbles = activeNibbles;
        this.activeNibblesDeltaKey = activeNibblesExpandedKey;
        
        //Object[] result = {leastNumActive,new Difference(leastActive),leastActiveExpandedKey,activeNibbles,activeNibblesExpandedKey};
        if(debug){
            System.out.println((int)amountAffectedNibbles);
            System.out.println(lowestDifferential);
            System.out.println((ByteArray)fullLowestDifferential);
            for (int i = 0; i < ((ArrayList<Integer>)posBaseKeyAffectedNibbles).size(); i++) 
                System.out.println(((ArrayList<Integer>)posBaseKeyAffectedNibbles).get(i));
            System.out.println("Active Nibbles in the Expanded Key");
            for (int i = 0; i < ((ArrayList<ByteArray>)activeNibblesDeltaKey).size(); i++) {
                System.out.println(((ArrayList<ByteArray>)activeNibblesDeltaKey).get(i));
            }
        }
    }
    
    public void checkIndependance() {
        for (int i = 91; i < activeNibblesDifference.size(); i++) {
            for (int j = 0; j < activeNibblesDifference.get(i).length()*2; j++) {
                if(activeNibblesDifference.get(i).getNibble(j) == 0x1){
                    independent = false;
                    return;
                }
            }
        }
        for (int i = 31; i < activeNibblesDifferenceKey.size(); i++) {
            for (int j = 0; j < activeNibblesDifferenceKey.get(i).length()*2; j++) {
                if(activeNibblesDifferenceKey.get(i).getNibble(j) == 0x1){
                    independent = false;
                    return;
                }
            }
        }
        //printActiveNibblesDifferenceKey();
        independent = true;
    }
    
    public boolean isIndependent(){
        return independent;
    }
    
    public void findIndependentNibbles(){
        for (int i = 0; i < activeNibblesDifferenceKey.size(); i++) {
            for (int j = 0; j < activeNibblesDifferenceKey.get(i).length()*2; j++) {
                if(activeNibblesDifferenceKey.get(i).getNibble(j) == 0xa)
                    independentNibbles.add(new IndependentNibble(i, j, true));
                else if(activeNibblesDifferenceKey.get(i).getNibble(j) == 0xb)
                    independentNibbles.add(new IndependentNibble(i, j, false));
            }
        }
    }
    
    public void computeCandidatesOriginal(int roundDelta, int roundNabla){        
        //Nabla
        ArrayList<Integer> indexActiveNibbles = nabla.keyDifference.getActiveNibbles();
        boolean temp = true;
        for(int active : indexActiveNibbles){
            //if(temp) temp = false;
            //else{
            for (int i = 0; i < 16; i++) {
                ByteArray key1 = new ByteArray(32);
                ByteArray key2 = key1.clone();

                key2.setNibble(active, i);

                Cipher cifra1 = cipher1.Reset(key1, roundNabla);
                Cipher cifra2 = cipher2.Reset(key2, roundNabla);

                ByteArray keyDifference = cifra1.getExpandedKey(key1, roundNabla).clone().getDifference(cifra2.getExpandedKey(key2, roundNabla));
                for (int k = 0; k < keyDifference.length()*2; k++) {
                    int key = k/32;
                    int nibble = k%32;

                    //Busca se o nibble está na lista de nibbles independentes
                    int aux = independentNibbles.indexOf(new IndependentNibble(key, nibble, false));
                    if(aux != -1) independentNibbles.get(aux).valueShowedUp(true, keyDifference.getNibble(k));
                    /*//Busca se o nibble está na lista de nibbles candidatos
                    int aux = candidateNibbles.indexOf(new IndependentNibble(key, nibble, active, false));
                    if(aux != -1){
                        candidateNibbles.add(independentNibbles.get(aux).clone());
                        candidateNibbles.get(candidateNibbles.size()).origin = active;
                        candidateNibbles.get(candidateNibbles.size()).valueShowedUp(true, keyDifference.getNibble(k));
                    }*/
                }   
            }
            //temp = false;
            //break;
            //}
        }
        //Delta
        indexActiveNibbles = delta.keyDifference.getActiveNibbles();
        for(int active : indexActiveNibbles){
            for (int i = 0; i < 16; i++) {
                ByteArray key1 = new ByteArray(32);
                ByteArray key2 = key1.clone();

                key2.setNibble(active, i);

                Cipher cifra1 = cipher1.Reset(key1, roundDelta);
                Cipher cifra2 = cipher2.Reset(key2, roundDelta);

                ByteArray keyDifference = cifra1.getExpandedKey(key1, roundDelta).clone().getDifference(cifra2.getExpandedKey(key2, roundDelta));
                for (int k = 0; k < keyDifference.length()*2; k++) {
                    int key = k/32;
                    int nibble = k%32;

                    //Busca se o nibble está na lista de nibbles independentes
                    int aux = independentNibbles.indexOf(new IndependentNibble(key, nibble, true));
                    if(aux != -1) independentNibbles.get(aux).valueShowedUp(true, keyDifference.getNibble(k));
                }   
            }
        }
        for (int i = 0; i < independentNibbles.size(); i++) {
            if(independentNibbles.get(i).isCandidate())
                candidateNibbles.add(independentNibbles.get(i));
        }
    }

    public void computeCandidates(){        
        //Nabla
        ArrayList<Integer> indexActiveNibbles = nabla.keyDifference.getActiveNibbles();
        boolean temp = true;
        for(int active : indexActiveNibbles){
            //if(temp) temp = false;
            //else{
            for (int i = 0; i < 16; i++) {
                ByteArray key1 = new ByteArray(32);
                ByteArray key2 = key1.clone();

                key2.setNibble(active, i);

                Cipher cifra1 = cipher1.Reset(key1, roundNabla);
                Cipher cifra2 = cipher2.Reset(key2, roundNabla);

                ByteArray keyDifference = cifra1.getExpandedKey(key1, roundNabla).clone().getDifference(cifra2.getExpandedKey(key2, roundNabla));
                for (int k = 0; k < keyDifference.length()*2; k++) {
                    int key = k/32;
                    int nibble = k%32;

                    /*//Busca se o nibble está na lista de nibbles independentes
                    int aux = independentNibbles.indexOf(new IndependentNibble(key, nibble, false));
                    if(aux != -1) independentNibbles.get(aux).valueShowedUp(true, keyDifference.getNibble(k));*/
                    //Busca se o nibble está na lista de nibbles candidatos
                    int aux = independentNibbles.indexOf(new IndependentNibble(key, nibble, false));
                    if(aux != -1){
                        int aux2 = candidateNibbles.indexOf(new IndependentNibble(key, nibble, active, false));
                        if(aux2 == -1){
                            candidateNibbles.add(independentNibbles.get(aux).clone());
                            aux2 = candidateNibbles.size()-1;
                            candidateNibbles.get(aux2).origin = active;
                        }
                        candidateNibbles.get(aux2).valueShowedUp(true, keyDifference.getNibble(k));
                    }
                    
                }   
            }
            //temp = false;
            //break;
            //}
        }
        //Delta
        indexActiveNibbles = delta.keyDifference.getActiveNibbles();
        for(int active : indexActiveNibbles){
            for (int i = 0; i < 16; i++) {
                ByteArray key1 = new ByteArray(32);
                ByteArray key2 = key1.clone();

                key2.setNibble(active, i);

                Cipher cifra1 = cipher1.Reset(key1, roundDelta);
                Cipher cifra2 = cipher2.Reset(key2, roundDelta);

                ByteArray keyDifference = cifra1.getExpandedKey(key1, roundDelta).clone().getDifference(cifra2.getExpandedKey(key2, roundDelta));
                for (int k = 0; k < keyDifference.length()*2; k++) {
                    int key = k/32;
                    int nibble = k%32;

                    /*//Busca se o nibble está na lista de nibbles independentes
                    int aux = independentNibbles.indexOf(new IndependentNibble(key, nibble, true));
                    if(aux != -1) independentNibbles.get(aux).valueShowedUp(true, keyDifference.getNibble(k));*/
                    //Busca se o nibble está na lista de nibbles candidatos
                    int aux = independentNibbles.indexOf(new IndependentNibble(key, nibble, -1, true));
                    if(aux != -1){
                        int aux2 = candidateNibbles.indexOf(new IndependentNibble(key, nibble, active, true));
                        if(aux2 == -1){
                            candidateNibbles.add(independentNibbles.get(aux).clone());
                            aux2 = candidateNibbles.size()-1;
                            candidateNibbles.get(aux2).origin = active;
                        }
                        candidateNibbles.get(aux2).valueShowedUp(true, keyDifference.getNibble(k));
                    }
                }   
            }
        }
        /*for (int i = 0; i < independentNibbles.size(); i++) {
            if(independentNibbles.get(i).isCandidate())
                candidateNibbles.add(independentNibbles.get(i));
        }*/
        
        //Remove os nibbles independentes que não são candidatos
        int i = 0;
        int size = candidateNibbles.size();
        while(i < size) {
            if(!(candidateNibbles.get(i).isCandidate())){
                candidateNibbles.remove(i);
                i--;
                size--;
            }
            i++;
        }
        
        //Remove os nibbles candidatos que são afetados por mais de um nibble.
        LinkedList<Integer>  aux = new LinkedList<>();
        i = 0;
        size = candidateNibbles.size();
        while(i < size){
            //Encontra as posições dos nibbles "iguais" ao nibble 'i', caso haja.
            for (int j = i+1; j < size; j++) {
                if( candidateNibbles.get(i).delta == candidateNibbles.get(j).delta &&
                    candidateNibbles.get(i).pos == candidateNibbles.get(j).pos &&
                    candidateNibbles.get(i).subkey == candidateNibbles.get(j).subkey){
                    aux.add(j);
                }
            }
            //Remove dos candidatos os nibbles repetidos, caso haja.
            for (int j : aux) {
                candidateNibbles.remove(j);
                size--;
            }
            
            //Se havia algum nibble repetido antes, o próprio 'i' deve ser também removido.
            if(aux.size() > 0){
                aux.clear();
                candidateNibbles.remove(i);
                size--;
                i--;
            }
            
            i++;
        }
    }

    public void computeBaseKeyCandidates(){
        for (int i = 0; i < candidateNibbles.size(); i++) {
            int aux = -1;
            for (int j = 0; j < baseKeyCandidates.size(); j++){
                if(candidateNibbles.get(i).subkey == baseKeyCandidates.get(j).subkey){
                    aux = j;
                    break;
                }
            }    
            
            if (aux == -1){
                int lastPos = baseKeyCandidates.size();
                baseKeyCandidates.add(new BaseKey(candidateNibbles.get(i).subkey));
                baseKeyCandidates.get(lastPos).candidates.add(candidateNibbles.get(i));
            }else baseKeyCandidates.get(aux).candidates.add(candidateNibbles.get(i));
        }
        
        //Remove as chaves que não são candidatas
        int i = 0;
        int size = baseKeyCandidates.size();
        while(i < size) {
            if(!(baseKeyCandidates.get(i).isCandidate())){
                baseKeyCandidates.remove(i);
                i--;
                size--;
            }
            i++;
        }
        
        baseKeyCandidates.sort((BaseKey o1, BaseKey o2) -> o1.subkey - o2.subkey);
    }
    
    public void printIndependentNibbles(){
        System.out.println("Nibbles that are independent from either nabla or delta differentials: ");
        for (int i = 0; i < independentNibbles.size(); i++)
            System.out.println(independentNibbles.get(i).toStringBasic());
    }    
    
    public void printCandidates(){
        System.out.println("Nibbles that are candidates for the Base Key: ");
        for (int i = 0; i < candidateNibbles.size(); i++)
            System.out.println(candidateNibbles.get(i));    
    }
    
    public void printBaseKeyCandidates(){
        System.out.println("Candidates for the Base Key: ");
        for (int i = 0; i < baseKeyCandidates.size(); i++)
            System.out.println(baseKeyCandidates.get(i));    
    }

    private class BaseKey{
        int subkey;
        public ArrayList<IndependentNibble> candidates;

        public BaseKey(int subkey) {
            this.subkey = subkey;
            candidates = new ArrayList<>();
        }
        
        public boolean isCandidate(){
            boolean nabla = false;
            boolean delta = false;
            for (IndependentNibble nibble : candidates) {
                if(nibble.delta) delta = true;
                else nabla = true;
                if(delta && nabla) return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + this.subkey;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BaseKey other = (BaseKey) obj;
            if (this.subkey != other.subkey) {
                return false;
            }
            return true;
        }
        
        @Override
        public String toString(){
            String result = "K["+subkey+"] = {\n";
            for (IndependentNibble nibble : candidates) result += nibble.toStringBasic() + "\n";
            return result + "}";
        }
    }
    
    private class IndependentNibble implements Cloneable{
        
        int subkey;                 //subchave a qual o nibble está associado.
        int pos;                    //posição na subchave a qual o nibble está.
        int origin;                 //é a posição do nibble que gerou este nibble.
        boolean delta;              //é verdadeiro se for ativo por delta e falso se for por nabla.
        int valoresPossiveis[];     //vetor que atesta se todos os valores possíveis apareceram para aquele nibble.

        public IndependentNibble(int subkey, int pos, boolean delta) {
            this.subkey = subkey;
            this.delta = delta;
            this.origin = -1;
            this.pos = pos;
            valoresPossiveis = new int[16];
        }
        
        public IndependentNibble(int subkey, int pos, int origin, boolean delta) {
            this.subkey = subkey;
            this.delta = delta;
            this.origin = origin;
            this.pos = pos;
            valoresPossiveis = new int[16];
        }
        
        public void valueShowedUp(boolean positive, int value){
            if(positive) valoresPossiveis[value]++;
            else valoresPossiveis[value]+=5;
        }
        
        public boolean isCandidate(){
            if (valoresPossiveis[0] == 0) return false;
            
            int aux = valoresPossiveis[0];
            for (int i = 1; i < valoresPossiveis.length; i++) 
                if(valoresPossiveis[i] != valoresPossiveis[0]) return false;
            return true;
        }
        
        @Override
        public IndependentNibble clone() {
            IndependentNibble newNibble = new IndependentNibble(subkey, pos, origin, delta);
            System.arraycopy(valoresPossiveis, 0, newNibble.valoresPossiveis, 0, valoresPossiveis.length);
            return newNibble;
        }
        
        @Override
        public String toString(){
            String result = "";
            if(subkey<10) result += " K" + subkey + "[";
            else result += "K" + subkey + "[";
            if(pos < 10) result += " " + pos + "] from nibble ";
            else result += pos + "] from nibble ";
            if(origin == -1) result += "?? of ";
            else if(origin < 10) result += " " + origin + " of ";
            else  result += origin + " of ";
            if(delta)   result += "delta is ";
            else        result += "nabla is ";
            if(isCandidate()) return result += "a candidate. (" + Arrays.toString(valoresPossiveis) + ")";
            return result += "NOT a candidate. (" + Arrays.toString(valoresPossiveis) + ")";
        }
        
        public String toStringBasic(){
            String result = "";
            if(subkey<10) result += " K" + subkey + "[";
            else result += "K" + subkey + "[";
            if(pos < 10) result += " " + pos + "] from nibble ";
            else result += pos + "] from nibble ";
            if(origin == -1) result += "?? of ";
            else if(origin < 10) result += " " + origin + " of ";
            else  result += origin + " of ";
            if(delta)   return result + "delta.";
            return result + "nabla.";
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + this.subkey;
            hash = 79 * hash + this.pos;
            hash = 79 * hash + this.origin;
            hash = 79 * hash + (this.delta ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IndependentNibble other = (IndependentNibble) obj;
            if (this.subkey != other.subkey) {
                return false;
            }
            if (this.pos != other.pos) {
                return false;
            }
            if (this.origin != other.origin) {
                return false;
            }
            if (this.delta != other.delta) {
                return false;
            }
            return true;
        }

        
    }
}
