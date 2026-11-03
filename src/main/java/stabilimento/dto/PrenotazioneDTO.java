package stabilimento.dto;

import java.util.Date;

public class PrenotazioneDTO {
    private final String codicePrenotazione;
    private final String idCliente;
    private final String nominativoCliente;
    private final String codiceOmbrellone;
    private final String coordinateOmbrellone;
    private final Date dataInizio;
    private final Date dataFine;
    private final int conteggioGiorniSoggiorno;
    private final int totaleServiziAccessoriAcquistati;
    private final double importoMonetarioComplessivo;
    private final String importoMonetarioFormattato;
    private final String statoCorrente;

    public PrenotazioneDTO(String codicePrenotazione,
                           String idCliente,
                           String nominativoCliente,
                           String codiceOmbrellone,
                           String coordinateOmbrellone,
                           Date dataInizio,
                           Date dataFine,
                           int conteggioGiorniSoggiorno,
                           int totaleServiziAccessoriAcquistati,
                           double importoMonetarioComplessivo,
                           String importoMonetarioFormattato,
                           String statoCorrente) {
        this.codicePrenotazione = codicePrenotazione;
        this.idCliente = idCliente;
        this.nominativoCliente = nominativoCliente;
        this.codiceOmbrellone = codiceOmbrellone;
        this.coordinateOmbrellone = coordinateOmbrellone;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.conteggioGiorniSoggiorno = conteggioGiorniSoggiorno;
        this.totaleServiziAccessoriAcquistati = totaleServiziAccessoriAcquistati;
        this.importoMonetarioComplessivo = importoMonetarioComplessivo;
        this.importoMonetarioFormattato = importoMonetarioFormattato;
        this.statoCorrente = statoCorrente;
    }

    public String getCodicePrenotazione() {
        return codicePrenotazione;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public String getNominativoCliente() {
        return nominativoCliente;
    }

    public String getCodiceOmbrellone() {
        return codiceOmbrellone;
    }

    public String getCoordinateOmbrellone() {
        return coordinateOmbrellone;
    }

    public Date getDataInizio() {
        return dataInizio;
    }

    public Date getDataFine() {
        return dataFine;
    }

    public int getConteggioGiorniSoggiorno() {
        return conteggioGiorniSoggiorno;
    }

    public int getTotaleServiziAccessoriAcquistati() {
        return totaleServiziAccessoriAcquistati;
    }

    public double getImportoMonetarioComplessivo() {
        return importoMonetarioComplessivo;
    }

    public String getImportoMonetarioFormattato() {
        return importoMonetarioFormattato;
    }

    public String getStatoCorrente() {
        return statoCorrente;
    }
}