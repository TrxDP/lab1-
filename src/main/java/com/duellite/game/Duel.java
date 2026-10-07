
package com.duellite.game;

import com.duellite.listener.BattleListener;
import com.duellite.model.Card;

import java.util.List;
import java.util.Random;

/**
 *
 *
 * Reglas:
 *  - Cada turno el participante elige una carta y su posicion (ataque o defensa).
 *  - Se compara el valor usado por cada carta (ATK si esta en ataque, DEF si esta en defensa).
 *    Esto cubre ATK vs ATK, ATK vs DEF, DEF vs ATK y DEF vs DEF. Gana el valor mayor; si son iguales, empate.
 *  - Una carta usada no puede volver a jugarse.
 *  - Gana quien llegue primero a 2 victorias. Si se agotan las cartas sin que nadie llegue a 2,
 *    gana quien tenga mas puntos (o hay empate).
 *  - El primer turno es aleatorio y los siguientes se alternan.
 */
public class Duel {

    public static final String PLAYER = "Jugador";
    public static final String AI = "Máquina";
    public static final String DRAW = "Empate";
    public static final int WINS_NEEDED = 2;

    private final List<Card> playerCards;
    private final List<Card> aiCards;
    private final boolean[] playerUsed;
    private final boolean[] aiUsed;
    private final BattleListener listener;
    private final Random random = new Random();

    private int playerScore;
    private int aiScore;
    private int round;
    private boolean started;
    private boolean ended;
    private boolean aiStarts;

    // Jugada de la maquina cuando inicia ella la ronda
    private int aiPendingIndex = -1;
    private boolean aiPendingAttack;

    // Ultima jugada de la maquina
    private int lastAiIndex = -1;
    private boolean lastAiAttack;

    public Duel(List<Card> playerCards, List<Card> aiCards, BattleListener listener) {
        this.playerCards = playerCards;
        this.aiCards = aiCards;
        this.playerUsed = new boolean[playerCards.size()];
        this.aiUsed = new boolean[aiCards.size()];
        this.listener = listener;
    }

    /** Inicia el duelo y decide aleatoriamente quien comienza. */
    public void start() {
        if (started) return;
        started = true;
        aiStarts = random.nextBoolean();
        startRound();
    }

    private void startRound() {
        round++;
        aiPendingIndex = -1;
        if (aiStarts) {
            aiPendingIndex = pickRandomAvailable(aiUsed);
            aiPendingAttack = random.nextBoolean();
        }
        listener.onRoundStart(round, aiStarts ? AI : PLAYER);
    }

    /**
     * El jugador juega una carta.
     *
     */
    public boolean playPlayerCard(int index, boolean attack) {
        if (!started || ended) return false;
        if (index < 0 || index >= playerCards.size() || playerUsed[index]) return false;

        playerUsed[index] = true;

        int aiIndex = aiPendingIndex;
        boolean aiAttack = aiPendingAttack;
        if (aiIndex < 0) {
            aiIndex = pickRandomAvailable(aiUsed);
            aiAttack = random.nextBoolean();
        }
        aiUsed[aiIndex] = true;
        aiPendingIndex = -1;
        lastAiIndex = aiIndex;
        lastAiAttack = aiAttack;

        Card p = playerCards.get(index);
        Card a = aiCards.get(aiIndex);
        int pValue = attack ? p.getAtk() : p.getDef();
        int aValue = aiAttack ? a.getAtk() : a.getDef();

        String winner;
        if (pValue > aValue) {
            winner = PLAYER;
            playerScore++;
        } else if (aValue > pValue) {
            winner = AI;
            aiScore++;
        } else {
            winner = DRAW;
        }

        listener.onTurn(describe(p, attack), describe(a, aiAttack), winner);
        listener.onScoreChanged(playerScore, aiScore);

        if (playerScore >= WINS_NEEDED || aiScore >= WINS_NEEDED || !hasAvailable(playerUsed)) {
            ended = true;
            listener.onDuelEnded(finalWinner());
        } else {
            aiStarts = !aiStarts; // se alterna quien inicia
            startRound();
        }
        return true;
    }

    private String finalWinner() {
        if (playerScore > aiScore) return PLAYER;
        if (aiScore > playerScore) return AI;
        return DRAW;
    }

    private String describe(Card c, boolean attack) {
        return c.getName() + (attack ? " [ATAQUE " + c.getAtk() + "]" : " [DEFENSA " + c.getDef() + "]");
    }

    private int pickRandomAvailable(boolean[] used) {
        int count = 0;
        for (boolean u : used) if (!u) count++;
        int n = random.nextInt(count);
        for (int i = 0; i < used.length; i++) {
            if (!used[i] && n-- == 0) return i;
        }
        return -1;
    }

    private boolean hasAvailable(boolean[] used) {
        for (boolean u : used) if (!u) return true;
        return false;
    }

    public boolean isPlayerCardAvailable(int index) {
        return index >= 0 && index < playerUsed.length && !playerUsed[index];
    }

    public boolean isActive() { return started && !ended; }
    public boolean isFinished() { return ended; }
    public int getPlayerScore() { return playerScore; }
    public int getAiScore() { return aiScore; }
    public int getLastAiIndex() { return lastAiIndex; }
    public boolean isLastAiAttack() { return lastAiAttack; }
}
