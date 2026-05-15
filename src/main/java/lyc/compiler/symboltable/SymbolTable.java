package lyc.compiler.symboltable;

import java.util.HashMap;

public final class SymbolTable {

  private static SymbolTable symt;
  private final HashMap<String, SymbolLYC> table;
  private int stringCounter = 0;

  private SymbolTable() {
    table = new HashMap<>();
  }

  public static SymbolTable getSymbolTable() {
    if (symt == null) {
      symt = new SymbolTable();
    }
    return symt;
  }

  public String insert(String name, String type, String value, boolean isID) {
    if (!isID) {
      type = "CTE_" + type;

      if (type.equals("CTE_STRING")) {

        for (SymbolLYC sym : table.values()) {
          if (sym.getValue().equals(value) && sym.getType().equals(type)) {
            return sym.getName();
          }
        }

        name = "_" + sanitizeString(value);
      } else {
        name = "_" + name;
      }
    }

    table.put(name, new SymbolLYC(name, value, type));
    return name;
  }

  private String sanitizeString(String value) {
    return value
            .replaceAll("\\s+", "_")     // espacios -> _
            .replaceAll("[^a-zA-Z0-9_]", "") // quitar símbolos raros
            .toLowerCase();
  }

  public SymbolLYC get(String name) {
    return table.get(name);
  }

  public void updateValue(String name, String value) {
    SymbolLYC symbol = table.get(name);
    if (symbol != null) {
      symbol.setValue(value);
    }
  }

  public boolean exists(String name) {
    return table.containsKey(name);
  }

  @Override
  public String toString() {
    String inicio =
        "┌" + "-".repeat(50) + "┬" + "-".repeat(10) + "┬" + "-".repeat(50) + "┬" + "-".repeat(10) + "┐"
            + "\n";
    String format = "│%-50s│%-10s│%-50s│%-10s│";
    String out = inicio + String.format(format, "NAME", "TYPE", "VALUE", "LENGTH") + "\n";
    String separador =
        "├" + "-".repeat(50) + "┼" + "-".repeat(10) + "┼" + "-".repeat(50) + "┼" + "-".repeat(10) + "┤"
            + "\n";
    String fin =
        "└" + "-".repeat(50) + "┴" + "-".repeat(10) + "┴" + "-".repeat(50) + "┴" + "-".repeat(10) + "┘"
            + "\n";
    out += separador;
    format = "│%s│";
    for (String k : table.keySet()) {
      out += String.format(format, table.get(k)) + "\n" + separador;
    }
    return out.substring(0, out.length() - separador.length()) + fin;
  }
}