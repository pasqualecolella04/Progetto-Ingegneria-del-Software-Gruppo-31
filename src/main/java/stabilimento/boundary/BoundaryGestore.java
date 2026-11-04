package stabilimento.boundary;

import stabilimento.control.GestioneStabilimentoController;
import stabilimento.dto.PrenotazioneDTO;
import stabilimento.dto.ReportDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Interfaccia grafica Swing per l'attore Gestore (UC12: GeneraReport).
 * Implementa la navigazione in 2 fasi:
 * 1) Scelta iniziale della tipologia di Report (Settimanale, Mensile, Cliente).
 * 2) Visualizzazione della schermata report unica e dedicata alla tipologia scelta.
 */
public class BoundaryGestore extends JFrame {
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    private CardLayout cardLayout;
    private JPanel mainContainer;

    // Componenti Step 1: Scelta Tipologia
    private JComboBox<String> cmbSceltaInizialeTipo;

    // Componenti Step 2: Schermata Report dedicata
    private String tipoReportScelto;
    private JLabel lblTitoloSchermataReport;
    private JPanel pnlParametroSpecifico;
    private JComboBox<String> cmbSettimanaAnno;
    private JComboBox<String> cmbMeseAnno;
    private JTextField txtIdCliente;
    private JComboBox<String> cmbFormatoEsportazione;
    private JButton btnGeneraAnteprima;
    private JButton btnEsportaReport;
    private JButton btnCambiaTipologia;
    private JLabel lblRiepilogoStatistiche;
    private JTable tblDettaglioReport;
    private DefaultTableModel tableModel;

    private ReportDTO datiReportElaboratiCorrente;

    public BoundaryGestore() {
        setTitle("Progetto Ing.S Gruppo 31 - BoundaryGestore (UC12: Genera Report)");
        setSize(950, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        mostraFormParametriReport();
    }

    public void accediSezioneReportistica() {
        mostraFormParametriReport();
        setVisible(true);
    }

    public void mostraFormParametriReport() {
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Costruzione dei due pannelli (Step 1: Scelta Tipo | Step 2: Schermata Report)
        JPanel step1SceltaTipo = creaPannelloSceltaTipologia();
        JPanel step2SchermataReport = creaPannelloSchermataReport();

        mainContainer.add(step1SceltaTipo, "STEP1_SCELTA_TIPO");
        mainContainer.add(step2SchermataReport, "STEP2_SCHERMATA_REPORT");

        setContentPane(mainContainer);
        cardLayout.show(mainContainer, "STEP1_SCELTA_TIPO");
    }

    /**
     * STEP 1: Schermata iniziale per selezionare prima la tipologia di report.
     */
    private JPanel creaPannelloSceltaTipologia() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel box = new JPanel(new GridLayout(3, 1, 15, 15));
        box.setBorder(BorderFactory.createTitledBorder("Passo 1: Seleziona la Tipologia di Report da Generare"));
        box.setPreferredSize(new Dimension(500, 200));

        JLabel lblIstruzioni = new JLabel("Scegli la tipologia di report per aprire la schermata dedicata:", SwingConstants.CENTER);
        lblIstruzioni.setFont(new Font("SansSerif", Font.PLAIN, 14));

        cmbSceltaInizialeTipo = new JComboBox<>(new String[]{"Settimanale", "Mensile", "Cliente"});
        cmbSceltaInizialeTipo.setFont(new Font("SansSerif", Font.BOLD, 14));

        JButton btnProsegui = new JButton("Prosegui alla Schermata Report →");
        btnProsegui.setFont(new Font("SansSerif", Font.BOLD, 14));

        btnProsegui.addActionListener(e -> {
            this.tipoReportScelto = (String) cmbSceltaInizialeTipo.getSelectedItem();
            configuraSchermataPerTipologia(this.tipoReportScelto);
            cardLayout.show(mainContainer, "STEP2_SCHERMATA_REPORT");
        });

        box.add(lblIstruzioni);
        box.add(cmbSceltaInizialeTipo);
        box.add(btnProsegui);

        panel.add(box);
        return panel;
    }

    /**
     * STEP 2: Schermata report unica e dinamica in base alla tipologia scelta.
     */
    private JPanel creaPannelloSchermataReport() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Barra superiore con titolo e pulsante per tornare alla scelta tipologia
        JPanel headerPanel = new JPanel(new BorderLayout());
        lblTitoloSchermataReport = new JLabel("Schermata Report");
        lblTitoloSchermataReport.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnCambiaTipologia = new JButton("← Cambia Tipologia Report");
        btnCambiaTipologia.addActionListener(e -> {
            tableModel.setRowCount(0);
            btnEsportaReport.setEnabled(false);
            cardLayout.show(mainContainer, "STEP1_SCELTA_TIPO");
        });
        headerPanel.add(lblTitoloSchermataReport, BorderLayout.WEST);
        headerPanel.add(btnCambiaTipologia, BorderLayout.EAST);

        // Pannello centrale del parametro specifico (Settimana / Mese / ID Cliente)
        pnlParametroSpecifico = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlParametroSpecifico.setBorder(BorderFactory.createTitledBorder("Filtro Specifico per Tipologia"));

        // Inizializzazione controlli specifici
        cmbSettimanaAnno = new JComboBox<>();
        popolaSettimaneAnno2026();
        cmbSettimanaAnno.setSelectedIndex(30); // Preseleziona Settimana 31 (inizio Agosto 2026)

        cmbMeseAnno = new JComboBox<>(new String[]{
                "1 - Gennaio 2026", "2 - Febbraio 2026", "3 - Marzo 2026", "4 - Aprile 2026",
                "5 - Maggio 2026", "6 - Giugno 2026", "7 - Luglio 2026", "8 - Agosto 2026",
                "9 - Settembre 2026", "10 - Ottobre 2026", "11 - Novembre 2026", "12 - Dicembre 2026"
        });
        cmbMeseAnno.setSelectedIndex(7); // Preseleziona Agosto 2026

        txtIdCliente = new JTextField("C01");
        cmbFormatoEsportazione = new JComboBox<>(new String[]{"PDF", "CSV"});

        // Pannello pulsanti azione
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnGeneraAnteprima = new JButton("1. Genera Anteprima Report");
        btnEsportaReport = new JButton("2. Esporta Report (PDF / CSV)");
        btnEsportaReport.setEnabled(false);
        lblRiepilogoStatistiche = new JLabel("Seleziona il parametro e clicca su 'Genera Anteprima Report'.");
        lblRiepilogoStatistiche.setFont(new Font("SansSerif", Font.BOLD, 13));

        buttonPanel.add(btnGeneraAnteprima);
        buttonPanel.add(btnEsportaReport);
        buttonPanel.add(lblRiepilogoStatistiche);

        JPanel northContainer = new JPanel(new BorderLayout(8, 8));
        northContainer.add(headerPanel, BorderLayout.NORTH);
        northContainer.add(pnlParametroSpecifico, BorderLayout.CENTER);
        northContainer.add(buttonPanel, BorderLayout.SOUTH);

        String[] columns = {
                "Codice Prenotazione", "ID Cliente", "Nominativo", "Ombrellone",
                "Coordinate", "Giorni", "Servizi Extra", "Importo", "Stato"
        };
        tableModel = new DefaultTableModel(columns, 0);
        tblDettaglioReport = new JTable(tableModel);

        panel.add(northContainer, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblDettaglioReport), BorderLayout.CENTER);

        initActionListenersStep2();
        return panel;
    }

    /**
     * Adatta dinamicamente la schermata di Step 2 mostrando SOLO il campo richiesto dalla tipologia scelta.
     */
    private void configuraSchermataPerTipologia(String tipo) {
        pnlParametroSpecifico.removeAll();
        lblTitoloSchermataReport.setText("Schermata Report: Tipologia " + tipo.toUpperCase());

        if ("Settimanale".equalsIgnoreCase(tipo)) {
            pnlParametroSpecifico.add(new JLabel("Seleziona Settimana dell'Anno (2026):"));
            pnlParametroSpecifico.add(cmbSettimanaAnno);
        } else if ("Mensile".equalsIgnoreCase(tipo)) {
            pnlParametroSpecifico.add(new JLabel("Seleziona Mese dell'Anno (2026):"));
            pnlParametroSpecifico.add(cmbMeseAnno);
        } else if ("Cliente".equalsIgnoreCase(tipo)) {
            pnlParametroSpecifico.add(new JLabel("Inserisci ID Cliente (es. C01, C02):"));
            pnlParametroSpecifico.add(txtIdCliente);
        }

        pnlParametroSpecifico.add(new JLabel("Formato di Esportazione:"));
        pnlParametroSpecifico.add(cmbFormatoEsportazione);

        lblRiepilogoStatistiche.setText("Premi '1. Genera Anteprima Report' per visualizzare i dati.");
        pnlParametroSpecifico.revalidate();
        pnlParametroSpecifico.repaint();
    }

    private void popolaSettimaneAnno2026() {
        cmbSettimanaAnno.removeAllItems();
        Calendar cal = Calendar.getInstance(Locale.ITALY);
        cal.clear();
        cal.set(Calendar.YEAR, 2026);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        for (int week = 1; week <= 52; week++) {
            Date start = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, 6);
            Date end = cal.getTime();
            cmbSettimanaAnno.addItem(String.format("Settimana %02d (%s - %s)", week, sdf.format(start), sdf.format(end)));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void initActionListenersStep2() {
        btnGeneraAnteprima.addActionListener(e -> {
            try {
                Date inizio = null;
                Date fine = null;
                String idCliente = null;

                if ("Settimanale".equalsIgnoreCase(tipoReportScelto)) {
                    int weekIndex = cmbSettimanaAnno.getSelectedIndex(); // 0-based
                    Calendar cal = Calendar.getInstance(Locale.ITALY);
                    cal.clear();
                    cal.set(Calendar.YEAR, 2026);
                    cal.set(Calendar.MONTH, Calendar.JANUARY);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    cal.add(Calendar.DAY_OF_MONTH, weekIndex * 7);
                    inizio = cal.getTime();
                    cal.add(Calendar.DAY_OF_MONTH, 6);
                    fine = cal.getTime();
                } else if ("Mensile".equalsIgnoreCase(tipoReportScelto)) {
                    int monthIndex = cmbMeseAnno.getSelectedIndex(); // 0 = Gennaio ... 7 = Agosto
                    Calendar cal = Calendar.getInstance(Locale.ITALY);
                    cal.clear();
                    cal.set(Calendar.YEAR, 2026);
                    cal.set(Calendar.MONTH, monthIndex);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    inizio = cal.getTime();
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                    fine = cal.getTime();
                } else if ("Cliente".equalsIgnoreCase(tipoReportScelto)) {
                    idCliente = txtIdCliente.getText().trim();
                    if (idCliente.isEmpty()) {
                        throw new IllegalArgumentException("Inserire l'ID Cliente per generare il Report Cliente.");
                    }
                }

                selezionaParametriReport(tipoReportScelto, inizio, fine, idCliente);
            } catch (Exception ex) {
                mostraMessaggioInformativo(ex.getMessage());
            }
        });

        btnEsportaReport.addActionListener(e -> {
            String formatoScelto = (String) cmbFormatoEsportazione.getSelectedItem();
            richiediEsportazioneReport(formatoScelto);
        });
    }

    public void selezionaParametriReport(String tipoReport, Date dataInizio, Date dataFine, String idCliente) {
        try {
            ReportDTO report = GestioneStabilimentoController.getInstance()
                    .generaReport(tipoReport, dataInizio, dataFine, idCliente);

            if (report == null) {
                btnEsportaReport.setEnabled(false);
                tableModel.setRowCount(0);
                mostraMessaggioInformativo("Nessuna prenotazione trovata per i criteri specificati");
                proponiModificaParametriOAnnulla();
            } else {
                this.datiReportElaboratiCorrente = report;
                mostraAnteprimaReport(report);
            }
        } catch (Exception ex) {
            btnEsportaReport.setEnabled(false);
            tableModel.setRowCount(0);
            mostraMessaggioInformativo(ex.getMessage());
        }
    }

    public void mostraAnteprimaReport(ReportDTO datiReportElaborati) {
        tableModel.setRowCount(0);
        for (PrenotazioneDTO riga : datiReportElaborati.getRigheDettaglio()) {
            tableModel.addRow(new Object[]{
                    riga.getCodicePrenotazione(),
                    riga.getIdCliente(),
                    riga.getNominativoCliente(),
                    riga.getCodiceOmbrellone(),
                    riga.getCoordinateOmbrellone(),
                    riga.getConteggioGiorniSoggiorno(),
                    riga.getTotaleServiziAccessoriAcquistati(),
                    riga.getImportoMonetarioFormattato(),
                    riga.getStatoCorrente()
            });
        }

        lblRiepilogoStatistiche.setText(String.format(Locale.ITALY,
                "Report %s (%s) | Prenotazioni: %d | Incasso Totale: € %.2f | Media: € %.2f | Servizi Extra: %d",
                datiReportElaborati.getIdReport(),
                datiReportElaborati.getTipoReport(),
                datiReportElaborati.getNumeroTotalePrenotazioni(),
                datiReportElaborati.getIncassoTotale(),
                datiReportElaborati.getIncassoMedioPerPrenotazione(),
                datiReportElaborati.getTotaleServiziAccessoriVenduti()));

        btnEsportaReport.setEnabled(true);
    }

    public void richiediEsportazioneReport(String formatoScelto) {
        try {
            File fileGenerato = GestioneStabilimentoController.getInstance()
                    .esportaReport(this.datiReportElaboratiCorrente, formatoScelto);
            avviaDownloadFile(fileGenerato);
        } catch (Exception ex) {
            mostraMessaggioInformativo(ex.getMessage());
        }
    }

    public void avviaDownloadFile(File fileGenerato) {
        JOptionPane.showMessageDialog(
                this,
                "Report esportato e pronto per il download in:\n" + fileGenerato.getAbsolutePath(),
                "Download Completato",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void mostraMessaggioInformativo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Messaggio Informativo", JOptionPane.WARNING_MESSAGE);
    }

    public void proponiModificaParametriOAnnulla() {
        int scelta = JOptionPane.showConfirmDialog(
                this,
                "Vuoi modificare i parametri di filtraggio del report?",
                "Nessun Dato Trovato",
                JOptionPane.YES_NO_OPTION
        );
        if (scelta == JOptionPane.NO_OPTION) {
            lblRiepilogoStatistiche.setText("Operazione annullata.");
            cardLayout.show(mainContainer, "STEP1_SCELTA_TIPO");
        }
    }
}