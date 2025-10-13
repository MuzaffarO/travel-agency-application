import DateSelector from "../DateSelector";
import DropdownMain from "../DropdownMain";
import { formatDateRange, formatDurations } from "../../dateUtils.ts";
import { useAppSelector } from "../../store/hooks/useAppSelector.ts";

type DateDropdownProps = {
  classNames?: string;
  showDuration?: boolean;
  noIcon?: boolean;
  customLabel?: string;
  allowPastDates?: boolean;
};

const DateDropdown = ({
  classNames = "",
  showDuration = true,
  allowPastDates = false,
  noIcon = false,
  customLabel,
}: DateDropdownProps) => {
  const { startDate, endDate, durations } = useAppSelector(
    (state) => state.dates
  );

  const label =
    startDate && endDate
      ? formatDateRange(startDate, endDate, !!customLabel) +
        (durations.length ? `, ${formatDurations(durations)}` : "")
      : customLabel || "Any start date, any duration";

  return (
    <DropdownMain
      iconName={noIcon ? undefined : "icon-calendar"}
      label={label}
      classNames={classNames}
      selector={
        <DateSelector
          showDuration={showDuration}
          allowPastDates={allowPastDates}
        />
      }
    />
  );
};

export default DateDropdown;
