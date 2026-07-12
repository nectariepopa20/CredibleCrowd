package ro.nectariepopa.crediblecrowd.core;

public record TimeRange(int min, int max) {
  public TimeRange { if (min < 0 || max < min) throw new IllegalArgumentException("Invalid range " + min + ".." + max); }
}

