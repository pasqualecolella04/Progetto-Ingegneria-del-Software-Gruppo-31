package stabilimento.entity;

import java.util.ArrayList;
import java.util.List;

public class Cliente extends Utente {
    private List<Prenotazione> storicoPrenotazioni;

    public Cliente(String nome, String cognome, String email, String tel, String pwd) {
        super(nome, cognome, email, tel, pwd, "Cliente");
        this.storicoPrenotazioni = new ArrayList<>();
    }

    public List<Prenotazione> getStoricoPrenotazioni() {
        return storicoPrenotazioni;
    }

    public void aggiungiPrenotazioneStorico(Prenotazione p) {
        if (p != null && !this.storicoPrenotazioni.contains(p)) {
            this.storicoPrenotazioni.add(p);
        }
    }
}