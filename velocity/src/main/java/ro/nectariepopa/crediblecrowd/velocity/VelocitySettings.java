package ro.nectariepopa.crediblecrowd.velocity;
import ro.nectariepopa.crediblecrowd.core.CrowdConfig;
public record VelocitySettings(CrowdConfig crowd, boolean addToReal, int maximumPlayers, boolean reserveNames, String deniedMessage, int fakePingMin, int fakePingMax) {}
