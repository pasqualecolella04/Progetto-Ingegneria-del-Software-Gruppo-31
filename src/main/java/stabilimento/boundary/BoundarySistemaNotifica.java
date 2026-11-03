package stabilimento.boundary;

public class BoundarySistemaNotifica {
    private static boolean servizioAttivo = true;

    public boolean inviaNotificaRiepilogo(String emailCliente, String testoRiepilogo) {
        return trasmettiMessaggio(emailCliente, testoRiepilogo);
    }

    public boolean trasmettiMessaggio(String destinatario, String messaggio) {
        if (!servizioAttivo || destinatario == null || !destinatario.contains("@") || messaggio == null) {
            return false;
        }
        System.out.println("[Servizio di Notifica] Trasmissione riuscita a " + destinatario + " -> " + messaggio);
        return true;
    }

    public static void setServizioAttivo(boolean attivo) {
        servizioAttivo = attivo;
    }
}