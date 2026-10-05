package com.zygy.roadlegends;

/**
 * Lightweight NPC steering/state director for Road Legends.
 *
 * The structure follows common game-AI patterns such as state machines,
 * steering and hysteresis. The project remains renderer-agnostic and does
 * not pull a full game engine into the APK.
 */
public final class NpcDirector {
    public enum State {
        CALM,
        ALERT,
        CHASE
    }

    public static final class Decision {
        public final State state;
        public final float speed;
        public final float separationWeight;

        Decision(State state, float speed, float separationWeight) {
            this.state = state;
            this.speed = speed;
            this.separationWeight = separationWeight;
        }
    }

    private NpcDirector() {}

    public static Decision decide(int wanted, float distance, int tier, int type) {
        State state;
        if (wanted <= 0) {
            state = State.CALM;
        } else if (wanted == 1) {
            state = distance < 11f ? State.ALERT : State.CALM;
        } else {
            state = State.CHASE;
        }

        float base;
        switch (state) {
            case CHASE:
                base = 1.25f + type * .30f + tier * .22f;
                break;
            case ALERT:
                base = .75f + tier * .08f;
                break;
            default:
                base = .18f;
                break;
        }

        float separation = state == State.CHASE ? .32f : .18f;
        return new Decision(state, base, separation);
    }
}
