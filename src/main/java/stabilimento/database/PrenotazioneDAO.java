package stabilimento.database;

import stabilimento.entity.Cliente;
import stabilimento.entity.Postazione;
import stabilimento.entity.Prenotazione;
import stabilimento.entity.ServizioAggiuntivo;
import stabilimento.entity.ServizioPrenotato;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrenotazioneDAO {
    private final UtenteDAO utenteDAO = new UtenteDAO();
    private final PostazioneDAO postazioneDAO = new PostazioneDAO();
    private final ServizioAggiuntivoDAO servizioDAO = new ServizioAggiuntivoDAO();

    public boolean createPrenotazione(Prenotazione p) {
        String sql = """
            INSERT INTO Prenotazione
            (idPrenotazione, dataInizio, dataFine, oraInizio, stato, importoTotale, Utenti_idUtente, Postazione_idPostazione)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, p.getIdPrenotazione());
                ps.setLong(2, p.getDataInizio().getTime());
                ps.setLong(3, p.getDataFine().getTime());
                ps.setString(4, p.getOraInizio());
                ps.setString(5, p.getStato());
                ps.setDouble(6, p.getImportoTotale());
                ps.setString(7, p.getCliente().getIdUtente());
                ps.setString(8, p.getPostazioneScelta().getIdPostazione());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio della prenotazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public Prenotazione readPrenotazioneById(String idPrenotazione) {
        String sql = "SELECT * FROM Prenotazione WHERE idPrenotazione = ?";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idPrenotazione);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRowToPrenotazione(conn, rs);
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la lettura della prenotazione per ID: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public List<Prenotazione> readPrenotazioniByPeriodo(Date inizio, Date fine) {
        String sql = "SELECT * FROM Prenotazione WHERE dataInizio <= ? AND dataFine >= ? ORDER BY dataInizio ASC";
        List<Prenotazione> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, fine.getTime());
                ps.setLong(2, inizio.getTime());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapRowToPrenotazione(conn, rs));
                    }
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la lettura delle prenotazioni per periodo: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public List<Prenotazione> readPrenotazioneByCliente(String idCliente) {
        String sql = "SELECT * FROM Prenotazione WHERE Utenti_idUtente = ? ORDER BY dataInizio ASC";
        List<Prenotazione> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapRowToPrenotazione(conn, rs));
                    }
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la lettura delle prenotazioni per cliente: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public boolean updatePrenotazione(Prenotazione p) {
        String sql = """
            UPDATE Prenotazione
            SET dataInizio = ?, dataFine = ?, oraInizio = ?, stato = ?, importoTotale = ?, Postazione_idPostazione = ?
            WHERE idPrenotazione = ?
            """;
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, p.getDataInizio().getTime());
                ps.setLong(2, p.getDataFine().getTime());
                ps.setString(3, p.getOraInizio());
                ps.setString(4, p.getStato());
                ps.setDouble(5, p.getImportoTotale());
                ps.setString(6, p.getPostazioneScelta().getIdPostazione());
                ps.setString(7, p.getIdPrenotazione());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento della prenotazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public boolean updateStatoPrenotazione(String idPrenotazione, String stato) {
        String sql = "UPDATE Prenotazione SET stato = ? WHERE idPrenotazione = ?";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, stato);
                ps.setString(2, idPrenotazione);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento dello stato della prenotazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public boolean checkConflittoPostazione(String idPostazione, Date inizio, Date fine) {
        String sql = """
            SELECT COUNT(*) FROM Prenotazione
            WHERE Postazione_idPostazione = ?
              AND stato != 'Annullata'
              AND dataInizio <= ?
              AND dataFine >= ?
            """;
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idPostazione);
                ps.setLong(2, fine.getTime());
                ps.setLong(3, inizio.getTime());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il controllo conflitti sulla postazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    private Prenotazione mapRowToPrenotazione(Connection conn, ResultSet rs) throws SQLException {
        String idPrenotazione = rs.getString("idPrenotazione");
        Date dataInizio = new Date(rs.getLong("dataInizio"));
        Date dataFine = new Date(rs.getLong("dataFine"));
        String stato = rs.getString("stato");
        String idCliente = rs.getString("Utenti_idUtente");
        String idPostazione = rs.getString("Postazione_idPostazione");

        Cliente cliente = utenteDAO.readClienteById(idCliente);
        Postazione postazione = postazioneDAO.readAllPostazioni().stream()
                .filter(pos -> pos.getIdPostazione().equals(idPostazione))
                .findFirst()
                .orElse(new Postazione(idPostazione, 1, 1, "Standard"));

        Prenotazione p = new Prenotazione(cliente, postazione, dataInizio, dataFine, stato);
        p.setIdPrenotazione(idPrenotazione);
        p.setOraInizio(rs.getString("oraInizio"));

        String sqlServizi = "SELECT * FROM ServizioPrenotato WHERE Prenotazione_idPrenotazione = ?";
        List<ServizioPrenotato> vociServizi = new ArrayList<>();
        try (PreparedStatement psServ = conn.prepareStatement(sqlServizi)) {
            psServ.setString(1, idPrenotazione);
            try (ResultSet rsServ = psServ.executeQuery()) {
                while (rsServ.next()) {
                    ServizioAggiuntivo sa = servizioDAO.readServizioById(rsServ.getString("ServizioAggiuntivo_idServizio"));
                    ServizioPrenotato sp = new ServizioPrenotato(
                            sa,
                            rsServ.getInt("quantitaRichiesta"),
                            rsServ.getDouble("subTotale")
                    );
                    vociServizi.add(sp);
                }
            }
        }
        p.setServiziPrenotati(vociServizi);
        p.setImportoTotale(rs.getDouble("importoTotale"));
        return p;
    }
}