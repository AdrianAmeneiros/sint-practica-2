package p2;

public class Programa {

    private String idP; // 2 cifras seguido de 3 letras mayúsculas
    private String edadMinima; // Opcional
    private String langs; // Varias cadenas de 2 caracteres separadas por espacios
    private String nombrePrograma;

    // Constructor
    public Programa(String idP, String edadMinima, String langs, String nombrePrograma) {
        this.idP = idP;
        this.edadMinima = edadMinima;
        this.langs = langs;
        this.nombrePrograma = nombrePrograma;
    }

    // Getters y setters
    public String getIdP() {
        return idP;
    }

    public void setIdP(String idP) {
        this.idP = idP;
    }

    public String getEdadMinima() {
        return edadMinima;
    }

    public void setEdadMinima(String edadMinima) {
        this.edadMinima = edadMinima;
    }

    public String getLangs() {
        return langs;
    }

    public void setLangs(String langs) {
        this.langs = langs;
    }

    public String getNombrePrograma() {
        return nombrePrograma;
    }

    public void setNombrePrograma(String nombrePrograma) {
        this.nombrePrograma = nombrePrograma;
    }
}