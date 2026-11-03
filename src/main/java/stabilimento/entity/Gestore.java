package stabilimento.entity;

public class Gestore extends Utente {

    public Gestore(String nome, String cognome, String email, String tel, String pwd) {
        super(nome, cognome, email, tel, pwd, "Gestore");
    }
}