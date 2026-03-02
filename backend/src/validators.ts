import { HttpsError } from "firebase-functions/v2/https";

export function parseLimit(value: unknown, defaultValue: number, maxValue: number): number {
  if (value === undefined || value === null || value === "") return defaultValue;
  if (typeof value !== "number" || Number.isNaN(value) || !Number.isFinite(value)) {
    throw new HttpsError("invalid-argument", "limit must be a number");
  }
  const normalized = Math.floor(value);
  if (normalized <= 0) {
    throw new HttpsError("invalid-argument", "limit must be greater than 0");
  }
  return Math.min(normalized, maxValue);
}

export function parseOptionalString(value: unknown, maxLen: number): string | undefined {
  if (value === undefined || value === null) return undefined;
  if (typeof value !== "string") {
    throw new HttpsError("invalid-argument", "value must be a string");
  }
  return value.trim().slice(0, maxLen);
}

export function parseRequiredId(value: unknown, fieldName: string): string {
  if (typeof value !== "string" || value.trim().length === 0) {
    throw new HttpsError("invalid-argument", `${fieldName} is required`);
  }
  return value.trim();
}

export function parseOptionalDate(value: unknown, fieldName: string): string | undefined {
  if (value === undefined || value === null || value === "") return undefined;
  if (typeof value !== "string") {
    throw new HttpsError("invalid-argument", `${fieldName} must be a string`);
  }
  const normalized = value.trim();
  if (!/^\d{4}-\d{2}-\d{2}$/.test(normalized)) {
    throw new HttpsError("invalid-argument", `${fieldName} must be yyyy-MM-dd`);
  }
  return normalized;
}

export function parseOptionalNumber(value: unknown, fieldName: string): number | undefined {
  if (value === undefined || value === null || value === "") return undefined;
  if (typeof value !== "number" || Number.isNaN(value) || !Number.isFinite(value)) {
    throw new HttpsError("invalid-argument", `${fieldName} must be a number`);
  }
  return value;
}
