package lyc.compiler.syntactictree;

public class Leaf implements Nodo {
	private String valor;

	public Leaf(String valor) {
		this.valor = valor;
	}

	public String getValor() {
		return valor;
	}

	@Override
	public String toString() {
		return "Hoja(" + this.valor + ")";
	}

	@Override
	public String accept(NodeVisitor visitor) {
		return visitor.visitLeaf(this);
	}

}
