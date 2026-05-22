package lyc.compiler.syntactictree;

public class TempGenerator {
	private int cont = 0;
	
	public String newTemp() {
		return "T" + cont++;
	}

}
