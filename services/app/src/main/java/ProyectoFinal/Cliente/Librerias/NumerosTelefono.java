package ProyectoFinal.Cliente.Librerias;

public class NumerosTelefono {
    private String pais;
    private String prefijo;

    public NumerosTelefono(String pais, String prefijo) {
        this.pais = pais;
        this.prefijo = prefijo;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public void setPrefijo(String numero) {
        this.prefijo = numero;
    }

    @Override
    public String toString() {
        return "NumerosTelefono{" +
                "pais='" + pais + '\'' +
                ", numero='" + prefijo + '\'' +
                '}';
    }
}
