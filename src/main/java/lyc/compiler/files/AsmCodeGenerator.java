package lyc.compiler.files;

import lyc.compiler.syntactictree.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class AsmCodeGenerator implements FileGenerator {

    private FileWriter writer;
    private int aux = 1;
    private int label = 1;
    private Set<String> variables = new LinkedHashSet<>();
    private Set<String> constantes = new LinkedHashSet<>();
    private Set<String> auxiliares = new LinkedHashSet<>();

    @Override
    public void generate(FileWriter fileWriter) throws IOException {
        writer = fileWriter;

        Nodo root = SyntacticTree.getSyntacticTree().getRoot();

        // Primero recolectar variables y constantes
        recolectarVariables(root);

        // Escribir estructura del programa
        writeHeader();

        // Generar código
        recorrer(root);

        // Escribir footer
        writeFooter();
    }

    private void writeHeader() throws IOException {

        writer.write(".MODEL LARGE\n");
        writer.write(".386\n");
        writer.write(".STACK 200h\n\n");

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

    private void writeDataDeclarations() throws IOException {
        // Declarar variables (en orden de aparición)
        for (String var : variables) {
            writer.write(var + " dd ?\n");
        }

        writer.write("\n"); // Separador

        // Declarar constantes (en orden de aparición)
        for (String cte : constantes) {
            writer.write("_" + cte + " dd " + cte + "\n");
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

    private void recolectarVariables(Nodo nodo) {
        if (nodo == null) return;

        if (nodo instanceof Leaf) {
            String valor = ((Leaf) nodo).getValor();

            // Ignorar si es texto (contiene espacios y no es número)
            if (valor.contains(" ") && !valor.matches("-?\\d+")) {
                return;  // No lo tratamos como variable ni constante
            }

            if (valor.matches("-?\\d+")) {
                constantes.add(valor);
            } else if (!valor.startsWith("\"")) {
                variables.add(valor);
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

            case "WRITE":
                return write(r);
        }

        return "";
    }

    private String formatearOperando(String valor) {
        if (valor == null) return "";
        if (valor.matches("-?\\d+")) {
            return "_" + valor;  // 20 -> _20
        }
        return valor;
    }

    private String suma(Root r) throws IOException {
        String a = recorrer(r.getIzq());
        String b = recorrer(r.getDer());
        String t = crearAuxiliar();

        writer.write("MOV R1, " + a + "\n");
        writer.write("ADD R1, " + b + "\n");
        writer.write("MOV " + t + ", R1\n");
        writer.write("\n");

        return t;
    }

    private String resta(Root r) throws IOException {
        String a = recorrer(r.getIzq());
        String b = recorrer(r.getDer());
        String t = crearAuxiliar();

        writer.write("MOV R1, " + a + "\n");
        writer.write("SUB R1, " + b + "\n");
        writer.write("MOV " + t + ", R1\n");
        writer.write("\n");

        return t;
    }

    private String mult(Root r) throws IOException {
        String a = recorrer(r.getIzq());
        String b = recorrer(r.getDer());
        String t = crearAuxiliar();

        writer.write("MOV R1, " + a + "\n");
        writer.write("MUL R1, " + b + "\n");  // MUL en lugar de IMUL
        writer.write("MOV " + t + ", R1\n");
        writer.write("\n");

        return t;
    }

    private String div(Root r) throws IOException {
        String a = recorrer(r.getIzq());
        String b = recorrer(r.getDer());
        String t = crearAuxiliar();

        writer.write("MOV R1, " + a + "\n");
        writer.write("DIV R1, " + b + "\n");
        writer.write("MOV " + t + ", R1\n");
        writer.write("\n");

        return t;
    }

    private String asignacion(Root r) throws IOException {
        String variable = ((Leaf) r.getIzq()).getValor();
        String valor = recorrer(r.getDer());

        writer.write("MOV R1, " + valor + "\n");
        writer.write("MOV " + variable + ", R1\n");
        writer.write("\n");

        return variable;
    }

    private String mod(Root r) throws IOException {

        String a = recorrer(r.getIzq());
        String b = recorrer(r.getDer());
        String t = crearAuxiliar();

        writer.write("MOV AX," + a + "\n");
        writer.write("CWD\n");
        writer.write("MOV BX," + b + "\n");
        writer.write("IDIV BX\n");
        writer.write("MOV " + t + ",DX\n\n");

        return t;
    }

    private String generarWhile(Root r) throws IOException {

        String inicio = "L" + label++;
        String fin = "L" + label++;

        writer.write(inicio + ":\n");

        generarCondicion(r.getIzq(), fin);

        recorrer(r.getDer());

        writer.write("JMP " + inicio + "\n");

        writer.write(fin + ":\n\n");

        return "";
    }

    private String generarIf(Root r) throws IOException {
        String fin = "L" + label++;
        generarCondicion(r.getIzq(), fin);
        recorrer(r.getDer());
        writer.write(fin + ":\n");
        writer.write("\n");
        return "";
    }

    private String generarIfElse(Root r) throws IOException {

        String elseLabel = "L" + label++;
        String fin = "L" + label++;

        generarCondicion(r.getIzq(), elseLabel);

        Root body = (Root) r.getDer();

        recorrer(body.getIzq());

        writer.write("JMP " + fin + "\n");

        writer.write(elseLabel + ":\n");

        recorrer(body.getDer());

        writer.write(fin + ":\n\n");

        return "";
    }

    private String write(Root r) throws IOException {
        Nodo hijo = r.getIzq();

        if (hijo instanceof Leaf) {
            String valor = ((Leaf) hijo).getValor();

            // Verificar si es un string literal (con comillas)
            if (valor.startsWith("\"") && valor.endsWith("\"")) {
                // Ya tiene comillas, lo usamos directamente
                writer.write("; WRITE " + valor + "\n");
            }
            // Verificar si parece texto (contiene espacios y no es número ni identificador)
            else if (valor.contains(" ") && !valor.matches("-?\\d+")) {
                // Es texto sin comillas, lo agregamos nosotros
                writer.write("; WRITE \"" + valor + "\"\n");
            }
            // Verificar si es un identificador (variable)
            else if (valor.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                writer.write("; WRITE variable: " + valor + "\n");
            }
            // Verificar si es un número
            else if (valor.matches("-?\\d+")) {
                writer.write("; WRITE number: " + valor + "\n");
            }
            // Cualquier otro caso
            else {
                writer.write("; WRITE \"" + valor + "\"\n");
            }
        } else {
            // Es una expresión compleja
            String texto = recorrer(hijo);
            writer.write("; WRITE expression: " + texto + "\n");
        }

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

                writer.write(siguiente + ":\n");

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

        writer.write("CMP " + a + ", " + b + "\n");  // Espacio después de la coma
        writer.write(salto + " " + etiqueta + "\n");
    }
}