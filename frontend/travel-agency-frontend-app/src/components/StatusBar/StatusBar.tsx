import Status from "../../ui/Status";

type StatusBarProps = {
  bookingStatus: string;
};

const StatusBar = ({ bookingStatus }: StatusBarProps) => {
  if (bookingStatus === "CANCELLED") {
    return (
      <div className="relative">
        <Status state="completed" label="Booked" classNames="z-4" />
        <Status state="cancelled" label="Cancelled" classNames="z-3" />
      </div>
    );
  }

  if (bookingStatus === "CREATED") {
    return (
      <div className="relative">
        <Status state="completed" label="Booked" classNames="z-4" />
        <Status state="future" label="Confirmed" classNames="z-3" />
        <Status state="future" label="Started" classNames="z-2" />
        <Status state="future" label="Finished" classNames="z-1" />
      </div>
    );
  }

  if (bookingStatus === "CONFIRMED") {
    return (
      <div className="relative">
        <Status state="completed" label="Booked" classNames="z-4" />
        <Status state="completed" label="Confirmed" classNames="z-3" />
        <Status state="future" label="Started" classNames="z-2" />
        <Status state="future" label="Finished" classNames="z-1" />
      </div>
    );
  }

  if (bookingStatus === "STARTED") {
    return (
      <div className="relative">
        <Status state="completed" label="Booked" classNames="z-4" />
        <Status state="completed" label="Confirmed" classNames="z-3" />
        <Status state="completed" label="Started" classNames="z-2" />
        <Status state="future" label="Finished" classNames="z-1" />
      </div>
    );
  }

  if (bookingStatus === "FINISHED") {
    return (
      <div className="relative">
        <Status state="completed" label="Booked" classNames="z-4" />
        <Status state="completed" label="Confirmed" classNames="z-3" />
        <Status state="completed" label="Started" classNames="z-2" />
        <Status state="completed" label="Finished" classNames="z-1" />
      </div>
    );
  }

  return (
    <div className="relative">
      <Status state="completed" label="Booked" classNames="z-4" />
      <Status state="future" label="Confirmed" classNames="z-3" />
      <Status state="future" label="Started" classNames="z-2" />
      <Status state="future" label="Finished" classNames="z-1" />
    </div>
  );
};

export default StatusBar;
