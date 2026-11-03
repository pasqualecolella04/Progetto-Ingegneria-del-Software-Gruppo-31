package stabilimento.database;

import stabilimento.entity.ServizioAggiuntivo;
import stabilimento.entity.ServizioPrenotato;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServizioAggiuntivoDAO {

    public boolean createServizio(ServizioAggiuntivo s) {
        String sql = "INSERT OR REPLACE INTO ServizioAggiuntivo (idServizio, nomeServizio, descrizione, disponibilitaMax) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, s.getIdServizio());
                ps.setString(2, s.getNomeServizio());
                ps.setString(3, s.getDescrizione());
                ps.setInt(4, s.getDisponibilitaMax());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio del servizio aggiuntivo: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public ServizioAggiuntivo readServizioById(String idServizio) {
        String sql = "SELECT * FROM ServizioAggiuntivo WHERE idServizio = ?";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idServizio);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new ServizioAggiuntivo(
                                rs.getString("idServizio"),
                                rs.getString("nomeServizio"),
                                rs.getString("descrizione"),
                                rs.getInt("disponibilitaMax")
                        );
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la lettura del servizio aggiuntivo: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public List<ServizioAggiuntivo> readAllServizi() {
        String sql = "SELECT * FROM ServizioAggiuntivo";
        List<ServizioAggiuntivo> catalogo = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    catalogo.add(new ServizioAggiuntivo(
                            rs.getString("idServizio"),
                            rs.getString("nomeServizio"),
                            rs.getString("descrizione"),
                            rs.getInt("disponibilitaMax")
                    ));
                }
            }
            return catalogo;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la lettura del catalogo servizi: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public boolean saveServiziPrenotati(String idPrenotazione, List<ServizioPrenotato> servizi) {
        if (servizi == null || servizi.isEmpty()) {
            return true;
        }
        String sql = "INSERT OR REPLACE INTO ServizioPrenotato (Prenotazione_idPrenotazione, ServizioAggiuntivo_idServizio, quantitaRichiesta, subTotale) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (ServizioPrenotato sp : servizi) {
                    ps.setString(1, idPrenotazione);
                    ps.setString(2, sp.getServizioRiferimento().getIdServizio());
                    ps.setInt(3, sp.getQuantitaRichiesta());
                    ps.setDouble(4, sp.getSubTotale());
                    ps.addBatch();
                }
                ps.executeBatch();
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio dei servizi prenotati: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }
}