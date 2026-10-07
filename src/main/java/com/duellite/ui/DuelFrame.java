package com.duellite.ui;

import com.duellite.api.YgoApiClient;
import com.duellite.game.Duel;
import com.duellite.listener.BattleListener;
import com.duellite.model.Card;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Ventana principal. Solo muestra informacion y reenvia las acciones del usuario a Duel;
 * las reglas viven en Duel y las peticiones HTTP en YgoApiClient.
 */
public class DuelFrame extends JFrame implements BattleListener {

    private static final int CARDS_PER_SIDE = 3;

    private final YgoApiClient api = new YgoApiClient();

    private final JButton startButton = new JButton("Iniciar duelo");
    private final JLabel scoreLabel = new JLabel("Jugador 0  -  0 Máquina", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("Presiona \"Iniciar duelo\" para comenzar.", SwingConstants.CENTER);
    private final JTextArea logArea = new JTextArea();

    private final CardPanel[] playerPanels = new CardPanel[CARDS_PER_SIDE];
    private final CardPanel[] aiPanels = new CardPanel[CARDS_PER_SIDE];

    private Duel duel;
    private List<Card> aiCards = new ArrayList<>();
    private List<BufferedImage> aiImages = new ArrayList<>();

    public DuelFrame() {
        super("Yu-Gi-Oh! Duel Lite");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(20, 16, 36));

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildBoard(), BorderLayout.CENTER);
        add(buildLogPanel(), BorderLayout.SOUTH);

        startButton.addActionListener(e -> startDuel());

        pack();

        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(getWidth(), screen.width), Math.min(getHeight(), screen.height));
        setMinimumSize(new Dimension(560, 500));
        setLocationRelativeTo(null);
    }

    // ---------------------------------------------------------------- construccion de la UI

    private JPanel buildTopPanel() {
        JPanel top = new JPanel(new GridLayout(3, 1, 0, 4));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        top.setOpaque(false);

        JLabel title = new JLabel("YU-GI-OH! DUEL LITE", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(212, 175, 55));

        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 18f));
        scoreLabel.setForeground(Color.WHITE);

        statusLabel.setForeground(new Color(255, 230, 150));

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(startButton, BorderLayout.WEST);
        row.add(scoreLabel, BorderLayout.CENTER);

        top.add(title);
        top.add(row);
        top.add(statusLabel);
        return top;
    }

    private JPanel buildBoard() {
        JPanel board = new JPanel(new GridLayout(2, 1, 0, 6));
        board.setOpaque(false);
        board.add(buildRow("Máquina", aiPanels, false));
        board.add(buildRow("Jugador", playerPanels, true));
        return board;
    }

    private JPanel buildRow(String title, CardPanel[] panels, boolean player) {

        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(212, 175, 55)), title,
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                null, Color.WHITE));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.weighty = 1.0;
        gc.fill = GridBagConstraints.VERTICAL;
        gc.insets = new Insets(2, 6, 2, 6);
        for (int i = 0; i < panels.length; i++) {
            gc.gridx = i;
            panels[i] = new CardPanel(player);
            if (player) {
                final int index = i;
                panels[i].getAttackButton().addActionListener(e -> playCard(index, true));
                panels[i].getDefendButton().addActionListener(e -> playCard(index, false));
            }
            row.add(panels[i], gc);
        }
        return row;
    }

    private JScrollPane buildLogPanel() {
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setPreferredSize(new Dimension(800, 120));
        scroll.setBorder(BorderFactory.createTitledBorder("Log de batalla"));
        return scroll;
    }

    // ---------------------------------------------------------------- carga de cartas (fuera del EDT)

    /** Resultado de la carga: 6 cartas (3 + 3) con sus imagenes. */
    private static class LoadResult {
        final List<Card> cards = new ArrayList<>();
        final List<BufferedImage> images = new ArrayList<>();
        final List<String> warnings = new ArrayList<>();
    }

    private void startDuel() {
        startButton.setEnabled(false);
        scoreLabel.setText("Jugador 0  -  0 Máquina");
        logArea.setText("");
        for (int i = 0; i < CARDS_PER_SIDE; i++) {
            playerPanels[i].showEmpty();
            aiPanels[i].showEmpty();
            playerPanels[i].setBackground(new Color(35, 30, 55));
            aiPanels[i].setBackground(new Color(35, 30, 55));
        }
        log("Preparando duelo: obteniendo 6 cartas Monster desde YGOProDeck...");

        // SwingWorker: doInBackground corre en otro hilo; process() y done() corren en el EDT.
        new SwingWorker<LoadResult, String>() {
            @Override
            protected LoadResult doInBackground() throws Exception {
                LoadResult result = new LoadResult();
                int total = CARDS_PER_SIDE * 2;
                for (int i = 0; i < total; i++) {
                    publish("Cargando carta " + (i + 1) + " de " + total + "...");
                    Card card = api.fetchRandomMonster();
                    BufferedImage image = null;
                    try {
                        image = api.fetchImage(card.getImage());
                        if (image == null) throw new IOException("formato de imagen no soportado");
                    } catch (IOException ex) {
                        result.warnings.add("No se pudo cargar la imagen de " + card.getName() + " (" + ex.getMessage() + ")");
                    }
                    result.cards.add(card);
                    result.images.add(image);
                }
                return result;
            }

            @Override
            protected void process(List<String> chunks) {
                statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    onCardsLoaded(get());
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof IOException) {
                        showError("Error de red: " + cause.getMessage());
                    } else if (cause instanceof IllegalStateException) {
                        showError(cause.getMessage()); // "No se pudo cargar la carta"
                    } else {
                        showError("Error inesperado: " + cause);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("La carga fue interrumpida.");
                }
            }
        }.execute();
    }

    /** Se ejecuta en el EDT cuando ya estan las 6 cartas. Recien aqui se crea el duelo. */
    private void onCardsLoaded(LoadResult result) {
        List<Card> playerCards = new ArrayList<>(result.cards.subList(0, CARDS_PER_SIDE));
        aiCards = new ArrayList<>(result.cards.subList(CARDS_PER_SIDE, CARDS_PER_SIDE * 2));
        aiImages = new ArrayList<>(result.images.subList(CARDS_PER_SIDE, CARDS_PER_SIDE * 2));

        for (int i = 0; i < CARDS_PER_SIDE; i++) {
            playerPanels[i].showCard(playerCards.get(i), result.images.get(i));
            aiPanels[i].showHidden();
        }
        for (String w : result.warnings) {
            log("Aviso: " + w);
        }
        log("Cartas cargadas. Tus cartas:");
        for (Card c : playerCards) log("  - " + c);

        duel = new Duel(playerCards, aiCards, this);
        duel.start();
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(255, 120, 120));
        log("ERROR: " + message);
        startButton.setText("Reintentar");
        startButton.setEnabled(true);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }



    private void playCard(int index, boolean attack) {
        if (duel == null || !duel.playPlayerCard(index, attack)) {
            statusLabel.setText("Jugada no valida: esa carta no esta disponible o no es tu turno.");
        }
    }

    private void refreshPlayerCards() {
        for (int i = 0; i < CARDS_PER_SIDE; i++) {
            boolean available = duel != null && duel.isActive() && duel.isPlayerCardAvailable(i);
            playerPanels[i].setCardButtonsEnabled(available);
            if (duel != null && !duel.isPlayerCardAvailable(i)) {
                playerPanels[i].setUsed(true);
            }
        }
    }

    private void log(String text) {
        logArea.append(text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength()); // desplaza al final
    }



    @Override
    public void onRoundStart(int round, String starter) {
        statusLabel.setForeground(new Color(255, 230, 150));
        log("--- Ronda " + round + " --- inicia: " + starter);
        if (starter.equals(Duel.AI)) {
            statusLabel.setText("Ronda " + round + ": inicia la Máquina (ya eligió una carta oculta). Elige tu carta.");
        } else {
            statusLabel.setText("Ronda " + round + ": inicias tú. Elige una carta y Atacar o Defender.");
        }
        refreshPlayerCards();
    }

    @Override
    public void onTurn(String playerCard, String aiCard, String winner) {
        // Se revela la carta que jugo la maquina.
        int i = duel.getLastAiIndex();
        aiPanels[i].reveal(aiCards.get(i), aiImages.get(i), duel.isLastAiAttack());
        aiPanels[i].setUsed(true);

        log("Jugador usa: " + playerCard);
        log("Máquina usa: " + aiCard);
        log("Resultado del turno: " + (winner.equals(Duel.DRAW) ? "Empate (nadie suma punto)" : "gana " + winner + " (+1 punto)"));
        statusLabel.setText("Resultado: " + (winner.equals(Duel.DRAW) ? "empate" : "gana " + winner));
    }

    @Override
    public void onScoreChanged(int playerScore, int aiScore) {
        scoreLabel.setText("Jugador " + playerScore + "  -  " + aiScore + " Máquina");
        log("Marcador: Jugador " + playerScore + " - " + aiScore + " Máquina");
    }

    @Override
    public void onDuelEnded(String winner) {
        refreshPlayerCards(); // duelo terminado: todos los botones quedan bloqueados
        String msg = winner.equals(Duel.DRAW) ? "El duelo termina en empate." : "Ganador del duelo: " + winner;
        statusLabel.setText(msg);
        log("=== " + msg + " ===");
        startButton.setText("Iniciar duelo");
        startButton.setEnabled(true);
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, msg, "Fin del duelo", JOptionPane.INFORMATION_MESSAGE));
    }
}