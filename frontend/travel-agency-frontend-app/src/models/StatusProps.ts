export default interface StatusProps {
  state: "completed" | "future" | "cancelled";
  label: string;
  classNames?: string;
}