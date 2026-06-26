package lyc.compiler.symboltable;

public class SymbolLYC {

  public String name;
  public String type;
  public String value;
  public int length;

  public SymbolLYC(String name, String value, String type) {
    this.type = type;
    this.value = value;
    this.length = calculateLength(value, type);
    this.name = name;
}

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
    this.length = calculateLength(value, this.type);
}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  private int calculateLength(String value, String type) {
    if (value == null) {
        return 0;
    }

    if ((type.equals("STRING") || type.equals("CTE_STRING"))
            && value.startsWith("\"")
            && value.endsWith("\"")) {
        return value.length() - 2;
    }

    return value.length();
}
  @Override
  public String toString() {
    String format = "%-50s│%-10s│%-50s│%-10s";
    if (!this.type.equals("STRING") && !this.type.equals("CTE_STRING")) {
      return String.format(format, this.name, this.type, this.value, "");
    }
    return String.format(format, this.name, this.type, this.value, this.length);
  }
}
