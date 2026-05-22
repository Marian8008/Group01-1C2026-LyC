package lyc.compiler.syntactictree;

public class SyntacticTree implements NodeVisitor {
	private static SyntacticTree synt;
	private Nodo root;
	private TempGenerator temp = new TempGenerator();
	private StringBuilder code = new StringBuilder();

	private SyntacticTree() {
	};

	public static SyntacticTree getSyntacticTree() {
		if (synt == null) {
			synt = new SyntacticTree();
		}
		return synt;
	}

	public Nodo getRoot() {
		return root;
	}

	public void setRoot(Nodo root) {
		this.root = root;
	}

	@Override
	public String visitRoot(Root root) {
		String izq = root.getIzq() != null ? root.getIzq().accept(this):null;
		String der = root.getDer() != null ? root.getDer().accept(this):null;
		String temps = temp.newTemp();
		code.append(temps + " = crearNodo("+ root.getOperador() +", "+ izq + ", " + der + ")\n");
		return temps;
	}

	@Override
	public String visitLeaf(Leaf leaf) {
		String temps = this.temp.newTemp();
		code.append(temps + " = crearHoja(" + leaf.getValor() + ")\n");
		return temps;
	}

	@Override
	public String toString() {
		this.visitRoot((Root)this.root);
		//this.root.accept(this);
		//quedarse con la mas legible
		return code.toString();
	}

}
