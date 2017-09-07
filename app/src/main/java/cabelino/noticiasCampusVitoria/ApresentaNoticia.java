package cabelino.noticiasCampusVitoria;

/**
 * Created by cabelino on 13/02/17.
 */

public class ApresentaNoticia {
    private String rank;
    private String country;
    private String population;
    private int flag;

    public ApresentaNoticia(int flag, String titulo, String texto) {
        this.rank = titulo;
        this.country = texto;
        this.population = population;
        this.flag = flag;
    }

    public String getTitulo() {
        return rank;
    }

    public void setTitulo(String rank) {
        this.rank = rank;
    }

    public String getTexto() {
        return country;
    }

    public void setTexto(String country) {
        this.country = country;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
