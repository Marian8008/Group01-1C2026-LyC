package lyc.compiler.syntactictree;

public class Root implements Nodo {
	private String operador;
	private Nodo izq;
	private Nodo der;

	public Root(String operador, Nodo izq, Nodo der) {
		this.operador = operador;
		this.der = der;
		this.izq = izq;
	}

	public String getOperador() {
		return operador;
	}

	public Nodo getIzq() {
		return this.izq;
	}

	public Nodo getDer() {
		return this.der;
	}

	@Override
	public String toString() {
		return "Nodo(" + this.operador + "," + this.izq + "," + this.der + ")";
	}

	@Override
	public String accept(NodeVisitor visitor) {
		return visitor.visitRoot(this);
	}
}
