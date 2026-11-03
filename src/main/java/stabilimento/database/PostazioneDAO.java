package stabilimento.database;

import stabilimento.entity.Postazione;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostazioneDAO {
    private final TariffaDAO tariffaDAO = new TariffaDAO();

    public boolean createPostazione(Postazione pos) {
        String sql = "INSERT OR REPLACE INTO Postazione (idPostazione, numeroProgressivo, fila, posizione, statoOccupazione, StabilimentoBalneare_idStabilimento) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pos.getIdPostazione());
                ps.setInt(2, pos.getNumeroProgressivo());
                ps.setInt(3, pos.getFila());
                ps.setString(4, pos.getPosizione());
                ps.setString(5, pos.isOccupata() ? "Occupata" : "Libera");
                ps.setString(6, "STAB-01");
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la creazione della postazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public List<Postazione> readAllPostazioni() {
        String sql = "SELECT * FROM Postazione ORDER BY fila ASC, numeroProgressivo ASC";
        List<Postazione> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Postazione p = mapRowToPostazione(rs);
                    p.setListinoTariffe(tariffaDAO.readTariffaByPostazione(p.getIdPostazione()));
                    lista.add(p);
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la lettura di tutte le postazioni: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public List<Postazione> readPostazioniLibere(Date inizio, Date fine) {
        String sql = """
            SELECT * FROM Postazione p
            WHERE p.idPostazione NOT IN (
                SELECT pr.Postazione_idPostazione FROM Prenotazione pr
                WHERE pr.stato != 'Annullata'
                  AND pr.dataInizio <= ?
                  AND pr.dataFine >= ?
            )
            ORDER BY p.fila ASC, p.numeroProgressivo ASC
            """;
        List<Postazione> postazioniLibere = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, fine.getTime());
                ps.setLong(2, inizio.getTime());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Postazione p = mapRowToPostazione(rs);
                        p.setListinoTariffe(tariffaDAO.readTariffaByPostazione(p.getIdPostazione()));
                        postazioniLibere.add(p);
                    }
                }
            }
            return postazioniLibere;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca delle postazioni libere: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public boolean updatePostazione(Postazione pos) {
        String sql = "UPDATE Postazione SET numeroProgressivo = ?, fila = ?, posizione = ?, statoOccupazione = ? WHERE idPostazione = ?";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, pos.getNumeroProgressivo());
                ps.setInt(2, pos.getFila());
                ps.setString(3, pos.getPosizione());
                ps.setString(4, pos.isOccupata() ? "Occupata" : "Libera");
                ps.setString(5, pos.getIdPostazione());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento della postazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public boolean deletePostazione(String idPostazione) {
        String sql = "DELETE FROM Postazione WHERE idPostazione = ?";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idPostazione);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione della postazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    private Postazione mapRowToPostazione(ResultSet rs) throws SQLException {
        Postazione pos = new Postazione(
                rs.getString("idPostazione"),
                rs.getInt("numeroProgressivo"),
                rs.getInt("fila"),
                rs.getString("posizione")
        );
        pos.setOccupata("Occupata".equalsIgnoreCase(rs.getString("statoOccupazione")));
        return pos;
    }
}