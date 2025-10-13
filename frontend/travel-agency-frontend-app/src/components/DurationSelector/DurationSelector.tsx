import {datePresets} from "./datePresets.ts";
import Checkbox from "../../ui/Checkbox";

interface DurationSelectorProps {
  duration: string[]
  onDurationChange: (preset: string) => void,
}

const DurationSelector = (
  {
    duration,
    onDurationChange
  }: DurationSelectorProps) => {

  return (
    <div>
      {datePresets.map((preset) => (
        <div key={preset.durationKey} className='p-2'>
          <Checkbox
            name="preset"
            isChecked={duration.includes(preset.durationKey)}
            onChange={() => onDurationChange(preset.durationKey)}
            value={preset.durationKey}
            caption={preset.label}
          />
        </div>
      ))}
    </div>
  )
};

export default DurationSelector;