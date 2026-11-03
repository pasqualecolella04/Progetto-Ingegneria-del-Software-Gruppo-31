package stabilimento.boundary;

import stabilimento.control.GestioneStabilimentoController;
import stabilimento.dto.PrenotazioneDTO;
import stabilimento.dto.ReportDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BoundaryGestore extends JFrame {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private JComboBox<String> cmbTipoReport;
    private JTextField txtDataInizio;
    private JTextField txtDataFine;
    private JTextField txtIdCliente;
    private JComboBox<String> cmbFormatoEsportazione;
    private JButton btnGeneraAnteprima;
    private JButton btnEsportaReport;
    private JLabel lblRiepilogoStatistiche;
    private JTable tblDettaglioReport;
    private DefaultTableModel tableModel;

    private ReportDTO datiReportElaboratiCorrente;

    public BoundaryGestore() {
        sdf.setLenient(false);
        setTitle("Progetto Ing.S Gruppo 31 - BoundaryGestore (UC12: Genera Report)");
        setSize(940, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        mostraFormParametriReport();
    }

    public void accediSezioneReportistica() {
        mostraFormParametriReport();
        setVisible(true);
    }

    public void mostraFormParametriReport() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel filterPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Seleziona Parametri Report"));

        cmbTipoReport = new JComboBox<>(new String[]{"Settimanale", "Mensile", "Cliente"});
        txtDataInizio = new JTextField("2026-08-01");
        txtDataFine = new JTextField("2026-08-07");
        txtIdCliente = new JTextField("");
        cmbFormatoEsportazione = new JComboBox<>(new String[]{"PDF", "CSV"});

        filterPanel.add(new JLabel("Tipologia Report (*):"));
        filterPanel.add(cmbTipoReport);
        filterPanel.add(new JLabel("Data Inizio Periodo (YYYY-MM-DD):"));
        filterPanel.add(txtDataInizio);
        filterPanel.add(new JLabel("Data Fine Periodo (YYYY-MM-DD):"));
        filterPanel.add(txtDataFine);
        filterPanel.add(new JLabel("ID Cliente (se Tipo = Cliente):"));
        filterPanel.add(txtIdCliente);
        filterPanel.add(new JLabel("Formato di Esportazione:"));
        filterPanel.add(cmbFormatoEsportazione);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnGeneraAnteprima = new JButton("1. Genera Anteprima Report");
        btnEsportaReport = new JButton("2. Esporta Report (PDF / CSV)");
        btnEsportaReport.setEnabled(false);
        lblRiepilogoStatistiche = new JLabel("Imposta i filtri e clicca su 'Genera Anteprima Report'.");
        lblRiepilogoStatistiche.setFont(new Font("SansSerif", Font.BOLD, 13));

        buttonPanel.add(btnGeneraAnteprima);
        buttonPanel.add(btnEsportaReport);
        buttonPanel.add(lblRiepilogoStatistiche);

        JPanel northContainer = new JPanel(new BorderLayout(5, 5));
        northContainer.add(filterPanel, BorderLayout.CENTER);
        northContainer.add(buttonPanel, BorderLayout.SOUTH);

        String[] columns = {
                "Codice Prenotazione", "ID Cliente", "Nominativo", "Ombrellone",
                "Coordinate", "Giorni", "Servizi Extra", "Importo", "Stato"
        };
        tableModel = new DefaultTableModel(columns, 0);
        tblDettaglioReport = new JTable(tableModel);

        mainPanel.add(northContainer, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(tblDettaglioReport), BorderLayout.CENTER);
        setContentPane(mainPanel);

        initActionListeners();
    }

    private void initActionListeners() {
        btnGeneraAnteprima.addActionListener(e -> {
            try {
                String tipoReport = (String) cmbTipoReport.getSelectedItem();
                Date inizio = null;
                Date fine = null;
                String idCliente = txtIdCliente.getText().trim();

                if ("Settimanale".equalsIgnoreCase(tipoReport) || "Mensile".equalsIgnoreCase(tipoReport)) {
                    inizio = validaSintassiData(txtDataInizio.getText(), "Data Inizio");
                    fine = validaSintassiData(txtDataFine.getText(), "Data Fine");
                } else if ("Cliente".equalsIgnoreCase(tipoReport) && idCliente.isEmpty()) {
                    throw new IllegalArgumentException("Inserire l'ID Cliente per generare il Report Cliente.");
                }

                selezionaParametriReport(tipoReport, inizio, fine, idCliente);
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
            txtIdCliente.setText("");
            lblRiepilogoStatistiche.setText("Operazione annullata.");
        }
    }

    private Date validaSintassiData(String input, String nomeCampo) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Il campo '" + nomeCampo + "' non può essere vuoto.");
        }
        try {
            return sdf.parse(input.trim());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Formato data non valido per '" + nomeCampo + "'. Usa YYYY-MM-DD.");
        }
    }
}