# CBe More Hitsplats

The game shows at most four hitsplats on a character at once. CBe More Hitsplats shows the rest, can add up each tick's damage, and fills in your own hits when the server leaves them out in a crowd.

![CBe More Hitsplats at the Gemstone Crab](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/columns-2.png)

## Features

### Every hitsplat

The game draws its own four hitsplats as usual, and the plugin draws the rest above them, continuing the game's staggered pattern. Each tick's extra hits go on the opposite side from the last tick's, so the two ticks a hitsplat stays on screen never share a side.

| Columns per side: 2 (default) | Columns per side: 3 |
|---|---|
| ![Extra hitsplats in two columns per side](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/columns-2.png) | ![Extra hitsplats in three columns per side](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/columns-3.png) |

### Total damage hitsplat

Optional. Shows the damage a character took in a tick, when it took two or more hits, on a larger hitsplat below the game's four. Heals and prayer drains don't count.

On a boss with the HP bar at the top of the screen, the total comes from the HP the boss lost, so it includes hits the server didn't send. Anywhere else, the total adds up the hitsplats that arrived and is marked with a star, such as `150*`, since the server may have left some out. You can turn the star off.

| Mark unconfirmed totals: on | Mark unconfirmed totals: off |
|---|---|
| ![A total of 132 with a star, below the hitsplats](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/total-marked.png) | ![A total of 155 without a star, below the hitsplats](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/total-unmarked.png) |

### Your missing hits

In a crowd, the server sends only so many hitsplats per character each tick (20 at the Gemstone Crab), and yours can be left out. When that happens, the plugin works out your hit from the combat XP you gained and shows it under the game's left hitsplat. If your real hitsplat turns up late, it replaces the estimate.

| Without your hit | With your estimated hit |
|---|---|
| ![A crowded tick with none of your hits](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/missing-hits-off.png) | ![A crowded tick with your estimated hit in bright red](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/missing-hits-on.png) |

## Settings

| Section | Setting | Default | What it does |
|---|---|---|---|
| Display | Columns per side | 2 | How many columns of extra hitsplats each side has, from 1 to 8 |
| Player Hitsplat | Show your missing hits | On | Shows your hit, worked out from your XP, when the server leaves it out |
| Total Damage Hitsplat | Display total damage | Off | Shows each tick's damage on a larger hitsplat below the game's four |
| Total Damage Hitsplat | Mark unconfirmed totals | On | Adds a star to a total the boss HP bar didn't confirm, since it may be missing hits |

## Limits

- **Hits the server doesn't send can't be shown.** Apart from your own estimated hit, the plugin can only draw the hitsplats your client receives.
- **The total damage hitsplat counts only the hits that arrived**, except on a boss with the HP bar. That includes the Gemstone Crab, whose bar counts down to when it burrows rather than showing damage. Bosses whose HP depends on group size, such as raid bosses, also fall back to adding up the hits. The HP bar has to be turned on in the game's settings.
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
