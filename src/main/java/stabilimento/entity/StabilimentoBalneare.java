package stabilimento.entity;

import stabilimento.database.PostazioneDAO;
import stabilimento.database.PrenotazioneDAO;
import stabilimento.dto.PrenotazioneDTO;
import stabilimento.dto.ReportDTO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class StabilimentoBalneare {
    private String idStabilimento;
    private String nome;
    private String indirizzo;
    private List<Postazione> postazioni;
    private List<ServizioAggiuntivo> catalogoServizi;

    public StabilimentoBalneare(String idStabilimento, String nome, String indirizzo) {
        this.idStabilimento = idStabilimento;
        this.nome = nome;
        this.indirizzo = indirizzo;
        this.postazioni = new ArrayList<>();
        this.catalogoServizi = new ArrayList<>();
    }


    public List<Postazione> getPostazioniDisponibili(Date dataInizio, Date dataFine) {
        PostazioneDAO posDAO = new PostazioneDAO();
        return posDAO.readPostazioniLibere(dataInizio, dataFine);
    }


    public ReportDTO estraiEdElaboraDatiReport(String tipoReport, Date dataInizio, Date dataFine, String idCliente) {
        PrenotazioneDAO prenDAO = new PrenotazioneDAO();
        List<Prenotazione> listaPrenotazioni;

        if ("Settimanale".equalsIgnoreCase(tipoReport) || "Mensile".equalsIgnoreCase(tipoReport)) {
            listaPrenotazioni = prenDAO.readPrenotazioniByPeriodo(dataInizio, dataFine);
        } else if ("Cliente".equalsIgnoreCase(tipoReport)) {
            listaPrenotazioni = prenDAO.readPrenotazioneByCliente(idCliente);
        } else {
            throw new IllegalArgumentException("Selezionare una tipologia di report valida");
        }

        if (listaPrenotazioni == null || listaPrenotazioni.isEmpty()) {
            return null;
        }

        ReportDTO baseReport = calcolaTotaliEIndiciAffluenza(listaPrenotazioni);
        return new ReportDTO(
                baseReport.getIdReport(),
                tipoReport,
                dataInizio,
                dataFine,
                idCliente,
                baseReport.getNumeroTotalePrenotazioni(),
                baseReport.getIncassoTotale(),
                baseReport.getIncassoMedioPerPrenotazione(),
                baseReport.getTotaleServiziAccessoriVenduti(),
                baseReport.getRigheDettaglio()
        );
    }


    public ReportDTO calcolaTotaliEIndiciAffluenza(List<Prenotazione> listaPrenotazioni) {
        int numeroTotalePrenotazioni = 0;
        double incassoTotale = 0.0;
        int totaleServiziVenduti = 0;
        List<PrenotazioneDTO> righeDettaglio = new ArrayList<>();

        if (listaPrenotazioni != null && !listaPrenotazioni.isEmpty()) {
            for (Prenotazione p : listaPrenotazioni) {
                if (!"Annullata".equalsIgnoreCase(p.getStato())) {
                    numeroTotalePrenotazioni++;
                    double importoPrenotazione = p.getImportoTotale();
                    incassoTotale += importoPrenotazione;

                    int serviziPrenotazioneCorrente = 0;
                    for (ServizioPrenotato sp : p.getServiziPrenotati()) {
                        serviziPrenotazioneCorrente += sp.getQuantitaRichiesta();
                    }
                    totaleServiziVenduti += serviziPrenotazioneCorrente;

                    String idCli = p.getCliente() != null ? p.getCliente().getIdUtente() : "N/D";
                    String nomCli = p.getCliente() != null
                            ? p.getCliente().getNome() + " " + p.getCliente().getCognome()
                            : "N/D";
                    String codOmbr = p.getPostazioneScelta() != null ? p.getPostazioneScelta().getIdPostazione() : "N/D";
                    String coordOmbr = p.getPostazioneScelta() != null ? p.getPostazioneScelta().toString() : "N/D";

                    righeDettaglio.add(new PrenotazioneDTO(
                            p.getIdPrenotazione(),
                            idCli,
                            nomCli,
                            codOmbr,
                            coordOmbr,
                            p.getDataInizio(),
                            p.getDataFine(),
                            p.calcolaGiorniSoggiorno(),
                            serviziPrenotazioneCorrente,
                            importoPrenotazione,
                            String.format(Locale.ITALY, "€ %.2f", importoPrenotazione),
                            p.getStato()
                    ));
                }
            }
        }

        double incassoMedio = (numeroTotalePrenotazioni > 0)
                ? (incassoTotale / numeroTotalePrenotazioni)
                : 0.0;

        String idReport = "REP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return new ReportDTO(
                idReport,
                "Generale",
                null,
                null,
                null,
                numeroTotalePrenotazioni,
                incassoTotale,
                incassoMedio,
                totaleServiziVenduti,
                righeDettaglio
        );
    }


    public File generaFileEsportazione(ReportDTO datiReportElaborati, String formatoScelto) {
        if (datiReportElaborati == null) {
            throw new IllegalArgumentException("Dati report non validi.");
        }
        if (formatoScelto == null || (!formatoScelto.equalsIgnoreCase("PDF") && !formatoScelto.equalsIgnoreCase("CSV"))) {
            throw new IllegalArgumentException("Formato di esportazione richiesto non supportato (formati ammessi: PDF, CSV)");
        }

        String estensione = formatoScelto.toLowerCase();
        File fileGenerato = new File("report_" + datiReportElaborati.getIdReport() + "." + estensione);

        try (FileWriter writer = new FileWriter(fileGenerato)) {
            writer.write("ID Report;Tipologia;Totale Prenotazioni;Incasso Totale (EUR);Incasso Medio (EUR);Servizi Venduti\n");
            writer.write(String.format(Locale.US, "%s;%s;%d;%.2f;%.2f;%d\n\n",
                    datiReportElaborati.getIdReport(),
                    datiReportElaborati.getTipoReport(),
                    datiReportElaborati.getNumeroTotalePrenotazioni(),
                    datiReportElaborati.getIncassoTotale(),
                    datiReportElaborati.getIncassoMedioPerPrenotazione(),
                    datiReportElaborati.getTotaleServiziAccessoriVenduti()));

            writer.write("Codice Prenotazione;ID Cliente;Nominativo;Ombrellone;Coordinate;Giorni;Servizi Extra;Importo;Stato\n");
            for (PrenotazioneDTO riga : datiReportElaborati.getRigheDettaglio()) {
                writer.write(String.format("%s;%s;%s;%s;%s;%d;%d;%s;%s\n",
                        riga.getCodicePrenotazione(),
                        riga.getIdCliente(),
                        riga.getNominativoCliente(),
                        riga.getCodiceOmbrellone(),
                        riga.getCoordinateOmbrellone(),
                        riga.getConteggioGiorniSoggiorno(),
                        riga.getTotaleServiziAccessoriAcquistati(),
                        riga.getImportoMonetarioFormattato(),
                        riga.getStatoCorrente()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore durante l'esportazione del file: " + e.getMessage(), e);
        }

        return fileGenerato;
    }

    public boolean aggiungiPostazione(Postazione pos) {
        if (pos == null || this.postazioni.contains(pos)) {
            return false;
        }
        this.postazioni.add(pos);
        return new PostazioneDAO().createPostazione(pos);
    }

    public boolean rimuoviPostazione(String idPostazione) {
        this.postazioni.removeIf(p -> p.getIdPostazione().equals(idPostazione));
        return new PostazioneDAO().deletePostazione(idPostazione);
    }

    public String getIdStabilimento() {
        return idStabilimento;
    }

    public String getNome() {
        return nome;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public List<Postazione> getPostazioni() {
        return postazioni;
    }

    public List<ServizioAggiuntivo> getCatalogoServizi() {
        return catalogoServizi;
    }
}