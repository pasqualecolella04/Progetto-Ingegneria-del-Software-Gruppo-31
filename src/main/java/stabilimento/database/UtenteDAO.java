package stabilimento.database;

import stabilimento.entity.Cliente;
import stabilimento.entity.Gestore;
import stabilimento.entity.Utente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteDAO {

    public boolean createUtente(Utente u, String ruolo) {
        String sql = "INSERT OR REPLACE INTO Utenti (idUtente, nome, cognome, email, telefono, passwordHash, ruolo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, u.getIdUtente());
                ps.setString(2, u.getNome());
                ps.setString(3, u.getCognome());
                ps.setString(4, u.getEmail());
                ps.setString(5, u.getTelefono());
                ps.setString(6, u.getPasswordHash());
                ps.setString(7, ruolo != null ? ruolo : u.getRuolo());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la creazione dell'utente su DB: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public Cliente readClienteById(String idCliente) {
        String sql = "SELECT * FROM Utenti WHERE idUtente = ? AND ruolo = 'Cliente'";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Cliente c = new Cliente(
                                rs.getString("nome"),
                                rs.getString("cognome"),
                                rs.getString("email"),
                                rs.getString("telefono"),
                                rs.getString("passwordHash")
                        );
                        c.setIdUtente(rs.getString("idUtente"));
                        return c;
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la lettura del cliente per ID: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public Utente readUtenteByEmail(String email) {
        String sql = "SELECT * FROM Utenti WHERE email = ?";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String ruolo = rs.getString("ruolo");
                        Utente u;
                        if ("Gestore".equalsIgnoreCase(ruolo)) {
                            u = new Gestore(
                                    rs.getString("nome"),
                                    rs.getString("cognome"),
                                    rs.getString("email"),
                                    rs.getString("telefono"),
                                    rs.getString("passwordHash")
                            );
                        } else {
                            u = new Cliente(
                                    rs.getString("nome"),
                                    rs.getString("cognome"),
                                    rs.getString("email"),
                                    rs.getString("telefono"),
                                    rs.getString("passwordHash")
                            );
                        }
                        u.setIdUtente(rs.getString("idUtente"));
                        return u;
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la lettura dell'utente per email: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }
}