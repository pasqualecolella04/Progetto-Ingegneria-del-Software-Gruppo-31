package stabilimento.database;

import stabilimento.entity.Tariffa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TariffaDAO {

    public boolean createTariffa(Tariffa t) {
        String sql = "INSERT OR REPLACE INTO Tariffa (idTariffa, stagione, prezzoGiornaliero, dataInizioValidita, dataFineValidita) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, t.getIdTariffa());
                ps.setString(2, t.getStagione());
                ps.setDouble(3, t.getPrezzoGiornaliero());
                ps.setLong(4, t.getDataInizioValidita().getTime());
                ps.setLong(5, t.getDataFineValidita().getTime());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la creazione della tariffa: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public List<Tariffa> readTariffaByPostazione(String idPostazione) {
        String sql = "SELECT * FROM Tariffa WHERE Postazione_idPostazione = ? OR Postazione_idPostazione IS NULL";
        List<Tariffa> tariffe = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idPostazione);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Tariffa t = new Tariffa(
                                rs.getString("idTariffa"),
                                rs.getString("stagione"),
                                rs.getDouble("prezzoGiornaliero"),
                                new Date(rs.getLong("dataInizioValidita")),
                                new Date(rs.getLong("dataFineValidita"))
                        );
                        tariffe.add(t);
                    }
                }
            }
            return tariffe;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il caricamento delle tariffe della postazione: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }

    public boolean updateTariffa(Tariffa t) {
        String sql = "UPDATE Tariffa SET stagione = ?, prezzoGiornaliero = ?, dataInizioValidita = ?, dataFineValidita = ? WHERE idTariffa = ?";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, t.getStagione());
                ps.setDouble(2, t.getPrezzoGiornaliero());
                ps.setLong(3, t.getDataInizioValidita().getTime());
                ps.setLong(4, t.getDataFineValidita().getTime());
                ps.setString(5, t.getIdTariffa());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento della tariffa: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }
}