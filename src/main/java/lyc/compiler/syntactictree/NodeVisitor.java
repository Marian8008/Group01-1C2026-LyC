package lyc.compiler.syntactictree;

public interface NodeVisitor {
	String visitRoot(Root root);

	String visitLeaf(Leaf leaf);
}
