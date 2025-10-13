import Icon from "../../ui/Icon";

const CalendarNavigator = (
  focusedDate: Date,
  changeShownDate: (value: string | number | Date, mode?: "monthOffset" | "set" | "setYear" | "setMonth") => void,
) => {
  return (
    <div className="flex justify-between items-center px-1 py-0 mb-4">
      <button onClick={() => changeShownDate(-1, "monthOffset")}>
        <Icon name='icon-chevron' className='rotate-90'/>
      </button>

      <span className="body-bold text-blue-09">
        {focusedDate.toLocaleString("en-US", {month: "long", year: "numeric"})}
      </span>

      <button onClick={() => changeShownDate(1, "monthOffset")}>
        <Icon name='icon-chevron' className='rotate-270'/>
      </button>
    </div>
  );
};

export default CalendarNavigator;