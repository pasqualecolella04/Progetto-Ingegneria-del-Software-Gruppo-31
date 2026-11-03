package stabilimento.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Prenotazione {
    private String idPrenotazione;
    private Date dataInizio;
    private Date dataFine;
    private String oraInizio;
    private String stato;
    private double importoTotale;
    private Cliente cliente;
    private Postazione postazioneScelta;
    private List<ServizioPrenotato> serviziPrenotati;

    public Prenotazione(Cliente cliente, Postazione pos, Date dataInizio, Date dataFine, String stato) {
        if (dataInizio == null || dataFine == null || dataInizio.after(dataFine)) {
            throw new IllegalArgumentException("Intervallo di date inserito non valido");
        }
        this.idPrenotazione = "PREN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.cliente = cliente;
        this.postazioneScelta = pos;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.oraInizio = "09:00";
        this.stato = stato;
        this.serviziPrenotati = new ArrayList<>();
        this.importoTotale = calcolaImportoTotale();
    }

    public int calcolaGiorniSoggiorno() {
        long diffMillis = dataFine.getTime() - dataInizio.getTime();
        return (int) TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS) + 1;
    }

    public void aggiungiServizioPrenotato(ServizioAggiuntivo s, int quantita) {
        if (s == null || !s.verificaDisponibilita(quantita)) {
            throw new IllegalArgumentException("Quantità richiesta non valida o superiore alla disponibilità massima per il servizio.");
        }
        int giorni = calcolaGiorniSoggiorno();
        double subTotale = s.getPrezzoGiornaliero(dataInizio) * quantita * giorni;
        ServizioPrenotato sp = new ServizioPrenotato(s, quantita, subTotale);
        this.serviziPrenotati.add(sp);
        calcolaImportoTotale();
    }

    public double calcolaImportoTotale() {
        double costoPostazione = 0.0;
        if (postazioneScelta != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dataInizio);
            while (!cal.getTime().after(dataFine)) {
                costoPostazione += postazioneScelta.getTariffaGiornaliera(cal.getTime());
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        double costoServizi = 0.0;
        if (serviziPrenotati != null) {
            for (ServizioPrenotato sp : serviziPrenotati) {
                costoServizi += sp.getSubTotale();
            }
        }

        this.importoTotale = costoPostazione + costoServizi;
        return this.importoTotale;
    }

    public double getImportoTotale() {
        return importoTotale;
    }

    public void setImportoTotale(double importoTotale) {
        this.importoTotale = importoTotale;
    }

    public List<ServizioPrenotato> getServiziPrenotati() {
        return serviziPrenotati;
    }

    public void setServiziPrenotati(List<ServizioPrenotato> serviziPrenotati) {
        this.serviziPrenotati = serviziPrenotati;
    }

    public void setStato(String nuovoStato) {
        this.stato = nuovoStato;
    }

    public String getStato() {
        return stato;
    }

    public String getIdPrenotazione() {
        return idPrenotazione;
    }

    public void setIdPrenotazione(String idPrenotazione) {
        this.idPrenotazione = idPrenotazione;
    }

    public Date getDataInizio() {
        return dataInizio;
    }

    public Date getDataFine() {
        return dataFine;
    }

    public String getOraInizio() {
        return oraInizio;
    }

    public void setOraInizio(String oraInizio) {
        this.oraInizio = oraInizio;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Postazione getPostazioneScelta() {
        return postazioneScelta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prenotazione)) return false;
        Prenotazione that = (Prenotazione) o;
        return Objects.equals(idPrenotazione, that.idPrenotazione);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPrenotazione);
    }
}