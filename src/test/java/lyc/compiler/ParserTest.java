package lyc.compiler;

import java_cup.runtime.Symbol;
import lyc.compiler.factories.ParserFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertThat;
import static lyc.compiler.Constants.EXAMPLES_ROOT_DIRECTORY;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest {

  @Test
  void assignmentWithExpression() throws Exception {
    compilationSuccessful("c:=d*(e-21)/4");
  }

  @Test
  void syntaxError() {
    System.out.println("### Syntax error test ###");
    compilationError("1234");
  }

  @Test
  void assignments() throws Exception {
    System.out.println("### Assignments test ###");
    compilationSuccessful(readFromResources("assignments.txt"));
  }

  @Test
  void write() throws Exception {
    System.out.println("### Write test ###");
    compilationSuccessful(readFromResources("write.txt"));
  }

  @Test
  void read() throws Exception {
    System.out.println("### Read test ###");
    compilationSuccessful(readFromResources("read.txt"));
  }

  @Test
  void comment() throws Exception {
    System.out.println("### Comment test ###");
    compilationSuccessful(readFromResources("comment.txt"));
  }

  @Test
  void init() throws Exception {
    System.out.println("### Init test ###");
    compilationSuccessful(readFromResources("init.txt"));
  }

  @Test
  void and() throws Exception {
    System.out.println("### AND test ###");
    compilationSuccessful(readFromResources("and.txt"));
  }

  @Test
  void or() throws Exception {
    System.out.println("### OR test ###");
    compilationSuccessful(readFromResources("or.txt"));
  }

  @Test
  void not() throws Exception {
    System.out.println("### NOT test ###");
    compilationSuccessful(readFromResources("not.txt"));
  }

  @Test
  void ifStatement() throws Exception {
    System.out.println("### If test ###");
    compilationSuccessful(readFromResources("if.txt"));
  }

  @Test
  void whileStatement() throws Exception {
    System.out.println("### While test ###");
    compilationSuccessful(readFromResources("while.txt"));
  }

  private void compilationSuccessful(String input) throws Exception {
    assertThat(scan(input).sym).isEqualTo(ParserSym.EOF);
  }

  private void compilationError(String input) {
    assertThrows(Exception.class, () -> scan(input));
  }

  private Symbol scan(String input) throws Exception {
    System.out.println(input);
    return ParserFactory.create(input).parse();
  }

  private String readFromFile(String fileName) throws IOException {
    URL url = new URL(EXAMPLES_ROOT_DIRECTORY + "/%s".formatted(fileName));
    System.out.println("Reading from file: " + url);
    assertThat(url).isNotNull();
    return IOUtils.toString(url.openStream(), StandardCharsets.UTF_8);
  }

  private String readFromResources(String filename) throws IOException {
    InputStream inputStream = getClass().getResourceAsStream("/%s".formatted(filename));
    assertThat(inputStream).isNotNull();
    return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
  }
}