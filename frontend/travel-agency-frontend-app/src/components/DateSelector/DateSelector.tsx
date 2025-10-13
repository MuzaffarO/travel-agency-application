import Calendar from "../Calendar";
import DurationSelector from "../DurationSelector";
import { formatToLocalDate, parseDate } from "../../dateUtils.ts";
import type { RangeKeyDict } from "react-date-range";
import "react-date-range/dist/styles.css"; // main style file
import "react-date-range/dist/theme/default.css"; // theme css file
import "../../calendar.css";
import { useAppDispatch } from "../../store/hooks/useAppDispatch.ts";
import {
  setEndDate,
  setStartDate,
  toggleDuration,
} from "../../store/dates/datesSlice.ts";
import { useAppSelector } from "../../store/hooks/useAppSelector.ts";

export interface ISelectedRange {
  startDate: Date;
  endDate: Date;
  key: string;
}

interface DateSelectorProps {
  showDuration?: boolean;
  allowPastDates?: boolean;
}

const DateSelector = ({
  showDuration = true,
  allowPastDates = false,
}: DateSelectorProps) => {
  const dispatch = useAppDispatch();
  const { startDate, endDate, durations } = useAppSelector(
    (state) => state.dates
  );

  const selectedRange = {
    startDate: parseDate(startDate),
    endDate: parseDate(endDate),
    key: "selection",
  };

  const handleCalendarSelection = (ranges: RangeKeyDict) => {
    const { startDate, endDate } = ranges.selection;
    dispatch(setStartDate(formatToLocalDate(startDate)));
    dispatch(setEndDate(formatToLocalDate(endDate)));
  };

  const handleDurationToggle = (duration: string) => {
    dispatch(toggleDuration(duration));
  };

  return (
    <div
      className={`flex gap-4 p-4 rounded-md shadow-card bg-white absolute top-15 z-10
        ${showDuration ? "border border-grey-05 w-124 max-w-124" : " border-none"}`}
    >
      <div
        className={`flex flex-col gap-4 ${
          showDuration
            ? "pr-6 border-r border-grey-05"
            : "pr-0 border-none w-full"
        }`}
      >
        {showDuration && <h3 className="h3 text-blue-09">Start date</h3>}
        <Calendar
          handleChange={handleCalendarSelection}
          selectedRange={selectedRange}
          allowPastDates={allowPastDates}
        />
      </div>

      {showDuration && (
        <div className="flex flex-col gap-4">
          <h3 className="h3 text-blue-09">Duration</h3>
          <DurationSelector
            onDurationChange={handleDurationToggle}
            duration={durations}
          />
        </div>
      )}
    </div>
  );
};

export default DateSelector;
