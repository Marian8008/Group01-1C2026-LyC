package lyc.compiler.files;

import lyc.compiler.syntactictree.SyntacticTree;

import java.io.FileWriter;
import java.io.IOException;

public class IntermediateCodeGenerator implements FileGenerator {

    @Override
    public void generate(FileWriter fileWriter) throws IOException {
        fileWriter.write(SyntacticTree.getSyntacticTree().toString());
    }
}
