package stabilimento.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ServizioAggiuntivo {
    private String idServizio;
    private String nomeServizio;
    private String descrizione;
    private int disponibilitaMax;
    private List<Tariffa> tariffeServizio;

    public ServizioAggiuntivo(String idServizio, String nomeServizio, String descrizione, int disponibilitaMax) {
        this.idServizio = idServizio;
        this.nomeServizio = nomeServizio;
        this.descrizione = descrizione;
        this.disponibilitaMax = disponibilitaMax;
        this.tariffeServizio = new ArrayList<>();
    }

    public String getIdServizio() {
        return idServizio;
    }

    public double getPrezzoGiornaliero(Date data) {
        if (tariffeServizio != null && data != null) {
            for (Tariffa t : tariffeServizio) {
                if (t.isValidaInData(data)) {
                    return t.getPrezzoGiornaliero();
                }
            }
        }
        return 10.0;
    }

    public boolean verificaDisponibilita(int quantita) {
        return quantita > 0 && quantita <= this.disponibilitaMax;
    }

    public String getNomeServizio() {
        return nomeServizio;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public int getDisponibilitaMax() {
        return disponibilitaMax;
    }

    public List<Tariffa> getTariffeServizio() {
        return tariffeServizio;
    }

    public void setTariffeServizio(List<Tariffa> tariffeServizio) {
        this.tariffeServizio = tariffeServizio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServizioAggiuntivo)) return false;
        ServizioAggiuntivo that = (ServizioAggiuntivo) o;
        return Objects.equals(idServizio, that.idServizio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idServizio);
    }
}