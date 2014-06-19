package UBL;
/**
@author mahek
*/

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.distance.dtw.DTWSimilarity;
import net.sf.javaml.distance.fastdtw.dtw.DTW;
import net.sf.javaml.distance.fastdtw.timeseries.TimeSeries;
import java.util.List;

public class SignatureDriven {
    

    ReadInput ri=new ReadInput();
    int sample_size=1440;
    
    double fft(List<Double> cpu)
    /* Applies FFT on input cpu and calculates dominating frequency */
    {
        double a[]=new double[cpu.size()];
        for(int i=0;i<cpu.size();i++)
        {
            a[i]=cpu.get(i);
        }
        int N=cpu.size();   //Maximum Frequency
        
        DoubleFFT_1D fftcpu= new DoubleFFT_1D(sample_size);
        double result[]= new double[sample_size*2];
        System.arraycopy(a, a.length-(sample_size+1), result, 0, sample_size);
        fftcpu.complexForward(result);
        
        /*To calculate Magnitude of power at each interval*/
        double fina[]=new double[a.length];
        for(int i=0;i<result.length/2;i++)
        {
            fina[i]=Math.sqrt(result[2*i]*result[2*i]+result[2*i+1]*result[2*i+1]);
        }
        
        //To find maximum power and index
        double max=Double.MIN_VALUE;
        int index=0;
        for(int i=0;i<fina.length;i++)
        {
            if(max<fina[i])
            {
                max=fina[i];
                index=i;
            }
        }
        
        //Returning dominating frequency
        return 1.0*index/N;
    }
 
    int signature(String para, double metricValue)
/*This is the main function called by UBL to start with prevention technique */
    {
        int max=Integer.MIN_VALUE;
        //read input metrics of around 1440 samples
        List<Double> cpu=ri.read(para);
	
	/*First we need to identify the presence of signature so we start with applying FFT on input signal */        
        //pass the input values to fft
        double fd=fft(cpu);
        
	//Split the input array into Q size based on fd values
        int q,z;
        if(fd==0)
        {
            z=1;
            q=cpu.size();
        }
        else
        {
            z=(int)Math.ceil(1/fd);
            q=(int) (cpu.size()/z);
        }
	/* Dividing the input signal into windows of size W which is q here*/        
	double l[][];
        l = new double[q+1][z];
        for(int i=0;i<cpu.size();i++)
        {
            l[i/z][i%z]=cpu.get(i);
        }
        
        //Calculating Pearson and checking for signatures
       if(pearson_calc(l))
       {
            double avg[]=sign(l,q,z);
            dtw(avg,l[l.length-1]);
           for(int i=0;i<avg.length;i++)
           {
               if(max<avg[i])
               {
                   max=(int)Math.round(avg[i]);
               }
           }
           return (int)Math.round(max*1.05);	//Returning predicted value with 5% padding
       }
	/* If no signature is identified then we use state driven approach which uses Markov's discrete time series. */
        else
        {
	/* Pass para as input from cause inference for Markov model to construct a P matrix */
            StateDriven st=new StateDriven(para);

	/*Create an input array from read resource data and pass it for training */
            int inp[]=new int[sample_size];
            for(int i=0;i<inp.length;i++)
            {
                inp[i]= (int) Math.round(cpu.get(cpu.size()-(sample_size+1)+i));
            }
            st.train(inp);

	/* After training we can identify the state of system after time 60 sec from current state. */
            return (int)Math.round(1.05*st.predict(60, (int)Math.round(metricValue))); // requesting for a prediction value after 60 secs

        }
    }
    boolean pearson_calc(double l[][])
/*Actual method called by signature to identify if there is a signature. 
1. Calculate mean of two windows and find the ratio and check if it is <=0.05
2. Calculate pearson correlation and check if it is >=0.85
*/
    {
        double mean[]=new double[l.length];
        for(int i=0;i<l.length;i++)
        {
            for(int j=0;j<l[i].length;j++)
            {
                mean[i]+=(double)l[i][j];
            }
            mean[i]=mean[i]/l[i].length;
        }
        boolean p[][]=new boolean[l.length][l[0].length];
        for(int i=0;i<l.length;i++)
        {
            for(int j=i+1;j<l.length;j++)
            {
                if(pearson(l[i],l[j])>=0.85 && mean[i]/mean[j]>=0.95)
                {
                    p[i][j]=true;
                }
                else
                {
                   return false;
                }
            }
        }
        return true;
    }

    double pearson(double x[], double y[])
/* To calculate Pearson correlation between two windows */
    {
        double res;
        double meanx=0,meany=0;
        double num=0,den1=0,den2=0;
        for(int i=0;i<x.length;i++)
        {
            meanx+=x[i];
            meany+=y[i];
        }
        meanx=meanx/(x.length); //mean of window x
        meany=meany/(y.length); //mean of window y
        float sum1=0,sum2=0;
        for(int i=0;i<x.length;i++)
        {
            den1=(x[i]-meanx);
            den2=(y[i]-meany);
            num+=den1*den2;
            sum1+=den1*den1;
            sum2+=den2*den2;
        }
        sum1=(float) Math.sqrt(sum1);
        sum2=(float) Math.sqrt(sum2);
        res=num/(sum1*sum2); //it gives coefficeint of Pearson
        return res;
    }
    double [] sign(double l[][],int q,int z)
/* This function is used for calculating signature from observed time series */
    {
        double avg[]= new double[z];
        for(int i=0;i<z;i++)
        {
                for(int j=0;j<q;j++)
                {
                    avg[i]+=l[j][i]; 	//Calculates the average value at each position in window.
                }
                avg[i]=avg[i]/q;
        }
        return avg;
    }

    double dtw(double[] a, double[] b)
/* DTW method for finding the position of current state in signature */
    {
        double w1[]=new double[a.length];
        double w2[]=new double[b.length];
        for(int i=0;i<w1.length;i++)
        {
            w1[i]=(double)a[i];
            w2[i]=(double)b[i];
        }
        DenseInstance window1=new DenseInstance(w1);
        DenseInstance window2=new DenseInstance(w2);
        TimeSeries t1=new TimeSeries(window1);
        TimeSeries t2=new TimeSeries(window2);
        double res=DTW.getWarpDistBetween(t1, t2);	//We identify the distance between two windows
        DTWSimilarity simi=new DTWSimilarity();
        System.out.println(simi.measure(window1, window2)+" "+res); //And calculate the similarity between two windows.
        return simi.measure(window1, window2);
     }
}
