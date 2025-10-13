import { startOfDay } from "date-fns";

export const today = startOfDay(new Date());

export const parseDate = (dateStr: string | null): Date => {
  if (!dateStr) return new Date();
  const [year, month, day] = dateStr.split("-").map(Number);
  return new Date(year, month - 1, day);
};

export const formatDateRange = (
  startDate: string | null,
  endDate: string | null,
  includeYear: boolean = false
): string => {
  if (!startDate || !endDate) return "";

  const options: Intl.DateTimeFormatOptions = {
    month: "short",
    day: "numeric",
    ...(includeYear && { year: "numeric" }),
  };

  const start = new Date(startDate).toLocaleDateString("en-US", options);
  const end = new Date(endDate).toLocaleDateString("en-US", options);

  return `${start} - ${end}`;
};

export const formatDurations = (durations: string[]): string => {
  if (!durations.length) return "";

  return durations.join(", ");
};

export const formatSelectedFilters = (
  startDate: string | null,
  endDate: string | null,
  durations: string[]
): string => {
  const dateRange = formatDateRange(startDate, endDate);
  const durationText = formatDurations(durations);

  if (dateRange && durationText) {
    return `${dateRange}, ${durationText}`;
  }
  if (dateRange) {
    return dateRange;
  }
  if (durationText) {
    return durationText;
  }
  return "Any start date, any duration";
};

export const formatToLocalDate = (
  date: Date | null | undefined
): string | null => {
  if (!date) return null;
  return date.toLocaleDateString("en-CA"); // en-CA form to "YYYY-MM-DD"
};
