package rcat;

import java.io.*;
import java.awt.Frame;
import java.awt.FileDialog;

import java.util.Enumeration;

import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RList;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.RMainLoopCallbacks;

class TextConsole3 implements RMainLoopCallbacks
{
    public void rWriteConsole(Rengine re, String text, int oType) {
        System.out.print(text);
    }
    
    public void rBusy(Rengine re, int which) {
        System.out.println("rBusy("+which+")");
    }
    
    public String rReadConsole(Rengine re, String prompt, int addToHistory) {
        System.out.print(prompt);
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
            String s=br.readLine();
            return (s==null||s.length()==0)?s:s+"\n";
        } catch (Exception e) {
            System.out.println("jriReadConsole exception: "+e.getMessage());
        }
        return null;
    }
    
    public void rShowMessage(Rengine re, String message) {
        System.out.println("rShowMessage \""+message+"\"");
    }
	
    public String rChooseFile(Rengine re, int newFile) {
	FileDialog fd = new FileDialog(new Frame(), (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
	fd.show();
	String res=null;
	if (fd.getDirectory()!=null) res=fd.getDirectory();
	if (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile());
	return res;
    }
    
    public void   rFlushConsole (Rengine re) {
    }
	
    public void   rLoadHistory  (Rengine re, String filename) {
    }			
    
    public void   rSaveHistory  (Rengine re, String filename) {
    }			
}

public class rcat {
    public static void main(String[] args) {
	// just making sure we have the right version of everything
	if (!Rengine.versionCheck()) {
	    System.err.println("** Version mismatch - Java files don't match library version.");
	    System.exit(1);
	}
        System.out.println("Creating Rengine (with arguments)");
		// 1) we pass the arguments from the command line
		// 2) we won't use the main loop at first, we'll start it later
		//    (that's the "false" as second argument)
		// 3) the callbacks are implemented by the TextConsole class above
		Rengine re=new Rengine(args, false, new TextConsole3());
        System.out.println("Rengine created, waiting for R");
		// the engine creates R is a new thread, so we should wait until it's ready
        if (!re.waitForR()) {
            System.out.println("Cannot load R");
            return;
        }

		/* High-level API - do not use RNI methods unless there is no other way
			to accomplish what you want */
		try {
			REXP x;
			re.eval("data(iris)",false);

	/* iris é um banco de dados da memoria do R */
			
			System.out.println(x=re.eval("iris"));

		// Pega os valores do banco de dados iris
			
		// generic vectors are RVector to accomodate 
			RVector v = x.asVector();
		if (v.getNames()!=null) {
		// Pega os nomes dos vetores em x 
		// Ou seja pega o nome das colunas do banco de dados 
				System.out.println("has names:");
			
		// Enquanto for diferente de nulo,mostra os nomes)	
				for (Enumeration e = v.getNames().elements() ; e.hasMoreElements() ;) {
					System.out.println(e.nextElement());
				}
			}
			// for compatibility with Rserve we allow casting of vectors to lists
		// Transformando vetores em listas para que o R entenda 
			RList vl = x.asList();
			String[] k = vl.keys();
			if (k!=null) {
				System.out.println("and once again from the list:");
				int i=0; while (i<k.length) System.out.println(k[i++]);
			}			

		// É uma função lógica, utilizando a primeira coluna do banco de dados 
		// Compara os valores em cada linha dessa coluna e verifica se são maiores que a média da primeira coluna
		// Se o valor é maior retorna 1 = TRUE se não retorna 0 = FALSE 
			// get boolean array
			System.out.println(x=re.eval("iris[[1]]>mean(iris[[1]])"));
			// R knows about TRUE/FALSE/NA, so we cannot use boolean[] this way
			// instead, we use int[] which is more convenient (and what R uses internally anyway)
			int[] bi = x.asIntArray();
			{
			    int i = 0; while (i<bi.length) { System.out.print(bi[i]==0?"F ":(bi[i]==1?"T ":"NA ")); i++; }
			    System.out.println("");
			}
			
			// push a boolean array
			boolean by[] = { true, false, false };
			re.assign("bool", by);
			System.out.println(x=re.eval("bool"));
			// asBool returns the first element of the array as RBool
			// (mostly useful for boolean arrays of the length 1). is should return true
			System.out.println("isTRUE? "+x.asBool().isTRUE());

			// now for a real dotted-pair list:
			System.out.println(x=re.eval("pairlist(a=1,g='foo',c=1:5)"));
			RList l = x.asList();
			if (l!=null) {
				int i=0;
				String [] a = l.keys();
				System.out.println("Keys:");
				while (i<a.length) System.out.println(a[i++]);
				System.out.println("Contents:");
				i=0;
				while (i<a.length) System.out.println(l.at(i++));
			}
			System.out.println(re.eval("sqrt(36)"));
		} catch (Exception e) {
			System.out.println("EX:"+e);
			e.printStackTrace();
		}
		
		// Part 2 - low-level API - for illustration purposes only!
		//System.exit(0);
		
        // simple assignment like a<-"hello" (env=0 means use R_GlobalEnv)
        long xp1 = re.rniPutString("hello");
        re.rniAssign("a", xp1, 0);
		
        REXP rexp;
        double a[] = {1.232,1.369,2.096,1.596,1.518,1.333,1.359,1.481,2.421,1.206,2.225,1.250,1.092,2.073,2.073,1.579,1.276,1.603,1.437,1.539,1.528,1.097,1.758,2.491,2.442,1.988,1.855,2.387,2.200,1.371,1.760,2.437,1.496,1.435,2.351,2.401,2.109,1.914,1.958,1.968,2.200,2.395,2.350,2.469,1.108,1.621,1.290,2.406,1.699,1.117,2.071,2.033,2.068,1.779,2.207,1.465,2.144,2.257,2.203,1.562,1.975,1.266,1.072,1.062,2.269,2.168,1.523,2.202,1.964,2.441,1.994,1.813,1.890,2.390,1.446,1.119,1.802,2.220,1.598,2.224,1.355,2.313,1.016,2.029,1.292,1.339,1.736,1.057,1.916,1.038,1.836,1.915,1.124,1.806,2.133,1.395,1.058,2.421,1.928,1.929,1.668,1.539,1.468,1.391,2.244,2.305,1.042,1.987,1.191,1.194,2.400,1.699,1.619,1.441,2.393,1.544,1.024,2.175,1.026,1.101,1.130,2.096,1.837,2.068,1.159,1.838,1.273,1.316,1.605,1.843,1.990,2.185,1.288,2.380,1.739,1.498,1.250,1.052,1.778,2.029,1.277,1.914,1.458,1.921,1.099,1.551,1.929,1.767,1.076,2.232,2.115,1.855,2.390,2.491,2.400,1.814,1.573,1.757,2.186,2.123,1.886,2.383,2.241,1.745,1.877,2.183,1.027,1.850,1.631,1.340,1.901,2.377,2.044,1.799,2.394,1.400,1.640,1.989,1.963,2.056,2.241,1.003,2.402,2.498,1.036,1.580,1.329,2.120,1.343,2.005,2.421,1.038,1.612,2.285,2.326,2.054,2.469,1.213,1.910,1.089,1.841,2.175,1.769,2.331,1.463,1.312,1.188,2.069,2.318,1.395,2.443,1.445,1.798,1.026,2.146,1.128,1.640,1.017,1.535,2.122,1.931,1.568,2.390,1.705,2.227,2.088,2.497,1.230,1.588,1.282,2.326,1.274,2.073,1.860,2.350,2.366,1.489,2.466,1.181,1.781,2.446,2.211,2.426,1.638,1.011,1.888,1.465,2.270,1.512,1.755,2.041,2.131,1.285,2.484,2.363,2.189,2.410,1.631,2.115,1.564,1.737,1.250,1.572,1.889,1.251,2.493,2.221,1.125,2.060,1.319,1.072,1.814,1.569,1.672,1.578,1.009,2.126,1.720,1.834,1.867,1.193,1.039,2.478,2.128,1.335,2.332,2.468,2.476,1.074,1.549,1.151,2.017,1.907,1.523,1.805,1.340,1.535,2.471,2.066,1.428,1.100,1.477,2.420,2.365,2.468,2.311,1.674,2.108,2.375,1.703,1.591,2.285,1.460,1.416,1.587,1.765,1.997,1.743,1.194,1.477,1.868,1.773,1.685,1.222,2.071,1.054,2.040,1.454,2.319,2.488,1.681,1.262,2.015,2.091,2.143,1.054,2.158,1.458,2.101,1.153,1.475,1.563,1.930,1.812,1.824,1.727,1.716,1.868,1.171,1.391,1.390,1.521,1.173,1.532,1.909,1.986,1.688,1.843,1.154,1.479,1.732,1.034,1.472,1.289,1.699,2.360,1.520,1.923,2.389,1.740,1.352,1.537,1.626,1.073,2.408,2.159,2.031,1.610,1.327,1.433,2.240,1.999,1.263,2.363,1.607,2.350,2.120,2.286,1.201,1.436,2.381,1.086,1.135,1.895,2.451,1.842,1.027,1.531,1.686,1.937,2.145,2.081,1.532,1.821,1.513,1.553,2.439,2.456,2.061,1.501,2.488,2.131,2.074,1.338,1.879,1.175,1.520,2.050,1.727,1.271,2.036,1.927,1.315,1.867,2.126,2.155,1.743,1.426,1.861,1.897,2.140,1.479,2.142,2.201,2.353,1.743,2.306,1.371,1.295,2.257,2.127,1.133,1.875,2.301,1.525,1.722,1.165,1.556,2.497,2.251,2.249,2.262,2.106,2.457,2.409,1.338,2.036,1.196,1.580,1.216,1.045,1.297,2.236,1.359,1.835,1.118,1.138,2.208,1.657,1.941,1.112,1.538,1.179,1.033,1.278,1.455,2.430,1.015,1.406,1.523,1.859,2.369,1.312,2.253,1.805,2.212,2.477,1.935,1.431,2.195,1.805,2.062,2.408,2.117,1.190,1.305,2.410,1.646,1.819,2.296};
        double g[] = {0.872,3.280,-1.214,-3.823,-3.504,-3.667,3.960,0.823,-5.278,2.614,3.403,3.408,3.063,4.179,3.488,4.575,1.646,1.509,-0.462,1.317,1.175,0.950,-4.328,0.508,1.082,2.339,-5.295,-4.392,-6.486,2.005,-4.769,6.529,-1.840,-2.726,-6.665,0.485,2.528,-2.873,1.745,-3.255,5.620,4.334,6.147,2.098,0.086,-0.109,0.980,-7.015,4.681,1.805,6.151,-5.198,4.960,2.597,-3.651,-1.579,-3.976,-3.037,5.486,3.446,-3.035,-0.961,2.100,0.606,-0.712,-2.752,-0.504,4.212,-5.543,-2.738,-0.345,-3.228,-3.467,3.410,2.794,-2.462,4.165,5.866,-2.508,-3.181,-3.952,-5.906,-2.463,-0.548,-0.006,-1.405,0.106,2.246,5.312,-1.845,-1.890,-2.662,-0.570,3.768,-5.018,1.023,1.901,4.273,1.921,2.024,2.000,-2.790,-1.727,1.908,-5.441,6.374,-1.307,0.552,-0.119,1.744,3.006,-3.081,-2.807,-1.522,-3.666,-4.156,-0.734,-2.897,-2.881,0.868,-2.376,-0.717,-4.112,0.731,1.963,-3.959,1.678,-1.605,-1.997,1.373,2.033,5.429,-3.686,3.063,-0.358,0.832,-3.734,-2.492,-3.316,-4.975,-0.571,-2.367,0.862,4.484,-2.621,1.058,4.679,-2.943,-1.831,-2.219,-1.990,2.689,-3.803,-2.993,-5.167,5.412,-1.685,-4.521,6.000,-0.231,-4.665,-6.497,-1.268,-0.604,-5.271,0.222,1.506,-3.529,0.039,-2.820,-1.917,-6.089,0.592,3.582,6.293,4.158,-0.404,-1.403,0.084,-0.980,-1.370,-2.587,-0.490,5.514,2.596,-0.720,-2.624,5.524,1.845,-2.402,1.246,0.837,0.628,2.329,4.033,-2.963,-0.348,-2.888,-1.185,-0.149,-0.992,-1.108,0.032,5.448,-0.817,-0.464,-1.954,3.341,-3.216,-0.726,-0.667,-2.665,-5.009,2.491,5.041,-1.437,-3.771,1.893,4.458,4.017,-0.927,-1.563,4.842,3.119,-2.376,-0.606,3.078,-0.301,3.113,2.421,-4.917,-3.425,3.100,0.762,-0.633,-6.635,-2.002,-6.674,-0.498,-4.709,3.524,4.418,-5.586,-3.153,2.778,-4.511,0.696,5.064,4.263,3.109,-2.229,-4.100,0.214,0.320,-1.205,1.807,-4.675,1.854,-6.223,-2.548,-2.390,2.526,-2.907,-4.830,2.010,1.770,5.460,-2.782,-4.351,-0.579,3.124,-2.105,-1.888,-3.892,3.134,-2.337,2.715,2.770,5.036,-4.653,2.382,-2.737,5.627,2.026,3.241,-6.213,-0.170,-1.924,-2.163,-4.409,0.168,4.216,2.465,-2.757,-3.340,-1.570,3.974,6.208,0.709,2.016,1.549,-2.749,4.342,0.649,1.359,-6.039,4.081,4.113,0.947,3.634,4.049,6.646,-1.921,1.127,-1.524,5.080,-4.536,4.959,1.415,2.973,0.449,-0.570,-4.638,1.421,3.562,1.723,-2.709,-3.493,5.906,-4.983,-4.477,1.875,-0.401,3.716,-3.300,-0.598,-0.241,3.048,1.484,-1.838,0.034,-3.206,3.992,-3.001,-4.300,3.926,-1.587,5.183,-0.222,-2.861,-2.917,-0.382,1.950,-2.008,-2.048,-3.181,3.330,-2.483,-3.091,-0.822,1.558,-0.609,-4.110,3.805,-0.068,0.693,-3.089,-4.521,-6.437,4.861,-2.347,-1.320,-2.644,-1.196,0.778,-5.848,-2.136,0.120,0.065,0.180,6.100,-2.054,-0.579,-3.436,0.647,-4.166,-5.174,-6.466,2.918,-2.002,1.220,1.765,-1.839,-5.573,-6.051,-0.788,1.122,0.865,3.682,-0.347,1.313,-2.981,3.664,-2.166,-3.047,2.854,-2.120,-2.980,-4.147,0.808,-1.375,2.077,-2.427,-3.552,0.712,2.447,2.933,-3.287,-4.609,1.802,3.634,-4.009,-0.696,-1.942,-6.317,2.830,2.966,-3.313,1.336,4.799,-4.732,3.561,1.990,5.612,5.138,-2.383,-0.670,3.784,-3.725,-1.286,-5.402,0.073,1.061,-0.160,4.281,2.633,-2.912,-0.625,2.546,3.071,5.048,-5.349,-6.232,-4.718,0.571,2.448,0.412,0.722,1.456,-2.248,0.700,-1.048,-3.855,-0.775,4.938,-0.072,-1.654,0.854,4.480,5.182,1.947,-0.946,1.626,-2.979,0.659,-2.673,-5.722,1.076,3.944,2.891,-0.440,-1.518,-2.477,6.501,2.735,-0.806,-0.837,0.341,0.817,-5.651,-3.237,5.055,2.634,1.305,2.321,0.617,-0.861,3.271,-3.276,0.953};
        double c[] = {0.194,0.254,0.204,0.283,0.291,0.265,0.152,0.219,0.159,0.232,0.212,0.269,0.194,0.228,0.179,0.168,0.167,0.205,0.245,0.163,0.220,0.155,0.159,0.278,0.177,0.246,0.253,0.218,0.212,0.204,0.249,0.298,0.273,0.288,0.174,0.204,0.280,0.282,0.174,0.171,0.290,0.208,0.280,0.197,0.241,0.218,0.211,0.220,0.277,0.236,0.254,0.178,0.206,0.252,0.170,0.170,0.154,0.180,0.166,0.262,0.209,0.197,0.265,0.278,0.216,0.168,0.191,0.188,0.232,0.266,0.278,0.231,0.229,0.210,0.276,0.177,0.174,0.266,0.174,0.172,0.268,0.232,0.195,0.166,0.211,0.168,0.201,0.233,0.150,0.273,0.298,0.253,0.201,0.237,0.187,0.247,0.166,0.255,0.256,0.265,0.235,0.288,0.286,0.223,0.245,0.217,0.151,0.245,0.227,0.155,0.156,0.241,0.274,0.277,0.156,0.202,0.204,0.184,0.195,0.214,0.203,0.152,0.161,0.191,0.230,0.240,0.155,0.275,0.250,0.203,0.243,0.250,0.290,0.237,0.268,0.274,0.294,0.249,0.178,0.213,0.248,0.233,0.197,0.200,0.267,0.195,0.248,0.287,0.214,0.247,0.197,0.170,0.186,0.232,0.192,0.154,0.247,0.239,0.256,0.287,0.281,0.160,0.202,0.185,0.156,0.271,0.242,0.215,0.166,0.220,0.206,0.276,0.292,0.219,0.283,0.276,0.202,0.153,0.157,0.205,0.182,0.272,0.200,0.160,0.258,0.224,0.251,0.151,0.264,0.235,0.227,0.213,0.210,0.164,0.203,0.191,0.256,0.211,0.176,0.185,0.240,0.236,0.257,0.257,0.178,0.245,0.200,0.250,0.174,0.213,0.191,0.266,0.261,0.291,0.296,0.247,0.265,0.178,0.221,0.270,0.204,0.196,0.201,0.285,0.272,0.295,0.193,0.171,0.170,0.150,0.284,0.172,0.279,0.293,0.225,0.209,0.233,0.264,0.281,0.209,0.260,0.174,0.232,0.263,0.212,0.155,0.223,0.200,0.188,0.189,0.157,0.228,0.201,0.205,0.262,0.294,0.238,0.226,0.290,0.256,0.293,0.295,0.280,0.175,0.181,0.282,0.178,0.263,0.249,0.194,0.208,0.243,0.224,0.183,0.186,0.216,0.161,0.278,0.270,0.244,0.295,0.241,0.291,0.273,0.151,0.166,0.291,0.181,0.167,0.273,0.186,0.206,0.178,0.221,0.278,0.194,0.283,0.284,0.209,0.184,0.226,0.219,0.212,0.299,0.268,0.273,0.281,0.227,0.290,0.295,0.224,0.155,0.238,0.214,0.276,0.283,0.160,0.248,0.268,0.196,0.251,0.193,0.197,0.164,0.191,0.220,0.186,0.234,0.155,0.273,0.283,0.262,0.224,0.194,0.172,0.176,0.277,0.225,0.170,0.177,0.246,0.257,0.268,0.166,0.256,0.178,0.238,0.213,0.244,0.287,0.271,0.291,0.219,0.235,0.211,0.198,0.158,0.253,0.166,0.155,0.254,0.267,0.237,0.269,0.217,0.238,0.215,0.281,0.274,0.159,0.278,0.175,0.185,0.182,0.237,0.216,0.275,0.244,0.179,0.251,0.183,0.236,0.177,0.273,0.238,0.242,0.291,0.160,0.265,0.244,0.173,0.273,0.274,0.172,0.235,0.207,0.239,0.289,0.268,0.272,0.268,0.237,0.155,0.261,0.175,0.205,0.213,0.247,0.206,0.267,0.271,0.212,0.263,0.167,0.261,0.254,0.157,0.246,0.224,0.280,0.160,0.239,0.264,0.156,0.193,0.241,0.161,0.156,0.232,0.250,0.205,0.263,0.251,0.214,0.273,0.257,0.194,0.278,0.224,0.204,0.245,0.295,0.262,0.293,0.237,0.267,0.297,0.202,0.265,0.174,0.267,0.266,0.153,0.249,0.294,0.279,0.276,0.165,0.225,0.278,0.204,0.293,0.298,0.225,0.205,0.188,0.228,0.210,0.281,0.171,0.251,0.191,0.276,0.265,0.222,0.169,0.213,0.241,0.156,0.237,0.235,0.294,0.255,0.209,0.163,0.251,0.215,0.221,0.174,0.280,0.209,0.213,0.219,0.258,0.169,0.281,0.246,0.297,0.240,0.249};

        long endereco_a = re.rniPutDoubleArray(a);
        long endereco_g = re.rniPutDoubleArray(g);
        long endereco_c = re.rniPutDoubleArray(c);
        re.rniAssign("parametro_a", endereco_a, 0);
        re.rniAssign("parametro_g", endereco_g, 0);
        re.rniAssign("parametro_c", endereco_c, 0);
        System.out.println("TESTE CAROL");
		System.out.println(rexp=re.eval("pairlist(a=parametro_a,g=parametro_g,c=parametro_c)"));
		
        // Example: how to create a named list or data.frame
        double da[] = {1.2, 2.3, 4.5};
        double db[] = {1.4, 2.6, 4.2};
        long xp3 = re.rniPutDoubleArray(da);
        long xp4 = re.rniPutDoubleArray(db);
        
        // now build a list (generic vector is how that's called in R)
        long la[] = {xp3, xp4};
        long xp5 = re.rniPutVector(la);

        // now let's add names
        String sa[] = {"a","b"};
        long xp2 = re.rniPutStringArray(sa);
        re.rniSetAttr(xp5, "names", xp2);

        // ok, we have a proper list now
        // we could use assign and then eval "b<-data.frame(b)", but for now let's build it by hand:       
        String rn[] = {"1", "2", "3"};
        long xp7 = re.rniPutStringArray(rn);
        re.rniSetAttr(xp5, "row.names", xp7);
        
        long xp6 = re.rniPutString("data.frame");
        re.rniSetAttr(xp5, "class", xp6);
        
        // assign the whole thing to the "b" variable
        re.rniAssign("b", xp5, 0);
        
        {
            System.out.println("Parsing");
            long e=re.rniParse("data(iris)", 1);
            System.out.println("Result = "+e+", running eval");
            long r=re.rniEval(e, 0);
            System.out.println("Result = "+r+", building REXP");
            REXP x=new REXP(re, r);
            System.out.println("REXP result = "+x);
        }
        {
            System.out.println("Parsing");
            long e=re.rniParse("iris", 1);
            System.out.println("Result = "+e+", running eval");
            long r=re.rniEval(e, 0);
            System.out.println("Result = "+r+", building REXP");
            REXP x=new REXP(re, r);
            System.out.println("REXP result = "+x);
        }
        {
            System.out.println("Parsing");
            long e=re.rniParse("names(iris)", 1);
            System.out.println("Result = "+e+", running eval");
            long r=re.rniEval(e, 0);
            System.out.println("Result = "+r+", building REXP");
            REXP x=new REXP(re, r);
            System.out.println("REXP result = "+x);
            String s[]=x.asStringArray();
            if (s!=null) {
                int i=0; while (i<s.length) { System.out.println("["+i+"] \""+s[i]+"\""); i++; }
            }
        }
        {
            System.out.println("Parsing");
            long e=re.rniParse("rnorm(10)", 1);
            System.out.println("Result = "+e+", running eval");
            long r=re.rniEval(e, 0);
            System.out.println("Result = "+r+", building REXP");
            REXP x=new REXP(re, r);
            System.out.println("REXP result = "+x);
            double d[]=x.asDoubleArray();
            if (d!=null) {
                int i=0; while (i<d.length) { System.out.print(((i==0)?"":", ")+d[i]); i++; }
                System.out.println("");
            }
            System.out.println("");
        }
        {
            REXP x=re.eval("1:10");
            System.out.println("REXP result = "+x);
            int d[]=x.asIntArray();
            if (d!=null) {
                int i=0; while (i<d.length) { System.out.print(((i==0)?"":", ")+d[i]); i++; }
                System.out.println("");
            }
        }

        re.eval("print(1:10/3)");
        
	if (true) {
	    // so far we used R as a computational slave without REPL
	    // now we start the loop, so the user can use the console
	    System.out.println("Now the console is yours ... have fun");
	    re.startMainLoop();
	} else {
	    re.end();
	    System.out.println("end");
	}
    }
}
