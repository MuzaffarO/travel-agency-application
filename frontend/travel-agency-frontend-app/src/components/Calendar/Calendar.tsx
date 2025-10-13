import CalendarNavigator from "../CalendarNavigator";
import { DateRange, type RangeKeyDict } from "react-date-range";
import type { ISelectedRange } from "../DateSelector/DateSelector.tsx";
import { today } from "../../dateUtils.ts";

interface CalendarProps {
  selectedRange: ISelectedRange;
  handleChange: (ranges: RangeKeyDict) => void;
  allowPastDates?: boolean;
}

const Calendar = ({
  selectedRange,
  handleChange,
  allowPastDates = false,
}: CalendarProps) => {
  return (
    <DateRange
      ranges={[selectedRange]}
      onChange={handleChange}
      showMonthAndYearPickers={false}
      showDateDisplay={false}
      moveRangeOnFirstSelection={false}
      weekdayDisplayFormat="EEEEE"
      navigatorRenderer={CalendarNavigator}
      showPreview={false}
      minDate={allowPastDates ? undefined : today}
      weekStartsOn={1}
    />
  );
};

export default Calendar;
