package stabilimento.entity;

import java.util.Objects;

public class ServizioPrenotato {
    private int quantitaRichiesta;
    private double subTotale;
    private ServizioAggiuntivo servizioRiferimento;

    public ServizioPrenotato(ServizioAggiuntivo servizio, int quantitaRichiesta, double subTotale) {
        this.servizioRiferimento = servizio;
        this.quantitaRichiesta = quantitaRichiesta;
        this.subTotale = subTotale;
    }

    public int getQuantitaRichiesta() {
        return quantitaRichiesta;
    }

    public double getSubTotale() {
        return subTotale;
    }

    public ServizioAggiuntivo getServizioRiferimento() {
        return servizioRiferimento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServizioPrenotato)) return false;
        ServizioPrenotato that = (ServizioPrenotato) o;
        return Objects.equals(servizioRiferimento, that.servizioRiferimento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servizioRiferimento);
    }
}