package rcat;

import java.io.PrintStream;

import org.rosuda.JRI.RConsoleOutputStream;
import org.rosuda.JRI.Rengine;

import rcat.core.FrameConsole;
import rcat.core.Parameters;

public class EduCat {
	public static void main(String[] args) {
		System.out.println("Creating Rengine (with arguments)");
		Rengine re = new Rengine(args, true, new FrameConsole());
		System.out.println("Rengine created, waiting for R");
		if (!re.waitForR()) {
			System.out.println("Cannot load R");
			return;
		}
		System.out.println("re-routing stdout/err into R console");
		System.setOut(new PrintStream(new RConsoleOutputStream(re, 0)));
		System.setErr(new PrintStream(new RConsoleOutputStream(re, 1)));

		long endereco_a = re.rniPutDoubleArray(Parameters.A);
		long endereco_d = re.rniPutDoubleArray(Parameters.D);
		long endereco_g = re.rniPutDoubleArray(Parameters.G);

		re.rniAssign("a", endereco_a, 0);
		re.rniAssign("d", endereco_d, 0);
		re.rniAssign("g", endereco_g, 0);

		re.eval("library(mirtCAT)");
		re.eval("pars <- data.frame(a1=a, d=d,g=g)");

		re.eval("mod <- generate.mirt_object(pars, '3PL')");
		re.eval("coef(mod,simplify=T)");
		re.eval("df <- data.frame(Question=Question,choices,Type = 'radio',Answer=Answer,stringsAsFactors = FALSE)");

	}
}
