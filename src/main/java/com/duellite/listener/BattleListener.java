package com.duellite.listener;

/** Permite que la logica del duelo notifique eventos a la interfaz sin depender de Swing. */
public interface BattleListener {


    void onTurn(String playerCard, String aiCard, String winner);


    void onScoreChanged(int playerScore, int aiScore);


    void onDuelEnded(String winner);


    void onRoundStart(int round, String starter);
}
