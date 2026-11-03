package stabilimento.entity;

import java.util.Objects;
import java.util.UUID;

public abstract class Utente {
    protected String idUtente;
    protected String nome;
    protected String cognome;
    protected String email;
    protected String telefono;
    protected String passwordHash;
    protected String ruolo;

    public Utente(String nome, String cognome, String email, String tel, String pwd, String ruolo) {
        this.idUtente = "U-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.telefono = tel;
        this.passwordHash = pwd;
        this.ruolo = ruolo;
    }

    public String getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(String idUtente) {
        this.idUtente = idUtente;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRuolo() {
        return ruolo;
    }

    public boolean verificaCredenziali(String pwd) {
        return this.passwordHash != null && this.passwordHash.equals(pwd);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utente)) return false;
        Utente utente = (Utente) o;
        return Objects.equals(idUtente, utente.idUtente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUtente);
    }
}