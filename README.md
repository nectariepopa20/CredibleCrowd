# CredibleCrowd

CredibleCrowd is a configurable virtual-population system for Velocity, with an optional Paper bridge. It keeps one coherent snapshot across the server-list ping, ping sample, `/glist`, reserved usernames, per-backend allocation, and Paper's `/list` output.

> CredibleCrowd changes reported/displayed population. It does not create authenticated Minecraft connections, tab-list entries, world entities, network traffic, gameplay, chat, or activity statistics. Server owners should use virtual population responsibly and comply with the rules of any server list, advertising service, or jurisdiction they use.

## Compatibility

- Velocity `3.5.1` or newer, Java 21+
- Optional bridge: Paper `26.1.2` stable, Java 25+
- The two JARs are independent; do not install the Paper JAR on Velocity or vice versa.

## Installation

1. Put `CredibleCrowd-Velocity-1.0.0.jar` in Velocity's `plugins/` directory.
2. Start Velocity once, then edit `plugins/crediblecrowd/config.yml` and `names.txt`.
3. Ensure every `servers[].name` exactly matches a server name in `velocity.toml`.
4. Optional: put `CredibleCrowd-Paper-1.0.0.jar` on every Paper backend whose `/list` output should be synchronized.
5. Restart the proxy and backends. Use `/crediblecrowd reload` after later Velocity configuration edits.

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
jitter: 0.16
seed: 8743921
ping-sample-size: 12
display-mode: add-to-real
maximum-players: 500
reserve-fake-names: true
already-connected-message: "You are already connected to this proxy!"
```

Ranges are smoothly interpolated at four-hour boundaries. A slow wave and bounded jitter produce variation while a seeded time bucket keeps repeated queries consistent. Server weights are proportional probabilities, so allocation varies naturally without losing the configured popularity order.

`names.txt` accepts one Minecraft username per line. Invalid names are ignored, names currently used by real players are removed, and the virtual count is capped by the number of valid available names. With reservation enabled, login attempts using an active virtual name receive the configured already-connected message.

## Commands and permissions

- `/glist` or `/ccplayers` — combined list and per-server allocation (`crediblecrowd.glist`)
- `/crediblecrowd` — current virtual/displayed counts (`crediblecrowd.admin`)
- `/crediblecrowd reload` — reload configuration and names (`crediblecrowd.admin`)
- Paper `/ccplayers` — synchronized local population (`crediblecrowd.players`, granted by default)
- Paper `/list` — intercepted when `intercept-list-command: true` in the bridge config

## Building

```shell
mvn clean verify
```

Artifacts are created in `velocity/target/` and `paper/target/`.

## License

[MIT](LICENSE)

