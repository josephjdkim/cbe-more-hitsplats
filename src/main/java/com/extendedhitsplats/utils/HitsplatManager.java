package com.extendedhitsplats.utils;

import net.runelite.api.Actor;
import net.runelite.api.Hitsplat;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks hitsplats per actor. Only touched from the client thread (events and overlay rendering).
 * <p>
 * An actor's entry stays until it despawns, even with no hitsplats up, because it remembers which of the
 * game's display objects belongs to which position.
 */
@Singleton
public class HitsplatManager {
    private final Map<Actor, ActorHitsplats> actors = new HashMap<>();

    public void add(Actor actor, Hitsplat hitsplat, int gameCycle, int tick)
    {
        actors.computeIfAbsent(actor, a -> new ActorHitsplats()).add(hitsplat, gameCycle, tick);
    }

    public void addEstimatedOwnHit(Actor actor, int amount, int gameCycle, int tick)
    {
        actors.computeIfAbsent(actor, a -> new ActorHitsplats()).addEstimatedOwnHit(amount, gameCycle, tick);
    }

    public void removeEstimatedOwnHit(Actor actor)
    {
        ActorHitsplats actorHitsplats = actors.get(actor);
        if (actorHitsplats != null)
        {
            actorHitsplats.removeEstimatedOwnHit();
        }
    }

    public void setExactDamage(Actor actor, int damage, int tick)
    {
        ActorHitsplats actorHitsplats = actors.get(actor);
        if (actorHitsplats != null)
        {
            actorHitsplats.setExactDamage(damage, tick);
        }
    }

    public void prune(int gameCycle)
    {
        actors.values().forEach(actorHitsplats -> actorHitsplats.prune(gameCycle));
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
