package stabilimento.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import stabilimento.dto.PrenotazioneDTO;
import stabilimento.dto.ReportDTO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StructuralTest {

    private StabilimentoBalneare stabilimento;
    private SimpleDateFormat sdf;
    private Date dataInizio;
    private Date dataFine;

    @BeforeEach
    void setUp() throws Exception {
        stabilimento = new StabilimentoBalneare("STAB-01", "Lido Gruppo 31", "Via Marina 31, Napoli");
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        dataInizio = sdf.parse("2026-08-01");
        dataFine = sdf.parse("2026-08-03"); // 3 giorni inclusivi
    }

    //TC_1: Cammino 1-2-17-19-20
    @Test
    void testTC1_ListaNull() {
        ReportDTO report = stabilimento.calcolaTotaliEIndiciAffluenza(null);

        assertNotNull(report);
        assertTrue(report.getIdReport().startsWith("REP-"));
        assertEquals("Generale", report.getTipoReport());
        assertEquals(0, report.getNumeroTotalePrenotazioni());
        assertEquals(0.0, report.getIncassoTotale(), 0.001);
        assertEquals(0.0, report.getIncassoMedioPerPrenotazione(), 0.001);
        assertEquals(0, report.getTotaleServiziAccessoriVenduti());
        assertTrue(report.getRigheDettaglio().isEmpty());
    }

    //TC_2: Cammino 1-2-3-17-19-20
    @Test
    void testTC2_ListaVuota() {
        List<Prenotazione> listaVuota = new ArrayList<>();

        ReportDTO report = stabilimento.calcolaTotaliEIndiciAffluenza(listaVuota);

        assertNotNull(report);
        assertEquals(0, report.getNumeroTotalePrenotazioni());
        assertEquals(0.0, report.getIncassoTotale(), 0.001);
        assertEquals(0.0, report.getIncassoMedioPerPrenotazione(), 0.001);
        assertEquals(0, report.getTotaleServiziAccessoriVenduti());
        assertTrue(report.getRigheDettaglio().isEmpty());
    }

    // TC_3: Cammino 1-2-3-4-5-4-17-19-20
    @Test
    void testTC3_PrenotazioneAnnullata() {
        Cliente c = new Cliente("Pasquale", "Colella", "pasquale@unina.it", "3331234567", "pwd");
        c.setIdUtente("C01");
        Postazione pos = new Postazione("POS-01", 1, 1, "Prima Fila");

        Prenotazione pAnnullata = new Prenotazione(c, pos, dataInizio, dataFine, "Annullata");
        List<Prenotazione> lista = Collections.singletonList(pAnnullata);

        ReportDTO report = stabilimento.calcolaTotaliEIndiciAffluenza(lista);

        assertNotNull(report);
        assertEquals(0, report.getNumeroTotalePrenotazioni());
        assertEquals(0.0, report.getIncassoTotale(), 0.001);
        assertEquals(0.0, report.getIncassoMedioPerPrenotazione(), 0.001);
        assertEquals(0, report.getTotaleServiziAccessoriVenduti());
        assertTrue(report.getRigheDettaglio().isEmpty());
    }

    //TC_4: Cammino 1-2-3-4-5-6-7-9-10-11-13-14-16-4-17-18-20
    @Test
    void testTC4_ValidaSenzaServizi() {
        Cliente c = new Cliente("Pasquale", "Colella", "pasquale@unina.it", "3331234567", "pwd");
        c.setIdUtente("C01");
        Postazione pos = new Postazione("POS-01", 1, 1, "Prima Fila");

        Prenotazione pValida = new Prenotazione(c, pos, dataInizio, dataFine, "Confermata");
        List<Prenotazione> lista = Collections.singletonList(pValida);

        ReportDTO report = stabilimento.calcolaTotaliEIndiciAffluenza(lista);

        assertNotNull(report);
        assertEquals(1, report.getNumeroTotalePrenotazioni());
        assertEquals(90.0, report.getIncassoTotale(), 0.001);
        assertEquals(90.0, report.getIncassoMedioPerPrenotazione(), 0.001);
        assertEquals(0, report.getTotaleServiziAccessoriVenduti());
        assertEquals(1, report.getRigheDettaglio().size());

        PrenotazioneDTO riga = report.getRigheDettaglio().get(0);
        assertEquals("C01", riga.getIdCliente());
        assertEquals("Pasquale Colella", riga.getNominativoCliente());
        assertEquals("POS-01", riga.getCodiceOmbrellone());
        assertEquals(pos.toString(), riga.getCoordinateOmbrellone());
    }

    // TC_5: Cammino 1-2-3-4-5-6-7-8-7-9-10-11-13-14-16-4-17-18-20
    @Test
    void testTC5_ValidaConServizi() {
        Cliente c = new Cliente("Pasquale", "Colella", "pasquale@unina.it", "3331234567", "pwd");
        c.setIdUtente("C01");
        Postazione pos = new Postazione("POS-01", 1, 1, "Prima Fila");

        Prenotazione pValida = new Prenotazione(c, pos, dataInizio, dataFine, "Confermata");
        ServizioAggiuntivo lettino = new ServizioAggiuntivo("S01", "Lettino Extra", "Alluminio", 5);
        pValida.aggiungiServizioPrenotato(lettino, 2);

        List<Prenotazione> lista = Collections.singletonList(pValida);

        ReportDTO report = stabilimento.calcolaTotaliEIndiciAffluenza(lista);

        assertNotNull(report);
        assertEquals(1, report.getNumeroTotalePrenotazioni());
        assertEquals(150.0, report.getIncassoTotale(), 0.001);
        assertEquals(150.0, report.getIncassoMedioPerPrenotazione(), 0.001);
        assertEquals(2, report.getTotaleServiziAccessoriVenduti());

        PrenotazioneDTO riga = report.getRigheDettaglio().get(0);
        assertEquals(2, riga.getTotaleServiziAccessoriAcquistati());
        assertEquals("€ 150,00", riga.getImportoMonetarioFormattato());
    }

    //TC_6: Cammino 1-2-3-4-5-6-7-9-10-12-13-14-16-4-17-18-20
    @Test
    void testTC6_ClienteNullPostazionePresente() {
        Postazione pos = new Postazione("POS-02", 2, 2, "Seconda Fila");

        Prenotazione pSenzaCliente = new Prenotazione(null, pos, dataInizio, dataFine, "In Attesa di Conferma");
        pSenzaCliente.setImportoTotale(75.0);
        List<Prenotazione> lista = Collections.singletonList(pSenzaCliente);

        ReportDTO report = stabilimento.calcolaTotaliEIndiciAffluenza(lista);

        assertNotNull(report);
        assertEquals(1, report.getNumeroTotalePrenotazioni());
        assertEquals(75.0, report.getIncassoTotale(), 0.001);

        PrenotazioneDTO riga = report.getRigheDettaglio().get(0);
        assertEquals("N/D", riga.getIdCliente());
        assertEquals("N/D", riga.getNominativoCliente());
        assertEquals("POS-02", riga.getCodiceOmbrellone());
        assertEquals(pos.toString(), riga.getCoordinateOmbrellone());
    }

    // TC_7: Cammino 1-2-3-4-5-6-7-9-10-11-13-15-16-4-17-18-20
    @Test
    void testTC7_ClientePresentePostazioneNull() {
        Cliente c = new Cliente("Antonio", "Borrelli", "antonio@unina.it", "3339876543", "pwd");
        c.setIdUtente("C02");

        Prenotazione pSenzaPos = new Prenotazione(c, null, dataInizio, dataFine, "Confermata");
        pSenzaPos.setImportoTotale(60.0);
        List<Prenotazione> lista = Collections.singletonList(pSenzaPos);

        ReportDTO report = stabilimento.calcolaTotaliEIndiciAffluenza(lista);

        assertNotNull(report);
        assertEquals(1, report.getNumeroTotalePrenotazioni());
        assertEquals(60.0, report.getIncassoTotale(), 0.001);

        PrenotazioneDTO riga = report.getRigheDettaglio().get(0);
        assertEquals("C02", riga.getIdCliente());
        assertEquals("Antonio Borrelli", riga.getNominativoCliente());
        assertEquals("N/D", riga.getCodiceOmbrellone());
        assertEquals("N/D", riga.getCoordinateOmbrellone());
    }

    // TC_8: Cammino 1-2-3-4-5-4-5-6-7-8-7-9-10-11-13-14-16-4-17-18-20
    @Test
    void testTC8_ListaMistaIterazioniMultiple() {
        Cliente c1 = new Cliente("Pasquale", "Colella", "pasquale@unina.it", "3331234567", "pwd");
        c1.setIdUtente("C01");
        Cliente c2 = new Cliente("Antonio", "Borrelli", "antonio@unina.it", "3339876543", "pwd");
        c2.setIdUtente("C02");

        Postazione pos1 = new Postazione("POS-01", 1, 1, "Prima Fila");

        Prenotazione p1Annullata = new Prenotazione(c1, pos1, dataInizio, dataFine, "Annullata");

        Prenotazione p2Confermata = new Prenotazione(c1, pos1, dataInizio, dataFine, "Confermata");
        ServizioAggiuntivo lettino = new ServizioAggiuntivo("S01", "Lettino Extra", "Alluminio", 5);
        p2Confermata.aggiungiServizioPrenotato(lettino, 2); // 90 base + 60 servizi = 150 EUR

        Prenotazione p3InAttesa = new Prenotazione(c2, pos1, dataInizio, dataFine, "In Attesa di Conferma"); // 90 EUR base

        List<Prenotazione> lista = Arrays.asList(p1Annullata, p2Confermata, p3InAttesa);

        ReportDTO report = stabilimento.calcolaTotaliEIndiciAffluenza(lista);

        assertNotNull(report);
        assertEquals(2, report.getNumeroTotalePrenotazioni());
        assertEquals(240.0, report.getIncassoTotale(), 0.001);
        assertEquals(120.0, report.getIncassoMedioPerPrenotazione(), 0.001);
        assertEquals(2, report.getTotaleServiziAccessoriVenduti());
        assertEquals(2, report.getRigheDettaglio().size());
    }
}
