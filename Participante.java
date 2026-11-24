package p2;

public class Participante {

    private String idS; // Formato de un dni
    private String nombreParticipante;
    private String role;

    // Constructor
    public Participante(String idS, String nombreParticipante, String role) {
        this.idS = idS;
        this.nombreParticipante = nombreParticipante;
        this.role = role;
    }

    // Getters y setters
    public String getIdS() {
        return idS;
    }

    public void setIdS(String idS) {
        this.idS = idS;
    }

    public String getNombreParticipante() {
        return nombreParticipante;
    }

    public void setNombreParticipante(String nombreParticipante) {
        this.nombreParticipante = nombreParticipante;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}