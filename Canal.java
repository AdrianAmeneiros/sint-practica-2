package p2;

public class Canal {

	private String idc;
	private String defaultlang;
	private String nombreCanal;

	// Constructor
	public Canal(String idc, String defaultlang, String nombreCanal) {
		this.idc = idc;
		this.defaultlang = defaultlang;
		this.nombreCanal = nombreCanal;
	}

	// Getters y setters
	public String getIdC() {
   	 	return idc;
	}

	public void setIdC(String idc) {
    	this.idc = idc;
	}

	public String getNombreCanal() {
		return nombreCanal;
	}

	public void setNombreCanal(String nombreCanal) {
		this.nombreCanal = nombreCanal;
	}
	public String getDefaultLang() {
    	return defaultlang;
	}

	public void setDefaultLang(String defaultlang) {
	    this.defaultlang = defaultlang;
	}
}