# CBe More Hitsplats

The game shows at most four hitsplats on a character at once. CBe More Hitsplats shows the rest, can add up each tick's damage, and fills in your own hits when the server leaves them out in a crowd.

![CBe More Hitsplats at a crowded NPC](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/with-plugin.png)

## Features

### Every hitsplat

The game draws its own four hitsplats as usual, and the plugin draws the rest above them, continuing the game's staggered pattern. Each tick's extra hits go on the opposite side from the last tick's, so the two ticks a hitsplat stays on screen never share a side.

| Without the plugin | With the plugin |
|---|---|
| ![Only four hitsplats without the plugin](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/without-plugin.png) | ![Every hitsplat with the plugin](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/with-plugin.png) |

### Large total

Optional. Shows the damage a character took in a tick, when it took two or more hits, on a larger hitsplat below the game's four. Heals and prayer drains don't count.

On a boss with the HP bar at the top of the screen, the total comes from the HP the boss lost, so it includes hits the server didn't send. Anywhere else, the total adds up the hitsplats that arrived and is marked with a star, such as `150*`, since the server may have left some out. You can turn the star off.

![The large total below the game's four hitsplats](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/large-total.png)

### Your missing hits

In a crowd, the server sends only so many hitsplats per character each tick (20 at the Gemstone Crab), and yours can be left out. When that happens, the plugin works out your hit from the combat XP you gained and shows it under the game's left hitsplat. If your real hitsplat turns up late, it replaces the estimate.

![Your estimated hit under the game's left hitsplat](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/your-missing-hits.png)

## Settings

| Setting | Default | What it does |
|---|---|---|
| Columns Per Side | 2 | How many columns of extra hitsplats each side has, from 1 to 8 |
| Show Your Missing Hits | On | Shows your hit, worked out from your XP, when the server leaves it out |
| Large Total: Show Large Total | Off | Shows each tick's damage on a larger hitsplat below the game's four |
| Large Total: Mark Uncertain Totals | On | Adds a star to a total that only adds up the hitsplats that arrived, since it may be missing some |

| Columns Per Side: 1 | Columns Per Side: 4 |
|---|---|
| ![One column per side](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/columns-1.png) | ![Four columns per side](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/columns-4.png) |

![The plugin's settings](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/settings.png)

## Limits

- **Hits the server doesn't send can't be shown.** Apart from your own estimated hit, the plugin can only draw the hitsplats your client receives.
- **The large total counts only the hits that arrived**, except on a boss with the HP bar. That includes the Gemstone Crab, whose bar counts down to when it burrows rather than showing damage. Bosses whose HP depends on group size, such as raid bosses, also fall back to adding up the hits. The HP bar has to be turned on in the game's settings.
- **Your missing hits are estimates:**
  - Misses give no XP, so they can't be shown.
  - Several hits in one tick, from multi-hit weapons or area attacks, show as one combined number on the character you're attacking.
  - Characters that give more or less XP than usual throw the estimate off by that much. The Gemstone Crab's 12.5% XP penalty is accounted for.
  - Magic hits can be off by 1.
- **Don't use it alongside Extended Hitsplats.** Both would draw hitsplats.

## Credits

Based on [Extended Hitsplats](https://github.com/Extended-Hitsplats/extended-hitsplats) by Ferrariic, with fixes by Rose ([YeahImRose](https://github.com/YeahImRose)). The hitsplat images come from the original plugin.

Licensed under the BSD 2-Clause License. See [LICENSE](LICENSE).

## Development

- Build and run the tests: `./gradlew build`
- Run RuneLite in developer mode with the plugin loaded: `./gradlew run`. It uses a separate RuneLite profile, "More Hitsplats Dev"; pass `-PruneliteProfile=...` to use another.
