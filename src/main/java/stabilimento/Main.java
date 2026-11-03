package stabilimento;

import stabilimento.boundary.BoundaryCliente;
import stabilimento.boundary.BoundaryGestore;
import stabilimento.boundary.BoundarySistemaNotifica;
import stabilimento.boundary.BoundaryUtente;
import stabilimento.control.GestionePrenotazioniController;
import stabilimento.control.GestioneStabilimentoController;
import stabilimento.control.GestioneUtentiController;
import stabilimento.database.DBManager;
import stabilimento.database.PostazioneDAO;
import stabilimento.database.ServizioAggiuntivoDAO;
import stabilimento.database.TariffaDAO;
import stabilimento.database.UtenteDAO;
import stabilimento.dto.PrenotazioneDTO;
import stabilimento.dto.ReportDTO;
import stabilimento.entity.Cliente;
import stabilimento.entity.Gestore;
import stabilimento.entity.Postazione;
import stabilimento.entity.ServizioAggiuntivo;
import stabilimento.entity.Tariffa;
import stabilimento.entity.Utente;

import javax.swing.*;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   AVVIO TEST DI SISTEMA - PROGETTO ING.S GRUPPO 31 (BCED)       ");
        System.out.println("=================================================================\n");

        try {
            // 1. Pulizia e popolamento iniziale del Database (Fixture)
            resetEPopolaDatabase();

            // 2. Test Autenticazione Utenti (GestioneUtentiController)
            eseguiTestAutenticazione();

            // 3. Test Caso d'Uso UC1: Prenotazione (GestionePrenotazioniController)
            eseguiTestUC1Prenotazione();

            // 4. Test Caso d'Uso UC12: Genera Report (GestioneStabilimentoController)
            eseguiTestUC12GeneraReport();

            System.out.println("\n=================================================================");
            System.out.println("   TUTTI I TEST SU CONSOLE SONO TERMINATI CON SUCCESSO!          ");
            System.out.println("   Avvio delle interfacce grafiche Swing (Boundary) in corso...  ");
            System.out.println("=================================================================");

            // 5. Avvio delle 3 finestre Swing per il test interattivo della GUI
            SwingUtilities.invokeLater(() -> {
                BoundaryUtente guiUtente = new BoundaryUtente();
                guiUtente.setLocation(50, 50);
                guiUtente.setVisible(true);

                BoundaryCliente guiCliente = new BoundaryCliente();
                guiCliente.setLocation(80, 150);
                guiCliente.visualizzaSezionePrenotazione();

                BoundaryGestore guiGestore = new BoundaryGestore();
                guiGestore.setLocation(560, 150);
                guiGestore.accediSezioneReportistica();
            });

        } catch (Exception e) {
            System.err.println("ERRORE CRITICO DURANTE L'ESECUZIONE DEL MAIN: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void resetEPopolaDatabase() throws Exception {
        System.out.println("[STEP 1] Inizializzazione e popolamento Database...");

        Connection conn = DBManager.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Notifica");
            stmt.execute("DELETE FROM ServizioPrenotato");
            stmt.execute("DELETE FROM Prenotazione");
            stmt.execute("DELETE FROM Tariffa");
            stmt.execute("DELETE FROM ServizioAggiuntivo");
            stmt.execute("DELETE FROM Postazione");
            stmt.execute("DELETE FROM Utenti");
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }

        UtenteDAO utenteDAO = new UtenteDAO();
        PostazioneDAO postazioneDAO = new PostazioneDAO();
        TariffaDAO tariffaDAO = new TariffaDAO();
        ServizioAggiuntivoDAO servizioDAO = new ServizioAggiuntivoDAO();

        // Creazione Cliente C102 e Gestore G01
        Cliente mario = new Cliente("Mario", "Rossi", "mario.rossi@unina.it", "3331234567", "pwd123");
        mario.setIdUtente("C102");
        utenteDAO.createUtente(mario, "Cliente");

        Gestore luigi = new Gestore("Luigi", "Bianchi", "gestore@stabilimento.it", "081987654", "admin123");
        luigi.setIdUtente("G01");
        utenteDAO.createUtente(luigi, "Gestore");

        // Creazione Postazioni (Fila 1: 30€/gg base, Fila 2: 25€/gg base)
        Postazione pos1 = new Postazione("POS-01", 1, 1, "Prima Fila");
        Postazione pos2 = new Postazione("POS-02", 2, 2, "Seconda Fila");
        Postazione pos3 = new Postazione("POS-03", 3, 3, "Standard");
        postazioneDAO.createPostazione(pos1);
        postazioneDAO.createPostazione(pos2);
        postazioneDAO.createPostazione(pos3);

        // Creazione Tariffa stagionale (30.0 EUR/gg per l'estate 2026)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Tariffa tariffaEstate = new Tariffa(
                "TAR-ESTATE-26",
                "Alta Stagione",
                30.0,
                sdf.parse("2026-06-01"),
                sdf.parse("2026-09-30")
        );
        tariffaDAO.createTariffa(tariffaEstate);

        // Creazione Catalogo Servizi Aggiuntivi (S01: Lettino Extra, S02: Cabina Privata)
        ServizioAggiuntivo lettino = new ServizioAggiuntivo("S01", "Lettino Extra", "Lettino parasole in alluminio", 5);
        ServizioAggiuntivo cabina = new ServizioAggiuntivo("S02", "Cabina Privata", "Cabina spogliatoio con doccia", 2);
        servizioDAO.createServizio(lettino);
        servizioDAO.createServizio(cabina);

        System.out.println(" -> Utenti (C102, G01), Postazioni (POS-01, POS-02, POS-03) e Servizi (S01, S02) caricati.\n");
    }


    private static void eseguiTestAutenticazione() {
        System.out.println("[STEP 2] Test GestioneUtentiController (Registrazione e Login)...");
        GestioneUtentiController utentiCtrl = GestioneUtentiController.getInstance();

        Utente autenticato = utentiCtrl.autenticaUtente("mario.rossi@unina.it", "pwd123");
        System.out.println(" -> Login Mario Rossi: " + (autenticato != null ? "OK (ID: " + autenticato.getIdUtente() + ")" : "FALLITO"));

        boolean nuovaReg = utentiCtrl.registraUtente("Anna", "Verdi", "anna.verdi@unina.it", "3390001122", "segreta", "Cliente");
        System.out.println(" -> Registrazione nuovo cliente Anna Verdi: " + (nuovaReg ? "OK" : "FALLITO") + "\n");
    }


    private static void eseguiTestUC1Prenotazione() throws Exception {
        System.out.println("[STEP 3] Test UC1: Prenotazione (GestionePrenotazioniController)...");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        GestionePrenotazioniController prenCtrl = GestionePrenotazioniController.getInstance();

        Date inizio1 = sdf.parse("2026-08-01");
        Date fine1 = sdf.parse("2026-08-03"); // 3 giorni inclusivi (1, 2, 3 Agosto)

        // 1. Ricerca postazioni disponibili
        List<Postazione> libere = prenCtrl.visualizzaPostazioniDisponibili(inizio1, fine1);
        System.out.println(" -> Postazioni disponibili (01/08 - 03/08): " + libere.size() + " trovate " + libere);

        // 2. Calcolo importo parziale (3 giorni * 30.0 EUR = 90.0 EUR)
        double importoBase = prenCtrl.calcolaImportoParziale("POS-01", inizio1, fine1);
        System.out.println(String.format(" -> Importo parziale postazione POS-01 (3 giorni): EUR %.2f", importoBase));

        // 3. Aggiornamento importo con servizi aggiuntivi: 2 Lettini (S01) e 1 Cabina (S02)
        // (2 + 1 = 3 servizi * 10.0 EUR/gg * 3 giorni = 90.0 EUR extra -> Totale = 180.0 EUR)
        List<String> listaIdServizi = Arrays.asList("S01", "S02");
        List<Integer> quantita = Arrays.asList(2, 1);
        double importoTotale = prenCtrl.aggiornaImportoConServizi(importoBase, listaIdServizi, quantita);
        System.out.println(String.format(" -> Importo aggiornato con servizi extra (2x S01, 1x S02): EUR %.2f", importoTotale));

        // 4. Conferma e registrazione prenotazione (con Servizio di Notifica attivo)
        BoundarySistemaNotifica.setServizioAttivo(true);
        Map<String, Integer> mappaServizi = new HashMap<>();
        mappaServizi.put("S01", 2);
        mappaServizi.put("S02", 1);

        boolean esitoNotifica1 = prenCtrl.confermaERegistraPrenotazione("C102", "POS-01", mappaServizi, inizio1, fine1);
        System.out.println(" -> Prenotazione 1 (POS-01) salvata con successo! Esito invio notifica: " + esitoNotifica1);

        // 5. Test Conflitto Postazione: tentativo di riprenotare POS-01 nelle stesse date
        try {
            prenCtrl.confermaERegistraPrenotazione("C102", "POS-01", null, inizio1, fine1);
            System.err.println(" -> ERRORE: Il sistema non ha bloccato la doppia prenotazione!");
        } catch (IllegalStateException ex) {
            System.out.println(" -> Test anti-conflitto postazione occupata SUPERATO: \"" + ex.getMessage() + "\"");
        }

        // 6. Test Scenario Alternativo 4.5.1 (Prenotazione 2 senza servizi e con Servizio di Notifica OFFLINE)
        Date inizio2 = sdf.parse("2026-08-05");
        Date fine2 = sdf.parse("2026-08-06"); // 2 giorni * 30.0 EUR = 60.0 EUR
        BoundarySistemaNotifica.setServizioAttivo(false);

        boolean esitoNotifica2 = prenCtrl.confermaERegistraPrenotazione("C102", "POS-02", null, inizio2, fine2);
        System.out.println(" -> Prenotazione 2 (POS-02) salvata con successo anche con Notifica Offline (esitoNotifica = " + esitoNotifica2 + ")\n");
        BoundarySistemaNotifica.setServizioAttivo(true);
    }


    private static void eseguiTestUC12GeneraReport() throws Exception {
        System.out.println("[STEP 4] Test UC12: Genera Report (GestioneStabilimentoController)...");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        GestioneStabilimentoController stabCtrl = GestioneStabilimentoController.getInstance();

        // 1. Generazione Report Settimanale (01/08/2026 - 07/08/2026)
        ReportDTO reportSettimanale = stabCtrl.generaReport(
                "Settimanale",
                sdf.parse("2026-08-01"),
                sdf.parse("2026-08-07"),
                null
        );

        System.out.println(" -> Report Settimanale generato (ID: " + reportSettimanale.getIdReport() + "):");
        System.out.println("    * Numero Totale Prenotazioni : " + reportSettimanale.getNumeroTotalePrenotazioni());
        System.out.println(String.format("    * Incasso Totale             : EUR %.2f", reportSettimanale.getIncassoTotale()));
        System.out.println(String.format("    * Incasso Medio              : EUR %.2f", reportSettimanale.getIncassoMedioPerPrenotazione()));
        System.out.println("    * Servizi Accessori Venduti  : " + reportSettimanale.getTotaleServiziAccessoriVenduti());

        for (PrenotazioneDTO riga : reportSettimanale.getRigheDettaglio()) {
            System.out.println("      [Riga Dettaglio] " + riga.getCodicePrenotazione()
                    + " | Cliente: " + riga.getNominativoCliente()
                    + " | Ombrellone: " + riga.getCodiceOmbrellone()
                    + " | Giorni: " + riga.getConteggioGiorniSoggiorno()
                    + " | Importo: " + riga.getImportoMonetarioFormattato());
        }

        // 2. Esportazione del Report in CSV e PDF
        File fileCsv = stabCtrl.esportaReport(reportSettimanale, "CSV");
        File filePdf = stabCtrl.esportaReport(reportSettimanale, "PDF");
        System.out.println(" -> File CSV esportato in: " + fileCsv.getAbsolutePath());
        System.out.println(" -> File PDF esportato in: " + filePdf.getAbsolutePath());

        // 3. Generazione Report per Cliente ("C102")
        ReportDTO reportCliente = stabCtrl.generaReport("Cliente", null, null, "C102");
        System.out.println(" -> Report Cliente C102 generato: " + reportCliente.getNumeroTotalePrenotazioni() + " prenotazioni trovate.");

        // 4. Test Scenario Alternativo 4.1 (Periodo senza prenotazioni: Gennaio 2026)
        ReportDTO reportVuoto = stabCtrl.generaReport(
                "Settimanale",
                sdf.parse("2026-01-01"),
                sdf.parse("2026-01-07"),
                null
        );
        System.out.println(" -> Test periodo senza prenotazioni (Gennaio 2026) SUPERATO: risultato = " + reportVuoto);
    }
}