# CBe More Hitsplats

OSRS renders four hitsplats at most on a character at once. `CBe More Hitsplats` draws additional hitsplats based on game data. The plugin can also show (1) each tick's total damage and (2) your own hitsplat when the server leaves it out.

## Features

### Every hitsplat

The game still draws its own four hitsplats. The plugin draws the extra ones above them, and your estimated hit and the total below. Each tick's extras go on the opposite side from the previous tick's, so hitsplats from consecutive ticks don't mix.

| 2 columns per side, the default | 3 columns per side |
|---|---|
| ![Extra hitsplats in two columns per side](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/columns-2.png) | ![Extra hitsplats in three columns per side](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/columns-3.png) |

### Total damage hitsplat

_Off by default_.

When a character takes two or more hits in a tick, a larger hitsplat below the game's four shows the total. Heals and prayer drains don't count toward it.

Total damage accuracy:
- On a boss with the HP bar at the top of the screen, the total damage value uses the HP the boss lost; this is more accurate because it can include damage from hits that may not be sent over from the server.
- Everywhere else, the total adds up the hitsplats that arrived and ends in a star (e.g. `150*`), because the server may have left some out. You can turn the star off.

| Star on | Star off |
|---|---|
| ![A total of 132 with a star, below the hitsplats](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/total-marked.png) | ![A total of 155 without a star, below the hitsplats](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/total-unmarked.png) |

### Your missing hits

In a crowd, the server sends only so many hitsplats per character each tick, 20 at the Gemstone Crab, and it can leave yours out. When it does, the plugin works out your hit from the combat XP you gained and draws it under the game's left hitsplat. If your real hitsplat arrives late, it replaces the estimate.

| Without your hit | With your estimated hit |
|---|---|
| ![A crowded tick with none of your hits](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/missing-hits-off.png) | ![A crowded tick with your estimated hit in bright red](https://github.com/josephjdkim/cbe-more-hitsplats/raw/HEAD/docs/missing-hits-on.png) |

## Settings

| Section | Setting | Default | What it does |
|---|---|---|---|
| Display | Columns per side | 2 | Sets how many columns of extra hitsplats each side has, from 1 to 8 |
| Player Hitsplat | Show your missing hits | On | Shows your hit, worked out from your XP, when the server leaves it out |
| Total Damage Hitsplat | Display total damage | Off | Shows each tick's total damage below the game's four hitsplats |
| Total Damage Hitsplat | Mark unconfirmed totals | On | Adds a star to totals the boss HP bar didn't confirm |

## Limits

- The plugin can only draw the hitsplats your client receives. Apart from your own hit, it can't recover hits the server leaves out.
- The total includes left-out hits only on a boss with the HP bar, and only for the boss you're attacking. The HP bar must be on in the game's settings. Bosses whose HP depends on group size, such as raid bosses, fall back to adding up hitsplats. So does the Gemstone Crab, whose bar counts down to when it burrows instead of tracking damage.
- Your missing hits are estimates from XP:
  - Misses give no XP, so the plugin can't show them.
  - Several hits in one tick, from multi-hit weapons or area attacks, show as one combined hit on the character you're attacking.
  - Characters with an XP bonus or penalty throw the estimate off by that amount. The plugin accounts for the Gemstone Crab's 12.5% penalty.
  - Magic hits can be off by 1.
- Don't run this plugin alongside Extended Hitsplats. Both draw hitsplats on the same characters, and they overlap.

## Credits

CBe More Hitsplats is based on [Extended Hitsplats](https://github.com/Extended-Hitsplats/extended-hitsplats) by Ferrariic, with fixes by [Rose](https://github.com/YeahImRose). The hitsplat images come from the original plugin.

It's released under the BSD 2-Clause License. See [LICENSE](LICENSE).

## Development

- `./gradlew build` builds the plugin and runs the tests.
- `./gradlew run` starts RuneLite in developer mode with the plugin loaded. It uses a separate RuneLite profile named "More Hitsplats Dev". Pass `-PruneliteProfile=<name>` to use a different one.
