package stabilimento.boundary;

import stabilimento.control.GestioneUtentiController;
import stabilimento.entity.Utente;

import javax.swing.*;
import java.awt.*;

public class BoundaryUtente extends JFrame {
    private JTextField txtNome;
    private JTextField txtCognome;
    private JTextField txtEmail;
    private JTextField txtTelefono;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRuolo;
    private Utente utenteAutenticato;

    public BoundaryUtente() {
        setTitle("Progetto Ing.S Gruppo 31 - Autenticazione e Registrazione");
        setSize(480, 340);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        mostraFormLogin();
    }

    public void mostraFormRegistrazione() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        txtNome = new JTextField();
        txtCognome = new JTextField();
        txtEmail = new JTextField();
        txtTelefono = new JTextField();
        txtPassword = new JPasswordField();
        cmbRuolo = new JComboBox<>(new String[]{"Cliente", "Gestore"});

        panel.add(new JLabel("Nome:"));
        panel.add(txtNome);
        panel.add(new JLabel("Cognome:"));
        panel.add(txtCognome);
        panel.add(new JLabel("Email:"));
        panel.add(txtEmail);
        panel.add(new JLabel("Telefono:"));
        panel.add(txtTelefono);
        panel.add(new JLabel("Password:"));
        panel.add(txtPassword);
        panel.add(new JLabel("Ruolo:"));
        panel.add(cmbRuolo);

        JButton btnRegistra = new JButton("Completa Registrazione");
        btnRegistra.addActionListener(e -> inviaDatiRegistrazione(
                txtNome.getText().trim(),
                txtCognome.getText().trim(),
                txtEmail.getText().trim(),
                txtTelefono.getText().trim(),
                new String(txtPassword.getPassword()),
                (String) cmbRuolo.getSelectedItem()
        ));

        JButton btnPassaALogin = new JButton("Vai al Login");
        btnPassaALogin.addActionListener(e -> mostraFormLogin());

        panel.add(btnRegistra);
        panel.add(btnPassaALogin);

        setContentPane(panel);
        revalidate();
        repaint();
    }

    public void inviaDatiRegistrazione(String nome, String cognome, String email, String tel, String pwd, String ruolo) {
        boolean registrato = GestioneUtentiController.getInstance()
                .registraUtente(nome, cognome, email, tel, pwd, ruolo);
        if (registrato) {
            JOptionPane.showMessageDialog(this, "Registrazione completata con successo!", "Esito", JOptionPane.INFORMATION_MESSAGE);
            mostraFormLogin();
        } else {
            JOptionPane.showMessageDialog(this, "Dati non validi o email già registrata.", "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void mostraFormLogin() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        txtEmail = new JTextField();
        txtPassword = new JPasswordField();

        panel.add(new JLabel("Email:"));
        panel.add(txtEmail);
        panel.add(new JLabel("Password:"));
        panel.add(txtPassword);

        JButton btnLogin = new JButton("Accedi");
        btnLogin.addActionListener(e -> inviaCredenziali(
                txtEmail.getText().trim(),
                new String(txtPassword.getPassword())
        ));

        JButton btnPassaARegistrazione = new JButton("Registrati");
        btnPassaARegistrazione.addActionListener(e -> mostraFormRegistrazione());

        panel.add(btnLogin);
        panel.add(btnPassaARegistrazione);

        setContentPane(panel);
        revalidate();
        repaint();
    }

    public void inviaCredenziali(String email, String pwd) {
        Utente u = GestioneUtentiController.getInstance().autenticaUtente(email, pwd);
        if (u != null) {
            this.utenteAutenticato = u;
            JOptionPane.showMessageDialog(this, "Benvenuto " + u.getNome() + " (" + u.getRuolo() + ")", "Login Riuscito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Credenziali non valide.", "Errore Login", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void effettuaLogout() {
        if (utenteAutenticato != null) {
            GestioneUtentiController.getInstance().logout(utenteAutenticato.getIdUtente());
            utenteAutenticato = null;
        }
        mostraFormLogin();
    }
}