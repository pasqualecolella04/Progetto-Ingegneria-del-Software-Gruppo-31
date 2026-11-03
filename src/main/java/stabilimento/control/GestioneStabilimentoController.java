package stabilimento.control;

import stabilimento.database.TariffaDAO;
import stabilimento.dto.ReportDTO;
import stabilimento.entity.Postazione;
import stabilimento.entity.StabilimentoBalneare;
import stabilimento.entity.Tariffa;

import java.io.File;
import java.util.Date;

public class GestioneStabilimentoController {
    private static GestioneStabilimentoController instance;
    private StabilimentoBalneare stabilimento;

    private GestioneStabilimentoController() {
        this.stabilimento = new StabilimentoBalneare("STAB-01", "Lido Gruppo 31", "Via Marina 31, Napoli");
    }

    public static synchronized GestioneStabilimentoController getInstance() {
        if (instance == null) {
            instance = new GestioneStabilimentoController();
        }
        return instance;
    }

    /**
     * Passo 2-4.2 del Sequence Diagram 4.2.2 (GeneraReport):
     * valida i filtri ed estrae i dati tramite StabilimentoBalneare.estraiEdElaboraDatiReport.
     * Restituisce null se non vi sono prenotazioni nel periodo/criterio (Scenario alternativo 4.1-4.2).
     */
    public ReportDTO generaReport(String tipoReport, Date dataInizio, Date dataFine, String idCliente) {
        if (tipoReport == null || tipoReport.isBlank()) {
            throw new IllegalArgumentException("Selezionare una tipologia di report valida");
        }

        if ("Settimanale".equalsIgnoreCase(tipoReport) || "Mensile".equalsIgnoreCase(tipoReport)) {
            if (dataInizio == null || dataFine == null || dataInizio.after(dataFine)) {
                throw new IllegalArgumentException("Intervallo di date inserito non valido");
            }
        } else if ("Cliente".equalsIgnoreCase(tipoReport)) {
            if (idCliente == null || idCliente.isBlank()) {
                throw new IllegalArgumentException("Selezionare un identificativo cliente valido");
            }
        } else {
            throw new IllegalArgumentException("Selezionare una tipologia di report valida");
        }

        return stabilimento.estraiEdElaboraDatiReport(tipoReport, dataInizio, dataFine, idCliente);
    }

    /**
     * Passo 4.3 del Sequence Diagram 4.2.2 (GeneraReport):
     * delega a StabilimentoBalneare.generaFileEsportazione(datiReportElaborati, formatoScelto).
     */
    public File esportaReport(ReportDTO datiReportElaborati, String formatoScelto) {
        return stabilimento.generaFileEsportazione(datiReportElaborati, formatoScelto);
    }

    public boolean aggiungiPostazione(int numero, int fila, String posizione) {
        if (numero <= 0 || fila <= 0 || posizione == null || posizione.isBlank()) {
            return false;
        }
        String idPostazione = String.format("POS-%02d-%02d", fila, numero);
        Postazione pos = new Postazione(idPostazione, numero, fila, posizione);
        return stabilimento.aggiungiPostazione(pos);
    }

    public boolean rimuoviPostazione(String idPostazione) {
        if (idPostazione == null || idPostazione.isBlank()) {
            return false;
        }
        return stabilimento.rimuoviPostazione(idPostazione);
    }

    public boolean aggiornaTariffa(String idTariffa, double nuovoPrezzo, String stagione) {
        if (idTariffa == null || idTariffa.isBlank() || nuovoPrezzo <= 0 || stagione == null) {
            return false;
        }
        Tariffa t = new Tariffa(idTariffa, stagione, nuovoPrezzo, new Date(), new Date(System.currentTimeMillis() + 7776000000L));
        t.setPrezzoGiornaliero(nuovoPrezzo);
        return new TariffaDAO().updateTariffa(t);
    }
}