package stabilimento.control;

import stabilimento.boundary.BoundarySistemaNotifica;
import stabilimento.database.NotificaDAO;
import stabilimento.database.PostazioneDAO;
import stabilimento.database.PrenotazioneDAO;
import stabilimento.database.ServizioAggiuntivoDAO;
import stabilimento.database.UtenteDAO;
import stabilimento.entity.Cliente;
import stabilimento.entity.Notifica;
import stabilimento.entity.Postazione;
import stabilimento.entity.Prenotazione;
import stabilimento.entity.ServizioAggiuntivo;
import stabilimento.entity.StabilimentoBalneare;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GestionePrenotazioniController {
    private static GestionePrenotazioniController instance;
    private StabilimentoBalneare stabilimentoCorrente;

    private Date dataInizioSessione;
    private Date dataFineSessione;

    private GestionePrenotazioniController() {
        this.stabilimentoCorrente = new StabilimentoBalneare("STAB-01", "Lido Gruppo 31", "Via Marina 31, Napoli");
    }

    public static synchronized GestionePrenotazioniController getInstance() {
        if (instance == null) {
            instance = new GestionePrenotazioniController();
        }
        return instance;
    }

    /**
     * Passo 2-3 del Sequence Diagram 4.2.1:
     * delega a StabilimentoBalneare.getPostazioniDisponibili(dataInizio, dataFine).
     */
    public List<Postazione> visualizzaPostazioniDisponibili(Date dataInizio, Date dataFine) {
        if (dataInizio == null || dataFine == null || dataInizio.after(dataFine)) {
            throw new IllegalArgumentException("Intervallo di date inserito non valido");
        }
        this.dataInizioSessione = dataInizio;
        this.dataFineSessione = dataFine;
        return stabilimentoCorrente.getPostazioniDisponibili(dataInizio, dataFine);
    }

    /**
     * Passo 4.1-4.2 del Sequence Diagram 4.2.1:
     * calcola l'importo parziale della postazione interrogando getTariffaGiornaliera(data).
     */
    public double calcolaImportoParziale(String idPostazione, Date dataInizio, Date dataFine) {
        if (idPostazione == null || idPostazione.isBlank()) {
            throw new IllegalArgumentException("Identificativo postazione non valido");
        }
        if (dataInizio == null || dataFine == null || dataInizio.after(dataFine)) {
            throw new IllegalArgumentException("Intervallo di date inserito non valido");
        }
        this.dataInizioSessione = dataInizio;
        this.dataFineSessione = dataFine;

        PostazioneDAO posDAO = new PostazioneDAO();
        Postazione pos = posDAO.readAllPostazioni().stream()
                .filter(p -> p.getIdPostazione().equals(idPostazione))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Postazione selezionata inesistente: " + idPostazione));

        double importoBase = 0.0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataInizio);
        while (!cal.getTime().after(dataFine)) {
            importoBase += pos.getTariffaGiornaliera(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return importoBase;
    }

    /**
     * Passo 4.3 del Sequence Diagram 4.2.1:
     * aggiorna l'importo parziale aggiungendo il costo dei servizi selezionati.
     */
    public double aggiornaImportoConServizi(double importoBase, List<String> listaIdServizi, List<Integer> quantita) {
        if (listaIdServizi == null || quantita == null || listaIdServizi.isEmpty()) {
            return importoBase;
        }
        if (listaIdServizi.size() != quantita.size()) {
            throw new IllegalArgumentException("Le liste dei servizi e delle quantità devono avere la stessa dimensione.");
        }

        int giorni = 1;
        Date dataRif = new Date();
        if (dataInizioSessione != null && dataFineSessione != null && !dataInizioSessione.after(dataFineSessione)) {
            long diff = dataFineSessione.getTime() - dataInizioSessione.getTime();
            giorni = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
            dataRif = dataInizioSessione;
        }

        ServizioAggiuntivoDAO servDAO = new ServizioAggiuntivoDAO();
        double importoServizi = 0.0;

        for (int i = 0; i < listaIdServizi.size(); i++) {
            String idServizio = listaIdServizi.get(i);
            int qta = quantita.get(i) != null ? quantita.get(i) : 0;
            if (qta > 0) {
                ServizioAggiuntivo s = servDAO.readServizioById(idServizio);
                if (s == null) {
                    throw new IllegalArgumentException("Servizio aggiuntivo non trovato: " + idServizio);
                }
                if (!s.verificaDisponibilita(qta)) {
                    throw new IllegalArgumentException("Quantità richiesta superiore alla disponibilità massima per: " + s.getNomeServizio());
                }
                importoServizi += s.getPrezzoGiornaliero(dataRif) * qta * giorni;
            }
        }

        return importoBase + importoServizi;
    }

    /**
     * Passo 4.5 del Sequence Diagram 4.2.1:
     * istanzia Prenotazione, ServizioPrenotato, persiste tramite i DAO e invia la Notifica.
     * Restituisce true se la prenotazione e la notifica vanno a buon fine,
     * false se la prenotazione è salvata ma il Servizio di Notifica restituisce errore (Scenario alternativo 4.5.1-4.5.3).
     */
    public boolean confermaERegistraPrenotazione(String idCliente,
                                                 String idPostazione,
                                                 Map<String, Integer> serviziScelti,
                                                 Date dataInizio,
                                                 Date dataFine) {
        if (idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException("Identificativo cliente obbligatorio");
        }
        if (idPostazione == null || idPostazione.isBlank()) {
            throw new IllegalArgumentException("Identificativo postazione obbligatorio");
        }
        if (dataInizio == null || dataFine == null || dataInizio.after(dataFine)) {
            throw new IllegalArgumentException("Intervallo di date inserito non valido");
        }

        UtenteDAO utenteDAO = new UtenteDAO();
        PostazioneDAO posDAO = new PostazioneDAO();
        PrenotazioneDAO prenDAO = new PrenotazioneDAO();
        ServizioAggiuntivoDAO servDAO = new ServizioAggiuntivoDAO();
        NotificaDAO notDAO = new NotificaDAO();

        Cliente cliente = utenteDAO.readClienteById(idCliente);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente non trovato nel sistema: " + idCliente);
        }

        Postazione pos = posDAO.readAllPostazioni().stream()
                .filter(p -> p.getIdPostazione().equals(idPostazione))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Postazione non trovata: " + idPostazione));

        if (prenDAO.checkConflittoPostazione(idPostazione, dataInizio, dataFine)) {
            throw new IllegalStateException("Nessuna postazione disponibile per il periodo richiesto");
        }

        // Creazione istanza Prenotazione nello stato "In Attesa di Conferma"
        Prenotazione p = new Prenotazione(cliente, pos, dataInizio, dataFine, "In Attesa di Conferma");

        // Aggiunta opzionale dei servizi scelti e creazione delle istanze di ServizioPrenotato
        if (serviziScelti != null && !serviziScelti.isEmpty()) {
            for (Map.Entry<String, Integer> entry : serviziScelti.entrySet()) {
                int qta = entry.getValue() != null ? entry.getValue() : 0;
                if (qta > 0) {
                    ServizioAggiuntivo s = servDAO.readServizioById(entry.getKey());
                    if (s == null) {
                        throw new IllegalArgumentException("Servizio aggiuntivo inesistente: " + entry.getKey());
                    }
                    p.aggiungiServizioPrenotato(s, qta);
                }
            }
        }

        // Passo 4.5.1: Salvataggio su DB e aggiornamento stato Postazione
        boolean creata = prenDAO.createPrenotazione(p);
        if (!creata) {
            throw new RuntimeException("Errore nel salvataggio della prenotazione su database.");
        }

        if (!p.getServiziPrenotati().isEmpty()) {
            servDAO.saveServiziPrenotati(p.getIdPrenotazione(), p.getServiziPrenotati());
        }

        pos.setOccupata(true);
        posDAO.updatePostazione(pos);
        cliente.aggiungiPrenotazioneStorico(p);

        // Passo 4.5.2: <<include>> InviaNotifica
        String testoRiepilogo = String.format("Prenotazione %s registrata nello stato '%s'. Importo totale: EUR %.2f",
                p.getIdPrenotazione(), p.getStato(), p.getImportoTotale());
        Notifica n = new Notifica(cliente.getEmail(), testoRiepilogo, new Date());

        BoundarySistemaNotifica boundaryNotifica = new BoundarySistemaNotifica();
        boolean esitoNotifica = boundaryNotifica.inviaNotificaRiepilogo(cliente.getEmail(), testoRiepilogo);
        n.setEsitoInvio(esitoNotifica);
        notDAO.createNotifica(n, p.getIdPrenotazione());

        return esitoNotifica;
    }

    /**
     * Passo 4.4.2 del Sequence Diagram 4.2.1:
     * pulisce i dati temporanei della sessione di prenotazione in caso di rinuncia.
     */
    public void pulisciSessioneTemporanea() {
        this.dataInizioSessione = null;
        this.dataFineSessione = null;
    }

    public boolean modificaPrenotazione(String idPrenotazione, String nuovaPostazione, Map<String, Integer> nuoviServizi) {
        PrenotazioneDAO prenDAO = new PrenotazioneDAO();
        PostazioneDAO posDAO = new PostazioneDAO();
        ServizioAggiuntivoDAO servDAO = new ServizioAggiuntivoDAO();

        Prenotazione p = prenDAO.readPrenotazioneById(idPrenotazione);
        if (p == null) {
            throw new IllegalArgumentException("Prenotazione non trovata: " + idPrenotazione);
        }

        Postazione targetPos = p.getPostazioneScelta();
        if (nuovaPostazione != null && !nuovaPostazione.equals(targetPos.getIdPostazione())) {
            if (prenDAO.checkConflittoPostazione(nuovaPostazione, p.getDataInizio(), p.getDataFine())) {
                return false;
            }
            targetPos = posDAO.readAllPostazioni().stream()
                    .filter(pos -> pos.getIdPostazione().equals(nuovaPostazione))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Nuova postazione inesistente: " + nuovaPostazione));
        }

        Prenotazione aggiornata = new Prenotazione(p.getCliente(), targetPos, p.getDataInizio(), p.getDataFine(), p.getStato());
        aggiornata.setIdPrenotazione(p.getIdPrenotazione());

        if (nuoviServizi != null) {
            for (Map.Entry<String, Integer> entry : nuoviServizi.entrySet()) {
                int qta = entry.getValue() != null ? entry.getValue() : 0;
                if (qta > 0) {
                    ServizioAggiuntivo sa = servDAO.readServizioById(entry.getKey());
                    aggiornata.aggiungiServizioPrenotato(sa, qta);
                }
            }
        }

        boolean ok = prenDAO.updatePrenotazione(aggiornata);
        if (ok && !aggiornata.getServiziPrenotati().isEmpty()) {
            servDAO.saveServiziPrenotati(aggiornata.getIdPrenotazione(), aggiornata.getServiziPrenotati());
        }
        return ok;
    }

    public boolean annullaPrenotazione(String idPrenotazione, Date dataRichiesta) {
        PrenotazioneDAO prenDAO = new PrenotazioneDAO();
        Prenotazione p = prenDAO.readPrenotazioneById(idPrenotazione);
        if (p == null || dataRichiesta == null) {
            return false;
        }
        if (dataRichiesta.after(p.getDataInizio())) {
            return false;
        }
        return prenDAO.updateStatoPrenotazione(idPrenotazione, "Annullata");
    }

    public boolean modificaStatoPrenotazione(String idPrenotazione, String nuovoStato) {
        if (idPrenotazione == null || nuovoStato == null || nuovoStato.isBlank()) {
            return false;
        }
        return new PrenotazioneDAO().updateStatoPrenotazione(idPrenotazione, nuovoStato);
    }
}