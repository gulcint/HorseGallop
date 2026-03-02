import type { HomeDashboardDto, HomeRecentActivityDto } from "./contracts";

type RideRecord = {
  id: string;
  distanceKm: number;
  durationMin: number;
  calories: number;
  barnName: string;
  startedAtMs: number;
};

function toDateParts(ms: number): { dateLabel: string; timeLabel: string } {
  const date = new Date(ms);
  if (Number.isNaN(date.getTime())) {
    return { dateLabel: "", timeLabel: "" };
  }
  const dateLabel = date.toISOString().slice(0, 10);
  const timeLabel = date.toISOString().slice(11, 16);
  return { dateLabel, timeLabel };
}

export function buildHomeDashboard(records: RideRecord[], limit: number): HomeDashboardDto {
  const sorted = [...records].sort((a, b) => b.startedAtMs - a.startedAtMs);
  const recent: HomeRecentActivityDto[] = sorted.slice(0, limit).map((r) => {
    const parts = toDateParts(r.startedAtMs);
    return {
      id: r.id,
      title: r.barnName || "Ride",
      dateLabel: parts.dateLabel,
      timeLabel: parts.timeLabel,
      durationMin: r.durationMin,
      distanceKm: r.distanceKm,
    };
  });

  const totalRides = records.length;
  const totalDistanceKm = records.reduce((acc, r) => acc + r.distanceKm, 0);
  const totalDurationMin = records.reduce((acc, r) => acc + r.durationMin, 0);
  const totalCalories = records.reduce((acc, r) => acc + r.calories, 0);

  const counts = new Map<string, number>();
  records.forEach((r) => {
    const barn = r.barnName.trim();
    if (!barn) return;
    counts.set(barn, (counts.get(barn) || 0) + 1);
  });

  let favoriteBarn: string | null = null;
  let best = 0;
  counts.forEach((count, barn) => {
    if (count > best) {
      favoriteBarn = barn;
      best = count;
    }
  });

  return {
    stats: {
      totalRides,
      totalDistanceKm,
      totalDurationMin,
      totalCalories,
      favoriteBarn,
    },
    recentActivities: recent,
  };
}
