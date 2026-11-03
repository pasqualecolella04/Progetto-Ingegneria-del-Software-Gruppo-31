package stabilimento.entity;

import java.util.Date;
import java.util.Objects;

public class Tariffa {
    private String idTariffa;
    private double prezzoGiornaliero;
    private String stagione;
    private Date dataInizioValidita;
    private Date dataFineValidita;

    public Tariffa(String idTariffa, String stagione, double prezzoGiornaliero, Date dataInizioValidita, Date dataFineValidita) {
        this.idTariffa = idTariffa;
        this.stagione = stagione;
        this.prezzoGiornaliero = prezzoGiornaliero;
        this.dataInizioValidita = dataInizioValidita;
        this.dataFineValidita = dataFineValidita;
    }

    public boolean isValidaInData(Date data) {
        if (data == null || dataInizioValidita == null || dataFineValidita == null) {
            return false;
        }
        return !data.before(dataInizioValidita) && !data.after(dataFineValidita);
    }

    public double getPrezzoGiornaliero() {
        return prezzoGiornaliero;
    }

    public void setPrezzoGiornaliero(double nuovoPrezzo) {
        this.prezzoGiornaliero = nuovoPrezzo;
    }

    public String getIdTariffa() {
        return idTariffa;
    }

    public String getStagione() {
        return stagione;
    }

    public Date getDataInizioValidita() {
        return dataInizioValidita;
    }

    public Date getDataFineValidita() {
        return dataFineValidita;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tariffa)) return false;
        Tariffa tariffa = (Tariffa) o;
        return Objects.equals(idTariffa, tariffa.idTariffa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTariffa);
    }
}