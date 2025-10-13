export default interface RadioProps {
  name: string;
  value: string;
  isChecked: boolean;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  caption: string;
  className?: string;
  disabled?: boolean;
}