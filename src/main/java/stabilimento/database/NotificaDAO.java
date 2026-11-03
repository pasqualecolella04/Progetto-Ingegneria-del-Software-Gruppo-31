package stabilimento.database;

import stabilimento.entity.Notifica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NotificaDAO {

    public boolean createNotifica(Notifica n, String idPrenotazione) {
        String sql = "INSERT OR REPLACE INTO Notifica (idNotifica, destinatario, testo, dataInvio, canale, esitoInvio, Prenotazione_idPrenotazione) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, n.getIdNotifica());
                ps.setString(2, n.getDestinatario());
                ps.setString(3, n.getTestoRiepilogo());
                ps.setLong(4, n.getDataInvio() != null ? n.getDataInvio().getTime() : System.currentTimeMillis());
                ps.setString(5, "EMAIL/SMS");
                ps.setInt(6, n.isEsitoInvio() ? 1 : 0);
                ps.setString(7, idPrenotazione);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la registrazione della notifica su DB: " + e.getMessage(), e);
        } finally {
            DBManager.getInstance().closeConnection(conn);
        }
    }
}