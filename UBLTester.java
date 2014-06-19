package UBL;
/**
 *
 * @author Sonia Ghanekar
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class UBLTester {
    //This is the window size used for measuring the true positive rates
    private static final int WINDOW_SIZE = 40;

    int inputDataSet;
    LearningManager learningManager;
    SOM trainedSOM;

    public UBLTester(int dataSize, File trainingFile) {
        inputDataSet = dataSize;
        learningManager = new LearningManager(dataSize, trainingFile);
    }

    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("Usage: UBLTester training_file training_datasize testdata_file");
            System.exit(1);
        }
        UBLTester ublTester;

        ublTester = new UBLTester(Integer.parseInt(args[1]), new File(args[0]));
        ublTester.train();
        ublTester.testFromFile(new File(args[2]));
    }

    // This function trains the SOM
    private void train() {
        trainedSOM = learningManager.train();
    }

    private void testFromFile(File file) {
        try {
            Scanner scanner = new Scanner(file, "UTF-8");
            int i=0;
            int memAnomaly=0;
            int cpuAnomaly=0;
            int normal=0;
            int actNormal = 0;
            int actAnomalous = 0;
            int truePositive = 0;
            int falsePositive = 0;
            WeightVector inputVector;
            int window[] = new int[WINDOW_SIZE];
            int windowF[] = new int[WINDOW_SIZE];
            for(i=0; i<WINDOW_SIZE; i++) {
                window[i] = 0;
                windowF[i] = 0;
            }

            double smooth_cpu[] = new double[5];
            double smooth_mem[] = new double[5];

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] strings = line.split(" ");
                double[] metrics = new double[2];

                //reading metrics from file
                metrics[0] = Math.round((Double.parseDouble(strings[4]))*100/Double.parseDouble(strings[2]));
                metrics[1] = Math.round((Double.parseDouble(strings[8]))*100/Double.parseDouble(strings[6]));

                //smoothing metrics
                if(i<4) {
                    smooth_cpu[i] = metrics[0];
                    smooth_mem[i] = metrics[1];
                }
                else {
                    for(int kk=0; kk<4; kk++) {
                        smooth_cpu[kk] = smooth_cpu[kk+1];
                        smooth_mem[kk] = smooth_mem[kk+1];
                    }
                    smooth_cpu[4] = metrics[0];
                    smooth_mem[4] = metrics[1];
                    metrics[0] = 0;
                    metrics[1] = 0;
                    for(int kk=0; kk<5; kk++) {
                        metrics[0]+=smooth_cpu[kk];
                        metrics[1]+=smooth_mem[kk];
                    }
                    metrics[0] = metrics[0] / 5.0;
                    metrics[1] = metrics[1] / 5.0;
                }

                if(Integer.parseInt(strings[27])==2)
                    actAnomalous++;
                else
                    actNormal++;
                inputVector = new WeightVector(metrics);

                //predict state for input vector
                int prediction = trainedSOM.testInput(inputVector);

                for(int k=0; i<WINDOW_SIZE-1; k++)
                    window[k] = window[k+1];
                window[WINDOW_SIZE-1] = prediction;

                for(int k=0; i<WINDOW_SIZE-1; k++)
                    windowF[k] = windowF[k+1];
                windowF[WINDOW_SIZE-1] = prediction;


                //calculation for true postives
                if(Integer.parseInt(strings[27])!=0) {
                    for(int k=0; k<WINDOW_SIZE; k++) {
                        if(window[k]!=0) {
                            truePositive++;
                            window[k] = 0;
                        }
                    }
                }
                //calculation of false positives
                else {
                    for(int k=0; k<WINDOW_SIZE; k++)
                        if(windowF[k]!=0) {
                            falsePositive++;
                            windowF[k] = 0;
                        }
                }
                if(prediction == 1) {
                    cpuAnomaly++;
                }
                else if(prediction == 2) {
                    memAnomaly++;
                }
                else {
                    normal++;
                }
                i++;
            }
            System.out.println("Normal :" + normal);
            System.out.println("Memory Anomaly : " + memAnomaly);
            System.out.println("CPU Anomaly " + cpuAnomaly);
            System.out.println("Actual normal " + actNormal);
            System.out.println("Actual Anomaly" + actAnomalous);
            System.out.println("true positive " + truePositive);
            System.out.println("true positive %" + (double)truePositive*100/(double)actAnomalous);
            System.out.println("false positive " + falsePositive);
            System.out.println("false positive %" + (double)falsePositive*100/(double)actNormal);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
