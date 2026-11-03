package stabilimento.boundary;

import stabilimento.control.GestionePrenotazioniController;
import stabilimento.entity.Postazione;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class BoundaryCliente extends JFrame {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private JTextField txtIdCliente;
    private JTextField txtDataInizio;
    private JTextField txtDataFine;
    private JTextField txtOra;
    private JComboBox<Postazione> cmbPostazioni;
    private JTextField txtQtaLettini;
    private JTextField txtQtaCabina;
    private JLabel lblRiepilogoImporto;
    private JButton btnCerca;
    private JButton btnSelezionaPostazione;
    private JButton btnAggiungiServizi;
    private JButton btnSenzaServizi;
    private JButton btnConferma;
    private JButton btnAnnulla;
    private JTable tblRiepilogo;
    private DefaultTableModel tableModel;

    private Date dataInizioCorrente;
    private Date dataFineCorrente;
    private String idPostazioneScelta;
    private double importoBaseCorrente;
    private double importoTotaleCorrente;
    private Map<String, Integer> mappaServiziScelti = new HashMap<>();

    public BoundaryCliente() {
        sdf.setLenient(false);
        setTitle("Progetto Ing.S Gruppo 31 - BoundaryCliente (UC1: Prenotazione)");
        setSize(920, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        mostraFormRicerca();
    }

    public void visualizzaSezionePrenotazione() {
        mostraFormRicerca();
        setVisible(true);
    }

    public void mostraFormRicerca() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Ricerca Disponibilità e Scelta Servizi"));

        txtIdCliente = new JTextField("C102");
        txtDataInizio = new JTextField("2026-08-01");
        txtDataFine = new JTextField("2026-08-03");
        txtOra = new JTextField("09:00");
        cmbPostazioni = new JComboBox<>();
        txtQtaLettini = new JTextField("0");
        txtQtaCabina = new JTextField("0");

        formPanel.add(new JLabel("ID Cliente Autenticato (*):"));
        formPanel.add(txtIdCliente);
        formPanel.add(new JLabel("Data Inizio (YYYY-MM-DD) (*):"));
        formPanel.add(txtDataInizio);
        formPanel.add(new JLabel("Data Fine (YYYY-MM-DD) (*):"));
        formPanel.add(txtDataFine);
        formPanel.add(new JLabel("Ora Inizio (HH:MM) (*):"));
        formPanel.add(txtOra);
        formPanel.add(new JLabel("Griglia Postazioni Libere:"));
        formPanel.add(cmbPostazioni);
        formPanel.add(new JLabel("Quantità Lettino Extra (ID: S01):"));
        formPanel.add(txtQtaLettini);
        formPanel.add(new JLabel("Quantità Cabina Privata (ID: S02):"));
        formPanel.add(txtQtaCabina);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        btnCerca = new JButton("1. Cerca Disponibilità");
        btnSelezionaPostazione = new JButton("2. Seleziona Postazione");
        btnAggiungiServizi = new JButton("3a. Aggiungi Servizi");
        btnSenzaServizi = new JButton("3b. Prosegui Senza Servizi");
        btnConferma = new JButton("4. Conferma Prenotazione");
        btnAnnulla = new JButton("Annulla Richiesta");
        lblRiepilogoImporto = new JLabel("Importo: € 0,00");
        lblRiepilogoImporto.setFont(new Font("SansSerif", Font.BOLD, 13));

        actionsPanel.add(btnCerca);
        actionsPanel.add(btnSelezionaPostazione);
        actionsPanel.add(btnAggiungiServizi);
        actionsPanel.add(btnSenzaServizi);
        actionsPanel.add(btnConferma);
        actionsPanel.add(btnAnnulla);
        actionsPanel.add(lblRiepilogoImporto);

        JPanel northContainer = new JPanel(new BorderLayout(5, 5));
        northContainer.add(formPanel, BorderLayout.CENTER);
        northContainer.add(actionsPanel, BorderLayout.SOUTH);

        String[] columns = {"ID Cliente", "Ombrellone", "Data Inizio", "Data Fine", "Servizi Extra", "Importo Complessivo", "Stato"};
        tableModel = new DefaultTableModel(columns, 0);
        tblRiepilogo = new JTable(tableModel);

        mainPanel.add(northContainer, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(tblRiepilogo), BorderLayout.CENTER);
        setContentPane(mainPanel);

        initActionListeners();
    }

    private void initActionListeners() {
        btnCerca.addActionListener(e -> {
            try {
                Date inizio = validaSintassiData(txtDataInizio.getText(), "Data Inizio");
                Date fine = validaSintassiData(txtDataFine.getText(), "Data Fine");
                String ora = txtOra.getText().trim();
                if (ora.isEmpty()) {
                    throw new IllegalArgumentException("Inserire l'orario di inizio.");
                }
                inserisciParametriRicerca(inizio, fine, ora);
            } catch (Exception ex) {
                mostraMessaggioErrore(ex.getMessage());
            }
        });

        btnSelezionaPostazione.addActionListener(e -> {
            Postazione selezionata = (Postazione) cmbPostazioni.getSelectedItem();
            if (selezionata == null) {
                mostraMessaggioErrore("Selezionare prima una postazione dalla griglia.");
                return;
            }
            selezionaPostazione(selezionata.getIdPostazione());
        });

        btnAggiungiServizi.addActionListener(e -> {
            try {
                int qLettini = Integer.parseInt(txtQtaLettini.getText().trim());
                int qCabina = Integer.parseInt(txtQtaCabina.getText().trim());
                if (qLettini < 0 || qCabina < 0) {
                    throw new IllegalArgumentException("Le quantità dei servizi non possono essere negative.");
                }
                List<String> ids = new ArrayList<>();
                List<Integer> qtas = new ArrayList<>();
                mappaServiziScelti.clear();

                if (qLettini > 0) {
                    ids.add("S01");
                    qtas.add(qLettini);
                    mappaServiziScelti.put("S01", qLettini);
                }
                if (qCabina > 0) {
                    ids.add("S02");
                    qtas.add(qCabina);
                    mappaServiziScelti.put("S02", qCabina);
                }
                selezionaServiziAggiuntivi(ids, qtas);
            } catch (NumberFormatException ex) {
                mostraMessaggioErrore("Inserire un numero intero valido per le quantità dei servizi.");
            } catch (Exception ex) {
                mostraMessaggioErrore(ex.getMessage());
            }
        });

        btnSenzaServizi.addActionListener(e -> proseguiSenzaServizi());

        btnConferma.addActionListener(e -> richiediConfermaFinale());

        btnAnnulla.addActionListener(e -> annullaRichiesta());
    }

    public void inserisciParametriRicerca(Date dataInizio, Date dataFine, String ora) {
        try {
            this.dataInizioCorrente = dataInizio;
            this.dataFineCorrente = dataFine;
            List<Postazione> postazioniLibere = GestionePrenotazioniController.getInstance()
                    .visualizzaPostazioniDisponibili(dataInizio, dataFine);

            if (postazioniLibere == null || postazioniLibere.isEmpty()) {
                mostraMessaggioErrore("Nessuna postazione disponibile per il periodo richiesto");
                proponiModificaOAnnulla();
            } else {
                mostraGrigliaPostazioni(postazioniLibere);
            }
        } catch (Exception ex) {
            mostraMessaggioErrore(ex.getMessage());
        }
    }

    public void mostraGrigliaPostazioni(List<Postazione> postazioniLibere) {
        cmbPostazioni.removeAllItems();
        for (Postazione pos : postazioniLibere) {
            cmbPostazioni.addItem(pos);
        }
    }

    public void selezionaPostazione(String idPostazione) {
        try {
            this.idPostazioneScelta = idPostazione;
            this.importoBaseCorrente = GestionePrenotazioniController.getInstance()
                    .calcolaImportoParziale(idPostazione, dataInizioCorrente, dataFineCorrente);
            this.importoTotaleCorrente = this.importoBaseCorrente;
            mostraImportoParziale(this.importoBaseCorrente);
        } catch (Exception ex) {
            mostraMessaggioErrore(ex.getMessage());
        }
    }

    public void mostraImportoParziale(double importoBase) {
        lblRiepilogoImporto.setText(String.format(Locale.ITALY, "Importo Parziale: € %.2f", importoBase));
    }

    public void selezionaServiziAggiuntivi(List<String> listaIdServizi, List<Integer> quantita) {
        try {
            this.importoTotaleCorrente = GestionePrenotazioniController.getInstance()
                    .aggiornaImportoConServizi(this.importoBaseCorrente, listaIdServizi, quantita);
            mostraRiepilogoAggiornato(this.importoTotaleCorrente);
        } catch (Exception ex) {
            mostraMessaggioErrore(ex.getMessage());
        }
    }

    public void proseguiSenzaServizi() {
        this.mappaServiziScelti.clear();
        this.txtQtaLettini.setText("0");
        this.txtQtaCabina.setText("0");
        this.importoTotaleCorrente = this.importoBaseCorrente;
        mostraImportoParziale(this.importoBaseCorrente);
    }

    public void mostraRiepilogoAggiornato(double importoTotaleAggiornato) {
        lblRiepilogoImporto.setText(String.format(Locale.ITALY, "Totale Aggiornato: € %.2f", importoTotaleAggiornato));
    }

    public void richiediConfermaFinale() {
        if (idPostazioneScelta == null || dataInizioCorrente == null || dataFineCorrente == null) {
            mostraMessaggioErrore("Selezionare prima un periodo valido e una postazione disponibile.");
            return;
        }
        int risposta = JOptionPane.showConfirmDialog(
                this,
                String.format(Locale.ITALY, "Confermare la prenotazione della postazione %s per un totale di € %.2f?",
                        idPostazioneScelta, importoTotaleCorrente),
                "Richiesta Conferma Finale",
                JOptionPane.YES_NO_OPTION
        );
        if (risposta == JOptionPane.YES_OPTION) {
            confermaPrenotazione();
        } else {
            annullaRichiesta();
        }
    }

    public void confermaPrenotazione() {
        try {
            String idCliente = txtIdCliente.getText().trim();
            if (idCliente.isEmpty()) {
                mostraMessaggioErrore("Il campo ID Cliente è obbligatorio.");
                return;
            }

            boolean esitoCompletaConNotifica = GestionePrenotazioniController.getInstance()
                    .confermaERegistraPrenotazione(idCliente, idPostazioneScelta, mappaServiziScelti, dataInizioCorrente, dataFineCorrente);

            int totServizi = mappaServiziScelti.values().stream().mapToInt(Integer::intValue).sum();
            tableModel.addRow(new Object[]{
                    idCliente,
                    idPostazioneScelta,
                    sdf.format(dataInizioCorrente),
                    sdf.format(dataFineCorrente),
                    totServizi,
                    String.format(Locale.ITALY, "€ %.2f", importoTotaleCorrente),
                    "In Attesa di Conferma"
            });

            if (esitoCompletaConNotifica) {
                mostraMessaggioSuccesso("Prenotazione registrata con successo");
            } else {
                mostraMessaggioInformativo("Prenotazione salvata con successo, ma non è stato possibile inviare l'e-mail/SMS di conferma");
            }
        } catch (Exception ex) {
            mostraMessaggioErrore(ex.getMessage());
        }
    }

    public void annullaRichiesta() {
        richiediConfermaAnnullamento();
    }

    public void richiediConfermaAnnullamento() {
        int scelta = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler annullare la procedura di prenotazione in corso?",
                "Conferma Annullamento",
                JOptionPane.YES_NO_OPTION
        );
        if (scelta == JOptionPane.YES_OPTION) {
            confermaAnnullamento();
        }
    }

    public void confermaAnnullamento() {
        GestionePrenotazioniController.getInstance().pulisciSessioneTemporanea();
        riportaSchermataPrincipale();
    }

    public void annullaProcedura() {
        GestionePrenotazioniController.getInstance().pulisciSessioneTemporanea();
        riportaSchermataPrincipale();
    }

    public void proponiModificaOAnnulla() {
        int opzione = JOptionPane.showConfirmDialog(
                this,
                "Vuoi modificare le date di ricerca? (Premi 'No' per annullare la procedura)",
                "Nessuna Disponibilità",
                JOptionPane.YES_NO_OPTION
        );
        if (opzione == JOptionPane.NO_OPTION) {
            annullaProcedura();
        }
    }

    public void riportaSchermataPrincipale() {
        cmbPostazioni.removeAllItems();
        txtQtaLettini.setText("0");
        txtQtaCabina.setText("0");
        mappaServiziScelti.clear();
        idPostazioneScelta = null;
        importoBaseCorrente = 0.0;
        importoTotaleCorrente = 0.0;
        lblRiepilogoImporto.setText("Importo: € 0,00");
    }

    public void mostraMessaggioSuccesso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Conferma", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostraMessaggioInformativo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Informazione", JOptionPane.WARNING_MESSAGE);
    }

    public void mostraMessaggioErrore(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    private Date validaSintassiData(String input, String nomeCampo) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Il campo '" + nomeCampo + "' è obbligatorio.");
        }
        try {
            return sdf.parse(input.trim());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Formato data non valido per '" + nomeCampo + "'. Usa YYYY-MM-DD.");
        }
    }
}