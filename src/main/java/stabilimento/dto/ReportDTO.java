package stabilimento.dto;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ReportDTO {
    private final String idReport;
    private final String tipoReport;
    private final Date dataInizio;
    private final Date dataFine;
    private final String idClienteFiltro;
    private final int numeroTotalePrenotazioni;
    private final double incassoTotale;
    private final double incassoMedioPerPrenotazione;
    private final int totaleServiziAccessoriVenduti;
    private final List<PrenotazioneDTO> righeDettaglio;

    public ReportDTO(String idReport,
                     String tipoReport,
                     Date dataInizio,
                     Date dataFine,
                     String idClienteFiltro,
                     int numeroTotalePrenotazioni,
                     double incassoTotale,
                     double incassoMedioPerPrenotazione,
                     int totaleServiziAccessoriVenduti,
                     List<PrenotazioneDTO> righeDettaglio) {
        this.idReport = idReport;
        this.tipoReport = tipoReport;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.idClienteFiltro = idClienteFiltro;
        this.numeroTotalePrenotazioni = numeroTotalePrenotazioni;
        this.incassoTotale = incassoTotale;
        this.incassoMedioPerPrenotazione = incassoMedioPerPrenotazione;
        this.totaleServiziAccessoriVenduti = totaleServiziAccessoriVenduti;
        this.righeDettaglio = righeDettaglio != null
                ? Collections.unmodifiableList(righeDettaglio)
                : Collections.emptyList();
    }

    public String getIdReport() {
        return idReport;
    }

    public String getTipoReport() {
        return tipoReport;
    }

    public Date getDataInizio() {
        return dataInizio;
    }

    public Date getDataFine() {
        return dataFine;
    }

    public String getIdClienteFiltro() {
        return idClienteFiltro;
    }

    public int getNumeroTotalePrenotazioni() {
        return numeroTotalePrenotazioni;
    }

    public double getIncassoTotale() {
        return incassoTotale;
    }

    public double getIncassoMedioPerPrenotazione() {
        return incassoMedioPerPrenotazione;
    }

    public int getTotaleServiziAccessoriVenduti() {
        return totaleServiziAccessoriVenduti;
    }

    public List<PrenotazioneDTO> getRigheDettaglio() {
        return righeDettaglio;
    }
}