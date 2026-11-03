package stabilimento.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
    private static DBManager instance;
    private String url;
    private String username;
    private String password;

    private DBManager() {
        this.url = "jdbc:sqlite:stabilimento_gruppo31.db";
        this.username = "root";
        this.password = "";
        inizializzaSchemaDatabase();
    }

    public static synchronized DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (url.startsWith("jdbc:sqlite:")) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, username, password);
    }

    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Errore durante la chiusura della connessione JDBC: " + e.getMessage());
            }
        }
    }

    private void inizializzaSchemaDatabase() {
        String sqlUtenti = """
            CREATE TABLE IF NOT EXISTS Utenti (
                idUtente VARCHAR(45) PRIMARY KEY,
                nome VARCHAR(45) NOT NULL,
                cognome VARCHAR(45) NOT NULL,
                email VARCHAR(45) UNIQUE NOT NULL,
                telefono VARCHAR(20),
                passwordHash VARCHAR(255) NOT NULL,
                ruolo VARCHAR(20) NOT NULL
            );
            """;

        String sqlStabilimento = """
            CREATE TABLE IF NOT EXISTS StabilimentoBalneare (
                idStabilimento VARCHAR(45) PRIMARY KEY,
                nome VARCHAR(45) NOT NULL,
                indirizzo VARCHAR(255) NOT NULL,
                numPostazioniTotali INT DEFAULT 0
            );
            """;

        String sqlPostazione = """
            CREATE TABLE IF NOT EXISTS Postazione (
                idPostazione VARCHAR(45) PRIMARY KEY,
                numeroProgressivo INT NOT NULL,
                fila INT NOT NULL,
                posizione VARCHAR(45) NOT NULL,
                statoOccupazione VARCHAR(45) NOT NULL,
                StabilimentoBalneare_idStabilimento VARCHAR(45),
                FOREIGN KEY (StabilimentoBalneare_idStabilimento) REFERENCES StabilimentoBalneare(idStabilimento)
            );
            """;

        String sqlServizioAggiuntivo = """
            CREATE TABLE IF NOT EXISTS ServizioAggiuntivo (
                idServizio VARCHAR(45) PRIMARY KEY,
                nomeServizio VARCHAR(45) NOT NULL,
                descrizione VARCHAR(255),
                disponibilitaMax INT NOT NULL
            );
            """;

        String sqlPrenotazione = """
            CREATE TABLE IF NOT EXISTS Prenotazione (
                idPrenotazione VARCHAR(45) PRIMARY KEY,
                dataInizio BIGINT NOT NULL,
                dataFine BIGINT NOT NULL,
                oraInizio VARCHAR(10),
                stato VARCHAR(45) NOT NULL,
                importoTotale DOUBLE NOT NULL,
                Utenti_idUtente VARCHAR(45) NOT NULL,
                Postazione_idPostazione VARCHAR(45) NOT NULL,
                FOREIGN KEY (Utenti_idUtente) REFERENCES Utenti(idUtente),
                FOREIGN KEY (Postazione_idPostazione) REFERENCES Postazione(idPostazione)
            );
            """;

        String sqlServizioPrenotato = """
            CREATE TABLE IF NOT EXISTS ServizioPrenotato (
                Prenotazione_idPrenotazione VARCHAR(45) NOT NULL,
                ServizioAggiuntivo_idServizio VARCHAR(45) NOT NULL,
                quantitaRichiesta INT NOT NULL,
                subTotale DOUBLE NOT NULL,
                PRIMARY KEY (Prenotazione_idPrenotazione, ServizioAggiuntivo_idServizio),
                FOREIGN KEY (Prenotazione_idPrenotazione) REFERENCES Prenotazione(idPrenotazione),
                FOREIGN KEY (ServizioAggiuntivo_idServizio) REFERENCES ServizioAggiuntivo(idServizio)
            );
            """;

        String sqlTariffa = """
            CREATE TABLE IF NOT EXISTS Tariffa (
                idTariffa VARCHAR(45) PRIMARY KEY,
                stagione VARCHAR(45) NOT NULL,
                prezzoGiornaliero DOUBLE NOT NULL,
                dataInizioValidita BIGINT NOT NULL,
                dataFineValidita BIGINT NOT NULL,
                Postazione_idPostazione VARCHAR(45),
                ServizioAggiuntivo_idServizio VARCHAR(45),
                FOREIGN KEY (Postazione_idPostazione) REFERENCES Postazione(idPostazione),
                FOREIGN KEY (ServizioAggiuntivo_idServizio) REFERENCES ServizioAggiuntivo(idServizio)
            );
            """;

        String sqlNotifica = """
            CREATE TABLE IF NOT EXISTS Notifica (
                idNotifica VARCHAR(45) PRIMARY KEY,
                destinatario VARCHAR(45),
                testo VARCHAR(500) NOT NULL,
                dataInvio BIGINT NOT NULL,
                canale VARCHAR(45),
                esitoInvio INT NOT NULL,
                Prenotazione_idPrenotazione VARCHAR(45) NOT NULL,
                FOREIGN KEY (Prenotazione_idPrenotazione) REFERENCES Prenotazione(idPrenotazione)
            );
            """;

        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sqlUtenti);
                stmt.execute(sqlStabilimento);
                stmt.execute(sqlPostazione);
                stmt.execute(sqlServizioAggiuntivo);
                stmt.execute(sqlPrenotazione);
                stmt.execute(sqlServizioPrenotato);
                stmt.execute(sqlTariffa);
                stmt.execute(sqlNotifica);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'inizializzazione delle tabelle del database: " + e.getMessage(), e);
        } finally {
            closeConnection(conn);
        }
    }
}