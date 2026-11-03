package stabilimento.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Postazione {
    private String idPostazione;
    private int numeroProgressivo;
    private int fila;
    private String posizione;
    private boolean occupata;
    private List<Tariffa> listinoTariffe;

    public Postazione(String idPostazione, int numero, int fila, String posizione) {
        this.idPostazione = idPostazione;
        this.numeroProgressivo = numero;
        this.fila = fila;
        this.posizione = posizione;
        this.occupata = false;
        this.listinoTariffe = new ArrayList<>();
    }

    public String getIdPostazione() {
        return idPostazione;
    }

    public double getTariffaGiornaliera(Date data) {
        if (listinoTariffe != null && data != null) {
            for (Tariffa t : listinoTariffe) {
                if (t.isValidaInData(data)) {
                    return t.getPrezzoGiornaliero();
                }
            }
        }
        return switch (fila) {
            case 1 -> 30.0;
            case 2 -> 25.0;
            default -> 20.0;
        };
    }

    public void setOccupata(boolean stato) {
        this.occupata = stato;
    }

    public boolean isOccupata() {
        return occupata;
    }

    public int getNumeroProgressivo() {
        return numeroProgressivo;
    }

    public int getFila() {
        return fila;
    }

    public String getPosizione() {
        return posizione;
    }

    public List<Tariffa> getListinoTariffe() {
        return listinoTariffe;
    }

    public void setListinoTariffe(List<Tariffa> listinoTariffe) {
        this.listinoTariffe = listinoTariffe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Postazione)) return false;
        Postazione that = (Postazione) o;
        return Objects.equals(idPostazione, that.idPostazione);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPostazione);
    }

    @Override
    public String toString() {
        return "Fila " + fila + " - Ombrellone N. " + numeroProgressivo + " (" + posizione + ")";
    }
}