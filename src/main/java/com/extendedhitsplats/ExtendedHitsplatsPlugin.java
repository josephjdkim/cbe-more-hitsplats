/*
 * Copyright (c) 2022, Ferrariic <ferrariictweet@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.extendedhitsplats;

import com.extendedhitsplats.overlays.ExtendedHitsplatsOverlay;
import com.extendedhitsplats.utils.HitsplatManager;
import com.extendedhitsplats.utils.OwnHitEstimator;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.NPCManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
	name = "Extended Hitsplats",description = "This plugin will allow you to see more than four hitsplats on a character",enabledByDefault = true, tags = {"extended", "hitsplat"}
)
public class ExtendedHitsplatsPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ExtendedHitsplatsOverlay overlay;
	@Inject
	private HitsplatManager hitsplatManager;
	@Inject
	private ExtendedHitsplatsConfig config;
	@Inject
	private NPCManager npcManager;

	// Client thread only, like the tracked hitsplats
	private final OwnHitEstimator<Actor> ownHitEstimator = new OwnHitEstimator<>();
	// Your XP as of the last tick; 0 until your stats have loaded
	private int lastHitpointsXp;
	private int lastStyleXp;
	private int lastMagicXp;
	// The NPC on the boss HP bar last tick, and its HP then
	private NPC hpBarNpc;
	private int lastHpBarHp;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		// shutDown runs on the Swing thread; the tracked hitsplats belong to the client thread
		clientThread.invokeLater(this::clear);
	}

	private void clear()
	{
		hitsplatManager.clear();
		ownHitEstimator.clear();
		lastHitpointsXp = 0;
		hpBarNpc = null;
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied){
		Actor actor = hitsplatApplied.getActor();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();
		int tick = client.getTickCount();
		hitsplatManager.add(actor, hitsplat, client.getGameCycle(), tick);

		// Hits you take are "yours" too; only hits you deal give XP
		if (hitsplat.isMine() && hitsplat.getAmount() > 0 && actor != client.getLocalPlayer()
			&& ownHitEstimator.hitsplatApplied(actor, tick)) {
			hitsplatManager.removeEstimatedOwnHit(actor);
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick){
		int gameCycle = client.getGameCycle();
		int tick = client.getTickCount();
		hitsplatManager.prune(gameCycle);

		checkXp(tick);
		for (OwnHitEstimator.Estimate<Actor> estimate : ownHitEstimator.tick(tick)) {
			hitsplatManager.addEstimatedOwnHit(estimate.target, estimate.damage, gameCycle, tick);
		}
		checkHpBar(tick);
	}

	// The boss HP bar at the top of the screen shows exact HP. What a boss lost this tick is all the damage it
	// took, including hits the server left out.
	private void checkHpBar(int tick)
	{
		NPC npc = hpBarNpc();
		int hp = client.getVarbitValue(VarbitID.HPBAR_HUD_HP);
		if (npc != null && npc == hpBarNpc && hp < lastHpBarHp) {
			hitsplatManager.setExactDamage(npc, lastHpBarHp - hp, tick);
		}
		hpBarNpc = npc;
		lastHpBarHp = hp;
	}

	// The NPC the boss HP bar shows, or null if there's none or its bar isn't the NPC's hitpoints
	private NPC hpBarNpc()
	{
		int npcId = client.getVarpValue(VarPlayerID.HPBAR_HUD_NPC);
		if (npcId == -1 || client.getVarbitValue(VarbitID.HPBAR_HUD_BOSS_DISABLED) != 0) {
			return null;
		}
		// Some bars measure something else, like the Gemstone Crab's time until it burrows
		Integer hitpoints = npcManager.getHealth(npcId);
		if (hitpoints == null || hitpoints != client.getVarbitValue(VarbitID.HPBAR_HUD_BASEHP)) {
			return null;
		}
		// The bar names a kind of NPC; take it to be the one you're attacking, or the one it was last tick
		Player localPlayer = client.getLocalPlayer();
		Actor target = localPlayer == null ? null : localPlayer.getInteracting();
		if (target instanceof NPC && isNpc((NPC) target, npcId)) {
			return (NPC) target;
		}
		return hpBarNpc != null && isNpc(hpBarNpc, npcId) ? hpBarNpc : null;
	}

	private static boolean isNpc(NPC npc, int npcId)
	{
		NPCComposition composition = npc.getComposition();
		return composition != null && composition.getId() == npcId;
	}

	// Your combat XP comes in on the tick you attack. Read once a tick, it's all the XP from that tick's hits.
	private void checkXp(int tick)
	{
		int hitpointsXp = client.getSkillExperience(Skill.HITPOINTS);
		int styleXp = client.getSkillExperience(Skill.ATTACK) + client.getSkillExperience(Skill.STRENGTH)
			+ client.getSkillExperience(Skill.DEFENCE) + client.getSkillExperience(Skill.RANGED);
		int magicXp = client.getSkillExperience(Skill.MAGIC);

		Player localPlayer = client.getLocalPlayer();
		Actor target = localPlayer == null ? null : localPlayer.getInteracting();
		if (config.estimateOwnHits() && lastHitpointsXp > 0 && hitpointsXp > lastHitpointsXp && target != null)
		{
			// Spells give XP of their own whatever they hit, so then only Hitpoints XP measures the damage
			int styleXpGained = magicXp > lastMagicXp ? 0 : styleXp - lastStyleXp;
			ownHitEstimator.xpGained(target, target.getName(), tick, hitpointsXp - lastHitpointsXp, styleXpGained);
		}

		lastHitpointsXp = hitpointsXp;
		lastStyleXp = styleXp;
		lastMagicXp = magicXp;
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned){
		hitsplatManager.remove(npcDespawned.getNpc());
		ownHitEstimator.forget(npcDespawned.getNpc());
		if (npcDespawned.getNpc() == hpBarNpc) {
			hpBarNpc = null;
		}
	}

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned playerDespawned){
		hitsplatManager.remove(playerDespawned.getPlayer());
		ownHitEstimator.forget(playerDespawned.getPlayer());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged){
		GameState state = gameStateChanged.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING) {
			clear();
		}
	}

	@Provides
	ExtendedHitsplatsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExtendedHitsplatsConfig.class);
	}
}
