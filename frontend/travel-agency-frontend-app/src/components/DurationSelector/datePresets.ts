export interface DatePreset {
  label: string;
  durationKey: string;
}

export const datePresets: DatePreset[] = [
  {
    label: "1–3 days",
    durationKey: "1-3",
  },
  {
    label: "4–7 days",
    durationKey: "4-7",
  },
  {
    label: "8–12 days",
    durationKey: "8–12",
  },
  {
    label: "13+ days",
    durationKey: "13+",
  },
];