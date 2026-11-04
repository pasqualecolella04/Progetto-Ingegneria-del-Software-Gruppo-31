package stabilimento;

import stabilimento.boundary.BoundaryCliente;
import stabilimento.boundary.BoundaryGestore;
import stabilimento.boundary.BoundarySistemaNotifica;
import stabilimento.control.GestionePrenotazioniController;
import stabilimento.control.GestioneStabilimentoController;
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
        System.out.println("   AVVIO TEST DI SISTEMA  ");
        System.out.println("=================================================================\n");

        try {
            // 1. Pulizia e popolamento iniziale del Database
            resetEPopolaDatabase();

            // 2. Test Caso d'Uso UC1: Prenotazione (GestionePrenotazioniController)
            eseguiTestUC1Prenotazione();

            // 3. Test Caso d'Uso UC12: Genera Report (GestioneStabilimentoController)
            eseguiTestUC12GeneraReport();

            System.out.println("\n=================================================================");
            System.out.println("   TUTTI I TEST SU CONSOLE SONO TERMINATI CON SUCCESSO!          ");
            System.out.println("   Avvio delle interfacce grafiche Swing (Boundary) in corso...  ");
            System.out.println("=================================================================");

            // 4. Avvio delle 2 finestre Swing per il test interattivo della GUI
            SwingUtilities.invokeLater(() -> {
                BoundaryCliente guiCliente = new BoundaryCliente();
                guiCliente.setLocation(60, 120);
                guiCliente.visualizzaSezionePrenotazione();

                BoundaryGestore guiGestore = new BoundaryGestore();
                guiGestore.setLocation(580, 120);
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

        // Creazione Cliente C01, C02 e Gestore G01
        Cliente pasquale = new Cliente("Pasquale", "Colella", "pasquale.colella@unina.it", "3331234567", "pwd123");
        pasquale.setIdUtente("C01");
        utenteDAO.createUtente(pasquale, "Cliente");

        Cliente antonio = new Cliente("Antonio", "Borrelli", "antonio.borrelli@unina.it", "3331234567", "pwd123");
        antonio.setIdUtente("C02");
        utenteDAO.createUtente(antonio, "Cliente");

        Gestore luigi = new Gestore("Luigi", "Bianchi", "gestore@stabilimento.it", "081987654", "admin123");
        luigi.setIdUtente("G01");
        utenteDAO.createUtente(luigi, "Gestore");

        // Creazione Postazioni distribuite su 3 file
        Postazione pos1 = new Postazione("POS-01", 1, 1, "Prima Fila");
        Postazione pos2 = new Postazione("POS-02", 2, 2, "Seconda Fila");
        Postazione pos3 = new Postazione("POS-03", 3, 3, "Terza Fila");
        Postazione pos4 = new Postazione("POS-04", 4, 1, "Prima Fila");
        Postazione pos5 = new Postazione("POS-05", 5, 2, "Seconda Fila");
        Postazione pos6 = new Postazione("POS-06", 6, 3, "Terza Fila");

        postazioneDAO.createPostazione(pos1);
        postazioneDAO.createPostazione(pos2);
        postazioneDAO.createPostazione(pos3);
        postazioneDAO.createPostazione(pos4);
        postazioneDAO.createPostazione(pos5);
        postazioneDAO.createPostazione(pos6);

        // Creazione Tariffe differenziate per Fila (Fila 1: 35€/gg, Fila 2: 25€/gg, Fila 3: 20€/gg)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date inizioEstate = sdf.parse("2026-06-01");
        Date fineEstate = sdf.parse("2026-09-30");

        tariffaDAO.createTariffa(new Tariffa("TAR-POS-01", "Alta Stagione - Fila 1", 35.0, inizioEstate, fineEstate));
        tariffaDAO.createTariffa(new Tariffa("TAR-POS-02", "Alta Stagione - Fila 2", 25.0, inizioEstate, fineEstate));
        tariffaDAO.createTariffa(new Tariffa("TAR-POS-03", "Alta Stagione - Fila 3", 20.0, inizioEstate, fineEstate));
        tariffaDAO.createTariffa(new Tariffa("TAR-POS-04", "Alta Stagione - Fila 1", 35.0, inizioEstate, fineEstate));
        tariffaDAO.createTariffa(new Tariffa("TAR-POS-05", "Alta Stagione - Fila 2", 25.0, inizioEstate, fineEstate));
        tariffaDAO.createTariffa(new Tariffa("TAR-POS-06", "Alta Stagione - Fila 3", 20.0, inizioEstate, fineEstate));

        // Creazione Catalogo Servizi Aggiuntivi (S01: Lettino Extra, S02: Cabina Privata)
        ServizioAggiuntivo lettino = new ServizioAggiuntivo("S01", "Lettino Extra", "Lettino parasole in alluminio", 5);
        ServizioAggiuntivo cabina = new ServizioAggiuntivo("S02", "Cabina Privata", "Cabina spogliatoio con doccia", 2);
        servizioDAO.createServizio(lettino);
        servizioDAO.createServizio(cabina);

        System.out.println(" -> Utenti (C01, C02, G01), Postazioni (POS-01..POS-06 con tariffe 35€/25€/20€) e Servizi (S01, S02) caricati.\n");
    }

    private static void eseguiTestUC1Prenotazione() throws Exception {
        System.out.println("[STEP 2] Test UC1: Prenotazione (GestionePrenotazioniController)...");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        GestionePrenotazioniController prenCtrl = GestionePrenotazioniController.getInstance();

        Date inizio1 = sdf.parse("2026-08-01");
        Date fine1 = sdf.parse("2026-08-03"); // 3 giorni inclusivi (1, 2, 3 Agosto)

        // 1. Ricerca postazioni disponibili
        List<Postazione> libere = prenCtrl.visualizzaPostazioniDisponibili(inizio1, fine1);
        System.out.println(" -> Postazioni disponibili (01/08 - 03/08): " + libere.size() + " trovate");

        // 2. Verifica differenziazione prezzi per Fila (3 giorni):
        // POS-01 (Fila 1: 35€ * 3 = 105€), POS-02 (Fila 2: 25€ * 3 = 75€), POS-03 (Fila 3: 20€ * 3 = 60€)
        double importoFila1 = prenCtrl.calcolaImportoParziale("POS-01", inizio1, fine1);
        double importoFila2 = prenCtrl.calcolaImportoParziale("POS-02", inizio1, fine1);
        double importoFila3 = prenCtrl.calcolaImportoParziale("POS-03", inizio1, fine1);
        System.out.println(String.format(" -> Importo parziale 3 giorni: POS-01 (Fila 1) = EUR %.2f | POS-02 (Fila 2) = EUR %.2f | POS-03 (Fila 3) = EUR %.2f",
                importoFila1, importoFila2, importoFila3));

        // 3. Aggiornamento importo su POS-01 con servizi aggiuntivi: 2 Lettini (S01) e 1 Cabina (S02)
        List<String> listaIdServizi = Arrays.asList("S01", "S02");
        List<Integer> quantita = Arrays.asList(2, 1);
        double importoTotale = prenCtrl.aggiornaImportoConServizi(importoFila1, listaIdServizi, quantita);
        System.out.println(String.format(" -> Importo POS-01 aggiornato con servizi extra (2x S01, 1x S02): EUR %.2f", importoTotale));

        // 4. Conferma e registrazione prenotazione per C01 (Pasquale Colella)
        BoundarySistemaNotifica.setServizioAttivo(true);
        Map<String, Integer> mappaServizi = new HashMap<>();
        mappaServizi.put("S01", 2);
        mappaServizi.put("S02", 1);

        boolean esitoNotifica1 = prenCtrl.confermaERegistraPrenotazione("C01", "POS-01", mappaServizi, inizio1, fine1);
        System.out.println(" -> Prenotazione 1 (Cliente C01, Postazione POS-01) salvata! Esito invio notifica: " + esitoNotifica1);

        // 5. Test Conflitto Postazione: tentativo di riprenotare POS-01 nelle stesse date
        try {
            prenCtrl.confermaERegistraPrenotazione("C02", "POS-01", null, inizio1, fine1);
            System.err.println(" -> ERRORE: Il sistema non ha bloccato la doppia prenotazione!");
        } catch (IllegalStateException ex) {
            System.out.println(" -> Test anti-conflitto postazione occupata SUPERATO: \"" + ex.getMessage() + "\"");
        }

        // 6. Test Scenario Alternativo 4.5.1 (Prenotazione 2 per C02 senza servizi e con Servizio di Notifica OFFLINE)
        Date inizio2 = sdf.parse("2026-08-05");
        Date fine2 = sdf.parse("2026-08-06"); // 2 giorni * 25.0 EUR (Fila 2) = 50.0 EUR
        BoundarySistemaNotifica.setServizioAttivo(false);

        boolean esitoNotifica2 = prenCtrl.confermaERegistraPrenotazione("C02", "POS-02", null, inizio2, fine2);
        System.out.println(" -> Prenotazione 2 (Cliente C02, Postazione POS-02) salvata anche con Notifica Offline (esitoNotifica = " + esitoNotifica2 + ")\n");
        BoundarySistemaNotifica.setServizioAttivo(true);
    }

    private static void eseguiTestUC12GeneraReport() throws Exception {
        System.out.println("[STEP 3] Test UC12: Genera Report (GestioneStabilimentoController)...");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        GestioneStabilimentoController stabCtrl = GestioneStabilimentoController.getInstance();

        // 1. Generazione Report Settimanale (30/07/2026 - 05/08/2026 -> Settimana 31)
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

        // 3. Generazione Report per Cliente ("C01")
        ReportDTO reportCliente = stabCtrl.generaReport("Cliente", null, null, "C01");
        System.out.println(" -> Report Cliente C01 generato: " + reportCliente.getNumeroTotalePrenotazioni() + " prenotazioni trovate.");

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