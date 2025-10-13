import { forwardRef } from "react";
import type InputProps from "../../models/InputProps";

const Input = forwardRef<HTMLInputElement, InputProps>(
  (
    {
      label,
      type = "text",
      name,
      placeholder,
      maxLength,
      helperText,
      className = "",
      error,
      icon,
      onIconClick,
      onKeyDown,
      ...rest
    },
    ref
  ) => {
    return (
      <div className={`flex flex-col w-full relative ${className}`}>
        <label
          htmlFor={name}
          className="mb-1 font-bold text-[13px] text-blue-09"
        >
          {label}
        </label>

        <input
          name={name}
          type={type}
          placeholder={placeholder}
          maxLength={maxLength}
          ref={ref}
          onKeyDown={onKeyDown}
          autoComplete="username"
          className={` w-full p-4 pr-12 rounded-[8px] border border-solid placeholder:text-grey-06 focus:outline-none focus:ring-1 focus:ring-blue-05 hover:shadow-[0px_2px_10px_6px_#027EAC33] transition-shadow duration-200
    ${error ? "border-red-04" : "border-grey-05"} 
  `}
          {...rest}
        />
        {icon && (
          <span
            onClick={onIconClick}
            className="absolute right-4 top-1/2 -translate-y-1/2 text-blue-09 cursor-pointer"
          >
            {icon}
          </span>
        )}

        {error ? (
          <span className="mt-1 text-xs text-red-04">{error}</span>
        ) : helperText ? (
          Array.isArray(helperText) ? (
            <ul className="mt-1 text-xs text-grey-07 list-none">
              {helperText.map((item, index) => (
                <li key={index} className="relative pl-4 mb-1">
                  <span className="absolute left-0 top-1/2 -translate-y-1/2 w-2 h-2 bg-grey-07 rounded-full"></span>
                  {item}
                </li>
              ))}
            </ul>
          ) : (
            <span className="mt-1 text-xs text-grey-07">{helperText}</span>
          )
        ) : null}
      </div>
    );
  }
);

Input.displayName = "Input";
export default Input;
