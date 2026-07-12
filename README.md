# CredibleCrowd

CredibleCrowd is a configurable virtual-population system for Velocity, with an optional Paper bridge. It keeps one coherent snapshot across the server-list ping, ping sample, `/glist`, reserved usernames, per-backend allocation, and Paper's `/list` output.

> CredibleCrowd changes reported/displayed population. It does not create authenticated Minecraft connections, tab-list entries, world entities, network traffic, gameplay, chat, or activity statistics. Server owners should use virtual population responsibly and comply with the rules of any server list, advertising service, or jurisdiction they use.

## Compatibility

- Velocity `3.5.1` or newer, Java 21+
- Optional bridge: Paper `26.1.2` stable, Java 25+
- The two JARs are independent; do not install the Paper JAR on Velocity or vice versa.

## Installation

1. Put `CredibleCrowd-Velocity-1.3.0.jar` in Velocity's `plugins/` directory.
2. Start Velocity once, then edit `plugins/crediblecrowd/config.yml` and `names.txt`.
3. Ensure every `servers[].name` exactly matches a server name in `velocity.toml`.
4. Optional: install [PlaceholderAPI 2.12.3+](https://github.com/PlaceholderAPI/PlaceholderAPI/releases) and [ProtocolLib 5.4.0+](https://github.com/dmulloy2/ProtocolLib/releases) on each Paper backend.
5. Put `CredibleCrowd-Paper-1.3.0.jar` on every Paper backend whose placeholders, tablist and `/list` output should be synchronized.
6. Restart the proxy and backends. Use `/crediblecrowd reload` after later Velocity configuration edits.

The Paper bridge receives its assigned names over `crediblecrowd:sync`. Standard Minecraft plugin messaging needs a real player connection as a carrier, so a backend with no connected real players receives its next snapshot when a real player connects.

## Configuration

The generated Velocity configuration contains six required ranges:

```yaml
timezone: Europe/Bucharest
periods:
  00-04: { min: 12, max: 28 }
  04-08: { min: 7, max: 18 }
  08-12: { min: 22, max: 52 }
  12-16: { min: 45, max: 90 }
  16-20: { min: 75, max: 140 }
  20-00: { min: 50, max: 105 }
servers:
  - { name: survival, weight: 55 }
  - { name: skyblock, weight: 30 }
  - { name: lobby, weight: 15 }
update-seconds: 60
activity-tick-seconds: 10
jitter: 0.16
seed: 8743921
turnover-per-minute: 0.06
quiet-tick-chance: 0.28
max-correction-ratio: 0.025
display-mode: add-to-real
maximum-players: 500
reserve-fake-names: true
already-connected-message: "You are already connected to this proxy!"
fake-ping-min: 35
fake-ping-max: 95
```

Ranges are smoothly interpolated at four-hour boundaries. A slow wave and bounded jitter produce variation while a seeded time bucket keeps repeated queries consistent. Server weights are proportional probabilities, so allocation varies naturally without losing the configured popularity order.

The simulator keeps active identities and their backend assignments between updates. `update-seconds` controls the target population's random time bucket, while `activity-tick-seconds` controls how often small join/leave batches may occur. Background churn is calculated proportionally:

```text
expected replacements per tick = current virtual players × turnover-per-minute × activity-tick-seconds / 60
```

`quiet-tick-chance` allows ticks with no background churn. `max-correction-ratio` limits how much of the population can be added or removed in one tick while catching up to the time-of-day target. Retained players are never reshuffled merely because a tick occurred.

`names.txt` accepts one Minecraft username per line. Invalid names are ignored, names currently used by real players are removed, and the virtual count is capped by the number of valid available names. With reservation enabled, login attempts using an active virtual name receive the configured already-connected message.

## Velocity commands and messages

- `/server [server]` — replaces Velocity's command; server hover uses real plus virtual counts and entries remain clickable
- `/glist` or `/ccplayers` — replaces Velocity's command with the Bungee-style per-server list and total
- `/find <player>` — reports the backend of real and virtual players
- `/ping [player]` — reports real Velocity latency or stable configured virtual latency
- `/crediblecrowd` — current virtual/displayed counts (`crediblecrowd.admin`)
- `/crediblecrowd reload` — reload configuration and names (`crediblecrowd.admin`)
- Paper `/ccplayers` — synchronized local population (`crediblecrowd.players`, granted by default)
- Paper `/list` — intercepted when `intercept-list-command: true` in the bridge config

`/server`, `/glist`, `/find`, and `/ping` are available to every command source without a permission node. All output is generated from `plugins/crediblecrowd/messages.yml` and supports MiniMessage formatting. The messages file is created automatically and is reloaded by `/crediblecrowd reload`.

For `/ping <player>`, connected real players use Velocity's actual ping and active virtual players use a deterministic value within `fake-ping-min` and `fake-ping-max`. If neither is online, CredibleCrowd queries the unauthenticated Minecraft Services profile endpoint asynchronously to recover the account's canonical casing, caches the result for six hours, and then sends the configurable offline message.

## PlaceholderAPI and tablist

- `%bungee_total%` returns the synchronized total across the Velocity network, including the virtual population.
- `%bungee_server%` returns the real plus virtual population assigned to the current Paper backend.
- `%cc_total%` returns the synchronized network total using CredibleCrowd's collision-free expansion.
- `%cc_server%` returns the current backend's real plus virtual population.
- `%cc_fake%` returns only the virtual population assigned to the current backend.
- If PlaceholderAPI's eCloud `Bungee` expansion is already installed, CredibleCrowd temporarily replaces that expansion while the bridge is enabled and restores it on shutdown.
- With `fake-tablist: true`, ProtocolLib adds every locally assigned virtual name as an individual client-side tablist row. Old rows are removed on snapshot changes and the current rows are sent to newly joined players.
- The Velocity status ping changes only its online/max count. CredibleCrowd does not supply a fake hover sample; Velocity's normal server-list behavior is preserved.

## Building

```shell
mvn clean verify
```

Artifacts are created in `velocity/target/` and `paper/target/`.

## License

[MIT](LICENSE)
