package lyc.compiler.files;

import lyc.compiler.syntactictree.*;
import lyc.compiler.symboltable.SymbolLYC;
import lyc.compiler.symboltable.SymbolTable;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AsmCodeGenerator implements FileGenerator {

    private FileWriter writer;
    private StringWriter codeWriter;
    private int aux = 1;
    private int label = 1;
    private Set<String> intVariables = new LinkedHashSet<>();
    private Set<String> floatVariables = new LinkedHashSet<>();
    private Set<String> stringVariables = new LinkedHashSet<>();
    private Map<String, String> intConstants = new LinkedHashMap<>();
    private Map<String, String> floatConstants = new LinkedHashMap<>();
    private Map<String, String> stringConstants = new LinkedHashMap<>();
    private Map<String, String> constantLabels = new HashMap<>();
    private Set<String> auxiliares = new LinkedHashSet<>();
    private final SymbolTable symbolTable = SymbolTable.getSymbolTable();

    @Override
    public void generate(FileWriter fileWriter) throws IOException {
        writer = fileWriter;
        codeWriter = new StringWriter();

        Nodo root = SyntacticTree.getSyntacticTree().getRoot();

        // Primero recolectar variables y constantes
        recolectarVariables(root);

        // Generar código en buffer antes de escribir .DATA
        recorrer(root);

        // Escribir estructura del programa y el código generado
        writeHeader();
        writer.write(codeWriter.toString());

        // Escribir footer
        writeFooter();
    }

    private void writeHeader() throws IOException {

        writer.write("include macros.asm\n");
        writer.write("include macros2.asm\n");
        writer.write("include number.asm\n\n");
        writer.write(".MODEL LARGE\n");
        writer.write(".386\n");
        writer.write(".STACK 200h\n");
        writer.write("MAXTEXTSIZE equ 50\n\n");

        writer.write(".DATA\n");

        writeDataDeclarations();

        writer.write("\n.CODE\n");
        writer.write("START:\n");
        writer.write("mov AX,@DATA\n");
        writer.write("mov DS,AX\n");
        writer.write("mov ES,AX\n\n");
    }

    private String crearAuxiliar() throws IOException {
        String t = "@aux" + aux++;
        auxiliares.add(t);
        return t;
    }

    private void writeDataDeclarations() throws IOException { // Declarar variables en el orden de aparición y con el
                                                              // tipo correcto
        for (String var : intVariables) {
            writer.write(var + " dd ?\n");
        }
        for (String var : floatVariables) {
            writer.write(var + " dd ?\n");
        }
        for (String var : stringVariables) {
            writer.write(var + " db MAXTEXTSIZE dup(?), '$'\n");
        }
        writer.write("\n"); // Separador
        // Declarar constantes en el orden de aparición y con el tipo correcto
        for (Map.Entry<String, String> entry : intConstants.entrySet()) {
            writer.write(entry.getKey() + " dd " + entry.getValue() + "\n");
        }
        for (Map.Entry<String, String> entry : floatConstants.entrySet()) {
            writer.write(entry.getKey() + " dd " + entry.getValue() + "\n");
        }
        for (Map.Entry<String, String> entry : stringConstants.entrySet()) {
            writer.write(entry.getKey() + " db " + entry.getValue() + ", '$'\n");
        }
        writer.write("\n"); // Separador
        // Declarar auxiliares (en orden de creación)
        for (String aux : auxiliares) {
            writer.write(aux + " dd ?\n");
        }
    }

    private void writeFooter() throws IOException {

        writer.write("\nmov AX,4C00h\n");
        writer.write("int 21h\n");
        writer.write("END START\n");
    }

    private void writeCode(String text) {
        if (codeWriter != null) {
            codeWriter.write(text);
        }
    }

    private boolean isStringLiteral(String valor) {
        return valor != null && valor.startsWith("\"") && valor.endsWith("\"");
    }

    private boolean isIntegerLiteral(String valor) {
        return valor != null && valor.matches("-?\\d+");
    }

    private boolean isFloatLiteral(String valor) {
        return valor != null && valor.matches("-?\\d+\\.\\d+");
    }

    private String createConstantLabel(String valor) {
        String label = valor.replaceFirst("^-", "neg").replace('.', 'x');
        label = "_" + label;
        String base = label;
        int suffix = 1;
        while (intConstants.containsKey(label) || floatConstants.containsKey(label)
                || stringConstants.containsKey(label)) {
            label = base + "_" + suffix++;
        }
        return label;
    }

    private void addIntConstant(String valor) {
        if (!constantLabels.containsKey(valor)) {
            String label = createConstantLabel(valor);
            constantLabels.put(valor, label);
            intConstants.put(label, valor + ".0");
        }
    }

    private void addFloatConstant(String valor) {
        if (!constantLabels.containsKey(valor)) {
            String label = createConstantLabel(valor);
            constantLabels.put(valor, label);
            floatConstants.put(label, valor);
        }
    }

    private String normalizeStringValue(String valor) {
        if (valor == null)
            return "\"\"";
        if (valor.startsWith("\"") && valor.endsWith("\"")) {
            return valor;
        }
        return "\"" + valor + "\"";
    }

    private void addStringConstant(String valor) {
        if (!constantLabels.containsKey(valor)) {
            String label = "_str" + (stringConstants.size() + 1);
            constantLabels.put(valor, label);
            stringConstants.put(label, normalizeStringValue(valor));
        }
    }

    private void recolectarVariables(Nodo nodo) {
        if (nodo == null)
            return;

        if (nodo instanceof Root) {
            Root r = (Root) nodo;

            if ("ASSIG".equals(r.getOperador())) {

                String izq = (r.getIzq() instanceof Leaf)
                        ? ((Leaf) r.getIzq()).getValor()
                        : r.getIzq().toString();

                String der = (r.getDer() instanceof Leaf)
                        ? ((Leaf) r.getDer()).getValor()
                        : r.getDer().toString();

                System.out.println(
                        "ASSIG -> IZQ=" + izq +
                                " DER=" + der);
            }
        }

        if (nodo instanceof Leaf) {
            String valor = ((Leaf) nodo).getValor();

            if (isStringLiteral(valor)) {
                addStringConstant(valor);
                return;
            }
            if (isIntegerLiteral(valor)) {
                addIntConstant(valor);
                return;
            }
            if (isFloatLiteral(valor)) {
                addFloatConstant(valor);
                return;
            }

            if (symbolTable.exists(valor)) {
                SymbolLYC symbol = symbolTable.get(valor);
                if (symbol != null) {
                    switch (symbol.getType()) {
                        case "INT":
                            intVariables.add(valor);
                            break;
                        case "FLOAT":
                            floatVariables.add(valor);
                            break;
                        case "STRING":
                            stringVariables.add(valor);
                            break;
                        case "CTE_INT":
                            if (!constantLabels.containsKey(valor)) {
                                constantLabels.put(valor, valor);
                                intConstants.put(valor, symbol.getValue() + ".0");
                            }
                            break;
                        case "CTE_FLOAT":
                            if (!constantLabels.containsKey(valor)) {
                                constantLabels.put(valor, valor);
                                floatConstants.put(valor, symbol.getValue());
                            }
                            break;
                        case "CTE_STRING":
                            if (!constantLabels.containsKey(valor)) {
                                constantLabels.put(valor, valor);
                                stringConstants.put(valor, symbol.getValue());
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        } else {
            Root r = (Root) nodo;
            recolectarVariables(r.getIzq());
            recolectarVariables(r.getDer());
        }
    }

    private String recorrer(Nodo nodo) throws IOException {
        if (nodo == null)
            return "";

        if (nodo instanceof Leaf) {
            String valor = ((Leaf) nodo).getValor();
            return formatearOperando(valor);
        }

        Root r = (Root) nodo;

        switch (r.getOperador()) {

            case "BLOCK":
                recorrer(r.getIzq());
                recorrer(r.getDer());
                return "";

            case "ASSIG":
                return asignacion(r);

            case "+":
                return suma(r);

            case "-":
                return resta(r);

            case "*":
                return mult(r);

            case "/":
                return div(r);

            case "%":
                return mod(r);

            case "IF":
                return generarIf(r);

            case "IF_ELSE":
                return generarIfElse(r);

            case "WHILE":
                return generarWhile(r);

            case "FOR":
                return generarFor(r);

            case "WRITE":
                return write(r);
            case "READ":
                return read(r);
        }

        return "";
    }

    private boolean isNumericLiteral(String valor) {
        return valor != null && valor.matches("-?\\d+(\\.\\d+)?");
    }

    private String getConstantLabel(String valor) {
        if (valor == null)
            return "";
        return constantLabels.getOrDefault(valor, valor);
    }

    private String formatearOperando(String valor) {
        if (valor == null)
            return "";
        if (isStringLiteral(valor) || isNumericLiteral(valor)) {
            return getConstantLabel(valor);
        }
        return valor;
    }

    private String suma(Root r) throws IOException {
        String a = recorrer(r.getIzq());
        String b = recorrer(r.getDer());
        if (a.isEmpty() || b.isEmpty())
            return "";
        String t = crearAuxiliar();

        writeCode("FLD " + a + "\n");
        writeCode("FLD " + b + "\n");
        writeCode("FADD\n");
        writeCode("FSTP " + t + "\n");
        writeCode("\n");

        return t;
    }

    private String resta(Root r) throws IOException {
        String a = recorrer(r.getIzq());
        String b = recorrer(r.getDer());
        if (a.isEmpty() || b.isEmpty())
            return "";
        String t = crearAuxiliar();

        writeCode("FLD " + a + "\n");
        writeCode("FLD " + b + "\n");
        writeCode("FSUB\n");
        writeCode("FSTP " + t + "\n");
        writeCode("\n");

        return t;
    }

    private String mult(Root r) throws IOException {
        String a = recorrer(r.getIzq());
        String b = recorrer(r.getDer());
        if (a.isEmpty() || b.isEmpty())
            return "";
        String t = crearAuxiliar();

        writeCode("FLD " + a + "\n");
        writeCode("FLD " + b + "\n");
        writeCode("FMUL\n");
        writeCode("FSTP " + t + "\n");
        writeCode("\n");

        return t;
    }

    private String div(Root r) throws IOException {
        String a = recorrer(r.getIzq());
        String b = recorrer(r.getDer());
        if (a.isEmpty() || b.isEmpty())
            return "";
        String t = crearAuxiliar();

        writeCode("FLD " + a + "\n");
        writeCode("FLD " + b + "\n");
        writeCode("FDIV\n");
        writeCode("FSTP " + t + "\n");
        writeCode("\n");

        return t;
    }

    private String asignacion(Root r) throws IOException {
        String variable = ((Leaf) r.getIzq()).getValor();
        String valor = recorrer(r.getDer());

        if (variable == null || variable.isEmpty() || valor.isEmpty()) {
            return "";
        }

        SymbolLYC symbol = symbolTable.get(variable);
        if (symbol != null && "STRING".equals(symbol.getType())) {
            writeCode("LEA SI, " + valor + "\n");
            writeCode("LEA DI, " + variable + "\n");
            writeCode("STRCPY\n\n");
            return variable;
        }

        writeCode("FLD " + valor + "\n");
        writeCode("FSTP " + variable + "\n");
        writeCode("\n");

        return variable;
    }

    private String mod(Root r) throws IOException {

        String a = recorrer(r.getIzq());
        String b = recorrer(r.getDer());
        if (a.isEmpty() || b.isEmpty())
            return "";
        String t = crearAuxiliar();

        writeCode("MOV AX," + a + "\n");
        writeCode("CWD\n");
        writeCode("MOV BX," + b + "\n");
        writeCode("IDIV BX\n");
        writeCode("MOV " + t + ",DX\n\n");

        return t;
    }

    private String generarWhile(Root r) throws IOException {

        String inicio = "L" + label++;
        String fin = "L" + label++;

        writeCode(inicio + ":\n");

        generarCondicion(r.getIzq(), fin);

        recorrer(r.getDer());

        writeCode("JMP " + inicio + "\n");

        writeCode(fin + ":\n\n");

        return "";
    }

    private String generarIf(Root r) throws IOException {
        String fin = "L" + label++;
        generarCondicion(r.getIzq(), fin);
        recorrer(r.getDer());
        writeCode(fin + ":\n");
        writeCode("\n");
        return "";
    }
    private String generarFor(Root r) throws IOException {
        if (r == null)
            return "";

        // FOR node: Left = loop variable leaf
        // Right = FOR_BODY node
        Root body = (Root) r.getDer();
        Root range = (Root) body.getIzq();
        Root dataFor = (Root) body.getDer();
        Nodo block = dataFor.getIzq();
        Nodo step = dataFor.getDer();

        String varName = ((Leaf) r.getIzq()).getValor();
        String startValue = recorrer(range.getIzq());
        String endValue = recorrer(range.getDer());
        String stepValue = step != null ? recorrer(step) : "1";

        String inicio = "L" + label++;
        String fin = "L" + label++;

        // Inicializar variable de control
        writeCode("FLD " + startValue + "\n");
        writeCode("FSTP " + varName + "\n");
        writeCode("\n");

        writeCode(inicio + ":\n");

        // Compare varName > endValue, if so exit
        generarCondicion(new Root(
                "<=",
                new Leaf(varName),
                range.getDer()
        ), fin);

        // Cuerpo del for
        recorrer(block);

        // Incrementar variable de control: varName = varName + stepValue
        String incrementValue = recorrer(new Root("+", new Leaf(varName), new Leaf(stepValue)));
        writeCode("FLD " + incrementValue + "\n");
        writeCode("FSTP " + varName + "\n");
        writeCode("\n");

        writeCode("JMP " + inicio + "\n");
        writeCode(fin + ":\n\n");

        return "";
    }
    private String generarIfElse(Root r) throws IOException {

        String elseLabel = "L" + label++;
        String fin = "L" + label++;

        generarCondicion(r.getIzq(), elseLabel);

        Root body = (Root) r.getDer();

        recorrer(body.getIzq());

        writeCode("JMP " + fin + "\n");

        writeCode(elseLabel + ":\n");

        recorrer(body.getDer());

        writeCode(fin + ":\n\n");

        return "";
    }

    private String write(Root r) throws IOException {
        Nodo hijo = r.getIzq();
        if (hijo == null)
            return "";

        // Obtener etiqueta/operando a escribir
        String operando;
        boolean isLeaf = hijo instanceof Leaf;

        if (isLeaf) {
            operando = ((Leaf) hijo).getValor();
        } else {
            operando = recorrer(hijo);
        }

        // String literal
        if (isLeaf && isStringLiteral(operando)) {
            String label = formatearOperando(operando);
            writeCode("displayString " + label + "\n");
            return "";
        }

        // If it's an identifier, consult symbol table
        if (operando != null && operando.matches("[a-zA-Z_][a-zA-Z0-9_]*") && symbolTable.exists(operando)) {
            SymbolLYC sym = symbolTable.get(operando);
            if (sym != null) {
                switch (sym.getType()) {
                    case "STRING":
                        writeCode("displayString " + operando + "\n");
                        return "";
                    case "INT":
                        writeCode("DisplayInteger " + operando + "\n");
                        return "";
                    case "FLOAT":
                        writeCode("DisplayFloat " + operando + ", 2\n");
                        return "";
                    default:
                        writeCode("; WRITE unsupported type for " + operando + "\n");
                        return "";
                }
            }
        }

        // If operando corresponds to known constant or auxiliar/variable sets
        if (operando != null) {
            if (stringConstants.containsKey(operando) || stringVariables.contains(operando)) {
                writeCode("displayString " + operando + "\n");
                return "";
            }
            if (auxiliares.contains(operando) || floatConstants.containsKey(operando) || floatVariables.contains(operando)) {
                writeCode("DisplayFloat " + operando + ", 2\n");
                return "";
            }
            if (intConstants.containsKey(operando) || intVariables.contains(operando)) {
                writeCode("DisplayInteger " + operando + "\n");
                return "";
            }
        }

        // As a fallback, if it's a numeric literal (leaf)
        if (isLeaf && isIntegerLiteral(operando)) {
            String label = formatearOperando(operando);
            writeCode("DisplayInteger " + label + "\n");
            return "";
        }
        if (isLeaf && isFloatLiteral(operando)) {
            String label = formatearOperando(operando);
            writeCode("DisplayFloat " + label + ", 2\n");
            return "";
        }

        writeCode("; WRITE unsupported operand: " + operando + "\n");
        return "";
    }

    private String read(Root r) throws IOException {
        Nodo hijo = r.getIzq();

        if (hijo == null)
            return "";

        if (!(hijo instanceof Leaf)) {
            writeCode("; READ unsupported non-leaf\n");
            return "";
        }

        String nombre = ((Leaf) hijo).getValor();
        if (nombre == null || nombre.isEmpty())
            return "";

        if (symbolTable.exists(nombre)) {
            SymbolLYC sym = symbolTable.get(nombre);
            if (sym != null) {
                switch (sym.getType()) {
                    case "STRING":
                        writeCode("getString " + nombre + "\n");
                        return "";
                    case "INT":
                        writeCode("GetInteger " + nombre + "\n");
                        return "";
                    case "FLOAT":
                        writeCode("GetFloat " + nombre + "\n");
                        return "";
                    default:
                        writeCode("; READ unsupported type for " + nombre + "\n");
                        return "";
                }
            }
        }

        writeCode("; READ unknown variable: " + nombre + "\n");
        return "";
    }

    private void generarCondicion(Nodo nodo, String salida) throws IOException {

        if (nodo == null)
            return;

        Root r = (Root) nodo;

        switch (r.getOperador()) {

            case "AND":

                generarCondicion(r.getIzq(), salida);
                generarCondicion(r.getDer(), salida);

                break;

            case "OR":

                String siguiente = "L" + label++;

                generarComparacion((Root) r.getIzq(), "JG", siguiente);

                generarCondicion(r.getDer(), salida);

                writeCode(siguiente + ":\n");

                break;

            case "NOT":

                Root hijo = (Root) r.getIzq();

                switch (hijo.getOperador()) {

                    case ">":
                        generarComparacion(hijo, "JG", salida);
                        break;

                    case "<":
                        generarComparacion(hijo, "JL", salida);
                        break;

                    case ">=":
                        generarComparacion(hijo, "JGE", salida);
                        break;

                    case "<=":
                        generarComparacion(hijo, "JLE", salida);
                        break;

                    case "==":
                        generarComparacion(hijo, "JE", salida);
                        break;

                    case "!=":
                        generarComparacion(hijo, "JNE", salida);
                        break;
                }

                break;

            case ">":
                generarComparacion(r, "JLE", salida);
                break;

            case "<":
                generarComparacion(r, "JGE", salida);
                break;

            case ">=":
                generarComparacion(r, "JL", salida);
                break;

            case "<=":
                generarComparacion(r, "JG", salida);
                break;

            case "==":
                generarComparacion(r, "JNE", salida);
                break;

            case "!=":
                generarComparacion(r, "JE", salida);
                break;
        }
    }

    private void generarComparacion(Root r, String salto, String etiqueta) throws IOException {
        String a = recorrer(r.getIzq());
        String b = recorrer(r.getDer());
        if (a.isEmpty() || b.isEmpty())
            return;

        writeCode("FLD " + a + "\n");
        writeCode("FLD " + b + "\n");
        writeCode("FXCH\n");
        writeCode("FCOMP\n");
        writeCode("FSTSW AX\n");
        writeCode("FFREE\n");
        writeCode("SAHF\n");
        writeCode(salto + " " + etiqueta + "\n");
    }
}