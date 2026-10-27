package com.cbemorehitsplats.utils;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks hitsplats per actor. Only touched from the client thread (events and overlay rendering).
 * <p>
 * An actor's entry stays until it despawns, even with no hitsplats up, because it remembers which of the
 * game's display objects belongs to which position.
 * <p>
 * With debug logging on, also logs when a tick breaks the server's hitsplat limit that confirmed totals rely on.
 */
@Slf4j
@Singleton
public class HitsplatManager {
    private final Map<Actor, ActorHitsplats> actors = new HashMap<>();
    // The most hitsplats one actor has got in one tick, once past the server's limit
    private int mostHitsplatsInATick = ActorHitsplats.SERVER_HITSPLAT_LIMIT;

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
            // Hits went missing although the server sent fewer than its limit, so totals under it can be wrong
            int missing = actorHitsplats.missingDamageUnderLimit(tick);
            if (missing > 0)
            {
                log.debug("{} lost {} more HP than its {} hitsplats add up to, under the limit of {}",
                    describe(actor), missing, actorHitsplats.hitsplatsOnTick(tick), ActorHitsplats.SERVER_HITSPLAT_LIMIT);
            }
        }
    }

    /**
     * Logs an actor that got more hitsplats this tick than the server's limit, which would mean the limit is
     * higher. Only a new highest count is logged, so a higher limit doesn't log every tick.
     */
    public void checkServerLimit(int tick)
    {
        actors.forEach((actor, actorHitsplats) -> {
            int hitsplats = actorHitsplats.hitsplatsOnTick(tick);
            if (hitsplats > mostHitsplatsInATick)
            {
                mostHitsplatsInATick = hitsplats;
                log.debug("{} got {} hitsplats in one tick, more than the limit of {}",
                    describe(actor), hitsplats, ActorHitsplats.SERVER_HITSPLAT_LIMIT);
            }
        });
    }

    // NPCs by name; players aren't named in the log
    private static String describe(Actor actor)
    {
        return actor instanceof NPC ? actor.getName() : "A player";
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
