import Icon from "../Icon";
import type RadioProps from "../../models/RadioProps.ts";

const Radio = (
  {
    name,
    value,
    isChecked,
    caption,
    onChange,
    disabled,
  }: RadioProps) => {

  const id = `${name}-${value}`;

  return (
    <label className='flex items-center gap-2 cursor-pointer text-blue-09'>
      <input
        type="radio"
        id={id}
        name={name}
        value={value}
        checked={isChecked}
        onChange={onChange}
        disabled={disabled}
        className='peer hidden'
      />

      <span className='w-5 h-5 flex items-center justify-center text-white rounded-sm border border-blue-05
        peer-checked:bg-blue-05
        '>
          <Icon name='icon-check' className='w-5'/>
      </span>

      {caption}
    </label>
  )
}

export default Radio;