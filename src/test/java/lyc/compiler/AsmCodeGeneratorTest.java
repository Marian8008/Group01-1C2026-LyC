package lyc.compiler;

import lyc.compiler.factories.ParserFactory;
import lyc.compiler.files.AsmCodeGenerator;
import lyc.compiler.symboltable.SymbolTable;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static com.google.common.truth.Truth.assertThat;

class AsmCodeGeneratorTest {

  @Test
  void readAndWriteUseFloatBackendForIntVariables() throws Exception {
    String input = "init {\n"
        + "    value : Int\n"
        + "}\n"
        + "read(value)\n"
        + "write(value)\n";

    SymbolTable.reset();
    ParserFactory.create(input).parse();

    File tempFile = File.createTempFile("asm-generator", ".asm");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile, StandardCharsets.UTF_8)) {
      new AsmCodeGenerator().generate(writer);
    }

    String asm = Files.readString(tempFile.toPath(), StandardCharsets.UTF_8);

    assertThat(asm).contains("GetFloat value");
    assertThat(asm).contains("DisplayFloat value, 0");
    assertThat(asm).doesNotContain("GetInteger value");
    assertThat(asm).doesNotContain("DisplayInteger value");
  }
}
