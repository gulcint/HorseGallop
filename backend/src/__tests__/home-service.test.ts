import test from "node:test";
import assert from "node:assert/strict";
import { buildHomeDashboard } from "../home-service";

test("buildHomeDashboard computes aggregates and favorite barn", () => {
  const result = buildHomeDashboard([
    {
      id: "r1",
      distanceKm: 8,
      durationMin: 40,
      calories: 220,
      barnName: "Adin Country",
      startedAtMs: Date.UTC(2026, 1, 20, 9, 0),
    },
    {
      id: "r2",
      distanceKm: 10,
      durationMin: 50,
      calories: 300,
      barnName: "Adin Country",
      startedAtMs: Date.UTC(2026, 1, 21, 10, 0),
    },
  ], 5);

  assert.equal(result.stats.totalRides, 2);
  assert.equal(result.stats.totalDistanceKm, 18);
  assert.equal(result.stats.totalDurationMin, 90);
  assert.equal(result.stats.totalCalories, 520);
  assert.equal(result.stats.favoriteBarn, "Adin Country");
  assert.equal(result.recentActivities[0]?.id, "r2");
});


test("buildHomeDashboard handles empty input", () => {
  const result = buildHomeDashboard([], 5);
  assert.equal(result.stats.totalRides, 0);
  assert.equal(result.stats.favoriteBarn, null);
  assert.equal(result.recentActivities.length, 0);
});
