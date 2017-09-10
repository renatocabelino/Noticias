package cabelino.noticiasCampusVitoria;

/**
 * Created by cabelino on 13/02/17.
 */

public class ApresentaNoticia {
    private String Titulo;
    private String Data_Hora;
    private String Resumo;
    private int flag;

    public ApresentaNoticia(int flag, String titulo, String texto) {
        this.Titulo = titulo;
        this.Data_Hora = texto;
        this.Resumo = Resumo;
        this.flag = flag;
    }

    public String getTitulo() {
        return Titulo;
    }

    public void setTitulo(String titulo) {
        this.Titulo = titulo;
    }

    public String getTexto() {
        return Data_Hora;
    }

    public void setTexto(String data_hora) {
        this.Data_Hora = data_hora;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
