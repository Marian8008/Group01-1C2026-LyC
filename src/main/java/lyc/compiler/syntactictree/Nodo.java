package lyc.compiler.syntactictree;

public interface Nodo {

	public String accept(NodeVisitor visitor);

	@Override
	String toString();
}
