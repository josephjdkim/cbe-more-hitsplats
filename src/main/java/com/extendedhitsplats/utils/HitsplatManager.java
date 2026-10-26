package com.extendedhitsplats.utils;

import net.runelite.api.Actor;
import net.runelite.api.Hitsplat;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks hitsplats per actor. Only touched from the client thread (events and overlay rendering).
 */
@Singleton
public class HitsplatManager {
    private final Map<Actor, ActorHitsplats> actors = new HashMap<>();

    public void add(Actor actor, Hitsplat hitsplat, int gameCycle, int tick)
    {
        actors.computeIfAbsent(actor, a -> new ActorHitsplats()).add(hitsplat, gameCycle, tick);
    }

    public void prune(int gameCycle)
    {
        actors.values().removeIf(actorHitsplats -> actorHitsplats.prune(gameCycle));
    }

    public void remove(Actor actor)
    {
        actors.remove(actor);
    }

    public void clear()
    {
        actors.clear();
    }

    public Map<Actor, ActorHitsplats> getActors()
    {
        return actors;
    }
}
