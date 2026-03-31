package com.narxoz.rpg.tournament;

import com.narxoz.rpg.arena.ArenaFighter;
import com.narxoz.rpg.arena.ArenaOpponent;
import com.narxoz.rpg.arena.TournamentResult;
import com.narxoz.rpg.chain.*;
import com.narxoz.rpg.command.*;

import java.util.Random;

public class TournamentEngine {
    private final ArenaFighter hero;
    private final ArenaOpponent opponent;
    private Random random = new Random(1L);

    public TournamentEngine(ArenaFighter hero, ArenaOpponent opponent) {
        this.hero = hero;
        this.opponent = opponent;
    }

    public TournamentEngine setRandomSeed(long seed) {
        this.random = new Random(seed);
        return this;
    }

    public TournamentResult runTournament() {
        TournamentResult result = new TournamentResult();
        int round = 0;
        final int maxRounds = 20;
        DefenseHandler dodge = new DodgeHandler(hero.getDodgeChance(), 42L);
        DefenseHandler block = new BlockHandler(hero.getBlockRating() / 100.0);
        DefenseHandler armor = new ArmorHandler(hero.getArmorValue());
        DefenseHandler hp = new HpHandler();

        dodge.setNext(block).setNext(armor).setNext(hp);
        ActionQueue actionQueue = new ActionQueue();
        while (hero.isAlive() && opponent.isAlive() && round < maxRounds) {
            round++;
            result.addLine("\n--- Round " + round + " ---");
            actionQueue.enqueue(new AttackCommand(opponent, hero.getAttackPower()));
            if (hero.getHealth() < hero.getMaxHealth() && hero.getHealPotions() > 0) {
                actionQueue.enqueue(new HealCommand(hero, 20));
            }
            actionQueue.enqueue(new DefendCommand(hero, 0.15));
            result.addLine("Hero queued actions:");
            for (String desc : actionQueue.getCommandDescriptions()) {
                result.addLine("  - " + desc);
            }

            actionQueue.executeAll();
            if (opponent.isAlive()) {
                result.addLine(opponent.getName() + " attacks for " + opponent.getAttackPower() + " damage!");
                System.out.println("\n[Round " + round + "] Opponent strikes! Processing defense chain...");
                dodge.handle(opponent.getAttackPower(), hero);
            }
            result.addLine("[Round " + round + " End] Opponent HP: " + opponent.getHealth() + " | Hero HP: " + hero.getHealth());
        }
        if (hero.isAlive() && !opponent.isAlive()) {
            result.setWinner(hero.getName());
        } else if (!hero.isAlive() && opponent.isAlive()) {
            result.setWinner(opponent.getName());
        } else {
            result.setWinner("Draw (Max Rounds Reached)");
        }

        result.setRounds(round);
        return result;
    }
}