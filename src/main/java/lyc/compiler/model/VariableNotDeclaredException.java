package lyc.compiler.model;

public class VariableNotDeclaredException extends CompilerException {
    public VariableNotDeclaredException(String message) {
        super(message);
    }
}
