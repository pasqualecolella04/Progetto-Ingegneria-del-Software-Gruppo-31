package stabilimento.entity;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class Notifica {
    private String idNotifica;
    private String destinatario;
    private String testoRiepilogo;
    private Date dataInvio;
    private boolean esitoInvio;

    public Notifica(String destinatario, String testoRiepilogo, Date dataInvio) {
        this.idNotifica = "NOT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.destinatario = destinatario;
        this.testoRiepilogo = testoRiepilogo;
        this.dataInvio = dataInvio;
        this.esitoInvio = false;
    }

    public String getIdNotifica() {
        return idNotifica;
    }

    public void setIdNotifica(String idNotifica) {
        this.idNotifica = idNotifica;
    }

    public void setEsitoInvio(boolean esito) {
        this.esitoInvio = esito;
    }

    public boolean isEsitoInvio() {
        return esitoInvio;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getTestoRiepilogo() {
        return testoRiepilogo;
    }

    public Date getDataInvio() {
        return dataInvio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notifica)) return false;
        Notifica notifica = (Notifica) o;
        return Objects.equals(idNotifica, notifica.idNotifica);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idNotifica);
    }
}