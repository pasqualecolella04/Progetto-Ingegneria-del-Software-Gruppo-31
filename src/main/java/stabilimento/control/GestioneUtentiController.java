package stabilimento.control;

import stabilimento.database.UtenteDAO;
import stabilimento.entity.Cliente;
import stabilimento.entity.Gestore;
import stabilimento.entity.Utente;

public class GestioneUtentiController {
    private static GestioneUtentiController instance;

    private GestioneUtentiController() {
    }

    public static synchronized GestioneUtentiController getInstance() {
        if (instance == null) {
            instance = new GestioneUtentiController();
        }
        return instance;
    }

    public boolean registraUtente(String nome, String cognome, String email, String tel, String pwd, String ruolo) {
        if (nome == null || nome.isBlank() || cognome == null || cognome.isBlank()
                || email == null || !email.contains("@") || pwd == null || pwd.isBlank()) {
            return false;
        }
        UtenteDAO utenteDAO = new UtenteDAO();
        if (utenteDAO.readUtenteByEmail(email) != null) {
            return false;
        }

        Utente nuovoUtente;
        if ("Gestore".equalsIgnoreCase(ruolo)) {
            nuovoUtente = new Gestore(nome, cognome, email, tel, pwd);
        } else {
            nuovoUtente = new Cliente(nome, cognome, email, tel, pwd);
        }
        return utenteDAO.createUtente(nuovoUtente, nuovoUtente.getRuolo());
    }

    public Utente autenticaUtente(String email, String pwd) {
        if (email == null || pwd == null) {
            return null;
        }
        UtenteDAO utenteDAO = new UtenteDAO();
        Utente u = utenteDAO.readUtenteByEmail(email);
        if (u != null && u.verificaCredenziali(pwd)) {
            return u;
        }
        return null;
    }

    public void logout(String idUtente) {
        if (idUtente != null) {
            GestionePrenotazioniController.getInstance().pulisciSessioneTemporanea();
        }
    }
}