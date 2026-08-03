package com.quimimatch.managers;

public class RunnerDifficulty {

    public final float duration;
    public final float spawnInterval;
    public final float obstacleChance;
    public final float airborneChance;
    public final float contaminantChance;

    public RunnerDifficulty(
        float duration,
        float spawnInterval,
        float obstacleChance,
        float airborneChance,
        float contaminantChance
    ) {
        this.duration = duration;
        this.spawnInterval = spawnInterval;
        this.obstacleChance = obstacleChance;
        this.airborneChance = airborneChance;
        this.contaminantChance = contaminantChance;
    }

    public static RunnerDifficulty fromTarget(
        EquationUtil.RunnerTarget target
    ) {
        int totalAtoms = 0;

        for (int amount : target.neededCounts) {
            totalAtoms += amount;
        }

        float duration = Math.max(
            14f,
            Math.min(
                28f,
                11f + totalAtoms * 0.65f
            )
        );

        float spawnInterval = Math.max(
            0.45f,
            Math.min(
                0.90f,
                duration / Math.max(totalAtoms * 2.4f, 1f)
            )
        );

        float obstacleChance = Math.min(
            0.38f,
            0.22f + totalAtoms * 0.004f
        );

        float airborneChance = Math.min(
            0.42f,
            0.18f + totalAtoms * 0.006f
        );

        float contaminantChance = Math.min(
            0.32f,
            0.16f + totalAtoms * 0.005f
        );

        return new RunnerDifficulty(
            duration,
            spawnInterval,
            obstacleChance,
            airborneChance,
            contaminantChance
        );
    }
}
