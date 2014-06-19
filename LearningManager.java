package UBL;
/**
 *
 * @author Sonia Ghanekar
 */

import java.io.File;
import java.util.ArrayList;

public class LearningManager {
    //training data size
    int inputDataSize;

    //weight vectors with which to train
    ArrayList<WeightVector> inputVectors;

    // K for K-cross fold validation
    final int K = 5;

    // used for our integrated code with two separate logs for CPU and memory
    public LearningManager(int inputDataSize, File cpu_log, File mem_log) {
        this.inputDataSize = inputDataSize;
        FileParser parser = new FileParser(cpu_log, mem_log);
        inputVectors = parser.createWeightVectors(inputDataSize);
    }

    // used while training with Daniel's data set
    public LearningManager(int inputDataSize, File trainFile) {
        this.inputDataSize = inputDataSize;
        FileParser parser = new FileParser(trainFile, null);
        inputVectors = parser.createWeightVectorsFromTestFile(inputDataSize,trainFile);
    }

    // function used for training
    public SOM train() {
        WeightVector[][] inputSets = createInputSets();
        SOM som = kFoldCrossValidation(inputSets);
        System.out.println("SOM Trained ");
        return som;
    }

    // function for doing K fold cross validation
    private SOM kFoldCrossValidation(WeightVector[][] inputSets) {
        SOM[] soms = trainWithOtherSets(inputSets);
        for(int i=0; i<K; i++)
            soms[i].classifyNeurons();
        for(int i=0; i<K; i++)
            soms[i].calculateAccuracy(inputSets[i]);
        return getBestAccuracy(soms);
    }

    // get the SOM with the best accuracy
    private SOM getBestAccuracy(SOM[] soms) {
        double accuracy = 0;
        SOM bestSOM = null;
        for(int i=0; i<soms.length; i++){
            System.out.println("Som " + i + " accuracy: " + soms[i].getAccuracy());
            if(soms[i].getAccuracy()> accuracy) {
                accuracy = soms[i].getAccuracy();
                bestSOM = soms[i];
            }
        }
        return bestSOM;
    }

    // trains for K fold cross validation
    private SOM[] trainWithOtherSets(WeightVector[][] inputSets) {
        SOM[] soms = new SOM[K];
        for(int i=0; i<K; i++)
            soms[i] = new SOM();
        int currentK=0;
        while(currentK<K) {
            for(int i=0; i<K; i++) {
                if(i!=currentK) {
                    for(int k=0; k<inputSets[i].length; k++) {
                        soms[currentK].trainWithInput(inputSets[i][k]);
                    }
                }
            }
            currentK++;
        }
        return soms;
    }

    // divides training data into K sets
    private WeightVector[][] createInputSets() {
        int setSize = inputDataSize / K;
        WeightVector inputSet[][] = new WeightVector[K][setSize];
        int i= 0;
        int m = 0;
        for(i=0; i<K; i++) {
            for(int j=0; j<setSize; j++) {
                inputSet[i][j] = inputVectors.get(m);
                m++;
            }
        }
        return inputSet;
    }
}
