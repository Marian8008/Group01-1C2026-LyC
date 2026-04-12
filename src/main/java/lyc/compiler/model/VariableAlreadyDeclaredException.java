package lyc.compiler.model;

public class VariableAlreadyDeclaredException extends CompilerException{
  public VariableAlreadyDeclaredException(String message) {
    super(message);
  }
}