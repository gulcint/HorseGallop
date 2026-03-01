import test from "node:test";
import assert from "node:assert/strict";
import { parseLimit, parseOptionalDate, parseRequiredId } from "../validators";

test("parseLimit returns default when value missing", () => {
  assert.equal(parseLimit(undefined, 5, 50), 5);
});

test("parseLimit caps to max", () => {
  assert.equal(parseLimit(99, 10, 50), 50);
});

test("parseOptionalDate rejects invalid format", () => {
  assert.throws(() => parseOptionalDate("01/03/2026", "from"));
});

test("parseRequiredId rejects blank values", () => {
  assert.throws(() => parseRequiredId("   ", "id"));
});
